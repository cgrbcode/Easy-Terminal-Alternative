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

import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.server.QstatParser.Job;
import cgrb.eta.server.QstatParser.Queue;
import cgrb.eta.server.remote.ProgramManager;

public class QstatDataService implements Runnable {

	private static QstatDataService instance;

	public static QstatDataService getService() {
		return instance == null ? instance = new QstatDataService() : instance;
	}

	private boolean isRunning = false;
	private Object qLock = new Object();

	private int timeDelay = 4;

	private Vector<HashMap<String, Queue>> timeStamps = new Vector<HashMap<String, Queue>>();

	private QstatDataService() {
		if (ProgramManager.getInstance().isInstalled("qstat")) {
			if (!isRunning) {
				new Thread(this).start();
			}
		}
	}

	public String[][] getJobsForMachine(String machine) {
		Queue temp;
		synchronized (qLock) {
			temp = timeStamps.get(timeStamps.size() - 1).get(machine);
		}
		if (temp != null) {
			Vector<Job> jobs = temp.getJobs();
			String[][] ret = new String[jobs.size()][0];
			for (int i = 0; i < jobs.size(); i++) {
				Job job = jobs.get(i);
				ret[i] = new String[] { job.getName(), job.getOwner(), job.getSlots() };
			}
			return ret;
		}
		System.out.println("is null");
		return new String[][] {};
	}

	public HashMap<String, Queue> getLatest() {
		HashMap<String, Queue> ret;
		synchronized (qLock) {
			ret = timeStamps.lastElement();
		}
		return ret;
	}

	public String[][] getResourcesForMachine(String machine, String... resources) {
		String[][] ret;
		synchronized (qLock) {
			ret = new String[timeStamps.size()][resources.length];
			int on = 0;
			for (HashMap<String, Queue> temp1 : timeStamps) {
				Queue temp = temp1.get(machine);

				if (temp != null) {
					String[] tempA = new String[resources.length + 1];
					tempA[0] = "" + on * timeDelay;
					for (int i = 0; i < resources.length; i++) {
						tempA[i + 1] = temp.getResources(resources[i]);
					}
					ret[on] = tempA;
				}
				on++;
			}
		}

		return ret;

	}

	public void run() {
		if (isRunning) {
			return;
		}
		QstatParser parser = new QstatParser();
		isRunning = true;
		while (true) {
			parser.parse();
			Vector<Queue> queues = parser.getQueues();
			HashMap<String, Queue> queuez = new HashMap<String, Queue>();
			synchronized (qLock) {
				for (Queue q : queues) {
					queuez.put(q.getName(), q);
				}

				timeStamps.add(queuez);
				if (timeStamps.size() >= 50) {
					timeStamps.remove(0);
				}
			}
			try {
				Thread.sleep(timeDelay * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
