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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.CheckButton;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.etatype.PendingCluster;

public class ETASettingsTab extends ETATab {

	private CommunicationServiceAsync communicationService;
	private TextBox schoolName;
	private TextBox host;
	private TextBox smtpServer;
	private TextBox smtpPort;
	private TextBox smtpEmail;
	private PasswordTextBox smtpPassword;
	private TextBox gvEmail;
	private PasswordTextBox gvPassword;
	private TextBox supportList;
	private TextBox gangServer;
	private TextBox gangPort;
	private CheckButton enablePublicJobs;
	private TextBox publicResultsFolder;
	private TextBox publicUser;
	private PasswordTextBox publicPassword;
	private CheckButton enableClusterUse;
	private Table<PendingCluster> pendingClusterRequests;

	public ETASettingsTab() {
		super("ETA Settings");
		communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
		HorizontalPanel pane = new HorizontalPanel();
		VerticalPanel left = new VerticalPanel();
		pane.add(left);
		left.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		left.add(new SimpleLabel("ETA Settings").setFontSize(30));
		left.setWidth("100%");
		pane.setCellWidth(left, "800px");
		Grid settings = new Grid(5, 2);
		settings.setStyleName("settings-grid");
		settings.setText(0, 0, "Info:");
		settings.setText(1, 0, "Notifications:");
		settings.setText(2, 0, "Gangila:");
		settings.setText(3, 0, "Public jobs:");
		settings.setText(4, 0, "External Cluster use:");
		// info section
		settings.setWidth("600px");
		settings.setCellSpacing(0);
		schoolName = new TextBox();
		host = new TextBox();

		Grid info = new Grid(2, 2);
		info.setStyleName("request-grid");
		info.setText(0, 0, "Organization:");
		info.setText(1, 0, "Web Address:");
		info.setWidget(0, 1, schoolName);
		info.setWidget(1, 1, host);
		// notification section

		smtpServer = new TextBox();
		smtpPort = new TextBox();
		smtpEmail = new TextBox();
		smtpPassword = new PasswordTextBox();
		gvEmail = new TextBox();
		gvPassword = new PasswordTextBox();
		supportList = new TextBox();
		
		Grid notifications = new Grid(7, 2);
		notifications.setStyleName("request-grid");
		notifications.setText(0, 0, "SMTP server:");
		notifications.setText(1, 0, "SMTP port:");
		notifications.setText(2, 0, "Email:");
		notifications.setText(3, 0, "Email password:");
		notifications.setText(4, 0, "Google Voice Email:");
		notifications.setText(5, 0, "Google Voice Password:");
		notifications.setText(6, 0, "Support List:");

		notifications.setWidget(0, 1, smtpServer );
		notifications.setWidget(1, 1, smtpPort);
		notifications.setWidget(2, 1, smtpEmail);
		notifications.setWidget(3, 1, smtpPassword);
		notifications.setWidget(4, 1, gvEmail);
		notifications.setWidget(5, 1, gvPassword);
		notifications.setWidget(6, 1, supportList);

		//gangila settings
		gangServer = new TextBox();
		gangPort = new TextBox();
		
		Grid defaults = new Grid(2, 2);
		defaults.setStyleName("request-grid");
		defaults.setText(0, 0, "Server:");
		defaults.setText(1, 0, "Port:");
		defaults.setWidget(0, 1, gangServer);
		defaults.setWidget(1, 1, gangPort);

		//public job account settings
		enablePublicJobs = new CheckButton("");
		enableClusterUse=new CheckButton("enable");
		publicResultsFolder = new TextBox();
		publicUser = new TextBox();
		publicPassword = new PasswordTextBox();
		
		Grid publicAccount = new Grid(4,2);
		publicAccount.setStyleName("request-grid");
		publicAccount.setText(0, 0, "Enable public jobs:");
		publicAccount.setText(1, 0, "Results folder:");
		publicAccount.setText(2, 0, "Account:");
		publicAccount.setText(3, 0, "Password:");
		publicAccount.setWidget(0, 1, enablePublicJobs);
		publicAccount.setWidget(1, 1, publicResultsFolder);
		publicAccount.setWidget(2, 1, publicUser);
		publicAccount.setWidget(3, 1, publicPassword);
		
		settings.setWidget(0, 1, info);
		settings.setWidget(1, 1, notifications);
		settings.setWidget(2, 1, defaults);
		settings.setWidget(3, 1, publicAccount);
		
		communicationService.getETASettings(new MyAsyncCallback<String[]>() {
			@Override
			public void success(String[] result) {
				smtpServer.setText(result[0]);
				smtpPort.setText(result[1]);
				smtpEmail.setText(result[2]);
				smtpPassword.setText(result[3]);
				gvEmail.setText(result[4]);
				gvPassword.setText(result[5]);
				gangServer.setText(result[6]);
				gangPort.setText(result[7]);
				host.setText(result[8]);
				schoolName.setText(result[9]);
				supportList.setText(result[10]);
				enablePublicJobs.setValue(Boolean.parseBoolean(result[11]));
				publicResultsFolder.setText(result[12]);
				publicUser.setText(result[13]);
				publicPassword.setText(result[14]);
			}
		});

		Column<PendingCluster> organization = new Column<PendingCluster>("Organization") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getOrganization();
			}
			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<PendingCluster> requestorEmail = new Column<PendingCluster>("Requestor") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getUsername();
			}
			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<PendingCluster> userName = new Column<PendingCluster>("Email") {
			@Override
			public Object getValue(PendingCluster record) {
				return record.getEmail();
			}
			@Override
			public String getWidth() {
				return "150px";
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
		pendingClusterRequests = new Table<PendingCluster>(false,organization,requestorEmail,userName,status);
		pendingClusterRequests.addListener(PendingCluster.class);
		pendingClusterRequests.addAction(new ImgButton(Resources.INSTANCE.add(), 20, "Accept Requests").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (PendingCluster file : pendingClusterRequests.getSelection()) {
					communicationService.acceptClusterRequest(file.getId(), new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);
		
		communicationService.getPendingClusters(new MyAsyncCallback<Vector<PendingCluster>>() {
			@Override
			public void success(Vector<PendingCluster> result) {
				pendingClusterRequests.setData(result);
			}
		});
		
		pendingClusterRequests.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Reject Requests").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (PendingCluster file : pendingClusterRequests.getSelection()) {
					communicationService.rejectClusterRequest(file.getId(), new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);
		pendingClusterRequests.addAction(new Button("Auto Accept").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (PendingCluster file : pendingClusterRequests.getSelection()) {
					communicationService.autoAcceptClusterRequest(file.getId(), new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);
		pendingClusterRequests.getScrollPane().setHeight("200px");
		VerticalPanel clusterPanel = new VerticalPanel();
		clusterPanel.add(enableClusterUse);
		clusterPanel.add(pendingClusterRequests);
		settings.setWidget(4, 1, clusterPanel);
		left.add(settings);
		setPane(pane);
	}

	public void save() {
		String[] settings = new String[15];
    settings[0]=smtpServer.getValue();
    settings[1]=smtpPort.getValue();
    settings[2]=smtpEmail.getValue();
    settings[3]=smtpPassword.getValue();
    settings[4]=gvEmail.getValue();
    settings[5]=gvPassword.getValue();
    settings[6]=gangServer.getValue();
    settings[7]=gangPort.getValue();
    settings[8]=host.getValue();
    settings[9]=schoolName.getValue();
    settings[10]=supportList.getValue();
    settings[11]=""+enablePublicJobs.getValue();
    settings[12]=publicResultsFolder.getValue();
    settings[13]=publicUser.getValue();
    settings[14]=publicPassword.getValue();
    
    communicationService.saveETASettings(settings, new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				
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
		return temp;
	}

	@Override
	public String getId() {
		return "es";
	}

}
