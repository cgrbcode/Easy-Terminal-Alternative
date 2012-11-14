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


import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ImgMenuButton;
import cgrb.eta.client.button.MenuButton;
import cgrb.eta.client.button.SearchInput;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.pipeline.PipelineCreator;
import cgrb.eta.client.tabs.ETASettingsTab;
import cgrb.eta.client.tabs.ExternalWrapperTab;
import cgrb.eta.client.tabs.HelpEditor;
import cgrb.eta.client.tabs.HomeTab;
import cgrb.eta.client.tabs.JobManager;
import cgrb.eta.client.tabs.NewRequestTab;
import cgrb.eta.client.tabs.PipelineRunner;
import cgrb.eta.client.tabs.PipelinesTab;
import cgrb.eta.client.tabs.PluginManagerTab;
import cgrb.eta.client.tabs.RequestItemTab;
import cgrb.eta.client.tabs.RequestTab;
import cgrb.eta.client.tabs.ResultViewer;
import cgrb.eta.client.tabs.ResultsTab;
import cgrb.eta.client.tabs.SharesTab;
import cgrb.eta.client.tabs.UserSettingsTab;
import cgrb.eta.client.tabs.WrapperCreator;
import cgrb.eta.client.tabs.WrapperRunner;
import cgrb.eta.client.tabs.WrappersTab;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.tabset.Tab;
import cgrb.eta.client.tabset.TabManager;
import cgrb.eta.client.tabset.TabPane;
import cgrb.eta.client.tools.ForLoopTab;
import cgrb.eta.client.tools.GeneCounter;
import cgrb.eta.client.window.FileBrowser;
import cgrb.eta.client.window.HelpWindow;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.Cluster;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.User;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 * 
 * This is the entry point to ETA.html, ServiceLogin.html, and ExternalLogin.html <br>
 * ETA is also a singleton object that provides a few methods.
 * 
 * @author Alexander Boyd
 */
public class ETA implements EntryPoint {

	private static ETA instance;
	private CommunicationServiceAsync communicationService;
	public static WrapperServiceAsync wrapperService;
	public static SQLServiceAsync sqlService;
	private User user = null;
	private TabPane mainTabSet;
	private static Label textFunction;

	/**
	 * Provides a static method to access the public methods inside ETA <code>ETA.getInstance();</code>
	 * 
	 * @return ETA The singleton instance of ETA
	 */
	public static ETA getInstance() {
		return instance;
	}

	public static int getTextWidth(String text) {
		textFunction.setText(text);
		return textFunction.getOffsetWidth();
	}

