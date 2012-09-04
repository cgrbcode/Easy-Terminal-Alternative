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

import java.util.ArrayList;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Alexander Boyd<br><br>
 *	<b>Purpose:<\b> A static class that provides a simple way for components to be notified when the Enter button on the keyboard is pressed.
 */
public class EnterButtonEventManager {
	/**
	 * A list of all the EnterListeners that will be notified of any enter event
	 */
	private static ArrayList<EnterListener> listeners = new ArrayList<EnterListener>();

	/**
	 * The KeyUpHandler that will listen to all keyboard key presses and when Enter is pressed go though all the listeners array list and notify each EnterListener
	 */
	private static KeyUpHandler handler = new KeyUpHandler() {
		public void onKeyUp(KeyUpEvent event) {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				EnterListener listener=	listeners.get(listeners.size()-1);
				if(listener!=null){
					listener.enter();
				}
			}
		}
	};
	
	/**
	 * Add the KeyUpHandler to the root panel so we can be notified of all key events
	 */
	static{
		RootPanel.get().addDomHandler(handler,KeyUpEvent.getType() );
	}
	/**
	 * @param listener The EnterListener that wants to be notified of Enter events
	 */
	public static void addListener(EnterListener listener){
		listeners.add(listener);
	}
	
	/**
	 * @param listener The EnterListener that no longer want to be notified of enter events
	 */
	public static void removeListener(EnterListener listener){
		listeners.remove(listener);
	}

}
