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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Alexander Boyd
 *
 */
public class ImgMenuButton extends MenuButton {

	private Image icon;
	private DataResource src;
	private String title;

	/**
	 * @param name
	 */
	public ImgMenuButton(DataResource src) {
		icon = new Image(src.getSafeUri().asString());
		this.src = src;
		icon.setWidth("25px");
		icon.setHeight("25px");
		icon.setPixelSize(25, 25);
		addStyleName("eta-img-menu-button");
		temp = new HorizontalPanel();
		temp.addStyleName("eta-button-img");
		Label arrow = new Label();
		arrow.setStyleName("gbma");
		temp.add(icon);
		temp.add(arrow);
		temp.setHeight("25px");
		add(temp);
		setWidth("1px");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());

		menu = new VerticalPanel();
		addStyleName("eta-button-menu");
		add(menu);
		menu.setStyleName("sub-menu");
		menu.setVisible(false);
	}

	public ImgMenuButton(DataResource src, String title) {
		icon = new Image(src.getSafeUri().asString());
		icon.setWidth("25px");
		icon.setHeight("25px");
		icon.setPixelSize(25, 25);
		addStyleName("eta-img-menu-button");
		temp = new HorizontalPanel();
		temp.addStyleName("eta-button-img");
		Label arrow = new Label();
		arrow.setStyleName("gbma");
		temp.add(icon);
		Label lable = new Label(title);
		lable.setStyleName("button-text");
		lable.setWordWrap(false);
		temp.add(lable);
		temp.add(arrow);
		temp.setHeight("25px");
		add(temp);
		setWidth("1px");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());

		menu = new VerticalPanel();
		addStyleName("eta-button-menu");
		add(menu);
		menu.setStyleName("sub-menu");
		menu.setVisible(false);
	}

	public void setSize(int size) {
		icon.setWidth(size + "px");
		icon.setHeight(size + "px");
		temp.setHeight(size + "px");
	}

	public void setClickHandler(ClickHandler handler) {
		icon.addDomHandler(handler, ClickEvent.getType());
	}

	public void onMouseOut(MouseOutEvent event) {
		menu.setVisible(false);
		temp.addStyleName("eta-img-menu-button");
		temp.removeStyleName("button-hover3");

	}

	public void onClick(ClickEvent event) {
		// removeStyleName("button-down");
		// addStyleName("eta-button");
		event.stopPropagation();
		if (!lock) {
			menu.setVisible(false);
			temp.addStyleName("eta-img-menu-button");
			temp.removeStyleName("button-hover3");
		}
	}

	public void onMouseOver(MouseOverEvent event) {
		menu.setVisible(true);
		temp.addStyleName("button-hover3");
		temp.removeStyleName("eta-img-menu-button");
	}

	public DataResource getDataResource() {
		return src;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
