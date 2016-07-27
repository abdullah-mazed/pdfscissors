package bd.amazed.pdfscissors.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;

import bd.amazed.pdfscissors.view.Rect;


/**
 * A map containing crop rectangle for each page.
 *
 */
public class PageRectsMap {
	private HashMap<Integer, ArrayList<Rectangle>> pageRectsMap;
	
	private ArrayList<Rect> rectsInsertionOrder;
	
	public PageRectsMap() {
		pageRectsMap = new HashMap<Integer, ArrayList<Rectangle>>();
	}
	
	public void putRects(int pageNumber, ArrayList<Rectangle> cropRects) {
		Integer page = Integer.valueOf(pageNumber);
		pageRectsMap.put(page, cropRects);
	}
	
	public void putRects(Integer pageNumber, ArrayList<Rectangle> cropRects) {
		pageRectsMap.put(pageNumber, cropRects);
	}
	
	public ArrayList<Rectangle> getRects(int pageNumber) {
		return pageRectsMap.get(Integer.valueOf(pageNumber));
	}
	
	/*
	 *@return Returns the converted awt rectangles ready to use for cropping. null means there is no cropping rect, keep the whole page
	 */
	public ArrayList<Rectangle> getConvertedRectsForCropping(int pageNumber, int viewWidth, int viewHeight, float pdfWidth, float pdfHeight) {
		ArrayList<Rectangle> rects = getRects(pageNumber);
		if (rects == null) {
			return null;
		}
		ArrayList<java.awt.Rectangle> cropRectsInIPDFCoords = new ArrayList<java.awt.Rectangle>(rects.size());
		for (java.awt.Rectangle rect : rects) {
			java.awt.Rectangle covertedRect = this.getConvertedRectForCropping(rect, pageNumber, viewWidth, viewHeight, pdfWidth, pdfHeight);
			cropRectsInIPDFCoords.add(covertedRect);
		}
		
		return cropRectsInIPDFCoords;

	}
	
	public Rectangle getConvertedRectForCropping(Rectangle rect, int pageNumber, int viewWidth, int viewHeight, float pdfWidth, float pdfHeight) {

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
	
	@Override
	public String toString() {
		return pageRectsMap.toString();
	}

	public ArrayList<Rect> getRectsInsertionOrder() {
		return rectsInsertionOrder;
	}

	public void setRectsInsertionOrder(ArrayList<Rect> rectsInsertionOrder) {
		this.rectsInsertionOrder = rectsInsertionOrder;
	}
			
}
