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
package cgrb.eta.client.tabs;

import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

import cgrb.eta.client.MyAsyncCallback;
import cgrb.eta.client.VoidAsyncCallback;
import cgrb.eta.client.WrapperService;
import cgrb.eta.client.WrapperServiceAsync;
import cgrb.eta.client.button.StarButton;
import cgrb.eta.client.button.ValueListener;
import cgrb.eta.client.images.Resources;
import cgrb.eta.client.table.Column;
import cgrb.eta.client.table.DragCreator;
import cgrb.eta.client.table.DragListener;
import cgrb.eta.client.table.RowClickHandler;
import cgrb.eta.client.table.Table;
import cgrb.eta.client.tabset.ETATab;
import cgrb.eta.client.window.SC;
import cgrb.eta.shared.etatype.ETAType;
import cgrb.eta.shared.pipeline.Pipeline;

public class PipelinesTab extends ETATab implements RowClickHandler<Pipeline>,DragListener{

	private final WrapperServiceAsync wrapperService = (WrapperServiceAsync)GWT.create(WrapperService.class);
	private Table<Pipeline> pipelines;
	public PipelinesTab(){
		super("Public Pipelines");
		
		Column<Pipeline> icon = new Column<Pipeline>("") {

			@Override
			public Object getValue(Pipeline record) {
				Image icon = new Image();
				icon.setWidth("16px");
				icon.setHeight("16px");
				if(record.isPublic()){
					icon.setUrl(Resources.INSTANCE.publicPipeline().getSafeUri().asString());
				}else
					icon.setUrl(Resources.INSTANCE.pipeline().getSafeUri().asString());
				return icon;
			}
			@Override
			public String getWidth() {
				return "20px";
			}
		};
		
		Column<Pipeline> name = new Column<Pipeline>(" Name") {
			@Override
			public Object getValue(Pipeline record) {
				return record.getName();
			}
			@Override
			public boolean canSort(){return true;}
			
			@Override
			public int compareTo(Pipeline o1, Pipeline o2) {
				return o1.getName().compareTo(o2.getName());
			}

			@Override
			public String getWidth() {
				return "200px";
			}
		};
		Column<Pipeline> description = new Column<Pipeline>("Description") {
			@Override
			public Object getValue(Pipeline record) {
				return record.getDescription();
			}
		};
		Column<Pipeline> creator = new Column<Pipeline>("Creator") {
			@Override
			public Object getValue(Pipeline record) {
				return record.getCreator();
			}
			@Override
			public boolean canSort(){return true;}
			
			@Override
			public int compareTo(Pipeline o1, Pipeline o2) {
				return o1.getCreator().compareTo(o2.getCreator());
			}
			@Override
			public String getWidth() {
				return "100px";
			}
		};
		
		Column<Pipeline> star = new Column<Pipeline>("My") {
			@Override
			public Object getValue(Pipeline record) {
				StarButton star = new StarButton();
				star.setStared(record.isStared());
				star.setSize(20);
				return star;
			}

			@Override
			public String getWidth() {
				return "25px";
			}
		};
		Column<Pipeline> stars = new Column<Pipeline>("Rating") {
			@Override
			public Object getValue(Pipeline record) {
				if(record.getRating()==0)
					return "N/A";
				return record.getRating() + "/5";
			}
			@Override
			public boolean canSort(){return true;}
			
			@Override
			public int compareTo(Pipeline o1, Pipeline o2) {
				return o1.getRating()-o2.getRating();
			}

			@Override
			public String getWidth() {
				return "50px";
			}
		};
		pipelines=new Table<Pipeline>(true,icon,star,stars, name,description,creator);
		pipelines.setWidth("100%");
		VerticalPanel pane = new VerticalPanel();
		pane.add(pipelines);
		setPane(pane);
		wrapperService.getPipelines(new MyAsyncCallback<Vector<Pipeline>>() {
			@Override
			public void success(Vector<Pipeline> result) {
				pipelines.setData(result);
			};
		});
		pipelines.setRowClickHandler(this);
		pipelines.setDragListener(this);
	}
	
	@Override
	public String getId() {
		return "pp";
	}

	public void rowClicked(final Pipeline record, int col, int row) {
		if(col==1){
			StarButton star = (StarButton) pipelines.getWidgetAt(row, col);
			star.setStared(true);
			SC.getRating("Rate the pipeline "+record.getName(), record.getRating(),new ValueListener<Integer>() {
				public void returned(Integer ret) {
					wrapperService.ratePipeline(ret, record.getId(), new VoidAsyncCallback());
				}
			});
			return;
		}
		//otherwise bring up the more indepth wrapper view
		
	}

	public void dragStart(ETAType record) {
		Element el = pipelines.getElementForRecord((Pipeline) record);
		el.getStyle().setOpacity(.5);		
	}

	public void dragEnter(ETAType record) {
		
	}

	public void dragOver(ETAType record) {
		
	}

	public void dragLeave(ETAType record) {
		
	}

	public void drop(ETAType record) {
		
	}

	public void dragEnd(ETAType record) {
		Element el = pipelines.getElementForRecord((Pipeline) record);
		el.getStyle().setOpacity(1);		
	}

	public Element getDragImage(ETAType rec) {
		Pipeline record = (Pipeline)rec;
		if (record.isPublic()) {
			 return DragCreator.getImageElement("../images/gear_public.png");
		} else {
			 return DragCreator.getImageElement("../images/gear.png");
		}
	}

}
