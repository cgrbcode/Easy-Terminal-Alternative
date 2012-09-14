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
package cgrb.eta.server.remote.etamonitor;

import cgrb.eta.server.rmi.RemoteService;
import cgrb.eta.shared.etatype.Job;

/**
 * This serves as the client side interface to ETA from the ETAMonitor tool
 * The purpose of this is to tell ETA that the job has been started 
 * and in the future this will provide a way to communicate to ETA 
 * when the job is ran interactively.
 * 
 * @see RemoteMonitorService RemoteMonitorService for the server side interface 
 * @author Alexander Boyd
 *
 */
public interface MonitorService extends RemoteService{

	/**
	 * The purpose of this method is to notify ETA that a job is being started
	 * so ETA can notify the owner of this job that it has started.
	 * 
	 * @param jobId The ETA job id of the job that just started.
	 * @param machine The machine name of the machine the job is running on.
	 */
	public void jobStarted(int jobId,String machine);
	
	/**
	 * The purpose of this method is to tell ETA if this job finished properly.
	 * 
	 * @param jobId The ETA job id of the job that just started.
	 * @param exitCode The exitCode that the job returned.
	 */
	public void jobFinished(int jobId, int exitCode);
	public Job jobFinishedWithNext(int jobId, int exitCode);
	public Job getJob(int id);
}
