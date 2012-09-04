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
package cgrb.eta.shared;

import java.io.Serializable;
import java.util.HashMap;

public class ResultSettings  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5314326731809461081L;
	HashMap<Integer, String> users = new HashMap<Integer, String>();
	boolean isPublic;
	String url;
	int sessionId;
	int resultId;
	
	public ResultSettings(){}

	public ResultSettings(HashMap<Integer, String> users, boolean isPublic, String url, int sessionId, int resultId) {
		this.users = users;
		this.resultId = resultId;
		this.isPublic = isPublic;
		this.url = url;
		this.sessionId = sessionId;
	}

	public HashMap<Integer, String> getUsers() {
		return users;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl(){
		return url;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public void addUser(String user, int id) {
		users.put(id, user);
	}
	
	public void removeUser(int id) {
		users.remove(id);
	}

	public boolean containsUser(int id) {
		return users.containsKey(id);
	}

	public int getResultId() {
		return resultId;
	}
	public boolean isPublic(){
		return isPublic;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setResultId(int resultId) {
		this.resultId = resultId;
	}
	
}
