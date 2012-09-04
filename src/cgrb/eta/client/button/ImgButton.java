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

import com.google.gwt.resources.client.DataResource;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class ImgButton extends Button{

	
	DataResource src;
	/**
	 * @param src The location of the image for the icon
	 */
	Image icon;

	public ImgButton(DataResource src,int size,String tooltip) {
		icon = new Image(src.getSafeUri().asString());
		this.src=src;
		icon.setWidth(size+"px");
		icon.setHeight(size+"px");
		icon.setPixelSize(size, size);
		add(icon);
		setHeight(size+"px");
		setStyleDependentName("-img", true);
		setToolTip(tooltip);
	}
	
	public ImgButton(DataResource src) {
		icon = new Image(src.getSafeUri().asString());
		icon.setWidth("25px");
		icon.setHeight("25px");
		add(icon);
		setHeight("25px");
		setStyleDependentName("-img", true);
	}
	
	@Override
	public void setSize(int size){
		icon.setWidth(size+"px");
		icon.setHeight(size+"px");
		setHeight(size+"px");
	}
	public ImgButton(DataResource src,String name) {
		icon = new Image(src.getSafeUri().asString());
		icon.setWidth("20px");
		icon.setHeight("20px");
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		add(icon);
		Label title = new Label(name);
		title.setStyleName("button-text");
		title.setWordWrap(false);
		add(title);
		setHeight("25px");
		setStyleDependentName("-img", true);
	}
	public ImgButton(DataResource src,String name, int size) {
		icon = new Image(src.getSafeUri().asString());
		icon.setWidth("20px");
		icon.setHeight("20px");
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		add(icon);
		Label title = new Label(name);
		title.setStyleName("button-text");
		title.setWordWrap(false);
		add(title);
		setHeight(size+"px");
		setStyleDependentName("-img", true);
	}
	public DataResource getResource(){
		return src;
	}
}
