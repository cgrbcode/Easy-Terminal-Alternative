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
package cgrb.eta.shared.pipeline;

import java.util.Vector;

import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;

public class Pipeline extends ETAType {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4189104767853411003L;

	private Vector<PipeComponent> steps;
	private String name;
	private String description;
	private int creatorId;
	private String creator;
	private boolean isPublic;
	private int rating = 0;
	private boolean stared = false;
	private Vector<Input> inputs;
	private Vector<Output> outputs;

	public Pipeline() {
	}

	/**
	 * @param name
	 *          the name of the pipeline
	 * @param description
	 *          of the pipeline
	 * @param creator
	 *          the id of the creator of the pipeline
	 * @param isPublic
	 *          boolean for if the pipeline is public
	 */
	public Pipeline(String name, String description, int creator, boolean isPublic, int id) {
		super();
		this.name = name;
		this.description = description;
		this.creatorId = creator;
		this.isPublic = isPublic;
		this.id = id;
		steps = new Vector<PipeComponent>();
		inputs = new Vector<Input>();
		outputs= new Vector<Output>();
	}

	public Vector<PipeComponent> getSteps() {
		return steps;
	}

	public void setSteps(Vector<PipeComponent> steps) {
		this.steps = steps;
	}

	public void addStep(PipeComponent step) {
		steps.add(step);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	/**
	 * @return
	 */
	public int getRating() {
		return rating;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public boolean isStared() {
		return stared;
	}

	public void setStared(boolean stared) {
		this.stared = stared;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Vector<Input> getInputs() {
		return inputs;
	}

	public void addInput(Input in) {
		inputs.add(in);
	}

	public void setInputs(Vector<Input> data) {
		inputs=data;
	}
	public Vector<Output> getOutputs() {
		return outputs;
	}

	public void addOutput(Output in) {
		outputs.add(in);
	}

	public void setOutputs(Vector<Output> data) {
		outputs=data;
	}
	public void setInput(int inputId, String value) {
		for (Input input : inputs) {
			if (input.getId() == inputId) {
				input.setValue(value);
				return;
			}
		}
	}
	
	public Pipeline clone(){
		Pipeline ret = new Pipeline(this.name, this.description, this.creatorId, this.isPublic, this.id);
		ret.setOutputs(getOutputs());
		for(PipeComponent com:steps){
			ret.addStep(com.clone());
		}
		for(Input in:inputs){
			ret.addInput(in.clone());
		}
		return ret;
	}
}
