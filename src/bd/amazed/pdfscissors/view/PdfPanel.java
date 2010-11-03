/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.RectChangeListener;

/**
 * 
 * @author Gagan
 */
public class PdfPanel extends JPanel implements ModelListener, RectChangeListener {

	private String name = "DefaultPanel";
	protected UIHandler uiHandler;
	
	public PdfPanel(UIHandler uiHandler) {
		this.uiHandler = uiHandler;
		MouseHandler handler = new MouseHandler();
		addMouseListener(handler);
		addMouseMotionListener(handler);
	}

	private void debug(String string) {
		System.out.println("PdfPanel: " + string);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Image image = getImage();
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
		
		Rectangle clipRect = g.getClipBounds();
		
		Iterator<Rect> iter = uiHandler.getRectIterator();
		while (iter.hasNext()) {
			(iter.next()).draw(g, clipRect);
		}
		//whole page area
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

	}

	private Image getImage() {
		Model model = Model.getInstance();
		Image image = null;
		if (model.getZoomFactor() != 1) {
			image = model.getScaledPreivewImage();
		} else {
			image = model.getPreviewImage();
		}
		return image;
	}

	private void updateSize() {
		Image image = getImage();
		if (image != null) {
			int width = image.getWidth(this);
			int height = image.getHeight(this);
			debug("Setting pdf size : " + width + "x" + height);
			setPreferredSize(new Dimension(width, height));
			setSize(new Dimension(width, height));
		}
		repaint();
	}

	@Override
	public void newPdfLoaded() {
		debug("listening to new pdf loaded");
		updateSize();
	}

	@Override
	public void pdfLoadFailed(File failedFile, Throwable cause) {
		//nothing to do
	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		updateSize();
	}
		
	
	@Override
	public void rectUpdated(Rect rect, Rectangle repaintArea) {
		if (repaintArea != null) {
			repaint(repaintArea);
		} else {
			repaint();
		}
	}

	protected Rect getRectAt(Point pt) {
		ArrayList<Rect> allRects = uiHandler.getAllRects();
		for (int i = allRects.size() - 1; i >= 0; i--) {
			Rect r = (Rect) allRects.get(i);
			if (r.inside(pt))
				return r;
		}
		return null;
	}


	protected class MouseHandler extends MouseAdapter implements MouseMotionListener {
		private Point dragAnchor; // variables using to track state during drag operations
		private int dragStatus;
		private static final int DRAG_NONE = 0;
		private static final int DRAG_CREATE = 1;
		private static final int DRAG_RESIZE = 2;
		private static final int DRAG_MOVE = 3;
		

		/**
		 * When the mouse is pressed we need to figure out what action to take.
		 * If the tool mode is arrow, the click might be a select, move or
		 * reisze. If the tool mode is one of the rects, the click initiates
		 * creation of a new rect.
		 */
		public void mousePressed(MouseEvent event) {
			Rect clicked = null;
			Point curPt = event.getPoint();

			if (uiHandler.getEditingMode() == uiHandler.EDIT_MODE_SELECT) {
				// first, determine if click was on resize knob of selected rect
				if (uiHandler.getSelectedRect() != null
						&& (dragAnchor = uiHandler.getSelectedRect().getAnchorForResize(curPt)) != null) {
					dragStatus = DRAG_RESIZE; // drag will resize this rect
				} else if ((clicked = getRectAt(curPt)) != null) { //if not check if any rect was clicked
					uiHandler.setSelectedRect(clicked);
					dragStatus = DRAG_MOVE; // drag will move this rect
					dragAnchor = curPt;
				} else { // else this was a click in empty area, deselect
							// selected rect,
					uiHandler.setSelectedRect(null);
					dragStatus = DRAG_NONE; // drag does nothing in this case
				}
			} else {
				Rect newRect = new Rect(curPt, uiHandler); // create rect here
				newRect.addListener(PdfPanel.this);
				uiHandler.addRect(newRect);
				uiHandler.setSelectedRect(newRect);
				dragStatus = DRAG_CREATE; // drag will create (resize) this rect
				dragAnchor = curPt;
			}
		}

		/**
		 * As the mouse is dragged, our listener will receive periodic updates
		 * as mouseDragged events. When we get an update position, we update the
		 * move/resize event that is in progress.
		 */
		public void mouseDragged(MouseEvent event) {
			Point pointer = event.getPoint();			
			switch (dragStatus) {
			case DRAG_MOVE:
				uiHandler.getSelectedRect().translate(pointer.x - dragAnchor.x,
						pointer.y - dragAnchor.y, getWidth(), getHeight());
				dragAnchor = pointer; // update for next dragged event
				break;
			case DRAG_CREATE:
			case DRAG_RESIZE:
				uiHandler.getSelectedRect().resize(dragAnchor, pointer,
						getWidth(), getHeight());
				break;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			Rect selectedRect = uiHandler.getSelectedRect();
			if (selectedRect != null && selectedRect.bounds != null &&(selectedRect.bounds.getWidth() <= 0 || selectedRect.bounds.getHeight() <= 10)) { //TOO small, we dont add those
				uiHandler.deleteSelected();
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			super.mouseEntered(e);
			if (uiHandler.getEditingMode() == UIHandler.EDIT_MODE_DRAW) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			} else {
				setCursor(Cursor.getDefaultCursor());
			}
		}
		
		@Override
		public void mouseExited(MouseEvent e) {		
			super.mouseExited(e);
			setCursor(Cursor.getDefaultCursor());	
		}
		
		public void showPopup(java.awt.event.MouseEvent evt) {
	        if (evt.isPopupTrigger()) {
	            
	        }
	 
	    }
	}
}
