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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SQLServiceAsync{
	
	void addUser(String name, String userName, String password, int level, AsyncCallback<Integer> callback);

	void deleteJob(int job, AsyncCallback<Void> callback);

	void getFavorites(AsyncCallback<HashMap<String, String>> callback);

	void getUserInfo(String token, AsyncCallback<String[]> callback);

	void removeFavorite(int type, String value, AsyncCallback<Void> callback);

	void saveFavorite(int type, String value, String name, AsyncCallback<Integer> callback);

	void saveUser(String name, int userName, String password, String email, String phone,String viaEmail,String viaPhone, AsyncCallback<Void> callback);

	void setSetting(String name, String value, AsyncCallback<String> callback);

	void setUpAuth(boolean local, boolean mysql, AsyncCallback<String> callback);

	void getFileTypes(AsyncCallback<String[]> callback);

	void saveFileType(String type, AsyncCallback<Boolean> callback);

	void getPlugins(AsyncCallback<Vector<Plugin>> callback);

	void getPlugins(String type, AsyncCallback<Vector<Plugin>> callback);

	void removePlugin(int id, AsyncCallback<Void> callback);

	void getUsers(AsyncCallback<Vector<User>> callback);

	void getResultSettings(String sessionId, AsyncCallback<ResultSettings> callback);

	void saveResultSetting(ResultSettings result, AsyncCallback<ResultSettings> callback);

	void getUserResults(AsyncCallback<Vector<UserResult>> callback);

	void addResultFolder(String value, AsyncCallback<Integer> asyncCallback);

	void moveResult(int id, int oldParent, int newParent, String name, int commandId, AsyncCallback<Integer> asyncCallback);

	void deleteResult(int result, int jobId, AsyncCallback<Void> asyncCallback);

	void getSearchResults(String search, AsyncCallback<HashMap<String, Vector<SearchResultItem>>> asyncCallback);

	void changeResultName(String id, String newVal, AsyncCallback<Void> callback);

	void getUsersShares(int user, AsyncCallback<Vector<String[]>> callback);

	void getOthersShares(int user, AsyncCallback<Vector<String[]>> callback);

	void removeToken(String token, AsyncCallback<Void> asyncCallback);

	void getRequests(AsyncCallback<Vector<RequestItem>> callback);

	void changeStatus(int request, String newStatus, AsyncCallback<Void> callback);

	void getTokens(int user, AsyncCallback<Vector<String[]>> callback);

	void getHelpHTML(String topic, AsyncCallback<String> callback);

	void getHelpList(AsyncCallback<Vector<Help>> callback);

	void getTipList(AsyncCallback<Vector<String>> callback);

	void saveHelpTopic(Help help, AsyncCallback<Integer> callback);

	void getJob(int id, AsyncCallback<Job> callback);

	void removeJobNote(int id, AsyncCallback<Void> callback);

	void addNote(int job, String note, AsyncCallback<JobNote> callback);

	void getJobs(AsyncCallback<Vector<Job>> callback);

	void removeResult(int result, int job, AsyncCallback<Void> callback);

	void rerunJob(int job, AsyncCallback<Void> callback);

	void makeResultPublic(int job, AsyncCallback<Void> callback);

	void shareResult(int id, int user, String name, AsyncCallback<Void> callback);

	void killJob(int job, AsyncCallback<Integer> callback);

	void getNextWrappers(Wrapper wrapper, AsyncCallback<Vector<Wrapper>> callback);

	void checkForErrors(String fileId, AsyncCallback<String> callback);

	void getTempPlugin(String fileId, AsyncCallback<Plugin> callback);

	void saveTempPlugin(String fileId, AsyncCallback<Plugin> callback);

	void getOtherShares(AsyncCallback<Vector<Share>> callback);

	void getMyShares(AsyncCallback<Vector<Share>> callback);

	void reRunFailedJobs(AsyncCallback<Void> callback);

	void getChildJobs(int jobId, AsyncCallback<ArrayList<Job>> callback);

	void saveJob(Job job, AsyncCallback<Void> callback);

}
