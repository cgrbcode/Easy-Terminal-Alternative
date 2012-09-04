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
package cgrb.eta.client.button;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.SearchResultLine;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabs.WrapperCreator;
import cgrb.eta.client.tabs.WrapperRunner;
import cgrb.eta.client.window.MultipleUserSelect;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.SearchResultItem;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.QJob;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Wrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SearchInput extends VerticalPanel implements ClickHandler, FocusHandler, BlurHandler, KeyUpHandler, SearchItemSelected {
	private Image cancel;
	private TextBox searchInput;
	private Image searchGlass;
	private HorizontalPanel searchPanel;
	private VerticalPanel results;
	private PopupPanel popup;
	private final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private WrapperServiceAsync wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);

	private boolean inSearch = false;
	private HandlerRegistration handler;

	public SearchInput() {
		searchPanel = new HorizontalPanel();
		popup = new PopupPanel(true);
		results = new VerticalPanel();
		popup.setWidget(results);
		searchPanel.setStyleName("eta-search");
		searchPanel.setHeight("15px");
		searchGlass = new Image(Resources.INSTANCE.searchWhite().getSafeUri().asString());
		searchInput = new TextBox();
		cancel = new Image(Resources.INSTANCE.closeOver().getSafeUri().asString());
		searchGlass.setHeight("15px");
		searchGlass.setWidth("15px");
		searchGlass.setPixelSize(15, 15);
		cancel.setHeight("15px");
		cancel.setWidth("15px");
		cancel.setPixelSize(15, 15);
		searchPanel.add(searchGlass);
		searchInput.setHeight("15px");
		searchInput.setStyleName("search-input");
		searchPanel.add(searchInput);
		searchGlass.addDomHandler(this, ClickEvent.getType());
		handler = searchInput.addDomHandler(this, FocusEvent.getType());
		searchInput.addDomHandler(this, BlurEvent.getType());
		cancel.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				popup.hide(true);
			}
		});
		popup.addCloseHandler(new CloseHandler<PopupPanel>() {

			@Override
			public void onClose(CloseEvent<PopupPanel> event) {
				handler.removeHandler();
				inSearch = false;
				searchPanel.setStyleName("eta-search");
				searchGlass.setUrl(Resources.INSTANCE.searchWhite().getSafeUri().asString());
				searchPanel.remove(cancel);
				searchInput.setFocus(false);
				searchInput.setText("");
				handler = searchInput.addDomHandler(SearchInput.this, FocusEvent.getType());
			}
		});

		popup.setStyleName("search-results");
		
		searchInput.addKeyUpHandler(this);
		add(searchPanel);
		getElement().getStyle().setZIndex(9999);
	}

	public void onClick(ClickEvent event) {
		searchInput.setFocus(true);
	}

	public void onFocus(FocusEvent event) {
		searchPanel.setStyleName("search-focus");
		searchGlass.setUrl(Resources.INSTANCE.search().getSafeUri().asString());
		searchPanel.add(cancel);
	}

	public void onBlur(BlurEvent event) {
		if (!inSearch) {
			searchPanel.setStyleName("eta-search");
			searchGlass.setUrl(Resources.INSTANCE.searchWhite().getSafeUri().asString());
			searchPanel.remove(cancel);
		}
	}

	Timer t;

	public void onKeyUp(KeyUpEvent event) {
		if (t == null) {
			t = new Timer() {
				@Override
				public void run() {
					search(searchInput.getValue());
				}
			};
		}
		t.cancel();
		t.schedule(200);
	}

	public void search(String what) {
		inSearch = true;
		if (what == null || what.equals("") || what.length() < 3) {
			results.clear();
			return;
		}
		sqlService.getSearchResults(what, new AsyncCallback<HashMap<String, Vector<SearchResultItem>>>() {

			public void onSuccess(HashMap<String, Vector<SearchResultItem>> result) {
				resultz = result;
				loadResults(result);
			}

			public void onFailure(Throwable caught) {
			}
		});
	}

	public void showAllResults(Vector<SearchResultItem> items) {
		results.clear();
		VerticalPanel temp = new VerticalPanel();
		temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.back(), "Back to search results", null), new SearchItemSelected() {
			public void itemSelected(SearchResultItem item) {
				loadResults(resultz);
			}
		}));
		for (SearchResultItem line : items) {
			temp.add(new SearchResultLine(line, this));
		}
		results.add(temp);

	}

	private HashMap<String, Vector<SearchResultItem>> resultz;

	private void loadResults(HashMap<String, Vector<SearchResultItem>> result) {
		results.clear();
		if (result.size() == 0) {
			Label sorry = new Label("Sorry there are no results for your query");
			sorry.setStyleName("search-sorry");
			results.add(sorry);
			return;
		}
		Grid grid = new Grid(result.size(), 2);
		int on = 0;
		Iterator<String> it = result.keySet().iterator();
		while (it.hasNext()) {
			String type = it.next();
			Label title = new Label(type);
			title.setHeight("100%");
			title.setStyleName("searchtype");
			grid.setWidget(on, 0, title);
			final Vector<SearchResultItem> results = result.get(type);
			VerticalPanel temp = new VerticalPanel();
			int onItem = 1;
			for (SearchResultItem line : results) {
				temp.add(new SearchResultLine(line, this));
				if (onItem++ >= 3 && results.size() > 3) {
					temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.more().getSafeUri().asString(), "Show all", "There are " + (results.size() - 3) + " more results"), new SearchItemSelected() {
						public void itemSelected(SearchResultItem item) {
							showAllResults(results);
						}
					}));
					break;
				}
			}
			grid.setWidget(on++, 1, temp);
		}
		results.add(grid);
		popup.showRelativeTo(this);
	}

	@SuppressWarnings("rawtypes")
	public void itemSelected(final SearchResultItem item) {
		results.clear();
		final VerticalPanel temp = new VerticalPanel();
		temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.back(), "Back to search results", null), new SearchItemSelected() {
			public void itemSelected(SearchResultItem item) {
				loadResults(resultz);
			}
		}));

		String type = item.getType();
		HorizontalPanel nameS = new HorizontalPanel();
		Image icon = new Image(item.getIcon());
		icon.setHeight("20px");
		icon.setWidth("20px");
		Label name = new Label(item.getTitle());
		name.setStyleName("searchtitle");
		name.setHeight("20px");
		nameS.add(icon);
		nameS.add(name);
		temp.add(nameS);
		if (type.equals("wrapper")) {
			wrapperService.getWrapperFromId(item.getId(), new AsyncCallback<Wrapper>() {
				public void onFailure(Throwable caught) {
				}

				public void onSuccess(final Wrapper result) {
					Grid info = new Grid(3, 2);
					info.setWidth("250px");
					info.setText(0, 0, "Description:");
					info.setText(0, 1, result.getDescription());
					info.setText(1, 0, "Command:");
					info.setText(1, 1, result.getProgram());
					info.setText(2, 0, "Author:");
					info.setText(2, 1, result.getCreator());
					results.add(info);
					// add some info about this wrapper like: command it runs, description, author,
					// add some action: add to my wrappers,share,edit,run
					if (ETA.getInstance().getUser().getName().equals(result.getCreator())) {
						// if the user owns this wrapper
						if (item.getIcon().contains("gear.png"))
							results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.share(), "Make public", null), new SearchItemSelected() {
								public void itemSelected(SearchResultItem item2) {
									wrapperService.makePublic(item.getId(), new AsyncCallback<Void>() {
										public void onFailure(Throwable caught) {
										}

										public void onSuccess(Void result) {
										}
									});
								}
							}));
						results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.edit(), "Edit this wrapper", null), new SearchItemSelected() {
							public void itemSelected(SearchResultItem item2) {
								wrapperService.getWrapperFromId(item.getId(), new MyAsyncCallback<Wrapper>() {
									@Override
									public void success(Wrapper result) {
										WrapperCreator creator = new WrapperCreator();
										creator.loadWrapper(result);
										ETA.getInstance().addTab(creator);
									}
								});
							}
						}));

					} else {
						if (!item.getIcon().contains("gear.png")) {
							results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.edit(), "Edit this wrapper", null), new SearchItemSelected() {
								public void itemSelected(SearchResultItem item2) {
									wrapperService.getWrapperFromId(item.getId(), new MyAsyncCallback<Wrapper>() {
										@Override
										public void success(Wrapper result) {
											WrapperCreator creator = new WrapperCreator();
											creator.loadWrapper(result);
											ETA.getInstance().addTab(creator);
										}
									});
								}
							}));

						}

					}
					results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.add(), "Add to my wrapper", null), new SearchItemSelected() {

						public void itemSelected(SearchResultItem item2) {
							wrapperService.moveWrapper(0, 2, 0, item.getTitle(), item.getId(), new MyAsyncCallback<Integer>() {
								public void success(Integer result) {
								}
							});
						}
					}));
					results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.share(), "Share wrapper with someone", null), new SearchItemSelected() {
						public void itemSelected(SearchResultItem item2) {
							final MultipleUserSelect users = new MultipleUserSelect();
							SC.ask("Select users to share with", users, new ValueListener<Boolean>() {
								public void returned(Boolean ret) {
									Vector<User> selUsers = users.getUsers();
									for (User user : selUsers) {
										wrapperService.shareWrapper(item.getId(), user.getId(), result.getName(), new MyAsyncCallback<Void>() {
											public void success(Void result) {
											}
										});
									}
								}
							});
						}
					}));

					results.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.redo(), "Run this wrapper", null), new SearchItemSelected() {
						public void itemSelected(SearchResultItem item) {
							ETA.getInstance().addTab(new WrapperRunner(result.getId()));
						}
					}));
				}
			});
		} else if (type.equals("favorite")) {
			Label path = new Label(item.getFound());
			path.setWidth("250px");
			temp.add(path);
			path.setHeight("10px");
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.redo(), "Navigate to", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					ETA.getInstance().navigateTo(item.getFound().replaceFirst("path:", ""));
				}
			}));
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.remove(), "Remove Favorite", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					sqlService.removeFavorite(1, item.getFound().replaceFirst("path:", ""), new AsyncCallback<Void>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(Void result) {
							loadResults(resultz);
						}
					});
				}
			}));
		} else if (type.equals("job")) {
			Label path = new Label(item.getFound());
			path.setWidth("250px");
			temp.add(path);
			path.setHeight("10px");
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.resultsSmall(), "View job", null), new SearchItemSelected() {

				public void itemSelected(SearchResultItem item2) {
					wrapperService.getJob(item.getId(), new AsyncCallback<Job>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(Job result) {
							ETA.getInstance().addTab(new WrapperRunner(result));
						}
					});
				}
			}));
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.redo(), "Rerun Job", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					wrapperService.getJob(item.getId(), new AsyncCallback<Job>() {
						public void onFailure(Throwable caught) {
						}

						public void onSuccess(Job result) {
							ETA.getInstance().addTab(new WrapperRunner(result));
						}
					});
				}
			}));
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.remove(), "Cancel Job", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					SC.ask("Cancel?", "This job is still running, do you want to cancel it?", new ValueListener<Boolean>() {
						public void returned(Boolean value) {
							if (value) {
								communicationService.cancelJob(item.getId(), new MyAsyncCallback<String>() {
									public void success(String result) {
									}
								});
							}
						}
					});
				}
			}));
		} else if (type.equals("result")) {
			Label path = new Label(item.getFound());
			path.setWidth("250px");
			temp.add(path);
			path.setHeight("10px");
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.resultsSmall(), "View Result", null), new SearchItemSelected() {

				public void itemSelected(SearchResultItem item2) {
					wrapperService.getJob(item.getId(), new MyAsyncCallback<Job>() {
						public void success(Job result) {
							ETA.getInstance().addTab(new WrapperRunner(result));
						}
					});
				}
			}));
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.redo(), "Rerun Job", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					wrapperService.getJob(item.getId(), new MyAsyncCallback<Job>() {
						public void success(Job result) {
							ETA.getInstance().addTab(new WrapperRunner(result));
						}
					});
				}
			}));
			temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.remove(), "Remove Result", null), new SearchItemSelected() {
				public void itemSelected(SearchResultItem item2) {
					sqlService.deleteResult(item.getId(), item.getId(), new MyAsyncCallback<Void>() {
						public void success(Void result) {
						}
					});
				}
			}));
		} else if (type.equals("server")) {
			Label path = new Label(item.getFound());
			path.setWidth("250px");
			temp.add(path);
			path.setHeight("10px");

			if (Storage.isLocalStorageSupported()) {
				temp.add(new SearchResultLine(new SearchResultItem(Resources.INSTANCE.monitorSmall(), "Monitor Server", null), new SearchItemSelected() {
					public void itemSelected(SearchResultItem item2) {
						Storage.getLocalStorageIfSupported().setItem("machine", item.getTitle());
						// ETA.getInstance().addTab(new SGEMonitorTab());
					}
				}));
			}
			Column<QJob> id = new Column<QJob>("Id") {
				@Override
				public Object getValue(QJob record) {
					return record.getId() + "";
				}
			};
			Column<QJob> nameF = new Column<QJob>("Name") {
				@Override
				public Object getValue(QJob record) {
					return record.getName() + "";
				}
			};
			Column<QJob> user = new Column<QJob>("user") {
				@Override
				public Object getValue(QJob record) {
					return record.getUser();
				}
			};

			@SuppressWarnings("unchecked")
			final Table jobs = new Table(false, id, nameF, user);
			jobs.setHeight(182 + "px");
			jobs.setWidth("100%");
			// jobs.setEmptyMessage("No jobs running");
			temp.add(jobs);
			communicationService.getJobsForMachine(item.getTitle(), new MyAsyncCallback<Vector<QJob>>() {
				@SuppressWarnings("unchecked")
				public void success(Vector<QJob> result) {
					jobs.setData(result);
				}
			});
		}

		results.add(temp);
	}

	int depth = 0;

	public void showOverflow(Element el) {
		if (el == null || el.getClassName().equals("rootpanel"))
			return;
		el.getStyle().setOverflow(Overflow.VISIBLE);
		showOverflow(el.getParentElement());
	}

	public static native String getAppName() /*-{
		return $wnd.navigator.appName;
	}-*/;
}
