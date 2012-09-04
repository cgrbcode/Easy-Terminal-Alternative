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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.button.NumberBox;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.window.Window;

public class JobOptions {
	private Window window;
	private NumberBox memFree = new NumberBox();
	private ListBox memType = new ListBox();
	private CheckBox export = new CheckBox("Export env variables");
	private ListBox threadEnv = new ListBox();
	private NumberBox cores = new NumberBox();
	private NumberBox priorty = new NumberBox();
	private ListBox queue = new ListBox();
	private static String[] queues;
	private static String[] threads;
	private CommunicationServiceAsync communicationService;

	public JobOptions() {
		communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);

		Grid pane = new Grid(6, 3);
		pane.setStyleName("job-options");
		pane.setWidget(0, 0, new SimpleLabel("Memory Free:"));
		pane.setWidget(2, 0, new SimpleLabel("Threads:"));
		pane.setWidget(3, 0, new SimpleLabel("Priority:"));
		pane.setWidget(4, 0, new SimpleLabel("Queue:"));

		memType.setStyleName("eta-input");
		threadEnv.setStyleName("eta-input");
		queue.setStyleName("eta-input");
		export.setValue(true);

		memType.addItem("M");
		memType.addItem("G");
		memType.setWidth("50px");
		threadEnv.setWidth("100%");
		queue.setWidth("100%");
		cores.setWidth("40px");
		memFree.setWidth("92%");
		priorty.setWidth("92%");

		priorty.setValue(".505");

		pane.setWidget(0, 1, memFree);
		pane.setWidget(0, 2, memType);
		pane.setWidget(1, 1, export);
		pane.setWidget(2, 1, threadEnv);
		pane.setWidget(2, 2, cores);
		pane.setWidget(3, 1, priorty);
		pane.setWidget(4, 1, queue);

		SimpleButton ok = new SimpleButton("OK").addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				window.destroy();
			}
		});
		pane.setWidget(5, 2, ok);

		window = new Window("SGE Options", pane,true);
		System.out.println("as;dhfa;sdj");
		if (threads != null) {
			for (int i = 0; i < threads.length; i++) {
				threadEnv.addItem(threads[i]);
				if (threads[i].equals("thread"))
					threadEnv.setSelectedIndex(i);
			}
			queue.addItem("");
			for (String thread : queues) {
				queue.addItem(thread);
			}
		} else {
			
			
			communicationService.getThreadEnviroments( new MyAsyncCallback<String[]>() {
				@Override
				public void success(String[] result) {
					threads=result;
					for (int i = 0; i < JobOptions.threads.length; i++) {
						threadEnv.addItem(JobOptions.threads[i]);
						if (JobOptions.threads[i].equals("thread"))
							threadEnv.setSelectedIndex(i);
					}
				}
			});
			communicationService.getQueues( new MyAsyncCallback<String[]>() {
				@Override
				public void success(String[] result) {
					queue.addItem("");
					queues=result;
					for (String thread : result) {
						queue.addItem(thread);
					}
				}
			});
		}
	}

	public void show() {
		window.showWindow();
	}

	public String getSpecs() {
		String ret = "";
		String ramValue = memFree.getValue();
		boolean exportVal = export.getValue();
		String threadsVal = threadEnv.getItemText(threadEnv.getSelectedIndex());
		String priorty = this.priorty.getValue();
		String coresVal = cores.getValue();
		String qVal = queue.getItemText(queue.getSelectedIndex());
		if (!(ramValue == null || ramValue.equals(""))) {
			ret += " -l h_vmem=" + ramValue + memType.getItemText(memType.getSelectedIndex());
		}
		if (!(threadsVal == null || threadsVal.equals("") || coresVal == null || coresVal.equals(""))) {
			ret += " -pe " + threadsVal + " " + coresVal;
		}
		if (exportVal) {
			ret += " -V";
		}
		if (!(priorty == null || priorty.equals(""))) {
			ret += " -p " + priorty;
		}
		if (!(qVal == null || qVal.equals(""))) {
			ret += " -q " + qVal;
		}

		return ret;
	}
}
