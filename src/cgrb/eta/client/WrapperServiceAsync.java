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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WrapperServiceAsync {
	void doesWrapperExist(int user, boolean isPublic, String name, AsyncCallback<Boolean> callback);
	void getUsersWrappers( AsyncCallback<Vector<UserWrapper>> callback);
	void getWrapperFromId(int id, AsyncCallback<Wrapper> callback);
	void makePublic(int wrapper, AsyncCallback<Void> callback);
	void getWrappers(AsyncCallback<Vector<Wrapper>> callback);
	void moveWrapper(int id, int oldParent, int newParent, String name, int wrapperId, AsyncCallback<Integer> callback);
	void saveWrapper(Wrapper wrapper, AsyncCallback<Wrapper> callback);
	void addFolder(String name, AsyncCallback<Integer> callback);
	void getJob(int job, AsyncCallback<Job> callback);
	void getJobResultSettings(int parseInt, int userId, AsyncCallback<ResultSettings> asyncCallback);
	void addShareJobResult(int user, int job, AsyncCallback<Void> callback);
	void removeShareJobResult(int user, int job, AsyncCallback<Void> callback);
	void saveExternalWrapper(EWrapper wrapper, AsyncCallback<Integer> callback);
	void getExternalWrappers(AsyncCallback<Vector<EWrapper>> callback);
	void removeExternalWrapper(int id, AsyncCallback<Void> callback);
	void shareWrapper(int wrapper, int user, String name, AsyncCallback<Void> callback);
	void rateWrapper(int rating, int wrapper, AsyncCallback<Void> callback);
	void runJob(Job job, AsyncCallback<Integer> callback);
	void addNotification(int user, int job, AsyncCallback<Void> callback);
	void getWrapperFromCMD(String[] cmd, AsyncCallback<Wrapper> callback);
	void generatePerlTemplate(int wrapperId, String path, AsyncCallback<String> callback);
	/**
	 * @param myAsyncCallback
	 */
	void getUsersPipelines(AsyncCallback<Vector<UserPipeline>> myAsyncCallback);
	/**
	 * @param pipelineId
	 * @param myAsyncCallback
	 */
	void getPipelineFromId(int pipelineId, AsyncCallback<Pipeline> myAsyncCallback);
	/**
	 * @param pipelineId
	 * @param userId
	 * @param name String the name of the pipeline or folder
	 * @param myAsyncCallback
	 */
	void sharePipeline(int pipelineId, int userId,String name, AsyncCallback<Void> myAsyncCallback);
	/**
	 * @param id
	 * @param i
	 * @param j
	 * @param string
	 * @param pipelineId
	 * @param myAsyncCallback
	 */
	void movePipeline(int id, int i, int j, String string, int pipelineId, AsyncCallback<Integer> myAsyncCallback);
	/**
	 * @param ret
	 * @param myAsyncCallback
	 */
	void addPipelineFolder(String ret, AsyncCallback<Integer> myAsyncCallback);
	/**
	 * @param myAsyncCallback
	 */
	void getPipelines(AsyncCallback<Vector<Pipeline>> myAsyncCallback);
	/**
	 * @param retInput
	 * @param id
	 * @param voidAsyncCallback
	 */
	void ratePipeline(Integer ret, int id, AsyncCallback<Void> voidAsyncCallback);
	/**
	 * @param pipeline
	 * @param myAsyncCallback
	 */
	void savePipeline(Pipeline pipeline, AsyncCallback<Pipeline> myAsyncCallback);
	/**
	 * @param pipelineId
	 * @param myAsyncCallback
	 */
	void makePipelinePublic(int pipelineId, AsyncCallback<Void> myAsyncCallback);
	void toJsonArr(Vector<Input> obj, AsyncCallback<String> callback);

	void getVectorObjFromJson(String str, AsyncCallback<Vector<Input>> callback);

}
