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
package cgrb.eta.shared.wrapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.shared.etatype.ETAType;

public class Wrapper extends ETAType {

	/**
	 * 
	 */
	private static final long serialVersionUID = 716607128574843142L;
	private Vector<Input> inputs;
	private Vector<Output> outputs;

	private String name;
	private String description;
	private String creator;
	private boolean isPublic;
	private String program;
	private boolean stared = false;
	private int creatorId;
	private int rating = 0;
	private HashMap<String, String> envVars;

	public void setEnvVars(HashMap<String, String> envVars) {
		this.envVars = envVars;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	private int publicId;

	public Wrapper(int id) {
		this.id = id;
		inputs = new Vector<Input>();
		outputs = new Vector<Output>();
		envVars = new HashMap<String, String>();
	}

	public Wrapper() {
		this.id = 0;
		inputs = new Vector<Input>();
		outputs = new Vector<Output>();
		envVars = new HashMap<String, String>();
	}

	public Vector<Input> getInputs() {
		Collections.sort(inputs);
		return inputs;
	}

	public Vector<Output> getOutputs() {
		return outputs;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getCreator() {
		return creator;
	}

	public int getPublicId() {
		return publicId;
	}

	public void addInput(Input input) {
		inputs.add(input);
	}

	public void addOutput(Output output) {
		outputs.add(output);
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public void setPublicId(int publicId) {
		this.publicId = publicId;
	}

	public void setInput(int inputId, String value) {
		for (Input input : inputs) {
			if (input.getId() == inputId) {
				input.setValue(value);
				return;
			}
		}
	}

	public void setStared(boolean stared) {
		this.stared = stared;
	}

	public boolean isStared() {
		return stared;
	}

	public int getRating() {
		return rating;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public String getCMD() {
		String cmd = program;
		for (Input par : getInputs()) {
			if (par.getFlag() != null && !par.getFlag().equals("")) {
				Input i = (Input) par;
				String value = i.getValue();
				if (i.getType().startsWith("Flag")) {
					if (value != null && value.equals("true")) {
						cmd += " " + i.getFlag();
					}
				} else if (i.getType().startsWith("Input-List")) {
					String[] lines = i.getValue().split("~~~");
					for (String line : lines) {
						String[] inputs = line.split("~`~");
						for (String input : inputs) {
							cmd += " " + input;
						}
					}
				} else if (value != null && !value.equals("")) {
					if (i.getFlag().endsWith("=")) {
						cmd += " " + i.getFlag() + i.getValue();
					} else {
						cmd += " " + i.getFlag() + " " + i.getValue();
					}
				}
			} else {
				if (par.getType().startsWith("Input-List")) {
					String[] lines = par.getValue().split("~~~");
					for (String line : lines) {
						String[] inputs = line.split("~`~");
						for (String input : inputs) {
							cmd += " " + input;
						}
					}
				} else if (par.getValue() != null && !par.getValue().equals("")) {
					cmd += " " + par.getValue();
				}
			}
		}
		return cmd;
	}

	public void addVar(String name, String value) {
		envVars.put(name, value);
	}

	public HashMap<String, String> getEnvVars() {
		return envVars;
	}

	public void setInputs(Vector<Input> inputs2) {
		inputs = inputs2;
	}

	public boolean setInput(String option, String value) {
		for (Input input : inputs) {
			if (input.getFlag().equals(option)) {
				input.setValue(value);
				return true;
			}
		}
		return false;
	}

	public Wrapper clone() {
		Wrapper ret = new Wrapper(id);
		ret.setCreator(getCreator());
		ret.setDescription(getDescription());
		ret.setEnvVars(getEnvVars());
		ret.setName(getName());
		ret.setProgram(getProgram());
		ret.setPublic(isPublic());
		ret.setPublicId(publicId);

		Vector<Input> inputRet = new Vector<Input>();
		for (Input in : inputs) {
			inputRet.add(in.clone());
		}
		ret.setInputs(inputRet);
		for (Output out : outputs) {
			ret.addOutput(out.clone());
		}
		return ret;
	}
}
