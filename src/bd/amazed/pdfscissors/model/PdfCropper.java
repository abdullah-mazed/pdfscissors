/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors.model;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DebugGraphics;
import javax.swing.ProgressMonitor;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;
import org.jpedal.objects.PdfPageData;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PRAcroForm;
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
import com.itextpdf.text.pdf.SimpleBookmark;

import bd.amazed.pdfscissors.view.Rect;

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

    public BufferedImage getImage(PropertyChangeListener listener) throws PdfException {
        //TODO validate page number
        int endPage = getPdfDecoder().getPageCount();
        BufferedImage lastPage = getPdfDecoder().getPageAsImage(endPage);

        if (lastPage != null) {
            for (int i = endPage; i >= 1 && !isCancel; i--) {
            	int percentageDone = (100 * (endPage - i))/endPage;
            	System.out.println("Stacking page: " + i + ", completed " + percentageDone);            	
            	listener.propertyChange(new PropertyChangeEvent(this, "progress", null, percentageDone));
                BufferedImage pageImage = getPdfDecoder().getPageAsImage(i);
                if (pageImage != null) {
                float alpha = 0.5f;
                int type = AlphaComposite.SRC_OVER;
                AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
                Graphics2D g = (Graphics2D) lastPage.getGraphics();
                g.setComposite(composite);
                g.drawImage(pageImage, 0, 0, null);
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
            pdfDecoder = new PdfDecoder(true);
            try {
            	pdfDecoder.openPdfFile(mainFile.getAbsolutePath());
            } catch (RuntimeException ex) {
            	if (ex.toString().contains("bouncy")) { //hah, stupid way of doing this, but what can i do if library is throwing RuntimeException :(
            		throw new PdfException("PDF is encrypted or password protected.\nTry to create an unencrypted pdf first using some other tool.");
            	}
            }
        }
        return pdfDecoder;
    }
    
    
	
    /**
     * Normalizing takes care of different widths / heights of pages by setting max width, height to all pages.a
     */
	public static PdfFile getNormalizedPdf(File originalFile) throws DocumentException, IOException
	{
		PdfReader reader = null;
		Document doc = null;
		PdfWriter writer = null;
		try {
			File tempFile = TempFileManager.getInstance().createTempFile(TEMP_PREFIX_PDFSCISSOR + originalFile.getName() + "_", null, true);
			debug("Creating temp file at " + tempFile);
			reader = new PdfReader(originalFile.getAbsolutePath());
			int endPage = reader.getNumberOfPages();
			doc = new Document(getMaxBoundingBox(reader, endPage), 0, 0, 0, 0);
			writer = PdfWriter.getInstance(doc, new FileOutputStream(tempFile));
			doc.open();
			PdfContentByte cb = writer.getDirectContent();

			for (int i = 1; i <= endPage; i++) {
				PdfImportedPage page = writer.getImportedPage(reader, i);
				float Scale = 1f;
				cb.addTemplate(page, Scale, 0, 0, Scale, 0, 0);
				doc.newPage();
			}
			
			//put the information, like author name etc.
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

			PdfFile pdfFile =  new PdfFile(tempFile, originalFile);
			pdfFile.setPdfInfo(info);
			
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
            
            if (boundingBox.getWidth()>maxBoundingBox.getWidth())
            	maxBoundingBox.setRight(boundingBox.getWidth());
            
            if (boundingBox.getHeight()>maxBoundingBox.getHeight())
            	maxBoundingBox.setBottom(boundingBox.getHeight());
        }
		return maxBoundingBox;
	}

    
	
	public static void cropPdf(PdfFile pdfFile, File targetFile, ArrayList<java.awt.Rectangle> rects, int viewWidth, int viewHeight, ProgressMonitor progressMonitor) throws IOException, DocumentException {
		
		File originalFile = pdfFile.getOriginalFile();
		HashMap<String, String> pdfInfo = pdfFile.getPdfInfo();
		 
		//convert to actual rectangles
		PdfReader reader = new PdfReader(originalFile.getAbsolutePath());
		
		Rectangle currentSize = reader.getPageSizeWithRotation(1);
		float pdfWidth = currentSize.getWidth();
		float pdfHeight = currentSize.getHeight();
		System.out.println("Finding ratio : viewSize " + viewWidth + "x" + viewHeight + ", pdf size " + pdfWidth + "x" + pdfHeight);
		ArrayList<java.awt.Rectangle> cropRectsInIPDFCoords = new ArrayList<java.awt.Rectangle>(rects.size());
		double widthRatio = pdfWidth / viewWidth;
		double heightRatio = pdfHeight / viewHeight;
		if (widthRatio != heightRatio) {
			System.err.println("WARNING>>> RATION NOT SAME ?! " + widthRatio + " " + heightRatio);
		}
		for (java.awt.Rectangle rect : rects) {
			java.awt.Rectangle covertedRect = new java.awt.Rectangle();
			covertedRect.x = (int) (widthRatio * rect.x);
			covertedRect.y = (int) (widthRatio * (viewHeight - rect.y - rect.height));
			covertedRect.width = (int) (widthRatio * rect.width);
			covertedRect.height = (int) (widthRatio * rect.height);
			cropRectsInIPDFCoords.add(covertedRect);
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
			int cropCellCount = cropRectsInIPDFCoords.size();
			int newPageCount = cropCellCount * originalPageCount;
			for (int i = 0; i < originalPageCount;) {
				++i;
				for (int iCell = 0; iCell < cropCellCount; iCell++) {
					progressMonitor.setNote("Writing page "
							+ ((i - 1) * cropCellCount + iCell) + " of "
							+ newPageCount);
					progressMonitor.setProgress(i * 100 / originalPageCount);
	
					java.awt.Rectangle awtRect = cropRectsInIPDFCoords.get(iCell);
					// Rectangle itextRect = new Rectangle(awtRect.x , awtRect.y +
					// awtRect.height, awtRect.x + awtRect.width, awtRect.y);
					// Rectangle itextRect = new Rectangle(100,50);
					// writer.setBoxSize("crop", itextRect);
					// writer.setBoxSize("trim", itextRect);
					// writer.setBoxSize("art", itextRect);
					// writer.setBoxSize("bleed", itextRect);
					page = writer.getImportedPage(reader, i);
					// writer.setPageSize(itextRect);
					writer.addPage(page);
				}
			}
			// PRAcroForm form = reader.getAcroForm();
			// if (form != null) {
			// writer.copyAcroForm(reader);
			// }
			// f++;
			// if (!master.isEmpty()) {
			// writer.setOutlines(master);
			// }
			document.close();
			document = null;
			reader = new PdfReader(tempFile.getAbsolutePath());
	
			stamper = new PdfStamper(reader, new FileOutputStream(targetFile));
			int pageCount = reader.getNumberOfPages();

			for (int i = 0; i < pageCount;) {
				++i;
				// http://stackoverflow.com/questions/4089757/how-do-i-resize-an-existing-pdf-with-coldfusion-itext
	
				PdfDictionary pdfDictionary = reader.getPageN(i);
				PdfArray cropCell = new PdfArray();
				progressMonitor.setNote("Cropping page " + i + " of " + pageCount);
				progressMonitor.setProgress(i * 100 / pageCount);
				int cellIndex = (i - 1) % cropCellCount;
				java.awt.Rectangle awtRect = cropRectsInIPDFCoords.get(cellIndex);
				cropCell.add(new PdfNumber(awtRect.x));// lower left x
				cropCell.add(new PdfNumber(awtRect.y));// lower left y
				cropCell.add(new PdfNumber(awtRect.x + awtRect.width)); // up right x
				cropCell.add(new PdfNumber(awtRect.y + awtRect.height));// up righty
				pdfDictionary.put(PdfName.CROPBOX, cropCell);
				pdfDictionary.put(PdfName.MEDIABOX, cropCell);
				pdfDictionary.put(PdfName.TRIMBOX, cropCell);
				pdfDictionary.put(PdfName.BLEEDBOX, cropCell);
				
			}
			
			//put the information, like author name etc.
			stamper.setMoreInfo(pdfInfo);
		} finally {
			if (document != null) {
				document.close();
			}
			
			if(stamper != null) {
				stamper.close();
			}
			
			if(tempFile != null) {
				tempFile.delete();
			}
		}
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
    
    //helper code:  //http://itext-general.2136553.n4.nabble.com/How-to-Shrink-Content-and-Add-Margins-td2167577.html
//    public static void solution2() throws IOException, DocumentException { 
//        PdfReader reader = new PdfReader("sample.pdf"); 
//        int n = reader.getNumberOfPages(); 
//        PdfDictionary pageDict; 
//        ArrayList old_mediabox; 
//        PdfArray new_mediabox; 
//        PdfNumber value; 
//        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("solution2.pdf")); 
//        BaseFont font = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED); 
//        PdfContentByte directcontent; 
//        for (int i = 1; i <= n; i++) { 
//                pageDict = reader.getPageN(i); 
//                new_mediabox = new PdfArray(); 
//                old_mediabox = pageDict.getAsArray(PdfName.MEDIABOX).getArrayList(); 
//                value = (PdfNumber)old_mediabox.get(0); 
//                new_mediabox.add(new PdfNumber(value.floatValue() - 36)); 
//                value = (PdfNumber)old_mediabox.get(1); 
//                new_mediabox.add(new PdfNumber(value.floatValue() - 36)); 
//                value = (PdfNumber)old_mediabox.get(2); 
//                new_mediabox.add(new PdfNumber(value.floatValue() + 36)); 
//                value = (PdfNumber)old_mediabox.get(3); 
//                new_mediabox.add(new PdfNumber(value.floatValue() + 36)); 
//                pageDict.put(PdfName.MEDIABOX, new_mediabox); 
//                directcontent = stamper.getOverContent(i); 
//                directcontent.beginText(); 
//                directcontent.setFontAndSize(font, 12); 
//                directcontent.showTextAligned(Element.ALIGN_LEFT, "TEST", 0, -18, 0); 
//                directcontent.endText(); 
//        } 
//        stamper.close(); 
//} 
	
	private static void debug(String string) {
		System.out.println("TaskPdfOpen:" + string);
	}
}

