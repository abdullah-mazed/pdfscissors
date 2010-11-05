/**
 * 
 */
package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.PdfCropper;
import bd.amazed.pdfscissors.model.TaskPdfOpen;
import bd.amazed.pdfscissors.model.TaskPdfSave;

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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
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

import org.jpedal.PdfDecoder;
import org.jpedal.exception.PdfException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.qrcode.Mode;

import javax.swing.JScrollPane;
import java.util.Vector;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.JComboBox;

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
	private JButton buttonSave = null;
	private JPanel bottomPanel;
	private JComboBox pageSelectionCombo = null;

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
		this.rectButtonGroup = new ButtonGroup();
		this.uiHandler = new UIHandler();
		this.setContentPane(getJContentPane());
		this.setTitle("PDF Scissors");
		this.setSize(new Dimension(800,600));
		this.setMinimumSize(new Dimension(200,200));
		Dimension screen = getToolkit().getScreenSize();
		this.setBounds( (screen.width-getWidth())/2, (screen.height-getHeight())/2, getWidth(), getHeight() );
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		

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
				
				if (component instanceof UIHandlerListener) {
					uiHandler.addListener((UIHandlerListener) component);
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
			jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
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

			
			getScrollPanel().setViewportView(pdfPanelsContainer);
			
			uiHandler.reset();
			uiHandler.removeAllListeners();
			registerComponentsToModel();
			uiHandler.addListener(new UIHandlerLisnterForFrame());
			
			launchOpenTask(currentFile, "Reading pdf...");
		}
	}

	private PdfPanel getDefaultPdfPanel() {
		if (defaultPdfPanel == null) {
			defaultPdfPanel = new PdfPanel(uiHandler);
		}
		return defaultPdfPanel;
	}

	private void launchOpenTask(File file, String string) {
		final TaskPdfOpen task = new TaskPdfOpen(file, this);
		final StackViewCreationDialog stackViewCreationDialog = new StackViewCreationDialog(this);
		stackViewCreationDialog.setModal(true);
		stackViewCreationDialog.enableProgress(task, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//what happens on cancel					
				task.cancel();
				stackViewCreationDialog.dispose();
				
			}
		});
		//what happens on ok
		task.execute();		
		
		stackViewCreationDialog.setVisible(true);
		
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
				return "*.pdf";
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
			toolBar.add(getButtonSave());
			toolBar.add(getButtonDraw());
			toolBar.add(getButtonSelect());
			toolBar.add(getButtonDeleteRect());
			toolBar.add(getButtonDelAll());
			toolBar.setFloatable(false);
		}
		return toolBar;
	}
	
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();
			
			JButton backButton = new JButton("<");
			openFileDependendComponents.add(backButton);
	        backButton.setToolTipText("Back One page");
	        bottomPanel.add(backButton);
	        backButton.addActionListener(new PageChangeHandler(false));


	        JButton forwardButton = new JButton(">");
	        openFileDependendComponents.add(forwardButton);
	        forwardButton.setToolTipText("Forward One page");
	        bottomPanel.add(getPageSelectionCombo(), null);
	        bottomPanel.add(forwardButton);
	        forwardButton.addActionListener(new PageChangeHandler(true));
		}
		return bottomPanel;
	}


	/**
	 * This method initializes buttonSave	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getButtonSave() {
		if (buttonSave == null) {
			buttonSave = new JButton("Save");
			setButton(buttonSave, "/crop.png", "Crop and save to another PDF", true);
			buttonSave.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveFile();
				}
			});
		}
		return buttonSave;
	}
	
	private void saveFile() {
		if (uiHandler.getRectCount() == 0) {
			JOptionPane.showMessageDialog(this, "You have not defined any croping area. Set some area first");
			return;
		}
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(createFileFilter());
		File originalPdf = Model.getInstance().getCurrentFile();
		// find file name without extension
		String filePath = originalPdf.getAbsolutePath();
		int dot = filePath.lastIndexOf('.');
		int separator = filePath.lastIndexOf(File.separator);
		filePath = filePath.substring(0, separator + 1) + filePath.substring(separator + 1, dot) + "_scissored.pdf";
		fileChooser.setSelectedFile(new File(filePath));
		int retval = fileChooser.showOpenDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File targetFile = fileChooser.getSelectedFile();
			if(targetFile.equals(originalPdf)) {
				if (0 != JOptionPane.showConfirmDialog(this, "You are trying to overwrite the original pdf file.\nYou will permanently loose your original pdf format.\n\nSure to overwrite?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			} else if (targetFile.exists()) {
				//confirm overwrite
				if (0 != JOptionPane.showConfirmDialog(this, targetFile.getName() + " already exists, overwrite?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			}
			launchSaveTask(originalPdf, targetFile);
		}	
	}
	
	private void launchSaveTask(File originalPdf, File targetFile) {
		ArrayList<Rectangle> newRects = uiHandler.getAllRectangles();
		new TaskPdfSave(originalPdf, targetFile,newRects, defaultPdfPanel.getWidth(), defaultPdfPanel.getHeight(), this).execute();		
		
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
		//update combo page list		
		int pageCount = getDefaultPdfPanel().getPageCount();
		JComboBox combo = getPageSelectionCombo();
		combo.removeAllItems();
		combo.addItem("All pages together");
		for (int i = 0; i < pageCount; i++) {
			combo.addItem(String.valueOf(i + 1));
		}
		
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

	

	/**
	 * This method initializes pageSelectionCombo	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getPageSelectionCombo() {
		if (pageSelectionCombo == null) {
			pageSelectionCombo = new JComboBox();
			openFileDependendComponents.add(pageSelectionCombo);
			pageSelectionCombo.setEditable(true);
			
			//to align text to center
			DefaultListCellRenderer comboCellRenderer = new DefaultListCellRenderer();
			comboCellRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			pageSelectionCombo.setRenderer(comboCellRenderer);
			
			pageSelectionCombo.addItemListener(new ItemListener() {
				
				@Override
				public void itemStateChanged(ItemEvent e) {
			        String pageIndex = (String)pageSelectionCombo.getSelectedItem();
			        if (Character.isDigit(pageIndex.charAt(0))) { //page number
			        	uiHandler.setMergeMode(false);
			        	int pageNumber = pageSelectionCombo.getSelectedIndex();
			        	uiHandler.setPage(pageNumber); // we dont have to add +1, cause first time is all page
			        	try {
							defaultPdfPanel.decodePage(pageNumber);
						} catch (Exception ex) {
							handleException("Failed to decode page " + pageNumber , ex);
						}
			        	defaultPdfPanel.invalidate();
			        	defaultPdfPanel.repaint();
			        } else {
			        	uiHandler.setMergeMode(true);
			        	defaultPdfPanel.repaint();
			        }
				}
			});
				
		}
		return pageSelectionCombo;
	}

   
	
	class PageChangeHandler implements ActionListener {

        private boolean forward;

        PageChangeHandler(boolean forward) {
            this.forward = forward;
        }

        public void actionPerformed(ActionEvent e) {
        	int currentIndex = getPageSelectionCombo().getSelectedIndex();
            if(forward && currentIndex < getPageSelectionCombo().getItemCount() - 1) {
            	currentIndex++;
            	getPageSelectionCombo().setSelectedIndex(currentIndex);
            } else if ( !forward && currentIndex > 0) {
            	currentIndex --;
            	getPageSelectionCombo().setSelectedIndex(currentIndex);
            }
        }

    } // inner class BackButtonListener
	
	class UIHandlerLisnterForFrame implements UIHandlerListener {

		@Override
		public void editingModeChanged(int newMode) {
			debug("Editing mode : " + newMode);
			AbstractButton selectedButton = null;
			if (newMode == UIHandler.EDIT_MODE_DRAW) {
				selectedButton = getButtonDraw();				
			} else if (newMode == UIHandler.EDIT_MODE_SELECT) {
				selectedButton = getButtonSelect();
			}
			selectedButton.setSelected(true);
			Enumeration<AbstractButton> otherButtons = rectButtonGroup.getElements();
			while(otherButtons.hasMoreElements()) {
				AbstractButton otherButton = otherButtons.nextElement();
				if (selectedButton != otherButton) {
					otherButton.setSelected(false);
				}
			}
		}

		@Override
		public void pageChanged(int index) {
			
		}
		
	}
	
}


