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
package cgrb.eta.client.window;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import cgrb.eta.client.CommunicationService;
import cgrb.eta.client.CommunicationServiceAsync;
import cgrb.eta.client.ETA;
import cgrb.eta.client.EventListener;
import cgrb.eta.client.FileSelector;
import cgrb.eta.client.ItemSelector;
import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.TextReader;
import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.ImgButton;
import cgrb.eta.client.button.ImgMenuButton;
import cgrb.eta.client.button.MenuButton;
import cgrb.eta.client.button.ProgressBar;
import cgrb.eta.client.button.SeperatorButton;
import cgrb.eta.client.button.Seprator;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.StarButton;
import cgrb.eta.client.button.UploadItemButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.table.RightClickHandler;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.RowFilter;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabs.PluginViewer;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.FileBrowserEvent;
import cgrb.eta.shared.UploadEvent;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Plugin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FileBrowser extends Composite implements DragListener, RowClickHandler<File> {
	public static final int FILE_SELECT = 0;
	public static final int FOLDER_SELECT = 1;
	private CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);
	private SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);
	private HashMap<String, String> favorites;
	private String ident;
	private MenuButton favs;
	private ItemSelector selector;
	private StarButton star;
	public static String lastFolder = "";
	Table<File> browser;
	MenuButton navigation;
	private FlowPanel pane;
	private UploadItemButton uploadButton;
	private static Vector<Plugin> plugins;
	private static Vector<String> machines;

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "b");
		String ret = ("" + (bytes / Math.pow(unit, exp)));
		return (ret.length() > 4 ? ret.substring(0, 5) : ret) + pre;
	}

	Column<File> dateCol = new Column<File>("Modified") {

		@Override
		public Object getValue(File record) {
			return DateTimeFormat.getFormat("MM/dd/yyyy").format(new Date(record.getModifiedDate()));
		}

		public boolean canSort() {
			return true;
		};

		@Override
		public String getWidth() {
			return "90px";
		}

		@Override
		public int compareTo(File o1, File o2) {
			if (o1.getType().startsWith("Folder") && o2.getType().startsWith("Folder"))
				return (o2.getModifiedDate() > o1.getModifiedDate()) ? 1 : -1;
			if (o1.getType().startsWith("Folder"))
				return -1;
			if (o2.getType().startsWith("Folder"))
				return 1;
			return (o2.getModifiedDate() > o1.getModifiedDate()) ? 1 : -1;
		}
	};
	Column<File> nameCol = new Column<File>("Name") {

		@Override
		public Object getValue(File record) {
			if (record.isLink()) {
				SimpleLabel lab = new SimpleLabel(record.getName());
				lab.setWidth("100%");
				lab.setToolTip(record.getCanonicalPath());
				lab.setColor("#7AE7E7");
				return lab;
			} else
				return record.getName();
		}

		public String getStyle(File record) {
			return record.isLink() ? "sym-link" : null;
		};

		public boolean canSort() {
			return true;
		};

		@Override
		public int compareTo(File o1, File o2) {
			if (o1.getType().startsWith("Folder") && o2.getType().startsWith("Folder"))
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			if (o1.getType().startsWith("Folder"))
				return -1;
			if (o2.getType().startsWith("Folder"))
				return 1;
			return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
		}
	};
	private ImgMenuButton back;
	private ImgMenuButton viewMenu;
	private HTML dropThingy;
	private VerticalPanel progressBars;

	public FileBrowser(boolean resize, boolean showSize, String ident) {
		this.ident = ident;
		Column<File> typeCol = new Column<File>("") {
			@Override
			public Object getValue(final File record) {
				Image icon = new Image();
				icon.setWidth("16px");
				icon.setHeight("16px");
				if (record.getType().startsWith("Folder")) {
					icon.setUrl(Resources.INSTANCE.folder().getSafeUri().asString());
					icon.getElement().getStyle().setCursor(Cursor.POINTER);
					icon.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							event.stopPropagation();
							navigateTo(record.getPath());
						}
					});
				} else {
					String mime = record.getMime();
					if (mime != null && (mime.equals("application/x-tar") || mime.equals("application/x-compressed-tar") || mime.equals("application/zip") || mime.equals("application/x-gzip"))) {
						icon.setUrl(Resources.INSTANCE.zip().getSafeUri().asString());
					} else
						icon.setUrl(Resources.INSTANCE.file().getSafeUri().asString());
				}
				return icon;
			}

			@Override
			public String getWidth() {
				return "20px";
			}
		};

		Column<File> sizeCol = new Column<File>("Size") {
			@Override
			public Object getValue(File record) {
				if (record.getType().startsWith("Folder"))
					return "";
				return humanReadableByteCount(record.getSize(), false);
			}

			public boolean canSort() {
				return true;
			};

			@Override
			public int compareTo(File o1, File o2) {
				if (o1.getType().startsWith("Folder") && o2.getType().startsWith("Folder"))
					return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
				if (o1.getType().startsWith("Folder"))
					return -1;
				if (o2.getType().startsWith("Folder"))
					return 1;
				return (o2.getSize() > o1.getSize()) ? 1 : -1;
			}

			@Override
			public String getWidth() {
				return "60px";
			}
		};
		if (showSize)
			browser = new Table<File>(resize, typeCol, nameCol, dateCol, sizeCol);
		else
			browser = new Table<File>(resize, typeCol, nameCol);

		browser.applyFilter(new RowFilter<File>() {
			@Override
			public boolean filterRecord(File record) {
				return record.getName().startsWith(".");
			}
		});
		browser.setEmptyString("This folder is empty");

		ImgMenuButton editMenu = new ImgMenuButton(Resources.INSTANCE.edit());
		editMenu.setSize(16);
		editMenu.addButton(new Button("Copy").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				copyFile(browser.getSelection().get(0));
			}
		}));
		editMenu.addButton(new Button("Move").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				moveFile(browser.getSelection().get(0));
			}
		}));
		editMenu.addButton(new Button("Rename").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				renameFile(browser.getSelection().get(0));
			}
		}));
		editMenu.addButton(new Button("Make link").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				linkFile(browser.getSelection().get(0));
			}
		}));
		editMenu.setSize(20);
		editMenu.setTitle("Edit");
		browser.addAction(editMenu, Table.SINGLE_SELECT);
		browser.addAction(new ImgButton(Resources.INSTANCE.download(), 20, "Download files").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				for (File file : browser.getSelection()) {
					if (!file.getType().startsWith("Folder")) {
						downloadFile(file);
					}
				}
			}
		}), Table.MULTIPLE_SELECT);

		browser.addAction(new ImgButton(Resources.INSTANCE.remove(), 20, "Delete files").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				browser.preventClearing();
				SC.ask("Delete files?", "Are you positive that you want to remove " + browser.getSelection().size() + " files?", new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (ret)
							removeFiles(browser.getSelection());
						browser.clearSelection();
					}
				});
			}
		}), Table.MULTIPLE_SELECT);
		viewMenu = new ImgMenuButton(Resources.INSTANCE.resultsSmall());
		viewMenu.setTitle("View File as");
		viewMenu.addButton(new Button("Text").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				TextReader reader = new TextReader(browser.getSelection().get(0));
				SC.ask("" + browser.getSelection().get(0).getName(), reader, null);
			}
		}));

		viewMenu.setSize(20);
		browser.addAction(viewMenu, Table.SINGLE_SELECT);
		navigation = new MenuButton("");
		pane = new FlowPanel();
		pane.setWidth("100%");

		browser.setWidth("100%");
		HorizontalPanel menu = new HorizontalPanel();
		ImgButton refresh = (ImgButton) new ImgButton(Resources.INSTANCE.refresh()).setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				navigateTo(lastFolder);
			}
		});
		star = new StarButton();
		star.setHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (star.isStared())
					SC.ask("What do you want to name this favorite?", new ValueListener<String>() {
						public void returned(String ret) {
							if (ret == null || ret.equals(""))
								return;
							sqlService.saveFavorite(1, lastFolder, ret, new MyAsyncCallback<Integer>() {
								@Override
								public void success(Integer result) {
									if (result < 1) {
										SC.alert("Error :(", "Sorry you already have a folder named that.");
									} else {
										getFavorites();
									}
								}
							});
						}
					});
				else {
					sqlService.removeFavorite(1, lastFolder, new MyAsyncCallback<Void>() {
						@Override
						public void success(Void result) {
							getFavorites();
						}
					});
				}
			}
		});

		star.setStared(false);
		star.setSize(20);
		back = new ImgMenuButton(Resources.INSTANCE.backArrow());

		back.setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				back(new File("", "", "Folder", 0, 0, 0));
			}
		});
		back.setSize(20);
		menu.setSpacing(0);
		menu.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		menu.add(back);
		if (!ident.equals("left"))
			menu.add(refresh);
		menu.add(star);
		pane.add(menu);
		menu.add(navigation);
		progressBars = new VerticalPanel();
		progressBars.setWidth("100%");
		pane.add(progressBars);
		dropThingy = new HTML("Drop Files Here");
		dropThingy.setStyleName("file-drop");
		pane.add(dropThingy);

		pane.add(browser);

		refresh.setSize(20);

		menu.setStyleName("browser-menu");
		menu.setCellWidth(refresh, 30 + "px");
		menu.setCellWidth(star, "30px");
		menu.setCellWidth(back, "30px");
		menu.setWidth("100%");
		initWidget(pane);
		setupFileDrop(getElement(), dropThingy.getElement(), browser, this);
		favs = new MenuButton("Favorites");
		menu.add(favs);
		menu.setCellHorizontalAlignment(favs, HasHorizontalAlignment.ALIGN_RIGHT);
		if (showSize) {
			favs.setRight(6);
			favs.addStyleName("left-align-menu");
		}
		browser.setDragListener(this);
		browser.setRowClickHandler(this);
		if (!lastFolder.equals("") || ETA.getInstance().getUser().isServiceConnected())
			navigateTo(lastFolder);
		EventListener.getInstance().addListener(ETAEvent.MACHINE, new EventOccuredListener() {
			public void eventOccured(ETAEvent event, int user) {
				// MachineEvent ev = (MachineEvent) event.getSource();

				navigateTo(lastFolder);
			}
		});
		getFavorites();
		browser.setCanSelect(true);

		if (plugins == null) {
			sqlService.getPlugins(new MyAsyncCallback<Vector<Plugin>>() {
				@Override
				public void success(Vector<Plugin> result) {
					plugins = result;
					setupViewMenu();
				}
			});
		} else {
			setupViewMenu();
		}
		browser.setRightClickHandler(new RightClickHandler<File>() {
			@Override
			public Widget getWigetForContext(final File record) {
				VerticalPanel panel = new VerticalPanel();
				String mime = record.getMime();
				if (mime != null && (mime.equals("application/x-tar") || mime.equals("application/x-compressed-tar") || mime.equals("application/zip") || mime.equals("application/x-gzip"))) {
					panel.add(new Button("Extract").setClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							extractFile(record);
						}
					}));
				} else {
					panel.add(compressButton);
				}
				return panel;
			}
		});

		EventListener.getInstance().addListener(ETAEvent.FILE_BROWSER, new EventOccuredListener() {
			@Override
			public void eventOccured(ETAEvent event, int user) {
				FileBrowserEvent browserEvent = (FileBrowserEvent) event.source;
				if (browserEvent.getFolder().equals(lastFolder)) {
					navigateTo(lastFolder);
				}
			}
		});
	}

	Button compressButton = new Button("Compress").setClickHandler(new ClickHandler() {
		@Override
		public void onClick(ClickEvent event) {
			final ListBox options = new ListBox();
			options.addItem("gzip");
			options.addItem("zip");
			options.addItem("tar");
			SC.ask("Please select a compression method.", options, new ValueListener<Boolean>() {
				@Override
				public void returned(Boolean ret) {
					String[] files = getSelectedFiles();
					if (ret) {
						int selected = options.getSelectedIndex();
						communicationService.compressFiles(options.getItemText(selected), files, getSelectedFile().getFolder(), new MyAsyncCallback<Boolean>() {
							@Override
							public void success(Boolean result) {
								navigateTo(lastFolder);
							}
						});
					}
				}
			});
		}
	});

	public boolean isIn(int x, int y) {
		return x > getAbsoluteLeft() && y > getAbsoluteTop() && x < getAbsoluteLeft() + getOffsetWidth() && y < getAbsoluteTop() + getOffsetHeight();
	}

	public void showOver() {
		dropThingy.getElement().addClassName("drag-over");
	}

	public void showOut() {
		dropThingy.getElement().removeClassName("drag-over");
	}

	private HashMap<String, FileInfo> files = new HashMap<String, FileInfo>();

	public void addFileUpload(JavaScriptObject file) {
		FileInfo info = new FileInfo(file, lastFolder);
		ProgressBar progress = new ProgressBar(info.name);
		if (info.length < FileInfo.BYTES_TO_READ) {
			info.currentPos = info.length;
		} else {
			info.currentPos += FileInfo.BYTES_TO_READ;
		}
		readFileBuffer(info.file, 0, info.currentPos, this);

		files.put(info.name, info);
		info.bar = progress;
		progressBars.add(progress);
	}

	public static native String getFileName(JavaScriptObject file)/*-{
		return file.name;
	}-*/;

	public void saveFileBuffer(final String fileName, JavaScriptObject obj) {
		byte[] contents = new ArrayConversion(obj).bytes;
		final FileInfo info = files.get(fileName);
		communicationService.saveFileBuffer(info.path, contents, new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				info.bar.setPercent(((info.currentPos * 100.0) / info.length));
				if (info.currentPos >= info.length) {
					// we reached the end return
					communicationService.saveFileBuffer(info.path, new byte[] {}, new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
							progressBars.remove(info.bar);
							navigateTo(lastFolder);
						}
					});
					return;
				}
				int startPos = info.currentPos;
				if (startPos + FileInfo.BYTES_TO_READ >= info.length) {
					info.currentPos = info.length;
				} else {
					info.currentPos += FileInfo.BYTES_TO_READ;
				}
				readFileBuffer(info.file, startPos, info.currentPos, FileBrowser.this);
			}
		});
	}

	public static native void setupFileDrop(Element element, Element show, Table<File> table, FileBrowser browser)/*-{

		function handleDragOver(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			if (evt.dataTransfer.types[0] != "text/plain") {
				evt.dataTransfer.dropEffect = 'copy'; // Explicitly show this is a copy.
				show.style.display = "block";
				table.@cgrb.eta.client.table.Table::adjustSize()();
			}
		}
		function isIn(x, y) {
			return browser.@cgrb.eta.client.window.FileBrowser::isIn(II)(x,y);
		}
		function handleDragLeave(e) {
			if (isIn(e.clientX, e.clientY))
				return;
			show.style.display = "none";
			table.@cgrb.eta.client.table.Table::adjustSize()();
		}

		function handleDragEnd(e) {
			show.style.display = "none";
			table.@cgrb.eta.client.table.Table::adjustSize()();
		}

		function handleDragLeaveShow(e) {
			browser.@cgrb.eta.client.window.FileBrowser::showOut()();
		}
		function handleDragOverShow(e) {
			e.stopPropagation();
			e.preventDefault();
			browser.@cgrb.eta.client.window.FileBrowser::showOver()();
		}
		function handleDropShow(evt) {
			evt.stopPropagation();
			evt.preventDefault();
			show.style.display = "none";

			var files = evt.dataTransfer.files;
			for ( var i = 0, f; f = files[i]; i++) {
				browser.@cgrb.eta.client.window.FileBrowser::addFileUpload(Lcom/google/gwt/core/client/JavaScriptObject;)(f);
			}
			browser.@cgrb.eta.client.window.FileBrowser::showOut()();
		}
		element.addEventListener('dragleave', handleDragLeave, false);
		element.addEventListener('dragend', handleDragEnd, false);
		element.addEventListener('dragover', handleDragOver, false);
		show.addEventListener('dragleave', handleDragLeaveShow, false);
		show.addEventListener('dragover', handleDragOverShow, false);
		show.addEventListener('drop', handleDropShow, false);

	}-*/;

	private static native void readFileBuffer(JavaScriptObject file, int start, int stop, FileBrowser browser)/*-{
		var reader = new FileReader();
		reader.onloadend = function(evt) {
			if (evt.target.readyState == FileReader.DONE) { // DONE == 2
				var bytes = new Uint8Array(evt.target.result);
				browser.@cgrb.eta.client.window.FileBrowser::saveFileBuffer(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(file.name,bytes);
			}
		};
		if (file.webkitSlice) {
			var blob = file.webkitSlice(start, stop);
		} else if (file.mozSlice) {
			var blob = file.mozSlice(start, stop);
		}
		reader.readAsArrayBuffer(blob)
		//reader.readAsBinaryString(blob);
	}-*/;

	private void extractFile(File file) {
		String mime = file.getMime();
		communicationService.deCompressFile(mime, file.getPath(), file.getFolder(), new MyAsyncCallback<Boolean>() {
			@Override
			public void success(Boolean result) {
				navigateTo(lastFolder);
			}
		});
	}

	public void setupViewMenu() {
		for (final Plugin plugin : plugins) {
			viewMenu.addButton(new Button(plugin.getName()).setClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					communicationService.startSession(plugin.getId(), browser.getSelection().get(0).getPath(), new MyAsyncCallback<String>() {
						@Override
						public void success(String result) {
							final Frame pluginFrame = new Frame("/plugins/" + plugin.getIdentifier() + "/" + plugin.getIndex() + "?session=" + result);
							pluginFrame.setWidth("100%");
							pluginFrame.setHeight("600px");
							HorizontalPanel menu = new HorizontalPanel();
							menu.add(new ImgButton(Resources.INSTANCE.popIn(), "Pop In", 20).setClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									ETA.getInstance().addTab(new PluginViewer(pluginFrame));
								}
							}));
							menu.add(new Seprator());
							menu.add(new ImgButton(Resources.INSTANCE.share(), "Share", 20));
							menu.add(new Seprator());
							menu.add(new Button("Publish"));
							menu.add(new Seprator());
							SC.ask(browser.getSelection().get(0).getPath(), pluginFrame, menu, null);
							// com.google.gwt.user.client.Window.open("/plugins/"+plugin.getIdentifier()+"/"+plugin.getIndex()+"?session=" + result + "&server=https://shell.cgrb.oregonstate.edu:8443/", "_blank", "");
						}
					});
				}
			}));
		}
	}

	public void hideFileSize() {

	}

	private void downloadFile(File file) {
		communicationService.startSession(com.google.gwt.user.client.Window.Location.getHostName(), file.getPath(), new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				Frame f = new Frame(GWT.getHostPageBaseURL() + "plugin?id=" + result + "&function=download");
				f.setPixelSize(1, 1);
				f.getElement().getStyle().setPosition(Position.ABSOLUTE);
				f.getElement().getStyle().setLeft(1, Unit.PX);
				pane.add(f);
			}
		});
	}

	private void moveFile(final File file) {
		browser.preventClearing();
		new FileSelector(new ItemSelector() {
			public void itemSelected(String[] items) {
				communicationService.moveFile(file.getPath(), items[0] + "/" + file.getName(), new MyAsyncCallback<Boolean>() {
					@Override
					public void success(Boolean result) {
						browser.clearSelection();
						navigateTo(lastFolder);
					}
				});
			}
		}, FileBrowser.FOLDER_SELECT);
	}

	private void linkFile(final File file) {
		browser.preventClearing();
		new FileSelector(new ItemSelector() {
			public void itemSelected(String[] items) {
				communicationService.linkFile(file.getPath(), items[0] + "/" + file.getName(), new MyAsyncCallback<Boolean>() {
					@Override
					public void success(Boolean result) {
						browser.clearSelection();
						navigateTo(lastFolder);
					}
				});
			}
		}, FileBrowser.FOLDER_SELECT);
	}

	private void copyFile(final File file) {
		browser.preventClearing();
		new FileSelector(new ItemSelector() {
			public void itemSelected(String[] items) {
				communicationService.copyFile(file.getPath(), items[0] + "/" + file.getName(), new MyAsyncCallback<Boolean>() {
					@Override
					public void success(Boolean result) {
						browser.clearSelection();
						navigateTo(lastFolder);
					}
				});
			}
		}, FileBrowser.FOLDER_SELECT);
	}

	private void renameFile(final File file) {
		browser.preventClearing();
		SC.ask("What do you want to name this file?", new ValueListener<String>() {
			public void returned(String ret) {
				communicationService.moveFile(file.getPath(), lastFolder + "/" + ret, new MyAsyncCallback<Boolean>() {
					@Override
					public void success(Boolean result) {
						browser.clearSelection();
						navigateTo(lastFolder);
					}
				});
			}
		});
	}
  // checks to see if an error message was passed back. If it was display a window telling the user that.
	// The check is surrounded in a try catch just incase there is an empty string returned
	private void removeFiles(Vector<File> files) {
		communicationService.removeFiles(files, new MyAsyncCallback<String>() {
			@Override
			public void success(String result) {
				navigateTo(lastFolder);
				try {
					if (result.substring(0, 2).equals("E#")) {
						SC.alert("Error", result.substring(2));
					}
				} catch (StringIndexOutOfBoundsException e) {

				}

			}
		});
	}

	private void getFavorites() {
		favs.clearMenu();
		uploadButton = (UploadItemButton) new UploadItemButton().setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ProgressBar bar = new ProgressBar(uploadButton.getFile());
				bar.setHeight("20px");
				pane.remove(browser);
				pane.add(bar);
				pane.add(browser);
				bar.setWidth("100%");
				EventListener.getInstance().addListener(ETAEvent.UPLOAD, new EventOccuredListener() {
					public void eventOccured(ETAEvent event, int user) {
						UploadEvent uEvent = (UploadEvent) event.getSource();
						if (uEvent.getFile().equals(uploadButton.getFileId())) {
							bar.setPercent(uEvent.getPercent());
							if (uEvent.getPercent() == 100) {
								pane.remove(bar);
								navigateTo(lastFolder);
							}
						}
					}
				});
			}
		});
		favs.addButton(new Button("Navigate To").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("Where would you like to navigate to?", new ValueListener<String>() {
					public void returned(String ret) {
						navigateTo(ret);
					}
				});
			}
		}));
		favs.addButton(uploadButton);

		favs.addButton(new Button("Create Folder").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				SC.ask("What is the folder name?", new ValueListener<String>() {
					public void returned(String ret) {
						communicationService.createFolder(lastFolder + "/" + ret, new MyAsyncCallback<Void>() {
							@Override
							public void success(Void result) {
								navigateTo(lastFolder);
							}
						});
					}
				});
			}
		}));
		favs.addButton(new SeperatorButton());
		favs.addButton(new Button("Home").setClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				navigateTo("$HOME");
			}
		}));
		sqlService.getFavorites(new MyAsyncCallback<HashMap<String, String>>() {
			public void success(HashMap<String, String> result) {
				favorites = result;
				Iterator<String> it = result.keySet().iterator();
				while (it.hasNext()) {
					final String name = it.next();
					favorites.put(name, favorites.get(name));
					favs.addButton(new Button(name).setClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							navigateTo(favorites.get(name));
						}
					}));
				}
			};
		});
	}

	private void back(File file) {
		communicationService.back(ident, file.getPath(), new MyAsyncCallback<Vector<File>>() {
			@Override
			public void success(Vector<File> result) {
				if (result == null || result.size() == 0) {
					SC.alert("Error :(", "It apears that you have gone to a path that doesn't exist.");
					return;
				}
				File cwd = result.remove(0);
				communicationService.getHistory(ident, new MyAsyncCallback<Vector<File>>() {
					@Override
					public void success(Vector<File> result) {
						back.clear();
						if (result.size() > 0)
							result.remove(0);
						for (final File file : result) {
							if (file.getPath().equals("/"))
								file.setName("/");
							back.addButton(new Button(file.getName()).setClickHandler(new ClickHandler() {
								public void onClick(ClickEvent event) {
									back(file);
								}
							}));
						}
					}
				});
				if (favorites != null)
					star.setStared(favorites.containsValue(cwd.getPath()));
				lastFolder = cwd.getPath();
				browser.setData(result);
				browser.sortData(nameCol);
				String[] folders = cwd.getPath().split("/");
				if (folders.length <= 1)
					navigation.setTitle("/");
				else
					navigation.setTitle(folders[folders.length - 1]);
				navigation.clearMenu();
				for (int i = folders.length - 2; i >= 1; i--) {
					String path = "";
					for (int i2 = 0; i2 <= i; i2++) {
						path += folders[i2] + "/";
					}
					final String path2 = path;
					navigation.addButton(new Button(folders[i]).setClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							navigateTo(path2);
						}
					}));
				}
				navigation.addButton(new Button("/").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						navigateTo("/");
					}
				}));

			}
		});
	}

	public void navigateTo(String path) {
		lastFolder = path;
		browser.displayWaiting("Please wait while fetching files from <br>" + path);
		communicationService.getFiles(path, ident, new MyAsyncCallback<Vector<File>>() {
			@Override
			public void success(Vector<File> result) {
				if (result.size() == 0) {
					SC.alert("Error :(", "It apears that you have gone to a path that doesn't exist.");
					return;
				}

				File cwd = result.remove(0);
				communicationService.getHistory(ident, new MyAsyncCallback<Vector<File>>() {
					@Override
					public void success(Vector<File> result) {
						back.clear();
						if (result == null)
							return;
						if (result.size() > 0)
							result.remove(0);
						for (final File file : result) {
							if (file.getPath().equals("/"))
								file.setName("/");
							back.addButton(new Button(file.getName()).setClickHandler(new ClickHandler() {
								public void onClick(ClickEvent event) {
									back(file);
								}
							}));
						}
					}
				});
				if (favorites != null)
					star.setStared(favorites.containsValue(cwd.getPath()));
				lastFolder = cwd.getPath();
				browser.setData(result);
				browser.sortData(nameCol);
				String[] folders = cwd.getPath().split("/");
				if (folders.length <= 1)
					navigation.setTitle("/");
				else
					navigation.setTitle(folders[folders.length - 1]);
				navigation.clearMenu();
				for (int i = folders.length - 2; i >= 1; i--) {
					String path = "";
					for (int i2 = 0; i2 <= i; i2++) {
						path += folders[i2] + "/";
					}
					final String path2 = path;
					navigation.addButton(new Button(folders[i]).setClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) {
							navigateTo(path2);
						}
					}));
				}
				navigation.addButton(new Button("/").setClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						navigateTo("/");
					}
				}));
			}
		});
	}

	public void setSelector(ItemSelector selector) {
		browser.setRowClickHandler(this);
		this.selector = selector;
		browser.setCanSelect(false);
	}

	public FlowPanel getScrollPanel() {
		return browser.getScrollPanel();
	}

	public void dragStart(ETAType record) {
		File rec = (File) record;
		Element el = browser.getElementForRecord((File) record);
		if (!rec.getType().startsWith("Folder"))
			el.setAttribute("data-downloadurl", "application/octet-stream:" + rec.getName() + ":" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + "/auth?request=download&query=" + rec.getPath());
		el.getStyle().setOpacity(.5);
	}

	public void dragEnter(ETAType record) {
	}

	public void dragOver(ETAType rec) {
		ETAType dragSource = DragCreator.getDragSource();
		if (dragSource instanceof File) {
			File record = (File) rec;
			if (record.getType().startsWith("Folder")) {
				Element el = browser.getElementForRecord(record);
				el.addClassName("drag-over");
			}
		}
	}

	public void dragLeave(ETAType record) {
		browser.getElementForRecord((File) record).removeClassName("drag-over");
	}

	public void drop(ETAType rec) {
		browser.getElementForRecord((File) rec).removeClassName("drag-over");
		ETAType dragSource = DragCreator.getDragSource();
		if (dragSource instanceof File) {
			final File record = (File) rec;
			final File source = (File) dragSource;
			if (record.getType().startsWith("Folder")) {
				SC.ask("Move file?", "Are you sure you want to move " + source.getName() + " to " + record.getPath() + "/" + source.getName(), new ValueListener<Boolean>() {
					public void returned(Boolean ret) {
						if (!ret)
							return;
						communicationService.moveFile(source.getPath(), record.getPath() + "/" + source.getName(), new MyAsyncCallback<Boolean>() {
							@Override
							public void success(Boolean result) {
								navigateTo(lastFolder);
							}
						});
					}
				});

			} else {
				if (!record.getFolder().equals(source.getFolder())) {
					SC.ask("Move file?", "Are you sure you want to move " + source.getName() + " to " + record.getFolder() + "/" + source.getName(), new ValueListener<Boolean>() {
						public void returned(Boolean ret) {
							if (!ret)
								return;
							communicationService.moveFile(source.getPath(), record.getPath() + "/" + source.getName(), new MyAsyncCallback<Boolean>() {
								@Override
								public void success(Boolean result) {
									navigateTo(lastFolder);
								}
							});
						}
					});
				}
			}
		}

	}

	public void dragEnd(ETAType record) {
		browser.getElementForRecord((File) record).removeClassName("drag-over");
		Element el = browser.getElementForRecord((File) record);
		el.getStyle().setOpacity(1);
	}

	public Element getDragImage(ETAType record) {
		File file = (File) record;
		if (file.getType().startsWith("Folder")) {
			return DragCreator.getImageElement("../images/folder.png");
		}
		return DragCreator.getImageElement("../images/file.png");
	}

	File selected = null;

	public void rowClicked(File record, int col, int row) {
		selected = record;
		if (selected.getType().startsWith("Folder")) {
			navigateTo(selected.getPath());
			selected = null;
			return;
		}
		if (selector != null) {
			selector.itemSelected(new String[] { selected.getPath() + "" });
			browser.getElementForRecord(record).addClassName("selected");
		}
	}

	public File getSelectedFile() {
		if (browser.getSelection().size() == 0)
			return new File("", lastFolder, "Folder", 0, 0, 0);
		return browser.getSelection().get(0);
	}

	public String[] getSelectedFiles() {
		if (browser.getSelection().size() == 0)
			return new String[] { new File("", lastFolder, "Folder", 0, 0, 0).getPath() };
		String[] ret = new String[browser.getSelection().size()];
		for (int i = 0; i < browser.getSelection().size(); i++) {
			ret[i] = browser.getSelection().get(i).getPath();
		}
		return ret;
	}
}
