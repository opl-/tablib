package me.opl.libs.tablib;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;

public class Window extends JFrame {
	private static final long serialVersionUID = -2843380015343477457L;

	protected TabLib tabLib;

	private CustomGlassPane glassPane;

	private ArrayList<Dialog> dialogs;

	public Window(TabLib tabLibInstance, WindowPopulator populator) {
		this.tabLib = tabLibInstance;

		dialogs = new ArrayList<Dialog>();

		setPreferredSize(new Dimension(800, 600));

		setTitle("[TabLib] Default title");

		glassPane = new CustomGlassPane();
		setGlassPane(glassPane);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Window.this.tabLib.onWindowClosed(Window.this);
			}
		});

		if (populator != null) populator.populate(this);

		pack();
		setLocationRelativeTo(null);
	}

	public Dialog openNewDialog(Class<? extends AbstractPanel> panelClass) {
		Dialog d = new Dialog(tabLib, this, tabLib.createPanel(panelClass));
		dialogs.add(d);
		d.setVisible(true);
		return d;
	}

	public Dialog[] getDialogs() {
		Dialog[] toReturn = new Dialog[dialogs.size()];
		dialogs.toArray(toReturn);
		return toReturn;
	}

	protected void onDialogClosed(Dialog dialog) {
		dialogs.remove(dialog);
	}

	public interface WindowPopulator {
		public void populate(Window window);
	}
}
