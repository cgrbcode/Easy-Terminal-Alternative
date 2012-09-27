package cgrb.eta.client.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;

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
public class OptionButton extends Button implements MouseOverHandler, MouseOutHandler {
	private MouseOutHandler out;
	private MouseOverHandler over;

	public MouseOutHandler getOut() {
		return out;
	}

	public void setOut(MouseOutHandler out) {
		this.out = out;
	}

	public MouseOverHandler getOver() {
		return over;
	}

	public void setOver(MouseOverHandler over) {
		this.over = over;
	}

	public OptionButton() {
		super();
	}

	public OptionButton(String name) {
		super(name);
	}

	public void onMouseOver(MouseOverEvent event) {
		// This button must of been clicked. Change the css state and notify the handler if there is one.
		super.onMouseOver(event);
	//	removeStyleName("button-down-options-button");
		setStyleName("eta-button-options-over");
		
	}

	public void onMouseOut(MouseOutEvent event) {
		super.onMouseOut(event);
		//removeStyleName("eta-button-options-over");
		setStyleName("eta-button-options-button");
	}
	
	@Override
	public void onClick(ClickEvent event) {
		removeStyleName("eta-button-options-over");
		addStyleName("button-down-animate-button");
		if (toolTip != null)
			toolTip.hide();
	}

}
