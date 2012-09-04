/* * Copyright 2012 Oregon State University.
 * All Rights Reserved. 
 *  
 * Permission to use, copy, modify, and distribute this software and its 
 * documentation for educational, research and non-profit purposes, without fee, 
 * and without a written agreement is hereby granted, provided that the above 
 * copyright notice, this paragraph and the following three paragraphs appear in 
 * all copies. 
 *
 * Permission to incorporate this software into commercial products may be 
 * obtained by contacting OREGON STATE UNIVERSITY Office for 
 * Commercialization and Corporate Development.
 *
 * This software program and documentation are copyrighted by OREGON STATE
 * UNIVERSITY. The software program and documentation are supplied "as is", 
 * without any accompanying services from the University. The University does 
 * not warrant that the operation of the program will be uninterrupted or errorfree. 
 * The end-user understands that the program was developed for research 
 * purposes and is advised not to rely exclusively on the program for any reason. 
 *
 * IN NO EVENT SHALL OREGON STATE UNIVERSITY BE LIABLE TO ANY PARTY 
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS 
 * SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE OREGON STATE  
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * OREGON STATE UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE AND ANY 
 * STATUTORY WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE PROVIDED 
 * HEREUNDER IS ON AN "AS IS" BASIS, AND OREGON STATE UNIVERSITY HAS 
 * NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 * ENHANCEMENTS, OR MODIFICATIONS. 
 * 
 */
package cgrb.eta.server.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

public class RMIConnection extends Thread implements InvocationHandler {
	public static final int METHOD = 1;
	public static final int RESULT = 0;
	protected Socket client = null;
	protected ConnectionListener listener;
	protected ObjectInputStream ois = null;
	protected ObjectOutputStream oos = null;
	public int recived_count = 0;
	protected Object reciveLock = new Object();
	private HashMap<Double, Object> results = new HashMap<Double, Object>();
	protected RemoteService runner;
	private boolean connected = true;
	private Object sendLock = new Object();
	public int sent_count = 0;
	private Vector<cgrb.eta.server.rmi.Method> savedMethods = new Vector<cgrb.eta.server.rmi.Method>();

