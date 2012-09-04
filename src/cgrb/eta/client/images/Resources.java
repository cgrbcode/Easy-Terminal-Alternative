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
package cgrb.eta.client.images;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;

public interface Resources extends ClientBundle {
	public static final Resources INSTANCE = GWT.create(Resources.class);

	@Source("blank.png")
	DataResource blank();

	@Source("submit_job_small.png")
	DataResource submitJobSmall();

	@Source("monitor_small.png")
	DataResource monitorSmall();

	@Source("results_small.png")
	DataResource resultsSmall();

	@Source("share_small.png")
	DataResource shareSmall();

	@Source("file_public.png")
	DataResource publicFile();

	@Source("file.png")
	DataResource file();

	@Source("newFolder.gif")
	DataResource newFolder();

	@Source("gear_public.png")
	DataResource gearPublic();

	@Source("pipeline_public.png")
	DataResource publicPipeline();

	@Source("folder.png")
	DataResource folder();

	@Source("gear.png")
	DataResource gear();

	@Source("pipeline.png")
	DataResource pipeline();

	@Source("ActivityIndicator.gif")
	DataResource working();

	@Source("add.png")
	DataResource add();

	@Source("remove.png")
	DataResource remove();

	@Source("edit.png")
	DataResource edit();

	@Source("redo.png")
	DataResource redo();

	@Source("public.png")
	DataResource share();

	@Source("search_white.png")
	DataResource searchWhite();

	@Source("close_button_Over.png")
	DataResource closeOver();

	@Source("search.png")
	DataResource search();

	@Source("back.png")
	DataResource back();

	@Source("more.png")
	DataResource more();

	@Source("star-on.png")
	DataResource starOn();

	@Source("star-off.png")
	DataResource starOff();

	@Source("download.png")
	DataResource download();

	@Source("refresh.png")
	DataResource refresh();

	@Source("home.png")
	DataResource home();

	@Source("submit_job.png")
	DataResource submit();

	@Source("monitor.png")
	DataResource monitor();

	@Source("share.png")
	DataResource shareLarge();

	@Source("results.png")
	DataResource results();
	
	@Source("zip.png")
	DataResource zip();
	
	@Source("job_big.png")
	DataResource jobBig();

	@Source("close_button.png")
	DataResource closeButton();

	@Source("close_button_Down.png")
	DataResource closeButtonDown();

	@Source("close_button_Over.png")
	DataResource closeButtonOver();

	@Source("back.png")
	DataResource backArrow();

	@Source("popin.png")
	DataResource popIn();

	@Source("down.png")
	DataResource down();

	@Source("popup.png")
	DataResource popOut();
}
