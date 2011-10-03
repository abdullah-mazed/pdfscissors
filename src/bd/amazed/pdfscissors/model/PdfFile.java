package bd.amazed.pdfscissors.model;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

/**
 * Contains data related to the pdf file. Use normalized pdf file for actual cropping.
 * 
 */
public class PdfFile {

	// we basically read from a temp normalized file.
	// Original file reference is just stored.
	private File normalizedFile;
	private File originalFile;
	private BufferedImage previewImage;
	/** When zoom factor is not 1. */
	private Image scaledPreviewImage;
	private HashMap<String, String> pdfInfo;

	private PdfFile() {

	}

	public PdfFile(File normalizedFile, File origiFile) {
		setNormalizedFile(normalizedFile);
		setOriginalFile(origiFile);
	}

	public File getNormalizedFile() {
		return normalizedFile;
	}

	public void setNormalizedFile(File normalizedFile) {
		if (normalizedFile == null) {
			throw new IllegalArgumentException("Cannot set null file to model");
		}
		this.normalizedFile = normalizedFile;
	}

	public File getOriginalFile() {
		return originalFile;
	}

	public void setOriginalFile(File originalFile) {
		if (originalFile == null) {
			throw new IllegalArgumentException("Cannot set null originalFile to model");
		}
		this.originalFile = originalFile;
	}

	public BufferedImage getPreviewImage() {
		return previewImage;
	}

	public void setPreviewImage(BufferedImage previewImage) {
		if (previewImage == null) {
			throw new IllegalArgumentException("Cannot set null preview image to model");
		}
		this.previewImage = previewImage;
	}

	public Image getScaledPreviewImage() {
		return scaledPreviewImage;
	}

	public void setScaledPreviewImage(Image scaledPreviewImage) {
		this.scaledPreviewImage = scaledPreviewImage;
	}

	public HashMap<String, String> getPdfInfo() {
		if (pdfInfo == null) {
			return new HashMap<String, String>();
		} else {
			return pdfInfo;
		}
	}

	public void setPdfInfo(HashMap<String, String> pdfInfo) {
		this.pdfInfo = pdfInfo;
	}

	public static PdfFile NullPdf() {
		return new PdfFile();
	}

}
