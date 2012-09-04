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
package cgrb.eta.client.wrapperrunner;

import java.util.Vector;

import cgrb.eta.client.ETA;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.CheckButton;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.LabelButton;
import cgrb.eta.client.button.MenuButton;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.window.Window;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CommandRunner {
	private HorizontalPanel bar;
	private JobOptions options;
//	private Wrapper wrapper;
	private MultipleUserSelect usersNot;
	private LabelButton workingFolder;
//	private int wrapperId;
	private LabelButton jobName;
	private CheckButton saveStd;
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private CheckButton notifyMe;
	private TextBox cmd = new TextBox();
	private Window window;

	
	public CommandRunner(){
		bar=new HorizontalPanel();
		usersNot=new MultipleUserSelect();
		options = new JobOptions();
		// make the bar
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
				SC.ask("Select the users you want to notify", usersNot,null);
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
		
		jobName = new LabelButton("Command");
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

		bar.add(new Seprator());
		bar.add(workingDir);
		bar.add(new Seprator());
		bar.add(notifyMenu);
		bar.add(new Seprator());
		bar.add(jobOptions);
		bar.add(new Seprator());

		// the panels
		HorizontalPanel top = new HorizontalPanel();
		top.setStyleName("wr-top");
		
		
		VerticalPanel panel = new VerticalPanel();

		HorizontalPanel input = new HorizontalPanel();
		cmd.setFocus(true);
		cmd.setStyleName("eta-input2");
		input.add(new SimpleLabel("Command:"));
		input.add(cmd);
		cmd.setWidth("500px");
		panel.add(input);

		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("Submit").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				submit();
			}
		}));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window= new Window("Please enter a command to run", panel,true);
		window.addBar(bar);
		window.showWindow();
		
	}
	private void submit() {
		if(cmd.getValue()==null||cmd.getValue().equals(""))
			return;
		
		wrapperService.getWrapperFromCMD(cmd.getValue().split(" "), new MyAsyncCallback<Wrapper>() {
			@Override
			public void success(Wrapper result) {
				if(jobName.getText().equals("Command")){
					jobName.setText(result.getProgram());
				}
				Job job = new Job();
				job.setWrapper(result);
				job.setName(jobName.getText());
				job.setWorkingDir(workingFolder.getText());
				job.setUserId(ETA.getInstance().getUser().getId());
				job.setSpecs(options.getSpecs());
				job.setSaveStd(saveStd.getValue());
				wrapperService.runJob(job, new MyAsyncCallback<Integer>() {
					@Override
					public void success(Integer result) {
						if (result > 0) {
							window.destroy();
							ETA.getInstance().addTab("jv");
							setupNotifications(result);
						} else {
							SC.alert("Error", "Sorry for some reason this job can't be submitted.");
						}
					}
				});
			}
		});
//	
	}

	private void setupNotifications(int job) {
		if(notifyMe.getValue()){
			wrapperService.addNotification(ETA.getInstance().getUser().getId(), job, new MyAsyncCallback<Void>() {
				@Override
				public void success(Void result) {
				}
			});
		}
		Vector<User> users = usersNot.getUsers();
		for(User user:users){
			wrapperService.addNotification(user.getId(), job, new MyAsyncCallback<Void>() {
				@Override
				public void success(Void result) {
				}
			});
		}
	}

}
