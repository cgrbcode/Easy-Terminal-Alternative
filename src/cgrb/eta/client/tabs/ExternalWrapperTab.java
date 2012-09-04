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

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.VoidAsyncCallback;
import cgrb.eta.client.WrapperSelector;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.etatype.EWrapper;
import cgrb.eta.shared.wrapper.Wrapper;

public class ExternalWrapperTab extends ETATab {
	HorizontalPanel bar = new HorizontalPanel();
	TextArea area = new TextArea();
	EWrapper currentEdit=new EWrapper();
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	Table<EWrapper> externalWrappers;
	private TextBox siteBox;
	private ListBox queue = new ListBox();
	private SimpleLabel wrapperName;
	private CommunicationServiceAsync communicationService= (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private HashMap<String,Integer> map = new HashMap<String, Integer>();

	public ExternalWrapperTab() {
		super("External Wrtappers");
		area.setSize("99%", "100%");
		area.setStyleName("eta-input2");
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				area.setHeight(event.getHeight() - area.getAbsoluteTop() - 26 + "px");
			}
		});
		Grid editor = new Grid(3,2);
		editor.setWidth("100%");
		editor.setStyleName("request-grid");
		editor.setText(0, 0, "Site:");
		editor.setText(1, 0, "Wrapper:");
		editor.setText(2, 0, "Queue:");
		
		siteBox = new TextBox();
		wrapperName=new SimpleLabel("Select a wrapper ");
		editor.setWidget(0, 1, siteBox);
		HorizontalPanel temp = new HorizontalPanel();
		temp.add(wrapperName);
		temp.add(new SimpleButton("Select").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new WrapperSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						wrapperService.getWrapperFromId(Integer.parseInt(items[0]), new MyAsyncCallback<Wrapper>() {
							@Override
							public void success(Wrapper result) {
								currentEdit.setWrapper(result.getName());
								currentEdit.setWrapperId(result.getId());
								siteBox.setText(result.getName());
							}
						});
					}
				});
			}
		}));
		editor.setWidget(1, 1, temp);
		editor.setWidget(2, 1, queue);
		// Add the components to a panel
		FlowPanel grid = new FlowPanel();
		grid.add(editor);
		grid.add(area);
		grid.setHeight("100%");

		HorizontalPanel pane = new HorizontalPanel(){
			@Override
			protected void onLoad() {
				super.onLoad();
				area.setHeight(Window.getClientHeight() - area.getAbsoluteTop() - 26 + "px");
			}
		};
		setPane(pane);
		Column<EWrapper> remove = new Column<EWrapper>("") {
			public Object getValue(EWrapper record) {
				Image remove = new Image(Resources.INSTANCE.remove().getSafeUri().asString());
				remove.setSize("16px", "16px");
				return remove;
			};
			@Override
			public String getWidth() {
				return "20px";
			}
		};
		Column<EWrapper> site = new Column<EWrapper>("Site") {
			public Object getValue(EWrapper record) {
				return record.getSite();
			};
		};
		Column<EWrapper> wrapName = new Column<EWrapper>("Wrapper") {
			public Object getValue(EWrapper record) {
				return record.getWrapper();
			};
			@Override
			public String getWidth() {
				return "100px";
			}
		};
		communicationService.getQueues( new MyAsyncCallback<String[]>() {
			@Override
			public void success(String[] result) {
				queue.addItem("");
				int i=1;
				for (String thread : result) {
					queue.addItem(thread);
					map.put(thread, i++);
				}
			}
		});
		externalWrappers = new Table<EWrapper>(true, remove,wrapName,site);
		externalWrappers.setWidth("400px");
		pane.add(externalWrappers);
		pane.add(new Filler(20));
		pane.add(grid);
		grid.setWidth("100%");
		grid.setHeight("100%");
		pane.setCellWidth(externalWrappers, "400px");
		bar.add(new Button("Add new external wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				siteBox.setValue("");
				queue.setSelectedIndex(0);
				currentEdit=new EWrapper();
				wrapperName.setText("Select a wrapper ");
				area.setValue("");
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("Save").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (currentEdit != null) {
					currentEdit.setSite(siteBox.getValue());
					currentEdit.setQueue(queue.getValue(queue.getSelectedIndex()));
					if(currentEdit.getId()==0)
						currentEdit.setKey(generateKey());
					wrapperService.saveExternalWrapper(currentEdit, new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
							currentEdit.setId(result);
							externalWrappers.onAddition(currentEdit);
						}
					});
				}
			}
		}));
		bar.add(new Seprator());
		wrapperService.getExternalWrappers( new MyAsyncCallback<Vector<EWrapper>>() {
			@Override
			public void success(Vector<EWrapper> result) {
				externalWrappers.setData(result);
			}
		});

		externalWrappers.setRowClickHandler(new RowClickHandler<EWrapper>() {
			public void rowClicked(EWrapper record, int col, int row) {
				if(col!=0)
					loadExternalWrapper(record);
				else{
					externalWrappers.onRemoval(record);
					wrapperService.removeExternalWrapper(record.getId(), new VoidAsyncCallback());
					siteBox.setValue("");
					queue.setSelectedIndex(0);
					currentEdit=new EWrapper();
					wrapperName.setText("Select a wrapper ");
					area.setValue("");
				}
			}
		});
	}
	
	
	public void loadExternalWrapper(EWrapper wrapper){
		currentEdit=wrapper;
		String html ="To use this wrapper externally  on site: "+currentEdit.getSite()+" just copy and paste the code below into your html page.";
    String code = "<script type=\"text/javascript\" src=\""+GWT.getHostPageBaseURL()+"externalwrapper.js\"></script>\n<script type=\"text/javascript\">\nvar wrapper=new ExternalWrapper('"+currentEdit.getKey()+"','"+GWT.getHostPageBaseURL()+"');\nfunction setupWrapper(){\n\\t//replace 'id' with the id of your container you want the wrapper form to go into\n\twrapper.setup('id');\n}\nwindow.onload=setupWrapper;\n</script>";
    area.setValue(html+"\n\n\n"+code);
    siteBox.setValue(currentEdit.getSite());
    wrapperName.setText(currentEdit.getWrapper());
    queue.setSelectedIndex(map.get(currentEdit.getQueue()));
	}

	@Override
	public String getId() {
		return "ew";
	}

	@Override
	public Widget getBar() {
		return bar;
	}
	 public String generateKey() {
     String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
     int string_length = 30;
     String randomstring = "";
     for (int i = 0; i < string_length; i++) {
             int rnum = (int) Math.floor(Math.random() * chars.length());
             randomstring += chars.substring(rnum, rnum + 1);
     }
     return randomstring;
}
}
