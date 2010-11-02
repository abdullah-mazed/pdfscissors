package bd.amazed.pdfscissors.model;

import java.awt.Rectangle;

import bd.amazed.pdfscissors.view.Rect;

public interface RectChangeListener {
	public void rectUpdated(Rect updatedRect, Rectangle repaintArea);
}
