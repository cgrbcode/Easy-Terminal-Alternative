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
package cgrb.eta.client.tabs;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.RichTextAreaToolbar;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.LabelButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.Help;

public class HelpEditor extends ETATab {
	HorizontalPanel bar = new HorizontalPanel();
	RichTextArea area = new RichTextArea();
	LabelButton editing = new LabelButton("Editing:");
	Help currentEdit = null;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	Table<Help> topics;

	public HelpEditor() {
		super("Help Administer");
		area.setSize("100%", "100%");
		RichTextAreaToolbar toolbar = new RichTextAreaToolbar(area);
		toolbar.setWidth("100%");

		// Add the components to a panel
		VerticalPanel grid = new VerticalPanel();
		grid.setStyleName("cw-RichText");
		grid.add(toolbar);
		grid.setCellHeight(toolbar, "60px");
		grid.add(area);

		HorizontalPanel pane = new HorizontalPanel();
		setPane(pane);
		Column<Help> name = new Column<Help>("Topic") {
			public Object getValue(Help record) {
				return record.getName();
			};
		};
		topics = new Table<Help>(true, name);
		topics.setWidth("200px");
		pane.add(topics);
		pane.add(new Filler(20));
		pane.add(grid);
		grid.setWidth("100%");
		grid.setHeight("100%");
		pane.setCellWidth(topics, "200px");
		bar.add(new Button("Add new Topic").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("What do you want to name this help topic?", new ValueListener<String>() {
					public void returned(String ret) {
						editing.setText("Editing: " + ret);
						currentEdit = new Help(ret, "", -1);
						area.setText("");
					}
				});
			}
		}));
		bar.add(new Seprator());
		bar.add(editing);
		bar.add(new Seprator());
		bar.add(new Button("Save").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (currentEdit != null) {
					currentEdit.setHtml(area.getHTML());
					sqlService.saveHelpTopic(currentEdit, new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
							currentEdit.setId(result);
						}
					});
				}
			}
		}));
		bar.add(new Seprator());

		sqlService.getHelpList(new MyAsyncCallback<Vector<Help>>() {
			public void success(Vector<Help> result) {
				topics.setData(result);
			};
		});
		topics.setRowClickHandler(new RowClickHandler<Help>() {
			public void rowClicked(Help record, int col, int row) {
				currentEdit=record;
				 area.setHTML(record.getHtml());
				 editing.setText("Editing: "+record.getName());
			}
		});
	}

	@Override
	public String getId() {
		return "hlp";
	}

	@Override
	public Widget getBar() {
		return bar;
	}

}
