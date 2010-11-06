package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.itextpdf.text.pdf.PdfException;

public class TaskPdfOpen extends SwingWorker<BufferedImage, Void> {

	private File file;
	private boolean isCancelled;
	PdfCropper cropper = null;
	private Component owner;
	
	public TaskPdfOpen(File file, Component owner) {
		this.file = file;
		isCancelled = false;
		this.owner = owner;
	}

	@Override
	protected BufferedImage doInBackground() throws Exception {

		debug("Extracting pdf image...");
		try {
			cropper = new PdfCropper(file);
			if (!checkEncryption()) {
				JOptionPane.showMessageDialog(owner, "Sorry, your pdf is protected, cannot continue");
			}
			setProgress(0);
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
		if(this.cropper != null) {
			cropper.cancel();
		}
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
	
	
	/**
	 * check if encryption present and acertain password, return true if content
	 * accessable
	 * 
	 * @throws org.jpedal.exception.PdfException
	 */
	private boolean checkEncryption() throws org.jpedal.exception.PdfException,
			PdfException {

		// check if file is encrypted
		if (cropper.isEncrypted()) {

			// if file has a null password it will have been decoded and
			// isFileViewable will return true
			while (!cropper.isFileViewable()) {

				/** popup window if password needed */
				String password = JOptionPane.showInputDialog(owner,
						"Please enter password");

				/** try and reopen with new password */
				if (password != null) {
					cropper.setEncryptionPassword(password);
				}
			}
			return true;
		}
		// if not encrypted return true
		return true;
	}
}
