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
        int endPage = pageNumber + 4;
        BufferedImage lastPage = getPdfDecoder().getPageAsImage(endPage);

        if (lastPage != null) {
            for (int i = endPage; i >= 1; i--) {
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
