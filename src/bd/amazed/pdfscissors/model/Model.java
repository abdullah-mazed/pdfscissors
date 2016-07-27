package bd.amazed.pdfscissors.model;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.management.RuntimeErrorException;

import bd.amazed.pdfscissors.view.Rect;

public class Model {

	public static final String PROPERTY_LAST_FILE = "lastFile";
	
	public static final String PROPERTY_LAST_STACK_TYPE= "lastStackType";
	

	public static final String PROPERTY_DEFAULT_STACK_TYPE = "defaultStackType";//MOD russa
	public static final String PROPERTY_DEFAULT_CREATE_STACKED_VIEW = "createStackedView";//MOD russa
	
	private static Model instance;

	private Vector<ModelListener> modelListeners;

	private PdfFile currentPdf = PdfFile.NullPdf();

	private double zoomFactor;

	private Rect clipboardRect;
	private boolean isClipboardCut;
	private Properties properties;
	private String propertyFileLocation;

	private int groupType;
	
	private Vector<PageGroup> pageGroups;
	
	private Model() {
		modelListeners = new java.util.Vector<ModelListener>();
		reset();
	}


	private void reset() {
		zoomFactor = 1;
		pageGroups = new Vector<PageGroup>();
	}



	public static Model getInstance() {
		if (instance == null) {
			instance = new Model();
		}
		return instance;
	}

	public void addListener(ModelListener listener) {
		if (!modelListeners.contains(listener)) {
			modelListeners.add(listener);
		}
	}

	/**
	 * 
	 * @return true if listener has been removed, false if not found
	 */
	public boolean removeListener(ModelListener listener) {
		return modelListeners.remove(listener);
	}

	/**
	 * 
	 * @param file file, must not be null. This should be a normalized temp file
	 * @param originalFile original file
	 * @param previewImage previewImage, must not be null
	 */
	public void setPdf(PdfFile pdfFile, Vector<PageGroup> pageGroups) {
		if (currentPdf == null) {
			throw new IllegalArgumentException("Cannot set null pdf file");
		}
		this.currentPdf = pdfFile;
		reset(); //on new pdf load reset everything
		fireNewPdf(pdfFile);
		setPageGroups(pageGroups);
	}


	public PdfFile getPdf() {
		return this.currentPdf;
	}

	/**
	 * Notify model that some pdf loading has failed.
	 * 
	 * @param file
	 */
	public void setPdfLoadFailed(File file, Throwable err) {
		fireSetPdfFailed(file, err);
	}
	
	private void setPageGroups(Vector<PageGroup> pageGroups) {
		this.pageGroups = pageGroups;
		firePageGroupChanged(pageGroups);
	}

	public void copyToClipboard(boolean isCut, Rect rect) {
		if (rect != null) {
			this.clipboardRect = rect;
			this.isClipboardCut = isCut;
			fireClipboardCopyEvent(clipboardRect, isClipboardCut);
		}
	}

	public void pasteFromClipboard() {
		if (clipboardRect != null) {
			fireClipboardPasteEvent(clipboardRect, isClipboardCut);
			if (isClipboardCut) {
				isClipboardCut = false; // clear clipboard
				clipboardRect = null;
			}
		}
	}

	public Rect getClipboardRect() {
		return clipboardRect;
	}
	
	public Vector<PageGroup> getPageGroups() {
		return pageGroups;
	}
	
	public PageRectsMap getPageRectsMap() {
		PageGroup pageGroup = null;
		Vector<Integer> pages = null;
		PageRectsMap pageRectsMap = new PageRectsMap();
		for (int i = 0; i < pageGroups.size(); i++) {
			pageGroup = pageGroups.elementAt(i);
			pages = pageGroup.getPages();
			for (int page = 0; page < pages.size(); page++) {
				pageRectsMap.putRects(pages.get(page), pageGroup.getRectangles());
			}
		}
		return pageRectsMap;
	}

