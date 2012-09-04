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

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.remote.etamonitor.MonitorService;
import cgrb.eta.server.remote.etamonitor.RemoteMonitorService;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.Job;

public class RemoteTestJob extends RemoteJob implements RemoteMonitorService {
	private MonitorService con;
	private RMIConnection connection;
	private boolean cancel = false;

	public RemoteTestJob(Job job, EventOccuredListener lis) {
		super(job, lis);
	}

	@Override
	public void cancel() {
		listener.eventOccured(new ETAEvent(ETAEvent.JOB, new JobEvent(JobEvent.CANCELLED, job.getId())), job.getUserId());
		cancel = true;
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
				System.out.println("started");
				JobEvent je = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
				je.setChange("Waiting in queuee");
				listener.eventOccured(new ETAEvent(ETAEvent.JOB, je), job.getUserId());
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (cancel) {
					con.jobFinished(job.getId(), 33);
					connection.close();
					return;
				}
				System.out.println("running");
				je.setChange("Running");
				listener.eventOccured(new ETAEvent(ETAEvent.JOB, je), job.getUserId());
				String hostname = "Localhost";
				if (connect()) {
					con.jobStarted(job.getId(), hostname);
				}
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (cancel) {
					con.jobFinished(job.getId(), 33);
					connection.close();
					return;
				}
				System.out.println("finished");
				con.jobFinished(job.getId(), 0);
				connection.close();
			}
		};
		new Thread(run).start();
	}

	public boolean connect() {
		try {
			File etaSettings = new File(System.getenv("HOME") + "/ETA/.settings");
			Settings settings = Settings.getInstance(etaSettings);
			String token = settings.getSetting("token").getStringValue();
			byte[] sending = (" " + token).getBytes();
			sending[0] = LocalETAConnectionServer.ETA_MONITOR;
			Socket sock = new Socket("127.0.0.1", 3256);
			sock.getOutputStream().write(sending);
			sock.getOutputStream().write((job.getId() + "\n").getBytes());
			connection = new RMIConnection(sock, this, true);
			con = (MonitorService) connection.getService(MonitorService.class);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void printOut(String line) {

	}

}
