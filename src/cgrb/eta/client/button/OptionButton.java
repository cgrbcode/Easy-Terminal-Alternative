package cgrb.eta.client.button;

import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpHandler;

/*
 * So get this working such that the hover matches the rest of the system. Either for free-time or for when you're back working on this
 * thursday. 
 * 
 * 
 * 
 */
/**
 * Button that is used to deploy the animated drop-down.
 * 
 * The reason for this class is to add in the onhover functionality for our options button. Without it, we cannot have a hoverable button that is independent of the others.
 * 
 * @author Steven Hill
 * 
 */
public class OptionButton extends Button {
	protected MouseOutHandler up;
	protected MouseOverHandler down;

	public MouseOutHandler getUp() {
		return up;
	}

	public void setUp(MouseOutHandler up) {
		this.up = up;
	}

	public MouseOverHandler getDown() {
		return down;
	}

	public void setDown(MouseOverHandler down) {
		this.down = down;
	}

	public OptionButton() {
		super();
	}

	public OptionButton(String name) {
		super(name);
	}

	public void onMouseOver(MouseOverEvent event) {
		// This button must of been clicked. Change the css state and notify the handler if there is one.
		removeStyleName("option-button-down");
		addStyleName("eta-button-options-button");
		if (handler != null)
			handler.onClick(null);
	}

	public void onMouseOut(MouseOutEvent event) {
		removeStyleName("eta-button-options-button");
		addStyleName("option-button-down");
	}

}
