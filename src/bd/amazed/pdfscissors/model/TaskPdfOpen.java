package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public class TaskPdfOpen extends SwingWorker<BufferedImage, Void> {

	private File file;
	ProgressMonitor progressMonitor;
	public TaskPdfOpen(File file, Component owner) {
		this.file = file;
		progressMonitor = new ProgressMonitor(owner,
                "Reading " + file.getName() + "...",
                "", 0, 100);
	}

	@Override
	protected BufferedImage doInBackground() throws Exception {

		debug("Extracting pdf image...");
		
		PdfCropper cropper = null;
		try {
			cropper = new PdfCropper(file);

			setProgress(1);
			BufferedImage image = cropper.getImage(1);
			setProgress(100);
			if (image == null) {
				debug("Ups.. null image for " + file);
			} else {
				debug("PDF loaded " + file);
			}
			return image;
		} finally {
			if (cropper != null) {
				cropper.close();
			}
		}
	}

	@Override
	protected void done() {
		super.done();
		progressMonitor.close();
		if (! progressMonitor.isCanceled()) {
			BufferedImage image = null;
			try {
				image = this.get();
				if (image != null && ! progressMonitor.isCanceled()) {
					Model.getInstance().setPdf(file, image);
				} else {
					Model.getInstance().setPdfLoadFailed(file, new org.jpedal.exception.PdfException("Failed to extract image. Check if PDF is password protected or corrupted."));
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); //ignore
			} catch (ExecutionException e) {
				Model.getInstance().setPdfLoadFailed(file, e.getCause());
			}
		}
	}

	private void debug(String string) {
		System.out.println("TaskPdfOpen:" + string);
	}
	
}
