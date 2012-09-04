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

import java.util.Vector;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.LabelButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.pipeline.PipelineWrapper;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class PipelineWorkspace extends Composite {
	private Image img = new Image("images/trash_can_close.png");
	private Workspace workspace;
	private TextBox nameBox;
	private TextArea descBox;
	private Pipeline pipeline;
	private static final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private FlowPanel userInputs = new FlowPanel();
	public PipelineWorkspace(Pipeline pipeline) {
		HorizontalPanel pane = new HorizontalPanel();
		initWidget(pane);
		this.pipeline=pipeline;
		workspace = new Workspace(pipeline);
		
		loadNonBlocks();
//		workspace.add(new ForBlock());
		pane.add(workspace);
		VerticalPanel infoPane = new VerticalPanel();
		FlexTable table = new FlexTable();
		table.setWidget(0, 0, new SimpleLabel("Name:"));
		nameBox = new TextBox();
		nameBox.setStyleName("eta-input2");
		nameBox.setText(pipeline.getName());
		table.setWidget(0, 1, nameBox);
		table.setWidget(1, 0, new SimpleLabel("Description:"));
		descBox = new TextArea();
		descBox.setText(pipeline.getDescription());
		descBox.setStyleName("eta-input2");
		descBox.setSize("100%", "100px");
		table.setWidget(2, 0, descBox);
		table.getFlexCellFormatter().setColSpan(1, 0, 2);
		table.getFlexCellFormatter().setColSpan(2, 0, 2);
		infoPane.setWidth("100%");
		table.setWidth("100%");
		infoPane.add(table);
		pane.add(infoPane);
		pane.setCellWidth(infoPane, "300px");
		PipelineWrapper temp=new PipelineWrapper(0, -1, -1);
		temp.setPipeline(new Pipeline("Foreach", "A foreach tool that will run the wrappers for every item in the input list", 0, true, -2));
		temp.getInputs().add(new Input(-1, "List of items", "Items", "", "", true, 0, "Default", "List"));
		temp.getOutputs().add(new Output("Foreach Item", "", "The item in the forloop", "${splitList($'Items')}", -1));
		table.setWidget(5, 0, new FunctionMiniBlock(temp));
		
		PipelineWrapper temp2=new PipelineWrapper(0, -1, -1);
		temp2.setPipeline(new Pipeline("If", "A  tool that will run the wrappers based on the conditional", 0, true, -3));
		temp2.getInputs().add(new Input(-1, "Condition", "Condition", "", "", true, 0, "Default", "List:File"));
		
		table.setWidget(5, 1, new FunctionMiniBlock(temp2));
		
		PipelineWrapper temp3=new PipelineWrapper(0, -1, -1);
		temp3.setPipeline(new Pipeline("Switch", "A  tool that will run the wrappers based a selection", 0, true, -3));
		temp3.getInputs().add(new Input(-1, "switch", "switch", "", "", true, 0, "Default", "String"));
		table.setWidget(5, 2, new FunctionMiniBlock(temp3));
		
		PipelineWrapper casePipe = new PipelineWrapper(0, -1, -1);
		casePipe.setPipeline(new Pipeline("Case", "Used in a switch statement to represent the steps when the switch value is equal to this case", 0, true, -3));
		table.setWidget(5, 3, new FunctionMiniBlock(casePipe));

		HorizontalPanel bar  = new HorizontalPanel();
		bar.setHeight("20px");
		bar.setStyleName("tab-bar");
		bar.add(new LabelButton("Pipeline inputs"));
		bar.setWidth("100%");
		table.setWidget(6, 0, bar);
		table.setWidget(7,0, userInputs);
		userInputs.setStyleName("pipeinput");
		table.getFlexCellFormatter().setColSpan(6, 0, 2);
		table.getFlexCellFormatter().setColSpan(7, 0, 2);

		workspace.setStyleName("pipe-workspace");
		workspace.setHeight("100%");
		for(Input input:pipeline.getInputs()){
			userInputs.add(new OutputBlock(new Output(input.getName(), input.getType(), input.getDescription(), input.getDefaultValue(), input.getId())));
		}
		userInputs.add(new OutputBlock(new Output("User Input", "", "This will force the user to provide this file", "", -1)));
		userInputs.add(new OutputBlock(new Output("Custom", "", "This will allow you to type in custom code to be used", "", -1)));
		userInputs.add(new OutputBlock(new Output("Working Folder", "Folder", "This will be the working folder that the user provides", "", -1)));

		

	}


	public void removeBlock(Block block) {
		workspace.remove(block);
	}



	public void add(Widget wid) {
		workspace.add(wid);
	}

	public void loadPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
		loadNonBlocks();
		workspace.loadPipeline(pipeline);
		userInputs.clear();
		for(Input input:pipeline.getInputs()){
			userInputs.add(new OutputBlock(new Output(input.getName(), input.getType(), input.getDescription(), input.getDefaultValue(), input.getId())));
		}
		userInputs.add(new OutputBlock(new Output("User Input", "", "This will force the user to provide this file", "", -1)));
		userInputs.add(new OutputBlock(new Output("Working Folder", "Folder", "This will be the working folder that the user provides", "", -1)));
		
	}

	private void loadNonBlocks() {
		HTML center = new HTML();
		center.setHeight("30px");
		center.setWidth("100px");
		center.setStyleName("start-block");
		center.setHTML("<div style='padding-top:5px;background:none;'>Start<div>");
		workspace.addNonBlock(center);

		img = new Image("images/trash_can_close.png");
		img.setStyleName("trash");
		workspace.addNonBlock(img);

		DragCreator.addDrop(img.getElement(), new TrashCan(), new DropListener() {
			public void drop(ETAType record) {
				img.setUrl("images/trash_can_close.png");
				DragCreator.getDragSource().setDragAction(DragCreator.DELETE);
			}

			public void dragOver(ETAType record) {
				img.setUrl("images/trash_can_open.png");
			}

			public void dragLeave(ETAType record) {
				img.setUrl("images/trash_can_close.png");
			}

			public void dragEnter(ETAType record) {
				img.setUrl("images/trash_can_open.png");
			}

		});		
	}


	public void save() {
		pipeline=workspace.getPipeline();
		pipeline.setName(nameBox.getText());
		pipeline.setDescription(descBox.getText());
		if (pipeline.getName().equals("") || pipeline.getDescription().equals("")) {
			SC.alert("Error saving pipeline", "Sorry but this pipeline must have a name and a descritpion in order to save");
			return;
		}

		wrapperService.savePipeline(pipeline, new MyAsyncCallback<Pipeline>() {
			@Override
			public void success(Pipeline result) {
				loadPipeline(result);
			}
		});

	}


	public void saveInputs(Vector<Input> data) {
		pipeline.setInputs(data);
		userInputs.clear();
		for(Input input:pipeline.getInputs()){
			userInputs.add(new OutputBlock(new Output(input.getName(), input.getType(), input.getDescription(), input.getDefaultValue(), input.getId())));
		}
		userInputs.add(new OutputBlock(new Output("User Input", "", "This will force the user to provide this file", "", -1)));
		userInputs.add(new OutputBlock(new Output("Working Folder", "Folder", "This will be the working folder that the user provides", "", -1)));
		
	}


	public Pipeline getPipeline() {
		return pipeline;
	}
}
