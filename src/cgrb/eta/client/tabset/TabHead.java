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
package cgrb.eta.client.tabset;

import cgrb.eta.client.images.Resources;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public class TabHead extends Composite implements ClickHandler{

	private TabEventListener listener;
	private boolean isSelected = true;
	private Label title;
	private FlowPanel layout;
	private PopupPanel toolTip;

	public TabHead(String name, TabEventListener listenerr, String iconSrc, boolean closeable) {
		layout = new FlowPanel(){
			@Override
			public void add(Widget w) {
				w.getElement().getStyle().setFloat(Float.LEFT);
				super.add(w);
			}
		};
		HTML topy = new HTML();
		topy.setStyleName("tab-top");
		layout.add(topy);
		topy.getElement().getStyle().setFloat(Float.NONE);
		layout.getElement().getStyle().setFloat(Float.LEFT);
		
		HTML leftRound = new HTML();
		layout.add(leftRound);
		leftRound.setStyleName("tab-left-round");
		leftRound.getElement().getStyle().setFloat(Float.NONE);
		
		HTML rightRound = new HTML();
		layout.add(rightRound);
		rightRound.setStyleName("tab-right-round");
		rightRound.getElement().getStyle().setFloat(Float.NONE);
		
		this.title = new Label(name);
		title.setWordWrap(false);
		this.listener = listenerr;

		if (iconSrc != null && !iconSrc.equals("")) {
			Image icon = new Image(iconSrc);
			icon.setHeight("20px");
			icon.setWidth("20px");
			icon.setPixelSize(20, 20);
			icon.addStyleName("tabicon");
			icon.getElement().getStyle().setMarginLeft(4, Unit.PX);

			layout.add(icon);
		}
		layout.add(title);

		if (closeable) {
			final Image closeBut = new Image();
			closeBut.addStyleName("closeBut");
			closeBut.setHeight("15px");
			closeBut.setWidth("15px");
			closeBut.setUrl(Resources.INSTANCE.closeButton().getSafeUri().asString());
			layout.add(closeBut);
			closeBut.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					listener.tabClosed(null);
					event.stopPropagation();
				}
			});
			closeBut.addDomHandler(new MouseDownHandler() {
				public void onMouseDown(MouseDownEvent event) {
					closeBut.setUrl(Resources.INSTANCE.closeButtonDown().getSafeUri().asString());				
				}
			}, MouseDownEvent.getType());
			
			closeBut.addMouseOutHandler(new MouseOutHandler() {
				
				public void onMouseOut(MouseOutEvent event) {
					closeBut.setUrl(Resources.INSTANCE.closeButton().getSafeUri().asString());
				}
			});
			
			closeBut.addDomHandler(new MouseOverHandler() {
				public void onMouseOver(MouseOverEvent event) {
					closeBut.setUrl(Resources.INSTANCE.closeButtonOver().getSafeUri().asString());					
				}
			}, MouseOverEvent.getType());
			
		}             
		
		layout.setStyleName("tab-head");
		initWidget(layout);
		
		addDomHandler(this, ClickEvent.getType());

	}

	public void unSelect() {
		isSelected = false;
		removeStyleName("selected");
	}

	public boolean isSelected() {
		return isSelected;
	}
	
	public void setTooltip(String tooltip){
		toolTip = new PopupPanel();
		toolTip.add(new HTML(tooltip));
		toolTip.setStyleName("tooltip");
		addDomHandler(new MouseOutHandler() {
			public void onMouseOut(MouseOutEvent event) {
				toolTip.hide();
			}
		}, MouseOutEvent.getType());
		addDomHandler(new MouseOverHandler() {
			public void onMouseOver(MouseOverEvent event) {
				toolTip.setPopupPosition(getAbsoluteLeft()+getOffsetWidth()+4, getAbsoluteTop() );
				toolTip.show();
			}
		}, MouseOverEvent.getType());
		
	}

	public void select() {
		isSelected = true;
		addStyleName("selected");
	}

	public void onClick(ClickEvent event) {
		listener.tabSelected(null);
	}
}
