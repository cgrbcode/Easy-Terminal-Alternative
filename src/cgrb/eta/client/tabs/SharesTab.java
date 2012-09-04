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
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.TreeTable;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.shared.etatype.Share;

public class SharesTab extends ETATab{
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private TreeTable<Share> myShares;
	private TreeTable<Share> otherShares;

	public SharesTab(){
		super("Shares");
		
		Column<Share> toUser = new Column<Share>("To User") {
			@Override
			public Object getValue(Share record) {
				return record.getToUser();
			}
			@Override
			public String getWidth() {
				return "200px";
			}
		};
		
		Column<Share> name = new Column<Share>("Name") {
			@Override
			public Object getValue(Share record) {
				HorizontalPanel panel = new HorizontalPanel();
				Image icon = new Image(getIcon(record));
				icon.setHeight("16px");
				icon.setWidth("16px");
				panel.add(icon);
				panel.add(new Label(record.getName()));
				return panel;
			}
		};
		
		myShares = new TreeTable<Share>(true,name,toUser) {
			@Override
			public int getParent(Share record) {
				return record.getType();
			}

			@Override
			public boolean isFolder(Share record) {
				return record.getType()==0;
			}
		};
		
		Column<Share> fromUser = new Column<Share>("From User") {
			@Override
			public Object getValue(Share record) {
				return record.getToUser();
			}
			@Override
			public String getWidth() {
				return "200px";
			}
		};
		myShares = new TreeTable<Share>(true,name,toUser) {
			@Override
			public int getParent(Share record) {
				return record.getType();
			}

			@Override
			public boolean isFolder(Share record) {
				return record.getType()==0;
			}
		};
		otherShares = new TreeTable<Share>(true,name,fromUser) {
			@Override
			public int getParent(Share record) {
				return record.getType();
			}

			@Override
			public boolean isFolder(Share record) {
				return record.getType()==0;
			}
		};
		
		myShares.addAction(new ImgButton(Resources.INSTANCE.remove(),20,"Remove Share"),TreeTable.MULTIPLE_SELECT);
		myShares.addAction(new ImgButton(Resources.INSTANCE.results(),20,"View Share").setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
//				Share share = myShares.getSelection().get(0);
				
			}
		}),TreeTable.SINGLE_SELECT);

		sqlService.getMyShares(new MyAsyncCallback<Vector<Share>>() {
			@Override
			public void success(Vector<Share> result) {
				myShares.setData(result);
			}
		});
		sqlService.getOtherShares(new MyAsyncCallback<Vector<Share>>() {
			@Override
			public void success(Vector<Share> result) {
				otherShares.setData(result);
			}
		});
		
		HorizontalPanel pane = new HorizontalPanel();
		otherShares.getScrollPane().setWidth("100%");
		myShares.getScrollPane().setWidth("100%");
		myShares.setWidth("100%");
		otherShares.setWidth("100%");
		
		pane.setHeight("100%");
		pane.setWidth("100%");
		pane.add(myShares);
		pane.add(otherShares);
		pane.setCellWidth(myShares, "50%");
		pane.setCellWidth(otherShares, "50%");

		setPane(pane);
		
	}
	
	@Override
	public String getId() {
		return "shs";
	}
	public String getIcon(Share record) {
		if (record.getType()==0 )
			return "images/folder.png";
		else
			return "images/file.png";
	}
}
