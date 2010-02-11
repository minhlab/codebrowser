package org.ngocminh.codebrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.ngocminh.codebrowser.Runner.State;
import org.ngocminh.codebrowser.i18n.Messages;

@SuppressWarnings("unchecked")
public class CodeBrowser extends JFrame {

	private static final long serialVersionUID = -8980540462550104503L;

	private static final String APP_NAME = "Code Browser v0.0.1"; //$NON-NLS-1$

	private String codeBase;

	public CodeBrowser() {
		try {
			Class.forName("jsyntaxpane.DefaultSyntaxKit"); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		initComponents();
		setSize(750, 480);
		showInfo();
	}

	private void initComponents() {
		setLocationByPlatform(true);
		setTitle(APP_NAME);

		splWrapper.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splWrapper.setDividerLocation(200);

		pnlLeft.setLayout(new BorderLayout());

		trExplorer.setEditable(false);
		trExplorer.setRootVisible(false);
		trExplorer.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		trExplorer.setCellRenderer(new DefaultRenderer());
		trExplorer.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				openSelected();
			}

		});
		trExplorer.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TreePath path = trExplorer.getPathForLocation(e.getX(), e
							.getY());
					if (path == null) {
						return;
					}
					openSelected();
				}
			}
		});
		pnlLeft.add(new JScrollPane(trExplorer), BorderLayout.CENTER);

		splWrapper.setLeftComponent(pnlLeft);

		txtAbout.setEditable(false);
		txtAbout.setContentType("text/html"); //$NON-NLS-1$
		txtAbout.setText(msgAbout);
		txtAbout.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					browse(e.getDescription());
				}
			}
		});
		scrAbout.setViewportView(txtAbout);

		pnlRight.setLayout(new BorderLayout());

		pnlToolbar.setLayout(new FlowLayout(FlowLayout.LEFT));

		btnStart.setText(Messages.getString("CodeBrowser.4")); //$NON-NLS-1$
		btnStart.setIcon(icoStart2);
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startSelected();
			}
		});
		pnlToolbar.add(btnStart);

		btnTerminate.setText(Messages.getString("CodeBrowser.5")); //$NON-NLS-1$
		btnTerminate.setIcon(icoTerminate);
		btnTerminate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				terminateSelected();
			}
		});
		pnlToolbar.add(btnTerminate);

		btnClose.setText(Messages.getString("CodeBrowser.6")); //$NON-NLS-1$
		btnClose.setIcon(icoClose);
		btnClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeSelected();
			}
		});
		pnlToolbar.add(btnClose);

		btnInfo.setText(Messages.getString("CodeBrowser.7")); //$NON-NLS-1$
		btnInfo.setIcon(icoInfo);
		btnInfo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showInfo();
			}
		});
		pnlToolbar.add(btnInfo);

		pnlRight.add(pnlToolbar, BorderLayout.NORTH);

		tabMain.getModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				updateTitle();
			}
		});
		pnlRight.add(tabMain, BorderLayout.CENTER);
		splWrapper.setRightComponent(pnlRight);

		getContentPane().add(splWrapper, BorderLayout.CENTER);
	}

	private void updateTitle() {
		if (tabMain.getSelectedComponent() == null) {
			setTitle(APP_NAME);
		} else {
			setTitle(tabMain.getTitleAt(tabMain.getSelectedIndex()) + " - " //$NON-NLS-1$
					+ APP_NAME);
		}
	}

	private void openSelected() {
		TreePath path = trExplorer.getSelectionPath();
		if (path == null) {
			return;
		}
		Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent())
				.getUserObject();
		if (obj instanceof Class) {
			try {
				String name = (String) ((DefaultMutableTreeNode) path
						.getPathComponent(path.getPathCount() - 2))
						.getUserObject();
				Runner runner = ((Class<Runner>) obj).newInstance();
				JScrollPane scroll = new JScrollPane(runner.getConsole());
				scroll.setFocusCycleRoot(false);
				tabMain.add(name, scroll);
				tabMain.setSelectedComponent(scroll);
				runner.getConsole().requestFocusInWindow();
				runner.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (obj instanceof String) {
			String fileName = (String) obj;
			String filePath = "/" + codeBase + "/" + fileName; //$NON-NLS-1$ //$NON-NLS-2$
			if (tabMain.indexOfTab(fileName) < 0) {
				JEditorPane txtSource = new JEditorPane();
				txtSource.setEditable(false);
				tabMain.add(fileName, new JScrollPane(txtSource));
				try {
					String content = readContent(CodeBrowser.class
							.getResourceAsStream(filePath));
					if (fileName.endsWith(".java")) { //$NON-NLS-1$
						txtSource.setContentType("text/java"); //$NON-NLS-1$
					} else {
						txtSource.setContentType("text/plain"); //$NON-NLS-1$
					}
					txtSource.setText(content);
				} catch (Exception e) {
					txtSource.setContentType("text/plain"); //$NON-NLS-1$
					txtSource
							.setText(Messages.getString("CodeBrowser.15") + e.getMessage() + "\n" //$NON-NLS-1$ //$NON-NLS-2$
									+ Arrays.deepToString(e.getStackTrace()));
				}
				txtSource.setCaretPosition(0);
				txtSource.requestFocusInWindow();
			}
			tabMain.setSelectedIndex(tabMain.indexOfTab(fileName));
		}
	}

	private static String readContent(InputStream resourceAsStream) {
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[4096];
		int read = 0;
		Reader in = new InputStreamReader(resourceAsStream);
		try {
			while ((read = in.read(buffer)) > 0) {
				sb.append(buffer, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private void browse(String href) {
		try {
			Desktop.getDesktop().browse(new URI(href));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void terminateSelected() {
		Component comp = tabMain.getSelectedComponent();
		if (comp instanceof JScrollPane) {
			JScrollPane scroll = (JScrollPane) comp;
			comp = scroll.getViewport().getView();
		}
		if (comp instanceof Console) {
			Console console = (Console) comp;
			Runner runner = console.getRunner();
			if (runner.getState() == State.RUNNING) {
				runner.terminate();
			}
		}
	}

	private void startSelected() {
		Component comp = tabMain.getSelectedComponent();
		if (comp instanceof JScrollPane) {
			JScrollPane scroll = (JScrollPane) comp;
			comp = scroll.getViewport().getView();
		}
		if (comp instanceof Console) {
			Console console = (Console) comp;
			Runner runner = console.getRunner();
			if (runner.getState() == State.READY) {
				console.requestFocusInWindow();
				runner.start();
			}
		}
	}

	private void showInfo() {
		if (tabMain.indexOfComponent(scrAbout) < 0) {
			tabMain.add(Messages.getString("CodeBrowser.17"), scrAbout); //$NON-NLS-1$
		}
		tabMain.setSelectedComponent(scrAbout);
	}

	private void closeSelected() {
		Component comp = tabMain.getSelectedComponent();
		if (comp == null) {
			return;
		}
		if (comp instanceof Console) {
			Console console = (Console) comp;
			Runner runner = console.getRunner();
			if (runner.getState() == State.RUNNING) {
				runner.terminate();
			}
		}
		tabMain.remove(comp);
	}

	public void load(String info, String codeBase, Object[][] data) {
		this.codeBase = codeBase;

		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		for (Object[] prog : data) {
			Class<Runner> clazz = (Class<Runner>) prog[1];
			DefaultMutableTreeNode progNode = new DefaultMutableTreeNode(
					prog[0]);
			Arrays.sort(prog, 2, prog.length);
			progNode.add(new DefaultMutableTreeNode(prog[1]));
			progNode.add(new DefaultMutableTreeNode(clazz.getSimpleName()
					+ ".java")); //$NON-NLS-1$
			for (int i = 2; i < prog.length; i++) {
				progNode.add(new DefaultMutableTreeNode(prog[i]));
			}
			root.add(progNode);
		}
		trExplorer.setModel(new DefaultTreeModel(root));
		expandAll(trExplorer, true);

		if (info != null) {
			txtAbout.setText(msgAbout + "<br /><hr />" + info); //$NON-NLS-1$
		}
	}

	public static void expandAll(JTree tree, boolean expand) {
		expandAll(tree, new TreePath(tree.getModel().getRoot()), expand);
	}

	private static void expandAll(JTree tree, TreePath path, boolean expand) {
		// Traverse children
		Object parent = path.getLastPathComponent();
		for (int i = tree.getModel().getChildCount(parent) - 1; i >= 0; i--) {
			Object child = tree.getModel().getChild(parent, i);
			expandAll(tree, path.pathByAddingChild(child), expand);
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(path);
		} else {
			tree.collapsePath(path);
		}
	}

	public static void show(String codeBase, Object[][] data) {
		show((String) null, codeBase, data);
	}

	public static void show(InputStream info, String codeBase, Object[][] data) {
		show(readContent(info), codeBase, data);
	}

	public static void show(String info, String codeBase, Object[][] data) {
		CodeBrowser browser = new CodeBrowser();
		browser.load(info, codeBase, data);
		browser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		browser.setVisible(true);
	}

	private class DefaultRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = -4425146698784095892L;

		private Icon icoDefault = getLeafIcon();

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree,
					value, sel, expanded, leaf, row, hasFocus);
			if (value instanceof DefaultMutableTreeNode) {
				Object obj = ((DefaultMutableTreeNode) value).getUserObject();
				if (obj instanceof Class) {
					label.setIcon(icoStart);
					label.setText(Messages.getString("CodeBrowser.20")); //$NON-NLS-1$
				} else if ((obj instanceof String)) {
					if (leaf) {
						label.setIcon(icoSource);
					} else {
						label.setIcon(icoProgram);
					}
				} else {
					label.setIcon(icoDefault);
				}
			}
			return label;
		}

	}

	private Icon loadIcon(String fileName) {
		return new ImageIcon(CodeBrowser.class
				.getResource("images/" + fileName)); //$NON-NLS-1$
	}

	private JSplitPane splWrapper = new JSplitPane();
	private JPanel pnlLeft = new JPanel();
	private JPanel pnlRight = new JPanel();
	private JTree trExplorer = new JTree();
	private JTabbedPane tabMain = new JTabbedPane();
	private JEditorPane txtAbout = new JEditorPane();
	private JScrollPane scrAbout = new JScrollPane();
	private JButton btnStart = new JButton();
	private JButton btnTerminate = new JButton();
	private JButton btnClose = new JButton();
	private JButton btnInfo = new JButton();
	private JPanel pnlToolbar = new JPanel();
	private Icon icoStart = loadIcon("start.png"); //$NON-NLS-1$
	private Icon icoSource = loadIcon("source.png"); //$NON-NLS-1$
	private Icon icoProgram = loadIcon("program.png"); //$NON-NLS-1$
	private Icon icoStart2 = loadIcon("start2.png"); //$NON-NLS-1$
	private Icon icoTerminate = loadIcon("terminate.png"); //$NON-NLS-1$
	private Icon icoClose = loadIcon("close.png"); //$NON-NLS-1$
	private Icon icoInfo = loadIcon("info.png"); //$NON-NLS-1$

	private String msgAbout = "<p><a href=\"" + //$NON-NLS-1$
			"http://code.google.com/p/codebrowser/\">" + //$NON-NLS-1$
			"Code Browser</a> version 0.0.1</p>"; //$NON-NLS-1$

}
