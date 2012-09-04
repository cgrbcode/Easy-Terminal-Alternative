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
package cgrb.eta.client.window;

import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.shared.etatype.User;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class MultipleUserSelect extends Composite {
	private static final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private Table<User> users;
	private SuggestBox userBox;
	private HashMap<String,User> usersHash ;
	public MultipleUserSelect() {
		VerticalPanel pane = new VerticalPanel();
		Column<User> name = new Column<User>("Name") {
			@Override
			public Object getValue(User record) {
				return record.getName();
			}
		};
		Column<User> remove = new Column<User>("") {
			@Override
			public Object getValue(User record) {
				Image removeI = new Image(Resources.INSTANCE.remove().getSafeUri().asString());
				removeI.setHeight("16px");
				removeI.setWidth("16px");
				removeI.getElement().getStyle().setCursor(Cursor.POINTER);
				return removeI;
			}
			@Override
			public String getWidth() {
				return "20px";
			}
		};
		users = new Table<User>(false, name, remove);
		users.setRowClickHandler(new RowClickHandler<User>() {
			public void rowClicked(User record, int col, int row) {
				if(col==1)
				users.onRemoval(record);
			}
		});
		HorizontalPanel top = new HorizontalPanel();
		pane.add(top);
		pane.add(users);
		initWidget(pane);
		final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
		userBox = new SuggestBox(oracle);
		sqlService.getUsers(new MyAsyncCallback<Vector<User>>() {
			@Override
			public void success(Vector<User> result) {
				usersHash=new HashMap<String, User>();
				for(User us:result){
					usersHash.put(us.getName(), us);
					oracle.add(us.getName());
				}
			}
		});
		top.add(userBox);
		top.add(new SimpleButton("Add").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addSelectedUser();
			}
		}));
		userBox.setStyleName("eta-input2");
		users.setWidth("250px");
		users.getScrollPane().setHeight("300px");
		userBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			public void onSelection(SelectionEvent<Suggestion> event) {
				addSelectedUser();
			}
		});
	}
	private void addSelectedUser(){
		String selected = userBox.getValue();
		if(usersHash.containsKey(selected)){
			userBox.setValue("");
			users.addRec(usersHash.get(selected));
		}
	}
	public Vector<User> getUsers(){
		return users.getData();
	}
}
