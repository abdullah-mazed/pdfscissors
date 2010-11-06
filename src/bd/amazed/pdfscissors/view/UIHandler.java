package bd.amazed.pdfscissors.view;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class UIHandler {
	public static int EDIT_MODE_SELECT = 1;
	public static int EDIT_MODE_DRAW = 0;

	private int editingMode;
	protected ArrayList<Rect> rects;
	protected Rect selectedRect;
	
	private int page;
	private boolean showMergeMode;
	
	private Vector<UIHandlerListener> listeners;

	public UIHandler() {
		rects = new ArrayList<Rect>();
		listeners = new Vector<UIHandlerListener>();
		reset();
	}

	public int getEditingMode() {
		return editingMode;
	}

	public void setEditingMode(int mode) {
		if (mode != EDIT_MODE_DRAW && mode != EDIT_MODE_SELECT) {
			throw new IllegalArgumentException("Invalid edit mode");
		}
		this.editingMode = mode;
		fireEditModeChanged();
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
		delete(selectedRect);
	}

	public void delete(Rect rect) {
		if (rect != null) {
			rects.remove(rect);
			if (selectedRect == rect) {
				selectedRect = null;
			}
			rect.fireEvent(null);			
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
		page = 1;
		showMergeMode = true;
	}

	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
		firePageChanged();
	}

	public void setMergeMode(boolean showMergeMode) {
		this.showMergeMode = showMergeMode;
	}

	public boolean isShowMergedMode() {
		return showMergeMode;
	}
	
	public void addListener(UIHandlerListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public boolean removeListener(UIHandler listner) {
		return listeners.remove(listner);
	}
	
	public void removeAllListeners() { 
		listeners.removeAllElements();
	}
	
	private void fireEditModeChanged() {
		for (UIHandlerListener listener : listeners) {
			listener.editingModeChanged(editingMode);
		}
	}
	
	private void firePageChanged() {
		for (UIHandlerListener listener : listeners) {
			listener.pageChanged(page);
		}
	}

	public void equalizeWidthOfSelected(int viewWidth) {
		int maxWidth = -1;
		Rect maxWidthRect;
		for (Rect rect: rects) {
			if (rect.bounds.width > maxWidth) {
				maxWidth = rect.bounds.width;
				maxWidthRect = rect;
			}
		}
		for (Rect rect: rects) {
			rect.bounds.width = maxWidth;
			if (rect.bounds.x + maxWidth > viewWidth) {
				rect.bounds.x = viewWidth - maxWidth;
			}
		}
		
		if (rects.size() > 0) {
			rects.get(0).fireEvent(null);
		}
	}
	
	public void equalizeHeightOfSelected(int viewHeight) {
		int maxHeight = -1;
		Rect maxWidthRect;
		for (Rect rect: rects) {
			if (rect.bounds.height > maxHeight) {
				maxHeight = rect.bounds.height;
				maxWidthRect = rect;
			}
		}
		for (Rect rect: rects) {
			rect.bounds.height = maxHeight;
			if (rect.bounds.y + maxHeight > viewHeight) {
				rect.bounds.y = viewHeight - maxHeight;
			}
		}
		if (rects.size() > 0) {
			rects.get(0).fireEvent(null);
		}
	}
}
