package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import com.itextpdf.text.pdf.PdfException;

public class TaskPdfOpen extends SwingWorker<Vector<PageGroup>, Void> {

	private PdfFile pdfFile;
	private File originalFile;
	private int groupType;
	private boolean isCancelled;
	private PdfCropper cropper = null;
	private Component owner;
	private boolean shouldCreateStackView;

	public TaskPdfOpen(File file, int groupType, boolean shouldCreateStackView, Component owner) {
		this.originalFile = file;
		isCancelled = false;
		this.owner = owner;
		this.groupType = groupType;
		this.shouldCreateStackView = shouldCreateStackView;
	}

	@Override
	protected Vector<PageGroup> doInBackground() throws Exception {

		debug("Normalzing pdf...");
		pdfFile = PdfCropper.getNormalizedPdf(originalFile);
		pdfFile.getNormalizedFile().deleteOnExit();

		debug("Extracting pdf image...");
		Vector<BufferedImage> loadedImages = null;
		try {
			cropper = new PdfCropper(pdfFile.getNormalizedFile());

			if (!checkEncryption()) {
				JOptionPane.showMessageDialog(owner, "Sorry, your pdf is protected, cannot continue");
			}
			Vector<PageGroup> pageGroups = PageGroup.createGroup(groupType, pdfFile.getPageCount());

			if (shouldCreateStackView && groupType != PageGroup.GROUP_TYPE_INDIVIDUAL) {
				setProgress(0);
				TaskCreateThumbnails thumbnailTask = launchThumbnailTask(pdfFile, pageGroups);
				
				//make this thread wait, until the thumbnails-thread has finished:
				// (images are previews for stacked-pages, i.e. required)
				thumbnailTask.get();
				
				setProgress(100);
			}
			else {
				//start thumbnails-thread without waiting for it to finish
				// (images are only previews for single pages, i.e. not required)
				launchThumbnailTask(pdfFile, pageGroups);
			}
			return pageGroups;
		} finally {
			if (cropper != null) {
				cropper.close();
			}
		}
	}

	public void cancel() {
		isCancelled = true;
		if (this.cropper != null) {
			cropper.cancel();
		}
	}

	@Override
	protected void done() {
		super.done();
		setProgress(100);
		firePropertyChange("done", false, true);
		if (!isCancelled) {
			Vector<PageGroup> pageGroups = null;
			try {
				pageGroups = this.get();
				if (pageGroups != null && !isCancelled) {
					Model.getInstance().setPdf(pdfFile, pageGroups);
				} else {
					Model.getInstance().setPdfLoadFailed(originalFile, new org.jpedal.exception.PdfException("Failed to extract image. Check if PDF is password protected or corrupted."));
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); // ignore
			} catch (ExecutionException e) {
				Model.getInstance().setPdfLoadFailed(originalFile, e.getCause());
			}
		}
	}
	
	private TaskCreateThumbnails launchThumbnailTask(PdfFile file, Vector<PageGroup> pageGroups) {
		
		final TaskCreateThumbnails task = new TaskCreateThumbnails(file, pageGroups, owner);
		
		//"forward" changes from the sub-task:
		task.addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			}
		});
		
		task.execute();
		
		return task;

	}

	private void debug(String string) {
		System.out.println("TaskPdfOpen: " + string);
	}

	/**
	 * check if encryption present and acertain password, return true if content accessable
	 * 
	 * @throws org.jpedal.exception.PdfException
	 */
	private boolean checkEncryption() throws org.jpedal.exception.PdfException, PdfException {

		// check if file is encrypted
		if (cropper.isEncrypted()) {

			// if file has a null password it will have been decoded and
			// isFileViewable will return true
			while (!cropper.isFileViewable()) {

				/** popup window if password needed */
				String password = JOptionPane.showInputDialog(owner, "Please enter password");

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
