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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.etamonitor.MonitorService;
import cgrb.eta.server.remote.etamonitor.RemoteMonitorService;
import cgrb.eta.server.remote.etastart.RemoteETAConnectionServiceImpl;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.remote.etasubmit.ETASubmit;
import cgrb.eta.server.remote.etautil.ETAUtilService;
import cgrb.eta.server.remote.etautil.RemoteETAUtil;
import cgrb.eta.server.rmi.ConnectionListener;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.services.AuthenticationService;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;

/**
 * A singleton class that will serve as the method listener for ETAStart, ETASubmit, and ETAMonitor.
 * 
 * @author Alexander Boyd
 * 
 */
public class LocalETAConnectionServer extends Thread implements MonitorService, ETAUtilService {
	/**
	 * Used for remote user connections. Should only be used by the program ETAStart which is the java main class {@link ETAStart}
	 */
	public static final int ETA_START = 1;
	/**
	 * Used for job submition connections. Should only be used by the program ETASubmit which is the java main class {@link ETASubmit}
	 */
	public static final int ETA_SUBMIT = 2;
	/**
	 * Used for job monitoring connections. Should only be used by the program ETAMonitor which is the java main class {@link Monitor}
	 */
	public static final int ETA_MONITOR = 3;
	/**
	 * Used for job ETA util connections. Should only be used by the program ETAUtil which is the java main class {@link RemoteETAUtil}
	 */
	public static final int ETA_UTIL = 4;

	private static byte[] empty = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	/**
	 * The singleton instance of {@link LocalETAConnectionServer}
	 */
	private static LocalETAConnectionServer instance;
	private ServerSocket socket;
	private SqlManager sql;

	/**
	 * The {@link HashMap} that maps the {@link User} id to the {@link RemoteUserService} instance.
	 */
	private HashMap<Integer, RemoteUserService> userServices = new HashMap<>();

	/**
	 * The {@link HashMap} that maps the {@link Job} id to the {@link RemoteMonitorService} instance.
	 */
	private HashMap<Integer, RemoteMonitorService> monitorServices = new HashMap<>();

	/**
	 * A method to ensure a singleton instance. If instance is null create an new instance and assign it to instance
	 * 
	 * @return the instance of this class
	 */
	public static LocalETAConnectionServer getInstance() {
		return instance == null ? (instance = new LocalETAConnectionServer()) : instance;
	}

