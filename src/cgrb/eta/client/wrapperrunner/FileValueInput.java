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
package cgrb.eta.client.wrapperrunner;

import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.wrapper.Output;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FileValueInput extends ValueInput{
	TextBox input;
	public FileValueInput(){
		input  = new TextBox();
		HorizontalPanel temp = new HorizontalPanel();
		input.setStyleName("eta-input2");
		temp.add(input);
		SimpleButton browse = new SimpleButton("...").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new FileSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						input.setValue(items[0]);
						values = items;
						update();
					}
				},  FileBrowser.FILE_SELECT);
			}
		});
		temp.add(browse);
		initWidget(temp);
		temp.setWidth("100%");
		input.addKeyUpHandler(this);
		DragCreator.addDrop(input.getElement(), null, new DropListener() {
			public void drop(ETAType record) {
				ETAType rec = DragCreator.getDragSource();
				if(rec instanceof File){
					input.setText(((File)rec).getPath());
					update();
					input.getElement().getStyle().setBorderColor("");
				}else if(rec instanceof Output){
					input.setText(((Output)rec).getValue());
					update();
					input.getElement().getStyle().setBorderColor("");
				}
			}
			
			public void dragOver(ETAType record) {
				input.getElement().getStyle().setBorderColor("#123A67");
			}
			public void dragLeave(ETAType record) {
				input.getElement().getStyle().setBorderColor("");
			}
			public void dragEnter(ETAType record) {
			}
		});
	}
	
	@Override
	public void setValue(String defaultValue) {
		input.setText(defaultValue);
	}

	@Override
	public String getValue() {
		return input.getText();
	}

}
