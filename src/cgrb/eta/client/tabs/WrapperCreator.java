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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Editor;
import cgrb.eta.client.table.FileTypeEditor;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.table.TextEditor;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;

public class WrapperCreator extends ETATab {
	private VerticalPanel pane;
	private TextBox nameBox;
	private SuggestBox programBox;
	private TextArea descriptionBox;
	private InputsTable inputs;
	private Table<Output> outputs;
	private int inputsInt = -1;
	private int outputsInt = -1;
	private int wrapperId = 0;
	private HorizontalPanel bar;
	private WrapperRunner runner;
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private HashMap<String, String> envVars = new HashMap<String, String>();
	MultiWordSuggestOracle oracle;

	public WrapperCreator() {
		super("Wrapper Editor");
		setup();
	}

	@Override
	public Widget getBar() {
		return bar;
	}

	public void setup() {
		bar = new HorizontalPanel();
		bar.add(new Button("Save").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				saveWrapper();
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("Preview").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				previewWrapper();
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("Set Env Vars").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setEnvVars();
			}
		}));
		bar.add(new Seprator());

		pane = new VerticalPanel() {
			protected void onLoad() {
				super.onLoad();
				int height = (Window.getClientHeight() - inputs.getScrollPane().getAbsoluteTop() - 216);
				if (height < 150)
					height = 150;
				inputs.getScrollPane().setHeight(height + "px");
				Window.addResizeHandler(new ResizeHandler() {
					public void onResize(ResizeEvent event) {
						int height = (Window.getClientHeight() - inputs.getScrollPane().getAbsoluteTop() - 216);
						if (height < 150)
							height = 150;
						inputs.getScrollPane().setHeight(height + "px");
					}
				});
			};
		};
		FlexTable info = new FlexTable();
		info.setWidth("100%");
		info.setStyleName("request-grid");
		info.setText(0, 0, "Name:");
		info.setText(1, 0, "Program:");
		nameBox = new TextBox();
		oracle = new MultiWordSuggestOracle();
		programBox = new SuggestBox(oracle);
		descriptionBox = new TextArea();
		nameBox.setStyleName("eta-input2");
		programBox.setStyleName("eta-input2");
		descriptionBox.setStyleName("eta-input2");
		info.setWidget(0, 1, nameBox);
		info.setWidget(1, 1, programBox);
		info.setText(0, 2, "Description:");
		info.setWidget(0, 3, descriptionBox);
		info.getFlexCellFormatter().setRowSpan(0, 3, 2);
		info.getColumnFormatter().setWidth(0, "50px");
		info.getColumnFormatter().setWidth(1, "200px");
		info.getColumnFormatter().setWidth(2, "50px");
		descriptionBox.setHeight("100%");
		pane.add(info);
		HorizontalPanel inputsHeader = new HorizontalPanel();
		HorizontalPanel tempP = new HorizontalPanel();
		inputsHeader.setStyleName("header");
		inputsHeader.setWidth("100%");
		tempP.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		tempP.add(new Filler(20));
		tempP.add(new SimpleLabel("Inputs"));
		tempP.add(new Filler(10));
		tempP.add(new ImgButton(Resources.INSTANCE.add(), "Add new Input").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Input newInput = new Input();
				newInput.setId(inputsInt--);
				newInput.setName("Untitled " + ((inputsInt * -1) - 1));
				addInput(newInput);
			}
		}));
		inputsHeader.add(tempP);
		pane.add(inputsHeader);
		inputs = new InputsTable(false);
		pane.add(inputs);
		VerticalPanel temp = new VerticalPanel();
		pane.setWidth("100%");
		temp.add(pane);
		setPane(temp);
		communicationService.getCommandsInPath(new MyAsyncCallback<String[]>() {
			@Override
			public void success(String[] result) {
				for (String program : result)
					oracle.add(program);
			}
		});

		HorizontalPanel outputsHeader = new HorizontalPanel();
		HorizontalPanel tempO = new HorizontalPanel();
		outputsHeader.setStyleName("header");
		outputsHeader.setWidth("100%");
		tempO.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		tempO.add(new Filler(20));
		tempO.add(new SimpleLabel("Outputs"));
		tempO.add(new Filler(10));
		tempO.add(new ImgButton(Resources.INSTANCE.add(), "Add new Output").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Output temp = new Output();
				temp.setName("Untitled" + (outputsInt * -1));
				temp.setId(outputsInt--);
				addOutput(temp);
			}
		}));
		outputsHeader.add(tempO);
		pane.add(outputsHeader);

		Column<Output> removeO = new Column<Output>("") {
			@Override
			public Object getValue(Output record) {
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
		Column<Output> nameO = new Column<Output>("Name") {
			@Override
			public Object getValue(Output record) {
				return record.getName();
			}

			@Override
			public Editor getEditor(Output rec) {
				return new TextEditor(rec.getName());
			}

			@Override
			public void setValue(Output rec, String newVal) {
				rec.setName(newVal);
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<Output> typeO = new Column<Output>("File Type") {
			@Override
			public Object getValue(Output record) {
				return record.getType();
			}

			@Override
			public Editor getEditor(Output rec) {
				return new FileTypeEditor(rec.getType());
			}

			@Override
			public void setValue(Output rec, String newVal) {
				rec.setType(newVal);
			}

			@Override
			public String getWidth() {
				return "120px";
			}
		};
		Column<Output> descriptionO = new Column<Output>("Description") {
			@Override
			public Object getValue(Output record) {
				return record.getDescription();
			}

			@Override
			public Editor getEditor(Output rec) {
				return new TextEditor(rec.getDescription());
			}

			@Override
			public void setValue(Output rec, String newVal) {
				rec.setDescription(newVal);
			}
		};
		Column<Output> valueO = new Column<Output>("Value") {
			@Override
			public Object getValue(Output record) {
				return record.getValue();
			}

			@Override
			public String getWidth() {
				return "200px";
			}

			@Override
			public Editor getEditor(Output rec) {
				return new TextEditor(rec.getValue());
			}

			@Override
			public void setValue(Output rec, String newVal) {
				rec.setValue(newVal);
			}
		};
		outputs = new Table<Output>(false, removeO, nameO, typeO, descriptionO, valueO);
		outputs.getScrollPane().setHeight("150px");
		outputs.setWidth("100%");
		outputs.setCanEdit(true);
		outputs.setRowClickHandler(new RowClickHandler<Output>() {
			public void rowClicked(Output record, int col, int row) {
				if (col == 0)
					outputs.onRemoval(record);
			}
		});
		pane.add(outputs);
	}

	public class Item extends ETAType {
		private static final long serialVersionUID = 4989801643100966361L;
		private String value;
		private String name;

		public Item(String name, String value, int id) {
			this.id = id;
			this.value = value;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

	int recs = 0;

	protected void setEnvVars() {
		Grid grid = new Grid(2, 3);
		final TextBox name = new TextBox();
		final TextBox value = new TextBox();
		name.setStyleName("eta-input2");
		value.setStyleName("eta-input2");

		SimpleButton add = new SimpleButton("Add");

		grid.setText(0, 0, "Name");
		grid.setText(1, 0, "Value");
		grid.setWidget(0, 1, name);
		grid.setWidget(1, 1, value);
		grid.setWidget(1, 2, add);

		Column<Item> nameCol = new Column<Item>("Name") {
			@Override
			public Object getValue(Item record) {
				return record.getName();
			}
		};
		Column<Item> valueCol = new Column<Item>("Value") {
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

		final Table<Item> table = new Table<Item>(false, remove, nameCol, valueCol);
		table.setRowClickHandler(new RowClickHandler<Item>() {
			public void rowClicked(Item record, int col, int row) {
				if (col == 0) {
					table.onRemoval(record);
				}
			}
		});
		add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (name.getValue() != null && !name.getValue().equals("") && value.getValue() != null && !value.getValue().equals("")) {
					Item newItem = new Item(name.getValue(), value.getValue(), recs++);
					table.addRec(newItem);
				}
			}
		});
		table.setWidth("200px");
		table.getScrollPane().setHeight("400px");
		VerticalPanel panel = new VerticalPanel();
		panel.add(grid);
		panel.add(table);
		recs = 0;
		if (envVars != null) {
			Iterator<String> it = envVars.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				Item newItem = new Item(key, envVars.get(key), recs++);
				table.addRec(newItem);
			}
		} else {
			envVars = new HashMap<String, String>();
		}
		SC.ask("Set Enviroment Variables", panel, new ValueListener<Boolean>() {
			public void returned(Boolean ret) {
				if (ret) {
					Vector<Item> data = table.getData();
					envVars.clear();
					for (Item it : data) {
						envVars.put(it.getName(), it.getValue());
					}
				}
			}
		});
	}

	public void saveWrapper() {
		Wrapper temp = updateWrapper();
		if (temp.getName() == null || temp.getDescription() == null || temp.getProgram() == null || temp.getName().equals("") || temp.getDescription().equals("") || temp.getProgram().equals("")) {
			SC.alert("Error", "You must fill in the name, description and program for a wrapper");
			return;
		}
		if (temp != null) {
			wrapperService.saveWrapper(temp, new MyAsyncCallback<Wrapper>() {
				@Override
				public void success(Wrapper result) {
					loadWrapper(result);
				}
			});
		}
	}

	private void saveAsWrapper() {
		Wrapper temp = updateWrapper();
		temp.setId(0);
		if (temp.getName() == null || temp.getDescription() == null || temp.getProgram() == null || temp.getName().equals("") || temp.getDescription().equals("") || temp.getProgram().equals("")) {
			SC.alert("Error", "You must fill in the name, description and program for a wrapper");
			return;
		}
		temp.setPublic(false);
		if (temp != null) {
			wrapperService.saveWrapper(temp, new MyAsyncCallback<Wrapper>() {
				@Override
				public void success(Wrapper result) {
					loadWrapper(result);
				}
			});
		}
	}

	public Wrapper updateWrapper() {

		Wrapper newWrapper = new Wrapper();
		newWrapper.setId(wrapperId);
		newWrapper.setName(nameBox.getValue());
		newWrapper.setCreator(ETA.getInstance().getUser().getName());
		newWrapper.setCreatorId(ETA.getInstance().getUser().getId());
		newWrapper.setProgram(programBox.getValue());
		newWrapper.setDescription(descriptionBox.getValue());
		int order = 0;
		Vector<Input> inputsA = inputs.getData();
		for (Input input : inputsA) {
			input.setOrder(order++);
			if (input.getName() == null || input.getDescription() == null || input.getName().length() == 0 || input.getDescription().length() == 0) {
				SC.alert("Error", "Sorry all inputs must have a name and description.");
				return null;
			}
			newWrapper.addInput(input);
		}
		newWrapper.setEnvVars(envVars);
		Vector<Output> outputs = this.outputs.getData();
		for (Output output : outputs) {
			newWrapper.addOutput(output);
		}
		return newWrapper;
	}

	public void previewWrapper() {
		if (runner == null) {
			runner = new WrapperRunner(0);
		}
		Wrapper temp = updateWrapper();
		if (temp != null) {
			runner.setup(temp);
			ETA.getInstance().addTab(runner);
		}
	}

	public void loadWrapper(Wrapper wrapper) {
		wrapperId = wrapper.getId();
		nameBox.setValue(wrapper.getName());
		programBox.setValue(wrapper.getProgram());
		descriptionBox.setValue(wrapper.getDescription());
		outputs.setData(wrapper.getOutputs());
		inputs.setData(wrapper.getInputs());
		User user = ETA.getInstance().getUser();

		envVars = wrapper.getEnvVars();

		bar.clear();
		if (user.getId() == wrapper.getCreatorId() || user.getPermissionLevel() >= 8) {
			bar.add(new Button("Save").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					saveWrapper();
				}
			}));
			bar.add(new Seprator());
			bar.add(new Button("Save As").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					saveAsWrapper();
				}
			}));
			bar.add(new Seprator());
			bar.add(new Button("Generate Perl template").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					new FileSelector(new ItemSelector() {
						public void itemSelected(final String[] items) {
							if (items == null || items.length == 0)
								return;
							wrapperService.generatePerlTemplate(wrapperId, items[0], new MyAsyncCallback<String>() {
								@Override
								public void success(String result) {
									SC.alert("Template Created", "You perl template has been created in " + items[0]);
								}
							});
						}
					}, FileBrowser.FOLDER_SELECT);

				}
			}));
		} else {
			bar.add(new Button("Save As").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					saveAsWrapper();
				}
			}));
		}
		bar.add(new Seprator());
		bar.add(new Button("Preview").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				previewWrapper();
			}
		}));
		bar.add(new Seprator());
		bar.add(new Button("Set Env Vars").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setEnvVars();
			}
		}));
		bar.add(new Seprator());
	}

	@Override
	public String getId() {
		return "wc#" + wrapperId;
	}

	public void addInput(Input input) {
		inputs.addInput(input);
	}

	public void addOutput(Output output) {
		outputs.onAddition(output);
	}

	/**
	 * @param string
	 */
	public void setWrapper(String string) {
		if (string.equals("0")) {

		} else {
			wrapperId = Integer.parseInt(string);
			wrapperService.getWrapperFromId(wrapperId, new MyAsyncCallback<Wrapper>() {
				@Override
				public void success(Wrapper result) {
					loadWrapper(result);
				}
			});
		}
	}
}
