package bd.amazed.pdfscissors.model;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;


public class Model {

	private static Model instance;
	
	private Vector<ModelListener> modelListeners;
	
	private File currentFile;
	private BufferedImage previewImage;
	/** When zoom factor is not 1.*/
	private Image scaledPreviewImage; 

	private double zoomFactor;
	
	private Model() {
		modelListeners = new java.util.Vector<ModelListener>();
		zoomFactor = 1;
	}
	
	public static Model getInstance() {
		if(instance == null) {
			instance = new Model();
		}
		return instance;
	}
	
	public void addListener(ModelListener listener) {
		if ( ! modelListeners.contains(listener)) {
			modelListeners.add(listener);
		}
	}
	
	/**
	 * 
	 * @return true if listener has been removed, false if not found
	 */
	public boolean removeListener(ModelListener listener) {
		return modelListeners.remove(listener);
	}
	
	/**
	 * 
	 * @param file file, must not be null
	 * @param previewImage previewImage, must not be null
	 */
	public void setPdf(File file, BufferedImage previewImage) {
		if(file == null) {
			throw new IllegalArgumentException("Cannot set null file to model");
		}
		if (previewImage == null) {
			throw new IllegalArgumentException("Cannot set null preview image to model");
		}
		currentFile = file;
		this.previewImage = previewImage;
		fireNewPdf();
	}
	
	/**
	 * 
	 * @return current file or null
	 */
	public File getCurrentFile() {
		return currentFile;
	}
	
	/**
	 * 
	 * @return preview image or null
	 */
	public BufferedImage getPreviewImage() {
		return previewImage;
	}

	/**
	 * Notify model that some pdf loading has failed.
	 * @param file
	 */
	public void setPdfLoadFailed(File file, Throwable err) {
		fireSetPdfFailed(file, err);
	}
	
	 /**
     * @param zoomFactor max 1.0
     */
    private void setZoomFactor(double zoomFactor) {
        if (this.zoomFactor != zoomFactor) {
        	double oldZoom = this.zoomFactor;
            this.zoomFactor = zoomFactor;
            if (zoomFactor == 1) {
                scaledPreviewImage = previewImage;                
            } else {
                if (previewImage != null) {
                    scaledPreviewImage = previewImage.getScaledInstance((int)(previewImage.getWidth() * zoomFactor), (int)(previewImage.getHeight() * zoomFactor), BufferedImage.SCALE_FAST);                    
                } else {
                    scaledPreviewImage = null; //if preview image is null, so is scaled
                }
            }
            fireZoomChanged(oldZoom, zoomFactor);
        }
        
    }

	public double getZoomFactor() {
		return zoomFactor;
	}

	public Image getScaledPreivewImage() {
		return scaledPreviewImage;
	}	
	

	protected void fireNewPdf() {
		for (ModelListener listener : modelListeners) {
			listener.newPdfLoaded();
		}
	}
	
	protected void fireSetPdfFailed(File failedFile, Throwable cause) {
		for (ModelListener listener : modelListeners) {
			listener.pdfLoadFailed(failedFile, cause);
		}
	}
	
	protected void fireZoomChanged(double oldZoomFactor, double newZoomFactor) {
		for (ModelListener listener : modelListeners) {
			listener.zoomChanged(oldZoomFactor, newZoomFactor);
		}
	}
}
