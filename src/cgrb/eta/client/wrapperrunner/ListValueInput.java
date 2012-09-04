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
package cgrb.eta.client.wrapperrunner;

import java.util.Vector;

import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.shared.etatype.ETAType;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ListValueInput extends ValueInput implements ClickHandler {

	/**
	 * @param string
	 */
	private String seperator;
	private ValueInput input;
	private Table<ValueType> values;

	public ListValueInput(String extendedType) {
		String type = extendedType.split("\\{")[0];
		seperator = extendedType.substring(extendedType.indexOf('{') + 1, extendedType.indexOf('}'));
		HorizontalPanel main = new HorizontalPanel();
		main.setWidth("100%");
		input = getValueInput(type);
		main.add(input);
		main.add(new SimpleButton("Add").addClickHandler(this));
		Column<ValueType> valCol = new Column<ListValueInput.ValueType>("Value") {
			@Override
			public Object getValue(ValueType record) {
				return record.getValue();
			}
		};
		Column<ValueType> remove = new Column<ValueType>("") {
			@Override
			public Object getValue(ValueType record) {
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
		values = new Table<ListValueInput.ValueType>(false, valCol, remove);
		values.setRowClickHandler(new RowClickHandler<ValueType>() {
			public void rowClicked(ValueType record, int col, int row) {
				if (col == 1) {
					values.onRemoval(record);
					update();
				}
			}
		});
		values.setWidth("100%");
		values.getScrollPane().setHeight("150px");
		VerticalPanel master = new VerticalPanel();
		master.setWidth("100%");
		master.add(main);
		master.add(values);
		initWidget(master);
	}

	public ValueInput getValueInput(String type) {
		ValueInput inputV;
		if (type.startsWith("String")) {
			inputV = new StringValueInput();
		} else if (type.startsWith("Number")) {
			inputV = new NumberValueInput();
		} else if (type.startsWith("Selection")) {
			inputV = new SelectionValueInput(type.split(":")[1]);
		} else if (type.startsWith("Flag")) {
			inputV = new FlagValueInput();
		} else if (type.startsWith("List")) {
			inputV = new ListValueInput(type.substring(type.indexOf(":") + 1));
		} else if (type.startsWith("File")) {
			inputV = new FileValueInput();
		} else {
			inputV = new StringValueInput();
		}
		inputV.setChangeHandeler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				// input.setValue(inputV.getValue());
				// handler.onValueChange(null);
			}
		});
		return inputV;
	}

	@Override
	public void setValue(String defaultValue) {
		if (defaultValue == null || defaultValue.equals(""))
			return;
		String[] values = defaultValue.split(seperator);
		for (String value : values) {
			this.values.addRec(new ValueType(value));
		}
	}

	@Override
	public String getValue() {
		Vector<ValueType> valueData = values.getData();
		String value = "";
		for (int i = 0; i < valueData.size(); i++) {
			if (i > 0)
				value += seperator;
			value += valueData.get(i).getValue();
		}
		return value;
	}

	private static int onId = 0;

	private class ValueType extends ETAType {
		private static final long serialVersionUID = 6846101481897523222L;

		private String value;

		public ValueType(String value) {
			this.value = value;
			this.id = onId++;
		}

		public String getValue() {
			return value;
		}
	}

	public void onClick(ClickEvent event) {
		String[] newVal = input.getValues();
		if (newVal == null)
			return;
		for (String va : newVal) {
			if (va != null && !va.equals(""))
				values.addRec(new ValueType(va));
		}
		input.setValue("");
		update();
	}

}
