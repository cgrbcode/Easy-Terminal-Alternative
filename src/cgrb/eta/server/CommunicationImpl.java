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
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.Cookie;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gwt.rpc.server.RpcServlet;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.WrapperService;
import cgrb.eta.remote.api.JobListener;
import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.ProgramManager;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.services.AuthenticationService;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.server.settings.Setting;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.LogoutEvent;
import cgrb.eta.shared.RequestEvent;
import cgrb.eta.shared.ResultSettings;
import cgrb.eta.shared.SearchResultItem;
import cgrb.eta.shared.etatype.Cluster;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.ETATypeEvent;
import cgrb.eta.shared.etatype.EWrapper;
import cgrb.eta.shared.etatype.Help;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.JobNote;
import cgrb.eta.shared.etatype.PendingCluster;
import cgrb.eta.shared.etatype.Plugin;
import cgrb.eta.shared.etatype.QJob;
import cgrb.eta.shared.etatype.RequestItem;
import cgrb.eta.shared.etatype.Share;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.etatype.UserResult;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.pipeline.PipeComponent;
import cgrb.eta.shared.pipeline.PipeWrapper;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.pipeline.PipelineWrapper;
import cgrb.eta.shared.pipeline.UserPipeline;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;
/**
 * The server implementation of methods that our application might want to run.
 * 
 * Every method in the CommunicationServer interface needs to be implemented here also, every method in here should be prototyped in the CommunicationService interface.
 * Alternatively, there is an AsyncCommuncationService which is similar but is asynchronous. That interface is preferred.
 * 
 * @author Alexander Boyd
 *
 */
public class CommunicationImpl extends RpcServlet implements CommunicationService, SQLService, WrapperService, EventOccuredListener {

	private static final long serialVersionUID = 1L;
	private HashMap<String, Vector<ETAEvent>> events = new HashMap<String, Vector<ETAEvent>>();
	private HashMap<Integer, Vector<String>> userTokens = new HashMap<Integer, Vector<String>>();
	private HashMap<String, java.util.Date> eventsLastGotten = new HashMap<String, java.util.Date>();
	private HashMap<Integer, Vector<String>> jobEventListeners = new HashMap<Integer, Vector<String>>();
	private HashMap<Integer, Date> userLastLoginDates = new HashMap<Integer, Date>();
	private AuthenticationService authService = AuthenticationService.getService();
	private UserManagerService userManager = UserManagerService.getService();
	private Object eventLock = new Object();
	private ClusterAPIService apiService;
	private HashMap<Integer, JobListener> jobListeners = new HashMap<>();
	private HashMap<Integer, Integer> preventJobs = new HashMap<>();

	static {
		System.out.println("getting communication");
	}
	private static CommunicationImpl instance = null;

	public static CommunicationImpl getInstance() {
		return instance;
	}

	public CommunicationImpl() {
		apiService = ClusterAPIService.getInstance();
		CommunicationImpl.instance = this;
	}

	private String getToken() {
		Cookie[] cookies = this.getThreadLocalRequest().getCookies();
		if (cookies != null) {
			for (Cookie cook : cookies) {
				if (cook.getName().equals("token")) {
					return cook.getValue();
				}
			}
		}
		Cookie cook = new Cookie("token", generateToken());
		this.getThreadLocalResponse().addCookie(cook);
		return cook.getValue();
	}

	public void preventJob(int jobId) {
		preventJobs.put(jobId, jobId);
	}

	public String generateToken() {
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		int string_length = 30;
		String randomstring = "";
		for (int i = 0; i < string_length; i++) {
			int rnum = (int) Math.floor(Math.random() * chars.length());
			randomstring += chars.substring(rnum, rnum + 1);
		}
		return randomstring;
	}

	public User getUser() {
		User ret = SqlManager.getInstance().getUser(getToken());
		if (ret == null) {
			return null;
		}
		if (userManager.getUserService(ret.getId()) != null) {
			ret.setServiceConnected(true);
		} else {
			ret.setServiceConnected(false);
			Date lastAtempt = userLastLoginDates.get(ret.getId());
			if (lastAtempt == null || (new Date().getTime() - lastAtempt.getTime()) > 30 * 1000) {
				return null;
			}
		}
		return ret;
	}

	public void addJobListener(int job, JobListener listener) {
		jobListeners.put(job, listener);
	}

