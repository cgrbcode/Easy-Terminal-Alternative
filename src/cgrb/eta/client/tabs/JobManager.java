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
package cgrb.eta.client.tabs;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.etatype.Job;

public class JobManager extends ETATab {
	private Table<Job> jobs;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);

	public JobManager() {
		super("Job Monitor");
		final Column<Job> icon = new Column<Job>("") {
			@Override
			public Object getValue(Job record) {
				Image icon = new Image();
				icon.setWidth("16px");
				icon.setHeight("16px");
				if (record.getPipeline() != 0) {
					icon.setUrl(Resources.INSTANCE.pipeline().getSafeUri().asString());
				} else
					icon.setUrl(Resources.INSTANCE.gear().getSafeUri().asString());
				return icon;
			}

			@Override
			public String getWidth() {
				return "30px";
			}
		};
		Column<Job> jobNumber = new Column<Job>("Job") {
			@Override
			public Object getValue(Job record) {
				HorizontalPanel panel = new HorizontalPanel();
				panel.add((Widget) icon.getValue(record));
				panel.add(new Label("" + record.getId()));
				return panel;
			}

			@Override
			public String getWidth() {
				return "150px";
			}
		};

		Column<Job> name = new Column<Job>("Name") {
			@Override
			public Object getValue(Job record) {
				return record.getName();
			}

			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<Job> program = new Column<Job>("Wrapper/Pipeline") {
			@Override
			public Object getValue(Job record) {
				if (record.getWrapper() != null)
					return record.getWrapper().getName();
				if (record.getPipelineObject() != null)
					return record.getPipelineObject().getName();
				return "";
			}

			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<Job> status = new Column<Job>("Status") {
			@Override
			public Object getValue(Job record) {
				return record.getStatus();
			}

			@Override
			public String getWidth() {
				return "160px";
			}
		};
		Column<Job> runningOn = new Column<Job>("Running on") {
			@Override
			public Object getValue(Job record) {
				return record.getMachine();
			}
		};

		jobs = new Table<Job>(true, jobNumber, name, program, status, runningOn);

		jobs.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Stop/Remove job").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (Job job : jobs.getSelection()) {
					if (job.getStatus().toLowerCase().equals("failed") || job.getStatus().equals("Stopped")) {
						sqlService.deleteJob(job.getId(), new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					} else {
						// assume that the job is still running and stop it
						sqlService.killJob(job.getId(), new MyAsyncCallback<Integer>() {
							@Override
							public void success(Integer result) {
							}
						});
					}
				}
			}
		}), Table.MULTIPLE_SELECT);
		jobs.addAction(new ImgButton(Resources.INSTANCE.redo(), 20, "Re-run job").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (Job job : jobs.getSelection()) {
					if (job.getStatus().toLowerCase().equals("failed") || job.getStatus().equals("Paused") || job.getStatus().equals("Stopped")) {
						sqlService.rerunJob(job.getId(), new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					} else {
						sqlService.getJob(job.getId(), new MyAsyncCallback<Job>() {
							@Override
							public void success(Job result) {
								WrapperRunner runner = new WrapperRunner(result);
								ETA.getInstance().addTab(runner);
							}
						});
					}
				}
			}
		}), Table.MULTIPLE_SELECT);
		jobs.addAction(new ImgButton(Resources.INSTANCE.resultsSmall(), 20, "View results").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sqlService.getJob(jobs.getSelection().get(0).getId(), new MyAsyncCallback<Job>() {
					@Override
					public void success(Job result) {
						ResultViewer viewer = new ResultViewer(result.getId());
						viewer.loadJob(result);
						ETA.getInstance().addTab(viewer);
					}
				});
			}
		}), Table.SINGLE_SELECT);
		setPane(jobs);
		jobs.setHeight("100%");
		jobs.addListener(Job.class);
		jobs.displayWaiting("Fetching jobs");
		sqlService.getJobs(new MyAsyncCallback<Vector<Job>>() {
			@Override
			public void success(Vector<Job> result) {
				jobs.setData(result);
			}
		});
	}

	@Override
	public String getId() {
		return "jv";
	}
	@Override
	public Widget getBar() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(new Button("Rerun all failed jobs").setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				sqlService.reRunFailedJobs(new MyAsyncCallback<Void>() {
					@Override
					public void success(Void result) {
					}
				});
			}
		}));
		return panel;
	}
}
