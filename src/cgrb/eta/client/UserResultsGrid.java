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
import cgrb.eta.client.table.Table;
import cgrb.eta.client.table.TreeTable;
import cgrb.eta.client.tabs.ResultViewer;
import cgrb.eta.client.tabs.WrapperRunner;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.etatype.UserResult;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserResultsGrid extends Composite implements RowClickHandler<UserResult>, DragListener {

	TreeTable<UserResult> grid;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);

	public UserResultsGrid(boolean large) {
		Column<UserResult> name = new Column<UserResult>("Name") {
			@Override
			public Object getValue(UserResult record) {
				HorizontalPanel panel = new HorizontalPanel();
				Image icon = new Image(getIcon(record));
				icon.setHeight("16px");
				icon.setWidth("16px");
				panel.add(icon);
				panel.add(new Label(record.getName()));
				return panel;
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public int compareTo(UserResult o1, UserResult o2) {
				if (o1.getJob() == 0||o1.getName()==null)
					return -1;
				if (o2.getJob() == 0||o2.getName()==null)
					return 1;
				return o1.getName().compareTo(o2.getName());
			}
		};
		Column<UserResult> jobId = new Column<UserResult>("Job Id") {
			@Override
			public Object getValue(UserResult record) {
				if (record.getJob() > 0)
					return "" + record.getJob();
				return "";
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public int compareTo(UserResult o1, UserResult o2) {
				return o1.getJob() - o2.getJob();
			}

			@Override
			public String getWidth() {
				return "50px";
			}
		};

		Column<UserResult> wrapper = new Column<UserResult>("Wrapper") {
			@Override
			public Object getValue(UserResult record) {
				if (record.getJob() > 0)
					return "" + record.getWrapperName();
				return "";
			}

			@Override
			public int compareTo(UserResult o1, UserResult o2) {
				return o1.getWrapperName().compareTo(o2.getWrapperName());
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};
		Column<UserResult> finishDate = new Column<UserResult>("Finished") {
			@Override
			public Object getValue(UserResult record) {
				if (record.getJob() > 0)
					return "" + record.getFinishDate();
				return "";
			}

			@Override
			public int compareTo(UserResult o1, UserResult o2) {
				return o1.getFinishDate().compareTo(o2.getFinishDate());
			}

			@Override
			public boolean canSort() {
				return true;
			}

			@Override
			public String getWidth() {
				return "100px";
			}
		};

		grid = new TreeTable<UserResult>(true, name, jobId) {
			@Override
			public int getParent(UserResult record) {
				return record.getParent();
			}

			@Override
			public boolean isFolder(UserResult record) {
				return record.getJob() == 0;
			}
		};
		if (large) {
			grid.addColumn(wrapper);
			grid.addColumn(finishDate);
		}
		grid.setEmptyString("Run a job for results to show up here");
		grid.addListener(UserResult.class);
		grid.setDragListener(this);
		initWidget(grid);

		sqlService.getUserResults(new MyAsyncCallback<Vector<UserResult>>() {
			@Override
			public void success(Vector<UserResult> result) {
				grid.setData(result);
			}
		});
		grid.addAction(new ImgButton(Resources.INSTANCE.redo(), 20, "Re-run job").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				sqlService.getJob(grid.getSelection().get(0).getJob(), new MyAsyncCallback<Job>() {
					@Override
					public void success(Job result) {
						WrapperRunner runner = new WrapperRunner(result);
						ETA.getInstance().addTab(runner);
					}
				});
			}
		}), Table.SINGLE_SELECT);
		grid.addAction(new ImgButton(Resources.INSTANCE.resultsSmall(), 20, "View job results").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ResultViewer viewer = new ResultViewer(grid.getSelection().get(0).getJob());
				viewer.loadJob(grid.getSelection().get(0).getJob());
				ETA.getInstance().addTab(viewer);
			}
		}), Table.SINGLE_SELECT);
		ImgMenuButton share = new ImgMenuButton(Resources.INSTANCE.share());
		share.addButton(new Button("With someone").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final MultipleUserSelect users = new MultipleUserSelect();
				SC.ask("Select users to share with", users, new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret) {
							Vector<User> sharingWith = users.getUsers();
							for (User user : sharingWith) {
								for (UserResult job : grid.getSelection())
									sqlService.shareResult(job.getJob(), user.getId(), job.getName(), new MyAsyncCallback<Void>() {
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
				for (UserResult job : grid.getSelection()) {
					if (!job.isPublic()) {
						sqlService.makeResultPublic(job.getJob(), new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
							}
						});
					}
				}
			}
		}));
		share.setSize(20);
		grid.addAction(share, Table.MULTIPLE_SELECT);
		grid.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Remove Result").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (UserResult job : grid.getSelection()) {
					sqlService.moveResult(job.getId(), job.getParent(), -1, job.getName(), job.getJob(), new MyAsyncCallback<Integer>() {
						@Override
						public void success(Integer result) {
						}
					});
				}
			}
		}), Table.MULTIPLE_SELECT);

		grid.setRowClickHandler(this);

	}

	public String getIcon(UserResult record) {
		if (record.getJob() == 0)
			return "images/folder.png";
		else
			return "images/file.png";
	}

	public void dragStart(ETAType record) {
		Element el = grid.getElementForRecord((UserResult) record);
		el.getStyle().setOpacity(.5);
	}

	public void dragEnter(ETAType rec) {

	}

	boolean overChild = false;

	public void dragOver(ETAType rec) {
		grid.getScrollPane().getElement().removeClassName("drag-over");
		ETAType dragSource = DragCreator.getDragSource();
		if (dragSource instanceof Wrapper) {

		} else if (dragSource instanceof UserResult) {

		} else {
			// not compatible!
			return;
		}

		UserResult record = (UserResult) rec;
		if (record.getJob() == 0) {
			Element el = grid.getElementForRecord(record);
			el.addClassName("drag-over");
		} else {
			// get the parent
			int parentId = grid.getParent(record);
			if (parentId == 0) {
				grid.getScrollPane().getElement().addClassName("drag-over");
			} else {
				grid.getElementForRecord(parentId).addClassName("drag-over");
			}
		}
		overChild = true;
	}

	public void dragLeave(ETAType rec) {
		overChild = false;
		UserResult record = (UserResult) rec;
		if (record.getJob() == 0) {
			Element el = grid.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = grid.getParent(record);
			if (parentId == 0) {
				grid.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				grid.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}
	}

	public void drop(ETAType rec) {
		UserResult record = (UserResult) rec;
		if (record.getJob() == 0) {
			Element el = grid.getElementForRecord(record);
			el.removeClassName("drag-over");
		} else {
			// get the parent
			int parentId = grid.getParent(record);
			if (parentId == 0) {
				grid.getScrollPane().getElement().removeClassName("drag-over");
			} else {
				grid.getElementForRecord(parentId).removeClassName("drag-over");
			}
		}

		ETAType dragSource = DragCreator.getDragSource();
		int parent = rec.getId();
		if (record.getJob() > 0)
			parent = record.getParent();
		// if (dragSource instanceof Result) {
		// // dropping a new wrapper here
		// sqlService.moveResult(0, 0, parent, "", ((UserResult) dragSource).getId(), new MyAsyncCallback<Integer>() {
		// @Override
		// public void success(Integer result) {
		// }
		// });
		// } else
		if (dragSource instanceof UserResult) {
			// looks like we are moving a userresult
			UserResult old = (UserResult) dragSource;
			sqlService.moveResult(old.getId(), old.getParent(), parent, old.getName(), old.getJob(), new MyAsyncCallback<Integer>() {
				@Override
				public void success(Integer result) {
				}
			});
		} else {
			// not compatible!
		}
	}

	public void dragEnd(ETAType record) {
		grid.getScrollPane().getElement().removeClassName("drag-over");
		Element el = grid.getElementForRecord((UserResult) record);
		el.getStyle().setOpacity(1);
	}

	public Element getDragImage(ETAType rec) {
		UserResult record = (UserResult) rec;
		if (record.getJob() == 0) {
			return DragCreator.getImageElement("../images/folder.png");
		} else if (record.getUser() != ETA.getInstance().getUser().getId()) {
			return DragCreator.getImageElement("../images/file_public.png");
		} else {
			return DragCreator.getImageElement("../images/file.png");
		}
	}

	/**
	 * @param resultSelector
	 */
	public void setSelector(ItemSelector resultSelector) {

	}

	public void rowClicked(UserResult record, int col, int row) {

	}

}