	public void onModuleLoad() {
		communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
		wrapperService = (WrapperServiceAsync) GWT.create(WrapperService.class);
		instance = this;
		// if the url is ETA or / than load ETA
		if (Location.getPath().contains("ETA.html") || Location.getPath().equals("/")) {
			// Check to see if the user is logged in
			checkToken();
			communicationService.getUser(new AsyncCallback<User>() {
				public void onSuccess(User result) {
					if (result == null) {
						// the user isn't logged in take them to the login page
						com.google.gwt.user.client.Window.open("/ServiceLogin.html?send=" + Location.getHref(), "_self", "");
					} else {
						user = result;
						setup();
					}
				}

				public void onFailure(Throwable caught) {
					com.google.gwt.user.client.Window.open("/ServiceLogin.html?send=" + Location.getHref(), "_self", "");
				}
			});
		} else if (Location.getPath().contains("ServiceLogin.html") || Location.getPath().contains("ExternalLogin.html") || Location.getPath().contains("external.html")) {
			// now set up the login page
			String sendTo2 = Location.getParameter("send");
			if (sendTo2 == null) {
				sendTo2 = "http://" + Location.getHostName() + ":" + Location.getPort() + "/ETA.html";
			}
			final String sendTo = sendTo2 + "#" + History.getToken();
			final String externalToken = Location.getParameter("external");
			communicationService.getUser(new AsyncCallback<User>() {
				public void onSuccess(User result) {
					if (result == null) {
						onFailure(null);
					} else if (Location.getParameter("send") != null) {
						if (externalToken != null && !externalToken.equals("")) {
							communicationService.associateToken(externalToken, Location.getParameter("site"), new MyAsyncCallback<Void>() {
								public void success(Void result) {
									Window.open(sendTo, "_self", "");
								}
							});
						} else
							Window.open(sendTo, "_self", "");
					}
				}

				public void onFailure(Throwable caught) {
					RootPanel.get("username").getElement().focus();
					// add the login page
					EnterButtonEventManager.addListener(new EnterListener() {
						public void enter() {
							submit(sendTo);
						}
					});
					
					
					RootPanel.get("pwchange-submit").addDomHandler(new ClickHandler() {

						public void onClick(ClickEvent event) {
							FlowPanel pwForm = new FlowPanel();
							SimpleLabel lemail = new SimpleLabel("Enter your email: ");
							final TextBox email = new TextBox();
							SimpleLabel laccount = new SimpleLabel("Enter your account name:");
							final TextBox account = new TextBox();

							pwForm.add(lemail);
							pwForm.add(email);
							pwForm.add(laccount);
							pwForm.add(account);
							SC.ask("Password Request Form", pwForm, new ValueListener<Boolean>() {
								public void returned(Boolean ret) {
									if (ret == true){
										pwRequest(account.getText(), email.getText());
									}
								}
							});
									
							
							
						}

					}, ClickEvent.getType());

					// submit.add
					RootPanel.get("submit").addDomHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							submit(sendTo);
						}
					}, ClickEvent.getType());
				}
			});

		}

	}

	public RootPanel getRootPanel() {
		return RootPanel.get();
	}

	public User getUser() {
		return user;
	}

	private void setup() {
		if (user == null) {
			// redirect
		}
		sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
		HorizontalPanel background = new HorizontalPanel();
		background.setHeight("100%");
		background.setWidth("100%");
		background.getElement().getStyle().setPaddingRight(5, Unit.PX);
		RootPanel.get("content").add(background);

		TabPane leftTabPane = new TabPane();
		mainTabSet = new TabPane();

		ImgMenuButton submit = new ImgMenuButton(Resources.INSTANCE.submitJobSmall());
		submit.addButton(new Button("Wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new WrapperSelector(new ItemSelector() {
					public void itemSelected(String[] items) {
						addTab(new WrapperRunner(Integer.parseInt(items[0])));
					}
				});
			}
		}));
		submit.addButton(new Button("Command").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.runCommand();
			}
		}));

		mainTabSet.addTopButton(submit);
		mainTabSet.addTopButton(new Seprator());

		ImgMenuButton monitor = new ImgMenuButton(Resources.INSTANCE.monitorSmall());
		monitor.addButton(new Button("The Cloud").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Window.open("http://doolittle.cgrb.oregonstate.edu/ganglia/", "_blank", "");
			}
		}));

		monitor.addButton(new Button("Jobs").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new JobManager());
			}
		}));
		mainTabSet.addTopButton(monitor);
		mainTabSet.addTopButton(new Seprator());

		ImgMenuButton view = new ImgMenuButton(Resources.INSTANCE.resultsSmall());
		view.addButton(new Button("Results").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new ResultsTab());
			}
		}));

		mainTabSet.addTopButton(view);
		mainTabSet.addTopButton(new Seprator());

		ImgMenuButton share = new ImgMenuButton(Resources.INSTANCE.shareSmall());
		share.addButton(new Button("Share Result"));
		share.addButton(new Button("Share File"));
		share.addButton(new Button("View Shares"));
		mainTabSet.addTopButton(share);
		mainTabSet.addTopButton(new Seprator());

		mainTabSet.addTopButton(new Button("Make a request").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new RequestTab());
			}
		}));
		mainTabSet.addTopButton(new Seprator());
		MenuButton user = new MenuButton(this.user.getName());
		if (this.user.getPermissionLevel() > 8) {
			user.addButton(new Button("Help Manager").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					addTab("hlp");
				}
			}));
			user.addButton(new Button("ETA Settings").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					addTab("es");
				}
			}));
			user.addButton(new Button("External Wrappers").setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					addTab("ew");
				}
			}));

			user.addButton(new SeperatorButton());
		}

		user.addButton(new Button("Settings").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab("us");
			}
		}));
		user.addButton(new Button("Logout").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				communicationService.removeTokens(new MyAsyncCallback<Void>() {
					@Override
					public void success(Void result) {
						com.google.gwt.user.client.Window.open("/ServiceLogin.html?send=" + Location.getHref(), "_self", "");
					}
				});
			}
		}));

		mainTabSet.addTopButton(user);

		MenuButton create = new MenuButton("Create");
		create.addButton(new Button("Request").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new NewRequestTab());
			}
		}));
		create.addButton(new Button("Wrapper").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				// new UserSelector(null);
				addTab(new WrapperCreator());
			}
		}));
		create.addButton(new Button("Pipeline").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new PipelineCreator());
			}
		}));
		mainTabSet.addLowerButton(new Seprator());
		mainTabSet.addLowerButton(new Button("Help").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				new HelpWindow(mainTabSet.getSelectedTab());
			}
		}));

		MenuButton tools = new MenuButton("Tools");
		tools.addButton(new Button("For each runner").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addTab(new ForLoopTab());
			}
		}));
		// tools.addButton(new Button("Gene Counter").setClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent event) {
		// addTab(new GeneCounter());
		// }
		// }));

		EventListener.getInstance().addETATypeListener(Cluster.class.getName(), new ETATypeEventOccurred<ETAType>() {

			@Override
			public void onUpdate(ETAType record) {
			}

			@Override
			public void onRemoval(ETAType record) {

			}

			@Override
			public void onAddition(ETAType record) {
				ETA.this.user.addCluster((Cluster) record);
			}
		});
		mainTabSet.addLowerButton(new Seprator());
		mainTabSet.addLowerButton(new Filler(30));
		mainTabSet.addLowerButton(new SearchInput());
		mainTabSet.addLowerButton(new Filler(15));
		mainTabSet.addLowerButton(tools);
		mainTabSet.addLowerButton(new Filler(15));
		mainTabSet.addLowerButton(create);
		mainTabSet.addLowerButton(new Filler(15));
		mainTabSet.enableHistory(true);

		leftTabPane.setWidth("230px");
		Label filler = new Label();
		background.add(leftTabPane);
		background.add(filler);
		background.add(mainTabSet);
		background.setCellWidth(leftTabPane, "230px");
		background.setCellWidth(filler, "20px");

		Tab results = new Tab("") {
			@Override
			public Widget getBar() {
				HorizontalPanel bar = new HorizontalPanel();
				bar.add(new ImgButton(Resources.INSTANCE.newFolder()).setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						SC.ask("What do you want to name this folder?", new ValueListener<String>() {
							public void returned(String ret) {
								if (ret != null && !ret.equals(""))
									sqlService.addResultFolder(ret, new MyAsyncCallback<Integer>() {
										@Override
										public void success(Integer result) {
											if (result < 1) {
												SC.alert("Error :(", "Sorry you already have a folder named that.");
											}
										}
									});
							}
						});
					}
				}));
				bar.add(new Seprator());
				bar.add(new ImgButton(Resources.INSTANCE.resultsSmall(), "Public Results").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						// addTab(new WrappersTab());
					}
				}));
				bar.add(new Seprator());
				return bar;
			}
		};
		results.setPane(new UserResultsGrid(false));
		results.setIcon(Resources.INSTANCE.file());

		Tab wrappers = new Tab("") {
			@Override
			public Widget getBar() {
				HorizontalPanel bar = new HorizontalPanel();
				bar.add(new ImgButton(Resources.INSTANCE.newFolder()).setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						SC.ask("What do you want to name this folder?", new ValueListener<String>() {
							public void returned(String ret) {
								if (ret != null && !ret.equals(""))
									wrapperService.addFolder(ret, new MyAsyncCallback<Integer>() {
										@Override
										public void success(Integer result) {
											if (result < 1) {
												SC.alert("Error :(", "Sorry you already have a folder named that.");
											}
										}
									});
							}
						});
					}
				}));
				bar.add(new Seprator());
				bar.add(new ImgButton(Resources.INSTANCE.gearPublic(), "Public Wrappers").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						addTab(new WrappersTab());
					}
				}));
				bar.add(new Seprator());
				return bar;
			}
		};

		Tab pipelines = new Tab("") {
			@Override
			public Widget getBar() {
				HorizontalPanel bar = new HorizontalPanel();
				bar.add(new ImgButton(Resources.INSTANCE.newFolder()).setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						SC.ask("What do you want to name this folder?", new ValueListener<String>() {
							public void returned(String ret) {
								if (ret != null && !ret.equals(""))
									wrapperService.addPipelineFolder(ret, new MyAsyncCallback<Integer>() {
										@Override
										public void success(Integer result) {
											if (result < 1) {
												SC.alert("Error :(", "Sorry you already have a folder named that.");
											}
										}
									});
							}
						});
					}
				}));
				bar.add(new Seprator());
				bar.add(new ImgButton(Resources.INSTANCE.publicPipeline(), "Public Pipelines").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						addTab(new PipelinesTab());
					}
				}));
				bar.add(new Seprator());
				return bar;
			}
		};
		Tab fileBrowser = new Tab("");
		FileBrowser browser = new FileBrowser(true, false, "left");
		browser.setHeight("100%");
		fileBrowser.setPane(browser);
		fileBrowser.setIcon(Resources.INSTANCE.folder());

		wrappers.setPane(new UserWrapperGrid(true));
		wrappers.setIcon(Resources.INSTANCE.gear());
		pipelines.setPane(new UserPipelineGrid(true));
		pipelines.setIcon(Resources.INSTANCE.pipeline());
		fileBrowser.setTooltip("File browser");
		results.setTooltip("Results");
		wrappers.setTooltip("Wrappers");
		pipelines.setTooltip("Pipelines");

		leftTabPane.addTab(fileBrowser);
		leftTabPane.addTab(results);
		leftTabPane.addTab(pipelines);
		leftTabPane.addTab(wrappers);

		mainTabSet.setManager(new TabManager() {
			public Tab getTab(String id) {
				// all the tabs go here
				if (id.equals("rs")) {
					return new RequestTab();
				} else if (id.startsWith("wc#")) {
					WrapperCreator creator = new WrapperCreator();
					creator.setWrapper(id.split("#")[1]);
					return creator;
				} else if (id.equals("hlp")) {
					return new HelpEditor();
				} else if (id.equals("fl")) {
					return new ForLoopTab();
				} else if (id.equals("shs")) {
					return new SharesTab();
				} else if (id.equals("home")) {
					return new HomeTab();
				} else if (id.equals("nr")) {
					return new NewRequestTab();
				} else if (id.equals("gc")) {
					return new GeneCounter();
				} else if (id.equals("pp")) {
					return new PipelinesTab();
				} else if (id.startsWith("plc#")) {
					id = id.split("#")[1];
					PipelineCreator creator = new PipelineCreator();
					creator.setPipeline(Integer.parseInt(id));
					return creator;
				} else if (id.startsWith("f#")) {
					id = id.split("#")[1];
					navigateTo(id);
					return null;
				} else if (id.startsWith("wr#")) {
					id = id.split("#")[1];
					WrapperRunner creator = new WrapperRunner(Integer.parseInt(id));
					return creator;
				} else if (id.startsWith("pr#")) {
					id = id.split("#")[1];
					PipelineRunner creator = new PipelineRunner(Integer.parseInt(id));
					return creator;
				} else if (id.startsWith("plc")) {
					PipelineCreator creator = new PipelineCreator();
					return creator;
				} else if (id.equals("pw")) {
					return new WrappersTab();
				} else if (id.startsWith("rq#")) {
					id = id.split("#")[1];
					return new RequestItemTab(Integer.parseInt(id));
				} else if (id.startsWith("rv#")) {
					id = id.split("#")[1];
					ResultViewer view = new ResultViewer(Integer.parseInt(id));
					view.loadJob(Integer.parseInt(id));
					return view;
				} else if (id.startsWith("jv")) {
					return new JobManager();
				} else if (id.startsWith("rts")) {
					return new ResultsTab();
				} else if (id.equals("us")) {
					return new UserSettingsTab();
				} else if (id.equals("es")) {
					if (getUser().getPermissionLevel() > 8)
						return new ETASettingsTab();
				} else if (id.equals("ew")) {
					if (getUser().getPermissionLevel() > 8)
						return new ExternalWrapperTab();
				} else if (id.equals("pm")) {
					if (getUser().getPermissionLevel() > 8)
						return new PluginManagerTab();
				}
				return null;
			}
		});
		mainTabSet.addTab("home");
		mainTabSet.setupTabs(History.getToken());

		if (this.user.getEmail() == null || this.user.getEmail().trim().equals("")) {
			SC.ask("Setup an email address?", "It looks like you don't have an email address set up currently so you will be unable to recieve notifications. Would you like to set one up now?", new ValueListener<Boolean>() {
				@Override
				public void returned(Boolean ret) {
					if (ret) {
						addTab("us");
					}
				}
			});
		}
		textFunction = new Label();
		textFunction.setStyleName("hiddenText");
		RootPanel.get().add(textFunction);

	}

	private void pwRequest(final String account, String email) {
		final String message = "Password change request from \nUser: " + account + "\nEmail: " + email;
		communicationService.requestPwChange(message, account, new MyAsyncCallback<Void>() {
			public void success(Void result) {
				SC.alert("Success!", "Password change request sent for "+ account);
			}
		});
	}

	private void submit(final String sendTo) {
		// submit.disable();
		RootPanel.get("submit").getElement().getStyle().setDisplay(Display.NONE);
		RootPanel.get("cG").getElement().getStyle().setDisplay(Display.BLOCK);

		String username = RootPanel.get("username").getElement().getPropertyString("value");
		String password = RootPanel.get("password").getElement().getPropertyString("value");
		communicationService.logIn(username, password, new MyAsyncCallback<Boolean>() {
			public void success(Boolean result) {
				if (result != null && result == true) {
					Window.open(sendTo, "_self", "");
				} else {
					SC.alert("Login Failed", "Invalid username or password");
					RootPanel.get("submit").getElement().getStyle().setDisplay(Display.BLOCK);
					RootPanel.get("cG").getElement().getStyle().setDisplay(Display.NONE);
				}
			}
		});

	}

	private void checkToken() {
		Storage store = Storage.getLocalStorageIfSupported();
		String token = null;
		if (store != null) {
			token = store.getItem("token");
		}
		if (token == null) {
			token = Cookies.getCookie("token");
		}

		if (token == null || token.equals("")) {
			String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
			int string_length = 30;
			String randomstring = "";
			for (int i = 0; i < string_length; i++) {
				int rnum = (int) Math.floor(Math.random() * chars.length());
				randomstring += chars.substring(rnum, rnum + 1);
			}
			token = randomstring;
			if (store != null) {
				store.setItem("token", token);
			} else {
				Cookies.setCookie("token", token);
			}
		}
		Cookies.setCookie("token", token);
	}

	public void removeTab(Tab tab) {
		mainTabSet.removeTab(tab.getId());
	}

	public void removeTab(String ident) {
		mainTabSet.removeTab(ident);
	}

	public void addTab(ETATab tab) {
		mainTabSet.addTab(tab);
	}

	public void addTab(String ident) {
		mainTabSet.addTab(ident);
	}

	/**
	 * This will change the files that are displayed in the file browser on the home tab on the right to the files that are in the the folder of the path provided.
	 * 
	 * @param path
	 *          The path of the folder to navigate the file browser on the home tab to.
	 */
	public void navigateTo(String path) {
		Tab tab = mainTabSet.getTab("home");
		if (tab != null) {
			((HomeTab) tab).navigateTo(path);
		}
	}

}
