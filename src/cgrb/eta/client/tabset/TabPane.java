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
package cgrb.eta.client.tabset;

import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.client.button.Button;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.OptionButton;
import cgrb.eta.client.button.Seprator;
import com.google.gwt.animation.client.Animation;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * TabPane is the pane that holds all of the tabs and controls the panel that the content goes into.
 * 
 * The variables themselves dictate exactly where they go. Anything you want loaded before a tab loads should go in here. Examples would be the animations panel, and the options button. Both of those things are loaded BEFORE the tab is loaded. Tabs are also loaded in this file, identified in the
 * file, and drawn in this file. Animation is also handled here.
 * 
 * 
 * @author Alexander Boyd
 * 
 */
public class TabPane extends Composite implements TabEventListener, ValueChangeHandler<String> {

	private HorizontalPanel tabBar;
	private HorizontalPanel topBar;
	private HorizontalPanel bottomBar;
	private FlowPanel tabHeads;
	Vector<Tab> tabs;
	private FlowPanel content;
	private Tab currentTab;
	private boolean enableHistory = false;
	private HandlerRegistration handler;
	private TabManager manager = null;
	private HashMap<String, Tab> tabMap;
	private final OptionButton showOptions;
	private FlowPanel animationHolder;

	public TabPane() {
		tabMap = new HashMap<String, Tab>();
		VerticalPanel panel = new VerticalPanel();
		HorizontalPanel top = new HorizontalPanel();
		bottomBar = new HorizontalPanel();
		tabs = new Vector<Tab>();
		HorizontalPanel lowerPanel = new HorizontalPanel();
		lowerPanel.setWidth("100%");
		lowerPanel.setHeight("25px");

		tabHeads = new FlowPanel();
		tabBar = new HorizontalPanel();
		topBar = new HorizontalPanel();
		topBar.setStyleName("top-bar");
		panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
		panel.setHeight("100%");
		panel.setWidth("100%");

		tabHeads.setWidth("100%");
		tabHeads.setHeight("25px");
		tabHeads.setStyleName("tab-heads");
		// tabHeads.setWidth("100px");
		// tabHeads.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		tabBar.setHeight("25px");
		bottomBar.setHeight("25px");
		lowerPanel.setStyleName("tab-bar");
		Label filler = new Label();
		filler.setWidth("15px");
		Label filler2 = new Label();
		filler2.setWidth("15px");

		top.add(filler);
		top.add(tabHeads);
		top.setCellWidth(tabHeads, "100%");
		top.add(filler2);
		top.add(topBar);
		top.add(filler2);
		top.setCellWidth(filler, "15px");
		top.setCellWidth(filler2, "15px");
		top.setCellHorizontalAlignment(topBar, HasHorizontalAlignment.ALIGN_RIGHT);
		top.setCellVerticalAlignment(topBar, HasVerticalAlignment.ALIGN_BOTTOM);
		top.setHeight("25px");
		top.setWidth("100%");

		panel.add(top);
		lowerPanel.add(tabBar);
		lowerPanel.add(bottomBar);
		lowerPanel.setCellHorizontalAlignment(bottomBar, HasHorizontalAlignment.ALIGN_RIGHT);

		panel.add(lowerPanel);
		content = new FlowPanel();
		content.setStyleName("tab-content");
		panel.setCellHeight(top, "25px");
		panel.setCellHeight(lowerPanel, "25px");
		content.add(new Label());
		panel.add(content);
		initWidget(panel);
		setHeight("100%");
		Window.addResizeHandler(new ResizeHandler() {
			public void onResize(ResizeEvent event) {
				content.setHeight(Window.getClientHeight() - content.getAbsoluteTop() - 10 + "px");
			}
		});
		animationHolder = new FlowPanel();
		animationHolder.setStylePrimaryName("animated-options-panel");
		showOptions = new OptionButton("Show Options");
		showOptions.removeStyleName("eta-button");
		showOptions.setStyleName("eta-button-options-button", true);
		showOptions.setClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent showOptionsevent) {
				animateOptionsView(showOptions);
				// showOptions.setStyleDependentName("-options", true);
			}
		});

	}

	@Override
	protected void onLoad() {
		super.onLoad();
		content.setHeight(Window.getClientHeight() - content.getAbsoluteTop() - 10 + "px");
	}

	public void addTab(Tab tab) {
		if (tabs.contains(tab)) {
			loadTab(tabs.get(tabs.indexOf(tab)));
			return;
		}
		tabMap.put(tab.getId(), tab);
		if (tabs.size() == 0)
			tab.getHead().addStyleName("first-tab");
		tabs.add(tab);
		tabHeads.add(tab.getHead());
		loadTab(tab);
		tab.setListener(this);
		if (enableHistory) {
			History.newItem(History.getToken() + "," + tab.getId(), false);
		}

	}

	public void setManager(TabManager manager) {
		this.manager = manager;
	}

	public void enableHistory(boolean shouldI) {
		enableHistory = shouldI;
		if (shouldI) {
			if (handler == null)
				handler = History.addValueChangeHandler(this);
		} else {
			if (handler != null) {
				handler.removeHandler();
				handler = null;
			}
		}
	}

	public String getSelectedTab() {
		return currentTab.getId();
	}

	private void removeTab(Tab tab) {
		int tabLoc = tabs.indexOf(tab);
		tabs.remove(tab);
		tabHeads.remove(tab.getHead());
		if (tab.getHead().isSelected()) {
			currentTab = null;
			if (tabs.size() > 0)
				loadTab(tabs.get(tabLoc - 1));
		}
		setHistory();
	}

	public void removeTab(String id) {
		Tab tab = null;
		for (Tab temp : tabs) {
			if (temp.getId().equals(id)) {
				tab = temp;
				break;
			}
		}
		if (tab != null) {
			removeTab(tab);
		}
	}

	Timer t;

	private void loadTab(final Tab tab) {
		// if (tabs.size() == 1) {
		// t = new Timer() {
		// @Override
		// public void run() {
		// content.clear();
		// content.add(tab.getPane());
		// content.getElement().getStyle().setOverflow(Overflow.AUTO);
		// }
		// };
		// t.schedule(100);
		// } else {
		// if (t != null) {
		// t.cancel();
		// t = null;
		// }
		// }animationHolder
		content.clear();
		animationHolder.clear();
		new ShowAnimation().cancel();
		animationHolder.getElement().getStyle().setHeight(0, Unit.PCT);
		showOptions.setText("Show Options");

		content.add(tab.getPane());
		content.add(animationHolder);
		content.getElement().getStyle().setOverflow(Overflow.AUTO);

		if (currentTab != null) {
			currentTab.getHead().unSelect();
		}
		currentTab = tab;
		currentTab.getHead().select();
		tabBar.clear();
		if (currentTab.getBar() != null) {
			tabBar.add(new Filler(10));
			tabBar.add(new Seprator());
			tabBar.add(currentTab.getBar());
		}
		if (currentTab.getAnimatedPanel() != null) {
			animationHolder.clear();
			tabBar.add(showOptions);
			tabBar.add(new Seprator());
			animationHolder.add(tab.getAnimatedPanel());
		}
	}

	public void tabSelected(TabEvent event) {
		loadTab(event.getItem());
	}

	public void addTopButton(Widget butt) {
		topBar.add(butt);
	}

	public void addLowerButton(Widget butt) {
		bottomBar.add(butt);
	}

	public void tabClosed(TabEvent event) {
		int tabLoc = tabs.indexOf(event.getItem());
		tabs.remove(event.getItem());
		tabHeads.remove(event.getItem().getHead());
		if (event.getItem().getHead().isSelected()) {
			currentTab = null;
			loadTab(tabs.get(tabLoc - 1));
		}
		setHistory();
	}

	public void setHistory() {
		if (!enableHistory)
			return;
		String token = "";
		for (Tab tab : tabs) {
			token += "," + tab.getId();
		}
		enableHistory(false);
		History.newItem(token, false);
		enableHistory(true);
	}

	private boolean hasTab(String ident) {
		for (Tab tab : tabs) {
			if (ident.equals(tab.getId())) {
				return true;
			}
		}
		return false;
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		// we must set up the tabs
		setupTabs(event.getValue());
	}

	public void setupTabs(String value) {
		String tabString = URL.decode(value).replaceAll("%23", "#");
		String[] tabsA = tabString.split(",");
		enableHistory = false;
		for (String id : tabsA) {
			if (id != null) {
				if (!hasTab(id)) {
					// add it
					Tab newTab = manager.getTab(id);
					if (newTab != null)
						addTab(newTab);
				}
			}
		}
		Vector<Tab> tabsToRemove = new Vector<Tab>();
		for (Tab tab : tabs) {
			if (!tabString.contains(tab.getId()) && tab.getCanClose()) {
				tabsToRemove.add(tab);
			}
		}
		for (Tab temp : tabsToRemove)
			removeTab(temp);
		enableHistory = true;
		setHistory();
	}

	/**
	 * @param ident
	 */
	public void addTab(String ident) {
		Tab newTab = manager.getTab(ident);
		if (newTab != null)
			addTab(newTab);
	}

	/**
	 * @param ident
	 * @return
	 */
	public Tab getTab(String ident) {
		return tabMap.get(ident);
	}

	/**
	 * 
	 * Method handles the up and down motion of the animation panel.
	 * 
	 * @param options
	 * The button that contains the "Show/Hide Options"
	 * Needs to be passed so the Title can be changed.
	 * 
	 * @see Animations.java
	 */
	private void animateOptionsView(Button options) {
		if (options.getTitle().equals("Show Options")) {
			new ShowAnimation().run(1000);
			options.setText("Hide Options");
		} else {
			new HideAnimation().run(1000);
			options.setText("Show Options");// animationHolder.getElement().getStyle().setHeight(40, Unit.PCT);

		}

	}

	private class ShowAnimation extends Animation {

		public ShowAnimation() {
		}

		@Override
		protected void onUpdate(double progress) {
			animationHolder.getElement().getStyle().setHeight((progress) * 40, Unit.PCT);

		}

		@Override
		public void cancel() {
			super.cancel();

		}
	}

	private class HideAnimation extends Animation {
		public HideAnimation() {
		}

		@Override
		protected void onUpdate(double progress) {
			animationHolder.getElement().getStyle().setHeight((1 - progress) * 40, Unit.PCT);
		}

		@Override
		public void cancel() {
			super.cancel();

		}
	}
}
