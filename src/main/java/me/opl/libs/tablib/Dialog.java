package me.opl.libs.tablib;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

public class Dialog extends JDialog {
	private static final long serialVersionUID = -7400372835640331302L;

	protected TabLib tabLib;
	private Window parent;

	public Dialog(TabLib tabLibInstance, Window parent, AbstractPanel panel) {
		super(parent);

		this.tabLib = tabLibInstance;
		this.parent = parent;

		setGlassPane(new CustomGlassPane());

		TabbedPanelContainer tpc = new TabbedPanelContainer(tabLib);
		tpc.addPanel(panel);

		add(tpc);

		setTitle("[TabLib] Default title");
		setType(Window.Type.UTILITY);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Dialog.this.parent.onDialogClosed(Dialog.this);
			}
		});

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}
