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

import cgrb.eta.client.ETA;
import cgrb.eta.client.VoidAsyncCallback;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.tabs.ResultViewer;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.client.wrapperrunner.JobOptions;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Output;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;

public class JobBlock extends FlowPanel {
	protected Job job;
	private HandlerRegistration handler;
	private HTML edit = new HTML("<img src='images/wrench.png'/>");
	private HTML view = new HTML("<img src='" + Resources.INSTANCE.searchWhite().getSafeUri().asString() + "'/>");
	protected BlockDisplayListener listener;

	protected JobBlock() {
	}

	public JobBlock(final Job job) {
		HTML jobName = new HTML(job.getName());
		jobName.setStyleName("name");
		add(jobName);
		setJob(job);
		edit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				HorizontalPanel bar = new HorizontalPanel();
				bar.add(new Seprator());
				final JobOptions options = new JobOptions();
				bar.add(new Button("SGE Options").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						options.show();
					}
				}));
				bar.add(new Seprator());
				bar.add(new OutputBlock(new Output("User Input", "", "Make the user fill this field out", "", -2)));
				bar.add(new Seprator());
				Inputs inputs = new Inputs(job.getWrapper());
				SC.ask("Configure inputs for step: " + job.getWrapper().getName(), inputs, bar, new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret) {
							job.setSpecs(options.getSpecs());
							PipelineMonitor.sqlService.saveJob(job, new VoidAsyncCallback());
						}
					}
				});
			}
		});
		setupClickAction();
		view.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ResultViewer tab = new ResultViewer(job.getId());
				tab.loadJob(job.getId());
				ETA.getInstance().addTab(tab);
			}
		});
		view.setStyleName("action");
		edit.setStyleName("action");
		view.addStyleName("view");
	}

	public int getTop() {
		return getAbsoluteTop() - getParent().getAbsoluteTop();
	}

	public int getLeft() {
		return getAbsoluteLeft() - getParent().getAbsoluteLeft();
	}

	public Job getJob() {
		return job;
	}

	protected void setupClickAction() {
		addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				PopupPanel paneler = new PopupPanel();
				FlowPanel panel = new FlowPanel();
				panel.setStyleName("job-block-popup");
				if (job.getStatus().equals("Running")) {
					panel.add(new HTML("<div id='notch'></div>Job#" + job.getId() + "<br>Status:" + job.getStatus() + "<br>Since:" + job.getRunTime() + "<br>On:" + job.getMachine()));
				} else if (job.getStatus().equals("Finished") || job.getStatus().equals("Failed")) {
					panel.add(new HTML("<div id='notch'></div>Job#" + job.getId() + "<br>Status:" + job.getStatus() + "<br>Since:" + job.getRunTime() + "<br>On:" + job.getMachine()));
				} else
					panel.add(new HTML("<div id='notch'></div>Job#" + job.getId() + "<br>Status:" + job.getStatus()));
				panel.add(new SimpleButton("Force Run").addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ETA.sqlService.rerunJob(job.getId(), new AsyncCallback<Void>() {
							@Override
							public void onSuccess(Void result) {
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
					}
				}));

				paneler.add(panel);
				paneler.setAutoHideEnabled(true);
				paneler.setPopupPosition(getAbsoluteLeft() + getOffsetWidth() + 10, getAbsoluteTop() - 8);
				paneler.show();
			}
		}, ClickEvent.getType());
	}

	public void setJob(final Job job) {
		setStyleName("job-block");
		remove(edit);
		remove(view);
		if (handler != null) {
			handler.removeHandler();
			handler = null;
		}

		this.job = job;
		if (job.getStatus().equals("Finished") || job.getStatus().equals("Running")) {
			if (job.getStatus().equals("Finished")) {
				setStyleName("job-block");
				addStyleName("finished");
			} else {
				setStyleName("job-block");
				addStyleName("running");
			}
			insert(view, 0);
		} else {
			if (job.getStatus().equals("Failed") || job.getStatus().equals("Cancelled")) {
				setStyleName("job-block");
				addStyleName("failed");
				insert(view, 0);
			}
			insert(edit, 0);
		}
	}

	public void setListener(BlockDisplayListener listener) {
		this.listener = listener;
	}
}
