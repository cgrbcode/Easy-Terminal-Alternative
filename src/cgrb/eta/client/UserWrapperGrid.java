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
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.DropListener;

import cgrb.eta.client.table.Table;
import cgrb.eta.client.table.TreeTable;
import cgrb.eta.client.tabs.WrapperCreator;
import cgrb.eta.client.tabs.WrapperRunner;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.wrapper.Wrapper;

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

public class UserWrapperGrid extends Composite implements RowClickHandler<UserWrapper>, DragListener {
	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private ItemSelector selector;
	TreeTable<UserWrapper> wrappers;
	private boolean overChild = false;

	public UserWrapperGrid(boolean resize) {

		final Column<UserWrapper> icon = new Column<UserWrapper>("") {
			@Override
			public Object getValue(UserWrapper record) {
				Image icon = new Image();
				if (record.getWrapperId() == 0) {
					icon.setUrl(Resources.INSTANCE.folder().getSafeUri().asString());
				} else if (record.isPublic()) {
					icon.setUrl(Resources.INSTANCE.gearPublic().getSafeUri().asString());
				} else {
					icon.setUrl(Resources.INSTANCE.gear().getSafeUri().asString());
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
		Column<UserWrapper> name = new Column<UserWrapper>("Name") {
			@Override
			public Object getValue(UserWrapper record) {
				HorizontalPanel panel = new HorizontalPanel();
				panel.add((Widget) icon.getValue(record));
				panel.add(new Label(record.getName()));
				return panel;
			}

			@Override
			public int compareTo(UserWrapper o1, UserWrapper o2) {
				if (o1.getWrapperId() == 0)
					return -1;
				if (o2.getWrapperId() == 0)
					return 1;
				return o1.getName().compareTo(o2.getName());
			}
		};

		if (resize)
			wrappers = new TreeTable<UserWrapper>(resize, name) {
				@Override
				public int getParent(UserWrapper record) {
					return record.getParent();
				}

				@Override
				public boolean isFolder(UserWrapper record) {
					return record.getWrapperId() == 0;
				}
			};
		else
			wrappers = new TreeTable<UserWrapper>(resize, name) {
				@Override
				public int getParent(UserWrapper record) {
					return record.getParent();
				}

				@Override
				public boolean isFolder(UserWrapper record) {
					return record.getWrapperId() == 0;
				}
			};

		wrappers.addAction(new ImgButton(Resources.INSTANCE.redo(), 20, "Run Wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ETA.getInstance().addTab(new WrapperRunner(wrappers.getSelection().get(0).getWrapperId()));
			}
		}), Table.SINGLE_SELECT);
		wrappers.addAction(new ImgButton(Resources.INSTANCE.edit(), 20, "Edit Wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				WrapperCreator creator = new WrapperCreator();
				creator.setWrapper(""+wrappers.getSelection().get(0).getWrapperId());
				ETA.getInstance().addTab(creator);
			}
		}), Table.SINGLE_SELECT);
		ImgMenuButton share = new ImgMenuButton(Resources.INSTANCE.share());
		share.setTitle("Share");
		share.addButton(new Button("With someone").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final MultipleUserSelect users = new MultipleUserSelect();
				wrappers.preventClearing();
				SC.ask("Select users to share with", users, new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret) {
							Vector<User> sharingWith = users.getUsers();
							for (User user : sharingWith) {
								for (UserWrapper wrap : wrappers.getSelection())
									wrapperService.shareWrapper(wrap.getWrapperId(), user.getId(), wrap.getName(), new MyAsyncCallback<Void>() {
										@Override
										public void success(Void result) {
										}
									});
							}
						}
						wrappers.clearSelection();
					}
				});
			}
		}));

		share.addButton(new Button("Make public").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (UserWrapper wrap : wrappers.getSelection()) {
					if (!wrap.isPublic() && wrap.getAuthor().equals(ETA.getInstance().getUser().getName())) {
						wrapperService.makePublic(wrap.getWrapperId(), new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					}
				}
			}
		}));
		share.setSize(20);
		wrappers.addAction(share, Table.MULTIPLE_SELECT);
		wrappers.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Remove Wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (UserWrapper wrap : wrappers.getSelection()) {
					wrapperService.moveWrapper(wrap.getId(), 0, -1, "", wrap.getWrapperId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);

		wrappers.setEmptyString("Search for wrappers and drag them into here.");
		wrappers.addListener(UserWrapper.class);
		wrappers.setDragListener(this);
		initWidget(wrappers);
		wrappers.displayWaiting("Fetching Wrappers");
		wrapperService.getUsersWrappers(new MyAsyncCallback<Vector<UserWrapper>>() {
			public void success(Vector<UserWrapper> result) {
				wrappers.setData(result);
			};
		});
		wrappers.setRowClickHandler(this);
		DragCreator.addDrop(wrappers.getScrollPane().getElement(), null, new DropListener() {
			public void drop(ETAType record) {
				ETAType dragSource = DragCreator.getDragSource();
				if (dragSource instanceof Wrapper) {
					// dropping a new wrapper here
					int parent = 0;
					wrapperService.moveWrapper(0, 0, parent, "", ((Wrapper) dragSource).getId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				} else if (dragSource instanceof UserWrapper) {
					UserWrapper old = (UserWrapper) dragSource;
					wrapperService.moveWrapper(old.getId(), old.getParent(), 0, old.getName(), ((Wrapper) dragSource).getId(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				} else {
					// not compatible!
				}
				wrappers.getScrollPane().getElement().removeClassName("drag-over");
			}


			public void dragOver(ETAType record) {
				if (!overChild)
					wrappers.getScrollPane().getElement().addClassName("drag-over");
			}

			public void dragLeave(ETAType record) {
				wrappers.getScrollPane().getElement().removeClassName("drag-over");
			}

			public void dragEnter(ETAType record) {

			}

		});
	}

	private UserWrapper selected;

	public void rowClicked(UserWrapper record, int col, int row) {
		if (selector != null) {
			// a selection is in proccess
			if (selected == null) {
				selected = record;
				wrappers.getElementForRecord(record).addClassName("selected");
			} else {
				if (selected.equals(record)) {
					// this has been clicked twice assume this is what the user wanted to select
					selector.itemSelected(new String[] { selected.getWrapperId() + "" });
					wrappers.getElementForRecord(record).addClassName("selected");
				} else {
					wrappers.getElementForRecord(selected).removeClassName("selected");
					wrappers.getElementForRecord(record).addClassName("selected");
					selected = record;
				}
			}
		}
	}

	public void dragStart(ETAType record) {
		Element el = wrappers.getElementForRecord((UserWrapper) record);
		el.getStyle().setOpacity(.5);
	}

	public void dragEnter(ETAType rec) {

	}

	public void dragOver(ETAType rec) {
		wrappers.getScrollPane().getElement().removeClassName("drag-over");
		ETAType dragSource = DragCreator.getDragSource();
		if (dragSource instanceof Wrapper) {

		} else if (dragSource instanceof UserWrapper) {

		} else {
			// not compatible!
			return;
		}

		UserWrapper record = (UserWrapper) rec;
		if (record.getWrapperId() == 0) {
			Element el = wrappers.getElementForRecord(record);
			el.addClassName("drag-over");
		} else {
			// get the parent
			int parentId = wrappers.getParent(record);
			if (parentId == 0) {
				wrappers.getScrollPane().getElement().addClassName("drag-over");
			} else {
				wrappers.getElementForRecord(parentId).addClassName("drag-over");
			}
		}
		overChild = true;
	}

	public void dragLeave(ETAType rec) {
		overChild = false;
		UserWrapper record = (UserWrapper) rec;
		if (record.getWrapperId() == 0) {
			Element el = wrappers.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = wrappers.getParent(record);
			if (parentId == 0) {
				wrappers.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				wrappers.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}
	}

	public void drop(ETAType rec) {
		UserWrapper record = (UserWrapper) rec;
		if (record.getWrapperId() == 0) {
			Element el = wrappers.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = wrappers.getParent(record);
			if (parentId == 0) {
				wrappers.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				wrappers.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}

		ETAType dragSource = DragCreator.getDragSource();
		int parent = rec.getId();
		if (record.getWrapperId() > 0)
			parent = record.getParent();
		if (dragSource instanceof Wrapper) {
			// dropping a new wrapper here
			wrapperService.moveWrapper(0, 0, parent, "", ((Wrapper) dragSource).getId(), new MyAsyncCallback<Integer>() {
				@Override
				public void success(Integer result) {
				}
			});
		} else if (dragSource instanceof UserWrapper) {
			// looks like we are moving a wrapper
			UserWrapper old = (UserWrapper) dragSource;
			wrapperService.moveWrapper(old.getId(), old.getParent(), parent, old.getName(), old.getWrapperId(), new MyAsyncCallback<Integer>() {
				@Override
				public void success(Integer result) {
				}
			});
		} else {
			// not compatible!
		}
	}

	public void dragEnd(ETAType record) {
		wrappers.getScrollPane().getElement().removeClassName("drag-over");
		Element el = wrappers.getElementForRecord((UserWrapper) record);
		el.getStyle().setOpacity(1);
	}

	public Element getDragImage(ETAType rec) {
		UserWrapper record = (UserWrapper) rec;
		if (record.getWrapperId() == 0) {
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
		wrappers.setCanSelect(false);
		selector = wrapperSelector;
	}

	public FlowPanel getScrollPanel() {
		return wrappers.getScrollPanel();
	}

	public UserWrapper getSelectedRecord() {
		return wrappers.getSelection().get(0);
	}
}
