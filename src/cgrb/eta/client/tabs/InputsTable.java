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

import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.BooleanEditor;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.table.Editor;
import cgrb.eta.client.table.HtmlEditor;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.SelectEditor;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.table.TextEditor;
import cgrb.eta.client.table.TypeEditor;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.wrapper.Input;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

public class InputsTable extends Composite implements DragListener {
	Table<Input> inputs;
	private int mouseY;

	public InputsTable(boolean simple) {
		Column<Input> remove = new Column<Input>("") {
			@Override
			public Object getValue(Input record) {
				Image img = new Image(Resources.INSTANCE.remove().getSafeUri().asString());
				img.setWidth("16px");
				img.setHeight("16px");
				img.setStyleName("button");
				return img;
			}

			@Override
			public String getWidth() {
				return "20px";
			}
		};
		Column<Input> name = new Column<Input>("Name") {
			@Override
			public Object getValue(Input record) {
				return record.getName();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new TextEditor(rec.getName());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setName(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		
		Column<Input> type = new Column<Input>("Type") {
			@Override
			public Object getValue(Input record) {
				String type = record.getType();
				if (type != null && type.contains(":")) {
					return type.split(":")[0];
				}
				return record.getType();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new TypeEditor(rec.getType());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setType(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<Input> description = new Column<Input>("Description") {
			@Override
			public Object getValue(Input record) {
				return record.getDescription();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new HtmlEditor(rec.getDescription());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setDescription(newVal);
			}
		};
		Column<Input> value = new Column<Input>("Default Value") {
			@Override
			public Object getValue(Input record) {
				return record.getDefaultValue();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new TextEditor(rec.getDefaultValue());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setDefaultValue(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<Input> flag = new Column<Input>("Flag") {
			@Override
			public Object getValue(Input record) {
				return record.getFlag();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new TextEditor(rec.getFlag());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setFlag(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<Input> cat = new Column<Input>("Category") {
			@Override
			public Object getValue(Input record) {
				return record.getDisplayType();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new SelectEditor(rec.getDisplayType(),"Default", "Advanced", "Hidden");
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setDisplayType(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<Input> required = new Column<Input>("Required") {
			@Override
			public Object getValue(Input record) {
				return "" + record.isRequired();
			}

			@Override
			public Editor getEditor(Input rec) {
				return new BooleanEditor(rec.isRequired());
			}

			@Override
			public void setValue(Input rec, String newVal) {
				rec.setRequired(Boolean.parseBoolean(newVal));
			}

			@Override
			public String getWidth() {
				return "70px";
			}
		};

		if(simple)
			inputs = new Table<Input>(false, remove, name, type,description, value, flag, required);
		else
		inputs = new Table<Input>(false, remove, name, type, description, value, flag, cat, required);
		inputs.setDragListener(this);
		inputs.setRowClickHandler(new RowClickHandler<Input>() {
			public void rowClicked(Input record, int col, int row) {
				if (col == 0)
					inputs.onRemoval(record);
			}
		});
		inputs.setWidth("100%");
		inputs.setCanEdit(true);
		initWidget(inputs);

		inputs.addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent event) {
				mouseY = event.getClientY();
			}
		}, MouseMoveEvent.getType());

	}

	
	public void addInput(Input input) {
		inputs.addRec(input);
	}

	public void dragStart(ETAType record) {
		Input rec = (Input) record;
		inputs.getElementForRecord(rec).getStyle().setOpacity(.5);
		inputs.cancelEdit();
	}

	public void dragEnter(ETAType record) {

	}
	
	public FlowPanel getScrollPane(){
		return inputs.getScrollPane();
	}

	public void dragOver(ETAType record) {
		if (DragCreator.getDragSource() instanceof Input) {

			Input rec = (Input) record;
			Element el = inputs.getElementForRecord(rec);
			int top = el.getAbsoluteTop();
			int bottom = el.getAbsoluteBottom();
			int height = bottom - top;

			if (mouseY < bottom - (height / 2)) {
				inputs.getElementForRecord(rec).removeClassName("input-over-top");
				el.addClassName("input-over-bottom");
			} else {
				inputs.getElementForRecord(rec).removeClassName("input-over-bottom");
				el.addClassName("input-over-top");
			}
		}
	}

	public void dragLeave(ETAType record) {
		Input rec = (Input) record;
		inputs.getElementForRecord(rec).removeClassName("input-over-top");
		inputs.getElementForRecord(rec).removeClassName("input-over-bottom");
	}

	public void drop(ETAType record) {
		Input rec = (Input) record;
		inputs.getElementForRecord(rec).removeClassName("input-over-top");
		inputs.getElementForRecord(rec).removeClassName("input-over-bottom");
		if (DragCreator.getDragSource() instanceof Input) {
			inputs.moveRecord((Input) DragCreator.getDragSource(), inputs.getRowForRec(rec));
		}

	}
	
	public void setData(Vector<Input> inputs){
		this.inputs.setData(inputs);
	}
	
	public void dragEnd(ETAType record) {
		Input rec = (Input) record;
		inputs.getElementForRecord(rec).removeClassName("input-over-top");
		inputs.getElementForRecord(rec).removeClassName("input-over-bottom");
		inputs.getElementForRecord((Input) DragCreator.getDragSource()).getStyle().setOpacity(1);

	}

	public Vector<Input> getData() {
		return inputs.getData();
	}

	public Element getDragImage(ETAType record) {
		return DragCreator.getImageElement("../images/file.png");
	}
}
