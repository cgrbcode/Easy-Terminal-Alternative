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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * An ETA theme button that should be used as a simple item button in a menu or on the bar bellow a tab pane.
 * <p>
 * <b>CSS Style Rules:</b><br>
 * .eta-button{primary style}<br>
 * When the button is in its down state:<br>
 * .button-down{primary style}
 * </p>
 * 
 * @author Alexander Boyd
 */
public class Button extends HorizontalPanel implements MouseDownHandler, MouseUpHandler, ClickHandler, MouseOutHandler, MouseOverHandler {
	protected String title;
	private String dependent = "";
	protected ClickHandler handler;
	protected PopupPanel toolTip;
	protected String toolTipString;
	protected MenuButton parent;
	protected HandlerRegistration downReg;

	/**
	 * Protected constructor that prevents an empty button to be created. This should only be called when a class extends this one and doesn't want to have text in the button
	 */
	protected Button() {
		downReg = addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
	}

	/**
	 * The constructor will create a Button with the specified title and add the listeners to it. Also if the title is very long it will split it up so no more than 70 characters are on a line.
	 * 
	 * @param name
	 *          The title to give this button
	 */
	public Button(String name) {
		title=name;
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		//if the text is greater than 70 characters break it down to ensure that each line is less than 70 chars.
		if (name.length() > 70) {
			String[] tempName = name.split(" ");
			String currentName = "";
			VerticalPanel namePanel = new VerticalPanel();
			int i = 0;
			for (String on : tempName) {
				currentName += " " + on;
				if (currentName.length() >= 70 || i == tempName.length - 1) {
					Label title = new Label(currentName);
					title.setStyleName("button-text");
					title.setWordWrap(false);
					namePanel.add(title);
					currentName = "";
				}
				i++;
			}
			add(namePanel);
		} else {
			Label title = new Label(name);
			title.setStyleName("button-text");
			title.setWordWrap(false);
			title.setWidth("100%");
			add(title);
		}
		//add the style and all the mouse handlers
		setStyleName("eta-button");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
	}

	/**
	 * This will remove the down click handler. This is useful if you have items inside this button that also have a down handler.
	 */
	public void removeDown() {
		downReg.removeHandler();
	}

	/**
	 * This is useful because if this button has a parent it will try to hide the parent when it is clicked.
	 * 
	 * @param parent
	 *          The MenuButton that this button sits in.
	 */
	public void setParent(MenuButton parent) {
		this.parent = parent;
	}

	/**
	 * This will return the parent button of this button.
	 * 
	 * @return MenuButton The button this button sits in.
	 */
	public MenuButton getParentMenu() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.user.client.ui.UIObject#setStyleDependentName(java.lang.String, boolean)
	 */
	@Override
	public void setStyleDependentName(String styleSuffix, boolean add) {
		dependent = styleSuffix;
		removeStyleName("eta-button");
		addStyleName("eta-button" + dependent);
	}

	/**
	 * This will change the text of a button to whatever is specified. It will also only put it on one line.
	 * 
	 * @param text
	 *          String the text to set the title of the button to.
	 */
	public void setText(String text) {
		clear();
		Label title = new Label(text);
		this.title = text;
		title.setWordWrap(false);
		add(title);
	}

	/**
	 * This will set the ClickHandler of this button and then return the button. This is for convenience so you can create the button and add the ClickHandler at the same time.
	 * 
	 * @param handler
	 *          The ClickHandler that will be called when this button is clicked.
	 * @return This, the button that the ClickHandler is being added to.
	 * 
	 * @see ClickHandler
		showOptions.onMouseOut(new MouseOutEvent() {
			public void onMouseOut() {
				// showOptions.setStyleDependentName("-options-button", true);
				// do y
			}
		});
	 */
	public Button setClickHandler(ClickHandler handler) {
		this.handler = handler;
		return this;
	}
	
	public ClickHandler getHandler(){
		return handler;
	}

	/**
	 * A convenience method to set the height of this button. The width is automatic
	 * 
	 * @param size
	 *          The size of pixels to set the height of this button
	 */
	public void setSize(int size) {
		setHeight(size + "px");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.MouseUpHandler#onMouseUp(com.google.gwt.event.dom.client.MouseUpEvent)
	 */
	public void onMouseUp(MouseUpEvent event) {
		// This button must of been clicked. Change the css state and notify the handler if there is one.
		removeStyleName("button-down" + dependent);
		addStyleName("eta-button" + dependent);
		if (handler != null)
			handler.onClick(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.MouseDownHandler#onMouseDown(com.google.gwt.event.dom.client.MouseDownEvent)
	 */
	public void onMouseDown(MouseDownEvent event) {
		removeStyleName("eta-button" + dependent);
		addStyleName("button-down" + dependent);
		if (toolTip != null)
			toolTip.hide();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
	 */
	public void onClick(ClickEvent event) {
		removeStyleName("button-down" + dependent);
		addStyleName("eta-button" + dependent);
		if (toolTip != null)
			toolTip.hide();
	}

	/**
	 * This will setup a tool-tip for this button. When the mouse is hovering over the button the tool-tip will be displayed.
	 * 
	 * @param text
	 *          String of the tool-tip to display
	 */
	public void setToolTip(String text) {
		toolTip = new PopupPanel();
		toolTip.add(new HTML(text));
		toolTip.setStyleName("tooltip");
		toolTipString=text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
	 */
	public void onMouseOver(MouseOverEvent event) {
		// if there is a tool-tip display it because the mouse is hovering over this button
		if (toolTip != null) {
			toolTip.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() + 4);
			toolTip.show();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
	 */
	public void onMouseOut(MouseOutEvent event) {
		// hide the tool-tip because the mouse is no longer over this button
		if (toolTip != null)
			toolTip.hide();
	}
	
	/**
	 * @return The tool tip text if any. null if there isn't a tooltip
	 */
	public String getToolTipText(){
		return toolTipString;
	}
	
	public String getTitle(){
		return title;
	}
}
