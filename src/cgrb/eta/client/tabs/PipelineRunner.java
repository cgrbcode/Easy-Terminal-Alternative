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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETA;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.CheckButton;
import cgrb.eta.client.button.LabelButton;
import cgrb.eta.client.button.MenuButton;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.pipeline.PipelineShower;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.wrapper.Input;

public class PipelineRunner extends ETATab {

	private HorizontalPanel bar;
	private VerticalPanel pane;
	private Inputs inputs;
	private Pipeline pipeline;
	private MultipleUserSelect usersNot;
	private LabelButton workingFolder;
	private int pipelineId;
	private LabelButton jobName;
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private CheckButton notifyMe;
	private HTML show = new HTML("");

	private HorizontalPanel advancedPanel = new HorizontalPanel();

	public PipelineRunner(int pipeline) {
		super("Pipeline Runner");
		pipelineId = pipeline;
		FlowPanel master = new FlowPanel();
		master.setStyleName("pipeline-runner-panel");
		pane = new VerticalPanel();
		pane.setStyleName("pipeline-inputs");
		bar = new HorizontalPanel();
		advancedPanel.setStyleName("pipeline-view");

		master.add(advancedPanel);
		master.add(pane);
		setPane(master);
		if (pipeline == 0)
			return;
		wrapperService.getPipelineFromId(pipeline, new MyAsyncCallback<Pipeline>() {
			@Override
			public void success(Pipeline result) {
				setup(result);
			}
		});
	}

	public PipelineRunner(Job job) {
		super("Pipeline Runner");
		pipelineId = job.getPipeline();
		pane = new VerticalPanel();
		bar = new HorizontalPanel();
		wrapperService.getPipelineFromId(pipelineId, new MyAsyncCallback<Pipeline>() {
			@Override
			public void success(Pipeline result) {
				setup(result);
			}
		});
	}

