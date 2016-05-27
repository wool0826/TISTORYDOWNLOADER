import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

public class GhostText implements FocusListener, DocumentListener, PropertyChangeListener {
	private final JTextComponent textComp;
	private boolean isEmpty;
	private Color ghostColor;
	private Color foregroundColor;
	private final String ghostText;

	public GhostText(JTextComponent textComp, String ghostText) {
		this.textComp = textComp;
		this.ghostText = ghostText;
		ghostColor = Color.LIGHT_GRAY;
		textComp.addFocusListener(this);
		registerListeners();
		updateState();
		if (!this.textComp.hasFocus()) {
			focusLost(null);
		}
	}

	public void delete() {
		unregisterListeners();
		this.textComp.removeFocusListener(this);
	}

	private void registerListeners() {
		this.textComp.getDocument().addDocumentListener(this);
		this.textComp.addPropertyChangeListener("foreground", this);
	}

	private void unregisterListeners() {
		this.textComp.getDocument().removeDocumentListener(this);
		this.textComp.removePropertyChangeListener("foreground", this);
	}

	public Color getGhostColor() {
		return this.ghostColor;
	}

	public void setGhostColor(Color ghostColor) {
		this.ghostColor = ghostColor;
	}

	private void updateState() {
		this.isEmpty = (this.textComp.getText().length() == 0);
		this.foregroundColor = this.textComp.getForeground();
	}

	public void focusGained(FocusEvent e) {
		if (this.isEmpty) {
			unregisterListeners();
			try {
				this.textComp.setText("");
				this.textComp.setForeground(this.foregroundColor);
			} finally {
				registerListeners();
			}
		}
	}

	public void focusLost(FocusEvent e) {
		if (this.isEmpty) {
			unregisterListeners();
			try {
				this.textComp.setText(this.ghostText);
				this.textComp.setForeground(this.ghostColor);
			} finally {
				registerListeners();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		updateState();
	}

	public void changedUpdate(DocumentEvent e) {
		updateState();
	}

	public void insertUpdate(DocumentEvent e) {
		updateState();
	}

	public void removeUpdate(DocumentEvent e) {
		updateState();
	}
}