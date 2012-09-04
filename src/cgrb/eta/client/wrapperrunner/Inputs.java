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

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import cgrb.eta.client.pipeline.OutputBlock;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Inputs extends Composite {
	private Wrapper wrapper;
	private ValueChangeHandler<Wrapper> handler;
	private ListValueInput loopValue;

	public Inputs(Wrapper wrapper) {
		this.wrapper = wrapper;
		FlexTable inputsGrid = new FlexTable();
		FlexTable advancedGrid = new FlexTable();
		VerticalPanel pane = new VerticalPanel();
		pane.add(inputsGrid);
		Vector<Input> inputs = wrapper.getInputs();
		Collections.sort(inputs, new Comparator<Input>() {
			public int compare(Input o1, Input o2) {
				if (o2.isRequired() && !o1.isRequired())
					return 1;
				if (o1.isRequired() && !o2.isRequired())
					return -1;
				return o1.compareTo(o2);
			}
		});
		int rows = 1;
		int advancedRows = 1;

		inputsGrid.setCellSpacing(0);
		advancedGrid.setCellSpacing(0);
		for (final Input input : inputs) {
			if (input.getDisplayType() == null || input.getDisplayType().equals("Default")) {
				rows++;
				if (rows % 2 == 0) {
					inputsGrid.getRowFormatter().setStyleName(rows - 2, "alt");
				}
				if (input.isRequired()) {
					inputsGrid.setHTML(rows - 2, 0, "<div class='req'>*</div>" + input.getName());
				} else
					inputsGrid.setText(rows - 2, 0, input.getName());
				String type = input.getType();
				final ValueInput inputV;
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
				if (input.getValue() != null)
					inputV.setValue(input.getValue());
				inputV.setupDrop();
				inputV.setChangeHandeler(new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> event) {
						input.setValue(inputV.getValue());
						if (handler != null)
							handler.onValueChange(null);
					}
				});
				if (type.startsWith("Input-List")) {
					inputsGrid.setWidget(rows - 2, 1, inputV);
					inputsGrid.getFlexCellFormatter().setColSpan(rows - 2, 1, 2);
				} else {
					inputsGrid.setWidget(rows - 2, 2, inputV);
					inputsGrid.setHTML(rows - 2, 1, input.getDescription());
				}
			} else if (input.getDisplayType().equals("Advanced")) {
				advancedRows++;
				if (advancedRows % 2 == 0) {
					advancedGrid.getRowFormatter().setStyleName(advancedRows - 2, "alt");
				}
				if (input.isRequired()) {
					advancedGrid.setHTML(advancedRows - 2, 0, "<div class='req'>*</div>" + input.getName());
				} else
					advancedGrid.setText(advancedRows - 2, 0, input.getName());
				String type = input.getType();
				final ValueInput inputV;
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
				if (input.getValue() != null)
					inputV.setValue(input.getValue());
				inputV.setChangeHandeler(new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> event) {
						input.setValue(inputV.getValue());
						handler.onValueChange(null);
					}
				});
				if (type.startsWith("Input-List")) {
					advancedGrid.setWidget(advancedRows - 2, 0, inputV);
					advancedGrid.getFlexCellFormatter().setColSpan(advancedRows - 2, 0, 3);
				} else {
					advancedGrid.setWidget(advancedRows - 2, 2, inputV);
					advancedGrid.setHTML(advancedRows - 2, 1, input.getDescription());
				}

			}
		}

		inputsGrid.getColumnFormatter().setWidth(0, "150px");
		inputsGrid.getColumnFormatter().setWidth(2, "250px");
		advancedGrid.getColumnFormatter().setWidth(0, "150px");
		advancedGrid.getColumnFormatter().setWidth(2, "250px");
		if (advancedRows > 1) {
			DisclosurePanel advancePanel = new DisclosurePanel("Advanced Inputs");
			advancePanel.setWidth("100%");

			advancePanel.add(advancedGrid);
			pane.add(advancePanel);
		}
		inputsGrid.setWidth("100%");
		advancedGrid.setWidth("100%");
		pane.setHeight("100%");
		pane.setWidth("100%");
		initWidget(pane);
	}

	public Inputs(Wrapper wrapper, int as) {
		this.wrapper = wrapper;
		Grid inputsGrid = new Grid(0, 3);
		Vector<Input> inputs = wrapper.getInputs();
		Collections.sort(inputs, new Comparator<Input>() {
			public int compare(Input o1, Input o2) {
				if (o2.isRequired())
					return 1;
				if (o1.isRequired())
					return -1;
				return o1.compareTo(o2);
			}
		});
		int rows = 1;
		inputsGrid.setCellSpacing(0);
		for (final Input input : inputs) {
			inputsGrid.resizeRows(rows++);
			if (rows % 2 == 0) {
				inputsGrid.getRowFormatter().setStyleName(rows - 2, "alt");
			}
			if (input.isRequired()) {
				inputsGrid.setHTML(rows - 2, 0, "<div class='req'>*</div>" + input.getName());
			} else
				inputsGrid.setText(rows - 2, 0, input.getName());
			final ValueInput inputV;
			inputV = new StringValueInput();
			if (input.getDefaultValue() != null)
				inputV.setValue(input.getValue());

			inputsGrid.setWidget(rows - 2, 2, inputV);

			inputsGrid.setHTML(rows - 2, 1, input.getDescription());
		}

		inputsGrid.getColumnFormatter().setWidth(0, "150px");
		inputsGrid.getColumnFormatter().setWidth(2, "250px");
		inputsGrid.setWidth("100%");
		initWidget(inputsGrid);
	}

	/**
	 * @param pipeline
	 */
	public Inputs(Pipeline pipeline) {
		Grid inputsGrid = new Grid(0, 3);
		int rows = 1;
		inputsGrid.setCellSpacing(0);

		Vector<Input> inputs = pipeline.getInputs();
		Collections.sort(inputs, new Comparator<Input>() {
			public int compare(Input o1, Input o2) {
				if (o2.isRequired())
					return 1;
				if (o1.isRequired())
					return -1;
				return o1.compareTo(o2);
			}
		});
		for (final Input input : inputs) {
				if (input.getValue().equals("$'User Input'"))
					input.setValue("");
				inputsGrid.resizeRows(rows++);
				if (rows % 2 == 0) {
					inputsGrid.getRowFormatter().setStyleName(rows - 2, "alt");
				}
				if (input.isRequired()) {
					inputsGrid.setHTML(rows - 2, 0, "<div class='req'>*</div>" +  input.getName());
				} else
					inputsGrid.setText(rows - 2, 0,  input.getName());
				String type = input.getType();
				final ValueInput inputV;
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
				if (input.getValue() != null)
					inputV.setValue(input.getValue());
				inputV.setChangeHandeler(new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> event) {
						input.setValue(inputV.getValue());
						if (handler != null)
							handler.onValueChange(null);
					}
				});
				inputsGrid.setWidget(rows - 2, 2, inputV);
				inputsGrid.setHTML(rows - 2, 1, input.getDescription());
		}
		inputsGrid.getColumnFormatter().setWidth(0, "150px");
		inputsGrid.getColumnFormatter().setWidth(2, "250px");
		inputsGrid.setWidth("100%");
		initWidget(inputsGrid);
	}

	/**
	 * @param wrapper2
	 * @param b
	 */
	public Inputs(Wrapper wrapper, boolean b) {
		this.wrapper = wrapper;
		Grid inputsGrid = new Grid(0, 3);
		Grid advancedGrid = new Grid(0, 3);
		VerticalPanel pane = new VerticalPanel();
		pane.add(inputsGrid);
		Vector<Input> inputs = wrapper.getInputs();
		Collections.sort(inputs, new Comparator<Input>() {
			public int compare(Input o1, Input o2) {
				if (o2.isRequired() && !o1.isRequired())
					return 1;
				if (o1.isRequired() && !o2.isRequired())
					return -1;
				return o1.compareTo(o2);
			}
		});
		int rows = 1;
		int advancedRows = 1;

		inputsGrid.setCellSpacing(0);
		advancedGrid.setCellSpacing(0);

		inputsGrid.resizeRows(rows++);
		if (rows % 2 == 0) {
			inputsGrid.getRowFormatter().setStyleName(rows - 2, "alt");
		}
		inputsGrid.setHTML(rows - 2, 0, "<div class='req'>*</div>Loop Items");
		loopValue = new ListValueInput("File{,}");
		inputsGrid.setWidget(rows - 2, 2, loopValue);
		OutputBlock block = new OutputBlock(new Output("Loop Item", "File", "Represents each item in this list", "$'Loop Item'", -1));
		FlowPanel tempPane = new FlowPanel();
		tempPane.add(new HTML("To make Inputs below use the items in this list drag the gold block below in a string or file field. Additionally you can append/prepend text to the input."));
		tempPane.add(block);
		block.setWidth("100px");
		inputsGrid.setWidget(rows - 2, 1, tempPane);

		for (final Input input : inputs) {
			if (input.getDisplayType() == null || input.getDisplayType().equals("Default")) {
				inputsGrid.resizeRows(rows++);
				if (rows % 2 == 0) {
					inputsGrid.getRowFormatter().setStyleName(rows - 2, "alt");
				}
				if (input.isRequired()) {
					inputsGrid.setHTML(rows - 2, 0, "<div class='req'>*</div>" + input.getName());
				} else
					inputsGrid.setText(rows - 2, 0, input.getName());
				String type = input.getType();
				final ValueInput inputV;
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
				if (input.getValue() != null)
					inputV.setValue(input.getValue());
				inputV.setupDrop();
				inputV.setChangeHandeler(new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> event) {
						input.setValue(inputV.getValue());
						if (handler != null)
							handler.onValueChange(null);
					}
				});
				inputsGrid.setWidget(rows - 2, 2, inputV);
				inputsGrid.setHTML(rows - 2, 1, input.getDescription());
			} else if (input.getDisplayType().equals("Advanced")) {
				advancedGrid.resizeRows(advancedRows++);
				if (advancedRows % 2 == 0) {
					advancedGrid.getRowFormatter().setStyleName(advancedRows - 2, "alt");
				}
				if (input.isRequired()) {
					advancedGrid.setHTML(advancedRows - 2, 0, "<div class='req'>*</div>" + input.getName());
				} else
					advancedGrid.setText(advancedRows - 2, 0, input.getName());
				String type = input.getType();
				final ValueInput inputV;
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
				if (input.getValue() != null)
					inputV.setValue(input.getValue());
				inputV.setChangeHandeler(new ValueChangeHandler<String>() {
					public void onValueChange(ValueChangeEvent<String> event) {
						input.setValue(inputV.getValue());
						handler.onValueChange(null);
					}
				});
				advancedGrid.setWidget(advancedRows - 2, 2, inputV);
				advancedGrid.setHTML(advancedRows - 2, 1, input.getDescription());
			}
		}

		inputsGrid.getColumnFormatter().setWidth(0, "150px");
		inputsGrid.getColumnFormatter().setWidth(2, "250px");
		advancedGrid.getColumnFormatter().setWidth(0, "150px");
		advancedGrid.getColumnFormatter().setWidth(2, "250px");
		if (advancedRows > 1) {
			DisclosurePanel advancePanel = new DisclosurePanel("Advanced Inputs");
			advancePanel.setWidth("100%");

			advancePanel.add(advancedGrid);
			pane.add(advancePanel);
		}
		inputsGrid.setWidth("100%");
		advancedGrid.setWidth("100%");
		pane.setHeight("100%");
		pane.setWidth("100%");
		initWidget(pane);
	}

	public void setHandler(ValueChangeHandler<Wrapper> hand) {
		this.handler = hand;
	}

	public ListValueInput getLoopInput() {
		return loopValue;
	}

	public Wrapper getWrapper() {
		return wrapper;
	}
}
