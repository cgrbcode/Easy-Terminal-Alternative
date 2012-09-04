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
package cgrb.eta.client.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.ETATypeEventOccurred;
import cgrb.eta.client.EventListener;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.HorizontalImgMenuButton;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ImgMenuButton;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.shared.etatype.ETAType;

public abstract class TreeTable<T extends ETAType> extends Table<T> {
	HashMap<Integer, Vector<T>> data;
	HashMap<Integer, Boolean> expansions = new HashMap<Integer, Boolean>();
	int count = 0;

	@SafeVarargs
	public TreeTable(boolean resize, Column<T>... cols) {
		super();
		actionTop = new HorizontalPanel();
		actionTop.setStyleName("tab-bar");
		actionTop.setHeight("16px");
		actionTop.setWidth("100%");
		actionTop.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		actionPanel = new HorizontalPanel();
		actionTop.add(actionPanel);
		this.resize = resize;
		map = new HashMap<Integer, Integer>();
		data = new HashMap<Integer, Vector<T>>();
		data.put(0, new Vector<T>());
		grid = new MyGrid(0, cols.length);
		columns = new Vector<Column<T>>();
		VerticalPanel dataPanel = new VerticalPanel();
		headers = new HorizontalPanel();

		for (Column<T> col : cols) {
			addColumn(col);
		}

		headers.setWidth("100%");
		headers.setHeight("20px");
		headers.setStyleName("header");
		grid.setWidth("100%");
		grid.addStyleName("data");
		grid.setCellSpacing(0);
		pane.add(headers);
		pane.setCellHeight(headers, "20px");
		pane.add(dataPanel);
		// grid.getElement().getStyle().setOverflow(Overflow.AUTO);
		scrollPane = new FlowPanel();
		scrollPane.getElement().getStyle().setOverflowX(Overflow.HIDDEN);
		scrollPane.add(grid);
		scrollPane.setStyleName("scroller");
		scrollPane.addStyleName("loading");
		scrollPane.add(spinner);
		dataPanel.add(scrollPane);
		dataPanel.setWidth("100%");
		pane.getElement().getStyle().setOverflow(Overflow.SCROLL);
		initWidget(pane);
		if (resize)
			Window.addResizeHandler(new ResizeHandler() {
				public void onResize(ResizeEvent event) {
					scrollPane.setHeight(event.getHeight() - scrollPane.getAbsoluteTop() - 18 + "px");
				}
			});
		setStyleName("eta-table");

		grid.addClickHandler(this);
		sinkEvents(Event.ONCONTEXTMENU);
	}


	public void onBrowserEvent(Event event) {
		event.stopPropagation();
		event.preventDefault();
		switch (DOM.eventGetType(event)) {

		case Event.ONCONTEXTMENU:
			handleContext(event);
			break;
		default:
			break;
		}
	}

	private void getPanelForWidgets(VerticalPanel panel, Vector<Widget> widgets) {
		for (Widget wid : widgets) {
			if (wid instanceof ImgButton) {
				ImgButton but = (ImgButton) wid;
				panel.add(new ImgButton(but.getResource(), but.getToolTipText()).setClickHandler(but.getHandler()));
			} else if (wid instanceof ImgMenuButton) {
				ImgMenuButton but = (ImgMenuButton) wid;
				HorizontalImgMenuButton newBut = new HorizontalImgMenuButton(but.getDataResource(), but.getTitle());
				Vector<Widget> items = but.getMenuItems();
				newBut.setSize(20);
				for (Widget item : items) {
					if (item instanceof Button) {
						Button temp = (Button) item;
						newBut.addButton(new Button(temp.getTitle()).setClickHandler(temp.getHandler()));
					}
				}
				panel.add(newBut);
			} else {
			}
		}
	}

