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
package cgrb.eta.server.remote.etamonitor;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;

public class Monitor implements RemoteMonitorService {

	private String server;
	private OutputStream out;
	private MonitorService con;
	private RMIConnection connection;
	private int job;
	private long startTime;
	private String home;
	private String hostname;
	private final long MAX_TIME = 1000 * 60 * 60 * 6;// 6 hours

	@SuppressWarnings("unused")
	public Monitor(String[] command) {
		Job jobO = null;
		home = System.getenv("HOME");
		startTime = new Date().getTime();
		try {
			hostname = System.getenv("HOSTNAME");
			server = System.getenv("ETAHOST");
			job = Integer.parseInt(System.getenv("ETAJOB"));
			if (connect()) {
				//jobO = con.getJob(job);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (jobO != null) {
			do {
				con.jobStarted(jobO.getId(), hostname);
				connection.close();
				int exit = runJob(jobO);
				if (connect()) {
					if ((new Date().getTime() - startTime) < MAX_TIME) {
						jobO = con.jobFinishedWithNext(jobO.getId(), exit);
					}else
						jobO=null;
				}else
					jobO=null;
				// if this has been going for less than 6 hours why not get the next job and run it
			} while (jobO != null);
			connection.close();
		} else {
			con.jobStarted(job, hostname);
			connection.close();
			command[0] = command[0].trim();
			ProcessBuilder builder = new ProcessBuilder(command);

			try {
				builder.inheritIO();
				Process p = builder.start();
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int exit = p.exitValue();
				if (connect()) {
					con.jobFinished(job, exit);
				}
				connection.close();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
				if (connect()) {
					con.jobFinished(job, 801);
				}
				connection.close();
				System.exit(0);
			}
		}
		System.exit(0);
	}

	public int runJob(Job job) {
		ProcessBuilder builder = new ProcessBuilder(getCommand(job.getWrapper()));
		if (job.isSaveStd()) {
			builder.redirectError(new File(job.getWorkingDir() + "/stderr"));
			builder.redirectOutput(new File(job.getWorkingDir() + "/stdout"));
		} else {
			builder.redirectError(new File(home + "/ETA/" + job.getId() + ".err"));
			builder.redirectOutput(new File(home + "/ETA/" + job.getId() + ".std"));
		}
		builder.directory(new File(job.getWorkingDir()));
		Map<String, String> env = builder.environment();
		HashMap<String, String> jobsEnv = job.getWrapper().getEnvVars();
		Iterator<String> it = jobsEnv.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			env.put(key, jobsEnv.get(key));
		}
		try {
			Process p = builder.start();
			try {
				return p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return 801;
			}

		} catch (IOException e) {
			e.printStackTrace();
			return 802;
		}

	}

	private List<String> getCommand(Wrapper wrapper) {
		ArrayList<String> commandPars = new ArrayList<String>();
		Vector<Input> inputs = wrapper.getInputs();
		commandPars.add(wrapper.getProgram());
		for (Input par : inputs) {
			if (par.getFlag() != null && !par.getFlag().equals("")) {
				Input i = (Input) par;
				String value = i.getValue();

				if (i.getType().startsWith("Flag")) {
					if (value != null && value.equalsIgnoreCase("true")) {
						commandPars.add(i.getFlag());
					}
				} else if (par.getType().startsWith("Input-List")) {
					String[] lines = par.getValue().split("~~~");
					for (String line : lines) {
						String[] inputs1 = line.split("~`~");
						for (String input : inputs1) {
							commandPars.add(input);
						}
					}
				} else if (par.getDescription().equals("auto input list of the options")) {
					String[] lines = par.getValue().split(",");
					for (String line : lines) {
						commandPars.add(line);
					}
				} else if (value != null && !value.replaceAll("", "").equals("")) {
					if (i.getFlag().endsWith("=")) {
						commandPars.add(i.getFlag() + value);
					} else {
						commandPars.add(i.getFlag());
						commandPars.add(value);
					}
				}
			} else if (par.getValue() != null && !par.getValue().replaceAll("", "").equals("")) {
				if (par.getType().startsWith("List") && par.getType().contains("{ }")) {
					String[] values = par.getValue().split(" ");
					for (String val : values) {
						commandPars.add(val);
					}
				} else if (par.getType().startsWith("Input-List")) {
					String[] lines = par.getValue().split("~~~");
					for (String line : lines) {
						String[] inputs1 = line.split("~`~");
						for (String input : inputs1) {
							commandPars.add(input);
						}
					}
				} else if (par.getDescription() != null && par.getDescription().equals("auto input list of the options")) {
					String[] lines = par.getValue().split(",");
					for (String line : lines) {
						commandPars.add(line.replaceAll(" ", "\\ "));
					}
				} else {
					commandPars.add(par.getValue());
				}
			}
		}
		return commandPars;
	}

	public boolean connect() {
		try {
			File etaSettings = new File(System.getenv("HOME") + "/ETA/.settings");
			Settings settings = Settings.getInstance(etaSettings);
			String token = settings.getSetting("token").getStringValue();
			byte[] sending = (" " + token).getBytes();
			sending[0] = LocalETAConnectionServer.ETA_MONITOR;
			Socket sock = new Socket(server, 3256);
			sock.getOutputStream().write(sending);
			sock.getOutputStream().write((job + "\n").getBytes());
			connection = new RMIConnection(sock, this, true);
			con = (MonitorService) connection.getService(MonitorService.class);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void main(String[] argssssss) {
		new Monitor(argssssss);
	}

	@Override
	public void printOut(String line) {
		try {
			out.write((line + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
