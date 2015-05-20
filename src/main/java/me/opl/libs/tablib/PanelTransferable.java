package me.opl.libs.tablib;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PanelTransferable implements Transferable {
	public static final DataFlavor PANEL_FLAVOR = new DataFlavor(AbstractPanel.class, "Panel Transferable");

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] {PANEL_FLAVOR};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor == PANEL_FLAVOR;
	}

}
