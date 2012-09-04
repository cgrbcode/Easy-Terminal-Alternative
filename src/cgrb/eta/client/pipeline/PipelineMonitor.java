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

import java.util.ArrayList;
import java.util.HashMap;

import cgrb.eta.client.ETA;
import cgrb.eta.client.ETATypeEventOccurred;
import cgrb.eta.client.EventListener;
import cgrb.eta.client.LoadingSpinner;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.Theme;
import cgrb.eta.shared.etatype.Job;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

public class PipelineMonitor extends FlowPanel {
	public static final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private HashMap<Integer, JobBlock> blocks = new HashMap<Integer, JobBlock>();
	private CurrentBlock current;
	private int lastJob = 0;
	private Job parent;
	private ETATypeEventOccurred<Job> jobListener = new ETATypeEventOccurred<Job>() {
		@Override
		public void onUpdate(Job record) {
			if (blocks.containsKey(record.getId())) {
				JobBlock block = blocks.get(record.getId());
				block.setJob(record);
				if (record.getId() >= lastJob) {
					current.changeJob(record);
					current.smoothMoveTo(block);
					lastJob = record.getId();
				}
			}
		}

		@Override
		public void onAddition(Job record) {

		}

		@Override
		public void onRemoval(Job record) {
			if (blocks.containsKey(record.getId())) {
				JobBlock block = blocks.get(record.getId());
				block.setJob(record);
				if (!blocks.containsKey(record.getId() + 1)) {
					remove(current);
				}
			}
		}
	};

	public PipelineMonitor(Job job) {
		parent = job;
		setStyleName("pipeline-monitor");
		addStyleName("pipe-workspace");
		current = new CurrentBlock(ETA.getInstance().getUser().getId() == job.getUserId());
		add(new LoadingSpinner(Theme.DARK, "Please wait while this pipeline is retrieved"));
		sqlService.getChildJobs(job.getId(), new MyAsyncCallback<ArrayList<Job>>() {
			@Override
			public void success(ArrayList<Job> result) {
				clear();
				HTML center = new HTML();
				center.setHeight("30px");
				center.setWidth("100px");
				center.setStyleName("start-block");
				center.setHTML("<div style='padding-top:5px;background:none;'>Start<div>");
				add(center);
				add(current);
				int lastWaitingFor = 0;
				for (Job job : result) {
					
					JobBlock temp ;
					if(job.getPipeline()>0)
						temp=new JobPipeBlock(job);
					else
						temp=new JobBlock(job);
					blocks.put(job.getId(), temp);
					if (job.getParent() == parent.getId()) {
						add(new Arrow());
						add(temp);
						if (job.getStatus().startsWith("Waiting for") && (job.getWaitingFor() < lastWaitingFor || lastWaitingFor == 0)) {
							lastWaitingFor = job.getWaitingFor();
						} else if (job.getStatus().equals("Failed") || job.getStatus().equals("Cancelled")) {
							lastWaitingFor = job.getId();
						}
					}else{
						JobBlock bl = blocks.get(job.getParent());
						if(bl!=null){
							((JobPipeBlock)bl).addBlock(temp);
						}
					}
				}
				if (blocks.containsKey(lastWaitingFor)) {
					JobBlock block = blocks.get(lastWaitingFor);
					if (block.getJob().getStatus().equals("Finished")) {
						remove(current);
						return;
					}
					current.changeJob(block.getJob());
					current.moveTo(blocks.get(lastWaitingFor));
				} else {
					remove(current);
				}
				EventListener.getInstance().addETATypeListener(Job.class.getName(), jobListener);
			}
		});
	}

}
