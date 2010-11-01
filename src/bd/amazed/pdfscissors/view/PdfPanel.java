/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JPanel;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;

/**
 * 
 * @author Gagan
 */
public class PdfPanel extends JPanel implements ModelListener {

	private String name = "DefaultPanel";

	private void debug(String string) {
		System.out.println("PdfPanel: " + string);// XXX
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth(), getHeight());

		Image image = getImage();
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		}
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
		// TODO Auto-generated method stub

	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		updateSize();
	}

}
