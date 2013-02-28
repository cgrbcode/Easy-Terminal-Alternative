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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cgrb.eta.server.settings.Settings;

public class Login {
	public static native int login(String user, String password);

	public static native int startUserService(String user, String password, String command);
	
	/**
	 * Changes a users password to the new given password
	 * 
	 * changePassword now calls an expect script that is system dependent and changes the users password. If a users login information is incorrect it will fail.
	 * If there is anything wrong with system call, the error will be displayed to the user.
	 * 
	 * @param user	Username of the user.
	 * @param oldPassword	User's old password.
	 * @param newPassword User's new password
	 * @return	Returns a string of all the returned information from the system call. This includes error messages, such as bad password, and success messages.
	 * 
	 */
	public static String changePassword(String user, String oldPassword, String newPassword){
		String ret = "";
		try {
			String p_line = "";
			ArrayList<String> last_read = new ArrayList<String>();
			Process p = Runtime.getRuntime().exec(
					Settings.getInstance().getSetting("localPath").getStringValue() + "/.passwd.exp " + user + " " + oldPassword + " " + newPassword);
					//"../lib/passwd.exp " + user + " " + oldPassword + " " + newPassword);
			BufferedReader p_buff = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
			try {
				Pattern pattern = Pattern.compile(""+user+"+");
				while ((p_line = p_buff.readLine()) != null) {
					if(pattern.matcher(p_line).find())
						break;
					if (!last_read.contains(p_line)){
						last_read.add(p_line);
					}
				}
				
				for(int i = 0; i < last_read.size(); i++){
					ret += last_read.get(i) + "<br/>";
				}
				return ret;
			} catch (IOException e) {
				e.printStackTrace();
				return "Reading error.";
			}
		} catch (IOException e) {
			e.printStackTrace();
			return "Could not execute passwd.";
		}
	}

	static {
		System.load(Settings.getInstance().getSetting("localPath").getStringValue() + "/liblogin.so");
	}
}
