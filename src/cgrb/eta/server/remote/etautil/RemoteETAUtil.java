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
package cgrb.eta.server.remote.etautil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.remote.ProgramManager;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.shared.etatype.Job;

public class RemoteETAUtil {

	private String serverMachine;
	private int port;
	private Socket sock;
	private RMIConnection con;
	private ETAUtilService service;

	public RemoteETAUtil() {
		serverMachine = "localhost";
		port = 3256;
		connect();
	}

	public static void main(String[] args) {
		RemoteETAUtil util = new RemoteETAUtil();
		for (String file : args) {
			File myFile = new File(file);
			util.checkFile(myFile);
		}
		System.exit(1);
	}
	
	public void checkFile(File file){
		String hash = hashFile(file);
		Job job= service.getJobForFile(file.getAbsolutePath(),hash);
		if(job!=null){
			System.out.println();
			System.out.println("Info for file: "+file.getAbsolutePath());
			System.out.println("ETA Job #"+job.getId()+" Name: "+job.getName());
			System.out.println("Submitted: "+job.getSubmitTime());
			System.out.println("Started: "+job.getRunTime()+ " on "+job.getMachine());
			System.out.println("Finished: "+job.getFinishedTime());
			System.out.println("Command ran: "+job.getWrapper().getCMD());
			System.out.println("Working dir: "+job.getWorkingDir());
			System.out.println("Job specs: "+job.getSpecs());
			System.out.println();
		}else{
			System.out.println("Sorry no entry for file: "+file.getAbsolutePath());
		}
	}

	private void connect() {
		ProgramManager.getInstance();
		if (sock != null && sock.isConnected()) {
			return;
		}

		try {
			sock = new Socket(serverMachine, port);
			sock.getOutputStream().write(new byte[] { LocalETAConnectionServer.ETA_UTIL, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		} catch (Exception e) {
			System.out.println("Couldn't connect to ETA, maybe it is down? :(");
			System.exit(1);
		}
		
		con = new RMIConnection(sock, null, true, null);
		service = (ETAUtilService) con.getService(ETAUtilService.class);

	}

	private String hashFile(File file) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			if (file.exists()) {
				if (file.isDirectory()) {
					File[] files = file.listFiles();
					for (File fileIn : files) {
						FileInputStream is;
						try {
							is = new FileInputStream(fileIn);
							FileChannel f = is.getChannel();
							ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
							while (f.read(buf) != -1) {
								buf.flip();
								byte[] reading = new byte[buf.remaining()];
								buf.get(reading);
								md.update(reading);
								buf.clear();
							}
							f.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					FileInputStream is;
					try {
						is = new FileInputStream(file);
						FileChannel f = is.getChannel();
						ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
						while (f.read(buf) != -1) {
							buf.flip();
							byte[] reading = new byte[buf.remaining()];
							buf.get(reading);
							md.update(reading);
							buf.clear();
						}
						f.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return new String(Hex.encodeHex(md.digest()));
		} catch (NoSuchAlgorithmException e1) {
		}
		return null;

	}

}
