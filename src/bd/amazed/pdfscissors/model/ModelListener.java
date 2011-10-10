package bd.amazed.pdfscissors.model;

import java.io.File;
import java.util.Vector;

import bd.amazed.pdfscissors.view.Rect;

public interface ModelListener {

	public void newPdfLoaded();

	public void pdfLoadFailed(File failedFile, Throwable cause);

	public void zoomChanged(double oldZoomFactor, double newZoomFactor);

	public void clipboardCopy(boolean isCut, Rect onClipboard);

	public void clipboardPaste(boolean isCut, Rect onClipboard);
	
	public void pageGroupChanged(Vector<PageGroup> pageGroups);
}
