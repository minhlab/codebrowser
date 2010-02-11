package org.ngocminh.codebrowser;

import java.io.BufferedReader;
import java.io.PrintWriter;

import org.ngocminh.codebrowser.i18n.Messages;

public abstract class Runner implements Runnable {

	protected ConsoleDocument doc = new ConsoleDocument();

	protected BufferedReader in = new BufferedReader(doc.in);

	protected PrintWriter out = new PrintWriter(doc.out);

	protected PrintWriter err = new PrintWriter(doc.err);

	protected Console console = new Console(doc);
	
	private Thread thread;

	private State state;

	public Runner() {
		state = State.READY;
		console.setRunner(this);
	}

	public Console getConsole() {
		return console;
	}
	
	public void start() {
		if (state != State.READY) {
			throw new IllegalStateException();
		}
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					state = State.RUNNING;
					doc.clear();
					Runner.this.run();
				} catch (Exception ex) {
					out.println(Messages.getString("Runner.0") + ex.getMessage()); //$NON-NLS-1$
					ex.printStackTrace(err);
				} finally {
					out.println(Messages.getString("Runner.1")); //$NON-NLS-1$
					state = State.READY;
				}
			}
		});
		thread.start();
	}

	public State getState() {
		return state;
	}
	
	@SuppressWarnings("deprecation")
	public void terminate() {
		if (thread != null) {
			thread.stop();
			state = State.READY;
		}
	}
	
	enum State {
		READY, RUNNING
	}

}
