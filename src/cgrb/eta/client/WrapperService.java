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

import cgrb.eta.shared.ResultSettings;
import cgrb.eta.shared.etatype.EWrapper;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.pipeline.UserPipeline;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.rpc.client.RpcService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("wrapper")
public interface WrapperService extends RpcService{
	public Vector<UserWrapper> getUsersWrappers();
	public int moveWrapper(int id, int oldParent, int newParent, String name, int wrapperId);
	Vector<Wrapper> getWrappers();
	void rateWrapper(int rating,int wrapper);
	public int addFolder(String name);
	int runJob(Job job);
	void addNotification(int user,int job);
	Wrapper getWrapperFromCMD(String[] cmd);
	String generatePerlTemplate(int wrapperId,String path);
	Vector<UserPipeline> getUsersPipelines();
	Pipeline getPipelineFromId(int pipelineId);
	void sharePipeline(int pipelineId, int userId,String name);
	int movePipeline(int id, int i, int j, String string, int pipelineId);
	int addPipelineFolder(String ret);
	Vector<Pipeline> getPipelines();
	void ratePipeline(Integer ret, int id);
	Pipeline savePipeline(Pipeline pipeline);
	void makePipelinePublic(int pipelineId);
	public void makePublic(int wrapper);
	Wrapper saveWrapper(Wrapper wrapper);
	public Wrapper getWrapperFromId(int id);
	public int saveExternalWrapper(EWrapper wrapper);
	public Vector<EWrapper> getExternalWrappers();

	
	boolean doesWrapperExist(int user, boolean isPublic, String name);
	public Job getJob(int job);
	ResultSettings getJobResultSettings(int parseInt, int userId);
	public void addShareJobResult(int user,int job);
	public void removeShareJobResult(int user,int job);
	public void removeExternalWrapper(int id);
	public void shareWrapper(int wrapper,int user,String name);
	
	public Vector<Input> getVectorObjFromJson(String str);
	String toJsonArr(Vector<Input> obj);

	
}
