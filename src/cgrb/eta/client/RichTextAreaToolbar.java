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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.Constants;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A sample toolbar for use with {@link RichTextArea}. It provides a simple UI for all rich text formatting, dynamically displayed only for the available functionality.
 */
public class RichTextAreaToolbar extends Composite {

	/**
	 * This {@link ImageBundle} is used for all the button icons. Using an image bundle allows all of these images to be packed into a single image, which saves a lot of HTTP requests, drastically improving startup time.
	 */
	public interface Images extends ClientBundle {

		@Source("images/bold.gif")
		ImageResource bold();

		@Source("images/createLink.gif")
		ImageResource createLink();

		@Source("images/hr.gif")
		ImageResource hr();

		@Source("images/indent.gif")
		ImageResource indent();

		@Source("images/insertImage.gif")
		ImageResource insertImage();

		@Source("images/italic.gif")
		ImageResource italic();

		@Source("images/justifyCenter.gif")
		ImageResource justifyCenter();

		@Source("images/justifyLeft.gif")
		ImageResource justifyLeft();

		@Source("images/justifyRight.gif")
		ImageResource justifyRight();

		@Source("images/ol.gif")
		ImageResource ol();

		@Source("images/outdent.gif")
		ImageResource outdent();

		@Source("images/removeFormat.gif")
		ImageResource removeFormat();

		@Source("images/removeLink.gif")
		ImageResource removeLink();

		@Source("images/strikeThrough.gif")
		ImageResource strikeThrough();

		@Source("images/subscript.gif")
		ImageResource subscript();

		@Source("images/superscript.gif")
		ImageResource superscript();

		@Source("images/ul.gif")
		ImageResource ul();

		@Source("images/underline.gif")
		ImageResource underline();
	}

	/**
	 * This {@link Constants} interface is used to make the toolbar's strings internationalizable.
	 */
	public interface Strings extends Constants {

		String black();

		String blue();

		String bold();

		String color();

		String createLink();

		String font();

		String green();

		String hr();

		String indent();

		String insertImage();

		String italic();

		String justifyCenter();

		String justifyLeft();

		String justifyRight();

		String large();

		String medium();

		String normal();

		String ol();

		String outdent();

		String red();

		String removeFormat();

		String removeLink();

		String size();

		String small();

		String strikeThrough();

		String subscript();

		String superscript();

		String ul();

		String underline();

		String white();

		String xlarge();

		String xsmall();

		String xxlarge();

		String xxsmall();

		String yellow();
	}

	/**
	 * We use an inner EventHandler class to avoid exposing event methods on the RichTextToolbar itself.
	 */
	private class EventHandler implements ClickHandler, ChangeHandler, KeyUpHandler {

		public void onChange(ChangeEvent event) {
			Widget sender = (Widget) event.getSource();

			if (sender == backColors) {
				basic.setBackColor(backColors.getValue(backColors.getSelectedIndex()));
				backColors.setSelectedIndex(0);
			} else if (sender == foreColors) {
				basic.setForeColor(foreColors.getValue(foreColors.getSelectedIndex()));
				foreColors.setSelectedIndex(0);
			} else if (sender == fonts) {
				basic.setFontName(fonts.getValue(fonts.getSelectedIndex()));
				fonts.setSelectedIndex(0);
			} else if (sender == fontSizes) {
				basic.setFontSize(fontSizesConstants[fontSizes.getSelectedIndex() - 1]);
				fontSizes.setSelectedIndex(0);
			}
		}

