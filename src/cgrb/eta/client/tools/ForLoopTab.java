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
package cgrb.eta.client.tools;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETA;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperSelector;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.CheckButton;
import cgrb.eta.client.button.LabelButton;
import cgrb.eta.client.button.LargeSeprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.client.wrapperrunner.JobOptions;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;
/**
 * The tab which will run a wrapper multiple times.
 * 
 * Displays the exact information that WrapperRunner displays, except it asks for a drop-in to load, and it also adds the for each field at the top of the form.
 * 
 * @author Alexander Boyd
 *
 */
public class ForLoopTab extends ETATab {

	private HorizontalPanel bar;
	private VerticalPanel pane;
	private JobOptions options;
	private Inputs inputs;
	private Wrapper wrapper;
	private MultipleUserSelect usersNot;
	private SimpleLabel workingFolder;
	private LabelButton jobName;
	private CheckButton saveStd;
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private CheckButton notifyMe;
	private FlowPanel jobOptions;

	public ForLoopTab() {
		super("For Loop");
		pane = new VerticalPanel();
		bar = new HorizontalPanel();
		pane.setStyleName("drop-here");
		jobOptions = new FlowPanel();
		this.setAnimatedPanel(jobOptions);

		pane.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		pane.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		SimpleLabel simpTemp = new SimpleLabel("Drag and drop a wrapper into here to get started.");

		FlowPanel temp = new FlowPanel();
		temp.add(simpTemp);
		temp.setWidth("100%");
		temp.add(new SimpleLabel("Or click 'Select' to select a wrapper"));
		temp.add(new SimpleButton("Select").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new WrapperSelector(new ItemSelector() {

					public void itemSelected(String[] items) {
						wrapperService.getWrapperFromId(Integer.parseInt(items[0]), new MyAsyncCallback<Wrapper>() {
							@Override
							public void success(Wrapper result) {
								setup(result);
							}
						});
						pane.getElement().getStyle().setBorderColor("#6288B3");
					}
				});
			}
		}));
		pane.add(temp);
		pane.setCellHeight(temp, "30px");

		pane.setHeight("100%");
		setPane(pane);

		DragCreator.addDrop(pane.getElement(), null, new DropListener() {
			public void drop(ETAType record) {
				wrapperService.getWrapperFromId(((UserWrapper) DragCreator.getDragSource()).getWrapperId(), new MyAsyncCallback<Wrapper>() {
					@Override
					public void success(Wrapper result) {
						setup(result);
					}
				});
				pane.getElement().getStyle().setBorderColor("#6288B3");

			}

			public void dragOver(ETAType record) {
				pane.getElement().getStyle().setBorderColor("#2B5E98");
			}

			public void dragLeave(ETAType record) {
				pane.getElement().getStyle().setBorderColor("#6288B3");
			}

			public void dragEnter(ETAType record) {
				if (DragCreator.getDragSource() instanceof UserWrapper) {
					pane.getElement().getStyle().setBorderColor("#2B5E98");
				}
			}
		});
	}

	public void setup(Wrapper wrapper) {
		this.wrapper = wrapper;
		bar.clear();
		pane.clear();
		options = new JobOptions();
		// make the bar
		usersNot = new MultipleUserSelect();
		Label descTitle = new Label("Description:");
		Label descText = new Label(wrapper.getDescription());
		descTitle.setStyleName("simple-label-desc");
		descText.setStyleName("simple-label-desc");
	

		// setup hidden options
		FlowPanel description = new FlowPanel();
		description.setStyleName("animated-options-floats");
		FlowPanel folder = new FlowPanel();
		folder.setStyleName("animated-options-floats");
		FlowPanel wrapOptions = new FlowPanel();
		wrapOptions.setStyleName("animated-options-floats");
		FlowPanel notifications = new FlowPanel();
		notifications.setStyleName("animated-options-floats");

		// description pane
		description.add(new SimpleLabel("Program: " + wrapper.getProgram()));
		Button change = new Button("Change Title").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("What do you want to name this job?", new ValueListener<String>() {
					public void returned(String ret) {
						jobName.setText(ret);
					}
				});
			}
		});
		change.setStyleDependentName("-animate-button", true);

		description.add(change);
		description.add(descTitle);
		description.add(descText);

		// wrapperOptions pane
		Button sge = new Button("SGE Options").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				options.show();
			}
		});
		sge.setStyleDependentName("-animate-button", true);
		wrapOptions.add(sge);
		saveStd = new CheckButton("Save STD");
		saveStd.setStyleDependentName("-animate-check", true);
		wrapOptions.add(saveStd);

		// Folder pane
		SimpleLabel workingDir = new SimpleLabel("Working Dir");
		folder.add(workingDir);
		workingFolder = new SimpleLabel((FileBrowser.lastFolder) != "" ? FileBrowser.lastFolder : "No working folder.");
		folder.add(workingFolder);
		Button changebutt = new Button("Change").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new FileSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						if (items != null && items.length > 0) {
							workingFolder.setText(items[0]);
						}
					}
				}, FileBrowser.FOLDER_SELECT);
			}
		});
		changebutt.setStyleDependentName("-animate-button", true);
		folder.add(changebutt);

		jobOptions.add(description);
		jobOptions.add(new LargeSeprator());
		jobOptions.add(notifications);
		jobOptions.add(wrapOptions);
		jobOptions.add(new LargeSeprator());
		jobOptions.add(folder);
		jobOptions.add(new LargeSeprator());
		jobOptions.add(notifications);
		jobOptions.add(new LargeSeprator());

		// notifypane
		SimpleLabel notifyMenu = new SimpleLabel("Notifications");
		notifyMe = new CheckButton("Notify me");
		notifyMe.setStyleDependentName("-animate-button", true);
		notifications.add(notifyMenu);
		notifications.add(notifyMe);

		Button noteothers = new Button("Notify Others").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Select the users you want to notify", usersNot, null);
			}
		});
		noteothers.setStyleDependentName("-animate-button", true);
		notifications.add(noteothers);

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

		inputs = new Inputs(wrapper, true);

		middle.add(inputs);
		middle.setStyleName("wr-middle");
		middle.setHeight("100%");
		middle.getElement().getStyle().setOverflowY(Overflow.AUTO);
		top.setHeight("20px");
		pane.add(top);
		pane.setCellHeight(top, "20px");
		pane.add(middle);
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
		// inputs.setHandler(this);
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	private void submit() {
		Vector<Input> inputs = wrapper.getInputs();
		Vector<Input> inputsOrg = new Vector<Input>();
		System.out.println(wrapper.getCMD());
		for (Input input : inputs) {
			inputsOrg.add(input.clone());
			if (input.isRequired() && (input.getValue() == null || input.getValue().equals(""))) {
				SC.alert("Can't submit", "Sorry all required inputs must have a value to submit this job.");
				return;
			}
		}

		int count = 0;
		String[] loopValues = this.inputs.getLoopInput().getValue().split(",");
		if (loopValues.length > 0) {
			for (String item : loopValues) {
				Vector<Input> inputs2 = new Vector<Input>();
				for (Input input2 : inputsOrg) {
					Input input = input2.clone();
					inputs2.add(input);
					System.out.println(input.getName()+" "+input.getValue());
					if (input.getValue() != null && !input.getValue().equals("")) {
						if (input.getType().startsWith("File")&&input.getValue().equals("$'Loop Item'"))
							input.setValue(input.getValue().replaceAll("\\$'Loop Item'", item));
						else{
							String[] temp = item.split("/");
							String smallItem=temp[temp.length-1];
							input.setValue(input.getValue().replaceAll("\\$'Loop Item'", smallItem));
						}
					}
				}
				Job job = new Job();
				wrapper.setInputs(inputs2);
				job.setWrapper(wrapper);
				job.setName(jobName.getText() + " " + count++);
				job.setWorkingDir(workingFolder.getTitle());
				job.setUserId(ETA.getInstance().getUser().getId());
				job.setWaitingFor(0);
				job.setSpecs(options.getSpecs());
				job.setSaveStd(saveStd.getValue());
				wrapperService.runJob(job, new MyAsyncCallback<Integer>() {
					@Override
					public void success(Integer result) {
						if (result > 0) {
							setupNotifications(result);
						} else {
							SC.alert("Error", "Sorry for some reason this job can't be submitted.");
						}
					}
				});
			}
			closeMe();
			ETA.getInstance().addTab("jv");
		} else {
			SC.alert("Error :(", "Sorry you must put in at least one item in the 'Loop Items' input");
		}
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

	@Override
	public String getId() {
		return "fl";
	}
}
