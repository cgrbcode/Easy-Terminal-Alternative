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

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
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
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.client.wrapperrunner.JobOptions;
import cgrb.eta.shared.etatype.Cluster;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

public class WrapperRunner extends ETATab implements ValueChangeHandler<Wrapper> {

	private HorizontalPanel bar;
	private VerticalPanel pane;
	private JobOptions options;
	private Inputs inputs;
	private Wrapper wrapper;
	private MultipleUserSelect usersNot;
	private LabelButton workingFolder;
	private int wrapperId;
	private LabelButton jobName;
	private CheckButton saveStd;
	private SimpleLabel commandRun;
	ListBox clustersBox;
	private int waitingFor=0;

	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private CheckButton notifyMe;

	public WrapperRunner(int wrapper) {
		super("Wrapper Runner");
		wrapperId = wrapper;
		pane = new VerticalPanel();
		bar = new HorizontalPanel();
		setPane(pane);
		if (wrapper == 0)
			return;
		wrapperService.getWrapperFromId(wrapper, new MyAsyncCallback<Wrapper>() {
			@Override
			public void success(Wrapper result) {
				setup(result);
			}
		});
	}

	public WrapperRunner(Wrapper wrapper) {
		super("Wrapper Runner");
		wrapperId = wrapper.getId();
		pane = new VerticalPanel();
		bar = new HorizontalPanel();
		setPane(pane);
		setup(wrapper);
	}

	public WrapperRunner(Job job) {
		super("Wrapper Runner");
		wrapperId = job.getWrapper().getId();
		pane = new VerticalPanel();
		bar = new HorizontalPanel();
		setPane(pane);
		setup(job.getWrapper());
		workingFolder.setText(job.getWorkingDir());
	}

	public void setup(Wrapper wrapper) {
		this.wrapper = wrapper;
		bar.clear();
		pane.clear();
		options = new JobOptions();
		// make the bar
		MenuButton programButton = new MenuButton("Program");
		programButton.addButton(new LabelButton(wrapper.getProgram()));
		usersNot = new MultipleUserSelect();
		MenuButton descButton = new MenuButton("Description");
		descButton.addButton(new LabelButton(wrapper.getDescription()));

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
				}, FileBrowser.FOLDER_SELECT);
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
		jobOptions.addButton(new Button("SGE Options").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				options.show();
			}
		}));
		jobOptions.addButton(new SeperatorButton());
		jobOptions.addButton(new LabelButton("Job name"));
		jobName = new LabelButton(wrapper.getName());
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
		saveStd = new CheckButton("Save STD");
		jobOptions.addButton(saveStd);

		bar.add(programButton);
		bar.add(new Seprator());
		bar.add(descButton);
		bar.add(new Seprator());
		bar.add(workingDir);
		bar.add(new Seprator());
		bar.add(notifyMenu);
		bar.add(new Seprator());
		bar.add(jobOptions);
		bar.add(new Seprator());
		bar.add(new SimpleLabel("Wrapper created by: " + wrapper.getCreator()).setColor("white").setFontSize(10));

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

		inputs = new Inputs(wrapper);

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
		SimpleLabel temp = new SimpleLabel("Command being ran:");
		bottom.add(temp);
		commandRun = new SimpleLabel("");
		bottom.add(commandRun.setSelectable());
		SimpleButton submit = new SimpleButton("Submit").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		
		bottom.setWidth("100%");
		bottom.setCellHorizontalAlignment(submit, HasHorizontalAlignment.ALIGN_RIGHT);
		pane.add(bottom);

		Vector<Cluster> clusters = ETA.getInstance().getUser().getClusters();
		if (clusters.size() > 0) {
			clustersBox = new ListBox();
			clustersBox.addItem("","");
			for(Cluster cluster:clusters){
				clustersBox.addItem(cluster.getCompany(),""+cluster.getId());
			}
			clustersBox.setStyleName("eta-input");
			bottom.add(clustersBox);
			bottom.setCellHorizontalAlignment(clustersBox, HasHorizontalAlignment.ALIGN_RIGHT);
			bottom.setCellWidth(clustersBox, "120px");

		}
		bottom.add(submit);
		bottom.setCellWidth(submit, "90px");
		bottom.setCellWidth(temp, "120px");
		bottom.setStyleName("wr-bottom");
		pane.setCellHeight(bottom, "30px");
		pane.setHeight("100%");
		inputs.setHandler(this);
		onValueChange(null);
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	@Override
	public String getId() {
		return "wr#" + wrapperId;
	}

	private void submit() {
		Vector<Input> inputs = wrapper.getInputs();
		for (Input input : inputs) {
			if (input.isRequired() && (input.getValue() == null || input.getValue().equals(""))) {
				SC.alert("Can't submit", "Sorry all required inputs must have a value to submit this job.");
				return;
			}
		}
		Job job = new Job();
		job.setWrapper(wrapper);
		job.setName(jobName.getText());
		job.setWorkingDir(workingFolder.getText());
		job.setUserId(ETA.getInstance().getUser().getId());
		job.setSpecs("");
		job.setStatus("Submitted");
		job.setSaveStd(saveStd.getValue());
		if(clustersBox!=null){
			int index = clustersBox.getSelectedIndex();
			if(index>0){
				job.setGlobalCluster(Integer.parseInt(clustersBox.getValue(index)));
			}
		}
		job.setWaitingFor(waitingFor);
		wrapperService.runJob(job, new MyAsyncCallback<Integer>() {
			@Override
			public void success(Integer result) {
				if (result > 0) {
					closeMe();
					ETA.getInstance().addTab("jv");
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

	public void onValueChange(ValueChangeEvent<Wrapper> event) {
		commandRun.setText(wrapper.getCMD());
	}

	public void matchInputs(Job job) {
		Vector<File> outputFiles = job.getOutputFiles();
		Vector<Output> outputs = job.getWrapper().getOutputs();
		HashMap<String, File> map = new HashMap<String, File>();
		for (int i = 0; i < outputs.size(); i++) {
			map.put(outputs.get(i).getType(), outputFiles.get(i+2));
		}

		for (Input in : wrapper.getInputs()) {
			if (in.getType().startsWith("File:")) {
				if (map.containsKey(in.getType().replaceAll("File:", ""))) {
					in.setDefaultValue(map.get(in.getType().replaceAll("File:", "")).getPath());
				}
			}
		}
		setup(wrapper);
		if(!job.getStatus().equals("Finished")){
			waitingFor=job.getId();
		}
	}

}
