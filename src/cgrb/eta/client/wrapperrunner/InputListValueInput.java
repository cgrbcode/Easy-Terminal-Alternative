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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.images.Resources;
import cgrb.eta.shared.wrapper.Input;

public class InputListValueInput extends ValueInput{

	
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private FlexTable table;
	private Vector<Input> inputs = new Vector<Input>();
	private Vector<Vector<Input>> values = new Vector<Vector<Input>>();

	public InputListValueInput(String inputS){
		table=new FlexTable();
		table.setStyleName("input-list-table");
		wrapperService.getVectorObjFromJson(inputS, new MyAsyncCallback<Vector<Input>>() {
			@Override
			public void success(Vector<Input> result) {
				int col=1;
				inputs=result;
				ImgButton add = new ImgButton(Resources.INSTANCE.add(),20,"Add Input");
				add.setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						addRow();
					}
				});
				table.setCellSpacing(0);
				table.setCellPadding(0);
				table.setWidget(0, 0, add);
				for(final Input input:inputs){
					table.setText(0, col++, input.getName());
				}
			}
		});
		table.getRowFormatter().setStyleName(0, "header");
		table.getColumnFormatter().setWidth(0, "25px");
		initWidget(table);
		
	}
	@Override
	public void setValue(String defaultValue) {
		String[] lines =defaultValue.split("~~~");
		for(String line:lines){
			String[] inputs = line.split("~`~");
			for(int i=0;i<this.inputs.size();i++){
				this.inputs.get(i).setValue(inputs[i]);
			}
			addRow();
			for(int i=0;i<this.inputs.size();i++){
				this.inputs.get(i).setValue("");
			}
		}
	}
	private ValueInput getInput(String type){
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
		} else if (type.startsWith("Input-List")) {
			inputV = new InputListValueInput(type.substring(type.indexOf(":") + 1));
		} else if (type.startsWith("File")) {
			inputV = new FileValueInput();
		} else {
			inputV = new StringValueInput();
		}
		return inputV;
	}
	
	public void addRow(){
		int col=1;
		ImgButton remove = new ImgButton(Resources.INSTANCE.remove(),20,"Remove this record");
		table.setWidget(values.size(), 0, remove);
		final Vector<Input> temp = new Vector<Input>();
		for(final Input input:inputs){
			final Input newInput = input.clone();
			temp.add(newInput);
			final ValueInput inputV=getInput(input.getType());
			if (input.getValue() != null)
				inputV.setValue(input.getValue());
			inputV.setupDrop();
			inputV.setChangeHandeler(new ValueChangeHandler<String>() {
				public void onValueChange(ValueChangeEvent<String> event) {
					newInput.setValue(inputV.getValue());
					update();
				}
			});
			table.setWidget(values.size(), col++, inputV);
			table.getRowFormatter().setStyleName(values.size(), "row");
		}
		values.add(temp);
		remove.setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				table.removeRow(values.indexOf(temp));
				values.remove(temp);
				update();
			}
		});
	}

	@Override
	public String getValue() {
		String ret ="";
		for(Vector<Input> inputV:values){
			if(!ret.equals(""))
				ret+="~~~";
			String line="";
			for(Input input:inputV){
				if(!line.equals(""))
					line+="~`~";
				line+=input.getValue();
			}
			ret+=line;
		}
		return ret;
	}

}