	protected void handleContext(Event event) {
		// grid.get
		VerticalPanel master = new VerticalPanel();
		master.setStyleName("context-menu");
		int row  = grid.getRowForPoint(event);
		T rec = getRecordForRow(row);
//		SC.alert("", "asdfasdf"+row);
		// check to see if this record is in the selection, if it is display the menu for the selected records, otherwise display the selection for this one record
		if (rec != null) {
			boolean hasInSelection = false;
			for (T temp : getSelection()) {
				if (temp.equals(rec)) {
					hasInSelection = true;
					break;
				}
			}
			if (hasInSelection) {
				if (selection.size() > 1) {
					getPanelForWidgets(master, multipleButtons);
				} else {
					getPanelForWidgets(master, singleButtons);
					getPanelForWidgets(master, multipleButtons);
				}
			} else {
				for (int i : selection) {
					grid.getRowFormatter().getElement(i).removeClassName("selected");
				}
				selection.clear();
				selection.add(row);
				grid.getRowFormatter().getElement(row).addClassName("selected");
				getPanelForWidgets(master, singleButtons);
				getPanelForWidgets(master, multipleButtons);
			}
			if (handler != null) {
				master.add(new SeperatorButton());
				Widget wid = handler.getWigetForContext(rec);
				master.add(wid);
			}
			final PopupPanel popup = new PopupPanel(true);
			popup.setWidget(master);
			popup.show();
			popup.setPopupPosition(event.getClientX(), event.getClientY());
			master.addDomHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					popup.hide(true);
				}
			}, ClickEvent.getType());
			popup.addCloseHandler(new CloseHandler<PopupPanel>() {
				@Override
				public void onClose(CloseEvent<PopupPanel> event) {
					if(!dontClear)
					clearSelection();
				}
			});
		}
	}


	@SuppressWarnings("unchecked")
	public Vector<T> getSelection() {
		Vector<ETAType> selectedItems = new Vector<ETAType>();
		for (int row : selection) {
			selectedItems.add(getRecordForRow(row));
		}
		return (Vector<T>) selectedItems;
	}

	@Override
	void addRow(T entry) {
		int parent = getParent(entry);
		if (data.containsKey(parent)) {
			data.get(parent).add(entry);
		} else {
			data.get(0).add(entry);
		}
		// tack it on the the end of the data
		grid.resizeRows(data.size());
		drawRow(data.size() - 1, entry);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setData(Vector<T> data) {
		pane.remove(actionTop);
		
		if ((data == null || data.size() == 0) && emptyString != null) {
			grid.resize(1, 1);
			grid.setHTML(0, 0, emptyString);
			grid.getColumnFormatter().getElement(0).getStyle().setWidth(100, Unit.PCT);
			scrollPane.removeStyleName("loading");
			return;
		}
		if (!addListener) {
			EventListener.getInstance().addETATypeListener(data.get(0).getClass().getName(), (ETATypeEventOccurred<ETAType>) this);
			addListener = true;
		}
		// grid.resizeRows(data.size());
		this.data.clear();
		this.data.put(0, new Vector<T>());
		count = data.size();

		for (T rec : data) {
			if (isFolder(rec)) {
				this.data.put(rec.getId(), new Vector<T>());
			}
		}

		for (T rec : data) {
			addRecord(rec);
		}

		draw();
		scrollPane.removeStyleName("loading");
	}

	private void addRecord(T rec) {
		if (grid.getRowCount() == 1 && grid.getColumnCount() == 1) {
			grid.resize(1, columns.size());
		}
		int parent = getParent(rec);
		if (this.data.containsKey(parent)) {
			this.data.get(parent).add(rec);
		} else {
			this.data.get(0).add(rec);
		}
	}

	private void removeRecord(T record) {
		Iterator<Integer> it = this.data.keySet().iterator();
		while (it.hasNext()) {
			Vector<T> temp = this.data.get(it.next());
			for (T rec : temp) {
				if (rec.getId() == record.getId()) {
					map.remove(rec.getId());
					temp.remove(rec);
					return;
				}
			}
		}
	}

	private int onRow = 0;
	private int depth = -1;

	@Override
	public void sortData(Column<T> col) {
		Iterator<Integer> it = this.data.keySet().iterator();
		int recordCount = 0;
		while (it.hasNext()) {
			Vector<T> temp = this.data.get(it.next());
			Collections.sort(temp, col);
			recordCount += temp.size();
		}
		grid.resizeRows(recordCount);
		onRow = 0;
		depth = -1;
		draw(0);
	}

	private void draw() {
		Iterator<Integer> it = this.data.keySet().iterator();
		int recordCount = 0;
		while (it.hasNext()) {
			Vector<T> temp = this.data.get(it.next());
			Collections.sort(temp, columns.get(0));
			recordCount += temp.size();
		}

		grid.resizeRows(recordCount);
		onRow = 0;
		depth = -1;
		draw(0);
	}

	void drawCell(final int row, int col, final T record) {
		Object obj = columns.get(col).getValue(record);
		if (obj instanceof String) {
			grid.setText(row, col, (String) obj);
		} else if (obj instanceof Widget) {
			if (col == 0) {
				HorizontalPanel temp = new HorizontalPanel();
				Label blank = new Label();
				blank.setWidth(depth * 15 + "px");
				temp.add(blank);
				if (isFolder(record)) {
					Label arrow = new Label();
					arrow.setStyleName("arrow");
					arrow.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							showChildren(record.getId(), !grid.getRowFormatter().getStyleName(row).equals("shown"));
							event.stopPropagation();
						}
					});
					arrow.getElement().getStyle().setCursor(Cursor.POINTER);
					temp.add(arrow);
					temp.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
				} else {
					Label nonArrow = new Label();
					nonArrow.setWidth("10px");
					temp.add(nonArrow);
				}
				temp.add((Widget) obj);
				grid.setWidget(row, col, temp);
			} else
				grid.setWidget(row, col, (Widget) obj);
		}
	};

	private void draw(int parent) {
		depth++;
		Vector<T> root = this.data.get(parent);
		if (root == null) {
			depth--;
			return;
		}
		for (T t : root) {
			map.put(t.getId(), onRow);
			drawRow(onRow++, t);
			if (parent > 0) {
				Boolean show = expansions.get(parent);
				if (show == null || show == false)
					grid.getRowFormatter().setVisible(onRow - 1, false);
			}
			if (isFolder(t)) {
				draw(t.getId());
			}
		}
		depth--;
	}

	private void showChildren(int parent, boolean expand) {
		Vector<T> children = data.get(parent);
		grid.getRowFormatter().setStyleName(map.get(parent), expand ? "shown" : "hidden");
		expansions.put(parent, expand);
		if (children != null) {
			for (T t : children) {
				grid.getRowFormatter().setVisible(map.get(t.getId()), expand);
				if (!expand && isFolder(t)) {
					hideChildren(t.getId());
				}
			}
		}
	}

	private void hideChildren(int parent) {
		Vector<T> children = data.get(parent);
		grid.getRowFormatter().setStyleName(map.get(parent), "hidden");
		if (children != null) {
			for (T t : children) {
				grid.getRowFormatter().setVisible(map.get(t.getId()), false);
				if (isFolder(t)) {
					hideChildren(t.getId());
				}
			}
		}
	}

	public abstract int getParent(T record);

	public int getId(T record) {
		return record.getId();
	}

	public abstract boolean isFolder(T record);

	protected T getRecordForRow(int row) {
		Iterator<Integer> it = data.keySet().iterator();
		while (it.hasNext()) {
			int parent = it.next();
			Vector<T> records = data.get(parent);
			for (T t : records) {
				if (map.get(t.getId()) == null) {
					return null;
				}
				if (row == map.get(t.getId())) {
					return t;
				}
			}
		}
		return null;
	}

	@Override
	public void onClick(ClickEvent event) {
		int row = grid.getCellForEvent(event).getRowIndex();
		// find the record for this
		// Iterator<Integer> it = data.keySet().iterator();
		// while (it.hasNext()) {
		// int parent = it.next();
		// Vector<T> records = data.get(parent);
		// for (T t : records) {
		// if (map.get(t.getId()) == null) {
		// return;
		// }
		// if (row == map.get(t.getId())) {
		// // we found the record
		// if (isFolder(t)) {
		// showChildren(t.getId(), !grid.getRowFormatter().getStyleName(row).equals("shown"));
		// } else {
		// if (clickHandler != null)
		// clickHandler.rowClicked(t, grid.getCellForEvent(event).getCellIndex(), row);
		// }
		// }
		// }
		// }
		if (multipleButtons.size() > 0 || singleButtons.size() > 0)
			handleSelection(row, event);
		else if (clickHandler != null) {
			// clickHandler.rowClicked(data.get(clickedCell.getRowIndex()), clickedCell.getCellIndex(), clickedCell.getRowIndex());
		}
		// show or hide the rows children
		// it = map.keySet().iterator();
		// while (it.hasNext()) {
		// int key = it.next();
		// if (map.get(key) == row){
		// }
		// }
	}

	public void onUpdate(T record) {
		if (map.containsKey(record.getId())) {
			removeRecord(record);
			addRecord(record);
			draw();
		}
	}

	public void onAddition(T record) {
		if (map.containsKey(record.getId())) {
			onUpdate(record);
			return;
		}
		grid.resize(grid.getRowCount() + 1, columns.size());
		addRecord(record);
		draw();
	}

	public void onRemoval(T record) {
		// data.remove(map.get(record.getId()));
		if (map.get(record.getId()) == null)
			return;
		grid.getRowFormatter().getElement(map.get(record.getId())).getStyle().setDisplay(Display.NONE);
		for (int i = 0; i < selection.size(); i++) {
			if (selection.get(i).equals(map.get(record.getId()))) {
				grid.getRowFormatter().getElement(i).removeClassName("selected");
				selection.remove(i);
				if (selection.size() == 0)
					pane.remove(actionTop);
			}
		}
		removeRecord(record);
	}

}
