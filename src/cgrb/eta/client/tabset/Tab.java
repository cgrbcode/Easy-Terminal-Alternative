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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Our base tab class. All other types of tabs extend this and use it's general methods. Big thing I'm noticing is canClose, 
 * Which I assume is set to false for the Home tab.
 * 
 * @author Alexander Boyd
 *
 */
public class Tab implements TabEventListener {
	private String title;
	private String icon;
	private Widget pane;
	private TabHead head;
	private boolean canClose = false;
	private TabEventListener listener;
	private String tooltip;
	private Widget animatedPanel;

	public Tab() {
	}

	public Tab(String title) {
		this.title = title;
	}

	public String getIcon() {
		return icon;
	}

	public void setTooltip(String tip){
		this.tooltip=tip;
	}
	public void setIcon(DataResource icon) {
		this.icon = icon.getSafeUri().asString();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPane(Widget pane) {
		pane.setHeight("100%");
		pane.setWidth("100%");
		pane.addStyleName("tabcontent");
		this.pane = pane;
	}

	public Widget getPane() {
		pane.getElement().getStyle().setPadding(4, Unit.PX);
		return pane;
	}

	/**
	 * @return
	 */
	public String getId() {
		return "";
	}

	public void setCanClose(boolean canClose) {
		this.canClose = canClose;
	}

	public boolean getCanClose() {
		return canClose;
	}

	public void setHead(TabHead head) {
		this.head = head;
	}

	public TabHead getHead() {
		if(head==null){
			head = new TabHead(getTitle(), this, getIcon(), canClose);
			if(tooltip!=null)
				head.setTooltip(tooltip);
		}
		return head;
	}

	public void tabSelected(TabEvent event) {
		if(listener!=null)
			listener.tabSelected(new TabEvent(this));
	}

	public void tabClosed(TabEvent event) {
		if(listener!=null)
			listener.tabClosed(new TabEvent(this));		
	}

	public void setListener(TabEventListener listener) {
		this.listener = listener;
	}
	
	public Widget getAnimatedPanel(){
		return this.animatedPanel;
	}
	
	public void setAnimatedPanel(Widget animatedPanel) {
		this.animatedPanel = animatedPanel;
	}

	/**
	 * @return
	 */
	public Widget getBar() {
		return null;
	}

}
