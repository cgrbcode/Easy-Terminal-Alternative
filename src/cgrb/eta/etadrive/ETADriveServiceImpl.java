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
package cgrb.eta.etadrive;



import java.util.Vector;

import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.rmi.AESRMIConnection;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.shared.etatype.File;

public class ETADriveServiceImpl implements ETADriveService {

	private ETADriveClientService service;
	private RemoteUserService userCon;
	private String home;

	public ETADriveServiceImpl(int user) {
		userCon = UserManagerService.getService().getUserService(user);
		home = userCon.getHomePath();
	}

	@Override
	public String saveFileBuffer(String file, byte[] buffer, int length) {
		return userCon.saveFileBuffer(home + "/ETADrive/" + file, buffer, length);
	}

	@Override
	public void removeFile(String name) {
		Vector<File> files = new Vector<File>();
		files.add(new File( home + "/ETADrive/" + name ));
		userCon.removeFiles(files);
	}

	public void setConnection(AESRMIConnection connection) {
		service = (ETADriveClientService) connection.getService(ETADriveClientService.class);
	}

	@Override
	public void mkdir(String relativeToDrive) {
		System.out.println("making folder");
		userCon.makeFolder(home + "/ETADrive/"+relativeToDrive);
	}

	public ETADriveClientService getRemoteService(){
		return service;
	}

}
