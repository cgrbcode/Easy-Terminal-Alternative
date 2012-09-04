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
package cgrb.eta.client.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Image;

import cgrb.eta.shared.etatype.ETAType;

public class DragCreator {

	private static ETAType draggedRecord;
	public static final int MOVE = 1;
	public static final int COPY = 2;
	public static final int DELETE = 3;

	public static native void addDrag(Element element, ETAType rec, DragListener listener) /*-{
		function handleDragStart(e) {
			var dragIcon = listener.@cgrb.eta.client.table.DragListener::getDragImage(Lcgrb/eta/shared/etatype/ETAType;)(rec);
			e.dataTransfer.setDragImage(dragIcon, -10, -10);
			e.dataTransfer.effectAllowed = 'all'; // only dropEffect='copy' will be dropable
			@cgrb.eta.client.table.DragCreator::draggedRecord = rec;
			listener.@cgrb.eta.client.table.DragListener::dragStart(Lcgrb/eta/shared/etatype/ETAType;)(rec);
			if (element.getAttribute("data-downloadurl") != null) {
				e.dataTransfer.setData("DownloadURL", element.getAttribute("data-downloadurl"));
			} else {
				e.dataTransfer.setData('Text',	rec.@cgrb.eta.shared.etatype.ETAType::getId()()); // required otherwise doesn't work
			}
		}

		function handleDragOver(e) {
			if (e.preventDefault)
				e.preventDefault();
			listener.@cgrb.eta.client.table.DragListener::dragOver(Lcgrb/eta/shared/etatype/ETAType;)(rec);

			//e.dataTransfer.dropEffect = 'move'; // See the section on the DataTransfer object.
			//this.style.border="1px dashed #84B4EA";
			return false;
		}

		function handleDragEnter(e) {
			// this / e.target is the current hover target.
			listener.@cgrb.eta.client.table.DragListener::dragEnter(Lcgrb/eta/shared/etatype/ETAType;)(rec);
			return false;
		}

		function handleDragLeave(e) {
			listener.@cgrb.eta.client.table.DragListener::dragLeave(Lcgrb/eta/shared/etatype/ETAType;)(rec);
		}
		function handleDrop(e) {
			if (e.stopPropagation)
				e.stopPropagation(); // stops the browser from redirecting...why???
			if (e.preventDefault)
				e.preventDefault();
			// this / e.target is current target element.
			listener.@cgrb.eta.client.table.DragListener::drop(Lcgrb/eta/shared/etatype/ETAType;)(rec);
			return false;
		}

		function handleDragEnd(e) {
			listener.@cgrb.eta.client.table.DragListener::dragEnd(Lcgrb/eta/shared/etatype/ETAType;)(rec);
		}

		element.addEventListener('dragstart', handleDragStart, false);
		element.addEventListener('dragenter', handleDragEnter, false);
		element.addEventListener('dragover', handleDragOver, false);
		element.addEventListener('dragleave', handleDragLeave, false);
		element.addEventListener('drop', handleDrop, false);
		element.addEventListener('dragend', handleDragEnd, false);
	}-*/;

	public static native void addDrop(Element element, ETAType rec, DropListener listener) /*-{

		function handleDragOver(e) {
			listener.@cgrb.eta.client.table.DropListener::dragOver(Lcgrb/eta/shared/etatype/ETAType;)(rec);

			if (e.preventDefault) {
				e.preventDefault(); // Necessary. Allows us to drop.
			}

			//e.dataTransfer.dropEffect = 'move'; // See the section on the DataTransfer object.
			//this.style.border="1px dashed #84B4EA";
			return false;
		}

		function handleDragEnter(e) {
			// this / e.target is the current hover target.
			listener.@cgrb.eta.client.table.DropListener::dragEnter(Lcgrb/eta/shared/etatype/ETAType;)(rec);
		}

		function handleDragLeave(e) {
			listener.@cgrb.eta.client.table.DropListener::dragLeave(Lcgrb/eta/shared/etatype/ETAType;)(rec);
		}
		function handleDrop(e) {
			// this / e.target is current target element.
			listener.@cgrb.eta.client.table.DropListener::drop(Lcgrb/eta/shared/etatype/ETAType;)(rec);
			if (e.preventDefault)
				e.preventDefault();
			if (e.stopPropagation) {
				e.stopPropagation(); // stops the browser from redirecting.
			}

			// See the section on the DataTransfer object.

			return false;
		}

		element.addEventListener('dragenter', handleDragEnter, false);
		element.addEventListener('dragover', handleDragOver, false);
		element.addEventListener('dragleave', handleDragLeave, false);
		element.addEventListener('drop', handleDrop, false);
	}-*/;

	public static ETAType getDragSource() {
		return draggedRecord;
	}

	public static Element getImageElement(String src) {
		Image img = new Image(src);
		img.setWidth("20px");
		img.setHeight("20px");
		return img.getElement();
	}
}
