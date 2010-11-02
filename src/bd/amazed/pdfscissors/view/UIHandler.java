package bd.amazed.pdfscissors.view;

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
}
