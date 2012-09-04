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

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.FileButton;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.StarButton;
import cgrb.eta.client.button.UploadButton;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class NewRequestTab extends ETATab {
	int files = 0;
	private VerticalPanel filePanel = new VerticalPanel();
	private HorizontalPanel pane;
	private Vector<String> testFiles = new Vector<String>();
	private SimpleLabel fileLabel = new SimpleLabel("Add test data to help with your request.");
	private UploadButton uploadButton;
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	ListBox typeBox = new ListBox();
	TextArea description = new TextArea();
	TextBox summary = new TextBox();
	VerticalPanel panee = new VerticalPanel();
	private StarButton star = new StarButton();

	public NewRequestTab() {
		super("New Request");
		pane = new HorizontalPanel();
		VerticalPanel leftSide = new VerticalPanel();
		// typeSelect.setValueMap("New Wrapper","Wrapper fix","Bug","New Feature","Other");
		panee.addStyleName("new-request");
		typeBox.setStyleName("eta-selectbox");
		typeBox.addItem("New Wrapper");
		typeBox.addItem("Wrapper fix");
		typeBox.addItem("Bug");
		typeBox.addItem("New Software");
		typeBox.addItem("New Feature");
		typeBox.addItem("Other");
		typeBox.setWidth("100%");
		summary.setStylePrimaryName("eta-input");
		summary.setWidth("100%");

		typeBox.setWidth("400px");
		summary.setWidth("400px");
		description.setWidth("400px");
		description.setHeight("200px");
		description.setStylePrimaryName("eta-textarea");
		//description.setWidth("100%");

		Grid formGrid = new Grid(7, 2);
		formGrid.setStyleName("request-grid");
		formGrid.setWidth("500px");
		formGrid.setText(1, 0, "Type:");
		formGrid.setWidget(1, 1, typeBox);
		formGrid.setText(2, 0, "Summary:");
		formGrid.setWidget(2, 1, summary);
		formGrid.setText(3, 0, "Description:");
		formGrid.setWidget(3, 1, description);
		leftSide.add(formGrid);
		formGrid.setWidget(0, 0, star);
		formGrid.setWidget(0, 1, new SimpleLabel("Unstar this request if you don't want to get emails from this request"));
		SimpleLabel testLabel = new SimpleLabel("Test data:");
		uploadButton = new UploadButton("$localpath/request-data");
		uploadButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				addClientFile();
			}
		});
		SimpleButton browseButton = new SimpleButton("Browse").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addLocalFile();
			}
		});

		HorizontalPanel testDataStack = new HorizontalPanel();
		testDataStack.add(testLabel);
		testDataStack.add(new Filler(10));
		testDataStack.add(uploadButton);
		//uploadButton.setWidth("10px");
		testDataStack.add(new Filler(10));
		testDataStack.add(browseButton);
		HorizontalPanel submitStack = new HorizontalPanel();
		SimpleButton submitButton = new SimpleButton("Submit request").addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				submit();
			}
		});
		SimpleButton disgardButton = new SimpleButton("Discard");
		disgardButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
//				ETA.getInstance().removeTab(NewRequestTab.this);
			}
		});

		submitStack.add(submitButton);
		submitStack.add(new Filler(10));
		submitStack.add(disgardButton);

		submitStack.setHeight(20+"px");
		formGrid.setWidget(4, 1, testDataStack);
		formGrid.setWidget(5, 1, filePanel);
		formGrid.setWidget(6, 1, submitStack);
		pane.add(leftSide);

		HTML hint = new HTML("<p><b>Tip:</b><br>Search though the requests to ensure that this is new, then star the request to get notifications.<br><br><b>Remember:</b><br>This information is viewable by anyone. Don't put any personal information like passwords.</p>");
		
		hint.setWidth("100px");
		hint.setStyleName("request-tip");
		pane.add(hint);
		panee.add(pane);
		pane.setHeight("100%");
		filePanel.addStyleName("request-files");
		filePanel.setWidth("400px");
		filePanel.add(fileLabel);

		addFileDropHandeler(filePanel.getElement());
		setPane(panee);
	}

	public static native void addFileDropHandeler(JavaScriptObject dropbox) /*-{
		function dragEnter(evt) {
			evt.stopPropagation();
			evt.preventDefault();
		}
		function dragExit(evt) {
			evt.stopPropagation();
			evt.preventDefault();
		}
		function dragOver(evt) {
			evt.stopPropagation();
			evt.preventDefault();
		}
		function drop(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			var files = evt.dataTransfer.files;
			var count = files.length;

			// Only call the handler if 1 or more files was dropped.
			if (count > 0)
				alert(files[0].name);
		}
		dropbox.addEventListener("dragenter", dragEnter, false);
		dropbox.addEventListener("dragexit", dragExit, false);
		dropbox.addEventListener("dragover", dragOver, false);
		dropbox.addEventListener("drop", drop, false);

	}-*/;

	public void addLocalFile() {
		new FileSelector(new ItemSelector() {

			public void itemSelected(final String[] items) {
				if (items != null) {
					if (testFiles.size() == 0) {
						filePanel.clear();
					}
					testFiles.add(items[0]);
					final FileButton adding = new FileButton(items[0]);
					filePanel.add(adding);
					adding.setClickHandler(new ClickHandler() {

						public void onClick(ClickEvent event) {
							filePanel.remove(adding);
							testFiles.remove(items[0]);
							if (testFiles.size() == 0)
								filePanel.add(fileLabel);
						}
					});
				}
			}
		}, FileBrowser.FILE_SELECT);
	}

	protected void addClientFile() {
		if (uploadButton.getFile() != null && !uploadButton.getFile().equals("")) {
			if (testFiles.size() == 0) {
				filePanel.clear();
			}
			final FileButton adding = new FileButton(uploadButton.getFile());

			adding.setFileId(uploadButton.getFileId());
			filePanel.add(adding);

			final String file = "$localpath/request-data/" + uploadButton.getFile();
			testFiles.add(file);
			adding.setClickHandler(new ClickHandler() {

				public void onClick(ClickEvent event) {
					filePanel.remove(adding);
					testFiles.remove(file);
					if (testFiles.size() == 0)
						filePanel.add(fileLabel);
				}
			});
			panee.add(pane);
		}
	}

	private void submit() {
		if (summary.getValue() != null && !summary.getValue().equals("") && description.getValue() != null && !description.getValue().equals("")) {
			communicationService.saveRequest(typeBox.getValue(typeBox.getSelectedIndex()), summary.getValue(), description.getValue(), new AsyncCallback<Integer>() {
				public void onSuccess(final Integer result) {
					// now associate the files with this request
					for (String file : testFiles) {
						communicationService.saveRequestFile(result, file, new AsyncCallback<Void>() {
							public void onFailure(Throwable caught) {
							}

							public void onSuccess(Void result2) {

							}
						});
					}
					communicationService.starRequest(star.isStared(), result, new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(Void result) {

						}
					});
					ETA.getInstance().removeTab(NewRequestTab.this);
					ETA.getInstance().addTab(new RequestItemTab(result));
				}

				public void onFailure(Throwable caught) {

				}
			});
			// now close this panel and bring up the request pane for this request
//			ETA.getInstance().removeTab(this);
		}

	}

	@Override
	public Widget getBar() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.add(new Button("List of Requests").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab(new RequestTab());
			}
		}));
		bar.add(new Seprator());
		return bar;
	}

	@Override
	public String getId() {
		return "nr";
	}
}
