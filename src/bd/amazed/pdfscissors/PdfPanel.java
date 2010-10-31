/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bd.amazed.pdfscissors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Gagan
 */
public class PdfPanel extends JPanel {

    private String name = "DefaultPanel";
    private BufferedImage image;

    public void setImage(BufferedImage image) {        
        this.image = image;
        PdfscissorsApp.debug(name + ".setImage : " + image);
        if( image != null) {
            PdfscissorsApp.debug(name + "setting size " + image.getWidth() + "x" + image.getHeight());
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            setSize(new Dimension(image.getWidth(), image.getHeight()));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, width, height);
            g.drawImage(image, 0, 0, this);
        } else {
        }
    }


}
