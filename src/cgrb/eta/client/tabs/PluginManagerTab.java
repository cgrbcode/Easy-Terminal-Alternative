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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.EventListener;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ProgressBar;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.UploadButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.UploadEvent;
import cgrb.eta.shared.etatype.Plugin;

public class PluginManagerTab extends ETATab {

	private Table<Plugin> pluginTable;
	private FlexTable table;
	private Grid pluginInfo;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);

	public PluginManagerTab() {
		super("Plugins");
		HorizontalPanel pane = new HorizontalPanel();

		pluginInfo=new Grid(9,2);
		pluginInfo.setText(0, 0, "Name:");
		pluginInfo.setText(1, 0, "Version:");
		pluginInfo.setText(2, 0, "Type:");
		pluginInfo.setText(3, 0, "File Types:");
		pluginInfo.setText(4, 0, "Author:");
		pluginInfo.setText(5, 0, "Email:");
		pluginInfo.setText(6, 0, "Permissions:");
		pluginInfo.setText(7, 0, "Icon:");
		pluginInfo.setText(8, 0, "Index Page:");
		
		
		Column<Plugin> nameCol = new Column<Plugin>("Name") {
			@Override
			public Object getValue(Plugin record) {
				return record.getName();
			}

			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<Plugin> authCol = new Column<Plugin>("Author") {
			@Override
			public Object getValue(Plugin record) {
				return record.getAuthor();
			}

			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<Plugin> versionCol = new Column<Plugin>("Version") {
			@Override
			public Object getValue(Plugin record) {
				return record.getVersion();
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};

		pluginTable = new Table<Plugin>(true, nameCol, authCol, versionCol);
		pluginTable.addListener(Plugin.class);
		pluginTable.setEmptyString("Click the add new plugin button to add one!");
		sqlService.getPlugins(new MyAsyncCallback<Vector<Plugin>>() {
			@Override
			public void success(Vector<Plugin> result) {
				pluginTable.setData(result);
			}
		});
		pluginTable.addAction(new ImgButton(Resources.INSTANCE.resultsSmall(), 20, "View plugin info").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Plugin result = pluginTable.getSelection().get(0);
				pluginInfo.setText(0, 1, result.getName());
				pluginInfo.setText(1, 1, result.getVersion());
				pluginInfo.setText(2, 1, result.getType());
				pluginInfo.setText(3, 1, result.getFileTypes().toString());
				pluginInfo.setText(4, 1, result.getAuthor());
				pluginInfo.setText(5, 1, result.getEmail());
				pluginInfo.setText(6, 1, result.getPermissions().toString());
				pluginInfo.setText(7, 1, result.getIcon());
				pluginInfo.setText(8, 1, result.getIndex());
				table.removeAllRows();
				table.setWidget(0, 0, pluginInfo);
			}
		}), Table.SINGLE_SELECT);
		pluginTable.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Delete Plugin").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Delete?", "Are you sure you want to remove this plugin?", new ValueListener<Boolean>() {
					@Override
					public void returned(Boolean ret) {
						if(ret){
							sqlService.removePlugin(pluginTable.getSelection().get(0).getId(), new MyAsyncCallback<Void>() {
								@Override
								public void success(Void result) {
								}
							});
						}
					}
				});
			}
		}), Table.SINGLE_SELECT);
		pane.add(pluginTable);
		pane.setCellWidth(pluginTable, "500px");
		table = new FlexTable();

		pane.add(table);

		setPane(pane);
	}

	@Override
	public Widget getBar() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.add(new Button("Add New").setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				addNew();
			}
		}));
		bar.add(new Seprator());
		return bar;
	}

	private void addNew() {
		final UploadButton upload = new UploadButton("/tmp");
		VerticalPanel newPluginPane = new VerticalPanel();
		newPluginPane.add(upload);
		table.setWidget(0, 0, upload);
		upload.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ProgressBar progress = new ProgressBar(upload.getFile());
				progress.setHeight("20px");
				table.setWidget(0, 1, progress);
				EventListener.getInstance().addListener(ETAEvent.UPLOAD, new EventOccuredListener() {
					public void eventOccured(ETAEvent event, int user) {
						UploadEvent uEvent = (UploadEvent) event.getSource();
						if (uEvent.getFile().equals(upload.getFileId())) {
							progress.setPercent(uEvent.getPercent());
							if (uEvent.getPercent() == 100) {
								table.removeCell(0, 1);
								//upload has finished now check for errors and if all goes well display the manifest 
								sqlService.checkForErrors(upload.getFileId(), new MyAsyncCallback<String>() {
									@Override
									public void success(String result) {
										if(result.equals("")){
											//there are no errors we can go on to displaying the plugin settings so the user can accept them
											sqlService.getTempPlugin(upload.getFileId(), new MyAsyncCallback<Plugin>() {
												@Override
												public void success(Plugin result) {
													pluginInfo.setText(0, 1, result.getName());
													pluginInfo.setText(1, 1, result.getVersion());
													pluginInfo.setText(2, 1, result.getType());
													pluginInfo.setText(3, 1, result.getFileTypes().toString());
													pluginInfo.setText(4, 1, result.getAuthor());
													pluginInfo.setText(5, 1, result.getEmail());
													pluginInfo.setText(6, 1, result.getPermissions().toString());
													pluginInfo.setText(7, 1, result.getIcon());
													pluginInfo.setText(8, 1, result.getIndex());
													table.setWidget(1, 0, pluginInfo);
													
													SimpleButton add = new SimpleButton("Add plugin").addClickHandler(new ClickHandler() {
														@Override
														public void onClick(ClickEvent event) {
															sqlService.saveTempPlugin(upload.getFileId(), new MyAsyncCallback<Plugin>() {
																@Override
																public void success(Plugin result) {
																	if(result!=null)
																		SC.alert("saved", "This plugin has been installed");
																	else
																		SC.alert("Error", "Sorry there was an unexpected error while installing this plugin");
																}
															});
														}
													});
													table.setWidget(2, 0, add);
												}
											});
										}else{
											//there was an error :(
											SC.alert("Sorry error :'(", result);
										}
									}
								});
							}
						}
					}
				});
			}
		});
	}

	@Override
	public String getId() {
		return "pm";
	}

}
