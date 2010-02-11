package org.ngocminh.codebrowser.test;

import java.io.IOException;

import org.ngocminh.codebrowser.CodeBrowser;
import org.ngocminh.codebrowser.Runner;

public class HelloWorld extends Runner {

	@Override
	public void run() {
		try {
			out.println("Hello world!");
			out.println("I'm Code Browser.");
			out.print("Who are you? ");
			String name = in.readLine();
			out.println("Hello " + name + "!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CodeBrowser.show(CodeBrowser.class.getResourceAsStream("info.html"),
				"src", new Object[][] { new Object[] { "Hello world!",
						HelloWorld.class } });
	}

}
