package bd.amazed.pdfscissors.model;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import bd.amazed.pdfscissors.view.Rect;

import com.itextpdf.text.DocumentException;

public class TaskPdfSave extends SwingWorker<Boolean, Void> {

	private PdfFile pdfFile;
	private File targetFile;
	private ArrayList<Rectangle> cropCells;
	private int viewWidth;
	private int viewHeight;
	private Component owner;
	
	ProgressMonitor progressMonitor;
	public TaskPdfSave(PdfFile pdfFile, File targetFile, ArrayList<Rectangle> cropCells, int viewWidth, int viewHeight, Component owner) {
		this.pdfFile = pdfFile;
		this.targetFile = targetFile;
		this.cropCells = cropCells;
		this.owner = owner;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		progressMonitor = new ProgressMonitor(owner,
                "Saving " + targetFile.getName() + "...",
                "", 0, 100);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		debug("Cropping to " + targetFile + "...");
		PdfCropper.cropPdf(pdfFile, targetFile, cropCells, viewWidth, viewHeight, progressMonitor);
		debug("Cropping success : " + targetFile);
		return true;
	}

	@Override
	protected void done() {
		super.done();
		progressMonitor.close();
		if (! progressMonitor.isCanceled()) {
			
			try {
				if(this.get()) {
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
					throw new ExecutionException("Failed to save!", null); //I guess this will never happen
				}
			} catch (InterruptedException e) {
				e.printStackTrace(); //ignore
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


