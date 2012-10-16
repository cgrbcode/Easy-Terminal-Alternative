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

import java.util.Vector;

import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.PendingCluster;
import cgrb.eta.shared.etatype.QJob;
import cgrb.eta.shared.etatype.RequestItem;
import cgrb.eta.shared.etatype.User;

import com.google.gwt.rpc.client.RpcService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("communication")
public interface CommunicationService extends RpcService {
	
	//methods that never need to change per instance
	int saveRequest(String type, String summary, String description);
	void starRequest(boolean star, int request);
	RequestItem getRequest(int id);
	void removeRequestResponse(int id);
	void removeRequest(int id);
	void addComment(String comment, int request);
	void removeTokens();
	void associateToken(String external, String site);
	Vector<Job> getJobs();
	void closeRequest(boolean close, int request);
	void saveUser(User user);
	String requestAccess(String cluster);
	String acceptClusterRequest(int request);
	String rejectClusterRequest(int request);
	String autoAcceptClusterRequest(int request);
	Vector<PendingCluster> getPendingClusters();
	Vector<PendingCluster> getUserPendingClusters();
	Vector<ETAEvent> getEvents();
	String startSession(int plugin, String files);
	String startSession(String url, String files);
	void listenForJob(int job, String id);
	void stopListeningJob(int job, String id);
	String[] getETASettings();
	String saveETASettings(String[] settings);
	void saveRequestFile(int request, String file);
	User getUser();
	

	
	//methods that I think should be removed
	public Vector<QJob> getJobsForMachine(String machine);
	String[] getMachines();

	
	
	//methods that can be different per installation
	Vector<File> getFiles(String browser, String path);
	Vector<File> back(String browser, String path);
	Vector<File> getHistory(String browser);
	//changed to string so that it could give us the error message
	String removeFiles(Vector<File> files);
	boolean logIn(String user, String password);
	String getFileContents(File file, long startByte, long bytes);
	void createFolder(String path);
	int changePassword(String oldPassword, String newPassword);
	String writeFile(String file, String contents);
	public String[] getCommandsInPath();
	public boolean isInstalled(String program);
	public String saveFileBuffer(String file,byte[] contents);
	public boolean moveFile(String from, String to);
	public boolean linkFile(String from, String to);
	public boolean copyFile(String from, String to);
	public boolean compressFiles(String type,String[] files, String to);
	public boolean deCompressFile(String type,String archive,String where);
	String cancelJob(int job);
	public String[] getQueues();
	public String[] getThreadEnviroments();
	public void requestPwChange(String body, String accountName);
	
	//methods that need to be changed to be more modular
	void runQmod(Vector<String> jobs, String wrapper);
	public String[][] getResourcesForMachine(String machine, String[] resources);
	
}
