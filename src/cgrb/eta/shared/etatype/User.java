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
package cgrb.eta.shared.etatype;

import java.util.HashMap;
import java.util.Vector;


public class User extends ETAType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5816251080423026295L;
	private int permissionLevel;
	private String name;
	private String email;
	private String username;
	private String phone;
	private boolean nEmail;
	private boolean nPhone;
	private boolean serviceConnected;
	private Vector<Cluster> clusters;
	private String password;
	
	private HashMap<String,String> settings;
	
	public User(){}
	
	public User(String name,String email,String username,int permission){
		this.name=name;
		this.email=email;
		this.username=username;
		this.permissionLevel=permission;
		settings= new HashMap<String, String>();
		clusters=new Vector<Cluster>();
	}
	public void setPermissionLevel(int permissionLevel) {
		this.permissionLevel = permissionLevel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setSetting(String setting, String value) {
		settings.put(setting, value);
	}

	public String getSetting(String setting){
		return settings.get(setting);
	}

	public int getPermissionLevel() {
		return permissionLevel;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}


		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public boolean isnEmail() {
			return nEmail;
		}

		public void setnEmail(boolean nEmail) {
			this.nEmail = nEmail;
		}

		public boolean isnPhone() {
			return nPhone;
		}

		public void setnPhone(boolean nPhone) {
			this.nPhone = nPhone;
		}

		/**
		 * @return
		 */
		public boolean isServiceConnected() {
			return serviceConnected;
		}

		/**
		 * @param b
		 */
		public void setServiceConnected(boolean b) {
			serviceConnected=b;
		}

		/**
		 * @return
		 */
		public HashMap<String, String> getSettings() {
			return settings;
		}
	
		public void addCluster(Cluster cluster){
			clusters.add(cluster);
		}

		public Vector<Cluster> getClusters() {
			return clusters;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		
}
