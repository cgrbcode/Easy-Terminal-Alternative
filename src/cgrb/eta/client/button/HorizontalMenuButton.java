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

import java.util.Vector;

import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HorizontalMenuButton extends HorizontalPanel implements MouseDownHandler, MouseUpHandler, ClickHandler, MouseOverHandler, MouseOutHandler {
	private ClickHandler handler;
	protected VerticalPanel menu;
	protected HorizontalPanel temp;
	private Label title;

	public HorizontalMenuButton(String name) {
		temp = new HorizontalPanel();
		temp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		temp.addStyleName("eta-button");
		title = new Label(name);
		title.setWordWrap(false);
		Label arrow = new Label();
		arrow.setStyleName("gbmb");
		temp.add(title);
		temp.setCellHorizontalAlignment(title,HasHorizontalAlignment.ALIGN_LEFT);
		temp.add(arrow);
		temp.setCellHorizontalAlignment(arrow,HasHorizontalAlignment.ALIGN_RIGHT);

		add(temp);
		setWidth("100%");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
		menu = new VerticalPanel();
		menu.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		addStyleName("eta-button-menu");
		add(menu);
		menu.setStyleName("sub-menu");
		menu.setVisible(false);

	}
	

	public void setRight(int pixles) {
		menu.getElement().getStyle().setRight(pixles, Unit.PX);
	}

	public HorizontalMenuButton() {
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
	}

	public void setTitle(String title) {
		this.title.setText(title);
	}

	public void clearMenu() {
		menu.clear();
		menu.setVisible(false);
	}

	protected boolean lock = false;

	public void forceVisible() {
		lock = true;
	}

	public void forceHide() {
		menu.setVisible(false);
		lock = false;
	}

	public void addButton(int before, Button butt) {
		butt.setStyleDependentName("-menu2", true);
		butt.setWidth("100%");
		menu.insert(butt, before);
//		butt.setParent();
	}

	@Override
	public void clear() {
		menu.clear();
	}
	
	public Vector<Widget> getMenuItems(){
		Vector<Widget> ret = new Vector<Widget>();
		for(int i=0;i<menu.getWidgetCount();i++)
			ret.add(menu.getWidget(i));
		return ret;
	}

	public void addButton(Button butt) {
		butt.setStyleDependentName("-menu2", true);
		butt.setWidth("100%");
		menu.add(butt);
//		butt.setParent(this);
	}

	public void onMouseUp(MouseUpEvent event) {
		// removeStyleName("button-down");
		// addStyleName("eta-button");
	}

	public void onMouseDown(MouseDownEvent event) {
		// addStyleName("button-down");
		// removeStyleName("eta-button");
	}

	public void onClick(ClickEvent event) {
		// removeStyleName("button-down");
		// addStyleName("eta-button");
		event.stopPropagation();
		if (!lock) {
			menu.setVisible(false);
			temp.addStyleName("eta-button");
			temp.removeStyleName("button-hover2");
		}
	}

	Timer t = new Timer() {

		@Override
		public void run() {
			menu.setVisible(false);
			temp.addStyleName("eta-button");
			temp.removeStyleName("button-hover2");
		}
	};

	public void onMouseOut(MouseOutEvent event) {
		if (!lock) {
			t.schedule(2);
		}
	}

	public void setClickHandler(ClickHandler handler) {
		title.addDomHandler(this.handler, ClickEvent.getType());
		this.handler = handler;
	}

	public void onMouseOver(MouseOverEvent event) {
		t.cancel();
		menu.setVisible(true);
		lock = false;
		temp.addStyleName("button-hover2");
		temp.removeStyleName("eta-button");
	}

}
