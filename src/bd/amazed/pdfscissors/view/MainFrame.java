/**
 * 
 */
package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.PdfCropper;
import bd.amazed.pdfscissors.model.TaskPdfOpen;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.ScrollPane;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventObject;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jpedal.exception.PdfException;
import javax.swing.JScrollPane;
import java.util.Vector;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;

/**
 * @author Gagan
 * 
 */
public class MainFrame extends JFrame implements ModelListener {

	private File currentFile;
	private PdfPanel defaultPdfPanel;

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jButton = null;
	private JScrollPane scrollPanel = null;
	/** Panel containing PdfPanels. */
	private JPanel pdfPanelsContainer = null;
	private ButtonGroup rectButtonGroup = null;  //  @jve:decl-index=0:
	/** Contains all components that are disabled until file open.*/
	private Vector<Component> openFileDependendComponents = null;
	/**
	 * Keeps track of already registered listeners. Used to re-register
	 * listners.
	 */
	private Vector<ModelListener> modelRegisteredListeners;
	private JToolBar toolBar = null;
	private UIHandler uiHandler = null;
	private JToggleButton buttonDraw = null;
	private JToggleButton buttonSelect = null;
	private JButton buttonDeleteRect = null;
	private JButton buttonDelAll = null;

	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		modelRegisteredListeners = new Vector<ModelListener>();
		openFileDependendComponents = new Vector<Component>();
		initialize();
		registerComponentsToModel();
		updateOpenFileDependents();
		try {
			URL url = MainFrame.class.getResource("/icon.png");
			if(url != null) {
				setIconImage(ImageIO.read(url));
			}
		} catch (IOException e) {
			System.err.println("Failed to get frame icon");
		}
		
		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		}catch(Throwable e) {
			System.err.println("Failed to set exit on close." + e);
		}
	}

	

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setSize(800, 600);
		this.rectButtonGroup = new ButtonGroup();
		this.setContentPane(getJContentPane());
		this.setTitle("PDF Scissors");
		this.uiHandler = new UIHandler();
	}

	private void registerComponentsToModel() {
		Model model = Model.getInstance();
		// we want to maintain listners order, first child components, then me.
		// So we remove old ones first
		for (ModelListener listener : modelRegisteredListeners) {
			model.removeListener(listener);
		}
		modelRegisteredListeners.removeAllElements();

		// if there is any listner component inside scrollpane (like PdfPanel),
		// lets add them first
		if (pdfPanelsContainer != null) {
			int scollPaneComponents = pdfPanelsContainer.getComponentCount();
			Component component = null;

			for (int i = 0; i < scollPaneComponents; i++) {
				component = pdfPanelsContainer.getComponent(i);
				if (component instanceof ModelListener) {
					model.addListener((ModelListener) component);
					modelRegisteredListeners.add((ModelListener) component);
				}
			}
		}

		// finally add myself.
		model.addListener(this);
		modelRegisteredListeners.add(this);
	}
	
	/**
	 * Enable/disable buttons etc which should be disabled when there is no file.
	 */
	private void updateOpenFileDependents() {
		boolean shouldEnable = Model.getInstance().getCurrentFile() != null;
		for (Component component : openFileDependendComponents) {
			component.setEnabled(shouldEnable);
		}
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getScrollPanel(), BorderLayout.CENTER);
			jContentPane.add(getToolBar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {			
			jButton = new JButton("Open"); //a string literal is here only for eclipse visual editor.
			String imageFile = "/open.png";
			String text = "Open a pdf file";
			setButton(jButton, imageFile, text, false);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openFile();
				}

			});
		}
		return jButton;
	}
	
	private void setButton(AbstractButton button, String imageLocation, String tooltip, boolean isOpenFileDependent) {
        String imgLocation = imageLocation;
        URL imageURL = MainFrame.class.getResource(imageLocation);
        if (imageURL != null) {                      //image found
            button.setIcon(new ImageIcon(imageURL, tooltip));
            button.setText(null);
        } else {                                     //no image found
            button.setText(tooltip);
            System.err.println("Resource not found: " + imgLocation);
        }
        button.setToolTipText(tooltip);
        button.setActionCommand(tooltip);
        if (isOpenFileDependent) {
        	openFileDependendComponents.add(button);
        }
    }

	private void openFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(createFileFilter());
		int retval = fileChooser.showOpenDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			currentFile = fileChooser.getSelectedFile();

			// create new scrollpane content
			pdfPanelsContainer = new JPanel();
			pdfPanelsContainer.setLayout(new GridBagLayout());

			int pdfPanelCount = 1; // more to come
			for (int i = 0; i < pdfPanelCount; i++) {
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = i;
				constraints.anchor = GridBagConstraints.CENTER;
				constraints.insets = new Insets(2, 0, 2, 0);
				if (i == 0) {
					pdfPanelsContainer.add(getDefaultPdfPanel(), constraints);
				}
			}
			registerComponentsToModel();
			getScrollPanel().setViewportView(pdfPanelsContainer);
			uiHandler.reset();
			launchTask(currentFile, "Reading pdf...");
		}
	}

	private Component getDefaultPdfPanel() {
		if (defaultPdfPanel == null) {
			defaultPdfPanel = new PdfPanel(uiHandler);
		}
		return defaultPdfPanel;
	}

	private void launchTask(File file, String string) {
		new TaskPdfOpen(file, this).execute();
	}

	private FileFilter createFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file.isDirectory()) {
					return true;
				}
				return file.toString().toLowerCase().endsWith(".pdf");
			}

			@Override
			public String getDescription() {
				return null;
			}
		};
	}


	/**
	 * This method initializes scrollPanel
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getScrollPanel() {
		if (scrollPanel == null) {
			scrollPanel = new JScrollPane();
			scrollPanel.setPreferredSize(new Dimension(200, 300));
		}
		return scrollPanel;
	}

	private void debug(String string) {
		System.out.println("MainFrame: " + string);
	}

	private void handleException(String userFriendlyMessage, Throwable ex) {
		JOptionPane.showMessageDialog(this,userFriendlyMessage
								+ "\n\n\nTechnical details:\n--------------------------------\n"
								+ ex);
		ex.printStackTrace();
	}

	private void message(String string) {
		JOptionPane.showMessageDialog(this, string);
	}

	

	/**
	 * This method initializes toolBar	
	 * 	
	 * @return javax.swing.JToolBar	
	 */
	private JToolBar getToolBar() {
		if (toolBar == null) {
			toolBar = new JToolBar();
			toolBar.add(getJButton());
			toolBar.add(getButtonDraw());
			toolBar.add(getButtonSelect());
			toolBar.add(getButtonDeleteRect());
			toolBar.add(getButtonDelAll());
		}
		return toolBar;
	}

	/**
	 * This method initializes buttonDraw	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getButtonDraw() {
		if (buttonDraw == null) {
			buttonDraw = new JToggleButton("Draw", true); //selected initially			
			setButton(buttonDraw, "/draw.png", "Draw an area for cropping.", true);
			setToggleButtonGroup(buttonDraw, rectButtonGroup);
			buttonDraw.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					uiHandler.setEditingMode(UIHandler.EDIT_MODE_DRAW);
				}
			});
		}
		return buttonDraw;
	}

	/**
	 * This method initializes buttonSelect	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JToggleButton getButtonSelect() {
		if (buttonSelect == null) {
			buttonSelect = new JToggleButton("Select");
			setButton(buttonSelect, "/select.png", "Select and resize an already created crop area.", true);
			setToggleButtonGroup(buttonSelect, rectButtonGroup);
			buttonSelect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					uiHandler.setEditingMode(UIHandler.EDIT_MODE_SELECT);
				}
			});
		}
		return buttonSelect;
	}
	
	/**
	 * This method initializes buttonDeleteRect	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getButtonDeleteRect() {
		if (buttonDeleteRect == null) {
			buttonDeleteRect = new JButton("Del");
			setButton(buttonDeleteRect, "/del.png", "Delete selected crop area", true);
			buttonDeleteRect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.deleteSelected();
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "Select a rectangle first using 'select' tool");
					}
				}
			});
		}
		return buttonDeleteRect;
	}
	
	/**
	 * This method initializes buttonDelAll	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getButtonDelAll() {
		if (buttonDelAll == null) {
			buttonDelAll = new JButton("DelAll");
			setButton(buttonDelAll, "/delAll.png", "Delete all crop areas.", true);
			buttonDelAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {					
					if (JOptionPane.showConfirmDialog(MainFrame.this, "Delete " + uiHandler.getRectCount() + " crop areas?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
						uiHandler.deleteAll();
					}
				}
			});
		}
		return buttonDelAll;
	}
	
	/**
	 * Ensures other buttons in the group will be unselected when given button is selected.
	 * @param button
	 * @param group
	 */
	private void setToggleButtonGroup(final JToggleButton button, final ButtonGroup group) {
		group.add(button);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean otherButtonMode = ! button.isSelected();
				Enumeration<AbstractButton> otherButtons = group.getElements();
				while(otherButtons.hasMoreElements()) {
					otherButtons.nextElement().setSelected(otherButtonMode);
				}
				
			}
		});
	}
	
	
	@Override
	public void newPdfLoaded() {
		debug("listening to new pdf loaded.");
		getScrollPanel().revalidate();
		updateOpenFileDependents();
	}

	@Override
	public void pdfLoadFailed(File failedFile, Throwable cause) {
		handleException("Failed to load pdf file.", cause);
	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		Rectangle oldView = scrollPanel.getViewport().getViewRect();
		Point newViewPos = new Point();
		newViewPos.x = Math.max(0, (int) ((oldView.x + oldView.width / 2)
				* newZoomFactor - oldView.width / 2));
		newViewPos.y = Math.max(0, (int) ((oldView.y + oldView.height / 2)
				* newZoomFactor - oldView.height / 2));
		scrollPanel.getViewport().setViewPosition(newViewPos);
		scrollPanel.revalidate();
	}



	



	
}


