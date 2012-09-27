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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A custom label that has a few useful features.
 * 
 * Contains a way to have a tooltip appear on hover, has a way to set the title such that it returns the current widget, useful for quick inline declarations of things.
 * 
 * @author Alexander Boyd
 * 
 */
public class SimpleLabel extends VerticalPanel implements MouseOverHandler, MouseOutHandler {
	protected PopupPanel toolTip;

	public SimpleLabel(String title) {
		if (title.length() > 70) {
			String[] tempName = title.split(" ");
			String currentName = "";
			int i = 0;
			for (String on : tempName) {
				if ((currentName + on).length() >= 70) {
					Label title2 = new Label(currentName);
					title2.setWordWrap(false);
					add(title2);
					currentName = "";
				}
				currentName += " " + on;
				if (i == tempName.length - 1) {
					Label title2 = new Label(currentName);
					title2.setWordWrap(false);
					add(title2);
				}
				i++;
			}
		} else {
			Label title2 = new Label(title);
			title2.setWordWrap(false);
			add(title2);
		}
		addStyleName("simple-label");
	}

	public Widget setText(String text) {
		clear();
		add(new Label(text));
		return this;
	}

	public SimpleLabel setColor(String color) {
		getElement().getStyle().setColor(color);
		return this;
	}

	public void setToolTip(String tip) {
		toolTip = new PopupPanel();
		toolTip.add(new HTML(tip));
		toolTip.setStyleName("tooltip");
		toolTip.setAutoHideEnabled(true);
		addDomHandler(this, MouseOverEvent.getType());
		addDomHandler(this, MouseOutEvent.getType());
	}

	Timer t = new Timer() {
		@Override
		public void run() {
			toolTip.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() + 4);
			toolTip.show();
		}
	};

	public void onMouseOver(MouseOverEvent event) {
		if (toolTip != null) {
			t.cancel();
			t.schedule(200);
		}
	}

	public void onMouseOut(MouseOutEvent event) {
		if (toolTip != null) {
			t.cancel();
			toolTip.hide();
		}
	}

	public SimpleLabel setFontSize(int size) {
		getElement().getStyle().setFontSize(size, Unit.PX);
		return this;
	}

	public Widget setSelectable() {
		addStyleName("selectable");
		return this;
	}
}
