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
package cgrb.eta.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.shared.ResultSettings;
import cgrb.eta.shared.SearchResultItem;
import cgrb.eta.shared.etatype.Help;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.JobNote;
import cgrb.eta.shared.etatype.Plugin;
import cgrb.eta.shared.etatype.RequestItem;
import cgrb.eta.shared.etatype.Share;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.etatype.UserResult;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.rpc.client.RpcService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("sql")
public interface SQLService extends RpcService {
	
	Vector<UserResult> getUserResults();

	HashMap<String, Vector<SearchResultItem>> getSearchResults(String search);
	Vector<RequestItem> getRequests();
	void removeFavorite(int type,String value);
	void deleteResult(int result, int jobId);
	HashMap<String, String> getFavorites();
	int saveFavorite(int type, String value, String name);
	int saveHelpTopic(Help help);
	Vector<Help> getHelpList();
	Vector<String> getTipList();
	String getHelpHTML(String topic);
	String[] getFileTypes();
	Vector<User> getUsers();
	Job getJob(int id);
	void removeJobNote(int id);
	JobNote addNote(int job,String note);
	Vector<Job> getJobs();
	int addResultFolder(String value);
	int moveResult(int id, int oldParent, int newParent, String name, int jobId);
	void removeResult(int result,int job);
	public void deleteJob(int job);
	public void rerunJob(int job);
	public void makeResultPublic(int job);
	public void shareResult(int id,int user,String name);
	public int killJob(int job);
	public Vector<Wrapper> getNextWrappers(Wrapper wrapper);
	public String checkForErrors(String fileId);
	public Plugin getTempPlugin(String fileId);
	public Plugin saveTempPlugin(String fileId);
	Vector<Plugin> getPlugins();
	Vector<Plugin> getPlugins(String type);
	void removePlugin(int id);
	Vector<Share> getMyShares();
	Vector<Share> getOtherShares();
	void reRunFailedJobs();
	ArrayList<Job> getChildJobs(int jobId);
	void saveJob(Job job);
	
	
	public String setUpAuth(boolean local, boolean mysql);
	int addUser(String name, String userName, String password, int level);
	String[] getUserInfo(String user);
	void saveUser(String name, int userName, String password, String email, String phone,String viaEmail,String viaPhone);
	public String setSetting(String name, String value);
	boolean saveFileType(String type);

	
	ResultSettings getResultSettings(String sessionId);
	ResultSettings saveResultSetting(ResultSettings result);
	void changeResultName(String id,String newVal);
	Vector<String[]> getUsersShares(int user);
	Vector<String[]> getOthersShares(int user);
	void removeToken(String token);
	void changeStatus(int request,String newStatus);
	Vector<String[]> getTokens(int user);

	
}
