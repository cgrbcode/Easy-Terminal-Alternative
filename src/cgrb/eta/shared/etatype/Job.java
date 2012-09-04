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
package cgrb.eta.shared.etatype;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

public class Job extends ETAType {

	/**
         * 
         */
	private static final long serialVersionUID = -8719604974111461086L;

	private String workingDir;
	private String name;
	private String user;
	private int userId;
	private String status;
	private Date date;
	private String machine;
	private Wrapper wrapper;
	private Pipeline pipeline;
	private String runTime;
	private String finishedTime;
	private String submitTime;
	private String specs;
	private boolean saveStd = false;
	private int parent;
	private Vector<JobNote> notes;
	private int waitingFor = 0;
	private int pipelineId;
	private boolean isPublic = false;
	private String stdoutPath = "";
	private int globalCluster = 0;

	public int getWaitingFor() {
		return waitingFor;
	}

	public int getGlobalCluster() {
		return globalCluster;
	}

	public void setGlobalCluster(int globalCluster) {
		this.globalCluster = globalCluster;
	}

	public void setWaitingFor(int waitingFor) {
		this.waitingFor = waitingFor;
	}

	public boolean isSaveStd() {
		return saveStd;
	}

	public void setSaveStd(boolean saveStd) {
		this.saveStd = saveStd;
	}

	public Job() {
		notes = new Vector<JobNote>();
	}

	public String getWorkingDir() {
		return workingDir;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public Wrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(Wrapper wrapper) {
		this.wrapper = wrapper;
	}

	public void setInput(int inputId, String value) {
		if(wrapper!=null)
		wrapper.setInput(inputId, value);
		else if(pipeline!=null)
			pipeline.setInput(inputId, value);
	}

	public String getSpecs() {
		return specs;
	}

	public void setSpecs(String specs) {
		this.specs = specs;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public int getParent() {
		return parent;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public String getFinishedTime() {
		return finishedTime;
	}

	public void setFinishedTime(String finishedTime) {
		this.finishedTime = finishedTime;
	}

	public String getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}

	public Vector<JobNote> getNotes() {
		return notes;
	}

	public void addNote(JobNote note) {
		notes.add(note);
	}

	public Vector<File> getOutputFiles() {
		if(wrapper==null)
			return null;
		HashMap<String, String> inputValues = new HashMap<String, String>();
		Vector<File> files = new Vector<File>();
		if (saveStd) {
			File std = new File("Std Out", getWorkingDir() + "/stdout", "File", 0, -1,0);
			File stdErr = new File("Std Error", getWorkingDir() + "/stderr", "File", 0, -2,0);
			files.add(std);
			files.add(stdErr);
		} else {
			File std;
			if (getStdoutPath().trim().equals(""))
				std = new File("Std Out", "$HOME/ETA/" + getId() + ".std", "File", 0, -1,0);
			else {
				if (getStdoutPath().startsWith("/"))
					std = new File("Std Out", getStdoutPath(), "File", 0, -1,0);
				else
					// std = new File("Std Out", "$HOME/ETA/" + getId() + ".std", "File", 0, -1);
					std = new File("Std Out", getWorkingDir() + "/" + getStdoutPath(), "File", 0, -1,0);
			}
			File stdErr = new File("Std Error", "$HOME/ETA/" + getId() + ".err", "File", 0, -2,0);
			files.add(std);
			files.add(stdErr);
		}
		for (Input input : wrapper.getInputs()) {
			inputValues.put(input.getName(), input.getValue());
		}
		inputValues.put("stdout", files.get(0).getPath());
		inputValues.put("stderr", files.get(1).getPath());

		for (Output ou : wrapper.getOutputs()) {
			String file = "";
			if (!workingDir.isEmpty()) {
				file = workingDir + "/";
			}
			if (ou.getValue() != null) {
				char[] tempValue = ou.getValue().toCharArray();
				String realValue = "";
				String var = "";
				boolean inInput = false;
				for (char ch : tempValue) {
					if (inInput) {
						if (ch == '\'' && !var.isEmpty()) {
							// get the value for the input named var
							realValue += inputValues.get(var);
							var = "";
							inInput = false;
						} else {
							if (ch != '\'') {
								var += ch;
							}
						}
					} else if (ch == '$') {
						inInput = true;
					} else {
						realValue += ch;
					}
				}
				file += realValue;
				if (realValue.startsWith("/"))
					file = realValue;
				File fil = new File(ou.getName(), file, ou.getType(), 0, ou.getId(),0);
				fil.setUser(getUserId());
				files.add(fil);
			}
		}
		return files;
	}

	public void setPipeline(int pipelineId) {
		this.pipelineId = pipelineId;
	}

	public int getPipeline() {
		if(pipeline!=null)
			return pipeline.getId();
		return pipelineId;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isPublic() {
		return isPublic;
	}

	/**
	 * @param pipeline2
	 */
	public void setPipeline(Pipeline pipeline) {
		pipelineId = pipeline.getId();
		this.pipeline = pipeline;
	}

	public Pipeline getPipelineObject() {
		return pipeline;
	}

	public String getStdoutPath() {
		if (stdoutPath == null)
			return "";
		return stdoutPath;
	}

	public void setStdoutPath(String stdoutPath) {
		if (stdoutPath != null) {
			this.stdoutPath = stdoutPath;
			if(this.stdoutPath.equals("!")){
				this.stdoutPath="";
				this.saveStd=true;
			}
		}
	}

}
