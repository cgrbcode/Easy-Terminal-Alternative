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
import cgrb.eta.client.EventListener;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.StarButton;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.RequestEvent;
import cgrb.eta.shared.RequestResponse;
import cgrb.eta.shared.etatype.RequestItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RequestItemTab extends ETATab {
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	int id;
	StarButton star = new StarButton();
	VerticalPanel responseStack = new VerticalPanel();
	VerticalPanel left = new VerticalPanel();
	HorizontalPanel pane = new HorizontalPanel();

	int responseCount = 0;

	public RequestItemTab(int request) {
		super("Request #" + request);
		id = request;
		communicationService.getRequest(request, new MyAsyncCallback<RequestItem>() {
			@Override
			public void success(RequestItem result) {
				setup(result);
			}
		});
		setPane(pane);
	}

	public void setup(final RequestItem request) {
		id = request.getId();
		HorizontalPanel title = new HorizontalPanel();
		title.setStyleName("request-title");
		title.setWidth("100%");
		star.setStared(request.isStarted());
		star.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				communicationService.starRequest(star.isStared(), id, new AsyncCallback<Void>() {
					public void onFailure(Throwable caught) {
					}

					public void onSuccess(Void result) {
					}
				});
			}
		});
		SimpleLabel requestNum = new SimpleLabel("Request #" + request.getId() + "  " + request.getSummary());
		title.add(star);
		title.add(requestNum);
		left.add(title);

		VerticalPanel desc = new VerticalPanel();
		desc.setStyleName("request-resp");
		SimpleLabel descLabel = new SimpleLabel("Created by " + request.getCreator() + " on " + request.getTimestamp());
		desc.add(descLabel);
		HTML descText = new HTML(request.getDescription().replaceAll("\n", "<br>"));
		descText.setStyleName("resp-text");
		desc.add(descText);
		left.add(desc);

		// now add the responses
		final Vector<RequestResponse> responses = request.getResponses();
		for (int i = 0; i < responses.size(); i++) {
			final VerticalPanel tempResp = new VerticalPanel();
			tempResp.setStyleName("request-resp");
			SimpleLabel tempLabel = new SimpleLabel("Response " + (i + 1) + " by " + responses.get(i).getCreator() + " on " + responses.get(i).getTimestamp());

			HorizontalPanel header = new HorizontalPanel();
			header.add(tempLabel);
			header.setStyleName("req-resp-head");
			HTML tempText = new HTML(responses.get(i).getResponse().replaceAll("\n", "<br>"));
			tempText.setStyleName("resp-text");
			tempResp.add(header);
			tempResp.add(tempText);
			final RequestResponse response = responses.get(i);
			left.add(tempResp);
			if (ETA.getInstance().getUser().getPermissionLevel() >= 8) {
				SimpleButton delete = new SimpleButton("Delete response").addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						left.remove(tempResp);
						communicationService.removeRequestResponse(response.getId(), new MyAsyncCallback<Void>() {
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
		responseCount = responses.size();

		// add the create response section
		responseStack.setStyleName("request-resp");
		SimpleLabel addCommentLabel = new SimpleLabel("Add Response");
		final TextArea responseBox = new TextArea();
		responseBox.setWidth("484px");
		responseBox.setHeight("100px");
		responseBox.setStylePrimaryName("eta-textarea");
		SimpleButton send = new SimpleButton("Respond");
		send.setHeight("10px");
		responseStack.add(addCommentLabel);
		responseStack.add(responseBox);
		responseStack.add(send);
		responseStack.setHeight(20 + "px");
		send.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				communicationService.addComment(responseBox.getText(), id, new AsyncCallback<Void>() {
					public void onSuccess(Void result) {
						responseBox.setValue("");
						responseBox.setText("");
					}

					public void onFailure(Throwable caught) {
					}
				});
			}
		});
		if (request.getStatus().equals("Open"))
			left.add(responseStack);
		left.setWidth(500 + "px");
		pane.setWidth("100%");
		pane.add(left);
		pane.setCellWidth(left, "500px");

		EventListener.getInstance().addListener(ETAEvent.REQUEST, new EventOccuredListener() {

			public void eventOccured(ETAEvent event, int user) {
				RequestEvent eventR = (RequestEvent) event.getSource();
				RequestResponse reqResp = eventR.getNewItem();
				if (eventR.getId() == id) {
					// add the new response
					left.remove(responseStack);

					final VerticalPanel tempResp = new VerticalPanel();
					tempResp.setStyleName("request-resp");
					SimpleLabel tempLabel = new SimpleLabel("Response " + ++responseCount + " by " + reqResp.getCreator() + " on " + reqResp.getTimestamp());

					HorizontalPanel header = new HorizontalPanel();
					header.add(tempLabel);
					header.setStyleName("req-resp-head");
					HTML tempText = new HTML(reqResp.getResponse().replaceAll("\n", "<br>"));
					tempText.setStyleName("resp-text");
					tempResp.add(header);
					tempResp.add(tempText);
					final RequestResponse response = reqResp;
					left.add(tempResp);
					if (ETA.getInstance().getUser().getPermissionLevel() >= 8) {
						SimpleButton delete = new SimpleButton("Delete response").addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								left.remove(tempResp);
								communicationService.removeRequestResponse(response.getId(), new AsyncCallback<Void>() {
									public void onFailure(Throwable caught) {
									}

									public void onSuccess(Void result) {
									}
								});
							}
						});
						header.add(delete);
						header.setCellHorizontalAlignment(delete, HasHorizontalAlignment.ALIGN_RIGHT);
					} else {
						tempLabel.setWidth("100%");
					}
					if (request.getStatus().equals("Open"))
						left.add(responseStack);

				}
			}
		});

		// make the right side with the type and status and the test data
		VerticalPanel rightStuff = new VerticalPanel();
		rightStuff.setStyleName("right-side");
		VerticalPanel statusStack = new VerticalPanel();
		statusStack.setStyleName("request-rightStack");
		statusStack.add(new SimpleLabel("Type: " + request.getType()));
		statusStack.add(new SimpleLabel("Status: " + request.getStatus()));

		if (request.getFiles().size() > 0) {
			statusStack.add(new SimpleLabel("Test data:"));
			Vector<String> files = request.getFiles();
			for (String file : files) {
				String[] fileA = file.split("/");

				statusStack.add(new SimpleButton(fileA[fileA.length - 1]));
			}
		}
		rightStuff.add(statusStack);
		if (ETA.getInstance().getUser().getPermissionLevel() >= 7) {
			if (request.getStatus().equals("Open"))
				rightStuff.add(new SimpleButton("Close Request").addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						communicationService.closeRequest(true, id, new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					}
				}));
			else
				rightStuff.add(new SimpleButton("Open Request").addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						communicationService.closeRequest(false, id, new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					}
				}));
			rightStuff.add(new SimpleButton("Delete").addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					communicationService.removeRequest(id, new MyAsyncCallback<Void>() {
						@Override
						public void success(Void result) {
							ETA.getInstance().removeTab(getId());
						}
					});
				}
			}));
		}
		pane.add(new Filler(20));
		pane.add(rightStuff);
	}

	@Override
	public Widget getBar() {
		HorizontalPanel bar = new HorizontalPanel();
		bar.add(new Button("List of Requests").setClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				// ETA.getInstance().addTab(new RequestTab());
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("New Request").setClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab(new NewRequestTab());
			}
		}));
		return bar;
	}

	@Override
	public String getId() {
		return "rq#" + id;
	}
}
