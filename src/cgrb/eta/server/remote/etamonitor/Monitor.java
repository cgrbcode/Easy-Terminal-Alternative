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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.settings.Settings;

public class Monitor implements RemoteMonitorService {

	private String server;
	private OutputStream out;
	private MonitorService con;
	private RMIConnection connection;
	private int job;

	public Monitor(String[] command) {
		try {
			String hostname = System.getenv("HOSTNAME");
			server = System.getenv("ETAHOST");
			job = Integer.parseInt(System.getenv("ETAJOB"));
			if (connect()) {
				con.jobStarted(job, hostname);
				connection.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		command[0]=command[0].trim();
		ProcessBuilder builder = new ProcessBuilder(command);
		try {
			Process p = builder.start();
			pipeStream(p.getInputStream(), System.out);
			pipeStream(p.getErrorStream(), System.err);
			out = p.getOutputStream();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int exit=p.exitValue();
			if(connect()){
				con.jobFinished(job, exit);
			}
			connection.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			if(connect()){
				con.jobFinished(job, 801);
			}
			connection.close();
			System.exit(0);
		}
		System.exit(0);
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

	private void pipeStream(final InputStream in, final OutputStream out) {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int reading;
				try {
					while ((reading = in.read()) >= 0) {
						out.write(reading);
					}
					in.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		thread.setName("std stream pipe");
		thread.start();
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
