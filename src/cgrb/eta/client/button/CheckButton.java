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

import cgrb.eta.client.table.BooleanEditor;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;

/**
 * An ETA theme check button that should be used as a simple yes/no item. This uses a BooleanEditor as the widget.
 * 
 * @see BooleanEditor
 * @author Alexander Boyd
 */
public class CheckButton extends Button implements MouseDownHandler, MouseUpHandler, ClickHandler {

	boolean checked = false;
	private BooleanEditor value;

	/**
	 * The constructor for CheckButton. This wraps a BooleanEditor in a HorzontialPanel with a Label.
	 * @param name The title of this CheckButton
	 */
	public CheckButton(String name) {
		Label title = new Label(name);
		title.setWordWrap(false);
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		add(title);
		value = new BooleanEditor(false);
		add(value);
	}

	/**
	 * @return boolean the value of this CheckButton
	 */
	public boolean getValue() {
		return Boolean.parseBoolean(value.getEditingValue());
	}

	/**
	 * This sets weather or not the check button is set on or off
	 * @param checked boolean if this button is on or off
	 */
	public void setValue(boolean checked) {
		value.setChecked(checked);
	}

	/* (non-Javadoc)
	 * @see cgrb.eta.client.button.Button#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	public void onClick(ClickEvent event) {
	}

	/* (non-Javadoc)
	 * @see cgrb.eta.client.button.Button#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
	 */
	public void onMouseUp(MouseUpEvent event) {
	}

	/* (non-Javadoc)
	 * @see cgrb.eta.client.button.Button#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
	 */
	public void onMouseDown(MouseDownEvent event) {
	}
}