	public void addEvent(ETAEvent event, int user) {
		if (event.getType() == ETAEvent.JOB) {
			JobEvent jEvent = (JobEvent) event.getSource();
			Job job = new Job();
			job.setId(jEvent.getJob());
			int jobId = jEvent.getJob();
			int type = jEvent.getType();
			SqlManager sql = SqlManager.getInstance();
			int globalId = 0;
			int externalJob = 0;
			if (user == 0) {
				Vector<String[]> temp = sql.runQuery(sql.getPreparedStatement("select * from external_job e where e.local_job=" + jobId));
				if (temp.size() > 0) {
					globalId = Integer.parseInt(temp.get(0)[3]);
					if (apiService.getIncomingConnection(globalId) == null)
						globalId = 0;
					// TODO: put it in a queue so we can finish these events when the connection is made again
					externalJob = Integer.parseInt(temp.get(0)[2]);
				}
			}
			JobListener listener = jobListeners.get(jobId);
			if (listener != null) {
				listener.jobStatusChanged(jEvent.getChange());
			}
			switch (type) {
			case JobEvent.CANCELLED:
				sql.updateJobStatus(jobId, "Cancelled");
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, SqlManager.getInstance().getJob(jobId))), user);
				if (globalId > 0)
					apiService.getIncomingConnection(globalId).jobStatusChange(externalJob, "Cancelled");
				break;
			case JobEvent.FINISHED:
				sql.updateJobStatus(jobId, "Finished");
				if (globalId > 0) {
					// now we need to copy the output files back over
					externalJobFinished(job.getId(), externalJob, globalId);
				}
				jobListeners.remove(jobId);
				job = sql.getJob(job.getId());
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.REMOVED, job)), user);
				jobFinished(jobId);
				runNextJobs(jobId);
				break;
			case JobEvent.STATUS_CHANGED:
				sql.updateJobStatus(jobId, jEvent.getChange());
				sql.getJob(jobId);
				if (globalId > 0)
					apiService.getIncomingConnection(globalId).jobStatusChange(externalJob, jEvent.getChange());
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, SqlManager.getInstance().getJob(jobId))), user);
				break;
			}
		} else {
			synchronized (eventLock) {
				if (user == -1) {
					Iterator<String> ids = events.keySet().iterator();
					while (ids.hasNext()) {
						String id = ids.next();
						Vector<ETAEvent> temp = events.get(id);
						if (temp == null) {
							temp = new Vector<ETAEvent>();
						}
						temp.add(event);
						events.put(id, temp);
					}
				} else {
					Vector<String> tokens = userTokens.get(user);
					if (tokens == null)
						return;
					for (String id : tokens) {
						Vector<ETAEvent> temp = events.get(id);
						if (temp == null) {
							temp = new Vector<ETAEvent>();
						}
						temp.add(event);
						events.put(id, temp);
					}
				}
			}
		}
	}

	private void jobFinished(final int jobId) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SqlManager sql = SqlManager.getInstance();
				Job job = sql.getJob(jobId);
				Vector<cgrb.eta.shared.etatype.File> outs = job.getOutputFiles();
				Vector<Output> outputs = job.getWrapper().getOutputs();
				final RemoteUserService localConnection = userManager.getUserService(job.getUserId());
				for (int i = 2; i < outs.size(); i++) {
					cgrb.eta.shared.etatype.File file = outs.get(i);
					String hash = localConnection.hashFile(file);
					if (hash != null && !hash.equals("")) {
						sql.executeUpdate(sql.getPreparedStatement("insert into output_hash values (null," + jobId + ",?,?," + outputs.get(i - 2).getId() + ")", file.getPath(), hash));
					}
				}
			}
		}).start();
	}

	private void externalJobFinished(final int localJobId, final int externalJob, int globalId) {
		// copy over the output files for this job

		final ClusterService con = apiService.getIncomingConnection(globalId);
		final RemoteUserService localConnection = userManager.getUserService(0);
		if (con == null) {
			System.out.println("couldn't connect to cluster " + globalId);
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				SqlManager sql = SqlManager.getInstance();
				Job job = sql.getJob(localJobId);
				Vector<cgrb.eta.shared.etatype.File> outs = job.getOutputFiles();
				for (cgrb.eta.shared.etatype.File file : outs) {
					System.out.println("sending file " + file.getPath() + " to " + new File(file.getPath()).getName());
					byte[] bbuf;
					con.jobStatusChange(externalJob, "Copying output files back");
					do {
						bbuf = localConnection.getFileBuffer(file.getPath());
						con.saveJobFile(externalJob, new File(file.getPath()).getName(), bbuf);
					} while (bbuf.length > 0);
				}
				con.jobStatusChange(externalJob, "Finished");
			}
		}).start();
	}

	/**
	 * @param jobId
	 */
	public void runNextJobs(int jobId) {
		SqlManager sql = SqlManager.getInstance();
		Vector<String[]> jobs = sql.runQuery(sql.getPreparedStatement("Select id from job where waiting_for=" + jobId));
		for (String[] job : jobs) {
			Job jobO = sql.getJob(Integer.parseInt(job[0]));
			if (jobO.getWrapper() == null) {
				// hmm this is a pipeline we should start its children
				if (jobO.getPipeline() > 0) {
					Pipeline pipe = jobO.getPipelineObject();
					for (Input in : pipe.getInputs()) {
						System.out.println(in.getName() + " " + in.getValue());
					}
					if (pipe.getName().equals("Foreach") && pipe.getInputs().get(0).getValue().startsWith("${")) {
						Vector<String[]> vars = sql.runQuery(sql.getPreparedStatement("select name,value from job_heap where job=" + jobO.getId()));
						int i = 1;
						for (String[] arr : vars) {
							pipe.addInput(new Input(i++, "", arr[0], arr[1], "", false, 1, "", ""));
						}
						jobO.setPipeline(pipe);
						runPipeline(jobO);
					}
				}
			} else {
				if (preventJobs.containsKey(jobO.getId())) {
					preventJobs.remove(jobO);
				} else {
					jobO.getWrapper().addVar("ETAJOB", jobId + "");
					jobO.getWrapper().addVar("ETAHOST", System.getenv("HOSTNAME"));
					userManager.getUserService(jobO.getUserId()).runJob(jobO);
				}
			}
		}
	}

	public void rerunJob(int job) {
		Job jobO = getJob(job);
		jobO.getWrapper().addVar("ETAJOB", job + "");
		jobO.getWrapper().addVar("ETAHOST", System.getenv("HOSTNAME"));
		userManager.getUserService(jobO.getUserId()).runJob(jobO);
	}

	public int addFolder(String name) {
		User user = getUser();
		if (user == null)
			return 0;
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into wrappers (parent,user,name,wrapper) values (0," + getUser().getId() + ",?,0) ", name));
		UserWrapper wrapper = new UserWrapper(name, 0, "", false, 0, id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.ADDED, wrapper)), user.getId());
		return id;
	}

	public int addUser(String name, String userName, String password, int level) {
		return SqlManager.getInstance().addUser(name, userName, password, level);
	}

	public boolean logIn(final String user, final String password) {

		if (password == null || password.length() == 0) {
			return false;
		}

		if (authService.checkCredentials(user, password)) {
			User userO = SqlManager.getInstance().getUserInfoFromUsername(user);
			if (userO == null) {
				SqlManager.getInstance().addUser(authService.getUserFullName(user), user, "", 0);
				userO = SqlManager.getInstance().getUserInfoFromUsername(user);
			}
			userO.setPassword(password);
			if (userManager.userLogedIn(userO)) {
				userLastLoginDates.put(userO.getId(), new Date());
			}
			SqlManager.getInstance().executeUpdate(
					SqlManager.getInstance().getPreparedStatement("insert into token (token,user,ip) values(?," + userO.getId() + ",?) on duplicate key update user=" + userO.getId() + ", ip=?", getToken(), getThreadLocalRequest().getRemoteAddr(), getThreadLocalRequest().getRemoteAddr()));
			return true;
		}
		return false;
	} 

	public String changePassword(String oldPassword, String newPassword) {
		User user = getUser();
		if (user == null)
			return "No one is logged in.";
		String userName = user.getUsername();
		if (!authService.checkCredentials(userName, oldPassword)) {
			return "Incorrect old password.";
		} else {
			String returned = authService.changePassword(userName, oldPassword, newPassword);
			return returned;
		}
	}

	public void deleteJob(int id) {
		User user = getUser();
		if (user == null)
			return;
		SqlManager sql = SqlManager.getInstance();
		Job jobO = sql.getJob(id);
		if (jobO.getPipeline() > 0) {
			ArrayList<Job> children = sql.getChildJobs(id);
			for (Job jo : children) {
				deleteJob(jo.getId());
			}
		}
		sql.executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job where id=" + id));
		sql.executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_value where job=" + id));
		sql.executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_hash where job=" + id));
		sql.executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_heap where job=" + id));
		if (jobO.getParent() == 0) {
			Job job = new Job();
			job.setId(id);
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.REMOVED, job)), user.getId());
		}
		deleteSTDJobFiles(id);
	}

	public boolean doesWrapperExist(int user, boolean isPublic, String name) {
		return SqlManager.getInstance().doesCommandExist(user, isPublic, name);
	}

	public void eventOccured(ETAEvent event, int user) {
		addEvent(event, user);
	}

	public String[] getCommandsInPath() {
		User user = getUser();
		if (user == null)
			return null;
		RemoteUserService con = userManager.getUserService(user.getId());
		if (con != null) {
			return con.getCommandsInPath();
		}
		return new String[] {};
	}

	public synchronized Vector<ETAEvent> getEvents() {
		String id = getToken();
		User user2 = getUser();
		if (user2 == null)
			return null;
		int user = user2.getId();
		Vector<ETAEvent> temp;
		synchronized (eventLock) {
			temp = events.get(id);
			events.put(id, new Vector<ETAEvent>());
		}
		Vector<String> tokens2 = userTokens.get(user);
		if (tokens2 == null) {
			tokens2 = new Vector<String>();
			userTokens.put(user, tokens2);
		}
		if (!tokens2.contains(id)) {
			tokens2.add(id);
			userTokens.put(user, tokens2);
		}
		java.util.Date curr = new java.util.Date();
		eventsLastGotten.put(id, curr);
		String remove = null;
		Iterator<String> it = eventsLastGotten.keySet().iterator();
		while (it.hasNext()) {
			String idd = it.next();
			java.util.Date evDate = eventsLastGotten.get(idd);
			if (evDate.getTime() + 5000 < curr.getTime()) {
				remove = idd;
				synchronized (eventLock) {
					events.remove(idd);
				}
				Vector<String> tokens = userTokens.get(user);
				if (tokens != null) {
					tokens.remove(idd);
					userTokens.put(user, tokens);
				}
			}
		}
		eventsLastGotten.remove(remove);
		if (temp != null && temp.size() > 0) {
			return temp;
		} else {
			return new Vector<ETAEvent>();
		}
	}

	public HashMap<String, String> getFavorites() {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getFavorites(user.getId());
	}

	public Vector<Job> getJobs() {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getJobs(user.getId());
	}

	public Vector<QJob> getJobsForMachine(String machine) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();// qstat -u "*" -q *@plant0.cgrb.oregonstate.local -xml -s r
			ProcessBuilder pb = new ProcessBuilder("qstat", "-u", "*", "-q", "*@" + machine, "-xml", "-s", "r");
			pb.redirectErrorStream(true);
			Process p = pb.start();
			Document dom = db.parse(p.getInputStream());
			Element el = dom.getDocumentElement();

			NodeList nlist = el.getElementsByTagName("queue_info");
			NodeList nl = ((Element) nlist.item(0)).getElementsByTagName("job_list");
			if (nl != null && nl.getLength() > 0) {
				Vector<QJob> ret = new Vector<QJob>();
				for (int i = 0; i < nl.getLength(); i++) {
					Element element = (Element) nl.item(i);
					ret.add(new QJob(element.getElementsByTagName("JB_name").item(0).getTextContent(), Integer.parseInt(element.getElementsByTagName("slots").item(0).getTextContent()), element.getElementsByTagName("JB_owner").item(0).getTextContent(), Integer.parseInt(element
							.getElementsByTagName("JB_job_number").item(0).getTextContent())));
				}
				p.destroy();
				return ret;
			}
		} catch (ParserConfigurationException e) {
		} catch (IOException e) {
		} catch (SAXException e) {
		}

		return new Vector<QJob>();// QstatDataService.getService().getJobsForMachine(machine);
	}

	public String[][] getResourcesForMachine(String machine, String[] resources) {
		return new String[][] {};// QstatDataService.getService().getResourcesForMachine(machine, resources);
	}

	public String[] getUserInfo(String token) {
		String[] temp = SqlManager.getInstance().getUserInfo(token);
		return temp;
	}

	public Vector<UserWrapper> getUsersWrappers() {
		User user = getUser();
		if (user == null)
			return null;
		if (userManager.getUserService(user.getId()) != null) {
			Vector<UserWrapper> temp = userManager.getUserService(user.getId()).getUserWrappers();
			if (temp != null)
				return temp;
		}
		return SqlManager.getInstance().getUsersWrappers(user.getId());
	}

	public boolean isInstalled(int user, String machine, String program) {
		if (user == 0 || machine.equals("")) {
			return ProgramManager.getInstance().isInstalled(program);
		}
		return false;
	}

	public int moveWrapper(int id, int oldParent, int newParent, String name, int wrapperId) {
		if (oldParent == newParent && id != 0)
			return 0;
		User user = getUser();
		if (user == null)
			return 0;
		UserWrapper wrapper = new UserWrapper(name, wrapperId, "", false, newParent, id);
		if (id == 0) {// assume that the wrapper being moved is new
			int newId = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into wrappers (parent,user,name,wrapper) values (" + newParent + "," + user.getId() + ",?," + wrapperId + ") ", name));
			wrapper.setId(newId);
			String[] temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select name,public from wrapper where id=" + wrapperId)).get(0);
			wrapper.setName(temp[0]);
			wrapper.setPublic(temp[1].equals("true"));
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.ADDED, wrapper)), user.getId());
			return newId;
		} else {
			if (newParent == -1) {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.REMOVED, wrapper)), user.getId());
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from wrappers where id=" + id));
			} else if (newParent == -2) {
				SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from wrapper where id=" + wrapperId));
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.REMOVED, wrapper)), user.getId());
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from wrappers where command=" + wrapperId));
			} else {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.UPDATED, wrapper)), user.getId());
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update wrappers set parent=" + newParent + " where id=" + id));
			}
		}
	}

	// public String runCommand(String[] command, String workingDir) {
	// User user = getUser();
	// if (user == null)
	// return null;
	// if (workingDir == null)
	// workingDir = "";
	// RemoteUserService con = userManager.getUserService(user.getId());
	// if (con != null) {
	// System.out.println("running command " + command[0]);
	// return (String) con.runSystemCommand(command, workingDir);
	// }
	// return "";
	// }

	public void runQmod(Vector<String> jobs, String command, int user, String machine) {
		RemoteUserService con = userManager.getUserService(user);
		if (con != null) {
			userManager.getUserService(user).runQmod(jobs, command);
		}
	}

	public int saveFavorite(int type, String value, String name) {
		return SqlManager.getInstance().saveFavorite(getUser().getId(), type, value, name);
	}

	public void saveUser(String name, int user, String password, String email, String phone, String useEmail, String usePhone) {
		SqlManager.getInstance().saveUser(name, user, password, email, phone, useEmail, usePhone);
		// save the email to the users ~/.forward file
		if (email != null && email.contains("@")) {
			if (userManager.getUserService(user) != null) {
				userManager.getUserService(user).saveEmail(email);
			}
		}
	}

	public Wrapper saveWrapper(Wrapper wrapper) {
		boolean addToWrappers = (wrapper.getId() == 0);
		Wrapper updatedWrapper = SqlManager.getInstance().saveWrapper(wrapper);
		if (addToWrappers)
			moveWrapper(0, 0, 0, wrapper.getName(), updatedWrapper.getId());
		return updatedWrapper;
	}

	public String setSetting(String name, String value) {
		return null;
	}

	public String setUpAuth(boolean local, boolean mysql) {
		Settings.getInstance().putSetting("localAuth", new Setting(local));
		return Settings.getInstance().putSetting("mysqlAuth", new Setting(mysql));
	}

	public void makePublic(int wrapper) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update wrapper set public=true where id=" + wrapper));
	}

	public String[] getFileTypes() {
		return SqlManager.getInstance().getFileTypes();
	}

	public boolean saveFileType(String type) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into filetype values (?)", type));
		return true;
	}

	public Vector<Plugin> getPlugins() {
		return SqlManager.getInstance().getPlugins();
	}

	public Vector<Plugin> getPlugins(String type) {
		return SqlManager.getInstance().getPlugins();
	}

	public void removePlugin(int id) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from plugin where id=" + id));
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from plugin_filetype where id=" + id));
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from plugin_permission where id=" + id));

		Plugin temp = new Plugin();
		temp.setId(id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Plugin>(ETATypeEvent.REMOVED, temp)), -1);

	}

	public String startSession(int plugin, String file) {
		User user = getUser();
		if (user == null)
			return null;
		Random r = new Random();
		String token = Long.toString(Math.abs(r.nextLong()), 36);
		PluginSession session = new PluginSession(user.getId(), token, plugin, file, "0");
		session = PluginService.getInstance().addSession(session);
		return session.getId();
	}

	public String startSession(String url, String file) {
		User user = getUser();
		if (user == null)
			return null;
		Random r = new Random();
		String token = Long.toString(Math.abs(r.nextLong()), 36);
		PluginSession session = new PluginSession(user.getId(), token, url, file, "0");
		session = PluginService.getInstance().addTempSession(session);
		return token;
	}

	public void saveSession(String token, String password) {

	}

	public void listenForJob(int job, String id) {
		synchronized (jobEventListeners) {
			Vector<String> jobListeners = jobEventListeners.get(job);
			if (jobListeners == null) {
				jobListeners = new Vector<String>();
			}
			jobListeners.add(id);
			jobEventListeners.put(job, jobListeners);
		}
	}

	public void stopListeningJob(int job, String id) {
		synchronized (jobEventListeners) {
			Vector<String> jobListeners = jobEventListeners.get(job);
			if (jobListeners != null) {
				jobListeners.remove(id);
				jobEventListeners.put(job, jobListeners);
			}
		}
	}

	public String cancelJob(int job) {
		User user2 = getUser();
		if (user2 == null)
			return null;
		int user = user2.getId();
		RemoteUserService con = userManager.getUserService(user);
		if (con != null) {
			String temp = (String) con.cancelJob(job);
			if (temp.equals("Lost control of job")) {
				SqlManager.getInstance().updateJobStatus(job, "Cancelled");
			}
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, SqlManager.getInstance().getJob(job))), user);
			return temp;
		}
		return "";
	}

	public Vector<User> getUsers() {
		return SqlManager.getInstance().getUsers();
	}

	public ResultSettings getResultSettings(String sessionId) {
		return SqlManager.getInstance().getResultSettings(sessionId);
	}

	public ResultSettings saveResultSetting(ResultSettings result) {
		if (result.getResultId() == 0) {
			// the result is new so add it to the db
			if (SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select * from result where url=?", result.getUrl())).size() == 0) {
				int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into result values (null,?," + result.getSessionId() + "," + (result.isPublic() ? 1 : 0) + ")", result.getUrl()));
				result.setResultId(id);
				SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update session set public=" + (result.isPublic() ? 1 : 0) + " where id=" + result.getSessionId()));
				Iterator<Integer> it = result.getUsers().keySet().iterator();
				while (it.hasNext()) {
					SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into shares values (null," + it.next() + "," + result.getSessionId() + ")"));
				}
			}
		} else {
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update session set public=" + (result.isPublic() ? 1 : 0) + " where id=" + result.getSessionId()));
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from shares where session=" + result.getSessionId()));
			Iterator<Integer> it = result.getUsers().keySet().iterator();
			while (it.hasNext()) {
				SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into shares values (null," + it.next() + "," + result.getSessionId() + ")"));
			}
		}
		return result;
	}

	public Vector<UserResult> getUserResults() {
		return SqlManager.getInstance().getUsersResults(getUser().getId());
	}

	public int addResultFolder(String value) {
		User user = getUser();
		if (user == null)
			return 0;
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into jobs (parent,user,name,job) values (0," + user.getId() + ",?,0) ", value));
		UserResult res = new UserResult(user.getId(), 0, value, 0, id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.ADDED, res)), user.getId());
		return id;
	}

	public int moveResult(int id, int oldParent, int newParent, String name, int jobId) {
		int user = getUser().getId();
		if (id == 0) {// assume that the job being moved is new
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.ADDED, new UserResult(user, newParent, name, jobId, id))), user);
			return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into jobs (parent,user,name,job) values (" + newParent + "," + user + ",?," + jobId + ") ", name));
		} else {
			if (newParent == -1) {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.REMOVED, new UserResult(user, 0, name, 0, id))), user);

				if (SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("SELECT id FROM jobs js where js.job=" + jobId)).size() == 1) {
					SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_value where job=" + jobId));
					SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_note where job=" + jobId));
					deleteSTDJobFiles(jobId);
				}
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from jobs where id=" + id));
			} else if (newParent == -2) {
				SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job where id=" + jobId));
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from jobs where job=" + id));
			} else {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.UPDATED, new UserResult(user, newParent, name, jobId, id))), user);
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update jobs set parent=" + newParent + " where id=" + id));
			}
		}
	}

	private void deleteSTDJobFiles(int jobId) {
	}

	public void deleteResult(int resultId, int jobId) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from jobs where id=" + resultId));
		if (SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select * from jobs where job=" + jobId)).size() == 0) {
			String user = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("Select user from job where id=" + jobId)).get(0)[0];
			int userInt = Integer.parseInt(user);
			if (userManager.getUserService(userInt) != null) {
				userManager.getUserService(userInt).removeResult(jobId);
			}
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job where id=" + jobId));
		}

	}

	public HashMap<String, Vector<SearchResultItem>> getSearchResults(String search) {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getSearchResults(search, user.getId());
	}

	public void changeResultName(String id, String newVal) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update jobs set name=? where id=" + id, newVal));
	}

	public Wrapper getWrapperFromId(int id) {
		return SqlManager.getInstance().getWrapperFromId(id);
	}

	public Job getJob(int job) {
		return SqlManager.getInstance().getJob(job);
	}

	public ResultSettings getJobResultSettings(int parseInt, int userId) {
		return SqlManager.getInstance().getJobResultSettings(parseInt, userId);
	}

	public void addShareJobResult(int user, int job) {
		SqlManager.getInstance().addShareResult(user, job);

	}

	public void removeShareJobResult(int user, int job) {
		SqlManager.getInstance().removeShareResult(user, job);
	}

	public void addNotification(int user, int job) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into notification values(null," + user + "," + job + ")"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#getUsersShares(int)
	 */
	public Vector<String[]> getUsersShares(int user) {
		// select session,file from session where creator=id
		// select distinct js.job,j.name from job j left join jobs js on js.job=j.id where j.user="+user+" and js.user!="+user+" group by js.job
		Vector<String[]> ret = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select id,file,'file' from session where creator=" + user));
		ret.addAll(SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select distinct js.job,j.name,'result' from job j left join jobs js on js.job=j.id where j.user=" + user + " and js.user!=" + user + " group by js.job")));
		return ret;
	}

	public Vector<String[]> getOthersShares(int user) {
		Vector<String[]> ret = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select r.id,sh.file,r.url,'file' from result r left join session sh on r.session=sh.id left join shares s on s.session=sh.id where s.user=" + user));
		ret.addAll(SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select js.job,j.name,u.name,'result' from job j left join jobs js on js.job=j.id left join user u on u.id=j.user where j.user!=" + user + " and js.user=" + user + " group by js.job")));
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#associateToken(java.lang.String, java.lang.String)
	 */
	public void associateToken(String external, String site) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into external_token (token,external_token,site) select id,?,? from token t where token=? on duplicate key update token=t.id", external, site, getToken()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#removeToken(java.lang.String)
	 */
	public void removeToken(String token) {
		int tokenId = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from token where token=?", token));
		Vector<ETAEvent> events2 = events.get(token);
		if (events2 != null) {
			events2.add(new ETAEvent(ETAEvent.LOGOUT, new LogoutEvent()));
		}
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from external_token where token=" + tokenId));
	}

	public void removeTokens(int user) {
		addEvent(new ETAEvent(ETAEvent.LOGOUT, new LogoutEvent()), user);
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from token where user=" + user));
	}

	public void removeTokens() {
		User user = getUser();
		if (user == null)
			return;
		addEvent(new ETAEvent(ETAEvent.LOGOUT, new LogoutEvent()), user.getId());
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from token where user=" + user.getId()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#getRequests()
	 * 
	 * @gwt.typeArgs <cgrb.eta.shared.RequestItem>
	 */
	public Vector<RequestItem> getRequests() {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getRequests(user.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#makeRequest(java.lang.String, int)
	 */
	public void makeRequest(String request, int user) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into request values (null," + user + ",?,'','Submitted')", request));
		String supportList = Settings.getInstance().getSetting("supportList").getStringValue();
		if (supportList.contains("@")) {
			Notifier.sendEmail("eta@cgrb.oregonstate.edu", supportList, "Request Posted", "A new request was posted: " + request + " \n you can view at " + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,rt");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#appendLog(int, int, java.lang.String)
	 */
	public void appendLog(int request, int user, String what) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update request set log=concat(?,log) where id=" + request, what));
		String creatorEmail = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select u.email from request r,user u where u.id=r.user and r.id=" + request)).get(0)[0];
		Notifier.sendEmail(creatorEmail, creatorEmail, "ETA Requst", "A request made by you has be updated. \n you can view at " + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,rt");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#changeStatus(int, java.lang.String)
	 */
	public void changeStatus(int request, String newStatus) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update request set status=? where id=" + request, newStatus));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.SQLService#getTokens(int)
	 */
	public Vector<String[]> getTokens(int user) {
		return SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("Select token,ip from token where user=" + user));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.CommunicationService#getETASettings()
	 */
	public String[] getETASettings() {
		// smtp server,smtp port, smtp email,smtp pass,gv email,gv pass,gang server,gang port,eta host,eta server
		Settings settings = Settings.getInstance();
		return new String[] { settings.getSetting("smtpServer").getStringValue(), settings.getSetting("smtpPort").getStringValue(), settings.getSetting("smtpEmail").getStringValue(), settings.getSetting("smtpPass").getStringValue(), settings.getSetting("gvEmail").getStringValue(),
				settings.getSetting("gvPass").getStringValue(), settings.getSetting("gangServer").getStringValue(), settings.getSetting("gangPort").getStringValue(), settings.getSetting("etaHost").getStringValue(), settings.getSetting("company").getStringValue(),
				settings.getSetting("supportList").getStringValue(), settings.getSetting("publicUse").getStringValue(), settings.getSetting("publicWorkingDir").getStringValue(), settings.getSetting("publicUser").getStringValue(), settings.getSetting("publicPassword").getStringValue() };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cgrb.eta.client.CommunicationService#saveETASettings(java.lang.String[])
	 */
	public String saveETASettings(String[] settings) {
		// smtp server,smtp port, smtp email,smtp pass,gv email,gv pass,gang server,gang port,eta host,eta server
		Settings setting = Settings.getInstance();
		setting.putSetting("smtpServer", new Setting(settings[0]));
		setting.putSetting("smtpPort", new Setting(settings[1]));
		setting.putSetting("smtpEmail", new Setting(settings[2]));
		setting.putSetting("smtpPass", new Setting(settings[3]));
		setting.putSetting("gvEmail", new Setting(settings[4]));
		setting.putSetting("gvPass", new Setting(settings[5]));
		setting.putSetting("gangServer", new Setting(settings[6]));
		setting.putSetting("gangPort", new Setting(settings[7]));
		setting.putSetting("etaHost", new Setting(settings[8]));
		setting.putSetting("company", new Setting(settings[9]));
		setting.putSetting("supportList", new Setting(settings[10]));
		setting.putSetting("publicUse", new Setting(settings[11]));
		setting.putSetting("publicWorkingDir", new Setting(settings[12]));
		setting.putSetting("publicUser", new Setting(settings[13]));
		setting.putSetting("publicPassword", new Setting(settings[14]));
		// do somekind of checking on various things to see if everything works
		boolean userWorks = AuthenticationService.getService().checkCredentials(settings[13], settings[14]);
		if (!userWorks)
			return "Sorry the user authentication failed for user" + settings[13];
		setting.save();
		return "";
	}

	public int saveExternalWrapper(EWrapper wrapper) {
		if (wrapper.getId() == 0)
			return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into external_wrapper values (null," + wrapper.getWrapperId() + ",?,?,?)", wrapper.getSite(), wrapper.getKey(), wrapper.getQueue()));
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update external_wrapper set site=?, queue=?,wrapper=" + wrapper.getWrapperId() + "  where id=" + wrapper.getId(), wrapper.getSite(), wrapper.getQueue()));
		return wrapper.getId();
	}

	public void removeExternalWrapper(int id) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from external_wrapper where id=" + id));
	}

	public Vector<EWrapper> getExternalWrappers() {
		Vector<String[]> tmp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select e.wrapper,w.name,e.site,e.key,e.id,e.queue from external_wrapper e left join wrapper w on w.id=e.wrapper"));
		Vector<EWrapper> ret = new Vector<EWrapper>();
		for (String[] line : tmp) {
			ret.add(new EWrapper(line[1], Integer.parseInt(line[0]), line[2], line[3], line[5], Integer.parseInt(line[4])));
		}

		return ret;
	}

	public void shareWrapper(int wrapper, int user, String name) {
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into wrappers values(null,0," + user + "," + wrapper + ",?)", name));
		UserWrapper wrapperO = new UserWrapper(name, wrapper, "", false, 0, id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserWrapper>(ETATypeEvent.ADDED, wrapperO)), user);
	}

	public Vector<Help> getHelpList() {
		Vector<String[]> query = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select * from help"));
		Vector<Help> ret = new Vector<Help>();
		for (String[] line : query) {
			ret.add(new Help(line[1], line[2], Integer.parseInt(line[0])));
		}
		return ret;
	}

	public Vector<String> getTipList() {
		Vector<String[]> query = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select action from help where action like 'tip%'"));
		Vector<String> ret = new Vector<String>();
		for (String[] line : query) {
			ret.add(line[0]);
		}
		return ret;
	}

	public String getHelpHTML(String topic) {
		Vector<String[]> query = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select html from help where action=?", topic));
		if (query.size() == 0)
			return null;
		return query.get(0)[0];
	}

	public int saveHelpTopic(Help help) {
		if (help.getId() == -1) {
			// its new insert it
			int newId = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into help values (null,?,?)", help.getName(), help.getHtml()));
			help.setId(newId);
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Help>(ETATypeEvent.ADDED, help)), -1);
		} else {
			// update
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update  help set html=? where id=" + help.getId(), help.getHtml()));
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Help>(ETATypeEvent.UPDATED, help)), -1);
		}
		return help.getId();
	}

	public int saveRequest(String type, String summary, String description) {
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into request_new values (null,'Open'," + getUser().getId() + ",null,?,?,?) ", type, summary, description));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<RequestItem>(ETATypeEvent.ADDED, getRequest(id))), -1);
		String supportList = Settings.getInstance().getSetting("supportList").getStringValue();
		Notifier.sendEmail("eta@cgrb.oregonstate.edu", supportList, "New request posted ", "A new request was posted you can view it " + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,rq#" + id + " here ");

		return id;
	}

	public void saveRequestFile(int request, String file) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into request_file values (null," + request + ",?) ", file));
	}

	public void starRequest(boolean star, int request) {
		User user = getUser();
		if (user == null)
			return;
		if (star)
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into request_star values(null," + user.getId() + "," + request + ")"));
		else
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_star where user=" + user.getId() + " and request=" + request));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<RequestItem>(ETATypeEvent.UPDATED, getRequest(request))), -1);
	}

	public void addComment(String comment, int request) {
		User user = getUser();
		int requestItem = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into request_item values(null," + user.getId() + ",null,?," + request + ")", comment));
		CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.REQUEST, new RequestEvent(request, SqlManager.getInstance().getRequestResponse(requestItem))), -1);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<RequestItem>(ETATypeEvent.UPDATED, getRequest(request))), -1);
		Vector<String[]> stars = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select u.email,u.phone,u.byEmail,u.byText,u.id from request_star r left join user u on u.id=r.user where r.request=" + request));
		for (String[] star : stars) {
			if (!star[4].equals("" + user.getId())) {
				if (star[0] != null) {
					Notifier.sendEmail(star[0], star[0], "A post about request #" + request + " has been posted", "To view this request you can click <a href=\"" + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,rq#" + request + "\">here</a>");
				} else if (star[3].equals("1")) {
					// Notifier.sendTextNotification(number, job)
				}
			}
		}
	}

	public void closeRequest(boolean close, int request) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update request_new set status=? where id=" + request, close ? "Closed" : "Open"));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<RequestItem>(ETATypeEvent.UPDATED, getRequest(request))), -1);
		Vector<String[]> stars = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select u.email,u.phone,u.byEmail,u.byText,u.id from request_star r left join user u on u.id=r.user where r.request=" + request));
		for (String[] star : stars) {
			if (!star[4].equals("" + getUser().getId())) {
				if (star[2].equals("1")) {
					Notifier.sendEmail(star[0], star[0], "Request #" + request + " has been " + (close ? "closed" : "re-opened"), "To view this request you can click <a href=\"" + Settings.getInstance().getSetting("etaHost").getStringValue() + "#home,rq#" + request + "\">here</a>");
				} else if (star[3].equals("1")) {
					// Notifier.sendTextNotification(number, job)
				}
			}
		}
	}

	public RequestItem getRequest(int request) {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getRequest(request, user.getId());
	}

	public void removeRequestResponse(int id) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_item where id=" + id));
	}

	public void removeRequest(int id) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_new where id=" + id));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<RequestItem>(ETATypeEvent.REMOVED, new RequestItem(id))), -1);
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_star where request=" + id));
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_item where request=" + id));
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from request_file where request=" + id));
	}

	public String getFileContents(cgrb.eta.shared.etatype.File file, long startByte, long bytes) {
		User user = getUser();
		if (user == null)
			return null;
		if (file.getUser() == 0)
			return userManager.getUserService(user.getId()).getFileContents(file.getPath(), startByte, bytes);
		else {
			return userManager.getUserService(file.getUser()).getFileContents(file.getPath(), startByte, bytes);
		}
	}

	public String[] getMachines() {
		return null;
	}

	public boolean isInstalled(String program) {
		return false;
	}

	public int runJob(Job job) {
		Date now = new Date();
		User user = getUser();
		if (user == null)
			return 0;
		System.out.println("got here");
		if (job.getGlobalCluster() > 0 || (job.getWrapper() != null && job.getWrapper().getProgram().startsWith("iplant:")))
			job.setSaveStd(true);
		if (job.getWrapper() != null && job.getWrapper().getProgram().startsWith("iplant:")) {
			job.setMachine("TACC");
		}
		job.setId(SqlManager.getInstance().addJob(job));
		if (job.getWrapper() == null && job.getPipelineObject() != null) {
			return runPipeline(job);
		}
		job.getWrapper().addVar("ETAJOB", job.getId() + "");
		job.getWrapper().addVar("ETAHOST", System.getenv("HOSTNAME"));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.ADDED, job)), user.getId());
		if (job.getGlobalCluster() > 0) {
			runJobOnCluster(job, SqlManager.getInstance().getCluster(job.getGlobalCluster(), user.getId()));
			return job.getId();
		}
		if (job.getWrapper().getProgram().startsWith("iplant:")) {
			System.out.println("IPLANT JOB!!!");
			IPlantConnector.getInstance().runJob(job);
			return job.getId();
		}
		System.out.println("Job took " + (new Date().getTime() - now.getTime()) + " millseconds for mysql");
		if (job.getWaitingFor() == 0)
			runJob(user.getId(), job);
		System.out.println("Job took " + (new Date().getTime() - now.getTime()) + " millseconds");
		return job.getId();
	}

	private void runJob(int user, Job job) {
		// only try to hash if there are outputs, otherwise what is the point? I don't know what files to copy back over
		if (job.getWrapper().getOutputs().size() > 0 && job.getParent() > 0) {
			SqlManager sql = SqlManager.getInstance();
			String hash = userManager.getUserService(user).hashWrapper(job.getWrapper());
			if (hash != null) {
				Vector<String[]> jobs = sql.runQuery(sql.getPreparedStatement("Select j.id from job_hash h left join job j on j.id=h.job where j.user=" + job.getUserId() + " and h.hash=? ", hash));
				HashMap<Integer, String> outputMap = new HashMap<>();
				if (jobs.size() > 0) {
					for (String[] entry : jobs) {
						String jobId = entry[0];
						Vector<String[]> outputs = sql.runQuery(sql.getPreparedStatement("select job,path,hash,output,id from output_hash where job=" + jobId));
						boolean allFailed = true;
						boolean allPassed = true;
						for (String[] out : outputs) {
							String tempHash = userManager.getUserService(user).hashFile(new cgrb.eta.shared.etatype.File(out[1]));
							if (!out[2].equals(tempHash)) {
								allPassed = false;
								sql.executeUpdate(sql.getPreparedStatement("delete from output_hash where id=" + out[4]));
							} else {
								outputMap.put(Integer.parseInt(out[3]), out[1]);
								allFailed = false;
							}
						}
						// if all failed delete the hash entrys from the db
						if (allFailed) {
							sql.executeUpdate(sql.getPreparedStatement("delete from job_hash where job=" + jobId));
						} else if (allPassed) {
							break;
						}
					}
					// now we need to check to see if all the outputs from this job are existing in the map
					Vector<Output> outputs = job.getWrapper().getOutputs();
					if (outputMap.size() == outputs.size()) {
						Vector<cgrb.eta.shared.etatype.File> files = job.getOutputFiles();
						for (int i = 2; i < files.size(); i++) {
							// link all the files
							userManager.getUserService(user).link(new cgrb.eta.shared.etatype.File(files.get(i).getPath()), new cgrb.eta.shared.etatype.File(outputMap.get(outputs.get(i - 2).getId())));
						}

						// job finished
						sql.executeUpdate(sql.getPreparedStatement("update job set status='Finished', machine='Used prev results' where id=" + job.getId()));
						addEvent(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.FINISHED, job.getId())), job.getUserId());
						return;
					}
				}

				SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into job_hash values (null," + job.getId() + ",?)", hash));
			}
		}
		System.out.println("running job");
		userManager.getUserService(user).runJob(job);
	}

	/**
	 * @param job
	 * @return
	 */
	private int runPipeline(Job job) {
		User user = null;
		if (this.getThreadLocalRequest() != null) {
			user = getUser();
			if (user == null && job.getUserId() == 0)
				return -1;
			else {
				user = new User();
				user.setId(job.getUserId());
			}
		} else {
			user = new User();
			user.setId(job.getUserId());
		}
		Pipeline pipeline = job.getPipelineObject();
		HashMap<String, String> vars = new HashMap<String, String>();
		for (Input input : pipeline.getInputs()) {
			if (input.getName().startsWith("$"))
				vars.put(input.getName(), input.getValue());
			else
				vars.put("$'" + input.getName() + "'", input.getValue());
		}

		int prevJobId = job.getWaitingFor();
		if (job.getWorkingDir() == null || job.getWorkingDir().equals(""))
			job.setWorkingDir(vars.get("$'Working Folder'"));
		else
			vars.put("$'Working Folder'", job.getWorkingDir());

		if (pipeline.getName().equals("Foreach")) {
			// System.out.println(pipeline.getInputs().get(0).getValue());
			String[] items = eval(job.getUserId(), pipeline.getInputs().get(0).getValue(), vars).split(",");
			Input in = new Input(-1, "", "Foreach Item", "", "", true, 1, "", "");
			pipeline.addInput(in);
			for (String item : items) {
				Job stepJob = new Job();
				pipeline.setName(new File(item).getName());
				stepJob.setName(new File(item).getName());
				stepJob.setParent(job.getId());
				stepJob.setWaitingFor(prevJobId);
				stepJob.setUserId(user.getId());
				stepJob.setWorkingDir(job.getWorkingDir());
				stepJob.setPipeline(pipeline.clone());
				stepJob.setInput(-1, item);
				runJob(stepJob, user.getId());
			}
			return job.getId();
		} else if (pipeline.getName().equals("Switch")) {
			String caseS = vars.get("$'switch'");
			System.out.println(caseS + " is the case");
			Vector<PipeComponent> steps = pipeline.getSteps();
			for (PipeComponent step : steps) {
				System.out.println(step.getName());
				if (step.getName().equals("Case:" + caseS) || step.getName().equals("Case:default")) {
					System.out.println("Found the case!");
					Pipeline pipe = ((PipelineWrapper) step).getPipeline();
					Iterator<String> it = vars.keySet().iterator();
					while (it.hasNext()) {
						String name = it.next();
						pipe.addInput(new Input(1, "", name, vars.get(name), "", true, 1, "", ""));
					}
					Job stepJob = new Job();
					stepJob.setName(caseS);
					stepJob.setParent(job.getId());
					stepJob.setWaitingFor(prevJobId);
					stepJob.setUserId(user.getId());
					stepJob.setWorkingDir(job.getWorkingDir());
					stepJob.setPipeline(pipe);
					runJob(stepJob, user.getId());
					return job.getId();
				}
			}
			return job.getId();
		}
		for (PipeComponent wrapper : pipeline.getSteps()) {
			Vector<Input> inputss = wrapper.getInputs();
			for (Input input : inputss) {
				if (!input.getValue().equals("") && input.getValue().contains("$'")) {
					// System.out.print("input " + input.getName() + " " + input.getValue());
					if (!input.getValue().startsWith("${"))
						input.setValue(eval(job.getUserId(), input.getValue(), vars));
					// System.out.println(" " + input.getValue());
				}
				vars.put("$'" + input.getName() + "'", input.getValue());
			}
			if (wrapper instanceof PipeWrapper) {
				PipeWrapper wrap = (PipeWrapper) wrapper;
				for (Input input : wrap.getWrapper().getInputs()) {
					if (!input.getValue().equals("") && input.getValue().contains("$'")) {
						input.setValue(eval(job.getUserId(), input.getValue(), vars));
					}
					vars.put("$'" + input.getName() + "'", input.getValue());
				}
				for (Output output : wrap.getWrapper().getOutputs()) {
					if (output.getValue() != null) {
						output.setValue(eval(job.getUserId(), output.getValue(), vars));
						if (!output.getValue().startsWith("/"))
							output.setValue(job.getWorkingDir() + "/" + output.getValue());
						vars.put("$'" + output.getName() + "'", output.getValue());
					}
				}
				Job stepJob = new Job();
				stepJob.setName(wrap.getWrapper().getName());
				stepJob.setParent(job.getId());
				stepJob.setWaitingFor(prevJobId);
				stepJob.setUserId(user.getId());
				stepJob.setSpecs(wrap.getJobOptions());
				stepJob.setWorkingDir(job.getWorkingDir());
				stepJob.setWrapper(wrap.getWrapper());
				prevJobId = runJob(stepJob, user.getId());
			} else if (wrapper instanceof PipelineWrapper) {
				PipelineWrapper pipe = (PipelineWrapper) wrapper;
				if (pipe.getName().equals("Foreach")) {
					Job forParent = new Job();
					forParent.setName(pipe.getName());
					forParent.setParent(job.getId());
					forParent.setWaitingFor(prevJobId);
					forParent.setUserId(user.getId());
					forParent.setWorkingDir(job.getWorkingDir());
					forParent.setPipeline(pipe.getPipeline());

					forParent.setId(SqlManager.getInstance().addJob(forParent));
					prevJobId = forParent.getId();
					SqlManager sql = SqlManager.getInstance();
					String value = pipe.getInputs().get(0).getValue();
					if (value.startsWith("${") && wrapper.getPosition() > 0) {
						// we have to execute this during runtime. so store the heap into mysql
						for (Iterator<String> it = vars.keySet().iterator(); it.hasNext();) {
							String val = it.next();
							sql.executeUpdate(sql.getPreparedStatement("insert into job_heap values (null," + forParent.getId() + ",?,?) ", val, vars.get(val)));
						}
					} else {
						String[] items = value.split(",");
						if (value.startsWith("${")) {
							items = eval(job.getUserId(), value, vars).split(",");
						}
						Input in = new Input(-1, "", "Foreach Item", "", "", true, 1, "", "");
						Pipeline pipeliney = pipe.getPipeline();
						pipeliney.addInput(in);
						System.out.println("items will be waiting for " + forParent.getWaitingFor());
						int i = 0;
						for (Iterator<String> it = vars.keySet().iterator(); it.hasNext();) {
							String val = it.next();
							pipeliney.addInput(new Input(i++, "", val, vars.get(val), "", false, 1, "", ""));
						}
						for (String item : items) {
							Job stepJob = new Job();
							pipeliney.setName(new File(item).getName());
							stepJob.setName(new File(item).getName());
							stepJob.setParent(forParent.getId());
							stepJob.setWaitingFor(forParent.getWaitingFor());
							stepJob.setUserId(user.getId());
							stepJob.setWorkingDir(job.getWorkingDir());
							stepJob.setPipeline(pipeliney.clone());
							stepJob.setInput(-1, item);
							runJob(stepJob, user.getId());
						}
					}
				} else if (pipe.getName().equals("If")) {
					String condition = vars.get("$'Condition'");
					if (condition != null && condition.equals("true")) {
						Pipeline pipeli = pipe.getPipeline();
						Iterator<String> it = vars.keySet().iterator();
						while (it.hasNext()) {
							String name = it.next();
							pipeli.addInput(new Input(1, "", name, vars.get(name), "", true, 1, "", ""));
						}
						Job stepJob = new Job();
						stepJob.setName("If");
						stepJob.setParent(job.getId());
						stepJob.setWaitingFor(prevJobId);
						stepJob.setUserId(user.getId());
						stepJob.setWorkingDir(job.getWorkingDir());
						stepJob.setPipeline(pipeli);
						runJob(stepJob, user.getId());
						return job.getId();
					}

				} else {
					Job stepJob = new Job();
					stepJob.setName(pipe.getName());
					stepJob.setParent(job.getId());
					stepJob.setWaitingFor(prevJobId);
					stepJob.setUserId(user.getId());
					stepJob.setWorkingDir(job.getWorkingDir());
					int i = 0;
					for (Iterator<String> it = vars.keySet().iterator(); it.hasNext();) {
						String val = it.next();
						pipe.getPipeline().addInput(new Input(i++, "", val, vars.get(val), "", false, 1, "", ""));
					}
					stepJob.setPipeline(pipe.getPipeline());
					prevJobId = runJob(stepJob, user.getId());
				}
			}
		}
		return job.getId();
	}

	private static class FunctionHandler {
		@SuppressWarnings("unused")
		public String listFolder(int user, String folder) {
			Vector<cgrb.eta.shared.etatype.File> files = UserManagerService.getService().getUserService(user).getFiles(folder);
			String ret = "";
			for (int i = 1; i < files.size(); i++) {
				cgrb.eta.shared.etatype.File file = files.get(i);
				if (file.getName().charAt(0) != '.') {
					if (!ret.equals(""))
						ret += ",";
					ret += file.getPath();
				}
			}
			return ret;
		}

		public String list(int user, String start, String end, String by) {
			int startI = Integer.parseInt(start);
			int endI = Integer.parseInt(end);
			int byI = Integer.parseInt(by);
			String ret = "";
			for (int i = startI; i <= endI; i += byI) {
				ret += i + ",";
			}
			ret = ret.substring(0, ret.length() - 1);
			return ret;
		}

		@SuppressWarnings("unused")
		public String list(int user, String start, String end) {
			return list(user, start, end, "1");
		}

		@SuppressWarnings("unused")
		public String size(int user, String list) {
			return "" + list.split(",").length;
		}

		@SuppressWarnings("unused")
		public String getFileName(int user, String file) {
			return new File(file).getName();
		}
	}

	private FunctionHandler functionHandler = new FunctionHandler();

	private String eval(int user, String value, HashMap<String, String> vars) {
		char[] tempValue = value.toCharArray();
		String realValue = "";
		String functionVar = "";
		String var = "";
		boolean inInput = false;
		boolean inVar = false;
		for (int i = 0; i < value.length(); i++) {
			char ch = tempValue[i];
			if (inInput) {
				if (ch == '}' && !functionVar.equals("")) {
					// now go though and run each method
					String methodName = "";
					String inside = "";
					int insideNum = 0;
					for (int x = 0; x < functionVar.length(); x++) {
						if (functionVar.charAt(x) == '(') {
							insideNum++;
							if (insideNum > 1)
								inside += functionVar.charAt(x);
						} else if (functionVar.charAt(x) == ')') {
							insideNum--;
							if (insideNum > 0) {
								inside += functionVar.charAt(x);
							} else {
								// we reached the end of the function
								break;
							}
						} else if (insideNum > 0) {
							inside += functionVar.charAt(x);
						} else {
							methodName += functionVar.charAt(x);
						}
					}
					if (("${" + methodName + "}").equals(value)) {
						String[] insideArgs = methodName.split(",");
						String ret = "";
						for (String in : insideArgs) {
							if (!ret.equals(""))
								ret += ",";
							ret += "\"" + eval(user, in, vars) + "\"";
						}
						return ret;
					}
					inside = eval(user, "${" + inside + "}", vars);
					Vector<String> strs = new Vector<>();
					boolean isInside = false;
					String current = "";
					System.out.println("inside===" + inside);
					for (int x = 0; x < inside.length(); x++) {
						if (inside.charAt(x) == '"') {
							if (isInside) {
								isInside = false;
							} else {
								isInside = true;
							}
						} else if (inside.charAt(x) == ',') {
							if (!isInside) {
								strs.add(current);
								current = "";
							}
						} else {
							current += inside.charAt(x);
						}
					}
					if (!current.equals(""))
						strs.add(current);
					Object[] params = new Object[strs.size() + 1];
					params[0] = user;
					@SuppressWarnings("rawtypes")
					Class[] parameterTypes = new Class[params.length];
					parameterTypes[0] = int.class;
					for (int ie = 1; ie < parameterTypes.length; ie++) {
						parameterTypes[ie] = String.class;
						params[ie] = strs.get(ie - 1);
					}
					try {
						realValue += (String) functionHandler.getClass().getMethod(methodName, parameterTypes).invoke(functionHandler, params);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						System.out.println("function :" + methodName + " not found :(");
						e.printStackTrace();
					}
					functionVar = "";
					inInput = false;
				} else {
					if (ch != '{') {
						functionVar += ch;
					}
				}
			} else if (inVar) {
				if (ch == '\'') {
					// we have reached the end of the var try to get the var value
					String newVar = vars.get("$'" + var + "'");
					int newPos = 10;
					while (newPos >= 0 && newVar == null) {
						newVar = vars.get("$'" + newPos-- + "." + var + "'");
					}
					if (!realValue.equals("") && newVar != null && newVar.startsWith("/")) {
						newVar = new File(newVar).getName();
					}
					realValue += newVar;
					var = "";
					inVar = false;
				} else {
					var += ch;
				}
			} else if (ch == '$') {
				if (tempValue[i + 1] == '{') {
					inInput = true;
				} else if (tempValue[i + 1] == '\'') {
					inVar = true;
				}
				i++;
			} else {
				realValue += ch;
			}
		}
		return realValue == null ? value : realValue;
	}

	public int runJob(Job job, int userId) {
		User user = new User();
		user.setId(userId);

		if (job.getGlobalCluster() > 0 || (job.getWrapper() != null && job.getWrapper().getProgram().startsWith("iplant:")))
			job.setSaveStd(true);
		if (job.getWrapper() != null && job.getWrapper().getProgram().startsWith("iplant:")) {
			job.setMachine("TACC");
		}
		job.setId(SqlManager.getInstance().addJob(job));

		if (job.getWrapper() == null && job.getPipelineObject() != null) {
			return runPipeline(job);
		}
		if (job.getWrapper() == null && job.getPipelineObject() == null)
			return job.getId();
		job.getWrapper().addVar("ETAJOB", job.getId() + "");
		job.getWrapper().addVar("ETAHOST", System.getenv("HOSTNAME"));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.ADDED, job)), user.getId());
		if (job.getGlobalCluster() > 0) {
			runJobOnCluster(job, SqlManager.getInstance().getCluster(job.getGlobalCluster(), user.getId()));
			return job.getId();
		}
		if (job.getWrapper().getProgram().startsWith("iplant:")) {
			System.out.println("IPLANT JOB!!!");
			IPlantConnector.getInstance().runJob(job);
			return job.getId();
		}
		SqlManager sql = SqlManager.getInstance();

		// only try to hash if there are outputs, otherwise what is the point? I don't know what files to copy back over
		// if (false){//job.getWrapper().getOutputs().size() > 0&&job.getWaitingFor()==0) {
		// String hash = userManager.getUserService(user.getId()).hashWrapper(job.getWrapper());
		// if (hash != null) {
		// Vector<String[]> jobs = sql.runQuery(sql.getPreparedStatement("Select j.id from job_hash h left join job j on j.id=h.job where j.user=" + job.getUserId() + " and h.hash=? ", hash));
		// HashMap<Integer, String> outputMap = new HashMap<>();
		// if (jobs.size() > 0) {
		// for (String[] entry : jobs) {
		// String jobId = entry[0];
		// System.out.println(jobId);
		// Vector<String[]> outputs = sql.runQuery(sql.getPreparedStatement("select job,path,hash,output,id from output_hash where job=" + jobId));
		// boolean allFailed = true;
		// boolean allPassed = true;
		// for (String[] out : outputs) {
		// String tempHash = userManager.getUserService(user.getId()).hashFile(new cgrb.eta.shared.etatype.File(out[1]));
		// if (!out[2].equals(tempHash)) {
		// allPassed = false;
		// sql.executeUpdate(sql.getPreparedStatement("delete from output_hash where id=" + out[4]));
		// } else {
		// outputMap.put(Integer.parseInt(out[3]), out[1]);
		// allFailed = false;
		// }
		// }
		// // if all failed delete the hash entrys from the db
		// if (allFailed) {
		// sql.executeUpdate(sql.getPreparedStatement("delete from job_hash where job=" + jobId));
		// } else if (allPassed) {
		// break;
		// }
		// }
		// // now we need to check to see if all the outputs from this job are existing in the map
		// Vector<Output> outputs = job.getWrapper().getOutputs();
		// if (outputMap.size() == outputs.size()) {
		// Vector<cgrb.eta.shared.etatype.File> files = job.getOutputFiles();
		// for (int i = 2; i < files.size(); i++) {
		// // link all the files
		// if (!files.get(i).getPath().equals(new cgrb.eta.shared.etatype.File(outputMap.get(outputs.get(i - 2).getId())).getPath()))
		// userManager.getUserService(user.getId()).link(new cgrb.eta.shared.etatype.File(files.get(i).getPath()), new cgrb.eta.shared.etatype.File(outputMap.get(outputs.get(i - 2).getId())));
		// }
		//
		// // job finished
		// sql.executeUpdate(sql.getPreparedStatement("update job set status='Finished', machine='Used prev results' where id=" + job.getId()));
		// addEvent(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.FINISHED, job.getId())), job.getUserId());
		// return job.getId();
		// }
		// }
		//
		// SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into job_hash values (null," + job.getId() + ",?)", hash));
		// }
		// }
		if (job.getWaitingFor() == 0) {
			userManager.getUserService(user.getId()).runJob(job);
		} else {
			if (sql.runQuery(sql.getPreparedStatement("select status from job where id=" + job.getWaitingFor() + " and status ='Finished'")).size() > 0)
				userManager.getUserService(user.getId()).runJob(job);
		}
		return job.getId();
	}

	public String hashWrapper(Wrapper wrapper) {
		return "";
	}

	public void runQmod(Vector<String> jobs, String wrapper) {
		// TODO I should probably get rid of this method, but I don't know if I should ....
	}

	public void removeFavorite(int type, String value) {
		SqlManager.getInstance().removeFavorite(getUser().getId(), type, value);
	}

	public Vector<Wrapper> getWrappers() {
		User user = getUser();
		Vector<Wrapper> temp = userManager.getUserService(user.getId()).getPublicWrappers();
		if (temp != null)
			return temp;
		if (user.getPermissionLevel() > 7) {
			return SqlManager.getInstance().getWrappers(0);

		} else
			return SqlManager.getInstance().getWrappers(user.getId());
	}

	public void rateWrapper(int rating, int wrapper) {
		SqlManager sql = SqlManager.getInstance();
		User user = getUser();
		if (user == null)
			return;
		sql.executeUpdate(sql.getPreparedStatement("delete from wrapper_star where user=" + user.getId() + " and wrapper=" + wrapper));
		sql.executeUpdate(sql.getPreparedStatement("insert into wrapper_star values (null," + user.getId() + "," + wrapper + "," + rating + ")"));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Wrapper>(ETATypeEvent.UPDATED, sql.getWrapper(wrapper, user.getId()))), user.getId());
	}

	public void removeJobNote(int id) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_note where id=" + id));
	}

	public JobNote addNote(int job, String note) {
		User user = getUser();
		if (user == null)
			return null;
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into job_note values(null,?," + user.getId() + ",null," + job + ")", note));
		JobNote ret = new JobNote();
		ret.setDate(SqlManager.getInstance().formatDate(new Date()));
		ret.setNote(note);
		ret.setUser(user.getName());
		ret.setUserId(user.getId());
		ret.setId(id);
		return ret;
	}

	public Wrapper getWrapperFromCMD(String[] cmd) {
		String program = cmd[0];
		File file = new File(program);
		Wrapper wrapperOld = SqlManager.getInstance().getWrapper(file.getName() + " auto");
		String inputValue = "";
		for (int i = 1; i < cmd.length; i++) {
			if (i > 1)
				inputValue += ",";
			inputValue += cmd[i];
		}
		if (wrapperOld != null) {
			if (wrapperOld.getInputs().size() > 0) {
				Input input = wrapperOld.getInputs().get(0);
				input.setValue(inputValue);
			} else {
				Input input = new Input(-1, "auto input list of the options", "options", inputValue, "", true, 0, "Default", "List:String{,}");
				wrapperOld.addInput(input);
				SqlManager.getInstance().saveWrapper(wrapperOld);
				input.setValue(inputValue);
			}
			return wrapperOld;
		} else {
			Wrapper wrapper = new Wrapper(0);
			wrapper.setCreatorId(-1);
			wrapper.setProgram(program);
			wrapper.setName(file.getName() + " auto");
			wrapper.setDescription("Auto generated wrapper for program " + program);
			Input input = new Input(-1, "auto input list of the options", "options", "", "", true, 0, "Default", "List:String{,}");
			wrapper.addInput(input);
			SqlManager.getInstance().saveWrapper(wrapper);
			input.setValue(inputValue);
			return wrapper;
		}
	}

	public void createFolder(String path) {
		userManager.getUserService(getUser().getId()).makeFolder(path);
	}

	public void removeResult(int result, int job) {
		User user = getUser();
		if (user == null)
			return;
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from jobs where user=" + user.getId() + " and id=" + result));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.REMOVED, new UserResult(user.getId(), 0, "", 0, result))), user.getId());
		Vector<String[]> jobs = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select id from jobs where job=" + job));
		if (jobs.size() == 0) {
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job where id=" + job));
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_note where job=" + job));
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from job_value where job=" + job));
		}
	}

	public void saveUser(User user) {
		SqlManager sql = SqlManager.getInstance();
		sql.executeUpdate(sql.getPreparedStatement("update user set email=?,phone=?,byEmail=" + user.isnEmail() + ",byText=" + user.isnPhone() + " where id=" + user.getId(), user.getEmail(), user.getPhone()));
		sql.executeUpdate(sql.getPreparedStatement("delete from user_setting where user=" + user.getId()));
		HashMap<String, String> settings = user.getSettings();
		Iterator<String> it = settings.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			sql.executeUpdate(sql.getPreparedStatement("insert into user_setting values (null," + user.getId() + ",?,?) ", key, settings.get(key)));
		}
	}

	public String generatePerlTemplate(int wrapperId, String path) {
		Wrapper wrapper = SqlManager.getInstance().getWrapperFromId(wrapperId);
		RemoteUserService con = userManager.getUserService(getUser().getId());
		return (String) con.generatePerlTemplate(wrapper, new cgrb.eta.shared.etatype.File(path + "/" + wrapper.getProgram()));
	}

	public void makeResultPublic(int job) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update job set public=true where id=" + job + " and user=" + getUser().getId()));
	}

	public void shareResult(int id, int user, String name) {
		int newId = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into jobs values (null," + user + ",0," + id + ",?)", name));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserResult>(ETATypeEvent.UPDATED, new UserResult(user, 0, name, id, newId))), user);
	}

	public int killJob(int job) {
		User user2 = getUser();
		if (user2 == null)
			return -1;
		int user = user2.getId();
		RemoteUserService connection = userManager.getUserService(user);
		if (connection == null) {
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update  job set status='Stopped' where id=" + job));
			Job jobO = SqlManager.getInstance().getJob(job);
			jobO.setStatus("Stopped");
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, jobO)), getUser().getId());
			return -1;
		}

		String ret = connection.cancelJob(job);
		if (ret.equals("Lost control of job")) {
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update  job set status='Stopped' where id=" + job));
			Job jobO = SqlManager.getInstance().getJob(job);
			jobO.setStatus("Stopped");
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, jobO)), getUser().getId());
			return -1;
		}
		return 0;
	}

	public Vector<UserPipeline> getUsersPipelines() {
		return SqlManager.getInstance().getUsersPipelines(getUser().getId());
	}

	public Pipeline getPipelineFromId(int pipelineId) {
		return SqlManager.getInstance().getPipelineFromId(pipelineId);
	}

	public void sharePipeline(int pipelineId, int userId, String name) {
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into pipelines values(null,0," + userId + "," + pipelineId + ",?)", name));
		UserPipeline wrapperO = new UserPipeline(0, name, false, pipelineId, "", "", id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserPipeline>(ETATypeEvent.UPDATED, wrapperO)), -1);
	}

	public int movePipeline(int id, int oldParent, int newParent, String name, int pipelineId) {
		if (oldParent == newParent && id != 0)
			return 0;
		User user = getUser();
		if (user == null)
			return -1;
		UserPipeline pipeline = new UserPipeline(newParent, name, false, pipelineId, "", "", id);
		if (id == 0) {// assume that the wrapper being moved is new
			int newId = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into pipelines (parent,user,name,pipeline) values (" + newParent + "," + user.getId() + ",?," + pipelineId + ") ", name));
			pipeline.setId(newId);
			String[] temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select name,public from pipeline where id=" + pipelineId)).get(0);
			pipeline.setName(temp[0]);
			pipeline.setPublic(temp[1].equals("true") || temp[1].equals("1"));
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserPipeline>(ETATypeEvent.ADDED, pipeline)), user.getId());
			return newId;
		} else {
			if (newParent == -1) {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserPipeline>(ETATypeEvent.REMOVED, pipeline)), user.getId());
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("delete from pipelines where id=" + id));
			} else {
				addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserPipeline>(ETATypeEvent.UPDATED, pipeline)), user.getId());
				return SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update pipelines set parent=" + newParent + " where id=" + id));
			}
		}
	}

	public int addPipelineFolder(String name) {
		User user = getUser();
		if (user == null)
			return -1;
		int id = SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into pipelines (parent,user,name,pipeline) values (0," + getUser().getId() + ",?,0) ", name));
		UserPipeline pipeline = new UserPipeline(0, name, false, 0, "", "", id);
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<UserPipeline>(ETATypeEvent.ADDED, pipeline)), user.getId());
		return id;
	}

	public Vector<Pipeline> getPipelines() {
		User user = getUser();
		if (user == null)
			return null;
		return SqlManager.getInstance().getPipelines(user.getId());

	}

	public void ratePipeline(Integer rating, int pipeline) {
		SqlManager sql = SqlManager.getInstance();
		User user = getUser();
		if (user == null)
			return;
		sql.executeUpdate(sql.getPreparedStatement("delete from pipeline_star where user=" + user.getId() + " and pipeline=" + pipeline));
		sql.executeUpdate(sql.getPreparedStatement("insert into pipeline_star values (null," + user.getId() + "," + pipeline + "," + rating + ")"));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Pipeline>(ETATypeEvent.UPDATED, sql.getPipeline(pipeline, user.getId()))), user.getId());
	}

	public Pipeline savePipeline(Pipeline pipeline) {
		boolean addToPipelines = (pipeline.getId() == 0);
		Pipeline updatedPipeline = SqlManager.getInstance().savePipeline(pipeline);
		if (addToPipelines)
			movePipeline(0, 0, 0, pipeline.getName(), updatedPipeline.getId());
		return updatedPipeline;
	}

	public void makePipelinePublic(int pipelineId) {
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update pipeline set public=true where id=" + pipelineId));

	}

	public String writeFile(String file, String contents) {
		User user = getUser();
		if (user == null)
			return null;
		RemoteUserService con = userManager.getUserService(user.getId());
		byte[] cont = contents.getBytes();
		String ret = con.saveFileBuffer(file, cont, cont.length);
		if (!ret.equals("")) {
			return ret;
		}
		con.saveFileBuffer(file, new byte[] {}, 0);
		return "";
	}

	public String toJson(ETAType obj) {
		return new Gson().toJson(obj);
	}

	public ETAType getObjFromJson(String str, Class<? extends ETAType> clas) {
		return new Gson().fromJson(str, clas);
	}

	public Vector<Input> getVectorObjFromJson(String str) {
		Vector<Input> ret = new Vector<Input>();
		JsonElement json = new JsonParser().parse(str);
		try {
			JsonArray array = json.getAsJsonArray();
			for (JsonElement el : array) {
				ret.add(new Gson().fromJson(el, Input.class));
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public String toJsonArr(Vector<Input> obj) {
		return new Gson().toJson(obj);
	}

	public Vector<cgrb.eta.shared.etatype.File> getFiles(String path, String ident) {
		User user = getUser();
		if (user == null)
			return null;
		if (user.getId() > 0) {
			Vector<cgrb.eta.shared.etatype.File> temp = userManager.getUserService(user.getId()).navigateTo(ident, path);
			return temp;
		}
		return new Vector<cgrb.eta.shared.etatype.File>();
	}

	public Vector<cgrb.eta.shared.etatype.File> back(String browser, String path) {
		User user = getUser();
		if (user == null)
			return null;
		if (user.getId() > 0) {
			Vector<cgrb.eta.shared.etatype.File> temp = userManager.getUserService(user.getId()).back(browser, path);
			return temp;
		}
		return new Vector<cgrb.eta.shared.etatype.File>();
	}

	public Vector<cgrb.eta.shared.etatype.File> getHistory(String browser) {
		User user = getUser();
		if (user == null)
			return null;
		if (user.getId() > 0) {
			Vector<cgrb.eta.shared.etatype.File> temp = userManager.getUserService(user.getId()).getHistory(browser);
			return temp;
		}
		return new Vector<cgrb.eta.shared.etatype.File>();
	}

	@Override
	public Vector<Wrapper> getNextWrappers(Wrapper wrapper) {
		Vector<Wrapper> ret = new Vector<>();
		HashMap<String, Integer> counts = new HashMap<>();
		for (Output out : wrapper.getOutputs()) {
			if (out.getType() != null && !out.getType().equals("File")) {
				Vector<String[]> tempWraps = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select wrapper from wrapper_input where type like ?", "File:" + out.getType() + "%"));
				for (String[] tmp : tempWraps) {
					int count = 0;
					if (counts.get(tmp[0]) != null)
						count = counts.get(tmp[0]);
					count++;
					counts.put(tmp[0], count);
				}
			}
		}
		Iterator<String> it = counts.keySet().iterator();
		while (it.hasNext()) {
			String wrapperId = it.next();
			ret.add(SqlManager.getInstance().getWrapperFromId(Integer.parseInt(wrapperId)));
		}
		return ret;
	}

	@Override
	public String checkForErrors(String fileId) {
		String file = PluginService.getInstance().getSession(fileId).getFilenames().replaceAll("\\$localpath", Settings.getInstance().getSetting("localPath").getStringValue());
		File zipFileF = new File(file);
		try {
			ZipFile zipFile = new ZipFile(zipFileF);
			Enumeration<? extends ZipEntry> zin = zipFile.entries();
			ZipEntry zipEn;

			while (zin.hasMoreElements()) {
				zipEn = zin.nextElement();
				String name = zipEn.getName();
				if (name.equals("manifest.json")) {
					// read the manifest and get some info
					String json = "";
					BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEn)));
					String line = "";
					while ((line = reader.readLine()) != null) {
						json += line;
					}
					reader.close();
					JsonObject jsonO = new JsonParser().parse(json).getAsJsonObject();

					SqlManager sql = SqlManager.getInstance();
					Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("Select * from plugin where name=?", jsonO.getAsJsonPrimitive("name").getAsString()));
					if (results.size() == 0) {
						// its new lets check the manifest to see if it has the the required info.
						if (jsonO.getAsJsonPrimitive("name") == null) {
							zipFile.close();
							zipFileF.delete();
							return "There must be a name field in the manifest";
						}
						if (jsonO.getAsJsonPrimitive("version") == null) {
							zipFile.close();
							zipFileF.delete();
							return "There must be a version field in the manifest";
						}
						if (jsonO.getAsJsonPrimitive("description") == null) {
							zipFile.close();
							zipFileF.delete();
							return "There must be a description field in the manifest";
						}
						if (jsonO.getAsJsonPrimitive("author") == null) {
							zipFile.close();
							zipFileF.delete();
							return "There must be a author field in the manifest";
						}
						if (jsonO.getAsJsonPrimitive("type") == null) {
							zipFile.close();
							zipFileF.delete();
							return "There must be a type field in the manifest";
						}

						JsonArray fileTypes = jsonO.getAsJsonArray("file-types");
						if (fileTypes == null || fileTypes.size() == 0) {
							zipFile.close();
							zipFileF.delete();
							return "Sorry there must be some file types associated with this plugin";
						}
						JsonArray permissions = jsonO.getAsJsonArray("permissions");
						if (permissions == null || permissions.size() == 0) {
							zipFile.close();
							zipFileF.delete();
							return "Sorry you must have some permissions otherwise this plugin can't do anything";
						}
						// all is good move all tests have passed!!
						zipFile.close();
						return "";
					} else {
						// there is already a plugin with this name
						zipFile.close();
						zipFileF.delete();
						return "There is already a plugin with the same name. Delete it to install this one.";
					}
				}
			}
			zipFile.close();
		} catch (FileNotFoundException e) {
			zipFileF.delete();
			return "Sorry for some reason the file wasn't uploaded correctly. Please try again.";
		} catch (IOException e) {
			zipFileF.delete();
			return "Sorry there was an unexpected error: " + e.getLocalizedMessage();
		}
		zipFileF.delete();
		return "The uploaded file is either not a Zip file or there wasn't a manifest.json file to read the properties from.";
	}

	@Override
	public Plugin getTempPlugin(String fileId) {
		String file = PluginService.getInstance().getSession(fileId).getFilenames().replaceAll("\\$localpath", Settings.getInstance().getSetting("localPath").getStringValue());
		try {
			ZipFile zipFile = new ZipFile(new File(file));
			Enumeration<? extends ZipEntry> zin = zipFile.entries();
			ZipEntry zipEn;

			while (zin.hasMoreElements()) {
				zipEn = zin.nextElement();
				String name = zipEn.getName();
				if (name.equals("manifest.json")) {
					// read the manifest and get some info
					String json = "";
					BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEn)));
					String line = "";
					while ((line = reader.readLine()) != null) {
						json += line;
					}
					reader.close();

					JsonObject jsonO = new JsonParser().parse(json).getAsJsonObject();
					Plugin plugin = new Plugin();
					plugin.setName(jsonO.getAsJsonPrimitive("name").getAsString());
					plugin.setAuthor(jsonO.getAsJsonPrimitive("author").getAsString());
					plugin.setEmail(jsonO.getAsJsonPrimitive("email").getAsString());
					plugin.setIcon(jsonO.getAsJsonPrimitive("icon").getAsString());
					plugin.setIndex(jsonO.getAsJsonPrimitive("index").getAsString());
					plugin.setVersion(jsonO.getAsJsonPrimitive("version").getAsString());
					plugin.setDescription(jsonO.getAsJsonPrimitive("description").getAsString());
					plugin.setType(jsonO.getAsJsonPrimitive("type").getAsString());

					JsonArray fileTypes = jsonO.getAsJsonArray("file-types");
					Vector<String> fileTypesV = new Vector<>();
					for (int i = 0; i < fileTypes.size(); i++) {
						fileTypesV.add(fileTypes.get(i).getAsString());
					}
					plugin.setFileTypes(fileTypesV);
					JsonArray permissions = jsonO.getAsJsonArray("permissions");
					Vector<String> permissionsV = new Vector<>();
					for (int i = 0; i < permissions.size(); i++) {
						permissionsV.add(permissions.get(i).getAsString());
					}
					plugin.setPermissions(permissionsV);
					zipFile.close();
					return plugin;
				}
			}
			zipFile.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public Plugin saveTempPlugin(String fileId) {
		String file = PluginService.getInstance().getSession(fileId).getFilenames().replaceAll("\\$localpath", Settings.getInstance().getSetting("localPath").getStringValue());
		File zipFileF = new File(file);
		try {
			ZipFile zipFile = new ZipFile(zipFileF);
			Enumeration<? extends ZipEntry> zin = zipFile.entries();
			ZipEntry zipEn;
			Plugin plugin = null;
			File pluginFolder = null;
			while (zin.hasMoreElements()) {
				zipEn = zin.nextElement();
				String name = zipEn.getName();

				if (name.equals("manifest.json")) {
					// read the manifest and get some info
					String json = "";
					BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEn)));
					String line = "";
					while ((line = reader.readLine()) != null) {
						json += line;
					}
					reader.close();
					plugin = new Plugin();
					JsonObject jsonO = new JsonParser().parse(json).getAsJsonObject();
					plugin.setName(jsonO.getAsJsonPrimitive("name").getAsString());
					plugin.setAuthor(jsonO.getAsJsonPrimitive("author").getAsString());
					plugin.setEmail(jsonO.getAsJsonPrimitive("email").getAsString());
					plugin.setIcon(jsonO.getAsJsonPrimitive("icon").getAsString());
					plugin.setIndex(jsonO.getAsJsonPrimitive("index").getAsString());
					plugin.setVersion(jsonO.getAsJsonPrimitive("version").getAsString());
					plugin.setDescription(jsonO.getAsJsonPrimitive("description").getAsString());
					plugin.setType(jsonO.getAsJsonPrimitive("type").getAsString());

					SqlManager sql = SqlManager.getInstance();
					Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("select id from filetype where type=?", plugin.getType()));
					results = sql.runQuery(sql.getPreparedStatement("Select * from plugin where name=?", jsonO.getAsJsonPrimitive("name").getAsString()));
					if (results.size() == 0) {
						// its new, good lets install it somewhere
						String ident;

						do {
							ident = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 29);
						} while (new File(getServletContext().getRealPath("/plugins") + "/" + ident).exists());
						pluginFolder = new File(getServletContext().getRealPath("/plugins") + "/" + ident);
						pluginFolder.mkdirs();
						int entry = sql.executeUpdate(sql.getPreparedStatement("insert into plugin values (null,?,?,?,?,?,?,?,?,?)", plugin.getName(), plugin.getVersion(), plugin.getDescription(), plugin.getAuthor(), plugin.getType(), plugin.getIcon(), ident, plugin.getEmail(), plugin.getIndex()));
						JsonArray fileTypes = jsonO.getAsJsonArray("file-types");
						plugin.setId(entry);
						Vector<String> fileTypesV = new Vector<>();
						for (int i = 0; i < fileTypes.size(); i++) {
							fileTypesV.add(fileTypes.get(i).getAsString());
							sql.executeUpdate(sql.getPreparedStatement("insert into plugin_filetype (id,plugin,type) values (null," + entry + ",?)", fileTypes.get(i).getAsString()));
						}

						JsonArray permissions = jsonO.getAsJsonArray("permissions");
						Vector<String> permissionsV = new Vector<>();
						for (int i = 0; i < permissions.size(); i++) {
							sql.executeUpdate(sql.getPreparedStatement("insert into plugin_permission (id,plugin,permission) values (null," + entry + ",?)", permissions.get(i).getAsString()));
							permissionsV.add(permissions.get(i).getAsString());
						}
						plugin.setFileTypes(fileTypesV);
						plugin.setPermissions(permissionsV);
					} else {
						zipFileF.delete();
						return null;
					}
				}
			}

			if (pluginFolder == null) {
				zipFileF.delete();
				zipFile.close();
				return null;
			}

			zipFile.close();

			ZipFile zipFile2 = new ZipFile(new File(file));
			Enumeration<? extends ZipEntry> zin2 = zipFile2.entries();
			ZipEntry zipEn2;
			while (zin2.hasMoreElements()) {
				zipEn2 = zin2.nextElement();
				if (zipEn2.isDirectory()) {
					new File(pluginFolder.getAbsolutePath() + "/" + zipEn2.getName()).mkdir();
				} else {
					InputStream in = zipFile2.getInputStream(zipEn2);
					byte[] buff = new byte[1024 * 12];
					DataOutputStream outer = new DataOutputStream(new FileOutputStream(new File(pluginFolder.getAbsolutePath() + "/" + zipEn2.getName())));
					int length;
					while ((length = in.read(buff)) != -1) {
						outer.write(buff, 0, length);
					}
					outer.close();
				}
			}
			zipFile2.close();
			zipFileF.delete();
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Plugin>(ETATypeEvent.ADDED, plugin)), -1);
			return plugin;
		} catch (FileNotFoundException e) {
			zipFileF.delete();
			return null;
		} catch (IOException e) {
			zipFileF.delete();
			return null;
		}
	}

	public String excapeURL(String url) {
		url = url.toLowerCase();
		if (url.contains("//")) {
			url = url.split("//")[1];
		}
		if (url.contains("/")) {
			url = url.split("/")[0];
		}
		return url;
	}

	@Override
	public String requestAccess(String cluster) {
		User user = getUser();
		cluster = excapeURL(cluster.toLowerCase());

		SqlManager sql = SqlManager.getInstance();
		// check to make sure that there isn't already a request out from this user and cluster
		if (sql.runQuery(sql.getPreparedStatement("select * from pending_cluster where server=? and user=" + user.getId() + " and type=0", cluster)).size() > 0) {
			return "There is already a request out to this cluster. Please wait...";
		}
		// create a new request and put it into mysql
		String requestId = generateToken();
		HashMap<String, String> props = new HashMap<>();

		// see if we have already talked to this server
		Vector<String[]> globals = sql.runQuery(sql.getPreparedStatement("select global_cluster.key from global_cluster where address=? and type=0", cluster));
		if (globals.size() > 0) {
			props.put("globalKey", globals.get(0)[0]);
		}
		props.put("server", excapeURL(Settings.getInstance().getSetting("etaHost").getStringValue()));
		props.put("email", user.getEmail());
		props.put("username", user.getName());
		props.put("userid", "" + user.getId());
		props.put("request", requestId);
		props.put("org", Settings.getInstance().getSetting("company").getStringValue());
		String ret = HTTPSPost.post("https://" + cluster + "/cluster", props);
		if (ret.equals("OK")) {
			int id = sql.executeUpdate(sql.getPreparedStatement("insert into pending_cluster (id,request,organization,username,user,email,status,type,server) values (null,?,?,?," + user.getId() + ",?,'Pending',0,?)", requestId, "", "", "", cluster));
			PendingCluster pend = new PendingCluster("", requestId, cluster, "Pending", "");
			pend.setId(id);
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.ADDED, pend)), user.getId());
			return "";
		}
		return ret;
	}

	public String acceptClusterRequest(int request) {
		SqlManager sql = SqlManager.getInstance();
		PendingCluster requestO = sql.getPendingCluster(request);
		String userKey = generateToken();
		Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("select global_cluster.key from global_cluster where address=? and type=0", requestO.getServer()));
		String globalKey;
		if (results.size() == 0) {
			globalKey = CommunicationImpl.getInstance().generateToken();
			sql.executeUpdate(sql.getPreparedStatement("insert into global_cluster values(null,?,?,0 )", requestO.getServer().split(":")[0], globalKey));
		} else {
			globalKey = results.get(0)[0];
		}

		HashMap<String, String> props = new HashMap<>();
		props.put("request", requestO.getRequest());
		props.put("response", "Approved");
		props.put("globalKey", globalKey);
		props.put("userid", "" + requestO.getUserId());
		props.put("key", userKey);
		props.put("org", Settings.getInstance().getSetting("company").getStringValue());

		String ret = HTTPSPost.post("https://" + requestO.getServer() + "/cluster", props);
		System.out.println("--" + ret + "--");
		if (ret.equals("Good")) {
			sql.executeUpdate(sql.getPreparedStatement("update pending_cluster set status='Approved' where id=" + request));
			sql.executeUpdate(sql.getPreparedStatement("insert into approved_cluster values(null," + request + ",?)", userKey));
			apiService.updateKeys();
			requestO.setStatus("Accepted");
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.UPDATED, requestO)), -1);
		}
		System.out.println(ret);
		return ret;
	}

	public String rejectClusterRequest(int request) {
		SqlManager sql = SqlManager.getInstance();
		PendingCluster requestO = sql.getPendingCluster(request);
		HashMap<String, String> props = new HashMap<>();
		props.put("request", requestO.getRequest());
		props.put("response", "denied");
		props.put("globalKey", "sorry");
		props.put("userid", "" + requestO.getUserId());
		props.put("org", Settings.getInstance().getSetting("company").getStringValue());

		props.put("key", "sorry");
		String ret = HTTPSPost.post("https://" + requestO.getServer() + "/cluster", props);
		sql.executeUpdate(sql.getPreparedStatement("update pending_cluster set status='Denied' where id=" + request));
		requestO.setStatus("Denied");
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.UPDATED, requestO)), -1);
		return ret;
	}

	public String autoAcceptClusterRequest(int request) {
		SqlManager sql = SqlManager.getInstance();
		PendingCluster requestO = sql.getPendingCluster(request);
		String userKey = generateToken();

		Vector<String[]> results = sql.runQuery(sql.getPreparedStatement("select global_cluster.key from global_cluster where address=? and type=0", requestO.getServer()));
		String globalKey;
		if (results.size() == 0) {
			globalKey = CommunicationImpl.getInstance().generateToken();
			sql.executeUpdate(sql.getPreparedStatement("insert into global_cluster values(null,?,?,0 )", requestO.getServer().split(":")[0], globalKey));
		} else {
			globalKey = results.get(0)[0];
		}

		HashMap<String, String> props = new HashMap<>();
		props.put("request", requestO.getRequest());
		props.put("response", "Approved");
		props.put("globalKey", globalKey);
		props.put("key", userKey);
		props.put("org", Settings.getInstance().getSetting("company").getStringValue());

		props.put("userid", "" + requestO.getUserId());

		String ret = HTTPSPost.post("https://" + requestO.getServer() + "/cluster", props);
		if (ret.equals("Good")) {
			sql.executeUpdate(sql.getPreparedStatement("insert into auto_cluster values (null,?) ", requestO.getServer()));
			sql.executeUpdate(sql.getPreparedStatement("update pending_cluster set status='Approved' where id=" + request));
			sql.executeUpdate(sql.getPreparedStatement("insert into approved_cluster values(null," + request + ",?)", userKey));
			apiService.updateKeys();
			requestO.setStatus("Accepted");
			addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.UPDATED, requestO)), -1);
		}
		System.out.println(ret);
		return ret;
	}

	@Override
	public Vector<PendingCluster> getPendingClusters() {
		return SqlManager.getInstance().getPendingClusters();
	}

	@Override
	public Vector<PendingCluster> getUserPendingClusters() {
		return SqlManager.getInstance().getUserPendingClusters(getUser().getId());
	}

	public void runJobOnCluster(final Job job, final Cluster cluster) {
		final ClusterRemoteService con = apiService.getConnection(cluster);
		final RemoteUserService localConnection = userManager.getUserService(job.getUserId());
		if (con == null) {
			System.out.println("couldn't connect to cluster " + cluster.getAddress());
			return;
		}
		System.out.println("con is not null!");
		final Boolean canRun = con.setupJob(cluster.getKey(), job);
		job.setStatus("Copying input files over");
		job.setMachine(cluster.getCompany());
		SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("update job set machine=? where id=" + job.getId(), cluster.getCompany()));
		addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Job>(ETATypeEvent.UPDATED, job)), job.getUserId());
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (canRun) {
					// next we have to copy over all of the input files
					Vector<Input> inputs = job.getWrapper().getInputs();
					for (Input input : inputs) {
						if (input.getType().startsWith("File")) {
							// found a file here!
							String file = input.getValue();
							copyFile(localConnection, con, file, job, cluster.getKey());
						} else if (input.getType().startsWith("List:File") && input.getValue() != null && !input.getValue().equals("")) {
							String delimiter = input.getType().split("\\{")[1].split("\\}")[0];
							String[] fileA = input.getValue().split(delimiter);
							for (String file : fileA) {
								System.out.println("copying file " + file + " over");
								copyFile(localConnection, con, file, job, cluster.getKey());
							}
						}
					}
				}
				// now we can start the job over there
				con.startJob(cluster.getKey(), job.getId());
			}
		}).start();
	}

	public void copyFile(RemoteUserService localConnection, ClusterRemoteService remote, String file, Job job, String key) {
		if (file != null && !file.equals("")) {
			if (!file.startsWith("/")) {
				file = job.getWorkingDir() + "/" + file;
			}
			// copy this file over!
			byte[] bbuf;
			do {
				bbuf = localConnection.getFileBuffer(file);
				remote.saveJobFile(key, job.getId(), new File(file).getName(), bbuf);
			} while (bbuf.length > 0);
		}
	}

	@Override
	public Vector<Share> getMyShares() {
		return SqlManager.getInstance().getMyShares(getUser().getId());
	}

	@Override
	public Vector<Share> getOtherShares() {
		return SqlManager.getInstance().getOtherShares(getUser().getId());
	}

	@Override
	public void reRunFailedJobs() {
		Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select id from job where user=" + getUser().getId() + " and status like 'Failed' or status like 'FAILED'"));
		for (String[] res : temp) {
			rerunJob(Integer.parseInt(res[0]));
		}
	}
	//now returns a string that holds the error message (or blank)
	@Override
	public String removeFiles(Vector<cgrb.eta.shared.etatype.File> files) {
		User user = getUser();
		if (user == null)
			return "";
		return userManager.getUserService(user.getId()).removeFiles(files);
	}

	@Override
	public String saveFileBuffer(String file, byte[] contents) {
		User user = getUser();
		if (user == null) {
			return null;
		}
		return userManager.getUserService(user.getId()).saveFileBuffer(file, contents, contents.length);
	}

	@Override
	public ArrayList<Job> getChildJobs(int jobId) {
		return SqlManager.getInstance().getChildJobs(jobId);
	}

	@Override
	public void saveJob(Job job) {
		SqlManager.getInstance().saveJob(job);
	}

	public void jobStatusChanged(final int etaJob) {
		Job job = SqlManager.getInstance().getJob(etaJob);
		userManager.getUserService(job.getUserId()).statusChanged(etaJob);
	}

	@Override
	public boolean moveFile(String from, String to) {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return userManager.getUserService(user.getId()).moveFile(from, to);
	}

	@Override
	public boolean linkFile(String from, String to) {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return userManager.getUserService(user.getId()).linkFile(from, to);
	}

	@Override
	public boolean copyFile(String from, String to) {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return userManager.getUserService(user.getId()).copyFile(from, to);
	}

	@Override
	public boolean compressFiles(String type, String[] files, String to) {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return userManager.getUserService(user.getId()).compressFiles(type, files, to);
	}

	@Override
	public boolean deCompressFile(String type, String archive, String where) {
		User user = getUser();
		if (user == null) {
			return false;
		}
		return userManager.getUserService(user.getId()).deCompressFile(type, archive, where);
	}

	@Override
	public String[] getQueues() {
		User user = getUser();
		if (user == null) {
			return null;
		}
		return userManager.getUserService(user.getId()).getQueues();
	}

	@Override
	public String[] getThreadEnviroments() {
		User user = getUser();
		if (user == null) {
			return null;
		}
		return userManager.getUserService(user.getId()).getThreadEnviroments();
	}

	/**
	 * requestPwChange should send an email notifying an administrator that they forgot their pw.
	 * 
	 * The email should be like, hey I forgot my password, at this point the administrator should email the user (in the database prob) and confirm
	 * that they want a password change. Upon doing so, a random password can be manually generated and sent to them. Alternatively, we could setup a system with security questions
	 * or a system that emails a user a token, and then if they respond with the paired token it resets the password and emails them a new random one.
	 * 
	 * @param	accountName	account name of the person who forgot their email.
	 * @param body	The input that a user gives to email.
	 * 
	 */
	public void requestPwChange(String body, String accountName){
		String supportList = Settings.getInstance().getSetting("supportList").getStringValue();
		//send the email to supportList :3
		String subject = "New Password Request for " + accountName;
		Notifier.sendEmail(supportList, supportList, subject, body);
	}
}