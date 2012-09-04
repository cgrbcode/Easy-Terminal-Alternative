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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Vector;

import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;

public class RemoteLocalJob extends RemoteJob {
	private Process p;

	// private String stdOut;

	public RemoteLocalJob(Job job, EventOccuredListener lis) {
		super(job, lis);
	}

	@Override
	public void cancel() {
		if (p != null) {
			p.destroy();
		}
		listener.eventOccured(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.CANCELLED, job.getId())), job.getUserId());
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
		Runnable run = new Runnable() {

			public void run() {
				String commandToRun = job.getWrapper().getProgram();
				Vector<Input> parms = job.getWrapper().getInputs();
				ArrayList<String> cmdList = new ArrayList<String>();
				cmdList.add(job.getWrapper().getProgram());
				for (Input par : parms) {
					if (par.getFlag() != null && !par.getFlag().equals("")) {
						Input i = (Input) par;
						String value = i.getValue();
						if (i.getType().equalsIgnoreCase("flag")) {
							if (value != null && value.equals("true")) {
								commandToRun += " " + i.getFlag();
								cmdList.add(par.getFlag());
							}
						} else if (value != null && !value.equals("")) {
							commandToRun += " " + i.getFlag() + " " + value;
							cmdList.add(par.getFlag());
							cmdList.add(par.getValue());
						}
					} else {
						if (par.getValue() != null && !par.getValue().equals("")) {
							cmdList.add(par.getValue());
							commandToRun += " " + par.getValue();
						}
					}
				}
				try {
					System.out.println(commandToRun);
					ProcessBuilder builder = new ProcessBuilder(cmdList);

					if (job.getWorkingDir() != null && !job.getWorkingDir().equals("")) {
						File dir = new File(job.getWorkingDir());
						builder.directory(dir);
					}

					System.out.println("started");
					JobEvent je = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
					je.setChange("Running");
					listener.eventOccured(new ETAEvent(ETAEvent.JOB, je), job.getUserId());

					builder.redirectErrorStream(true);

					p = builder.start();

					BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = "";
					String home = System.getenv("HOME");
					File temp = new File(home);
					if (!new File(temp.getAbsolutePath() + "/ETA").exists()) {
						new File(temp.getAbsolutePath() + "/ETA").mkdir();
					}
					temp = new File(temp.getAbsoluteFile() + "/ETA/" + job.getId() + ".std");
					temp.createNewFile();
					BufferedWriter writer = new BufferedWriter(new FileWriter(temp));

					while ((line = r.readLine()) != null) {
						writer.write(line);
						writer.newLine();
						// JobEvent event = new JobEvent(JobEvent.STDOUT, job.getId());
						// event.setChange(line + "\n");
						// if (listener != null) {
						// listener.eventOccured(new ETAEvent(ETAEvent.JOB, event));
						// }
						// stdOut += line + "\n";
					}
					writer.close();
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				listener.eventOccured(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.FINISHED, job.getId())), job.getUserId());

			}
		};
		new Thread(run).start();
	}

}