	/**
	 * Construct and setup the singleton instance. This will create the server socket on port 3265
	 */
	private LocalETAConnectionServer() {
		try {
			socket = new ServerSocket(3256);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sql = SqlManager.getInstance();
	}

	@Override
	public void run() {
		while (true) {
			try {
				// System.out.println("Waiting for connections.");
				final Socket client = socket.accept();
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							byte[] info = new byte[31];
							client.getInputStream().read(info);
							byte mode = info[0];
							info[0] = 0;
							switch (mode) {
							case ETA_START:
								etaStartConnected(client, info);
								break;
							case ETA_SUBMIT:
								etaSubmitConnected(client, info);
								break;
							case ETA_MONITOR:
								etaMonitorConnected(client, info);
								break;
							case ETA_UTIL:
								etaUtilConnected(client, info);
								break;
							default:
								client.close();
								System.out.println("old or unauthorized connection");
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
				// if (client.isConnected())
				// System.out.println("Accepted a connection from: " + client.getInetAddress());
			} catch (Exception e) {
			}
		}
	}

	/**
	 * An instance of {@link ETAUtilService} that all ETAUtil calls get passed though. Instead of creating a new instance of {@link ETAUtilService} every time a connection is established we just create one instance of it.
	 */
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

	/**
	 * This will parse the info byte array and take the 1-30 bytes off and convert that to a string and search the database for the user that owns this token.
	 * 
	 * @param info
	 *          the byte array of size 31 that contains the user token
	 * @return the {@link User} that owns the token.
	 */
	private User getUserForToken(byte[] info) {
		User user = null;
		String token = new String(info).substring(1);
		Vector<String[]> query = sql.runQuery(sql.getPreparedStatement("select u.id,u.username,u.name,u.email,u.permission from user_connection c left join user u on u.id=c.user where c.token=?", token));
		if (query.size() > 0) {
			String[] userInfo = query.get(0);
			user = new User(userInfo[2], userInfo[3], userInfo[1], Integer.parseInt(userInfo[4]));
			user.setId(Integer.parseInt(userInfo[0]));
		}
		return user;
	}

	/**
	 * Called whenever the program ETAUtil attempts to make a connection to this instance of ETA. This should always be under the local IP. Since we don't care about who is connecting and we will never need to make a call back to this there is no need to keep this instance around.
	 * 
	 * @param client
	 *          The {@link Socket} that the {@link ServerSocket} created when this connection was established.
	 * @param info
	 *          The first 31 bytes that was sent to this socket
	 */
	private void etaUtilConnected(Socket client, byte[] info) {
		new RMIConnection(client, this, false);
	}

	/**
	 * Called whenever the program ETAMonitor attempts to make a connection to this instance of ETA. This should always be under the local IP and the last 30bytes are extracted and checked against the database and checked to see if this is a valid token.
	 * 
	 * A connection should be established any time a job gets started on a machine. The idea of this is to provide a gateway to the job that is running in case the user wants to send data to job and it tells ETA that the job started on which machine and when it finishes. It also gets the exit code to
	 * check if the job finished properly.
	 * 
	 * @param client
	 *          The {@link Socket} that the {@link ServerSocket} created when this connection was established.
	 * @param info
	 *          The first 31 bytes that was sent to this socket
	 */
	private void etaMonitorConnected(Socket client, byte[] info) {
		User user = getUserForToken(info);
		if (user == null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		String jobNumber = "";
		char letter;
		try {
			System.out.println("reading job for user " + user.getName());
			while ((letter = (char) client.getInputStream().read()) != '\n' && letter > -1) {
				jobNumber += letter;
			}
			System.out.println("job #" + jobNumber);
		} catch (IOException e) {
			return;
		}
		final int job = Integer.parseInt(jobNumber);
		RMIConnection con = new RMIConnection(client, this, false, new ConnectionListener() {
			@Override
			public void connectionLost() {
				monitorServices.remove(job);
			}
		});
		monitorServices.put(job, (RemoteMonitorService) con.getService(RemoteMonitorService.class));
	}

	@Override
	public void jobStarted(int jobId, String machine) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update job set machine=? where id=" + jobId, machine));
		JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, jobId);
		evt.setChange("Running");
		CommunicationImpl.getInstance().eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
	}

	@Override
	public void jobFinished(int jobId, int exitCode) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update job set exitCode=" + exitCode + " where id=" + jobId));
		if (exitCode == 0) {
			JobEvent evt = new JobEvent(JobEvent.FINISHED, jobId);
			evt.setChange("Finished");
			CommunicationImpl.getInstance().eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		} else {
			JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, jobId);
			evt.setChange("Failed");
			CommunicationImpl.getInstance().eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		}
	}

	/**
	 * Called whenever the program ETASubmit attempts to make a connection to this instance of ETA. This should always be under the local IP and the last 30bytes are extracted and checked against the database and checked to see if this is a valid token.
	 * 
	 * @param client
	 *          The {@link Socket} that the {@link ServerSocket} created when this connection was established.
	 * @param info
	 *          The first 31 bytes that was sent to this socket
	 */
	private void etaSubmitConnected(Socket client, byte[] info) {
		User user = getUserForToken(info);
		if (user == null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}

	/**
	 * Called whenever the program ETAStart attempts to make a connection to this instance of ETA. This should always be under the local IP and the last 30bytes are extracted and checked against the database and checked to see if this is a valid token.
	 * 
	 * This provides a 2 way communication gateway for talking back and forth in order for the file io calls and other calls to be secured.
	 * 
	 * There can only ever be one connection per user so if there is already a connection deny the current one and close the socket.
	 * 
	 * @param client
	 *          The {@link Socket} that the {@link ServerSocket} created when this connection was established.
	 * @param info
	 *          The first 31 bytes that was sent to this socket
	 */
	private void etaStartConnected(Socket client, byte[] info) {
		if (Arrays.equals(info, empty)) {
			// it looks like this is a new user. get the next line and check the userName in the database.
			String userName = "";
			char letter;
			try {
				while ((letter = (char) client.getInputStream().read()) != '\n' && letter > -1) {
					userName += letter;
				}
				// see if this user is in the database yet
				Vector<String[]> query = sql.runQuery(sql.getPreparedStatement("select id from user where username=?", userName));
				int userId;
				if (query.size() > 0) {
					// cool this person isn't new. save the user id to the int userId
					userId = Integer.parseInt(query.get(0)[0]);
				} else {
					// drat this person isn't in the database. get their information and add them in
					sql.addUser(AuthenticationService.getService().getUserFullName(userName), userName, "", 0);
					userId = SqlManager.getInstance().getUserInfoFromUsername(userName).getId();
				}
				String newToken = CommunicationImpl.getInstance().generateToken();
				System.out.println(newToken + " for user " + userName);
				info = (" " + newToken).getBytes();
				sql.executeUpdate(sql.getPreparedStatement("delete from user_connection where user=" + userId));
				sql.executeUpdate(sql.getPreparedStatement("insert into user_connection values(?," + userId + ")", newToken));
				client.getOutputStream().write(newToken.getBytes());
				client.getOutputStream().flush();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}
		}
		final User user = getUserForToken(info);
		if (user == null) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		if (userServices.containsKey(user.getId())) {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		System.out.println("user " + user.getId() + " is connected");
		RMIConnection con = new RMIConnection(client, new RemoteETAConnectionServiceImpl(user.getId()), false, new ConnectionListener() {
			@Override
			public void connectionLost() {
				System.out.println("Connection lost :(");
				userServices.remove(user.getId());
				sql.executeUpdate(sql.getPreparedStatement("delete from token where user=" + user.getId()));
			}
		});
		userServices.put(user.getId(), (RemoteUserService) con.getService(RemoteUserService.class));
	}

	/**
	 * This will return the {@link RemoteUserService} for the user that is provided. If the user isn't connected this will return null.
	 * 
	 * @param user
	 *          The id of the user to get the {@link RemoteUserService} for.
	 * @return The {@link RemoteUserService} that is running as the user. <code>null</code> if the user isn't connected
	 */
	public RemoteUserService getServiceForUser(int user) {
		return userServices.get(user);
	}

	/**
	 * This will return the {@link RemoteMonitorService} for the job that is provided. If the job isn't connected this will return null.
	 * 
	 * @param job
	 *          The id of the job to get the {@link RemoteMonitorService} for.
	 * @return The {@link RemoteMonitorService} that is running under the job id. <code>null</code> if the job isn't connected
	 */
	public RemoteMonitorService getServiceForJob(int job) {
		return monitorServices.get(job);
	}

	@Override
	public Job getJob(int id) {
		return SqlManager.getInstance().getJob(id);
	}

	@Override
	public Job jobFinishedWithNext(int jobId, int exitCode) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update job set exitCode=" + exitCode + " where id=" + jobId));
		if (exitCode == 0) {
			Job jobO = null;
			SqlManager sql = SqlManager.getInstance();
			Vector<String[]> jobs = sql.runQuery(sql.getPreparedStatement("Select id from job where waiting_for=" + jobId));
			for (String[] job : jobs) {
				jobO = sql.getJob(Integer.parseInt(job[0]));
				if (jobO.getWrapper() != null) {
					break;
				}
			}
			if (jobO != null) {
				CommunicationImpl.getInstance().preventJob(jobO.getId());
			}
			JobEvent evt = new JobEvent(JobEvent.FINISHED, jobId);
			evt.setChange("Finished");
			CommunicationImpl.getInstance().eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
			return jobO;
		} else {
			JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, jobId);
			evt.setChange("Failed");
			CommunicationImpl.getInstance().eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		}
		return null;
	}

}
