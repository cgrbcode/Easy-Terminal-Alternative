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
package cgrb.eta.server.remote.etastart;

import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.server.remote.MalformedQueryException;
import cgrb.eta.server.rmi.RemoteService;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.wrapper.Wrapper;

/**
 * This is the server side interface to ETA for ETAStart, which is service that 
 * runs as each user and gives sandboxed access to the infustructre.
 * Any call from ETA by the user goes though this service
 * @author Alexander Boyd
 *
 */
public interface RemoteUserService extends RemoteService{

	/**
	 * Returns a vector of files that are in the folder path
	 * <p>
	 * If the path is empty the path should be the last known path
	 * and if that is empty than the files in the home folder should 
	 * be returned.
	 * 
	 * @param path An absolute path of the folder that will be listed
	 * @return A list of files that are in the specified path
	 * @see File
	 */
	public Vector<File> getFiles(String path);
	
	
	/**
	 * This will return a list of files that are in the folder path.
	 * It will also keep track of the history for the browser specified.
	 * 
	 * @param browser The name of the browser that is requesting the file list.
	 * @param path The path of the folder that will be listed
	 * @return A list of all the files in the folder path
	 * @see File
	 */
	public Vector<File> navigateTo(String browser, String path);
	public Vector<File> back(String browser, String path);
	public Vector<File> getHistory(String browser);
	public String getFileContents(String file, long startByte, long bytes);
	public int runJob(Job job);
	public void runQmod(Vector<String> jobs, String command);
	
	public String getHomePath();
	
	public String tail(String file,int lines);
	
	
	//should change these in the future
	public String[] getCommandsInPath();
	public boolean isInPath(String program);
	//public String runSystemCommand(String[] args,String workingFolder);
	public String generatePerlTemplate(Wrapper wrapper,File where);
	public String runPluginCommand(String[] command, String workingDir);
	public byte[] getFileBuffer(String file);
	public String saveFileBuffer(String file, byte[] buff, int length);
	//this should really be changed! make to make random dir. not sure what it does right now
	public String makedir(String where);
	public void saveEmail(String emailAddress);
	public void terminate();
	public String hashWrapper(Wrapper wrapper);
	public String[][] runQuery(HashMap<String, String> query, String filepath) throws MalformedQueryException;
	public String cancelJob(int jobId);
	public void link(File target, File src);
	public String hashFile(File file);
	public void removeResult(int jobId);
	public long getFileSize(String path);
	public String downloadFile(String url);

	public Vector<UserWrapper> getUserWrappers();
	public Vector<Wrapper> getPublicWrappers();

	public void makeFolder(String string);
	public void statusChanged(int jobId);


	public void removeFiles(Vector<cgrb.eta.shared.etatype.File> files);


	public boolean moveFile(String from, String to);


	public boolean linkFile(String from, String to);


	public boolean copyFile(String from, String to);


	public boolean compressFiles(String type, String[] files, String to);


	public boolean deCompressFile(String type, String archive, String where);


	public String[] getQueues();


	public String[] getThreadEnviroments();


}
