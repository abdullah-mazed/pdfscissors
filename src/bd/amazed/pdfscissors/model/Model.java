package bd.amazed.pdfscissors.model;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

import bd.amazed.pdfscissors.view.Rect;


public class Model {

	public static final String PROPERTY_LAST_FILE = "lastFile";
	private static Model instance;
	
	private Vector<ModelListener> modelListeners;

	// we basically read from a temp normalized file. Original file reference is just stored.
	private File currentFile;
	private File originalFile;
	private BufferedImage previewImage;
	/** When zoom factor is not 1.*/
	private Image scaledPreviewImage; 

	private double zoomFactor;
	
	private Rect clipboardRect;
	private boolean isClipboardCut;
	private Properties properties;
	private String propertyFileLocation;
	
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
	 * @param file file, must not be null. This should be a normalized temp file
	 * @param originalFile original file
	 * @param previewImage previewImage, must not be null
	 */
	public void setPdf(File file, File originalFile, BufferedImage previewImage) {
		if(file == null) {
			throw new IllegalArgumentException("Cannot set null file to model");
		}
		if (previewImage == null) {
			throw new IllegalArgumentException("Cannot set null preview image to model");
		}
		currentFile = file;
		this.originalFile = originalFile;
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
	
	public File getOriginalFile() {
		return originalFile;
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
	
	public void copyToClipboard(boolean isCut, Rect rect) {
		if (rect != null) {
			this.clipboardRect = rect;
			this.isClipboardCut = isCut;
			fireClipboardCopyEvent(clipboardRect, isClipboardCut);
		}
	}
	
	public void pasteFromClipboard() {
		if (clipboardRect != null) {
			fireClipboardPasteEvent(clipboardRect, isClipboardCut);
			if (isClipboardCut) {
				isClipboardCut = false; //clear clipboard
				clipboardRect = null;
			}			
		}		
	}
	
	public Rect getClipboardRect() {
		return clipboardRect;
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
	
	protected void fireClipboardCopyEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardCopy(isCut, onClipboard);
		}
	}
	
	protected void fireClipboardPasteEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardPaste(isCut, onClipboard);
		}
	}
	
	public Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
			    properties.load(new FileInputStream(getPropertyFileLocation()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.properties;
	}
	
	
	private String getPropertyFileLocation() {
		if (this.propertyFileLocation == null) {
			this.propertyFileLocation = System.getProperty("user.home") + File.separator + "pdfscissors/";
			File file = new File(propertyFileLocation );
			if (!file.exists()) {
				file.mkdir();
			}
			this.propertyFileLocation += "pdfscissors.properties";
		}
		return this.propertyFileLocation;
	}
	
	public void close() {
		try {
		    getProperties().store(new FileOutputStream(getPropertyFileLocation()), null);
		} catch (IOException e) {
		}
	}
}