		public void onClick(ClickEvent event) {
			Widget sender = (Widget) event.getSource();

			if (sender == bold) {
				basic.toggleBold();
			} else if (sender == italic) {
				basic.toggleItalic();
			} else if (sender == underline) {
				basic.toggleUnderline();
			} else if (sender == subscript) {
				basic.toggleSubscript();
			} else if (sender == superscript) {
				basic.toggleSuperscript();
			} else if (sender == strikethrough) {
				basic.toggleStrikethrough();
			} else if (sender == indent) {
				basic.rightIndent();
			} else if (sender == outdent) {
				basic.leftIndent();
			} else if (sender == justifyLeft) {
				basic.setJustification(RichTextArea.Justification.LEFT);
			} else if (sender == justifyCenter) {
				basic.setJustification(RichTextArea.Justification.CENTER);
			} else if (sender == justifyRight) {
				basic.setJustification(RichTextArea.Justification.RIGHT);
			} else if (sender == insertImage) {
				String url = Window.prompt("Enter an image URL:", "http://");
				if (url != null) {
					basic.insertImage(url);
				}
			} else if (sender == createLink) {
				String url = Window.prompt("Enter a link URL:", "http://");
				if (url != null) {
					basic.createLink(url);
				}
			} else if (sender == removeLink) {
				basic.removeLink();
			} else if (sender == hr) {
				basic.insertHorizontalRule();
			} else if (sender == ol) {
				basic.insertOrderedList();
			} else if (sender == ul) {
				basic.insertUnorderedList();
			} else if (sender == removeFormat) {
				basic.removeFormat();
			} else if (sender == richText) {
				// We use the RichTextArea's onKeyUp event to update the toolbar status.
				// This will catch any cases where the user moves the cursur using the
				// keyboard, or uses one of the browser's built-in keyboard shortcuts.
				updateStatus();
			}
		}

		public void onKeyUp(KeyUpEvent event) {
			Widget sender = (Widget) event.getSource();
			if (sender == richText) {
				// We use the RichTextArea's onKeyUp event to update the toolbar status.
				// This will catch any cases where the user moves the cursur using the
				// keyboard, or uses one of the browser's built-in keyboard shortcuts.
				updateStatus();
			}
		}
	}

	private static final RichTextArea.FontSize[] fontSizesConstants = new RichTextArea.FontSize[] { RichTextArea.FontSize.XX_SMALL, RichTextArea.FontSize.X_SMALL, RichTextArea.FontSize.SMALL, RichTextArea.FontSize.MEDIUM, RichTextArea.FontSize.LARGE, RichTextArea.FontSize.X_LARGE,
			RichTextArea.FontSize.XX_LARGE };

	private Strings strings = (Strings) GWT.create(Strings.class);
	private EventHandler handler = new EventHandler();

	private RichTextArea richText;
	private RichTextArea.Formatter basic;

	private VerticalPanel outer = new VerticalPanel();
	private HorizontalPanel topPanel = new HorizontalPanel();
	private HorizontalPanel bottomPanel = new HorizontalPanel();
	private ToggleButton bold;
	private ToggleButton italic;
	private ToggleButton underline;
	private ToggleButton subscript;
	private ToggleButton superscript;
	private ToggleButton strikethrough;
	private PushButton indent;
	private PushButton outdent;
	private PushButton justifyLeft;
	private PushButton justifyCenter;
	private PushButton justifyRight;
	private PushButton hr;
	private PushButton ol;
	private PushButton ul;
	private PushButton insertImage;
	private PushButton createLink;
	private PushButton removeLink;
	private PushButton removeFormat;

	private ListBox backColors;
	private ListBox foreColors;
	private ListBox fonts;
	private ListBox fontSizes;

