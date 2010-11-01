package bd.amazed.pdfscissors.model;

import java.io.File;

public class ModelListenerAdapter implements ModelListener {

	@Override
	public void newPdfLoaded() {
		
	}

	@Override
	public void pdfLoadFailed(File failedFile, Throwable cause) {
		
	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		
	}

}
