package org.ngocminh.codebrowser;

import java.awt.Font;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Console extends JTextArea implements DocumentListener {

	private static final long serialVersionUID = -6212663554837456014L;
	
	private Runner runner;
	
	private ConsoleDocument doc;

	public Console(ConsoleDocument doc) {
		super(doc);
		this.doc = doc;
		setFont(new Font("Monospaced", Font.PLAIN, 13)); //$NON-NLS-1$
		doc.addDocumentListener(this);
	}

	public Runner getRunner() {
		return runner;
	}
	
	void setRunner(Runner runner) {
		this.runner = runner;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (doc.isWriting()) {
			setCaretPosition(doc.getLength());
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		// DO NOTHING
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// DO NOTHING
	}
	
}
