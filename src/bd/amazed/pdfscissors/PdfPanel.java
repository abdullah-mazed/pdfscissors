/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Gagan
 */
public class PdfPanel extends JPanel {

    private String name = "DefaultPanel";
    private BufferedImage image;
    private Image scaledImage;
    private double zoomFactor = 1.0;

    /**
     * Should call revalidate of Form after calling this.
     * @param image
     */
    public void setImage(BufferedImage image) {        
        this.image = image;
        PdfscissorsApp.debug(name + ".setImage : " + image);
        if( image != null) {
            PdfscissorsApp.debug(name + "setting size " + image.getWidth() + "x" + image.getHeight());            
        }
        setZoomFactor(zoomFactor, true); //update zoom, if there is already any
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), getHeight());        
        if (scaledImage != null) {
            g.drawImage(scaledImage, 0, 0, this);
        } else if(image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }


    public void setZoomFactor(double zoomFactor) {
        setZoomFactor(zoomFactor, false);
    }
    /**
     * Should call revalidate of Form after calling this.
     * @param zoomFactor max 1.0
     */
    private void setZoomFactor(double zoomFactor, boolean forceRefresh) {
        if (this.zoomFactor != zoomFactor || forceRefresh) {
            this.zoomFactor = zoomFactor;
            if (zoomFactor == 1) {
                scaledImage = null;
                if (image != null) {
                    setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
                    setSize(new Dimension(image.getWidth(this), image.getHeight(this)));
                }
                repaint();
            } else {
                if (image != null) {
                    scaledImage = image.getScaledInstance((int)(image.getWidth() * zoomFactor), (int)(image.getHeight() * zoomFactor), BufferedImage.SCALE_FAST);
                    setPreferredSize(new Dimension(scaledImage.getWidth(this), scaledImage.getHeight(this)));
                    setSize(new Dimension(scaledImage.getWidth(this), scaledImage.getHeight(this)));
                } else {
                    scaledImage = null;
                }
            }
        }
    }


}