	public RMIConnection(Socket clientSocket, RemoteService runner, boolean asClient) {
		client = clientSocket;
		try {
			if (asClient) {
				oos = new ObjectOutputStream(client.getOutputStream());
				ois = new ObjectInputStream(client.getInputStream());
			} else {
				ois = new ObjectInputStream(client.getInputStream());
				oos = new ObjectOutputStream(client.getOutputStream());
			}
			ois.mark(1024 * 5);
			client.getInputStream().mark(1024 * 5);
			this.runner = runner;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.start();
	}

	public RMIConnection(Socket clientSocket, RemoteService runner, boolean asClient, ConnectionListener list) {
		listener = list;
		client = clientSocket;
		try {
			if (asClient) {
				oos = new ObjectOutputStream(client.getOutputStream());
				ois = new ObjectInputStream(client.getInputStream());
			} else {
				ois = new ObjectInputStream(client.getInputStream());
				oos = new ObjectOutputStream(client.getOutputStream());
			}
			client.getInputStream().mark(1024 * 5);
			ois.mark(1024 * 5);

			this.runner = runner;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.start();
	}

	public void reconnect(Socket clientSocket, RemoteService runner, boolean asClient) {
		client = clientSocket;
		try {
			if (asClient) {
				oos = new ObjectOutputStream(client.getOutputStream());
				ois = new ObjectInputStream(client.getInputStream());
			} else {
				ois = new ObjectInputStream(client.getInputStream());
				oos = new ObjectOutputStream(client.getOutputStream());
			}
			ois.mark(1024 * 5);
			client.getInputStream().mark(1024 * 5);
			this.runner = runner;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		this.start();
	}

	public Object getObject() {
		Object ret = null;
		try {
			synchronized (reciveLock) {
				ret = ois.readUnshared();
			}
		} catch (java.io.EOFException e) {
		} catch (IOException e) {
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private Object runLock = new Object();

	// this is the thread that receives and runs methods
	@Override
	public void run() {
		setName("My RMI reciever");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		while (true) {
			Object recived = null;
			try {
				recived = getObject();
			} catch (Exception e) {
			}
			if (recived == null) {
				break;
			}
			if (recived instanceof cgrb.eta.server.rmi.Method) {
				final cgrb.eta.server.rmi.Method met = ((cgrb.eta.server.rmi.Method) recived);
				new Thread(new Runnable() {
					public void run() {
						setName("RMI Method runner "+met.getName());
						runMethodO(met);
					}
				}).start();

			} else if (recived instanceof Result) {
				Result rs = (Result) recived;
				synchronized (runLock) {
					results.put(rs.getMethodID(), rs.getResult());
				}
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (listener != null) {
			listener.connectionLost();
			connected = false;
		}
		if (!client.isConnected()) {
			if (listener != null) {
				try {
					oos.close();
					ois.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				client = null;
				connected = false;
				listener.connectionLost();
			}
		}
	}

	public void writeObject(Object obj) throws IOException {
		oos.writeUnshared(obj);
	}

	public static RMIConnection getConnection() {
		RMIConnection ret = null;
		synchronized (currentConnections) {
			ret = currentConnections.get(Thread.currentThread().getId());
		}
		return ret;
	}

	private static HashMap<Long, RMIConnection> currentConnections = new HashMap<>();

	private void release() {
		synchronized (currentConnections) {
			currentConnections.remove(Thread.currentThread().getId());
		}
	}

	private void put() {
		synchronized (currentConnections) {
			currentConnections.put(Thread.currentThread().getId(), this);
		}
	}

	private void runMethodO(cgrb.eta.server.rmi.Method method) {
		Result ret = null;
		try {
			if (method == null)
				return;
			Class<?>[] tempClasses = null;
			if (method.getArguments() != null) {
				tempClasses = new Class[method.getArguments().length];
				for (int i = 0; i < tempClasses.length; i++) {
					if (method.getArguments()[i].getClass().equals(Integer.class))
						tempClasses[i] = int.class;
					else if (method.getArguments()[i].getClass().equals(Long.class))
						tempClasses[i] = long.class;
					else
						tempClasses[i] = method.getArguments()[i].getClass();
				}
			}
			put();
			Object result;
			if (method.getArguments() != null) {
				result = runner.getClass().getMethod(method.getName(), tempClasses).invoke(runner, method.getArguments());
			} else {
				result = runner.getClass().getMethod(method.getName()).invoke(runner);
			}
			ret = new Result(result, method.getId());
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}catch(Exception e){
			System.out.println("odd error!!!!");
			e.printStackTrace();
		}finally {
			release();
		}
		if(method.getReturnType()!=null && ret==null){
			ret=new Result(null, method.getId());
		}

		if (ret != null) {
			try {
				synchronized (sendLock) {
					writeObject(ret);
					oos.flush();
					sentCount++;
					if (sentCount >= 100) {
						oos.reset();
						sentCount = 0;
					}
				}

			} catch (IOException e) {
			}
		}
	}

	private int sentCount = 0;

	private Object runMethod(cgrb.eta.server.rmi.Method method) {
		if (connected) {
			synchronized (sendLock) {
				try {
					writeObject(method);
					oos.flush();
					sentCount++;
					if (sentCount >= 100) {
						oos.reset();
						sentCount = 0;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			if (method.getReturnType() == null) {
				savedMethods.add(method);
			}
			return null;
		}
		if (method.getReturnType() == null||(""+method.getReturnType()).equals("void")) {
			return null;
		}
		while (true) {
			if (results.containsKey(method.getId())) {
				Object ret;
				synchronized (runLock) {
					ret = results.remove(method.getId());
				}
				return ret;
			}
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setListener(ConnectionListener listener) {
		this.listener = listener;
	}

	public void close() {
		try {
			ois.close();
			oos.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return runMethod(new cgrb.eta.server.rmi.Method(method.getName(), method.getReturnType(), args));
	}

	public RemoteService getService(Class<? extends RemoteService> oClass) {
		return (RemoteService) java.lang.reflect.Proxy.newProxyInstance(oClass.getClassLoader(), new Class[] { oClass }, this);
	}
}
