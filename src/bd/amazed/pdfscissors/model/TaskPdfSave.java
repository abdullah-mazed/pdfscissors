package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

public class TaskPdfSave extends SwingWorker<Boolean, Void> {

	private PdfFile pdfFile;
	private File targetFile;
	PageRectsMap pageRectsMap;
	private int viewWidth;
	private int viewHeight;
	private Component owner;
	
	private int targetPageIndex;//MOD russa: if != -1, only current selection (on the page) is saved

	ProgressMonitor progressMonitor;

	public TaskPdfSave(PdfFile pdfFile, File targetFile, PageRectsMap pageRectsMap, int viewWidth, int viewHeight,
			Component owner) {
		
		this(pdfFile, targetFile, pageRectsMap, -1, //MOD russa: additional arg pageIndex: if -1 ignored, otherwise: only save this single page
				viewWidth, viewHeight, owner);
	}
	
	public TaskPdfSave(PdfFile pdfFile, File targetFile, PageRectsMap pageRectsMap, int pageIndex, //MOD russa: additional arg pageIndex: if -1 ignored, otherwise: only save this single page
			int viewWidth, int viewHeight, Component owner) {
		this.pdfFile = pdfFile;
		this.targetFile = targetFile;
		this.pageRectsMap = pageRectsMap;
		this.owner = owner;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.targetPageIndex = pageIndex;
		progressMonitor = new ProgressMonitor(owner, "Saving " + targetFile.getName() + "...", "", 0, 100);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		debug("Cropping to " + targetFile + "...");
		PdfCropper.cropPdf(pdfFile, targetFile, pageRectsMap, targetPageIndex, viewWidth, viewHeight, progressMonitor);
		debug("Cropping success : " + targetFile);
		return true;
	}

	@Override
	protected void done() {
		super.done();
		progressMonitor.close();
		if (!progressMonitor.isCanceled()) {

			try {
				if (this.get()) {
					if (Desktop.isDesktopSupported()) {
						progressMonitor.setNote("Cropping done! Opening cropped file...");
						try {
							Desktop.getDesktop().open(targetFile);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(owner, "Saved file, but could not open it. Please check " + targetFile.getAbsolutePath() + "\n\nDetails:" + e.getCause());
						}
					} else {
						JOptionPane.showMessageDialog(owner, "Cropping done.\nFile saved to \n" + targetFile.getAbsolutePath());
						debug("Hmm.. cannot open the cropped file. Desktop.isDesktopSupported() is false");
					}
				} else {
					throw new ExecutionException("Failed to save!", null); // I guess this will never happen
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); // ignore
			} catch (ExecutionException e) {
				JOptionPane.showMessageDialog(owner, "Failed to save image ...\nDetails:" + e.getCause());
				e.printStackTrace();
			}
		}
	}

	private void debug(String string) {
		System.out.println("TaskPdfSave:" + string);
	}

}
