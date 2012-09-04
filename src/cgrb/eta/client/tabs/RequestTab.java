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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.VoidAsyncCallback;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.StarButton;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.etatype.RequestItem;

public class RequestTab extends ETATab implements RowClickHandler<RequestItem> {
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	HorizontalPanel bar = new HorizontalPanel();
	Table<RequestItem> requests;

	public RequestTab() {
		super("Requests");
		VerticalPanel pane = new VerticalPanel();
		Column<RequestItem> star = new Column<RequestItem>("") {
			@Override
			public Object getValue(RequestItem record) {
				StarButton star = new StarButton();
				star.setStared(record.isStarted());
				star.setSize(20);
				return star;
			}

			@Override
			public String getWidth() {
				return "25px";
			}
		};
		Column<RequestItem> stars = new Column<RequestItem>("Stars") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getStarCount() + "";
			}

			@Override
			public int compareTo(RequestItem o1, RequestItem o2) {
				return o1.getStarCount() - o2.getStarCount();
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public String getWidth() {
				return "40px";
			}
		};
		Column<RequestItem> id = new Column<RequestItem>("Id") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getId() + "";
			}
			@Override
			public boolean canSort() {
				return true;
			}
			@Override
			public String getWidth() {
				return "30px";
			}
		};
		Column<RequestItem> date = new Column<RequestItem>("Date") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getTimestamp() + "";
			}

			@Override
			public String getWidth() {
				return "80px";
			}
		};
		Column<RequestItem> status = new Column<RequestItem>("Status") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getStatus();
			}

			@Override
			public int compareTo(RequestItem o1, RequestItem o2) {
				return o1.getStatus().compareTo(o2.getStatus());
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public String getWidth() {
				return "50px";
			}
		};
		Column<RequestItem> type = new Column<RequestItem>("Type") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getType();
			}
			@Override
			public int compareTo(RequestItem o1, RequestItem o2) {
				return o1.getType().compareTo(o2.getType());
			}
			@Override
			public boolean canSort() {
				return true;
			}
			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<RequestItem> creator = new Column<RequestItem>("Creator") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getCreator();
			}
			@Override
			public int compareTo(RequestItem o1, RequestItem o2) {
				return o1.getCreator().compareTo(o2.getCreator());
			}
			@Override
			public boolean canSort() {
				return true;
			}
			@Override
			public String getWidth() {
				return "160px";
			}
		};
		Column<RequestItem> summary = new Column<RequestItem>("Summary") {
			@Override
			public Object getValue(RequestItem record) {
				return record.getSummary();
			}
		};
		requests = new Table<RequestItem>(true, star, stars, id, date, status, type, creator, summary);
		requests.displayWaiting("Fetching requests");
		sqlService.getRequests(new MyAsyncCallback<Vector<RequestItem>>() {
			@Override
			public void success(Vector<RequestItem> result) {
				requests.setData(result);
			}
		});

		requests.setRowClickHandler(this);
		requests.setWidth("100%");
		// requests.setStyleName("request-grid2");

		pane.add(requests);
		pane.setHeight("100%");
		setPane(pane);
		bar.add(new Button("New Request").setClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab(new NewRequestTab());
			}
		}));
		bar.add(new Seprator());
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	@Override
	public String getId() {
		return "rs";
	}

	public void rowClicked(RequestItem record, int col, int row) {
		if (col == 0) {
			StarButton star = (StarButton) requests.getWidgetAt(row, col);
			communicationService.starRequest(star.isStared(), record.getId(), new VoidAsyncCallback());
			return;
		}
		ETA.getInstance().addTab(new RequestItemTab(record.getId()));
	}

}
