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
package cgrb.eta.server.remote;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;

public class RemoteSGEJob extends RemoteJob {

	private static Thread s;
	private static Session session;
	private static HashMap<String, String> sgeJobs = new HashMap<String, String>();
	private static HashMap<String, Integer> sgeStatuses = new HashMap<String, Integer>();
	private static Object statusLock = new Object();

	private static Thread t;

	public static Session getSession() throws java.lang.UnsatisfiedLinkError {
		if (session == null) {
			SessionFactory factory = SessionFactory.getFactory();
			session = factory.getSession();
			try {
				session.init("");
			} catch (Exception e) {
				session = null;
			}
		}
		return session;
	}

	public static String getStatusFromCode(int code) {
		switch (code) {
		case Session.HOLD:
			return "On Hold";
		case Session.DONE:
			return "Finished";
		case Session.QUEUED_ACTIVE:
			return "Waiting in the queue";
		case Session.RUNNING:
			return "Running";
		case Session.SYSTEM_ON_HOLD:
			return "On Hold";

		}
		return "Unknown";
	}

	public static void spawnListener(final EventOccuredListener listen) {
		if (t == null) {
			Runnable r = new Runnable() {

				public void run() {
					while (sgeJobs.size() > 0) {
						try {
							JobInfo info = session.wait(Session.JOB_IDS_SESSION_ANY, Session.TIMEOUT_WAIT_FOREVER);
							if (info.wasAborted()) {
								System.out.println("Job " + info.getJobId() + " never ran");

								String etaJob = sgeJobs.get(info.getJobId());
								JobEvent event = new JobEvent(JobEvent.STATUS_CHANGED, Integer.parseInt(etaJob));
								event.setChange("Failed");
								listen.eventOccured(new ETAEvent(ETAEvent.JOB, event), -1);
								sgeJobs.remove(info.getJobId());
							} else if (info.hasExited()) {
								System.out.println("Job " + info.getJobId() + " finished regularly with exit status " + info.getExitStatus());
								String etaJob = sgeJobs.get(info.getJobId());
								listen.eventOccured(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.FINISHED, Integer.parseInt(etaJob))), -1);
								sgeJobs.remove(info.getJobId());
							} else if (info.hasSignaled()) {
								System.out.println("Job " + info.getJobId() + " finished due to signal " + info.getTerminatingSignal());
							} else {
								System.out.println("Job " + info.getJobId() + " finished with unclear conditions");
							}

						} catch (org.ggf.drmaa.InvalidJobException a) {
							sgeJobs.clear();
						} catch (DrmaaException e) {
							e.printStackTrace();
						}
					}
					t = null;
				}
			};
			t = new Thread(r);
			t.start();
		}
		if (s == null) {
			Runnable status = new Runnable() {

				public void run() {
					while (sgeJobs.size() > 0) {
						Vector<String> remval = new Vector<String>();
						synchronized (statusLock) {

							Iterator<String> it = sgeStatuses.keySet().iterator();
							while (it.hasNext()) {
								String job = it.next();
								int curStatus = sgeStatuses.get(job);
								try {
									int status = session.getJobProgramStatus(job);
									if (status != curStatus) {
										System.out.println(getStatusFromCode(status));
										sgeStatuses.put(job, status);
										JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, Integer.parseInt(sgeJobs.get(job)));
										evt.setChange(getStatusFromCode(status));
										listen.eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);

										if (status == Session.RUNNING) {
											remval.add(job);
										}
									}

								} catch (org.ggf.drmaa.InvalidJobException e) {
									e.printStackTrace();
									remval.add(job);
								} catch (DrmaaException e) {
									e.printStackTrace();
								}

							}
							for (String job : remval) {
								sgeStatuses.remove(job);
							}
							remval.clear();
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					s = null;
				}
			};
			s = new Thread(status);
			s.start();
		}
	}

	private String SGEJobId;

	public RemoteSGEJob(Job job, EventOccuredListener list) {
		super(job, list);
	}

	@Override
	public void cancel() {
		Session sess = RemoteSGEJob.getSession();
		try {
			sess.control(SGEJobId, org.ggf.drmaa.Session.TERMINATE);
			JobEvent evt = new JobEvent(JobEvent.CANCELLED, job.getId());
			evt.setChange("Cancelled");
			listener.eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		} catch (DrmaaException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public void pause() {

	}

	@Override
	public void start() {

		Session sess = RemoteSGEJob.getSession();
		try {
			JobTemplate template = sess.createJobTemplate();
			ArrayList<String> commandPars = new ArrayList<String>();
			Vector<Input> inputs = job.getWrapper().getInputs();
			boolean isMonitored = false;
			// if (ProgramManager.getInstance().isInstalled("ETAMon")) {
			if (new File("/local/cluster/ETA/dev/bin/ETAMon").exists()) {
				template.setRemoteCommand("/local/cluster/ETA/dev/bin/ETAMon");
				isMonitored = true;
				commandPars.add(job.getWrapper().getProgram());
			} else {
				template.setRemoteCommand(job.getWrapper().getProgram());
			}
			// commandPars.add(command.getCommand());

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
					if(par.getType().startsWith("List")&&par.getType().contains("{ }")){
						String[] values = par.getValue().split(" ");
						for(String val:values){
							commandPars.add(val);
						}
					}else	if (par.getType().startsWith("Input-List")) {
						String[] lines = par.getValue().split("~~~");
						for (String line : lines) {
							String[] inputs1 = line.split("~`~");
							for (String input : inputs1) {
								commandPars.add(input);
							}
						}
					} else if (par.getDescription()!=null&&par.getDescription().equals("auto input list of the options")) {
						String[] lines = par.getValue().split(",");
						for (String line : lines) {
							commandPars.add(line.replaceAll(" ", "\\ "));
						}
					} else {
						commandPars.add(par.getValue());
					}
				}
			}
			if (job.getWorkingDir() != null && !job.getWorkingDir().equals("")) {
				template.setWorkingDirectory(job.getWorkingDir());
			}
			if (job.getWrapper().getEnvVars() != null) {
				template.setJobEnvironment(job.getWrapper().getEnvVars());
			}
			template.setArgs(commandPars);

			template.setJobName("ETA#" + job.getId());

			if (job.getSpecs() != null || !job.getSpecs().equals("")) {
				if (job.getSpecs().contains("-V"))
					template.setNativeSpecification(job.getSpecs().trim() + " -w n");
				else
					template.setNativeSpecification(job.getSpecs().trim() + " -V -w n");
			} else {
				template.setNativeSpecification("-V -w n");
			}

			String home = System.getenv("HOME");
			File temp = new File(home);
			if (!new File(temp.getAbsolutePath() + "/ETA").exists()) {
				new File(temp.getAbsolutePath() + "/ETA").mkdir();
			}

			if (job.getUserId() == 0 || (job.isSaveStd() && job.getStdoutPath().equals(""))) {
				template.setErrorPath(":" + job.getWorkingDir() + "/stderr");
				template.setOutputPath(":" + job.getWorkingDir() + "/stdout");
			} else {
				if (job.getStdoutPath().equals(""))
					template.setOutputPath(":" + temp.getAbsolutePath() + "/ETA/" + job.getId() + ".std");
				else {
					String stdOut = job.getStdoutPath();
					if (!stdOut.startsWith("/"))
						template.setOutputPath(":" + job.getWorkingDir() + "/" + stdOut);
					else
						template.setOutputPath(":" + stdOut);

				}
				template.setErrorPath(":" + temp.getAbsolutePath() + "/ETA/" + job.getId() + ".err");
			}

			Vector<Output> outputss = job.getWrapper().getOutputs();
			for (Output ou : outputss) {
				String file = job.getWorkingDir() + "/";
				if (ou.getValue() != null) {
					char[] tempValue = ou.getValue().toCharArray();
					String realValue = "";
					String var = "";
					boolean inInput = false;
					for (char ch : tempValue) {
						if (inInput) {
							if (ch == '\'' && !var.equals("")) {
								// get the value for the input named var
								Vector<Input> pars = job.getWrapper().getInputs();
								for (Input p : pars) {
									if (p.getName().equals(var)) {
										realValue += p.getValue();
										break;
									}
								}
								var = "";
								inInput = false;
							} else {
								if (ch != '\'') {
									var += ch;
								}
							}
						} else if (ch == '$') {
							inInput = true;
						} else {
							realValue += ch;
						}
					}
					file += realValue;
					if (realValue.startsWith("/"))
						file = realValue;
					File outFile = new File(file);
					File parentFolder = outFile.getParentFile();
					BufferedWriter w;
					try {
						w = new BufferedWriter(new FileWriter(parentFolder.getAbsolutePath() + "/.ETAtypes"));
						w.write(outFile.getName() + "~" + ou.getType());
						w.newLine();
						w.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.print("runngin "+ template.getOutputPath());
			SGEJobId = sess.runJob(template);
			System.out.println(" "+SGEJobId+" is moitored "+isMonitored);
			
			if (!isMonitored){
				sgeJobs.put(SGEJobId, "" + job.getId());
				sgeStatuses.put(SGEJobId, -1);
				RemoteSGEJob.spawnListener(listener);
			}
			sess.deleteJobTemplate(template);

			JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
			evt.setChange("Waiting in queue");
			listener.eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		} catch (DrmaaException e) {
			e.printStackTrace();
			// listener.eventOccured(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.FINISHED, job.getId())));
			JobEvent evt = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
			evt.setChange("FAILED");
			listener.eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
			evt = new JobEvent(JobEvent.STDERR, (job.getId()));
			evt.setChange(e.getLocalizedMessage() + "\n");
			listener.eventOccured(new ETAEvent(ETAEvent.JOB, evt), -1);
		}

	}

}
