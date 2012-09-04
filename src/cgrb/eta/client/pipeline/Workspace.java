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
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.pipeline.PipeComponent;
import cgrb.eta.shared.pipeline.PipeWrapper;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.pipeline.PipelineWrapper;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class Workspace extends FlowPanel implements DropListener, BlockChangeListener {
	private static final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private Vector<PipeComponent> wrappers;
	private int offset = 1;
	private Pipeline pipe;
	private int id = 0;
	private static int onId = 0;

	public Workspace(Pipeline pipeline) {
		id = onId++;
		loadPipeline(pipeline);
		pipe = pipeline;
		DragCreator.addDrop(getElement(), null, this);
		HTML temp = new HTML();
		temp.setHeight("1px");
		temp.setWidth("400px");
		add(temp);
	}

	public void dragEnter(ETAType recod) {
		ETAType record = DragCreator.getDragSource();
		if (record instanceof UserWrapper) {
			// addStyleName("hover");
		}
	}

	public void dragOver(ETAType recor) {
		ETAType record = DragCreator.getDragSource();
		if (record instanceof UserWrapper) {
			// addStyleName("hover");
		}
	}

	public void dragLeave(ETAType record) {
		removeStyleName("hover");
	}

	public void drop(ETAType reco) {
		ETAType record = DragCreator.getDragSource();
		
			if(pipe.getName().equals("Switch")){
				if(record instanceof PipelineWrapper){
					PipelineWrapper droping = (PipelineWrapper)record;
					if(droping.getName().equals("Case")){
						SC.ask("What is the value of the case?", new ValueListener<String>() {
							@Override
							public void returned(String ret) {
								if(ret!=null&& !ret.equals("")){
									PipelineWrapper casePipe = new PipelineWrapper(0, -1, -1);
									casePipe.setPipeline(new Pipeline("Case:"+ret, "", 0, true, -1));
									wrappers.add(casePipe);
									add(new Block(casePipe, Workspace.this));
									for (int i = 0; i < wrappers.size(); i++) {
										wrappers.get(i).setPosition(i);
									}
								}
							}
						});
					}
				}
				removeStyleName("hover");
				return;
			}
		
		
		// if (record instanceof PipeComponent) {
		// PipeComponent wrap = (PipeComponent) record;
		// if (wrap.getPosition() == -1) {
		// // this is new and being dragged on
		// wrap.setPosition(wrappers.size());
		// wrappers.add(wrap);
		// add(FunctionBlock.getBlock(wrap, this));
		// } else {
		// // this is being moved handle it!
		// Widget wid = getWidget(wrap.getPosition() + offset);
		// remove(wrap.getPosition() + offset);
		// add(wid);
		// wrappers.remove(wrap.getPosition());
		// wrappers.add(wrap);
		// for (int i = 0; i < wrappers.size(); i++) {
		// wrappers.get(i).setPosition(i);
		// }
		// }
		// } else

		if (record instanceof UserWrapper) {
			UserWrapper wrapper = (UserWrapper) record;
			wrapperService.getWrapperFromId(wrapper.getWrapperId(), new MyAsyncCallback<Wrapper>() {
				@Override
				public void success(Wrapper result) {
					PipeWrapper wrap = new PipeWrapper(-1, result.getId(), wrappers.size());
					wrap.setWrapper(result);
					wrappers.add(wrap);
					add(new Block(wrap, Workspace.this));
					for (int i = 0; i < wrappers.size(); i++) {
						wrappers.get(i).setPosition(i);
					}
				}
			});
		} else if (record instanceof PipeComponent) {
			// assume this is being moved and add to the bottom of the stack
			PipeComponent wrap = (PipeComponent) record;
			if (wrap.getPosition() >= 0) {
				Widget wid = getWidget(wrap.getPosition() + offset);
				remove(wrap.getPosition() + offset);
				add(wid);
				wrappers.remove(wrap.getPosition());
				wrappers.add(wrap);
				for (int i = 0; i < wrappers.size(); i++) {
					wrappers.get(i).setPosition(i);
				}
			} else {
				wrap = wrap.clone();
				wrappers.add(wrap);
				add(new Block(wrap, Workspace.this));
				for (int i = 0; i < wrappers.size(); i++) {
					wrappers.get(i).setPosition(i);
				}
			}
		}
		removeStyleName("hover");
	}

	public void dragEnd(ETAType record) {
		removeStyleName("hover");
	}

	public void blockMoved(PipeComponent wrapper, int before) {
		if (wrapper.getPosition() == before)
			return;
		Widget wid = getWidget(wrapper.getPosition() + offset);
		remove(wrapper.getPosition() + offset);
		wrappers.remove(wrapper.getPosition());
		if (wrapper.getPosition() > before) {
			insert(wid, before + offset);
			wrappers.insertElementAt(wrapper, before);
		} else {
			insert(wid, before + offset - 1);
			wrappers.insertElementAt(wrapper, before - 1);
		}
		for (int i = 0; i < wrappers.size(); i++) {
			wrappers.get(i).setPosition(i);
		}
	}

	public void blockRemoved(PipeComponent wrapper) {
		remove(wrapper.getPosition() + offset);
		wrappers.remove(wrapper.getPosition());
		for (int i = 0; i < wrappers.size(); i++) {
			wrappers.get(i).setPosition(i);
		}
	}

	public void blockAdded(PipeComponent wrapper, int before) {
		insert(new Block(wrapper, this), before + offset);
		wrappers.insertElementAt(wrapper, before);
		for (int i = 0; i < wrappers.size(); i++) {
			wrappers.get(i).setPosition(i);
		}
		removeStyleName("hover");
	}

	public Vector<PipeComponent> getPreviousWrappers(int position) {
		Vector<PipeComponent> ret = new Vector<PipeComponent>();
		for (int i = position - 1; i >= 0; i--) {
			ret.add(wrappers.get(i));
		}
		return ret;
	}

	public void addNonBlock(Widget w) {
		insert(w, 0);
		offset++;
	}

	/**
	 * @param pipeline
	 */
	public void loadPipeline(Pipeline pipeline) {
		pipe = pipeline;
		wrappers = pipeline.getSteps();

		clear();
		offset=1;
		HTML temp = new HTML();
		temp.setHeight("1px");
		temp.setWidth("400px");
		add(temp);
		for (PipeComponent wrapper : wrappers) {
			// if (wrapper instanceof PipeWrapper)
			add(new Block(wrapper, this));
		}
	}

	public int getId() {
		return id;
	}

	public Pipeline getPipeline() {
		return pipe;
	}
}
