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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

import cgrb.eta.client.images.Resources;
import cgrb.eta.shared.etatype.Job;

public class JobPipeBlock extends JobBlock {

	private FlowPanel workspace = new FlowPanel();
	private ArrayList<JobBlock> blocks = new ArrayList<JobBlock>();

	private boolean isConcurrent = false;

	public JobPipeBlock(Job job) {
		this.job = job;
		isConcurrent = job.getName().equals("Foreach");
		FlowPanel title = new FlowPanel();
		title.setStyleName("job-block");
		HTML jobName = new HTML(job.getName());
		jobName.setStyleName("name");
		title.add(jobName);
		add(title);
		add(workspace);
		workspace.setStyleName("sub-workspace");
		HTML wider = new HTML();
		wider.setWidth("400px");
		wider.setHeight("1px");
		workspace.add(wider);
		if (isConcurrent) {
			Image left = new Image(Resources.INSTANCE.backArrow().getSafeUri());
			workspace.add(left);
			left.addStyleName("concurent");
			Image right = new Image(Resources.INSTANCE.backArrow().getSafeUri());
			right.setStyleName("mirror");
			right.addStyleName("concurent");
			workspace.add(right);
			right.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (on >= blocks.size() - 1)
						return;
					workspace.remove(2);
					listener.blockHidden(blocks.get(on).getJob().getId());
					workspace.insert(blocks.get(++on), 2);
					listener.blockShown(blocks.get(on).getJob().getId());
				}
			});
			left.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (on >= 1) {
						workspace.remove(2);
						listener.blockHidden(blocks.get(on).getJob().getId());
						workspace.insert(blocks.get(--on), 2);
						listener.blockShown(blocks.get(on).getJob().getId());
					}
				}
			});
		}

	}
	
	int on = 0;

	public void addBlock(JobBlock block) {
		if (isConcurrent) {
			block.addStyleName("concurent");
			if (blocks.isEmpty()){
				workspace.insert(block, 2);
				if(listener!=null)
				listener.blockShown(block.getJob().getId());
			}
			blocks.add(block);
		} else {
			workspace.add(new Arrow());
			workspace.add(block);
			blocks.add(block);
		}
	}
	
	public ArrayList<JobBlock> getBlocks(){
		return blocks;
	}

}
