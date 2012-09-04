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

import cgrb.eta.server.rmi.RemoteService;
import cgrb.eta.shared.etatype.Job;

public interface ClusterService extends RemoteService {
	public boolean saveJobFile(Integer job, String filename, byte[] bs);

	public boolean startJob(Integer job);

	/**
	 * This method will take a Job from another ETA instance and convert it over Steps are as follow: 
	 * 1. Check to see if this job can even run on this server 
	 * 2. Create a working folder in the public space and then change the working folder to this 
	 * 3. Change all the input files paths to the working-dir/name 
	 * 4. Save the standard files to the working folder 
	 * 5. Remove the job id from job and save it to mysql 
	 * 6. return true if all goes well!
	 * 
	 * @param job The raw job that is from the other instance and is being moved here
	 * @param globalCluster The id of the cluster this job is coming from
	 * 
	 * @return true if this job can run on this server and false otherwise
	 */
	public boolean setupJob(Job job, int globalCluster);
	
	public boolean jobStatusChange(Integer job, String status);
	
	public boolean saveOutFile(Integer jobId, String filename, byte[] bs);
}
