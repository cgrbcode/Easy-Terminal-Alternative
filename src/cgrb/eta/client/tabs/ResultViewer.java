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
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.TextReader;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.pipeline.PipelineMonitor;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.wrapperrunner.Inputs;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.JobNote;
import cgrb.eta.shared.wrapper.Wrapper;

public class ResultViewer extends ETATab {

	private HorizontalPanel bar;
	private FlowPanel pane;
	private VerticalPanel overView;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private VerticalPanel notes;
	private VerticalPanel responseStack;
	private VerticalPanel outputs;
	private SimpleButton selectedButton;
	private Job job;
	private int jobId = 0;

	public ResultViewer(int jobId) {
		super("Job Viewer");
		this.jobId=jobId;
		VerticalPanel left = new VerticalPanel();
		bar = new HorizontalPanel();
		bar.add(new Button("Share"));
		bar.add(new Seprator());
		bar.add(new Button("Rerun"));
		bar.add(new Seprator());
		bar.add(new Button("Delete"));
		bar.add(new Seprator());
		HorizontalPanel master = new HorizontalPanel() {
			@Override
			protected void onAttach() {
				super.onAttach();
				getElement().getStyle().setPadding(0, Unit.PX);
			}
		};

		left.setWidth("200px");
		VerticalPanel leftLeft = new VerticalPanel();
		leftLeft.setStyleName("job-left");
		final SimpleButton overViewB = new SimpleButton("Overview");
		selectedButton = overViewB;
		overViewB.setStyleDependentName("-sel", true);
		overViewB.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				pane.clear();
				pane.add(overView);
				selectedButton.setStyleDependentName("-sel", false);
				overViewB.setStyleDependentName("-sel", true);
				selectedButton = overViewB;
			}
		});
		left.add(overViewB);
		final SimpleButton inputsB = new SimpleButton("Inputs");
		inputsB.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (selectedButton.equals(inputsB))
					return;
				selectedButton.setStyleDependentName("-sel", false);
				inputsB.setStyleDependentName("-sel", true);
				selectedButton = inputsB;

				showInputs();
			}
		});
		left.add(inputsB);

		outputs = new VerticalPanel();
		outputs.setWidth("100%");
		left.add(outputs);
		leftLeft.add(left);
		master.add(leftLeft);
		master.setCellWidth(leftLeft, "200px");
		leftLeft.setHeight("100%");
		overView = new VerticalPanel();
		pane = new FlowPanel();
		pane.add(overView);
		master.add(pane);
		pane.setHeight("100%");
		pane.setStyleName("job-viewer");

		setPane(master);

	}

	private void showNext() {
		sqlService.getNextWrappers(job.getWrapper(), new MyAsyncCallback<Vector<Wrapper>>() {
			@Override
			public void success(Vector<Wrapper> result) {
				FlowPanel nextOnes = new FlowPanel();
				if (result.size() == 0) {
					nextOnes.add(new SimpleLabel("Sorry I couldn't find any wrappers that have inputs that match the outputs of this result"));
				} else {
					nextOnes.add(new SimpleLabel("I have found the following wrappers that can use the outputs of this result"));
					for (final Wrapper wrap : result) {
						HorizontalPanel wrapper = new HorizontalPanel();
						wrapper.setStyleName("next-wrapper");
						wrapper.add(new SimpleLabel(wrap.getName()));
						wrapper.add(new SimpleButton("Run").addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								WrapperRunner runner = new WrapperRunner(wrap);
								runner.matchInputs(job);
								ETA.getInstance().addTab(runner);
							}
						}));
						nextOnes.add(wrapper);
					}
				}
				pane.clear();
				pane.add(nextOnes);
			}
		});

	}

	public void loadJob(final Job job) {
		this.job = job;
		HorizontalPanel title = new HorizontalPanel();
		title.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		Image img = new Image(Resources.INSTANCE.jobBig().getSafeUri().asString());
		img.setHeight("70px");
		img.setWidth("70px");
		title.add(img);
		title.add(new SimpleLabel("Name:  " + job.getName()).setFontSize(18));
		overView.clear();
		HorizontalPanel temp = new HorizontalPanel();
		temp.add(title);
		String status = job.getStatus();
		Grid infoGrid = new Grid(1, 2);
		infoGrid.setText(0, 0, "Submitted on:");
		infoGrid.setText(0, 1, job.getSubmitTime());
		if (status.equals("Running")) {
			infoGrid.resizeRows(3);
			infoGrid.setText(1, 0, "Running since:");
			infoGrid.setText(1, 1, job.getRunTime());
			infoGrid.setText(2, 0, "Running on:");
			infoGrid.setText(2, 1, job.getMachine());
		} else if (status.equals("Finished")) {
			infoGrid.resizeRows(4);
			infoGrid.setText(1, 0, "Started on:");
			infoGrid.setText(1, 1, job.getRunTime());
			infoGrid.setText(2, 0, "Ran on:");
			infoGrid.setText(2, 1, job.getMachine());
			infoGrid.setText(3, 0, "Finished on:");
			infoGrid.setText(3, 1, job.getFinishedTime());
		} else {
			infoGrid.resizeRows(3);
			infoGrid.setText(1, 0, job.getStatus() + " since:");
			infoGrid.setText(1, 1, job.getDate().toString());
		}
		temp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		temp.add(infoGrid);
		infoGrid.setStyleName("request-grid");
		temp.setWidth("100%");
		temp.add(new Filler(30));
		overView.setWidth("100%");
		overView.add(temp);
		if (job.getWrapper() != null) {
			final SimpleButton nextB = new SimpleButton("What can I do next?");
			nextB.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (selectedButton.equals(nextB))
						return;
					selectedButton.setStyleDependentName("-sel", false);
					nextB.setStyleDependentName("-sel", true);
					selectedButton = nextB;
					showNext();
				}
			});
			overView.add(((SimpleLabel)new SimpleLabel("").setText("Command ran: " + job.getWrapper().getCMD())).setSelectable());
			overView.add(((SimpleLabel)new SimpleLabel("").setText("Working Folder: " + job.getWorkingDir())).setSelectable());
			outputs.clear();
			Vector<File> outFiles = job.getOutputFiles();
			for (final File file : outFiles) {
				final SimpleButton tempB = new SimpleButton(file.getName());
				tempB.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						if (selectedButton.equals(tempB))
							return;
						selectedButton.setStyleDependentName("-sel", false);
						tempB.setStyleDependentName("-sel", true);
						selectedButton = tempB;
						showFile(file);
					}
				});

				outputs.add(tempB);
			}
		} else {
			overView.add(new SimpleLabel("Working Folder: " + job.getWorkingDir()));
			//add a overview panel for pipelines
			final SimpleButton pipelineView = new SimpleButton("Pipeline View");
			outputs.add(pipelineView.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if(selectedButton.equals(pipelineView))
						return;
					selectedButton.setStyleDependentName("-sel", false);
					pipelineView.setStyleDependentName("-sel", true);
					selectedButton = pipelineView;
					showPipelineView();
				}
			}));
		}
		overView.add(new SimpleLabel("Notes:").setFontSize(22));
		notes = new VerticalPanel();

		notes.getElement().getStyle().setPaddingLeft(10, Unit.PX);
		responseStack = new VerticalPanel();
		responseStack.setStyleName("request-resp");
		SimpleLabel addCommentLabel = new SimpleLabel("Add Note");
		final TextArea responseBox = new TextArea();
		responseBox.setWidth("484px");
		responseStack.setWidth("484px");

		responseBox.setHeight("100px");
		responseBox.setStylePrimaryName("eta-textarea");
		SimpleButton send = new SimpleButton("Add");
		send.setHeight("10px");
		responseStack.add(addCommentLabel);
		responseStack.add(responseBox);
		responseStack.add(send);
		responseStack.setHeight(20 + "px");
		send.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				sqlService.addNote(job.getId(), responseBox.getText(), new MyAsyncCallback<JobNote>() {
					@Override
					public void success(JobNote result) {
						addNote(result);
						responseBox.setText("");
					}
				});
			}
		});
		notes.add(responseStack);
		Vector<JobNote> jobNotes = job.getNotes();
		for (int i = 0; i < jobNotes.size(); i++) {
			addNote(jobNotes.get(i));
		}
		overView.add(notes);

	}
	FlowPanel pipelineViewPanel=null;
	protected void showPipelineView() {
		if(pipelineViewPanel==null){
			pipelineViewPanel = new FlowPanel();
			pipelineViewPanel.setStyleName("pipeline-viewer");
			pipelineViewPanel.add(new PipelineMonitor(job));
			
		}else{
			
		}
		pane.clear();
		pane.add(pipelineViewPanel);
	}

	private void showFile(File file) {
		pane.clear();
		pane.add(new TextReader(file));
	}

	private void showInputs() {
		VerticalPanel pane = new VerticalPanel();
		HorizontalPanel top = new HorizontalPanel();
		top.setStyleName("wr-top");
		SimpleLabel nameHeader = new SimpleLabel("Name");
		SimpleLabel descHeader = new SimpleLabel("Description");
		SimpleLabel value = new SimpleLabel("Value");
		FlowPanel middle = new FlowPanel();
		top.add(nameHeader);
		top.add(descHeader);
		top.add(value);
		top.setWidth("100%");
		top.setCellWidth(nameHeader, "150px");
		top.setCellWidth(value, "250px");
		SimpleLabel commandRun=(SimpleLabel) new SimpleLabel("").setSelectable();

		Inputs inputs;
		if(job.getWrapper()!=null){
			commandRun.setText(job.getWrapper().getCMD());
			inputs= new Inputs(job.getWrapper(), 12);
		}else{
			inputs=new Inputs(job.getPipelineObject());
		}

		middle.add(inputs);
		middle.setStyleName("wr-middle");
		middle.setHeight("100%");
		middle.getElement().getStyle().setOverflowY(Overflow.AUTO);
		top.setHeight("20px");
		pane.add(top);
		pane.setCellHeight(top, "20px");
		pane.add(middle);
		pane.setCellHeight(middle, "100%");
		HorizontalPanel bottom = new HorizontalPanel();
		SimpleLabel temp = new SimpleLabel("Command being ran:");
		bottom.add(temp);
		if(commandRun!=null)
		bottom.add(commandRun);
		bottom.setWidth("100%");
		pane.add(bottom);
		bottom.setCellWidth(temp, "120px");
		bottom.setStyleName("wr-bottom");
		pane.setCellHeight(bottom, "30px");
		pane.setHeight("100%");
		pane.setWidth("100%");
		this.pane.clear();
		this.pane.add(pane);
	}

	private int noteNumber = 1;

	private void addNote(JobNote note) {
		final VerticalPanel tempResp = new VerticalPanel();
		tempResp.setStyleName("request-resp");
		SimpleLabel tempLabel = new SimpleLabel("Note " + (noteNumber++) + " by " + note.getUser() + " on " + note.getDate());

		HorizontalPanel header = new HorizontalPanel();
		header.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		header.add(tempLabel);
		header.setStyleName("req-resp-head");
		Label tempText = new Label(note.getNote());
		tempText.setStyleName("resp-text");
		tempResp.add(header);
		tempResp.add(tempText);
		final JobNote response = note;
		notes.remove(responseStack);
		notes.add(tempResp);
		notes.add(responseStack);
		if (ETA.getInstance().getUser().getId() == response.getUserId()) {
			SimpleButton delete = new SimpleButton("Delete Note").addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					notes.remove(tempResp);
					sqlService.removeJobNote(response.getId(), new MyAsyncCallback<Void>() {
						@Override
						public void success(Void result) {
						}
					});
				}
			});
			header.add(delete);
			header.setCellHorizontalAlignment(delete, HasHorizontalAlignment.ALIGN_RIGHT);
		} else {
			tempLabel.setWidth("100%");
		}
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	@Override
	public String getId() {
		return "rv#" + jobId;
	}

	/**
	 * @param job2
	 */
	public void loadJob(int job) {
		jobId = job;
		sqlService.getJob(job, new MyAsyncCallback<Job>() {
			@Override
			public void success(Job result) {
				loadJob(result);
			}
		});
	}

}
