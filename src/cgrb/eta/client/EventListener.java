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

import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.etatype.ETATypeEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * 
 * This singleton class provides a way for any component to listen for events that occur from the server. The idea is that this checks the server every 2 seconds for events using a short pooling method.<br>
 * I'm not sure if it is better to use a long term pooling method maybe I can change this latter if it is much better to do that method.
 * 
 * To get the instance of this class call <code>EventListener.getInstance();</code>
 * 
 * @author Alexander Boyd
 */
public class EventListener {

	/**
	 * The representation of this class. This will be the only instance of EventListener
	 */
	private static EventListener instance;

	/**
	 * This will ensure that only one instance of this class ever exists. And this method should be called if anyone ever wants to use this class
	 * 
	 * @return EventListener The singleton representation of this class.
	 */
	public static EventListener getInstance() {
		return instance == null ? instance = new EventListener() : instance;
	}

	private Timer t;
	private final CommunicationServiceAsync communicationService = (CommunicationServiceAsync) GWT.create(CommunicationService.class);

	private HashMap<Integer, Vector<EventOccuredListener>> listeners = new HashMap<Integer, Vector<EventOccuredListener>>();
	private HashMap<String, Vector<ETATypeEventOccurred<ETAType>>> typeListeners = new HashMap<String, Vector<ETATypeEventOccurred<ETAType>>>();

	private boolean running = false;
	private Label counterLabel = new Label("100s");

	private EventListener() {
		temp = new HorizontalPanel();
		HorizontalPanel errorStack = new HorizontalPanel();
		errorStack.setStyleName("errorLabel");
		temp.setWidth("100%");
		temp.setStyleName("errorstack");
		HTML errorLabel = new HTML();
		errorLabel.setHTML("<b>Lost Connection.</b> Please check your internet connection. Connecting in");
		Label tryNow = new Label("Try now");
		tryNow.setStyleName("trynow");
		tryNow.setWordWrap(false);
		// tryNow.addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent event) {
		// tryConnection();
		// }
		// });
		counterLabel.setHeight("20px");
		counterLabel.setWordWrap(false);
		tryNow.setHeight("20px");
		errorLabel.setWordWrap(false);
		errorLabel.setHeight("20px");
		errorStack.setHeight("20px");
		errorStack.add(errorLabel);
		errorStack.add(counterLabel);
		errorStack.add(tryNow);
		temp.add(errorStack);
		RootPanel.get().add(temp);
		temp.setVisible(false);
		temp.getElement().getStyle().setPosition(Position.ABSOLUTE);
		temp.getElement().getStyle().setTop(0, Unit.PX);
		addListener(ETAEvent.ETA_TYPE, new EventOccuredListener() {
			public void eventOccured(ETAEvent event, int user) {
				ETATypeEvent<? extends ETAType> source = ((ETATypeEvent<?>) event.getSource());
				String type = source.getItem().getClass().getName();
				Vector<ETATypeEventOccurred<ETAType>> listeners = typeListeners.get(type);
				if (listeners == null)
					return;
				for (ETATypeEventOccurred<ETAType> listener : listeners) {
					if (listener != null) {
						switch (source.getType()) {
						case ETATypeEvent.ADDED:
							listener.onAddition(source.getItem());
							break;
						case ETATypeEvent.REMOVED:
							listener.onRemoval(source.getItem());
							break;
						case ETATypeEvent.UPDATED:
							listener.onUpdate(source.getItem());
							break;
						}
					} else {
						// remove the listener from listeners somehow
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void addETATypeListener(String type, ETATypeEventOccurred<? extends ETAType> listener) {
		Vector<ETATypeEventOccurred<ETAType>> listeners = typeListeners.get(type);
		if (listeners == null)
			listeners = new Vector<ETATypeEventOccurred<ETAType>>();
		listeners.add((ETATypeEventOccurred<ETAType>) listener);
		typeListeners.put(type, listeners);
	}

	public void addListener(int type, EventOccuredListener listener) {
		if (listeners.containsKey(type)) {
			listeners.get(type).add(listener);
		} else {
			Vector<EventOccuredListener> temp = new Vector<EventOccuredListener>();
			temp.add(listener);
			listeners.put(type, temp);
		}
		checkEvents();
	}

	int attempt = 0;
	int time = 3;
	HorizontalPanel temp;
	Timer counter = new Timer() {
		@Override
		public void run() {
			counterLabel.setText(time-- + "s");
			if (time <= 0) {
				tryConnection();
			}
		}
	};
	/**
	 * checkEvents is the code that looks for new events that are occurring. It checks every 1000 milliseconds and notifies the handler that it heard something.
	 * 
	 * @see cgrb.eta.server.CommunicationImpl.java
	 * @see cgrb.eta.shared.ETAEvent.java
	 */
	private void checkEvents() {
		if (running) {
			return;
		}
		running = true;
		t = new Timer() {

			@Override
			public void run() {
				communicationService.getEvents(new MyAsyncCallback<Vector<ETAEvent>>() {
					public void onFailure(Throwable caught) {
						if (caught.getLocalizedMessage().startsWith("456")) {
							com.google.gwt.user.client.Window.open("/ServiceLogin.html?send=" + Location.getHref(), "_self", "");
							return;
						}
						t.cancel();
						attempt++;
						temp.setVisible(true);
						counterLabel.setText(time-- + "s");
						counter.scheduleRepeating(1000);
					}

					@Override
					public void success(Vector<ETAEvent> result) {
						if (attempt > 0) {
							attempt = 0;
							temp.setVisible(false);
							counter.cancel();
						}
						if(result==null){
							com.google.gwt.user.client.Window.open("/ServiceLogin.html?send=" + Location.getHref(), "_self", "");
						}
						if (result.size() == 0) {
							t.schedule(1000);
							return;
						}
						for (ETAEvent jE : result) {
							int type = jE.getType();
							if (type == ETAEvent.LOGOUT) {
								Window.Location.reload();
							}
							Vector<EventOccuredListener> temp = listeners.get(type);
							if (temp != null) {
								for (EventOccuredListener jl : temp) {
									jl.eventOccured(jE, 1);
								}
							}
						}
						t.schedule(1000);
					}
				});
			}
		};
		t.schedule(1000);
	}

	private void tryConnection() {
		time = attempt * 5;
		counter.cancel();
		t.scheduleRepeating(1000);
	}
}