	/**
	 * Creates a new toolbar that drives the given rich text area.
	 * 
	 * @param richText
	 *          the rich text area to be controlled
	 */
	public RichTextAreaToolbar(RichTextArea richText) {
		this.richText = richText;
		this.basic = richText.getFormatter();
		Images images = GWT.create(Images.class);
		outer.add(topPanel);
		outer.add(bottomPanel);
		topPanel.setWidth("100%");
		bottomPanel.setWidth("100%");
		HorizontalPanel temp = new HorizontalPanel();
		temp.add(outer);
		initWidget(temp);
		setStyleName("gwt-RichTextToolbar");
		richText.addStyleName("hasRichTextToolbar");

		if (basic != null) {
			topPanel.add(bold = createToggleButton(images.bold(), strings.bold()));
			topPanel.add(italic = createToggleButton(images.italic(), strings.italic()));
			topPanel.add(underline = createToggleButton(images.underline(), strings.underline()));
			topPanel.add(subscript = createToggleButton(images.subscript(), strings.subscript()));
			topPanel.add(superscript = createToggleButton(images.superscript(), strings.superscript()));
			topPanel.add(justifyLeft = createPushButton(images.justifyLeft(), strings.justifyLeft()));
			topPanel.add(justifyCenter = createPushButton(images.justifyCenter(), strings.justifyCenter()));
			topPanel.add(justifyRight = createPushButton(images.justifyRight(), strings.justifyRight()));
		}

		if (basic != null) {
			topPanel.add(strikethrough = createToggleButton(images.strikeThrough(), strings.strikeThrough()));
			topPanel.add(indent = createPushButton(images.indent(), strings.indent()));
			topPanel.add(outdent = createPushButton(images.outdent(), strings.outdent()));
			topPanel.add(hr = createPushButton(images.hr(), strings.hr()));
			topPanel.add(ol = createPushButton(images.ol(), strings.ol()));
			topPanel.add(ul = createPushButton(images.ul(), strings.ul()));
			topPanel.add(insertImage = createPushButton(images.insertImage(), strings.insertImage()));
			topPanel.add(createLink = createPushButton(images.createLink(), strings.createLink()));
			topPanel.add(removeLink = createPushButton(images.removeLink(), strings.removeLink()));
			topPanel.add(removeFormat = createPushButton(images.removeFormat(), strings.removeFormat()));
		}

		if (basic != null) {
			bottomPanel.add(backColors = createColorList("Background"));
			bottomPanel.add(foreColors = createColorList("Foreground"));
			bottomPanel.add(fonts = createFontList());
			bottomPanel.add(fontSizes = createFontSizes());

			// We only use these handlers for updating status, so don't hook them up
			// unless at least basic editing is supported.
			richText.addKeyUpHandler(handler);
			richText.addClickHandler(handler);
		}
	}

	private ListBox createColorList(String caption) {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(caption);
		lb.addItem(strings.white(), "white");
		lb.addItem(strings.black(), "black");
		lb.addItem(strings.red(), "red");
		lb.addItem(strings.green(), "green");
		lb.addItem(strings.yellow(), "yellow");
		lb.addItem(strings.blue(), "blue");
		return lb;
	}

	private ListBox createFontList() {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(strings.font(), "");
		lb.addItem(strings.normal(), "");
		lb.addItem("Times New Roman", "Times New Roman");
		lb.addItem("Arial", "Arial");
		lb.addItem("Courier New", "Courier New");
		lb.addItem("Georgia", "Georgia");
		lb.addItem("Trebuchet", "Trebuchet");
		lb.addItem("Verdana", "Verdana");
		return lb;
	}

	private ListBox createFontSizes() {
		ListBox lb = new ListBox();
		lb.addChangeHandler(handler);
		lb.setVisibleItemCount(1);

		lb.addItem(strings.size());
		lb.addItem(strings.xxsmall());
		lb.addItem(strings.xsmall());
		lb.addItem(strings.small());
		lb.addItem(strings.medium());
		lb.addItem(strings.large());
		lb.addItem(strings.xlarge());
		lb.addItem(strings.xxlarge());
		return lb;
	}

	private PushButton createPushButton(ImageResource img, String tip) {
		PushButton pb = new PushButton(new Image(img));
		pb.addClickHandler(handler);
		pb.setTitle(tip);
		return pb;
	}

	private ToggleButton createToggleButton(ImageResource img, String tip) {
		ToggleButton tb = new ToggleButton(new Image(img));
		tb.addClickHandler(handler);
		tb.setTitle(tip);
		return tb;
	}

	/**
	 * Updates the status of all the stateful buttons.
	 */
	private void updateStatus() {
		if (basic != null) {
			bold.setDown(basic.isBold());
			italic.setDown(basic.isItalic());
			underline.setDown(basic.isUnderlined());
			subscript.setDown(basic.isSubscript());
			superscript.setDown(basic.isSuperscript());
		}

		if (basic != null) {
			strikethrough.setDown(basic.isStrikethrough());
		}
	}
}
