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
package cgrb.eta.client;

import cgrb.eta.shared.etatype.File;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextArea;

public class TextReader extends Composite {
	private static final CommunicationServiceAsync commService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private TextArea contents;
	private String text = "";
	private File file;
	private long onByte = 0;
	private static final long readBytes = 512 * 8;

	public TextReader(File file) {
		contents = new TextArea();
		this.file = file;
		initWidget(contents);
		readNext();
		contents.addMouseWheelHandler(new MouseWheelHandler() {
			public void onMouseWheel(MouseWheelEvent event) {
				// contents.get
				int scrollHeight = contents.getElement().getScrollHeight();
				int scrollTop = contents.getElement().getScrollTop();
				if (scrollHeight - scrollTop - contents.getElement().getClientHeight() < 100) {
					readNext();
				}
			}
		});
		contents.setStyleName("eta-input2");
		contents.getElement().getStyle().setWidth(100, Unit.PCT);
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				contents.setHeight(event.getHeight() - contents.getAbsoluteTop() - 25 + "px");
				contents.setWidth(event.getWidth() - contents.getAbsoluteLeft() - 25 + "px");

			}
		});
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		contents.setHeight(Window.getClientHeight() - contents.getAbsoluteTop() - 25 + "px");
		contents.setWidth(Window.getClientWidth() - contents.getAbsoluteLeft() - 25 + "px");
	}

	private boolean reading = false;

	public void readNext() {
		if (reading)
			return;
		reading = true;
		commService.getFileContents(file, onByte, readBytes, new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				if (result == null || result.equals("")) {
					if (text.equals(""))
						contents.setText("Sorry It looks like this file is blank.");
					reading = false;
					return;
				}
				text += result;
				contents.setText(text);
				onByte += result.getBytes().length;
				reading = false;
			}
		});
	}
}
