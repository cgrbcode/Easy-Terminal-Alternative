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

import cgrb.eta.client.EnterButtonEventManager;
import cgrb.eta.client.EnterListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class SimpleButton extends HorizontalPanel implements MouseDownHandler, MouseUpHandler, ClickHandler {
	String dependantStyle = "";
	ClickHandler handler;
	Label label;
	EnterListener listener;

	public SimpleButton(String title) {
		setHeight("20px");
		label = new Label(title);
		label.setWordWrap(false);
		add(label);
		if (title.equals("OK")) {
			listener = new EnterListener() {
				public void enter() {
					if (handler != null) {
						handler.onClick(null);
						EnterButtonEventManager.removeListener(listener);
					}
				}
			};
			EnterButtonEventManager.addListener(listener);
		}
		addStyleName("simple-button");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
	}

	public SimpleButton() {
	}

	public SimpleButton addClickHandler(ClickHandler handle) {
		this.handler = handle;
		return this;
	}

	@Override
	public void setStyleDependentName(String styleSuffix, boolean add) {
		if (add) {
			removeStyleName("simple-button" + dependantStyle);
			dependantStyle = styleSuffix;
			addStyleName("simple-button" + dependantStyle);
		} else {
			removeStyleName("simple-button" + dependantStyle);
			dependantStyle = "";
			addStyleName("simple-button" + dependantStyle);
		}
	}

	public void onClick(ClickEvent event) {
	}

	public void onMouseUp(MouseUpEvent event) {
		removeStyleName("simple-button" + dependantStyle + "-down");
		addStyleName("simple-button" + dependantStyle);
		if (handler != null)
			handler.onClick(null);
	}

	public void onMouseDown(MouseDownEvent event) {
		removeStyleName("simple-button" + dependantStyle);
		addStyleName("simple-button" + dependantStyle + "-down");
	}
}
