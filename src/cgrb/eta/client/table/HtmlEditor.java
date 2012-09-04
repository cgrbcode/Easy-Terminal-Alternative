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

import cgrb.eta.client.RichTextAreaToolbar;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.window.SC;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HtmlEditor extends Editor {
	protected TextBox editor;
	RichTextArea area = new RichTextArea();
	VerticalPanel grid = new VerticalPanel();

	public HtmlEditor(final String value) {
		editor = new TextBox();
		HorizontalPanel pane = new HorizontalPanel();
		pane.setWidth("100%");
		pane.add(editor);
		ImgButton edit = (ImgButton) new ImgButton(Resources.INSTANCE.edit()).setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				area.setHTML(value);
				SC.ask("Editing description", grid, new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if(ret){
							editor.setText(area.getHTML());
						}
					}
				});
			}
		});
		area.setSize("100%", "300px");
		RichTextAreaToolbar toolbar = new RichTextAreaToolbar(area);
		toolbar.setWidth("100%");
		
		grid.setStyleName("cw-RichText");
		grid.add(toolbar);
		grid.setCellHeight(toolbar, "60px");
		grid.add(area);
		
		pane.setCellWidth(edit, "20px");
		edit.setSize(20);
		pane.add(edit);
		
		initWidget(pane);
		editor.setValue(value);
		editor.setStyleName("eta-input2");
		editor.addKeyPressHandler(this);
	}

	public String getEditingValue() {
		return editor.getValue();
	}

	public void setFocus() {
		editor.setFocus(true);
	}

}
