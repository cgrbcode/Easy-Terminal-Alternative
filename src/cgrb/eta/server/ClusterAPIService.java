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

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.etadrive.ETADriveServiceImpl;
import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.rmi.AESRMIConnection;
import cgrb.eta.server.rmi.ConnectionListener;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.etatype.Cluster;
import cgrb.eta.shared.etatype.ETATypeEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Input;

public class ClusterAPIService extends Thread implements ClusterService, ClusterRemoteService {
	private static ClusterAPIService instance;
	private ServerSocket socket;
	private HashMap<Integer, ClusterRemoteService> outConnections = new HashMap<>();
	private HashMap<Integer, ClusterService> inConnections = new HashMap<>();
	private HashMap<String, String> keys = new HashMap<>();
	private HashMap<Integer, String> pendingConnections = new HashMap<>();
	private HashMap<Integer,Vector<ETADriveServiceImpl>> ETADriveServices = new HashMap<>();
	
	public static ClusterAPIService getInstance() {
		return instance == null ? instance = new ClusterAPIService() : instance;
	}

	private ClusterAPIService() {
		try {
			socket = new ServerSocket(3289);
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		startConnections();
	}
	
	public Vector<ETADriveServiceImpl> getETADrives(int user){
		return ETADriveServices.get(user);
	}

	public void startConnections() {
		updateKeys();
		// get all the clusters that we have access to and reconnect to them if there are still jobs running on them and reconnect to them
		SqlManager sql = SqlManager.getInstance();
		Vector<String[]> clusters = sql.runQuery(sql.getPreparedStatement("select * from global_cluster where type=1"));
		for (String[] cluster : clusters) {
			spawnClusterConnection(cluster[1], cluster[2], Integer.parseInt(cluster[0]));
		}
	}

	public void spawnClusterConnection(final String address, final String key, final int id) {
		if (pendingConnections.containsKey(id))
			return;
		pendingConnections.put(id, "");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Socket sock;
				int reconnectAttempts = 0;
				while (true) {
					try {
						sock = new Socket(address, 3289);
						reconnectAttempts = 0;
						break;
					} catch (Exception e) {
						reconnectAttempts++;
						if (reconnectAttempts > 10) {
							pendingConnections.remove(id);
							return;
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				try {
					sock.getOutputStream().write("000000000000000000000000000000".getBytes());
					sock.getOutputStream().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				AESRMIConnection connection = new AESRMIConnection(key, sock, ClusterAPIService.this, true, null);
				connection.setListener(new ConnectionListener() {
					@Override
					public void connectionLost() {
						outConnections.remove(id);
						pendingConnections.remove(id);
						System.out.println("lost connection to server trying again in 10secs");
						spawnClusterConnection(address, key, id);
					}
				});
				outConnections.put(id, (ClusterRemoteService) connection.getService(ClusterRemoteService.class));
				pendingConnections.remove(id);
			}
		}).start();

	}

	@Override
	public void run() {
		SqlManager sql = SqlManager.getInstance();
		while (true) {
			try {
				System.out.println("Waiting for connections. clusterAPI");
				Socket client = socket.accept();
				byte[] token = new byte[30];
				System.out.println("accepted connection");
				client.getInputStream().read(token);
				String tokenString = new String(token);
				System.out.println("read token "+tokenString);
				if (tokenString.equals("000000000000000000000000000000")) {
					// this must be a Cluster to Cluster api connection; connect as so

					// in order to actually accept this connection first we must make sure the InetAddress is from the correct
					// which sucks because we are going to have to get all the global_cluster and check the address to see if it resolves to the same address
					String hostname = client.getInetAddress().getHostName();
					System.out.println(hostname);
					Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("select * from global_cluster where address=? and type=0", hostname));
					String key = null;
					int id = 0;
					if (results.size() > 0) {
						key = results.get(0)[2];
						id = Integer.parseInt(results.get(0)[0]);
					} else {
						// try to go though all the cluster entries and check the ips to see if they match
						results = sql.runQuery(sql.getPreparedStatement("select * from global_cluster where type=0"));
						String ipAddress = client.getInetAddress().getHostAddress();
						System.out.println(ipAddress);
						for (String[] cluster : results) {
							String address2 = InetAddress.getByName(cluster[1]).getHostAddress();
							if (address2.equals(ipAddress)) {
								// good we found a match break and go on creating
								key = cluster[2];
								id = Integer.parseInt(cluster[0]);
								break;
							}
						}
					}
					if (key != null) {
						// accept the connection!!
						System.out.println("accepted connection!");
						AESRMIConnection conection = new AESRMIConnection(key, id, client, this, false);
						inConnections.put(id, (ClusterService) conection.getService(ClusterService.class));
						updateKeys();
					} else {
						System.out.println("denined connection!");
					}
				} else {
					// this is a connection from a user api call connect as so
					Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("select cypher,user from user_api_connection where token=?", tokenString));
					if (results.size() > 0) {
						int user = Integer.parseInt(results.get(0)[1]);
						System.out.println(results.get(0)[1]+" connected");
						String cypher = results.get(0)[0];
						System.out.println(cypher+"=cypher");
						byte[] type = new byte[3];
						client.getInputStream().read(type);
						System.out.println(type[0]);
						if (type[0] == 0) {
							UserAPIConnection userCon = new UserAPIConnection(user);
							AESRMIConnection connection = new AESRMIConnection(cypher, client, userCon, false, null);
							userCon.setConnection(connection);
						} else if (type[0] == 1) {
							System.out.println("ETADrive Connected!");
							ETADriveServiceImpl userCon = new ETADriveServiceImpl(user);
							AESRMIConnection connection = new AESRMIConnection(cypher, client, userCon, false, null);
							userCon.setConnection(connection);
							Vector<ETADriveServiceImpl> imps = ETADriveServices.get(user);
							if(imps==null)
								imps = new Vector<>();
							imps.add(userCon);
							ETADriveServices.put(user, imps);
						}
					} else {
						client.close();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean saveOutFile(Integer jobId, String filename, byte[] bs) {
		// TODO: keep some of this info in memory so I don't have to keep looking up the info in mysql
		SqlManager sql = SqlManager.getInstance();
		Job job = sql.getJob(jobId);

		if (job != null) {
			RemoteUserService publicConnection = UserManagerService.getService().getUserService(job.getUserId());
			String ret = (String) publicConnection.saveFileBuffer(job.getWorkingDir() + "/" + filename, bs, bs.length);
			if (!ret.equals("")) {
				System.out.println("error:" + ret);
				return false;
			}
			return true;
		}
		return false;
	}

	public boolean jobStatusChange(Integer job, String status) {
		SqlManager sql = SqlManager.getInstance();
		sql.updateJobStatus(job, status);
		Job jobO = sql.getJob(job);
		if (jobO.getStatus().equals("Finished"))
			CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.REMOVED, jobO)), jobO.getUserId());
		else
			CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, jobO)), jobO.getUserId());
		return true;
	}

	@Override
	public boolean saveJobFile(Integer job, String filename, byte[] bs) {
		// TODO: keep some of this info in memory so I don't have to keep looking up the info in mysql
		SqlManager sql = SqlManager.getInstance();
		RemoteUserService publicConnection = UserManagerService.getService().getUserService(0);

		Vector<String[]> tmp = sql.runQuery(sql.getPreparedStatement("select j.working_dir from external_job e left join job j on j.id=e.local_job where e.external_job=" + job));
		if (tmp.size() > 0) {
			String ret = (String) publicConnection.saveFileBuffer(tmp.get(0)[0] + "/" + filename, bs, bs.length);
			if (!ret.equals("")) {
				System.out.println("error:" + ret);
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean startJob(Integer job) {
		SqlManager sql = SqlManager.getInstance();
		int localJob = Integer.parseInt(sql.runQuery(sql.getPreparedStatement("select local_job from external_job where external_job=" + job)).get(0)[0]);
		CommunicationImpl.getInstance().rerunJob(localJob);
		return true;
	}

	@Override
	public boolean setupJob(Job job, int globalCluster) {
		SqlManager sql = SqlManager.getInstance();
		// step 1 check if we can run this job
		// TODO: check to see if the wrapper actually exists and if the public id matches

		// step 2 create a working folder
		String publicFolder = Settings.getInstance().getSetting("publicWorkingDir").getStringValue();

		// create a random folder to put the input files and results
		RemoteUserService publicConnection = UserManagerService.getService().getUserService(0);
		if (publicConnection == null) {
			User publicUser = new User();
			publicUser.setId(0);
			publicUser.setUsername(Settings.getInstance().getSetting("publicUser").getStringValue());
			publicUser.setPassword(Settings.getInstance().getSetting("publicPassword").getStringValue());
			UserManagerService.getService().userLogedIn(publicUser);
			// delay a little then get publicConnection again
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			publicConnection = UserManagerService.getService().getUserService(0);
			if (publicConnection == null) {
				System.out.println("can't spawn the public user :(");
				return false;
			}
		}
		File workingDir = new File(publicConnection.makedir(publicFolder));
		job.setWorkingDir(workingDir.getAbsolutePath());
		// step 3 change the input paths
		Vector<Input> inputs = job.getWrapper().getInputs();
		for (Input input : inputs) {
			if (input.getType().startsWith("File")) {
				// found a file here!
				String file = input.getValue();
				if (file != null && !file.equals("")) {
					if (!file.startsWith("/")) {
						file = job.getWorkingDir() + "/" + file;
					} else {
						// this is referencing the path from the other server change it!
						File file2 = new File(file);
						file = job.getWorkingDir() + "/" + file2.getName();
					}
					input.setValue(file);
				}
			} else if (input.getType().startsWith("List:File") && input.getValue() != null && !input.getValue().equals("")) {
				String delimiter = input.getType().split("\\{")[1].split("\\}")[0];
				String[] fileA = input.getValue().split(delimiter);
				String newValue = "";
				for (String file : fileA) {
					if (file != null && !file.equals("")) {
						if (!file.startsWith("/")) {
							file = job.getWorkingDir() + "/" + file;
						} else {
							// this is referencing the path from the other server change it!
							File file2 = new File(file);
							file = job.getWorkingDir() + "/" + file2.getName();
						}
						if (!newValue.equals("")) {
							newValue += delimiter;
						}
						newValue += file;
					}
				}
				input.setValue(newValue);
			}
		}

		// step 4 save standard files to the working folder. wow that was easy
		job.setSaveStd(true);

		// step 5 remove job id and save in mysql
		int remoteJobId = job.getId();
		job.setId(0);
		job.setUserId(0);
		job.setUser("public user");
		int localJobId = sql.addJob(job);
		sql.executeUpdate(sql.getPreparedStatement("insert into external_job values (null," + localJobId + "," + remoteJobId + "," + globalCluster + ")"));

		// step 6
		return true;
	}

	public void updateKeys() {
		SqlManager sql = SqlManager.getInstance();
		Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("SELECT g.key FROM  approved_cluster g"));
		keys.clear();
		for (String[] line : results) {
			keys.put(line[0], "");
		}
	}

	public ClusterRemoteService getConnection(Cluster cluster) {
		ClusterRemoteService con = outConnections.get(cluster.getId());
		if (con == null) {
			spawnClusterConnection(cluster.getAddress(), cluster.getGlobalKey(), cluster.getId());
		}
		return con;
	}

	public ClusterService getIncomingConnection(int globalId) {
		ClusterService con = inConnections.get(globalId);
		return con;
	}

	@Override
	public boolean setupJob(String key, Job job) {
		if (keys.containsKey(key)) {
			// TODO: Change this so we actually pass in the global cluster id
			setupJob(job, 0);
		}
		return false;
	}

	@Override
	public boolean startJob(String key, int id) {
		if (keys.containsKey(key)) {
			return startJob(id);
		}
		return false;
	}

	@Override
	public boolean saveJobFile(String key, int jobId, String filename, byte[] bbuf) {
		if (keys.containsKey(key)) {
			return saveJobFile(jobId, filename, bbuf);
		}
		return false;

	}

}
