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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.client.ETA;
import cgrb.eta.client.ETATypeEventOccurred;
import cgrb.eta.client.EventListener;
import cgrb.eta.client.LoadingSpinner;
import cgrb.eta.client.Theme;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.FocusLostHandler;
import cgrb.eta.client.button.HorizontalImgMenuButton;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ImgMenuButton;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.shared.etatype.ETAType;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class Table<T extends ETAType> extends Composite implements ClickHandler, ETATypeEventOccurred<T> {

	private Vector<T> data;
	protected MyGrid grid;
	protected HorizontalPanel headers;
	protected Vector<Column<T>> columns;
	protected RowClickHandler<T> clickHandler;
	protected FlowPanel scrollPane;
	protected boolean addListener = false;
	protected HorizontalPanel actionTop;
	protected boolean canSelect = true;
	protected RowFilter<T> filter;
	protected boolean resize = false;
	protected String emptyString = null;
	protected DragListener listener;
	protected HashMap<Integer, Integer> map;
	protected boolean canEdit = false;
	protected int[] editingCell = null;
	protected VerticalPanel pane = new VerticalPanel();
	protected RightClickHandler<T> handler;
	protected HorizontalPanel actionPanel;
	public static final int MULTIPLE_SELECT = 0;
	public static final int SINGLE_SELECT = 1;
	protected Vector<Widget> singleButtons = new Vector<Widget>();
	protected Vector<Widget> multipleButtons = new Vector<Widget>();
	protected LoadingSpinner spinner = new LoadingSpinner(Theme.DARK, "Loading data");
	protected boolean dontClear = false;
	public boolean canSelect() {
		return canSelect;
	}

	public void setCanSelect(boolean canSelect) {
		this.canSelect = canSelect;
	}

	public FlowPanel getScrollPane() {
		return scrollPane;
	}

	protected class MyGrid extends Grid {
		public MyGrid(int i, int length) {
			super(i, length);
		}

		public int getRowForPoint(Event event) {
			Element td = getEventTargetCell(event);
			if (td == null)
				return -1;
			return TableRowElement.as(td.getParentElement()).getSectionRowIndex();
		}
	}

	public Table() {
		sinkEvents(Event.ONCONTEXTMENU);
	}

	public void setRightClickHandler(RightClickHandler<T> handler) {
		this.handler = handler;
	}

	@SafeVarargs
	public Table(boolean resize, Column<T>... cols) {
		this.resize = resize;
		actionTop = new HorizontalPanel();
		actionTop.setStyleName("tab-bar");
		actionTop.setHeight("16px");
		actionTop.setWidth("100%");
		actionTop.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		actionPanel = new HorizontalPanel();
		actionTop.add(actionPanel);
		map = new HashMap<Integer, Integer>();
		data = new Vector<T>();
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
		grid.getElement().getStyle().setOverflow(Overflow.AUTO);
		scrollPane = new FlowPanel();
		scrollPane.getElement().getStyle().setOverflow(Overflow.AUTO);
		scrollPane.add(grid);
		scrollPane.setStyleName("scroller");
//		scrollPane.addStyleName("loading");
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
		sinkEvents(Event.ONCONTEXTMENU | Event.ONMOUSEOUT | Event.ONMOUSEMOVE);
	}

	public void onBrowserEvent(Event event) {
		event.stopPropagation();
		event.preventDefault();
		switch (DOM.eventGetType(event)) {
		case Event.ONCONTEXTMENU:
			handleContext(event);
			break;
		case Event.ONMOUSEMOVE:
			int row = grid.getRowForPoint(event);
			handleMouseMove(row);
			break;
		default:
			break;
		}

	}

	private int lastRow = -1;

	ArrayList<ScrollAnimation> animations = new ArrayList<ScrollAnimation>();

	private void handleMouseMove(int row) {
		if (row != lastRow && row > -1) {
			int cols = grid.getColumnCount();
			if (lastRow > -1) {
				while (animations.size() > 0) {
					animations.remove(0).cancel();
				}
			}
			for (int i = 0; i < cols; i++) {
				Widget wid = grid.getWidget(row, i);
				if (wid instanceof Label) {
					Label lab = (Label) wid;
					int actualWidth = ETA.getTextWidth(lab.getText());
					if (actualWidth - 12 > lab.getOffsetWidth()) {
						ScrollAnimation scroller = new ScrollAnimation(lab, actualWidth);
						scroller.run((actualWidth - lab.getOffsetWidth()) * 20);
						animations.add(scroller);
					}
				}
			}
			lastRow = row;
		}
	}

	private class ScrollAnimation extends Animation {
		private Label lab;
		private int width;
		private int actualWidth;

		public ScrollAnimation(Label label, int actualWidth) {
			lab = label;
			this.actualWidth = actualWidth;
			width = label.getOffsetWidth();
		}

		@Override
		protected void onUpdate(double progress) {
			lab.getElement().getStyle().setLeft((width - actualWidth) * progress, Unit.PX);
		}

		@Override
		public void cancel() {
			super.cancel();
			lab.getElement().getStyle().setLeft(0, Unit.PX);
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
		int row = grid.getRowForPoint(event);
		if (row < 0)
			return;
		T rec = getRecordForRow(row);
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
				clearSelection();
				selection.add(getRowForRec(rec));
				grid.getRowFormatter().getElement(getRowForRec(rec)).addClassName("selected");
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
			int width = popup.getOffsetWidth();
			int height = popup.getOffsetHeight();
			int x = event.getClientX();
			int y = event.getClientY();
			if (Window.getClientWidth() < x + width) {
				x = Window.getClientWidth() - width - 20;
			}
			if (Window.getClientHeight() < y + height) {
				y = Window.getClientHeight() - height - 20;
			}
			popup.setPopupPosition(x, y);
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
	
	public void preventClearing(){
		dontClear=true;
	}

	@Override
	protected void onLoad() {
		super.onLoad();
		if (resize)
			adjustSize();
	}

	public void adjustSize() {
		scrollPane.setHeight(Window.getClientHeight() - scrollPane.getAbsoluteTop() - 18 + "px");

	}

	public void setDragListener(DragListener listener) {
		this.listener = listener;
	}

	public void addColumn(final Column<T> col) {
		columns.add(col);
		// now go and draw the cols
		grid.resizeColumns(columns.size());
		Label name = new Label(col.getTitle());
		name.setWidth(col.getWidth());
		if (col.canSort()) {
			name.getElement().getStyle().setCursor(Cursor.POINTER);
			name.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					col.sorted();
					sortData(col);
				}
			});
		}
		headers.add(name);

		if (col.getWidth() != null && !col.getWidth().equals("")) {
			// headers.setCellHorizontalAlignment(name, HasHorizontalAlignment.ALIGN_CENTER);
			grid.getColumnFormatter().setWidth(columns.size() - 1, col.getWidth());
			headers.setCellWidth(name, col.getWidth());
		}
	}

	public void removeColumn(final Column<T> col) {
		columns.remove(col);
		grid.resizeColumns(columns.size());

	}

	void drawRow(int row, T record) {
		for (int i = 0; i < columns.size(); i++) {
			drawCell(row, i, record);
		}
		if (listener != null) {
			grid.getRowFormatter().getElement(row).setPropertyString("draggable", "true");
			DragCreator.addDrag(grid.getRowFormatter().getElement(row), record, listener);
		}
		map.put(record.getId(), row);

	}

	public void applyFilter(RowFilter<T> filter) {
		this.filter = filter;
	}

	void drawCell(int row, int col, T record) {
		Object obj = columns.get(col).getValue(record);
		if (obj instanceof String) {
			Label lab = new Label((String) obj);
			lab.setStyleName("eta-label");
			grid.setWidget(row, col, lab);
		} else if (obj instanceof Widget) {
			grid.setWidget(row, col, (Widget) obj);
		}
		String style = columns.get(col).getStyle(record);
		if (style != null)
			grid.getCellFormatter().setStyleName(row, col, style);
	}

	public void sortData(Column<T> col) {
		Collections.sort(data, col);
		// redraw the data
		int i = 0;
		map.clear();
		grid.resizeRows(0);
		grid.resizeRows(data.size());
		for (T rec : data) {
			drawRow(i++, rec);
			if (filter != null && filter.filterRecord(rec)) {
				grid.getRowFormatter().setVisible(i - 1, false);
			} else {
				grid.getRowFormatter().setVisible(i - 1, true);
			}
		}
	}

	void addRow(T entry) {
		data.add(entry);
		// tack it on the the end of the data
		drawRow(data.size() - 1, entry);
	}

	public void addRec(T record) {
		data.add(record);
		grid.resizeRows(data.size());
		drawRow(data.size() - 1, record);
	}

	@SuppressWarnings("unchecked")
	public void setData(Vector<T> data) {
		lastRow = -1;
		this.data.clear();
		map.clear();
		pane.remove(actionTop);
		grid.resize(0, columns.size());
		if (data == null || data.size() == 0) {
			grid.resize(1, 1);
			scrollPane.removeStyleName("loading");
			if(emptyString != null)
			grid.setHTML(0, 0, emptyString);
			return;
		}
		if (!addListener && data.size() > 0) {
			EventListener.getInstance().addETATypeListener(data.get(0).getClass().getName(), (ETATypeEventOccurred<ETAType>) this);
			addListener = true;
		}
		grid.resize(data.size(), columns.size());

		for (T rec : data) {
			addRow(rec);
		}
		scrollPane.removeStyleName("loading");
		selection.clear();
	}

	@SuppressWarnings("unchecked")
	public void addListener(Class<?> type) {
		if (!addListener) {
			EventListener.getInstance().addETATypeListener(type.getName(), (ETATypeEventOccurred<ETAType>) this);
			addListener = true;
		}
	}

	public void setRowClickHandler(RowClickHandler<T> handler) {
		this.clickHandler = handler;
	}

	public void setEmptyString(String mesg) {
		emptyString = mesg;
	}

	public int getRowForRec(T record) {
		return data.lastIndexOf(record);
	}

	public Element getElementForRecord(T rec) {
		return grid.getRowFormatter().getElement(map.get(rec.getId()));
	}

	public Element getElementForRecord(int rec) {
		return grid.getRowFormatter().getElement(map.get(rec));
	}

	public Widget getWidgetAt(int row, int col) {
		return grid.getWidget(row, col);
	}

	public void onClick(ClickEvent event) {
		Cell clickedCell = grid.getCellForEvent(event);
		if (clickedCell == null)
			return;
		int row = clickedCell.getRowIndex();
		int col = clickedCell.getCellIndex();

		if (canEdit) {
			if (editingCell == null || !(editingCell[0] == row && editingCell[1] == col)) {
				if (editingCell != null) {
					Editor editor = (Editor) grid.getWidget(editingCell[0], editingCell[1]);
					columns.get(editingCell[1]).setValue(data.get(editingCell[0]), editor.getEditingValue());
					drawCell(editingCell[0], editingCell[1], data.get(editingCell[0]));
				}
				editingCell = null;
			} else {
				return;
			}
			Editor wid = (Editor) columns.get(col).getEditor(data.get(row));
			if (wid != null) {
				wid.setFocusListener(new FocusLostHandler() {
					public void onBlur() {
						cancelEdit();
					}
				});
				grid.setWidget(clickedCell.getRowIndex(), clickedCell.getCellIndex(), wid);
				wid.setFocus();
				editingCell = new int[] { row, col };
				return;
			}
		}
		if (multipleButtons.size() > 0 || singleButtons.size() > 0)
			handleSelection(clickedCell.getRowIndex(), event);
		else if (clickHandler != null) {
			clickHandler.rowClicked(data.get(clickedCell.getRowIndex()), clickedCell.getCellIndex(), clickedCell.getRowIndex());
		}

	}

	int lastClickedRow = -1;
	Timer t = null;

	protected void handleSelection(final Integer row, ClickEvent event) {
		Cell clickedCell = grid.getCellForEvent(event);
		final boolean shift = event.isShiftKeyDown();
		final boolean control = event.isMetaKeyDown() || event.isControlKeyDown();
		if (t == null) {
			t = new Timer() {
				@Override
				public void run() {
					t = null;
					Element rowE = grid.getRowFormatter().getElement(row);
					if (!(control || shift) && lastClickedRow != row) {
						// remove all other selections
						for (int i : selection) {
							grid.getRowFormatter().getElement(i).removeClassName("selected");
						}
						selection.clear();
					}
					if (shift) {
						// get select all rows between the lastClickedRow
						if (lastClickedRow > -1) {
							if (lastClickedRow > row) {
								for (int i = row + 1; i <= lastClickedRow; i++) {
									grid.getRowFormatter().getElement(i).addClassName("selected");
									if (!selection.contains(i))
										selection.add(i);
								}
							} else {
								for (int i = lastClickedRow; i < row; i++) {
									grid.getRowFormatter().getElement(i).addClassName("selected");
									if (!selection.contains(i))
										selection.add(i);
								}
							}
						}
						lastClickedRow = row;
					}
					lastClickedRow = row;
					if (selection.contains(row)) {
						rowE.removeClassName("selected");
						selection.remove(row);
					} else {
						rowE.addClassName("selected");
						selection.add(row);
					}

					if (!canSelect)
						return;
					if (selection.size() == 0) {
						pane.remove(actionTop);
						if (resize)
							scrollPane.setHeight(Window.getClientHeight() - scrollPane.getAbsoluteTop() - 18 + "px");
					} else if (selection.size() == 1) {
						pane.remove(actionTop);
						pane.insert(actionTop, 0);
						actionPanel.clear();
						for (Widget but : singleButtons) {
							but.setSize("16px", "16px");
							actionPanel.add(but);
						}
						for (Widget but : multipleButtons) {
							but.setSize("16px", "16px");
							actionPanel.add(but);
						}
						if (resize)
							scrollPane.setHeight(Window.getClientHeight() - scrollPane.getAbsoluteTop() - 18 + "px");

					} else if (selection.size() > 1) {
						actionPanel.clear();
						for (Widget but : multipleButtons) {
							but.setSize("16px", "16px");
							actionPanel.add(but);
						}
					}
				}
			};
			t.schedule(300);
		} else if (clickHandler != null) {
			t.cancel();
			t = null;
			clickHandler.rowClicked(getRecordForRow(row), clickedCell.getCellIndex(), clickedCell.getRowIndex());
		}
	}

	protected T getRecordForRow(Integer row) {
		return data.get(row);
	}

	protected Vector<Integer> selection = new Vector<Integer>();

	@SuppressWarnings("unchecked")
	public Vector<T> getSelection() {
		Vector<ETAType> selectedItems = new Vector<ETAType>();
		for (int row : selection) {
			selectedItems.add(data.get(row));
		}
		return (Vector<T>) selectedItems;
	}

	public void cancelEdit() {
		if (editingCell == null)
			return;
		Editor editor = (Editor) grid.getWidget(editingCell[0], editingCell[1]);
		columns.get(editingCell[1]).setValue(data.get(editingCell[0]), editor.getEditingValue());
		drawCell(editingCell[0], editingCell[1], data.get(editingCell[0]));
		editingCell = null;

	}

	public FlowPanel getScrollPanel() {
		return scrollPane;
	}

	public void onUpdate(T record) {
		if (data.contains(record)) {
			int row = map.get(record.getId());
			data.remove(row);
			data.insertElementAt(record, row);
			drawRow(row, record);
		}
	}

	public void onAddition(T record) {
		if (data.contains(record)) {
			onUpdate(record);
			return;
		}
		grid.resize(data.size() + 1, columns.size());
		addRow(record);
	}

	public void clearSelection() {
		dontClear=false;
		for (int i : selection) {
			grid.getRowFormatter().getElement(i).removeClassName("selected");
		}
		selection.clear();
		pane.remove(actionTop);
	}

	public void onRemoval(T record) {
		if (data.contains(record)) {
			for (int i = 0; i < selection.size(); i++) {
				if (data.get(selection.get(i)).equals(record)) {
					grid.getRowFormatter().getElement(i).removeClassName("selected");
					selection.remove(i);
					if (selection.size() == 0)
						pane.remove(actionTop);
				}
			}
			data.remove(record);
			grid.removeRow(map.get(record.getId()));
			map.clear();
			int i = 0;
			for (T t : data) {
				map.put(t.getId(), i++);
			}
		}
	}

	/**
	 * @param b
	 */
	public void setCanEdit(boolean b) {
		canEdit = b;
	}

	public void moveRecord(T record, int newLoc) {
		int oldLoc = map.get(record.getId());
		data.remove(oldLoc);
		data.insertElementAt(record, newLoc);
		map.clear();
		int i = 0;
		for (T t : data) {
			map.put(t.getId(), i++);
		}
		grid.removeRow(oldLoc);
		grid.insertRow(newLoc);
		drawRow(newLoc, record);
	}


	public void addAction(Widget button, int type) {
		if (type == MULTIPLE_SELECT)
			multipleButtons.add(button);
		else
			singleButtons.add(button);
	}

	public Vector<T> getData() {
		return data;
	}
	
	public void displayWaiting(String message){
		lastRow = -1;
		if(data!=null)
		this.data.clear();
		map.clear();
		pane.remove(actionTop);
		grid.resize(0, columns.size());
		spinner.setMessage(message);
		scrollPane.addStyleName("loading");
	}
}
