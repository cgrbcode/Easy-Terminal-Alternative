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
package cgrb.eta.client.tools;

import java.util.Date;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.wrapper.Wrapper;

public class GeneCounter extends ETATab{
	
	
	private Table<GeneCounterFile> table;
	private CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private TextBox runName;
	private String folder="";
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);

	public GeneCounter(){
		super("Gene Counter");
		VerticalPanel pane = new VerticalPanel();
		HorizontalPanel topPanel = new HorizontalPanel();
		SimpleButton addFolder = new SimpleButton("Add Folder").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new FileSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						String selectedFolder = items[0];
						folder=selectedFolder;
						communicationService.getFiles(selectedFolder,"", new MyAsyncCallback<Vector<File>>() {
							@Override
							public void success(Vector<File> result) {
								Vector<GeneCounterFile> files = new Vector<GeneCounter.GeneCounterFile>();
								for(File file:result){
									if(file.getType().startsWith("File"))
									files.add(new GeneCounterFile(file));
								}
								table.setData(files);
							}
						});
					}
				}, FileBrowser.FOLDER_SELECT);
			}
		});
		topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		topPanel.add(addFolder);
		SimpleLabel label = new SimpleLabel("Run Name:");
		topPanel.add(label);
		topPanel.setCellHorizontalAlignment(label, HasHorizontalAlignment.ALIGN_RIGHT);
		runName = new TextBox();
		runName.setStyleName("eta-input2");
		topPanel.add(runName);
		topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		topPanel.add(new SimpleButton("Submit").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				submit();
			}
		}));
		
		Column<GeneCounterFile> fileName = new Column<GeneCounter.GeneCounterFile>("File") {
			@Override
			public Object getValue(GeneCounterFile record) {
				return record.getFilename();
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		
		Column<GeneCounterFile> treatment = new Column<GeneCounter.GeneCounterFile>("Treatment") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				final TextBox input = new TextBox();
				input.setStyleName("eta-input2");
				input.setText(record.getTreatment());
				input.addChangeHandler(new ChangeHandler() {
					public void onChange(ChangeEvent event) {
						record.setTreatment(input.getValue());
					}
				});
				return input;
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		Column<GeneCounterFile> lab = new Column<GeneCounter.GeneCounterFile>("Lab") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				final TextBox input = new TextBox();
				input.setStyleName("eta-input2");
				input.setText(record.getLab());
				input.addChangeHandler(new ChangeHandler() {
					public void onChange(ChangeEvent event) {
						record.setLab(input.getValue());
					}
				});
				return input;
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		Column<GeneCounterFile> ecoType = new Column<GeneCounter.GeneCounterFile>("Eco Type") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				final TextBox input = new TextBox();
				input.setStyleName("eta-input2");
				input.setText(record.getEcoType());
				input.addChangeHandler(new ChangeHandler() {
					public void onChange(ChangeEvent event) {
						record.setEcoType(input.getValue());
					}
				});
				return input;
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		
		Column<GeneCounterFile> timePoint = new Column<GeneCounter.GeneCounterFile>("Timepoint") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				final TextBox input = new TextBox();
				input.setStyleName("eta-input2");
				input.setText(record.getTimePoint());
				input.addChangeHandler(new ChangeHandler() {
					public void onChange(ChangeEvent event) {
						record.setTimePoint(input.getValue());
					}
				});
				return input;
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		
		Column<GeneCounterFile> lane = new Column<GeneCounter.GeneCounterFile>("Lane") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				final TextBox input = new TextBox();
				input.setStyleName("eta-input2");
				input.setText(record.getLane());
				input.addChangeHandler(new ChangeHandler() {
					public void onChange(ChangeEvent event) {
						record.setLane(input.getValue());
					}
				});
				return input;
			}
			@Override
			public String getWidth() {
				return "80px";
			}
		};
		Column<GeneCounterFile> remove = new Column<GeneCounter.GeneCounterFile>("") {
			@Override
			public Object getValue(final GeneCounterFile record) {
				Image removeImg = new Image("images/remove.png");
				removeImg.setSize("16px", "16px");
				removeImg.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						table.onRemoval(record);
					}
				});
				return removeImg;
			}
			@Override
			public String getWidth() {
				return "16px";
			}
		};
		
		table=new Table<GeneCounterFile> (true,remove,fileName,treatment,lab,ecoType,timePoint,lane);
		pane.add(topPanel);
		topPanel.setWidth("100%");
		pane.setWidth("100%");
		pane.setCellHeight(topPanel, "30px");
		table.setWidth("100%");
		pane.add(table);
		setPane(pane);
	}
	
	int geneId=0;
	private class GeneCounterFile extends ETAType{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5544542253247991566L;
		private File file;
		private String treatment="";
		private String lab="";
		private String ecoType="";
		private String timePoint="";
		private String lane="";
		
		public GeneCounterFile(File f){
			file=f;
			id=geneId++;
		}
		public String getFilename(){
			return file.getName();
		}

		public String getTreatment() {
			return treatment;
		}

		public String getLab() {
			return lab;
		}

		public String getEcoType() {
			return ecoType;
		}

		public String getTimePoint() {
			return timePoint;
		}

		public String getLane() {
			return lane;
		}
		public void setTreatment(String treatment) {
			this.treatment = treatment;
		}
		public void setLab(String lab) {
			this.lab = lab;
		}
		public void setEcoType(String ecoType) {
			this.ecoType = ecoType;
		}
		public void setTimePoint(String timePoint) {
			this.timePoint = timePoint;
		}
		public void setLane(String lane) {
			this.lane = lane;
		}
		
	}

	@Override
	public String getId() {
		return "gc";
	}

	private void submit(){
		Vector<GeneCounterFile> files = table.getData();
		if(runName.getValue().equals(""))
		{
			SC.alert("Error :(", "You must fill in the run name field in order to submit");
			return;
		}
		String fileContent="date="+DateTimeFormat.getFormat("yyyy-dd-MM").format(new Date())+"\tname="+runName.getValue()+"\n";
		for(GeneCounterFile file:files){
			if(file.getEcoType().equals("")||file.getLab().equals("")||file.getLane().equals("")||file.getTimePoint().equals("")||file.getTreatment().equals("")){
				SC.alert("Error :(", "Sorry you must fill in all the boxes to submit this");
				return;
			}
			fileContent+="treatment="+file.getTreatment()+"\tlab="+file.getLab()+"\tfile="+file.getFilename()+"\tecotype="+file.getEcoType()+"\ttimepoint="+file.getTimePoint()+"\tlane="+file.getLane()+"\n";
		}
		communicationService.writeFile(folder+"/"+DateTimeFormat.getFormat("M-d-yyyy:H:mm:ss").format(new Date())+"run-info.txt", fileContent, new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				if(result.equals("")){
					wrapperService.getWrapperFromCMD("processSeqs.pl -d".split(" "), new MyAsyncCallback<Wrapper>() {
						@Override
						public void success(Wrapper result) {
							
						}
					});
				}else{
					SC.alert("Error", "Sorry for some reason I couldn't write the file " );
				}
			}
		});
	}
}
