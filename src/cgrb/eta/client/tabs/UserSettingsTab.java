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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.CheckButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.PendingCluster;
import cgrb.eta.shared.etatype.User;

public class UserSettingsTab extends ETATab {

	private TextBox email;
	private TextBox phone;
	private CheckButton nEmail;
	private CheckButton nPhone;
	private TextBox defaultQueue;
	private CheckButton defaultNotify;
	private CommunicationServiceAsync communicationService;
	private Table<PendingCluster> accessClusters; 

	public UserSettingsTab() {
		super("settings");
		User user = ETA.getInstance().getUser();
		communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
		HorizontalPanel pane = new HorizontalPanel();
		VerticalPanel left = new VerticalPanel();
		pane.add(left);
		left.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		left.add(new SimpleLabel(user.getName()).setFontSize(30));
		left.setWidth("100%");
		pane.setCellWidth(left, "800px");
		Grid settings = new Grid(4, 2);
		settings.setStyleName("settings-grid");
		settings.setText(0, 0, "Info:");
		settings.setText(1, 0, "Notifications:");
		settings.setText(2, 0, "Defaults:");
		settings.setText(3, 0, "External Clusters:");

//		settings.setText(3, 0, "Instances:");
		settings.setWidth("600px");
		settings.setCellSpacing(0);
		email = new TextBox();
		phone = new TextBox();
		nEmail = new CheckButton("");
		nPhone = new CheckButton("");
		defaultNotify=new CheckButton("");
		defaultQueue= new TextBox();
		nEmail.setValue(user.isnEmail());
		nPhone.setValue(user.isnPhone());
		email.setValue(user.getEmail());
		phone.setValue(user.getPhone());
		email.setStyleName("eta-input2");
		phone.setStyleName("eta-input2");
		defaultQueue.setStyleName("eta-input2");
		defaultQueue.setValue(user.getSetting("deafultQueue"));
		defaultNotify.setValue(Boolean.parseBoolean(user.getSetting("defaultNotify")));
		Grid info = new Grid(2,2);
		info.setStyleName("request-grid");
		info.setText(0, 0, "Email:");
		info.setText(1, 0, "Phone:");
		info.setWidget(0, 1, email);
		info.setWidget(1, 1, phone);
		
		Grid notifications = new Grid(2,2);
		notifications.setStyleName("request-grid");
		notifications.setText(0, 0, "By email:");
		notifications.setText(1, 0, "By phone:");
		notifications.setWidget(0, 1, nEmail);
		notifications.setWidget(1, 1, nPhone);
		
		Grid defaults = new Grid(2,2);
		defaults.setStyleName("request-grid");
		defaults.setText(0, 0, "Queue:");
		defaults.setText(1, 0, "Notify:");
		defaults.setWidget(0, 1, defaultQueue);
		defaults.setWidget(1, 1, defaultNotify);

		VerticalPanel externalCluster= new VerticalPanel();
		SimpleButton addNew = new SimpleButton("Add connection").addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				SC.ask("What is the addess of the Cluster you want access to?", new ValueListener<String>() {
					@Override
					public void returned(String ret) {
						if(ret!=null){
							communicationService.requestAccess(ret, new MyAsyncCallback<String>(){
								@Override
								public void success(String result) {
									if(result!=null&&!result.equals(""))
									SC.alert("Error :(", "Sorry there was a problem with your request. I got the error: "+result);
								}
							});
						}
					}
				});
			}
		});
		externalCluster.add(addNew);
		
		Column<PendingCluster> org = new Column<PendingCluster>("Organization") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getServer();
			}
			@Override
			public String getWidth() {
				return "150px";
			}
		};
		Column<PendingCluster> admin = new Column<PendingCluster>("Administrator") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getUsername();
			}
			@Override
			public String getWidth() {
				return "120px";
			}
		};
		Column<PendingCluster> status = new Column<PendingCluster>("Status") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getStatus();
			}
			@Override
			public String getWidth() {
				return "100px";
			}
		};
		
		accessClusters = new Table<PendingCluster>(false, org,admin,status);
		communicationService.getUserPendingClusters(new MyAsyncCallback<Vector<PendingCluster>>() {
			public void success(Vector<PendingCluster> result) {
				accessClusters.setData(result);
			};
		});
		accessClusters.addListener(PendingCluster.class);
		accessClusters.getScrollPane().setHeight("200px");
		externalCluster.add(accessClusters);
		
		settings.setWidget(0, 1, info);
		settings.setWidget(1, 1, notifications);
		settings.setWidget(2, 1, defaults);
		settings.setWidget(3, 1, externalCluster);
		left.add(settings);
		setPane(pane);
	}
	
	public void save(){
		User user = ETA.getInstance().getUser();
		user.setEmail(email.getValue());
		user.setPhone(phone.getValue());
		user.setnEmail(nEmail.getValue());
		user.setnPhone(nPhone.getValue());
		user.setSetting("deafultQueue", defaultQueue.getValue());
		user.setSetting("defaultNotify", ""+defaultNotify.getValue());
		communicationService.saveUser(user,new MyAsyncCallback<Void>() {
			@Override
			public void success(Void result) {
				
			}
		});
	}

	@Override
	public Widget getBar() {
		HorizontalPanel temp = new HorizontalPanel();
		temp.add(new Button("Save").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				save();
			}
		}));
		temp.add(new Seprator());
		temp.add(new Button("Change Password").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				changePassword();
			}
		}));
		temp.add(new Seprator());
		return temp;
	}
	
	private void changePassword() {
		final PasswordTextBox oldPass = new PasswordTextBox();
		final PasswordTextBox newPass = new PasswordTextBox();
		final PasswordTextBox newPass2 = new PasswordTextBox();
		oldPass.setStyleName("eta-input2");
		newPass.setStyleName("eta-input2");
		newPass2.setStyleName("eta-input2");
		
		Grid temp = new Grid(3,2);
		temp.setStyleName("request-grid");
		temp.setText(0,0,"Old password:");
		temp.setText(1,0,"New password:");
		temp.setText(2,0,"Confirm password:");
		temp.setWidget(0, 1, oldPass);
		temp.setWidget(1, 1, newPass);
		temp.setWidget(2, 1, newPass2);
		
		
		SC.ask("Change Password", temp,new ValueListener<Boolean>() {
			public void returned(Boolean ret) {
				if(newPass.getValue().equals(newPass2.getValue())){
					communicationService.changePassword(oldPass.getValue(), newPass.getValue(), new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
							HTML res = new HTML(result);
							res.addStyleName("simple-label");
							SC.show("Change Password", res);
						}
					});
				}else{
					SC.alert("Error", "Sorry the new passwords do not match");
				}
			}
		});
		
	}
	
	@Override
	public String getId() {
		return "us";
	}

}
