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
package cgrb.eta.client.wrapperrunner;

import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DropListener;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.wrapper.Output;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;

public abstract class ValueInput extends Composite implements KeyUpHandler{
	protected String value;
	public abstract void setValue(String defaultValue);
	public abstract String getValue();
	private ValueChangeHandler<String> changeHandler;
	private  Timer timer;
	protected String[] values;
	public void setChangeHandeler(ValueChangeHandler<String> hand){
		changeHandler=hand;
		timer=new Timer() {
			@Override
			public void run() {
				update();
			}
		};
	}

	public void setupDrop(){
		DragCreator.addDrop(getElement(), null, new DropListener() {
			public void drop(ETAType record) {
				ETAType rec = DragCreator.getDragSource();
				if(rec instanceof Output){
					Output out = (Output)rec;
					setValue("$'"+out.getName()+"'");
				}
			}
			public void dragOver(ETAType record) {
			}
			
			public void dragLeave(ETAType record) {
			}
			
			public void dragEnter(ETAType record) {
			}
		});
	}
	protected void update(){
		if(changeHandler!=null)
		changeHandler.onValueChange(null);
	}
	
	public void onKeyUp(KeyUpEvent event) {
		timer.cancel();
		timer.schedule(200);
	}
	public String[] getValues(){
		if(values==null|| values.length==0)
			return new String[]{getValue()};
		return values;
	}
}
