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

import cgrb.eta.client.button.SearchItemSelected;
import cgrb.eta.shared.SearchResultItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchResultLine extends HorizontalPanel implements ClickHandler,MouseDownHandler,MouseUpHandler{

	private SearchItemSelected handler;
	private SearchResultItem item;
	public SearchResultLine( SearchResultItem line,SearchItemSelected handler) {
		this.handler=handler;
		this.item=line;
		Image icon = new Image(line.getIcon());
		icon.setHeight(20+"px");
		icon.setWidth(20+"px");
		add(icon);
		setCellWidth(icon, "20px");
		Label name = new Label(line.getTitle());
		name.addStyleName("searchname");
		name.setHeight("20px");
		name.setWidth("100%");
		setStyleName("searchline");
		VerticalPanel temp = new VerticalPanel();
		temp.add(name);
		name.setWordWrap(false);
		if(line.getFound()!=null){
			HTML result = new HTML(line.getFound());
			result.addStyleName("searchfound");
//			result.setHeight("20px");
			result.setWidth("100%");
			temp.add(result);
		}else{
			name.addStyleName("searchnameonly");
		}
		
		
		add(temp);
		setWidth(200+"px");
		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
		addDomHandler(this, ClickEvent.getType());
	}

	public void onMouseUp(MouseUpEvent event) {
//		setStyleName("line-down",false);
//		setStyleName("searchline",true);
		 getElement().getStyle().setBackgroundColor("");
	}

	public void onMouseDown(MouseDownEvent event) {
//		setStyleName("line-down",true);
//		setStyleName("searchline",false);
		getElement().getStyle().setBackgroundColor("#34A6D1");
	}

	public void onClick(ClickEvent event) {
		if(handler!=null)
			handler.itemSelected(item);
	}
}
