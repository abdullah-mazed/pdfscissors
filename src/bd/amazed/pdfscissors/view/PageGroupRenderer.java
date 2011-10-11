package bd.amazed.pdfscissors.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.model.RectChangeListener;

public class PageGroupRenderer extends JComponent implements ListCellRenderer {

	private PageGroup currentGroup;
	private float pdfWidth = 100;
	private float pdfHeight = 100;
	private int padding = 1;
	private boolean isSelected;
	private static Color selectedBg = new Color(0x00D0FF);
	
	public PageGroupRenderer() {
	}
	
	public void setPageSize(float pdfWidth, float pdfHeight) {
		this.pdfWidth = pdfWidth;
		this.pdfHeight = pdfHeight;
	}
	

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean hasFocus) {
		if (value instanceof PageGroup) {
			PageGroup pageGroup = (PageGroup) value;
			currentGroup = pageGroup;
		}
		this.isSelected = isSelected;
		return this;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (isSelected) {
			g.setColor(selectedBg);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		int offsetFromEdge = padding * 2;
		int boxWidth = getWidth() - offsetFromEdge * 2;
		int boxHeight = getHeight() - offsetFromEdge * 2;
		g.setColor(Color.white);
		g.fillRect(offsetFromEdge, offsetFromEdge, boxWidth , boxHeight);
		g.setColor(Color.black);
		g.drawRect(offsetFromEdge, offsetFromEdge, boxWidth, boxHeight);
		//shadow
		g.drawLine(offsetFromEdge + 1, offsetFromEdge + boxHeight + 1, offsetFromEdge + boxWidth + 1, offsetFromEdge + boxHeight + 1);
		g.drawLine(offsetFromEdge + boxWidth + 1, offsetFromEdge + 1, offsetFromEdge + boxWidth + 1, offsetFromEdge + boxHeight + 1);
	
		
		
		Iterator<Rect> iter = currentGroup.getRects().iterator();
		g.translate(offsetFromEdge, offsetFromEdge);
		while (iter.hasNext()) {			
			(iter.next()).draw(g, (getWidth()  - offsetFromEdge * 2)/ pdfWidth, Rect.STROKE_SOLID, false);
		}
		g.translate(-offsetFromEdge, -offsetFromEdge);
		
		g.setColor(Color.black);
		String text = currentGroup.toString();
		g.drawString(text, offsetFromEdge + (boxWidth - g.getFontMetrics().stringWidth(text))/2, offsetFromEdge + boxHeight - g.getFontMetrics().getHeight());
	}

	@Override
	public Dimension getPreferredSize() {
		int prefWidth = 100;
		prefWidth = Math.max(prefWidth, (int)(pdfWidth / 10));
		padding = Math.max(1, prefWidth / 20);
		int prefHeight = (int)((prefWidth * pdfHeight) / pdfWidth);
		return new Dimension(prefWidth + padding * 4, prefHeight + padding * 4);
	}

}