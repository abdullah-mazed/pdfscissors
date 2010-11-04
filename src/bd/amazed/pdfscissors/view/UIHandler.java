package bd.amazed.pdfscissors.view;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

public class UIHandler {
	public static int EDIT_MODE_SELECT = 1;
	public static int EDIT_MODE_DRAW = 0;

	private int editingMode;
	protected ArrayList<Rect> rects;
	protected Rect selectedRect;

	public UIHandler() {
		rects = new ArrayList<Rect>();
		selectedRect = null;
	}

	public int getEditingMode() {
		return editingMode;
	}

	public void setEditingMode(int mode) {
		if (mode != EDIT_MODE_DRAW && mode != EDIT_MODE_SELECT) {
			throw new IllegalArgumentException("Invalid edit mode");
		}
		this.editingMode = mode;
	}

	public Iterator<Rect> getRectIterator() {
		return rects.iterator();
	}

	public Rect getSelectedRect() {
		return selectedRect;
	}

	protected void setSelectedRect(Rect rectToSelect) {
		if (selectedRect != rectToSelect) { // if change in selection
			if (selectedRect != null) // deselect previous selection
				selectedRect.setSelected(false);
			selectedRect = rectToSelect; // set selection to new rect
			if (selectedRect != null) {
				rectToSelect.setSelected(true);
			}
		}
	}

	public void addRect(Rect rect) {
		rects.add(rect);
	}

	public ArrayList<Rect> getAllRects() {
		return rects;
	}
	
	public ArrayList<Rectangle> getAllRectangles() {
		ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
		for (Rect rect : rects) {
			rectangles.add(rect.bounds); //TODO rename rect by CropCell or something
		}
		return rectangles;
	}

	public void deleteSelected() {
		if (selectedRect != null) {
			rects.remove(selectedRect);
			Rect toDelRect = selectedRect;
			selectedRect = null;
			toDelRect.fireEvent(null);
			
		}
	}
	
	public int getRectCount() {
		return rects.size();
	}
	
	public int getIndexOf(Rect rect) {
		return rects.indexOf(rect);
	}

	public void deleteAll() {
		Rect anyRect = null;
		if (rects.size() > 0) {
			anyRect = rects.get(0);
		}
		rects.clear();
		if (anyRect != null) {
			anyRect.fireEvent(null); // if canvas repaints whole area once, that
										// will do.
		}
	}

	public void reset() {
		deleteAll();
		selectedRect = null; //we are removing all rects, so all rects listners should vanish too.
	}
}