	/**
	 * 
	 * @param targetPageIndex if -1 use crop all page, otherwise crop only the given page
	 * @param rectOrderList if non null, crop the pdf using the given order of rects
	 * @return
	 */
	public ArrayList<CropRect> getCropRects(int targetPageIndex, ArrayList<Rect> rectOrderList) {
		ArrayList<CropRect> cropRects = new ArrayList<CropRect>();
		
		if (this.groupType != PageGroup.GROUP_TYPE_INDIVIDUAL) {
			int indexStart = 0;
	
			int originalPageCount = pageGroups.size();
			if(targetPageIndex > -1){
				//adjust start-indices / end-conditions, so that only the one page gets saved
				indexStart = targetPageIndex;
				originalPageCount = indexStart + 1;
			}
			
			PageGroup pageGroup = null;
			Vector<Integer> pages = null;
			for (int i = indexStart; i < originalPageCount; i++) {
				pageGroup = pageGroups.elementAt(i);
				pages = pageGroup.getPages();
				for (int page = 0; page < pages.size(); page++) {
					ArrayList<Rectangle> pageGroupRectangles = pageGroup.getRectangles();
					if (pageGroupRectangles == null || pageGroupRectangles.size() == 0) {
						CropRect cropRect = new CropRect();
						cropRect.pageNumber = pages.get(page);
						cropRect.rectangle = null;//keep original page crop
						cropRects.add(cropRect);
					} else {
						for (Rectangle rectangle : pageGroupRectangles) {
							CropRect cropRect = new CropRect();
							cropRect.pageNumber = pages.get(page);
							cropRect.rectangle = rectangle;
							cropRects.add(cropRect);
						}
					}
				}
			}
		} else {
			//for each rect, find which page it is in and add to list
			for (Rect rectInOrder : rectOrderList) {
				int pageNumber = findPageNumber(rectInOrder);
				if (pageNumber == -1) {
					throw new RuntimeException("Cound not find ordered rect");
				}
				CropRect cropRect = new CropRect();
				cropRect.pageNumber = pageNumber;
				cropRect.rectangle = rectInOrder.getRectangleBound();
				cropRects.add(cropRect);
			}
		}
		return cropRects;
	}


	private int findPageNumber(Rect rectToFind) {
		for (PageGroup pageGroup : pageGroups) {
			for (Rect rect : pageGroup.getRects()) {
				if (rect == rectToFind) {
					return pageGroup.getLastPage();
				}
			}
		}
		return -1;
	}


	public double getZoomFactor() {
		return zoomFactor;
	}

	protected void fireNewPdf(PdfFile pdfFile) {
		for (ModelListener listener : modelListeners) {
			listener.newPdfLoaded(pdfFile);
		}
	}

	protected void fireSetPdfFailed(File failedFile, Throwable cause) {
		for (ModelListener listener : modelListeners) {
			listener.pdfLoadFailed(failedFile, cause);
		}
	}
	
	protected void firePageGroupChanged(Vector<PageGroup> pageGroups) {
		for (ModelListener listener : modelListeners) {
			listener.pageGroupChanged(pageGroups);
		}
	}


	protected void fireZoomChanged(double oldZoomFactor, double newZoomFactor) {
		for (ModelListener listener : modelListeners) {
			listener.zoomChanged(oldZoomFactor, newZoomFactor);
		}
	}

	protected void fireClipboardCopyEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardCopy(isCut, onClipboard);
		}
	}

	protected void fireClipboardPasteEvent(Rect onClipboard, boolean isCut) {
		for (ModelListener listener : modelListeners) {
			listener.clipboardPaste(isCut, onClipboard);
		}
	}

	public Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			try {
				properties.load(new FileInputStream(getPropertyFileLocation()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.properties;
	}

	private String getPropertyFileLocation() {
		if (this.propertyFileLocation == null) {
			this.propertyFileLocation = System.getProperty("user.home") + File.separator + "pdfscissors/";
			File file = new File(propertyFileLocation);
			if (!file.exists()) {
				file.mkdir();
			}
			this.propertyFileLocation += "pdfscissors.properties";
		}
		return this.propertyFileLocation;
	}

	public void close() {
		try {
			getProperties().store(new FileOutputStream(getPropertyFileLocation()), null);
		} catch (IOException e) {
		}
	}


	public int getGroupType() {
		return groupType;
	}


	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}
}
