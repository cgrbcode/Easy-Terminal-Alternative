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

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.window.FileBrowser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;

public class UploadItemButton extends Button {

	FileUpload upload = new FileUpload();
	private String file = "";
	private String id;
	final FormPanel uploadPanel = new FormPanel();
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	Hidden idThing = new Hidden("id");
	private ClickHandler handlerM;

	public UploadItemButton() {
		upload.getElement().setAttribute("size", "1");
		upload.getElement().getStyle().setFontSize(15, Unit.PX);
		upload.getElement().getStyle().setWidth(80, Unit.PX);
		upload.getElement().getStyle().setOpacity(0);
		FlowPanel temp = new FlowPanel();
		temp.getElement().getStyle().setPosition(Position.ABSOLUTE);
		temp.getElement().getStyle().setTop(0, Unit.PX);
		uploadPanel.setAction("/plugin");
		uploadPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		uploadPanel.setMethod(FormPanel.METHOD_POST);
		upload.setName("filedata");
		temp.add(upload);
		Hidden function = new Hidden("function", "upload");
		temp.add(function);
		temp.add(idThing);
		uploadPanel.add(temp);
		FlowPanel temp2 = new FlowPanel();
		temp2.getElement().getStyle().setPosition(Position.RELATIVE);
		temp2.add(new Label("Upload"));
		temp2.add(uploadPanel);
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		add(temp2);
		getElement().getStyle().setOverflow(Overflow.HIDDEN);
		upload.getElement().getStyle().setZIndex(99999);
		upload.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				if (upload.getFilename().equals(file))
					return;
				file = upload.getFilename();
				upload(FileBrowser.lastFolder);
			}
		});
		removeDown();
	}

	public String getFile() {
		return file;
	}

	public String getFileId() {
		return id;
	}
	
	@Override
	public Button setClickHandler(ClickHandler hand) {
		handlerM=hand;
		return this;
	}

	@Override
	public void onClick(ClickEvent event) {
		getParentMenu().forceVisible();
	}

	public void upload(String to) {
		String fileToBe = file;
		fileToBe = fileToBe.replaceFirst("C:\\\\fakepath\\\\", "");
		file=fileToBe;
		getParentMenu().forceHide();
		communicationService.startSession(com.google.gwt.user.client.Window.Location.getHostName(), to + "/" + fileToBe, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}

			public void onSuccess(String result) {
				handlerM.onClick(null);
				idThing.setValue(result);
				Cookies.setCookie("session-eta", result);
				id = result;
				uploadPanel.submit();
			}
		});
	}
}
