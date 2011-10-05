package bd.amazed.pdfscissors.model;

import java.awt.image.BufferedImage;

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

/**
 * 
 * Only to get the page image with transparency support
 * @author Gagan
 *
 */
public class PdfDecoderMod extends PdfDecoder{

	public PdfDecoderMod() {
		super();
	}
	
	public PdfDecoderMod(boolean newRender) {
		super(newRender);
	}
	
	public BufferedImage getPageAsImage(int pageIndex) throws PdfException {
		boolean originalValue = isRunningOnAIX;
		isRunningOnAIX = true;
		//stupid looking code is to get the transparency support. Too bad getPageAsTransparent was a private method
		BufferedImage image = super.getPageAsImage(pageIndex);
		isRunningOnAIX = originalValue;
		return image;
	}
	
}
