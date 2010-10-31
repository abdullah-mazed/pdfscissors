/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors.model;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

/**
 *
 * @author Gagan
 */
public class PdfCropper {

    private File mainFile;
    private PdfDecoder pdfDecoder;

    public PdfCropper(File file) {
        if (file == null) {
            throw new IllegalArgumentException("PDFCropper does not accept null file.");
        }
        this.mainFile = file;
    }

    public BufferedImage getImage(int pageNumber) throws PdfException {
        //TODO validate page number
        BufferedImage pageImage = getPdfDecoder().getPageAsImage(pageNumber);
        BufferedImage pageImage2 = getPdfDecoder().getPageAsImage(pageNumber + 1);
        if (pageImage != null && pageImage2 != null) {
        float alpha = 0.5f;
        int type = AlphaComposite.SRC_OVER;
        AlphaComposite composite = AlphaComposite.getInstance(type, alpha);
        Graphics2D g = (Graphics2D) pageImage2.getGraphics();
        g.setComposite(composite);
        g.drawImage(pageImage, 0, 0, null);
        }

        return pageImage2;
    }

    public void close() {
        if (pdfDecoder != null) {
            pdfDecoder.closePdfFile();
        }
    }

    private PdfDecoder getPdfDecoder() throws PdfException {
        if (pdfDecoder == null) {
            pdfDecoder = new PdfDecoder(true);
            pdfDecoder.openPdfFile(mainFile.getAbsolutePath());
        }
        return pdfDecoder;
    }
}
