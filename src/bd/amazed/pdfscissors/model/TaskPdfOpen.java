package bd.amazed.pdfscissors.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

public class TaskPdfOpen extends SwingWorker<BufferedImage, Void> {

	private File file;

	public TaskPdfOpen(File file) {
		this.file = file;
	}

	@Override
	protected BufferedImage doInBackground() throws Exception {

		debug("Extracting pdf image...");
		for (int i = 0; i < 5; i++) {
			Thread.sleep(200);// TODO remove intentional load delay, simluating progress bar
			setProgress(i * 10);
		}
		PdfCropper cropper = null;
		try {
			cropper = new PdfCropper(file);

			setProgress(100);
			BufferedImage image = cropper.getImage(1);
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
		BufferedImage image = null;
		try {
			image = this.get();
			if (image != null) {
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

	private void debug(String string) {
		System.out.println("TaskPdfOpen:" + string);
	}
	
}
