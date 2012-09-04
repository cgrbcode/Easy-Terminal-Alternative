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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import cgrb.eta.client.ETA;
import cgrb.eta.client.HomeIcon;
import cgrb.eta.client.HomeIconMenuItem;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.WrapperSelector;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.SC;

public class HomeTab extends ETATab {
	private FileBrowser browser;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private HorizontalPanel main;
	private VerticalPanel left;

	public HomeTab() {
		setTitle("Home");
		setIcon(Resources.INSTANCE.home());
		setCanClose(false);

		main = new HorizontalPanel();
		main.setHeight("100%");
		main.setWidth("100%");

		FlowPanel homeIcons = new FlowPanel();
		left = new VerticalPanel();
		homeIcons.setWidth("100%");
		left.add(homeIcons);
		main.add(left);
		main.setCellWidth(left, "630px");

		HomeIcon submit = new HomeIcon(Resources.INSTANCE.submit(), "Submit Job");
		HomeIcon monitor = new HomeIcon(Resources.INSTANCE.monitor(), "Monitor");
		HomeIcon results = new HomeIcon(Resources.INSTANCE.results(), "View Results");
		HomeIcon share = new HomeIcon(Resources.INSTANCE.shareLarge(), "Share Results");

		HomeIconMenuItem program = new HomeIconMenuItem("Wrapper");
		HomeIconMenuItem command = new HomeIconMenuItem("Command");
		HomeIconMenuItem cloud = new HomeIconMenuItem("The Cloud");
		HomeIconMenuItem jobs = new HomeIconMenuItem("Running Jobs");
		HomeIconMenuItem finishedJobs = new HomeIconMenuItem("Finished Jobs");
		HomeIconMenuItem selectFile = new HomeIconMenuItem("Select File");
		HomeIconMenuItem selectJob = new HomeIconMenuItem("Select Result");
		HomeIconMenuItem viewShares = new HomeIconMenuItem("View Shares");

		program.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new WrapperSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						ETA.getInstance().addTab(new WrapperRunner(Integer.parseInt(items[0])));
					}
				});
			}
		});
		cloud.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Window.open("http://doolittle.cgrb.oregonstate.edu/ganglia/", "_blank", "");
			}
		});
		command.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.runCommand();
			}
		});
		jobs.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab("jv");
			}
		});
		finishedJobs.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab("rts");
			}
		});
		viewShares.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab("shs");
			}
		});
	
		
		submit.setMenu(program, command);
		monitor.setMenu(cloud, jobs);
		results.setMenu(finishedJobs);
		share.setMenu(viewShares, selectJob, selectFile);

		homeIcons.add(submit);
		homeIcons.add(monitor);
		homeIcons.add(results);
		homeIcons.add(share);
		browser = new FileBrowser(true,true,"main");
		main.add(browser);
		sqlService.getHelpHTML("#help", new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				HTML homeHtml = new HTML(result);
				homeHtml.setStyleName("home-help");
				left.add(homeHtml);
			}
		});
		
		setPane(main);
	}

	@Override
	public String getId() {
		return "home";
	}

	/**
	 * @param path
	 */
	public void navigateTo(String path) {
		browser.navigateTo(path);
	}

}
