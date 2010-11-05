package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public class TaskPdfOpen extends SwingWorker<BufferedImage, Void> {

	private File file;
	private boolean isCancelled;
	
	public TaskPdfOpen(File file, Component owner) {
		this.file = file;
		isCancelled = false;
	}

	@Override
	protected BufferedImage doInBackground() throws Exception {

		debug("Extracting pdf image...");
		
		PdfCropper cropper = null;
		try {
			cropper = new PdfCropper(file);

			setProgress(1);
			BufferedImage image = cropper.getImage(new PropertyChangeListener() {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}
			});
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
	
	public void cancel() {
		isCancelled = true;
	}

	@Override
	protected void done() {
		super.done();
		setProgress(100);
		firePropertyChange("done", false, true);
		if (! isCancelled) {
			BufferedImage image = null;
			try {
				image = this.get();
				if (image != null && ! isCancelled) {
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
