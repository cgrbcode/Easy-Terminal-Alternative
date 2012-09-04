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
package cgrb.eta.client.pipeline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.tabs.InputsTable;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.wrapper.Input;

public class PipelineCreator extends ETATab {

	PipelineWorkspace workspace;
	HorizontalPanel main = new HorizontalPanel();
	private static final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private int pipelineId = 0;
	private InputsTable inputs;
	private int inputsInt = -1;

	public PipelineCreator() {
		super("Pipeline creator");
		workspace = new PipelineWorkspace(new Pipeline("", "", ETA.getInstance().getUser().getId(), false, 0));
		workspace.setHeight("100%");
		workspace.setWidth("100%");
		main.setStyleName("pipe-table");
		main.add(workspace);
		main.setCellHeight(workspace, "100%");
		main.setHeight("100%");
		inputs = new InputsTable(false);
		inputs.getScrollPane().setHeight("500px");
		setPane(main);
	}

	@Override
	public String getId() {
		return "plc#" + pipelineId;
	}

	@Override
	public Widget getBar() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.add(new Button("Save").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				workspace.save();
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("Modify Inputs").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setupInputs();
			}
		}));
		bar.add(new Seprator());
		return bar;
	}

	public void setupInputs() {
		HorizontalPanel bar = new HorizontalPanel();
		inputs.setData(workspace.getPipeline().getInputs());
		bar.add(new ImgButton(Resources.INSTANCE.add(), "Add new Input").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Input newInput = new Input();
				newInput.setId(inputsInt--);
				newInput.setName("Untitled " + ((inputsInt * -1) - 1));
				addInput(newInput);
			}

		}));
		bar.add(new Seprator());
		SC.ask("Setup your inputs", inputs,bar,new ValueListener<Boolean>() {
			@Override
			public void returned(Boolean ret) {
				if(ret){
					//save the inputs back to the pipeline
					workspace.saveInputs(inputs.getData());
				}
			}
		});
	}

	private void addInput(Input input) {
		inputs.addInput(input);
	}

	/**
	 * @param result
	 */
	private void loadPipeline(Pipeline result) {
		main.remove(workspace);
		workspace = new PipelineWorkspace(result);
		workspace.setHeight("100%");
		workspace.setWidth("100%");
		main.insert(workspace, 0);
	}

	/**
	 * @param parseInt
	 */
	public void setPipeline(int pipeId) {
		pipelineId = pipeId;
		if (pipeId > 0) {
			wrapperService.getPipelineFromId(pipeId, new MyAsyncCallback<Pipeline>() {
				@Override
				public void success(Pipeline result) {
					loadPipeline(result);
				}
			});
		}

	}
}
