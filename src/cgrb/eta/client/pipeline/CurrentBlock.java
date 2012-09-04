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

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.shared.etatype.Job;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.touch.client.Point;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
public class CurrentBlock extends FlowPanel{
	
	private HTML text;
	private HTML currentAction;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private HandlerRegistration handler;
	private boolean hasControl;

	public CurrentBlock(boolean hasControl){
		this.hasControl=hasControl;
		setStyleName("currentBlock");
		text = new HTML("Running");
		text.setStyleName("status");
		currentAction = new HTML();
		HTML rightArrow = new HTML();
		add(currentAction);
		rightArrow.setStyleName("right-arrow");
		add(text);
		add(rightArrow);
	}

	public void changeJob(final Job job){
		text.setHTML(job.getStatus());
		if(!hasControl)
			return;
		if(handler!=null)
			handler.removeHandler();
		if(job.getStatus().equals("Running")||job.getStatus().startsWith("Waiting in queue")){
			currentAction.setHTML("||");
			currentAction.setStyleName("pause");

			handler = currentAction.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sqlService.killJob(job.getId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				}
			});
		}else if(job.getStatus().equals("Failed")||job.getStatus().startsWith("Cancelled")){
			currentAction.setHTML("");
			currentAction.setStyleName("restart");
			handler = currentAction.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					sqlService.rerunJob(job.getId(), new MyAsyncCallback<Void>() {
						@Override
						public void success(Void result) {
						}
					});
				}
			});
		}else{
			currentAction.setHTML("");
		}
	}
	public void moveTo(JobBlock temp) {
		getElement().getStyle().setTop(temp.getTop(), Unit.PX);
		getElement().getStyle().setLeft(temp.getLeft()-getOffsetWidth()-40,Unit.PX);
	}
	public void smoothMoveTo(JobBlock temp) {
		Point from = new Point(getAbsoluteLeft()-getParent().getAbsoluteLeft(), getAbsoluteTop()-getParent().getAbsoluteTop());
		Point to = new Point(temp.getLeft()-getOffsetWidth()-40,temp.getTop());
		new MoveAnimation(getElement().getStyle(), from, to).run(1000);
		getElement().getStyle().setTop(temp.getTop(), Unit.PX);
		getElement().getStyle().setLeft(temp.getLeft()-getOffsetWidth()-40,Unit.PX);
	}
	private class MoveAnimation extends Animation {
		private Style style;
		private Point from;
		private Point to;
		public MoveAnimation(Style style, Point from, Point to) {
			this.style=style;
			this.to=to;
			this.from=from;
		}

		@Override
		protected void onUpdate(double progress) {
			style.setLeft(from.getX()+((to.getX()-from.getX())*progress), Unit.PX);
			style.setTop(from.getY()+((to.getY()-from.getY())*progress), Unit.PX);
		}

		@Override
		public void cancel() {
			super.cancel();
			style.setLeft(to.getX(), Unit.PX);
			style.setTop(to.getY(), Unit.PX);
		}

	}

}
