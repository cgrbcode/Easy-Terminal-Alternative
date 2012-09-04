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
package cgrb.eta.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.etamonitor.RemoteMonitorService;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.remote.etasubmit.JobService;
import cgrb.eta.server.remote.etautil.ETAUtilService;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.etatype.ETATypeEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Wrapper;

public class MethodServer extends Thread implements ETAUtilService, JobService {

	private HashMap<Integer, HashMap<String, RemoteUserService>> connections = new HashMap<Integer, HashMap<String, RemoteUserService>>();
	private HashMap<String, MethodRecivedListener> methodListeners = new HashMap<String, MethodRecivedListener>();
	private HashMap<Integer, RemoteMonitorService> jobs = new HashMap<Integer, RemoteMonitorService>();
	private ServerSocket socket;

	private MethodServer() {
		try {
			socket = new ServerSocket(3256);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addMethodListener(String method, MethodRecivedListener list) {
		methodListeners.put(method, list);
	}

	public void connected(String user, String machine) {

	}

	public void connectionLost(String user, String machine) {

	}

	public RemoteUserService getConnection(int user, String machine) {
		if (connections.get(user) == null) {
			return null;
		}
		return connections.get(user).get(machine);
	}

	/**
	 * get the first connection for the specified user
	 * 
	 * @param user the int of the user of the connection to get
	 * @return RMIConnection of the user
	 */
	public RemoteUserService getConnection(int user) {
		if (connections.get(user) == null) {
			return null;
		}
		if (getMachines(user).length == 0)
			return null;
		return connections.get(user).get(getMachines(user)[0]);
	}

	/**
	 * @param user
	 * @return String[] of machines names connections for the user
	 */
	public String[] getMachines(int user) {
		HashMap<String, RemoteUserService> cons = connections.get(user);
		if (cons != null) {
			Iterator<String> it = cons.keySet().iterator();
			String[] ret = new String[cons.keySet().size()];
			for (int i = 0; it.hasNext(); i++) {
				ret[i] = it.next();
			}
			return ret;
		}
		return new String[] {};
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.println("Waiting for connections.");
				Socket client = socket.accept();
				System.out.println("Accepted a connection from: " + client.getInetAddress());
				new RMIConnection(client, this, false);
				/*
				 * services that are connecting under this port: ETAUtil- don't really have to worry about this. ETA will never need to send anything to this ETAMonitor ETASubmit creates a JobService. first call checkuser ETAStart will call connect
				 */

			} catch (Exception e) {
			}
		}
	}

	public String fingerUser(String user) {
		try {
			Process p = Runtime.getRuntime().exec(new String[] { "finger", user });
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.contains("Name:")) {
					String ret = line.split("Name:")[1];
					r.close();
					p.destroy();
					return ret.trim();
				}
			}
			r.close();
			p.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	

	@Override
	public Job getJobForFile(String path, String hash) {
		SqlManager sql = SqlManager.getInstance();
		Vector<String[]> jobHash = sql.runQuery(sql.getPreparedStatement("select job from output_hash where hash=? and path=?", hash, path));
		if (jobHash.size() == 0) {
			jobHash = sql.runQuery(sql.getPreparedStatement("select job from output_hash where hash=?", hash));
		}
		if (jobHash.size() > 0) {
			return sql.getJob(Integer.parseInt(jobHash.get(0)[0]));
		}
		return null;
	}


	@Override
	public int checkUser(int userId, String user) {
		if (SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select id from user where id=" + userId + " and username=?", user)).size() == 0)
			return -2;
		else if (getConnection(userId) == null)
			return -1;
		return 0;
	}

	@Override
	public int createParent(int userId, String parentName, int waitFor, int parent) {
		CommunicationImpl comm = CommunicationImpl.getInstance();
		Job job = new Job();
		job.setUserId(userId);
		job.setName(parentName);
		job.setWaitingFor(waitFor);
		job.setWorkingDir("");
		job.setSpecs("");
		job.setPipeline(-1);
		job.setParent(parent);
		job.setId(SqlManager.getInstance().addJob(job));
		comm.addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.ADDED, job)), job.getUserId());
		return job.getId();
	}

	/* (non-Javadoc)
	 * @see cgrb.spatafora.pipe.start.JobService#runCmd(int, java.lang.String, java.lang.String, java.lang.String, int, int, java.lang.String, java.lang.String)
	 */
	@Override
	public int runCmd(int userId, String command, String jobOptions, String jobName, int waitFor, int parent, String workingDir, String stdOutPath) {
		RemoteUserService conn = getConnection(userId);
		CommunicationImpl comm = CommunicationImpl.getInstance();
		Wrapper wrapper = comm.getWrapperFromCMD(command.split(" "));
		Job job = new Job();
		job.setUserId(userId);
		job.setName(jobName);
		job.setSpecs(jobOptions);
		job.setWrapper(wrapper);
		job.setWaitingFor(waitFor);
		job.setParent(parent);
		job.setWorkingDir(workingDir);
		job.setStdoutPath(stdOutPath);
		job.setId(SqlManager.getInstance().addJob(job));
		comm.addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.ADDED, job)), job.getUserId());
		if (job.getWaitingFor() <= 0)
			conn.runJob(job);
		return job.getId();
	}

	@Override
	public void addNotification(int jobId, int userId) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into notification (user,job) values (" + jobId + "," + userId + ")"));
	}

	@Override
	public void sendOut(int jobId, String line) {
		if (jobs.containsKey(jobId)) {
			jobs.get(jobId).printOut(line);
		}
	}

	
}
