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

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HomeIcon extends Composite implements ClickHandler{
	private FlowPanel main;
	private	VerticalPanel menu = new VerticalPanel();

	
	public HomeIcon(DataResource icon,String desc){
		Image image = new Image(icon.getSafeUri().asString());
		image.setStyleName("iconmenuimg");
		image.setHeight("128px");
		main=new FlowPanel();
		main.add(image);
		Label name = new Label(desc);
		name.setStyleName("mainicon");
		name.setWidth(138+"px");
		name.setHeight(20+"px");
		main.setWidth(138+"px");
		main.add(name);
		initWidget(main);

		setStyleName("homeIconMenu");
		menu.setStyleName("home-icon-menu");
		main.add(menu);
		main.getElement().getStyle().setPosition(Position.RELATIVE);

		menu.setWidth("100%");
		menu.setVisible(false);
		addDomHandler(new MouseOverHandler() {
			
			public void onMouseOver(MouseOverEvent event) {
				menu.setVisible(true);
			}
		}, MouseOverEvent.getType());
		
		addDomHandler(new MouseOutHandler() {
			
			public void onMouseOut(MouseOutEvent event) {
				menu.setVisible(false);
			}
		}, MouseOutEvent.getType());
		addDomHandler(this, ClickEvent.getType());
	}

	public void onClick(ClickEvent event) {
		//getContextMenu().showContextMenu();
	}
	
	public void setMenu(HomeIconMenuItem... items) {

		for(HomeIconMenuItem item:items){
			Label temp = new Label(item.getTitle());
			temp.setStyleName("iconmenuitem");
			
			if(item.getHandler()!=null)
			temp.addClickHandler(item.getHandler());
			temp.setHeight(20+"px");
			menu.add(temp);
		}
	}

}
