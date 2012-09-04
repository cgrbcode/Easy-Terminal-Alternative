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
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.client.wrapperrunner.JobOptions;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.pipeline.PipeComponent;
import cgrb.eta.shared.pipeline.PipeWrapper;
import cgrb.eta.shared.pipeline.PipelineWrapper;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class Block extends Composite implements DragListener {
	private PipeComponent wrapper;
	private HorizontalPanel inputPanel;
	private HorizontalPanel outputPanel;
	HorizontalPanel startBlock = new HorizontalPanel();
	private BlockChangeListener listener;
	private static final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	FlowPanel temp = new FlowPanel();
	private Workspace workspace;
	public Block(PipeComponent wrapper, BlockChangeListener listener) {
		this.listener = listener;
		startBlock.setStyleName("block");
		if (wrapper instanceof PipelineWrapper)
			startBlock.addStyleName("function");
		inputPanel = new HorizontalPanel();
		outputPanel = new HorizontalPanel();
		outputPanel.setVisible(false);
		inputPanel.setVisible(false);
		inputPanel.setStyleName("input-panel");
		outputPanel.setStyleName("output-panel");
		this.wrapper = wrapper;
		HTML expandInputs = new HTML();
		expandInputs.setStyleName("left-expand");
		expandInputs.setHTML("<div class='arrow2'></div>");
		startBlock.add(expandInputs);
		expandInputs.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				inputPanel.setVisible(!inputPanel.isVisible());
			}
		});

		startBlock.add(expandInputs);
		startBlock.add(inputPanel);
		HTML img = new HTML("<img src='images/wrench.png'/>");
		startBlock.add(img);
		img.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				edit();
			}
		});
		img.setStyleName("center");
		img.setHeight("30px");
		HTML center = new HTML();
		center.setHeight("30px");
		startBlock.add(center);
		center.setStyleName("center");
		center.setHTML("<div style='padding-top:5px;background:none;'>" + wrapper.getName() + "<div>");

		startBlock.add(outputPanel);

		HTML expandOutputs = new HTML();
		expandOutputs.setStyleName("right-expand");
		expandOutputs.setHTML("<div class='arrow2'></div>");
		startBlock.add(expandOutputs);
		expandOutputs.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				outputPanel.setVisible(!outputPanel.isVisible());
			}
		});

		temp.add(new Arrow());
		temp.add(startBlock);
		initWidget(temp);
		center.getElement().setAttribute("draggable", "true");

		DragCreator.addDrag(center.getElement(), wrapper, this);
		Vector<Input> inputs = wrapper.getInputs();
		if (listener == null)
			return;
		Vector<PipeComponent> wrappers = listener.getPreviousWrappers(wrapper.getPosition());
		outer: for (Input input : inputs) {
			if (input.getType().startsWith("File") || input.getType().startsWith("List")||wrapper.getName().equals("Switch")) {
				InputBlock block = new InputBlock(input);
				inputPanel.add(block);
				if (!input.getValue().equals("")) {
					block.setInputValue(new Output(input.getValue(), "", "", input.getValue(), -1));
					continue outer;
				}
				if (input.getType().equals("File"))
					continue outer;
				for (PipeComponent wrap : wrappers) {
					for (Output output : wrap.getOutputs()) {
						if (input.getType().length() <= 5 || input.getType().substring(5).startsWith(output.getType())) {
							block.setInputValue(output);
							continue outer;
						}
					}
				}
			}
		}
		for (Output output : wrapper.getOutputs()) {
			outputPanel.add(new OutputBlock(output));
		}

	}

	//private JobOptions options = new JobOptions();

	public void edit() {
		if (wrapper instanceof PipeWrapper) {
			final PipeWrapper wrapper = (PipeWrapper) this.wrapper;
			final JobOptions options = new JobOptions();
			HorizontalPanel bar = new HorizontalPanel();
			bar.add(new Seprator());
			bar.add(new Button("SGE Options").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					options.show();
				}
			}));
			bar.add(new Seprator());
			bar.add(new OutputBlock(new Output("User Input", "", "Make the user fill this field out", "", -2)));
			bar.add(new Seprator());
			Inputs inputs = new Inputs(wrapper.getWrapper());
			SC.ask("Configure inputs for step: " + wrapper.getWrapper().getName(), inputs, bar, new ValueListener<Boolean>() {
				public void returned(Boolean ret) {
					if (ret) {
						wrapper.setJobOptions(options.getSpecs());
					}
				}
			});
		} else if (wrapper instanceof PipelineWrapper) {
			if(workspace==null){
			PipelineWrapper wrapper = (PipelineWrapper) this.wrapper;
			workspace = new Workspace(wrapper.getPipeline());
			// temp.getElement().getStyle().setPosition(Position.ABSOLUTE);
			workspace.setStyleName("sub-workspace");
			
			temp.add(workspace);
			}else{
				temp.remove(workspace);
				workspace=null;
			}
		}
	}

	public void dragStart(ETAType record) {
		getElement().getStyle().setOpacity(.3);
	}

	public void dragEnter(ETAType record) {
	}

	public void dragOver(ETAType record) {
		ETAType rec = DragCreator.getDragSource();
		if (rec instanceof Output) {
			inputPanel.setVisible(true);
		} else if ((rec instanceof PipeComponent || rec instanceof UserWrapper) && !rec.equals(wrapper)) {
			addStyleName("hoverO");
		}
	}

	public void dragLeave(ETAType record) {
		removeStyleName("hoverO");
	}

	public void drop(ETAType record) {
		getElement().getStyle().setOpacity(1);
		ETAType rec = DragCreator.getDragSource();

		if (rec instanceof PipeComponent) {
			// assume that the source is being moved
			PipeComponent src = (PipeComponent) rec;
			if(src.getPosition()>=0)
			listener.blockMoved(src, wrapper.getPosition());
			else{
				listener.blockAdded(src.clone(), wrapper.getPosition());
			}
		} else if (rec instanceof UserWrapper) {
			UserWrapper wrapperU = (UserWrapper) rec;
			wrapperService.getWrapperFromId(wrapperU.getWrapperId(), new MyAsyncCallback<Wrapper>() {
				@Override
				public void success(Wrapper result) {
					PipeWrapper wrap = new PipeWrapper(-1, result.getId(), wrapper.getPosition());
					wrap.setWrapper(result);
					listener.blockAdded(wrap, wrapper.getPosition());
				}
			});
		}
		removeStyleName("hoverO");

	}

	public void dragEnd(ETAType record) {
		getElement().getStyle().setOpacity(1);
		int action = record.getDragAction();
		if (action == DragCreator.DELETE) {
			listener.blockRemoved(wrapper);
		}
		removeStyleName("hoverO");

	}

	public Element getDragImage(ETAType record) {
		return startBlock.getElement();
	}

}
