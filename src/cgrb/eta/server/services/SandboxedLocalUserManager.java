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
package cgrb.eta.server.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.etatype.User;

public class SandboxedLocalUserManager extends UserManagerService {
	private LocalETAConnectionServer etaConnectionServer= LocalETAConnectionServer.getInstance();;

	@Override
	public RemoteUserService getUserService(int user) {
		return etaConnectionServer.getServiceForUser(user);
	}

	@Override
	public boolean userLogedIn(final User user) {
		if (getUserService(user.getId()) != null)
			return false;
		final Thread t = new Thread(new Runnable() {
			public void run() {
				try {
						spawnUserLocalConnection(user.getUsername(),user.getPassword() , user.getId());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		return true;
	}
	public void spawnUserLocalConnection(String user, String password, int id) {
		try {
			File script = new File(Settings.getInstance().getSetting("localPath").getStringValue() + "/spawnuser");
			ProcessBuilder builder = new ProcessBuilder(new String[] { script.getAbsolutePath(), user, Settings.getInstance().getSetting("localPath").getStringValue() + "/bin/ETAStart" });
			builder.redirectErrorStream(true);
			Process p = builder.start();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = r.readLine()) != null) {
				if (line.contains("connected")) {
					r.close();
					try {
						p.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return;
				} else if (line.contains("failed")) {
					p.destroy();
					r.close();
					return;
				} else if (line.contains("password:")) {
					BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
					wr.write(password);
					wr.newLine();
					wr.flush();
					wr.close();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
}
