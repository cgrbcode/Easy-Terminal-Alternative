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

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.SQLService;
import cgrb.eta.client.SQLServiceAsync;
import cgrb.eta.client.button.Filler;
import cgrb.eta.client.button.RatingButton;
import cgrb.eta.client.button.SimpleButton;
import cgrb.eta.client.button.SimpleLabel;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.wrapperrunner.CommandRunner;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SC {
	private static final SQLServiceAsync sqlService = (SQLServiceAsync) GWT.create(SQLService.class);

	public static void ask(String message,final ValueListener<String> listener){
		VerticalPanel panel = new VerticalPanel();
		final Window window;
		panel.add(new SimpleLabel(message));
		final TextBox input = new TextBox();
		input.setStyleName("eta-input2");
		panel.add(input);
		input.setFocus(true);
		window=new Window("Input?", panel,true);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				listener.returned(input.getValue());
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
	}
	
	public static void ask(String title,String message,final ValueListener<Boolean> listener){
		VerticalPanel panel = new VerticalPanel();
		final Window window;
		panel.add(new SimpleLabel(message));
		window=new Window(title, panel,true);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);

		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("Yes").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(listener!=null)
				listener.returned(true);
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		buttons.add(new SimpleButton("No").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				listener.returned(false);
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));

		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
	}
	
	public static void getRating(String what,int rating,final ValueListener<Integer> listener){
		VerticalPanel panel = new VerticalPanel();
		final Window window;
		final RatingButton ratingPanel = new RatingButton(rating);
		panel.add(ratingPanel);
		window=new Window(what, panel,true);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				listener.returned(ratingPanel.getRating());
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
	}
	public static void ask(String message,Widget content,final ValueListener<Boolean> listener,String width){
		VerticalPanel panel = new VerticalPanel();
		panel.add(content);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(message, panel,true);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(listener!=null)
				listener.returned(true);
				window.destroy();
			}
		}));
		window.setWidth(width);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
		
	}	
	public static void show(String message,Widget content){
		VerticalPanel panel = new VerticalPanel();
		panel.add(content);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(message, panel,true);

		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
	}
	
	public static void ask(String message,Widget content,final ValueListener<Boolean> listener){
		VerticalPanel panel = new VerticalPanel();
		panel.add(content);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(message, panel,true);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(listener!=null)
				listener.returned(true);
				window.destroy();
			}
		}));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
		
	}
	public static void ask(String message,Widget content,Widget bar,final ValueListener<Boolean> listener){
		VerticalPanel panel = new VerticalPanel();
		panel.add(content);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(message, panel,false);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if(listener!=null)
				listener.returned(true);
				window.destroy();
			}
		}));
		window.setWidth("80%");
		window.addBar(bar);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
		
	}
	public static void show(String message,Widget content,Widget bar){
		VerticalPanel panel = new VerticalPanel();
		panel.add(content);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(message, panel,true);
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		window.setWidth("80%");
		window.addBar(bar);
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
		
	}
	
	public static void alert(String title,String message){
		VerticalPanel panel = new VerticalPanel();
		panel.add(new SimpleLabel(message));
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setWidth("100%");
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		final Window window = new Window(title, panel,true);
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		panel.add(buttons);
		window.showWindow();
	}
	
	public static void getFileType(final ValueListener<String> listener){
		VerticalPanel panel = new VerticalPanel();
		final Window window;
		final MultiWordSuggestOracle orcle = new MultiWordSuggestOracle();
		final SuggestBox types = new SuggestBox(orcle);
		types.setStyleName("eta-input2");
		sqlService.getFileTypes(new MyAsyncCallback<String[]>() {
			@Override
			public void success(String[] result) {
				for(String type:result){
					orcle.add(type);
				}
			}
		});
		
		panel.add(types);
		window=new Window("Select a file type", panel,true);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.add(new SimpleButton("Cancel").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				window.destroy();
			}
		}));
		buttons.add(new Filler(10));
		buttons.add(new SimpleButton("OK").addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				listener.returned(types.getValue());
				window.destroy();
			}
		}));
		buttons.add(new Filler(15));
		panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		panel.add(buttons);
		window.showWindow();
	}
	
	public static void runCommand(){
		new CommandRunner();
	}
}
