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
package cgrb.eta.client.table;

import cgrb.eta.client.button.ValueListener;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

public class BooleanEditor extends Editor {
	boolean checked = false;
	HTML box;
	private ValueListener<Boolean> handler;
	
	public void setHandler(ValueListener<Boolean> handler) {
		this.handler = handler;
	}

	public BooleanEditor(boolean checkedM,String yes,String no){
		box = new HTML("<p class='on'>"+yes+"</p><p class='off'>"+no+"</p><div class=\"inner\"></div>");
		initWidget(box);
		setStyleName("check-box");
		box.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setChecked(!checked);
				event.stopPropagation();
				if(handler!=null)
					handler.returned(checked);
			}
		});
		setChecked(checkedM);
	}
	
	public BooleanEditor(boolean checkedM) {
		box = new HTML("<p class='on'>YES</p><p class='off'>NO</p><div class=\"inner\"></div>");
		initWidget(box);
		setStyleName("check-box");
		box.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setChecked(!checked);
				event.stopPropagation();
			}
		});
		setChecked(checkedM);
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		if (checked) {
			addStyleName("checked");
			removeStyleName("unchecked");
		} else {
			addStyleName("unchecked");
			removeStyleName("checked");
		}
	}

	@Override
	public String getEditingValue() {
		return "" + checked;
	}

	@Override
	public void setFocus() {

	}

}
