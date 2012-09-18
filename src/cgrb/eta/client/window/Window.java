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
package cgrb.eta.client.window;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * Window is used for showing things over other things. Such as viewing a file in our folders.
 * 
 * The java file itself handles all the stylings and placement of our window. Probably the only neat function is the add widget, in which you can
 * add any other widget into our window.
 * 
 * 
 * @author Alexander Boyd
 *
 */
public class Window {
	Label title;
	HTML modalMask = new HTML();
	FlowPanel outside;
	HorizontalPanel bar;
	private boolean isDraging = false;
	private boolean isModal =false;
	private int x_start = 0;
	private int y_start = 0;
	private MouseMoveHandler moveHandler;
	/**
	 * Only constructor. Builds our window
	 * 
	 * @param title	Title of the window, displayed at the top.
	 * @param content	Any widget that needs to be added to the FlowPanel should be passed as content.
	 * @param isModal	The boolean representing if you need to interact with the window or not before returning.
	 */
	public Window(String title, Widget content,boolean isModal) {
		this.isModal=isModal;
		this.title = new Label(title);
		this.title.setWordWrap(false);
		bar = new HorizontalPanel();
		bar.setWidth("100%");
		bar.setStyleName("window-bar");
		modalMask.setStyleName("modal-mask");
		VerticalPanel master = new VerticalPanel();
		outside = new FlowPanel() {
			@Override
			protected void onLoad() {
				getElement().getStyle().setLeft((com.google.gwt.user.client.Window.getClientWidth() / 2) - (getOffsetWidth() / 2), Unit.PX);
				getElement().getStyle().setTop((com.google.gwt.user.client.Window.getClientHeight() / 2) - (getOffsetHeight() / 2), Unit.PX);
				super.onLoad();
			}
		};
		com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				outside.getElement().getStyle().setLeft((com.google.gwt.user.client.Window.getClientWidth() / 2) - (outside.getOffsetWidth() / 2), Unit.PX);
				outside.getElement().getStyle().setTop((com.google.gwt.user.client.Window.getClientHeight() / 2) - (outside.getOffsetHeight() / 2), Unit.PX);
			}
		});
		outside.setStyleName("eta-window");
		HorizontalPanel header = new HorizontalPanel();
		this.title.addMouseDownHandler(new MouseDownHandler() {

			@Override
			public void onMouseDown(MouseDownEvent event) {
				isDraging = true;
				x_start = event.getX();
				y_start = event.getY();
			}
		});

		moveHandler = new MouseMoveHandler() {
			@Override
			public void onMouseMove(MouseMoveEvent event) {
				if(!isDraging)return;
				int left = event.getClientX() - x_start;
				int top = event.getClientY() - y_start;
				if (left > 0 && left + outside.getOffsetWidth() < com.google.gwt.user.client.Window.getClientWidth())
					outside.getElement().getStyle().setLeft(left-16,Unit.PX);
				if (top > 0 && top + outside.getOffsetHeight() < com.google.gwt.user.client.Window.getClientHeight())
					outside.getElement().getStyle().setTop(top-16, Unit.PX);
			}
		};
		modalMask.addMouseMoveHandler(moveHandler);
		this.title.addMouseMoveHandler(moveHandler);
		outside.addDomHandler(moveHandler,MouseMoveEvent.getType());
		master.addDomHandler(moveHandler,MouseMoveEvent.getType());
		
		this.title.addMouseUpHandler(new MouseUpHandler() {
			@Override
			public void onMouseUp(MouseUpEvent event) {
				isDraging=false;
			}
		});
		this.title.getElement().getStyle().setCursor(Cursor.MOVE);
		header.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		header.setHeight("25px");
		master.add(header);
		header.setStyleName("header");
		header.add(this.title);
		Label close = new Label("x");
		close.setStyleName("close");
		FlowPanel tempContent = new FlowPanel() {
			@Override
			protected void onLoad() {
				super.onLoad();
				if (com.google.gwt.user.client.Window.getClientHeight() < getOffsetHeight()) {
					setHeight(com.google.gwt.user.client.Window.getClientHeight() - 100 + "px");
					getElement().getStyle().setOverflow(Overflow.AUTO);
				} else if (com.google.gwt.user.client.Window.getClientWidth() < getOffsetWidth()) {
					setWidth(com.google.gwt.user.client.Window.getClientWidth() - 300 + "px");
					getElement().getStyle().setOverflow(Overflow.AUTO);
				}
			}
		};
		tempContent.getElement().getStyle().setPadding(10, Unit.PX);
		tempContent.add(content);
		content.setWidth("100%");
		master.add(bar);
		master.add(tempContent);
		header.add(close);
		header.setCellHorizontalAlignment(close, HasHorizontalAlignment.ALIGN_RIGHT);
		header.setCellWidth(close, "16px");
		header.setCellHeight(close, "16px");
		header.setWidth("100%");
		master.setWidth("100%");
		master.setCellHeight(header, "25px");
		master.setStyleName("inside");
		outside.add(master);

		close.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				destroy();
			}
		});

	}

	public void setWidth(String width) {
		outside.setWidth(width);
	}

	public void destroy() {
		RootPanel.get().remove(outside);
		if(isModal)
		RootPanel.get().remove(modalMask);
	}

	public void showWindow() {
		if(isModal)
		RootPanel.get().add(modalMask);
		RootPanel.get().add(outside);
	}
	/**
	 * Adds a widget to our bar, our bar is the bottom of the window and can be used for buttons or other neat things.
	 * 
	 * 
	 * @param wid The widget to be passed and added to our bottom bar.
	 */
	public void addBar(Widget wid) {
		bar.add(wid);
	}
}
