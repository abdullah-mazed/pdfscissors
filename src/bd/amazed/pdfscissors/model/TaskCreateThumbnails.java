package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.SwingWorker;

public class TaskCreateThumbnails extends SwingWorker<Vector<PageGroup>, Void> {

	private PdfFile pdfFile;
//	private File originalFile;
	private boolean isCancelled;
	private PdfCropper cropper = null;
	private Component owner;
	
	Vector<PageGroup> pageGroups = null;

	public TaskCreateThumbnails(PdfFile file, Vector<PageGroup> pageGroups, Component owner) {
//		this.originalFile = file;
		
		this.pdfFile = file;
		
		isCancelled = false;
		this.owner = owner;
		this.pageGroups = pageGroups;
	}

	@Override
	protected Vector<PageGroup> doInBackground() throws Exception {

		debug("Extracting pdf image...");
//		Vector<BufferedImage> loadedImages = null;
		try {
			cropper = new PdfCropper(pdfFile.getNormalizedFile());
			
			setProgress(0);
			PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
				}
			};

			for (int i = 0; i < pageGroups.size(); i++) {
				
				if(this.isCancelled){
					break;
				}
				
				PageGroup pageGroup = pageGroups.elementAt(i);
				BufferedImage image = cropper.getImage(propertyChangeListener, pageGroup, i, pageGroups.size());

				if (image == null) {
					debug("Ups.. null image for " + pdfFile.getNormalizedFile());
				} else {
					debug("PDF loaded " + pageGroup + " from " + pdfFile.getNormalizedFile());
				}
				pageGroup.setStackImage(image);

			}
			setProgress(100);
				
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
	}

	private void debug(String string) {
		System.out.println("TaskCreateThumbnail: " + string);
	}

}