	public void setup(Pipeline pipeline) {
		this.pipeline = pipeline;
		pipelineId = pipeline.getId();
		bar.clear();
		pane.clear();
		// make the bar
		usersNot = new MultipleUserSelect();
		MenuButton descButton = new MenuButton("Description");
		descButton.addButton(new LabelButton(pipeline.getDescription()));

		MenuButton workingDir = new MenuButton("Working Dir");
		workingFolder = new LabelButton(FileBrowser.lastFolder);
		workingDir.addButton(workingFolder);
		workingDir.addButton(new Button("Change").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new FileSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						if (items != null && items.length > 0) {
							workingFolder.setText(items[0]);
						}
					}
				}, FileBrowser.FILE_SELECT);
			}
		}));

		MenuButton notifyMenu = new MenuButton("Notifications");
		notifyMe = new CheckButton("Notify me");
		notifyMenu.addButton(notifyMe);

		notifyMenu.addButton(new Button("Notify Others").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Select the users you want to notify", usersNot, null);
			}
		}));

		MenuButton jobOptions = new MenuButton("Job Options");
		jobOptions.addButton(new SeperatorButton());
		jobOptions.addButton(new LabelButton("Job name"));
		jobName = new LabelButton(pipeline.getName());
		jobOptions.addButton(jobName);
		jobOptions.addButton(new Button("Change").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("What do you want to name this job?", new ValueListener<String>() {
					public void returned(String ret) {
						jobName.setText(ret);
					}
				});
			}
		}));
		jobOptions.addButton(new SeperatorButton());
		bar.add(descButton);
		bar.add(new Seprator());
		bar.add(workingDir);
		bar.add(new Seprator());
		bar.add(notifyMenu);
		bar.add(new Seprator());
		bar.add(jobOptions);
		bar.add(new Seprator());
		bar.add(new SimpleLabel("Pipeline created by: " + pipeline.getCreator()).setColor("white").setFontSize(10));

		// the panels
		HorizontalPanel top = new HorizontalPanel();
		top.setStyleName("wr-top");
		SimpleLabel nameHeader = new SimpleLabel("Name");
		SimpleLabel descHeader = new SimpleLabel("Description");
		SimpleLabel value = new SimpleLabel("Value");
		FlowPanel middle = new FlowPanel();
		top.add(nameHeader);
		top.add(descHeader);
		top.add(value);
		top.setWidth("100%");
		top.setCellWidth(nameHeader, "150px");
		top.setCellWidth(value, "250px");

		inputs = new Inputs(pipeline);

		middle.add(inputs);
		middle.setStyleName("wr-middle");
		middle.setHeight("100%");
		middle.getElement().getStyle().setOverflowY(Overflow.AUTO);
		top.setHeight("20px");
		pane.add(top);
		pane.setCellHeight(top, "20px");
		pane.add(middle);
		pane.setCellHeight(middle, "100%");
		HorizontalPanel bottom = new HorizontalPanel();
		SimpleButton submit = new SimpleButton("Submit").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		bottom.add(submit);
		bottom.setWidth("100%");
		bottom.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_RIGHT);
		pane.add(bottom);
		bottom.setCellWidth(submit, "90px");
		bottom.setStyleName("wr-bottom");
		pane.setCellHeight(bottom, "30px");
		pane.setHeight("100%");

		advancedPanel.add(new PipelineShower(pipeline));
		show.setStyleName("show-pipeline");
		advancedPanel.add(show);
		advancedPanel.setCellVerticalAlignment(show, HasVerticalAlignment.ALIGN_MIDDLE);
		advancedPanel.setCellHeight(show, "100%");
		advancedPanel.setCellWidth(show, "20px");
		show.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				showPipelineView();
			}
		});
	}

	boolean advancedShown = false;

	public void showPipelineView() {
		if (!advancedShown)
			new ShowAnimation().run(2000);
		else
			new HideAnimation().run(2000);
		advancedShown = !advancedShown;
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	@Override
	public String getId() {
		return "pr#" + pipelineId;
	}

	private class ShowAnimation extends Animation {
		public ShowAnimation(){
			show.addStyleName("rotate-cw");
			show.removeStyleName("rotate-ccw");
		}
		@Override
		protected void onUpdate(double progress) {
			advancedPanel.getElement().getStyle().setRight((1 - progress) * 95, Unit.PCT);
		}

		@Override
		public void cancel() {
			super.cancel();
			advancedPanel.getElement().getStyle().setRight(95, Unit.PCT);
		}
	}

	private class HideAnimation extends Animation {
		public HideAnimation(){
			show.addStyleName("rotate-ccw");
			show.removeStyleName("rotate-cw");
		}
		@Override
		protected void onUpdate(double progress) {
			advancedPanel.getElement().getStyle().setRight((progress) * 95, Unit.PCT);
		}

		@Override
		public void cancel() {
			super.cancel();
			advancedPanel.getElement().getStyle().setRight(0, Unit.PCT);
		}
	}

	private void submit() {
		for (Input input : pipeline.getInputs()) {
			if (input.isRequired() && (input.getValue() == null || input.getValue().equals(""))) {
				SC.alert("Can't submit", "Sorry all required inputs must have a value to submit this job.");
				return;
			}
		}

		Job job = new Job();
		job.setPipeline(pipeline);
		job.setName(jobName.getText());
		job.setWorkingDir(workingFolder.getText());
		job.setUserId(ETA.getInstance().getUser().getId());
		wrapperService.runJob(job, new MyAsyncCallback<Integer>() {
			@Override
			public void success(Integer result) {
				if (result > 0) {
					closeMe();
					ETA.getInstance().addTab("rv#"+result);
					setupNotifications(result);
				} else {
					SC.alert("Error", "Sorry for some reason this job can't be submitted.");
				}
			}
		});
		
	}

	private void setupNotifications(int job) {
		if (notifyMe.getValue()) {
			wrapperService.addNotification(ETA.getInstance().getUser().getId(), job, new MyAsyncCallback<Void>() {
				@Override
				public void success(Void result) {
				}
			});
		}
		Vector<User> users = usersNot.getUsers();
		for (User user : users) {
			wrapperService.addNotification(user.getId(), job, new MyAsyncCallback<Void>() {
				@Override
				public void success(Void result) {
				}
			});
		}
	}

	private void closeMe() {
		ETA.getInstance().removeTab(this);
	}
}
