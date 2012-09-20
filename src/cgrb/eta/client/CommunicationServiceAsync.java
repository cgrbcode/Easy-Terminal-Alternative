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
import cgrb.eta.shared.etatype.PendingCluster;
import cgrb.eta.shared.etatype.QJob;
import cgrb.eta.shared.etatype.RequestItem;
import cgrb.eta.shared.etatype.User;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CommunicationServiceAsync {

	void getCommandsInPath( AsyncCallback<String[]> callback);

	void getEvents( AsyncCallback<Vector<ETAEvent>> callback);

	void getFiles(String browser, String path, AsyncCallback<Vector<File>> callback);

	void getJobs(AsyncCallback<Vector<cgrb.eta.shared.etatype.Job>> callback);

	void getJobsForMachine(String machine, AsyncCallback<Vector<QJob>> callback);

	void getMachines(AsyncCallback<String[]> callback);

	void getResourcesForMachine(String machine, String[] resources, AsyncCallback<String[][]> callback);

	void isInstalled( String program, AsyncCallback<Boolean> callback);

	void runQmod(Vector<String> jobs, String command, AsyncCallback<Void> callback);

	void startSession( int plugin, String files, AsyncCallback<String> callback);

	void listenForJob(int job, String id, AsyncCallback<Void> callback);

	void stopListeningJob(int job, String id, AsyncCallback<Void> callback);

	void cancelJob(int job, AsyncCallback<String> callback);

	void startSession( String url, String files, AsyncCallback<String> callback);

	void getETASettings(AsyncCallback<String[]> callback);

	void saveETASettings(String[] settings, AsyncCallback<String> callback);

	void saveRequest(String type, String summary, String description, AsyncCallback<Integer> callback);

	void addComment(String comment,int request ,AsyncCallback<Void> callback);

	void starRequest(boolean star, int request, AsyncCallback<Void> callback);

	void saveRequestFile(int request, String file, AsyncCallback<Void> callback);

	void getRequest(int id,AsyncCallback<RequestItem> callback);

	void removeRequestResponse(int id, AsyncCallback<Void> callback);

	void removeRequest(int id, AsyncCallback<Void> callback);

	void getUser(AsyncCallback<cgrb.eta.shared.etatype.User> callback);

	void associateToken(String external, String site, AsyncCallback<Void> callback);

	void logIn(String user, String password, AsyncCallback<Boolean> callback);

	void removeTokens(AsyncCallback<Void> callback);

	void getFileContents(File file, long startByte, long bytes, AsyncCallback<String> callback);

	void createFolder(String path, AsyncCallback<Void> callback);

	void changePassword(String oldPassword, String newPassword, AsyncCallback<Integer> callback);

	void closeRequest(boolean close, int request, AsyncCallback<Void> callback);

	void saveUser(User user, AsyncCallback<Void> callback);

	void writeFile(String file, String contents, AsyncCallback<String> callback);

	void back(String browser, String path, AsyncCallback<Vector<File>> callback);

	void getHistory(String browser, AsyncCallback<Vector<File>> callback);

	void requestAccess(String cluster, AsyncCallback<String> callback);

	void acceptClusterRequest(int request, AsyncCallback<String> callback);

	void rejectClusterRequest(int request, AsyncCallback<String> callback);

	void autoAcceptClusterRequest(int request, AsyncCallback<String> callback);

	void getPendingClusters(AsyncCallback<Vector<PendingCluster>> callback);

	void getUserPendingClusters(AsyncCallback<Vector<PendingCluster>> callback);

	void removeFiles(Vector<File> files, AsyncCallback<Boolean> callback);

	void saveFileBuffer(String file, byte[] contents, AsyncCallback<String> callback);

	void moveFile(String from, String to, AsyncCallback<Boolean> callback);

	void linkFile(String from, String to, AsyncCallback<Boolean> callback);

	void copyFile(String from, String to, AsyncCallback<Boolean> callback);

	void compressFiles(String type, String[] files, String to, AsyncCallback<Boolean> callback);

	void deCompressFile(String type, String archive, String where, AsyncCallback<Boolean> callback);

	void getQueues(AsyncCallback<String[]> callback);

	void getThreadEnviroments(AsyncCallback<String[]> callback);

}
