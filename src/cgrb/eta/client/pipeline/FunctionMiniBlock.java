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

import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.pipeline.PipelineWrapper;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class FunctionMiniBlock extends Composite implements DragListener, MouseOverHandler, MouseOutHandler {
	private PopupPanel toolTip;
	private Block block;
	
	public FunctionMiniBlock(PipelineWrapper pipeline){
		this.block=new Block(pipeline, null);
		HTML name = new HTML();
		name.setStyleName("function-block");
		name.setHTML(pipeline.getName());
		initWidget(name);
		getElement().setAttribute("draggable", "true");
		DragCreator.addDrag(getElement(), pipeline, this);
		toolTip = new PopupPanel();
		toolTip.add(new HTML("Description:" + pipeline.getPipeline().getDescription()));
		toolTip.setStyleName("tooltip-small");
		addDomHandler(this, MouseOutEvent.getType());
		addDomHandler(this, MouseOverEvent.getType());
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		toolTip.hide();
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		toolTip.setPopupPosition(getAbsoluteLeft(), getAbsoluteTop() + getOffsetHeight() + 4);
		toolTip.show();
	}

	@Override
	public void dragStart(ETAType record) {
		getElement().getStyle().setOpacity(.5);
	}

	@Override
	public void dragEnter(ETAType record) {
	}

	@Override
	public void dragOver(ETAType record) {
	}

	@Override
	public void dragLeave(ETAType record) {
	}

	@Override
	public void drop(ETAType record) {
	}

	@Override
	public void dragEnd(ETAType record) {
		getElement().getStyle().setOpacity(1);
	}

	@Override
	public Element getDragImage(ETAType record) {
		return block.getElement();
	}

}
