package org.ngocminh.codebrowser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class ConsoleDocument extends PlainDocument {

	private static final long serialVersionUID = 716175549378590188L;

	public final Reader in = new ConsoleReader();

	public final Writer out = new ConsoleOutputWriter();

	public final Writer err = out;

	private int inStart = 0;

	private boolean writing = false;
	
	private StringBuilder buffer = new StringBuilder();

	public ConsoleDocument() {
	}

	public void clear() {
		try {
			super.remove(0, getLength());
			inStart = 0;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException {
		synchronized (buffer) {
			if (offs < inStart) {
				return;
			}
			super.insertString(offs, str, a);
			if (!writing) {
				if (str.indexOf('\n') >= 0) {
					int inEnd = getLength()
							- (str.length() - 1 - str.lastIndexOf('\n'));
					buffer.append(getText(inStart, inEnd - inStart));
					inStart = inEnd;
				}
				buffer.notifyAll();
			}
		}
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		synchronized (buffer) {
			if (offs < inStart) {
				return;
			}
			super.remove(offs, len);
		}
	}
	
	public boolean isWriting() {
		return writing;
	}

	private class ConsoleReader extends Reader {

		@Override
		public void close() throws IOException {
			// DO NOTHING
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			synchronized (buffer) {
				// wait for input
				while (buffer.length() <= 0) {
					try {
						buffer.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// copy data
				if (len > buffer.length()) {
					len = buffer.length();
				}
				buffer.getChars(off, off + len, cbuf, 0);
				buffer.delete(0, len);
				return len;
			}
		}

	}

	private class ConsoleOutputWriter extends Writer {

		@Override
		public void close() throws IOException {
			// DO NOTHING
		}

		@Override
		public void flush() throws IOException {
			// DO NOTHING
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			synchronized (buffer) {
				try {
					writing = true;
					insertString(getLength(), new String(cbuf, off, len), null);
					inStart = getLength();
				} catch (BadLocationException e) {
					e.printStackTrace();
				} finally {
					writing = false;
				}
			}
		}

	}

}
