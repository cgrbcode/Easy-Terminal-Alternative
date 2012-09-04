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
package cgrb.eta.client.pipeline;

import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class InputBlock extends Composite implements MouseOverHandler, MouseOutHandler, DropListener {

	private Input input;
	private PopupPanel toolTip;
	FlowPanel top = new FlowPanel();
	HTML name = new HTML();

	public InputBlock(Input input) {
		this.input = input;
		FlowPanel pane = new FlowPanel();
		initWidget(pane);

		FlowPanel bottom = new FlowPanel();
		setStyleName("input-block");
		top.setStyleName("top");
		bottom.setStyleName("bottom");
		pane.add(top);
		pane.add(bottom);

		if (!input.isRequired()) {
			name.getElement().getStyle().setOpacity(.6);
		}
		name.setStyleName("name");
		bottom.add(name);
		name.setHTML(input.getName());
		toolTip = new PopupPanel();
		toolTip.add(new HTML("Type: " + input.getType() + "<br>Description:" + input.getDescription()));
		toolTip.setStyleName("tooltip-small");
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
		DragCreator.addDrop(top.getElement(), null, this);
	}

	public void onMouseOut(MouseOutEvent event) {
		toolTip.hide();
	}

	public void onMouseOver(MouseOverEvent event) {
		toolTip.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() + 4);
		toolTip.show();
	}

	public void dragEnter(ETAType record2) {
		ETAType record = DragCreator.getDragSource();
		if (record instanceof Output) {
			top.getElement().getStyle().setBackgroundColor("#A5CAF4");
		}
	}

	public void dragOver(ETAType record2) {
		ETAType record = DragCreator.getDragSource();
		if (record instanceof Output) {
			top.getElement().getStyle().setBackgroundColor("#A5CAF4");
		}
	}

	public void dragLeave(ETAType record2) {
		top.getElement().getStyle().setBackgroundColor("");
	}

	public void setInputValue(Output output) {
		top.clear();
		name.getElement().getStyle().setBackgroundColor("#299C47");
		HTML name = new HTML();
		name.setHTML(output.getName());
		name.setStyleName("output-block");
		top.add(name);
		input.setValue(output.getValue());
	}

	public void drop(ETAType record2) {
		ETAType record = DragCreator.getDragSource();
		if (record instanceof Output) {
			final Output output = (Output) record;
			top.getElement().getStyle().setBackgroundColor("");

			if(output.getName().startsWith("Custom")){
				SC.ask("Value?", new ValueListener<String>() {
					@Override
					public void returned(String ret) {
						if(!ret.equals("")){
							top.clear();
							name.getElement().getStyle().setBackgroundColor("#299C47");
							HTML name = new HTML();
							name.setHTML(output.getName());
							name.setStyleName("output-block");
							top.add(name);
							input.setValue("${" + ret + "}");
						}
					}
				});
			}else	if (input.getType().length() <= 5 || input.getType().substring(5).startsWith(output.getType())) {
				top.clear();
				name.getElement().getStyle().setBackgroundColor("#299C47");
				HTML name = new HTML();
				name.setHTML(output.getName());
				name.setStyleName("output-block");
				top.add(name);
				input.setValue("$'" + output.getName() + "'");
			} else {
				SC.ask("Types don't match", "The input type: " + input.getType().substring(5) + " doesn't match the output type: " + output.getType() + ", Would you like to ignore this?", new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret) {
							top.clear();
							name.getElement().getStyle().setBackgroundColor("#299C47");
							HTML name = new HTML();
							name.setHTML(output.getName());
							name.setStyleName("output-block");
							top.add(name);
							input.setValue("$'" + output.getName() + "'");
						}
					}
				});
			}
		}
	}

	public void dragEnd(ETAType record) {
	}

	public Element getDragImage(ETAType record) {
		return null;
	}

}
