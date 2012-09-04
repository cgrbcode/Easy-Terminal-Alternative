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


import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.window.Window;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class WrapperSelector implements ItemSelector{
	private ItemSelector event;
	private Window window;
	public WrapperSelector(final ItemSelector eventer){
		event=eventer;
		final UserWrapperGrid results = new UserWrapperGrid(false){
			@Override
			protected void onLoad() {
				super.onLoad();
				getScrollPanel().setHeight(com.google.gwt.user.client.Window.getClientHeight()*.7+ "px");
				com.google.gwt.user.client.Window.addResizeHandler(new ResizeHandler() {
					public void onResize(ResizeEvent event) {
						getScrollPanel().setHeight(com.google.gwt.user.client.Window.getClientHeight()*.7+ "px");
					}
				});
			}
		};
		results.setWidth("400px");
		results.setSelector(this);
		VerticalPanel panel = new VerticalPanel();
		panel.add(results);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				eventer.itemSelected(new String[]{results.getSelectedRecord().getWrapperId()+""});
				window.destroy();
			}
		}));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window=new Window("Please select a wrapper", panel,true);
		window.showWindow();
	}
	public void itemSelected(String[] items) {
		event.itemSelected(items);
		window.destroy();
	}
}
