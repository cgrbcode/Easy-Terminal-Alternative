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

import java.util.Vector;

import cgrb.eta.client.EnterButtonEventManager;
import cgrb.eta.client.EnterListener;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.tabs.InputsTable;
import cgrb.eta.client.window.SC;
import cgrb.eta.client.window.Window;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.wrapper.Input;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TypeEditor extends Editor implements ClickHandler {
	ListBox select = new ListBox();
	HorizontalPanel panel = new HorizontalPanel();
	ImgButton edit;
	String selected = "";
	String extendedType = "";
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);

	public TypeEditor(String value) {
		edit = new ImgButton(Resources.INSTANCE.edit());
		edit.setSize(20);
		select.addItem("String");
		select.addItem("File");
		select.addItem("Flag");
		select.addItem("Number");
		select.addItem("Selection");
		select.addItem("List");
		select.addItem("Input-List");
		select.addBlurHandler(this);
		select.addKeyPressHandler(this);
		select.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				String val = select.getValue(select.getSelectedIndex());
				setTypeValue(val);
			}
		});
		initWidget(panel);
		panel.add(select);
		edit.setClickHandler(this);
		if (value != null && value.length() > 1) {
			selected = value.split(":")[0];
			if (value.contains(":")) {
				extendedType = value.substring(value.indexOf(":") + 1);
			}
		} else
			selected = "String";
		if (selected.equals("File")) {
			select.setSelectedIndex(1);
		} else if (selected.equals("Flag")) {
			select.setSelectedIndex(2);
		} else if (selected.equals("Number")) {
			select.setSelectedIndex(3);
		} else if (selected.equals("Selection")) {
			select.setSelectedIndex(4);
		} else if (selected.equals("List")) {
			select.setSelectedIndex(5);
		} else if (selected.equals("Input-List")) {
			select.setSelectedIndex(6);
		}
		setTypeValue(selected);
	}

	private void setTypeValue(String type) {
		selected = type;
		panel.remove(edit);
		if (type.equals("File") || type.equals("Selection") || type.equals("List") || type.equals("Input-List")) {
			panel.add(edit);
		}
	}

	@Override
	public String getEditingValue() {
		return selected + ":" + extendedType;
	}

	@Override
	public void setFocus() {
	}

	public void onClick(ClickEvent event) {
		if (selected.equals("File")) {
			SC.getFileType(new ValueListener<String>() {
				public void returned(String ret) {
					extendedType = ret;
				}
			});
		} else if (selected.equals("Selection")) {
			showSelectDialog();
		} else if (selected.equals("List")) {
			showListDialog();
		} else if (selected.equals("Input-List")) {
			showInputListDialog();
		}
	}

	private int inputCount=0;
	private void showInputListDialog() {
		final InputsTable table = new InputsTable(true);
		wrapperService.getVectorObjFromJson(extendedType, new MyAsyncCallback<Vector<Input>>() {
			@Override
			public void success(Vector<Input> result) {
				for (Input in : result) {
					table.addInput(in);
				}
			}
		});
		table.getScrollPane().setHeight("400px");
		HorizontalPanel hpane = new HorizontalPanel();
		hpane.setHeight("20px");
		hpane.add(new Button("Add input").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				table.addInput(new Input(inputCount, "", "Untitled "+inputCount++, "", "", false, inputCount, "", ""));
			}
		}));
		hpane.add(new Seprator());
		
		SC.ask("Modify the inputs for this input-list", table,hpane, new ValueListener<Boolean>() {
			public void returned(Boolean ret) {
				wrapperService.toJsonArr(table.getData(), new MyAsyncCallback<String>() {
					@Override
					public void success(String result) {
						extendedType = result;
					}
				});
			}
		});

	}

	public class Item extends ETAType {
		private static final long serialVersionUID = 4989801643100966361L;
		private String value;

		public Item(String value, int id) {
			this.id = id;
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private int on = 0;

	public void showSelectDialog() {
		VerticalPanel panel = new VerticalPanel();
		HorizontalPanel addPanel = new HorizontalPanel();
		final TextBox value = new TextBox();

		value.setStyleName("eta-input2");
		addPanel.add(value);
		panel.add(addPanel);
		Column<Item> name = new Column<Item>("Value") {
			@Override
			public Object getValue(Item record) {
				return record.getValue();
			}
		};
		Column<Item> remove = new Column<Item>("") {
			@Override
			public Object getValue(Item record) {
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
		final Table<Item> items = new Table<TypeEditor.Item>(false, name, remove);
		items.setRowClickHandler(new RowClickHandler<TypeEditor.Item>() {
			public void rowClicked(Item record, int col, int row) {
				if (col == 1) {
					items.onRemoval(record);
				}
			}
		});
		items.setWidth("200px");
		items.getScrollPane().setHeight("400px");
		addPanel.add(new SimpleButton("Add").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (value.getValue() != null || value.getValue().length() > 0) {
					items.addRec(new Item(value.getValue(), on++));
					value.setValue("");
				}

			}
		}));
		EnterButtonEventManager.addListener(new EnterListener() {
			public void enter() {
				if (value.getValue() != null || value.getValue().length() > 0) {
					items.addRec(new Item(value.getValue(), on++));
					value.setValue("");
				}
			}
		});
		panel.add(items);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setWidth("100%");
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window("Enter a new Item for your list", panel,true);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		buttons.add(new SimpleButton("Okay").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Vector<Item> data = items.getData();
				extendedType = "";
				for (Item item : data) {
					extendedType += item.getValue() + ",";
				}
				extendedType = extendedType.substring(0, extendedType.length() - 1);
				window.destroy();
			}
		}));
		panel.add(buttons);
		window.showWindow();

		String[] tempItems = extendedType.split(",");
		for (int i = 0; i < tempItems.length; i++) {
			if (tempItems[i].length() > 0)
				items.onAddition(new Item(tempItems[i], i));
		}
	}

	public void showListDialog() {
		VerticalPanel panel = new VerticalPanel();
		final Window window;
		final TypeEditor innerList = new TypeEditor(extendedType);
		HorizontalPanel hoz = new HorizontalPanel();
		final TextBox seprator = new TextBox();
		seprator.setStyleName("eta-input");
		hoz.add(innerList);
		hoz.add(seprator);
		panel.add(hoz);
		window = new Window("Select a type", panel,true);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				extendedType = innerList.getEditingValue() + "{" + seprator.getValue() + "}";
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		panel.add(buttons);
		window.showWindow();
	}
}
