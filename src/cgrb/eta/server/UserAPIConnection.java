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
import java.util.Vector;

import cgrb.eta.remote.api.ETAConnectionService;
import cgrb.eta.remote.api.ETAUserConnectionService;
import cgrb.eta.remote.api.JobListener;
import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.rmi.AESRMIConnection;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

/**
 * This serves as a wrapper class that implements {@link ETAConnectionService} and stores the info for the user id so we can route all calls to the sandbox user service that the user is running.
 * 
 * @author Alexander Boyd
 * 
 */
public class UserAPIConnection implements ETAConnectionService {

	private int user;
	private AESRMIConnection connection;
	private ETAUserConnectionService service;
	private RemoteUserService userService;

	public UserAPIConnection(int user) {
		this.user = user;
		userService = UserManagerService.getService().getUserService(user);
	}

	@Override
	public Vector<cgrb.eta.shared.etatype.File> getFiles(String path) {
		Vector<cgrb.eta.shared.etatype.File> temp = UserManagerService.getService().getUserService(user).getFiles(path);
		Vector<cgrb.eta.shared.etatype.File> ret = new Vector<>();
		for (cgrb.eta.shared.etatype.File fil : temp)
			ret.add(new cgrb.eta.shared.etatype.File(fil.getPath()));
		return ret;
	}

	/**
	 * @param connection
	 */
	public void setConnection(AESRMIConnection connection) {
		this.connection = connection;
		service = (ETAUserConnectionService) connection.getService(ETAUserConnectionService.class);
	}

	public void close() {
		connection.close();
	}

	public ETAUserConnectionService getService() {
		return service;
	}

	@Override
	public Wrapper requestWrapper(String program, String[] args) {
		SqlManager sql = SqlManager.getInstance();
		String[] statementArgs = new String[args.length + 1];
		statementArgs[0] = program;
		String in = "(";
		for (int i = 0; i < args.length; i++) {
			if (in.length() > 1) {
				in += ",";
			}
			in += "?";
			statementArgs[i + 1] = args[i];
		}
		String statement = "select id from wrapper w where (public=true or creator=" + user + ") and program like ? and (select count(i.wrapper) from wrapper_input i where i.wrapper=w.id and binary i.flag in " + in + ") group by i.wrapper)=" + args.length;
		System.out.println(statement);
		Vector<String[]> results = sql.runQuery(sql.getPreparedStatement(statement, statementArgs));
		if (results.size() > 0) {
			return sql.getWrapperFromId(Integer.parseInt(results.get(0)[0]));
		}
		return null;
	}

	@Override
	public int runJob(Job job) {
		CommunicationImpl.getInstance().rerunJob(job.getId());
		return job.getId();
	}

	@Override
	public String getOutputContents(int startByte, int endByte, Output output) {
		// userService.getFileContents(file, startByte, bytes)
		return null;
	}

	@Override
	public Vector<cgrb.eta.shared.etatype.File> getJobFiles(int job, String path) {
		String workingFolder = SqlManager.getInstance().getJob(job).getWorkingDir();
		return userService.getFiles(workingFolder);
	}

	@Override
	public void saveFileBuffer(int job, String path, byte[] buffer) {
		String workingFolder = SqlManager.getInstance().getJob(job).getWorkingDir();
		userService.saveFileBuffer(workingFolder + "/" + path, buffer, buffer.length);
	}

	@Override
	public int setupJob(Job job) {
		String home = userService.getHomePath();
		String jobPath = userService.makedir(home + "/ETA/");
		job.setWorkingDir(jobPath);
		job.setSaveStd(true);
		Wrapper wrapper = job.getWrapper();
		for (Input input : wrapper.getInputs()) {
			if (input.getType().startsWith("File")&&input.getValue()!=null&&!input.getValue().equals("")) {
				input.setValue(jobPath + "/" + new File(input.getValue()).getName());
			}
		}
		job.setUserId(user);
		final int jobId = SqlManager.getInstance().addJob(job);
		CommunicationImpl.getInstance().addJobListener(jobId, new JobListener() {
			@Override
			public void jobStatusChanged(String change) {
				service.jobStatusChanged(jobId, change);
			}
		});
		return jobId;
	}

	@Override
	public String getLastLine(int job, String file, int lines) {
		String workingFolder = SqlManager.getInstance().getJob(job).getWorkingDir();
		return userService.tail(workingFolder + "/" + file, lines);
	}
}
