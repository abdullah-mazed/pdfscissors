/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors.model;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ProgressMonitor;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import bd.amazed.pdfscissors.view.Rect;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * 
 * @author Gagan
 */
public class PdfCropper {

	private static final String TEMP_PREFIX_PDFSCISSOR = "~pdfscissor_";
	private File mainFile;
	private PdfDecoder pdfDecoder;
	private boolean isCancel;

	public PdfCropper(File file) {
		if (file == null) {
			throw new IllegalArgumentException("PDFCropper does not accept null file.");
		}
		this.mainFile = file;
	}

	public BufferedImage getImage(PropertyChangeListener listener, PageGroup pageGroup) throws PdfException {
		return getImage(listener, pageGroup, 0, 0);
	}
	
	public BufferedImage getImage(PropertyChangeListener listener, PageGroup pageGroup, int currentImageIndex, int totalImageCount) throws PdfException {
		// TODO validate page number
		int endPage = pageGroup.getLastPage();
		BufferedImage lastPage = getPdfDecoder().getPageAsImage(endPage);

		if (lastPage != null) {
			listener.propertyChange(new PropertyChangeEvent(this, "message", null, "Stacking " + pageGroup));
			lastPage = new BufferedImage(lastPage.getWidth(), lastPage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g2d = (Graphics2D) lastPage.getGraphics();
			
			g2d.setColor(Color.WHITE);
			g2d.fillRect(0, 0, lastPage.getWidth(), lastPage.getHeight());

			int pageCount = pageGroup.getPageCount();
			float alpha = pageCount > 1? 0.5f : 1f;//MOD russa: use transparency if multiple images are stacked / keep opaque if it is only 1 image 
			int type = AlphaComposite.SRC_OVER;
			AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
			g2d.setComposite(composite);
			int argb;
			int r;
			int g;
			int b;
			for (int iPageInGroup = pageCount - 1; iPageInGroup >= 0 && !isCancel; iPageInGroup--) {
				int i = pageGroup.getPageNumberAt(iPageInGroup);
				int percentageDone = (100 * (pageCount - iPageInGroup)) / pageCount;
				if(totalImageCount > 1)
					percentageDone = (int) ((((double)(100 * currentImageIndex)) / totalImageCount) + ( (double) percentageDone / totalImageCount));
				System.out.println("Stacking page: " + i + ", completed " + percentageDone);
				listener.propertyChange(new PropertyChangeEvent(this, "progress", null, percentageDone));
				BufferedImage pageImage = getPdfDecoder().getPageAsImage(i);
				if (pageImage != null) {
					for (int x = 0; x < pageImage.getWidth(); x++) {
						for (int y = 0; y < pageImage.getHeight(); y++) {
							argb = pageImage.getRGB(x, y);
							r = (argb >> 16) & 0x000000FF;
							g = (argb >> 8) & 0x000000FF;
							b = (argb) & 0x000000FF;
							if ((r == g && g == b && b == r && r > 150)) {
								pageImage.setRGB(x, y, 0x00FF0000);// transparent
							}
						}
					}
					g2d.drawImage(pageImage, 0, 0, null);
				}
			}
		}

		return lastPage;
	}

	public boolean isEncrypted() throws PdfException {
		return getPdfDecoder().isEncrypted();
	}

	public boolean isExtractionAllowed() throws PdfException {
		return getPdfDecoder().isExtractionAllowed();
	}

	public void close() {
		if (pdfDecoder != null) {
			pdfDecoder.closePdfFile();
		}
	}

	private PdfDecoder getPdfDecoder() throws PdfException {
		if (pdfDecoder == null) {
			pdfDecoder = new PdfDecoderMod(true);
			try {
				pdfDecoder.openPdfFile(mainFile.getAbsolutePath());
			} catch (RuntimeException ex) {
				if (ex.toString().contains("bouncy")) { // hah, stupid way of doing this, but what can i do if library
					// is throwing RuntimeException :(
					throw new PdfException("PDF is encrypted or password protected.\nTry to create an unencrypted pdf first using some other tool.");
				}
			}
		}
		return pdfDecoder;
	}

	/**
	 * Normalizing takes care of different widths / heights of pages by setting max width, height to all pages.a
	 */
	public static PdfFile getNormalizedPdf(File originalFile) throws DocumentException, IOException {
		PdfReader reader = null;
		Document doc = null;
		PdfWriter writer = null;
		try {
			File tempFile = TempFileManager.getInstance().createTempFile(TEMP_PREFIX_PDFSCISSOR + originalFile.getName() + "_", null, true);
			debug("Creating temp file at " + tempFile);
			reader = new PdfReader(originalFile.getAbsolutePath());
			int endPage = reader.getNumberOfPages();
			Rectangle maxBoundingBox = getMaxBoundingBox(reader, endPage);
			doc = new Document(maxBoundingBox, 0, 0, 0, 0);
			writer = PdfWriter.getInstance(doc, new FileOutputStream(tempFile));
			doc.open();
			PdfContentByte cb = writer.getDirectContent();

			for (int i = 1; i <= endPage; i++) {
				PdfImportedPage page = writer.getImportedPage(reader, i);
				float Scale = 1f;
				cb.addTemplate(page, Scale, 0, 0, Scale, 0, 0);
				doc.newPage();
			}

			// put the information, like author name etc.
			HashMap<String, String> info = reader.getInfo();
			String keywords = info.get("Keywords");
			if (keywords == null) {
				keywords = "";
			}
			if (keywords.length() > 0 && !keywords.endsWith(" ")) {
				keywords += " ";
			}
			keywords += "Cropped by pdfscissors.com";
			info.put("Keywords", keywords);

			PdfFile pdfFile = new PdfFile(tempFile, originalFile, endPage);
			pdfFile.setPdfInfo(info);
			pdfFile.setPageCount(endPage);
			pdfFile.setNormalizedPdfWidth(Math.abs(maxBoundingBox.getWidth()));
			pdfFile.setNormalizedPdfHeight(Math.abs(maxBoundingBox.getHeight()));

			return pdfFile;

		} finally {
			if (doc != null) {
				doc.close();
			}

			if (writer != null) {
				writer.close();
			}

			if (reader != null) {
				reader.close();
			}
		}
	}

	private static com.itextpdf.text.Rectangle getMaxBoundingBox(PdfReader reader, int endPage) {
		com.itextpdf.text.Rectangle maxBoundingBox = new com.itextpdf.text.Rectangle(0, 0, 0, 0);
		for (int i = 1; i <= endPage; i++) {
			com.itextpdf.text.Rectangle boundingBox = reader.getPageSize(i);

			if (boundingBox.getWidth() > maxBoundingBox.getWidth())
				maxBoundingBox.setRight(boundingBox.getWidth());

			if (boundingBox.getHeight() > maxBoundingBox.getHeight())
				maxBoundingBox.setBottom(boundingBox.getHeight());
		}
		return maxBoundingBox;
	}
	
	public static void cropPdf(PdfFile pdfFile, File targetFile, ArrayList<CropRect> cropRects,
			int viewWidth, int viewHeight, ProgressMonitor progressMonitor) throws IOException, DocumentException {

		File originalFile = pdfFile.getOriginalFile();
		HashMap<String, String> pdfInfo = pdfFile.getPdfInfo();

		PdfReader reader = new PdfReader(originalFile.getAbsolutePath());

		float pdfWidth = pdfFile.getNormalizedPdfWidth();
		float pdfHeight = pdfFile.getNormalizedPdfHeight();
		
		System.out.println("Finding ratio : viewSize " + viewWidth + "x" + viewHeight + ", pdf size " + pdfWidth + "x" + pdfHeight);
		double widthRatio = pdfWidth / viewWidth;
		double heightRatio = pdfHeight / viewHeight;
		if (widthRatio != heightRatio) {
			System.err.println("WARNING>>> RATION NOT SAME ?! " + widthRatio + " " + heightRatio);
		}
		

		Document document = null;
		PdfCopy writer = null;
		PdfStamper stamper = null;
		File tempFile = null;

		// TODO handle bookmarks
		// List bookmarks = SimpleBookmark.getBookmark(reader);
		// if (bookmarks != null) {
		// if (pageOffset != 0) {
		// SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset,
		// null);
		// }
		// master.addAll(bookmarks);
		// }

		// open the original
		try {

			reader.consolidateNamedDestinations();
			int originalPageCount = reader.getNumberOfPages();
			document = new Document(reader.getPageSizeWithRotation(1));
			tempFile = TempFileManager.getInstance().createTempFile(TEMP_PREFIX_PDFSCISSOR + System.currentTimeMillis(), null, true);
			writer = new PdfCopy(document, new FileOutputStream(tempFile));
			document.open();

			PdfImportedPage page;
			
			int newPageCount = cropRects.size();
			

			
			for (int i = 0; i < cropRects.size(); i++) {
				CropRect cropRect = cropRects.get(i);
				progressMonitor.setNote("Writing page " + (i + 1) + " of " + newPageCount);
				progressMonitor.setProgress(i * 100 / cropRects.size());
				page = writer.getImportedPage(reader, cropRect.pageNumber);
				writer.addPage(page);	
			}
			
			
			document.close();
			document = null;
			reader = new PdfReader(tempFile.getAbsolutePath());

			stamper = new PdfStamper(reader, new FileOutputStream(targetFile));
			int pageCount = reader.getNumberOfPages();
			newPageCount = 0;
			for (CropRect cropRect : cropRects) {
				java.awt.Rectangle cropRectsInIPDFCoords = getConvertedRectForCropping(cropRect.rectangle, viewWidth, viewHeight, pdfWidth,pdfHeight);

				if (cropRectsInIPDFCoords == null) {
					newPageCount++; // we will still add one full page
					System.out.println("Cropping page " + newPageCount + " ... full page size");
				} else {

					newPageCount++;
					// http://stackoverflow.com/questions/4089757/how-do-i-resize-an-existing-pdf-with-coldfusion-itext
					progressMonitor.setNote("Cropping page " + newPageCount + " of " + pageCount);
					progressMonitor.setProgress(newPageCount * 100 / pageCount);
					if (cropRectsInIPDFCoords != null) {
						PdfDictionary pdfDictionary = reader
								.getPageN(newPageCount);
						PdfArray cropCell = new PdfArray();
						java.awt.Rectangle awtRect = cropRectsInIPDFCoords;
						System.out.println("Cropping page " + newPageCount + " with " + awtRect);
						cropCell.add(new PdfNumber(awtRect.x));// lower left x
						cropCell.add(new PdfNumber(awtRect.y));// lower left y
						cropCell.add(new PdfNumber(awtRect.x + awtRect.width)); // up
																				// right
																				// x
						cropCell.add(new PdfNumber(awtRect.y + awtRect.height));// up
																				// righty
						pdfDictionary.put(PdfName.CROPBOX, cropCell);
						pdfDictionary.put(PdfName.MEDIABOX, cropCell);
						pdfDictionary.put(PdfName.TRIMBOX, cropCell);
						pdfDictionary.put(PdfName.BLEEDBOX, cropCell);
					}
				}
			}

			// put the information, like author name etc.
			stamper.setMoreInfo(pdfInfo);
		} finally {
			if (document != null) {
				document.close();
			}

			if (stamper != null) {
				stamper.close();
			}

			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}
	
	public static java.awt.Rectangle getConvertedRectForCropping(java.awt.Rectangle rect,int viewWidth, int viewHeight, float pdfWidth, float pdfHeight) {

		if (rect == null) {
			return null;
		}
		
		double widthRatio = pdfWidth / viewWidth;
		double heightRatio = pdfHeight / viewHeight;
		if (widthRatio != heightRatio) {
			System.err.println("WARNING>>> RATION NOT SAME ?! " + widthRatio + " " + heightRatio);
		}
		
		java.awt.Rectangle covertedRect = new java.awt.Rectangle();
		covertedRect.x = (int) (widthRatio * rect.x);
		covertedRect.y = (int) (widthRatio * (viewHeight - rect.y - rect.height));
		covertedRect.width = (int) (widthRatio * rect.width);
		covertedRect.height = (int) (widthRatio * rect.height);
			
		return covertedRect;

	}

	public void cancel() {
		isCancel = true;
	}

	public boolean isFileViewable() throws PdfException {
		return getPdfDecoder().isFileViewable();
	}

	public void setEncryptionPassword(String password) throws PdfException {
		getPdfDecoder().setEncryptionPassword(password);

	}

	// helper code: //http://itext-general.2136553.n4.nabble.com/How-to-Shrink-Content-and-Add-Margins-td2167577.html
	// public static void solution2() throws IOException, DocumentException {
	// PdfReader reader = new PdfReader("sample.pdf");
	// int n = reader.getNumberOfPages();
	// PdfDictionary pageDict;
	// ArrayList old_mediabox;
	// PdfArray new_mediabox;
	// PdfNumber value;
	// PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("solution2.pdf"));
	// BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
	// PdfContentByte directcontent;
	// for (int i = 1; i <= n; i++) {
	// pageDict = reader.getPageN(i);
	// new_mediabox = new PdfArray();
	// old_mediabox = pageDict.getAsArray(PdfName.MEDIABOX).getArrayList();
	// value = (PdfNumber)old_mediabox.get(0);
	// new_mediabox.add(new PdfNumber(value.floatValue() - 36));
	// value = (PdfNumber)old_mediabox.get(1);
	// new_mediabox.add(new PdfNumber(value.floatValue() - 36));
	// value = (PdfNumber)old_mediabox.get(2);
	// new_mediabox.add(new PdfNumber(value.floatValue() + 36));
	// value = (PdfNumber)old_mediabox.get(3);
	// new_mediabox.add(new PdfNumber(value.floatValue() + 36));
	// pageDict.put(PdfName.MEDIABOX, new_mediabox);
	// directcontent = stamper.getOverContent(i);
	// directcontent.beginText();
	// directcontent.setFontAndSize(font, 12);
	// directcontent.showTextAligned(Element.ALIGN_LEFT, "TEST", 0, -18, 0);
	// directcontent.endText();
	// }
	// stamper.close();
	// }

	private static void debug(String string) {
		System.out.println("TaskPdfOpen:" + string);
	}
}


