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
package cgrb.eta.client.button;

import cgrb.eta.client.EventListener;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.UploadEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class FileButton extends HorizontalPanel implements ClickHandler{
	private ClickHandler handler;
	private String file;
	private Label fileName;
	public FileButton(String file) {
		String[] filepath = file.split("/");
		this.file=filepath[filepath.length - 1];
		fileName = new Label(this.file);
		fileName.setStyleName("file-name");
		fileName.setWordWrap(false);
		Label close = new Label("X");
		close.setStyleName("file-close");
		setHeight("30px");
		add(fileName);
		add(new Filler(5));
		add(close);
		close.addDomHandler(this, ClickEvent.getType());
	}
	public void setClickHandler(ClickHandler hand){
		handler=hand;
	}

	public void onClick(ClickEvent event) {
		if(handler!=null)
			handler.onClick(event);
	}
	/**
	 * @param fileId
	 */
	public void setFileId(String fileId) {
		EventListener.getInstance().addListener(ETAEvent.UPLOAD, new EventOccuredListener() {
			public void eventOccured(ETAEvent event, int user) {
				int percent =(int) ((UploadEvent)event.getSource()).getPercent();
				fileName.setText(file+" "+percent+"%");
			}
		});
			
	}

}
