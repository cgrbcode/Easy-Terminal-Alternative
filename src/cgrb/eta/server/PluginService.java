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

import cgrb.eta.server.mysql.SqlManager;

public class PluginService {

	private static PluginService instance;
	private HashMap<String, PluginSession> sessions = new HashMap<String, PluginSession>();

	public static PluginService getInstance() {
		return instance == null ? instance = new PluginService() : instance;
	}

	private PluginService() {

	}
	public PluginSession addTempSession(PluginSession sess) {
		sessions.put(sess.getId(), sess);
		return sess;
	}
	
	public PluginSession addSession(PluginSession sess) {
		Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select * from session where  file=? and plugin=" + sess.getPlugin(),sess.getFilenames()));
		if (temp.size() > 0){
			for(String[] line:temp){
				if(line[1].equals(sess.getUser())||line[5].equals("1")){
					sess.setId(temp.get(0)[3]);
					return sess;
				}
			}
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into session values (null,"+sess.getUser()+",?,?,"+sess.getPlugin()+",0)",sess.getFilenames(),sess.getId()));
		}else{
			SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into session values (null,"+sess.getUser()+",?,?,"+sess.getPlugin()+",0)",sess.getFilenames(),sess.getId()));
		}
		return sess;
	}

	public PluginSession getSession(String name) {
		if(sessions.containsKey(name))return sessions.get(name);
		Vector<String[]> results = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select creator,token,plugin,file,public from session where token=?",name));
		if (results.size() > 0) {
			String[] temp = results.get(0);
			return new PluginSession(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[2]), temp[3],temp[4]);
		}
		return null;
	}

	public void removeSession(String id) {
		sessions.remove(id);
	}

}
