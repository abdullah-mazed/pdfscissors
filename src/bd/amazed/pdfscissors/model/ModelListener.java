package bd.amazed.pdfscissors.model;

import java.awt.image.BufferedImage;
import java.io.File;

public interface ModelListener {
	
	public void newPdfLoaded();
	
	public void pdfLoadFailed(File failedFile, Throwable cause);

	public void zoomChanged(double oldZoomFactor, double newZoomFactor);
}
