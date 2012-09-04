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

import java.io.File;
import java.util.Vector;

import cgrb.eta.etadrive.ETADriveServiceImpl;
import cgrb.eta.server.ClusterAPIService;
import cgrb.eta.server.CommunicationImpl;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Wrapper;

public class RemoteETAConnectionServiceImpl implements RemoteETAConnectionService {

	private int userId;

	public RemoteETAConnectionServiceImpl(int user) {
		userId = user;
	}

	@Override
	public void eventOccured(ETAEvent event) {
		CommunicationImpl.getInstance().eventOccured(event, userId);
	}

	@Override
	public int runJob(Job job, String[] command) {
		CommunicationImpl com = CommunicationImpl.getInstance();
		if (command.length > 0) {
			Wrapper wrapper = com.getWrapperFromCMD(command);
			job.setWrapper(wrapper);
		}
		job.setUserId(userId);
		int id = com.runJob(job, userId);
		return id;
	}

	@Override
	public void newFile(String file) {
		Vector<ETADriveServiceImpl> drives = ClusterAPIService.getInstance().getETADrives(userId);
		RemoteUserService etaStart =UserManagerService.getService().getUserService(userId);
		File driveFolder = new File(etaStart.getHomePath() + "/ETADrive");
		Vector<ETADriveServiceImpl> drivesToCopy = new Vector<>();
		for (ETADriveServiceImpl impl : drives) {
			// impl.saveFileBuffer(file, buffer, length)
			if (!impl.getRemoteService().fileExist(file)) {
				drivesToCopy.add(impl);
			}
		}
		byte[] buffer;
		do {
			buffer = etaStart.getFileBuffer(driveFolder.getAbsolutePath() + "/" + file);
			for (ETADriveServiceImpl impl : drivesToCopy) {
				impl.getRemoteService().saveFileBuffer(file, buffer, buffer.length);
			}
		} while (buffer.length > 0);

	}

	@Override
	public void fileRemoved(String name) {
		System.out.println("removing file " + name + " from drives");
		Vector<ETADriveServiceImpl> drives = ClusterAPIService.getInstance().getETADrives(userId);
		for (ETADriveServiceImpl impl : drives) {
			impl.getRemoteService().remoteRemoveFile(name);
		}
	}

}
