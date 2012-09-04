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
package cgrb.eta.client;

import java.util.Vector;

import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ImgMenuButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.pipeline.PipelineCreator;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.table.TreeTable;
import cgrb.eta.client.tabs.PipelineRunner;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.pipeline.Pipeline;
import cgrb.eta.shared.pipeline.UserPipeline;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class UserPipelineGrid extends Composite implements RowClickHandler<UserPipeline>, DragListener {
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private ItemSelector selector;
	TreeTable<UserPipeline> pipelines;
	private boolean overChild = false;

	public UserPipelineGrid(boolean resize) {

		final Column<UserPipeline> icon = new Column<UserPipeline>("") {
			@Override
			public Object getValue(UserPipeline record) {
				Image icon = new Image();
				if (record.getPipelineId() == 0) {
					icon.setUrl(Resources.INSTANCE.folder().getSafeUri().asString());
				} else if (record.isPublic()) {
					icon.setUrl(Resources.INSTANCE.publicPipeline().getSafeUri().asString());
				} else {
					icon.setUrl(Resources.INSTANCE.pipeline().getSafeUri().asString());
				}
				icon.setWidth("16px");
				icon.setHeight("16px");
				return icon;
			}

			@Override
			public String getWidth() {
				return "20px";
			}
		};
		Column<UserPipeline> name = new Column<UserPipeline>("Name") {
			@Override
			public Object getValue(UserPipeline record) {
				HorizontalPanel panel = new HorizontalPanel();
				panel.add((Widget) icon.getValue(record));
				panel.add(new Label(record.getName()));
				return panel;
			}

			@Override
			public int compareTo(UserPipeline o1, UserPipeline o2) {
				if (o1.getPipelineId() == 0)
					return -1;
				if (o2.getPipelineId() == 0)
					return 1;
				return o1.getName().compareTo(o2.getName());
			}
		};

		if (resize)
			pipelines = new TreeTable<UserPipeline>(resize, name) {
				@Override
				public int getParent(UserPipeline record) {
					return record.getParent();
				}

				@Override
				public boolean isFolder(UserPipeline record) {
					return record.getPipelineId() == 0;
				}
			};
		else
			pipelines = new TreeTable<UserPipeline>(resize, name) {
				@Override
				public int getParent(UserPipeline record) {
					return record.getParent();
				}

				@Override
				public boolean isFolder(UserPipeline record) {
					return record.getPipelineId() == 0;
				}
			};

		pipelines.addAction(new ImgButton(Resources.INSTANCE.redo(), 20, "Run Pipeline").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab(new PipelineRunner(pipelines.getSelection().get(0).getPipelineId()));
			}
		}), Table.SINGLE_SELECT);
		pipelines.addAction(new ImgButton(Resources.INSTANCE.edit(), 20, "Edit Pipeline").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				PipelineCreator creator = new PipelineCreator();
				creator.setPipeline(pipelines.getSelection().get(0).getPipelineId());
				ETA.getInstance().addTab(creator);
			}
		}), Table.SINGLE_SELECT);
		ImgMenuButton share = new ImgMenuButton(Resources.INSTANCE.share());
		share.setTitle("Share");
		share.addButton(new Button("With someone").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final MultipleUserSelect users = new MultipleUserSelect();
				SC.ask("Select users to share with", users, new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret) {
							Vector<User> sharingWith = users.getUsers();
							for (User user : sharingWith) {
								for (UserPipeline wrap : pipelines.getSelection())
									wrapperService.sharePipeline(wrap.getPipelineId(), user.getId(), wrap.getName(), new MyAsyncCallback<Void>() {
										@Override
										public void success(Void result) {
										}
									});
							}
						}
					}
				});
			}
		}));

		share.addButton(new Button("Make public").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (UserPipeline wrap : pipelines.getSelection()) {
					if (!wrap.isPublic() && wrap.getAuthor().equals(ETA.getInstance().getUser().getName())) {
						wrapperService.makePipelinePublic(wrap.getPipelineId(), new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					}
				}
			}
		}));
		share.setSize(20);
		pipelines.addAction(share, Table.MULTIPLE_SELECT);
		pipelines.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Remove Pipeline").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (UserPipeline wrap : pipelines.getSelection()) {
					wrapperService.movePipeline(wrap.getId(), 0, -1, "", wrap.getPipelineId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);

		pipelines.setEmptyString("Search for Pipelines and drag them into here.");
		pipelines.addListener(UserPipeline.class);
		pipelines.setDragListener(this);
		initWidget(pipelines);
		wrapperService.getUsersPipelines(new MyAsyncCallback<Vector<UserPipeline>>() {
			public void success(Vector<UserPipeline> result) {
				pipelines.setData(result);
			};
		});
		pipelines.setRowClickHandler(this);
		DragCreator.addDrop(pipelines.getScrollPane().getElement(), null, new DropListener() {
			public void drop(ETAType record) {
				ETAType dragSource = DragCreator.getDragSource();
				if (dragSource instanceof Pipeline) {
					// dropping a new wrapper here
					int parent = 0;
					wrapperService.movePipeline(0, 0, parent, "", ((Pipeline) dragSource).getId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				} else if (dragSource instanceof UserPipeline) {
					UserPipeline old = (UserPipeline) dragSource;
					wrapperService.movePipeline(old.getId(), old.getParent(), 0, old.getName(), dragSource.getId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				} else {
					// not compatible!
				}
				pipelines.getScrollPane().getElement().removeClassName("drag-over");
			}

			public void dragOver(ETAType record) {
				if (!overChild)
					pipelines.getScrollPane().getElement().addClassName("drag-over");
			}

			public void dragLeave(ETAType record) {
				pipelines.getScrollPane().getElement().removeClassName("drag-over");
			}

			public void dragEnter(ETAType record) {

			}
		});
	}

	private UserPipeline selected;

	public void rowClicked(UserPipeline record, int col, int row) {
		if (selector != null) {
			// a selection is in proccess
			if (selected == null) {
				selected = record;
				pipelines.getElementForRecord(record).addClassName("selected");
			} else {
				if (selected.equals(record)) {
					// this has been clicked twice assume this is what the user wanted to select
					selector.itemSelected(new String[] { selected.getPipelineId() + "" });
					pipelines.getElementForRecord(record).addClassName("selected");
				} else {
					pipelines.getElementForRecord(selected).removeClassName("selected");
					pipelines.getElementForRecord(record).addClassName("selected");
					selected = record;
				}
			}
		}
	}

	public void dragStart(ETAType record) {
		Element el = pipelines.getElementForRecord((UserPipeline) record);
		el.getStyle().setOpacity(.5);
	}

	public void dragEnter(ETAType rec) {

	}

	public void dragOver(ETAType rec) {
		pipelines.getScrollPane().getElement().removeClassName("drag-over");
		ETAType dragSource = DragCreator.getDragSource();
		if (dragSource instanceof Pipeline) {

		} else if (dragSource instanceof UserPipeline) {

		} else {
			// not compatible!
			return;
		}

		UserPipeline record = (UserPipeline) rec;
		if (record.getPipelineId() == 0) {
			Element el = pipelines.getElementForRecord(record);
			el.addClassName("drag-over");
		} else {
			// get the parent
			int parentId = pipelines.getParent(record);
			if (parentId == 0) {
				pipelines.getScrollPane().getElement().addClassName("drag-over");
			} else {
				pipelines.getElementForRecord(parentId).addClassName("drag-over");
			}
		}
		overChild = true;
	}

	public void dragLeave(ETAType rec) {
		overChild = false;
		UserPipeline record = (UserPipeline) rec;
		if (record.getPipelineId() == 0) {
			Element el = pipelines.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = pipelines.getParent(record);
			if (parentId == 0) {
				pipelines.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				pipelines.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}
	}

	public void drop(ETAType rec) {
		UserPipeline record = (UserPipeline) rec;
		if (record.getPipelineId() == 0) {
			Element el = pipelines.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = pipelines.getParent(record);
			if (parentId == 0) {
				pipelines.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				pipelines.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}

		ETAType dragSource = DragCreator.getDragSource();
		int parent = rec.getId();
		if (record.getPipelineId() > 0)
			parent = record.getParent();
		if (dragSource instanceof Pipeline) {
			// dropping a new wrapper here
			wrapperService.movePipeline(0, 0, parent, ((Pipeline) dragSource).getName(), ((Pipeline) dragSource).getId(), new MyAsyncCallback<Integer>() {
				@Override
				public void success(Integer result) {
				}
			});
		} else if (dragSource instanceof UserPipeline) {
			// looks like we are moving a wrapper
			UserPipeline old = (UserPipeline) dragSource;
			wrapperService.movePipeline(old.getId(), old.getParent(), parent, old.getName(), old.getPipelineId(), new MyAsyncCallback<Integer>() {
				@Override
				public void success(Integer result) {
				}
			});
		} else {
			// not compatible!
		}
	}

	public void dragEnd(ETAType record) {
		pipelines.getScrollPane().getElement().removeClassName("drag-over");
		Element el = pipelines.getElementForRecord((UserPipeline) record);
		el.getStyle().setOpacity(1);
	}

	public Element getDragImage(ETAType rec) {
		UserPipeline record = (UserPipeline) rec;
		if (record.getPipelineId() == 0) {
			return DragCreator.getImageElement("../images/folder.png");
		} else if (record.isPublic()) {
			return DragCreator.getImageElement("../images/gear_public.png");
		} else {
			return DragCreator.getImageElement("../images/gear.png");
		}
	}

	/**
	 * @param wrapperSelector
	 */
	public void setSelector(ItemSelector wrapperSelector) {
		pipelines.setCanSelect(false);
		selector = wrapperSelector;
	}

	public FlowPanel getScrollPanel() {
		return pipelines.getScrollPanel();
	}

	public UserPipeline getSelectedRecord() {
		return selected;
	}
}
