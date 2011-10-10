/**
 * 
 */
package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.model.PageRectsMap;
import bd.amazed.pdfscissors.model.PdfCropper;
import bd.amazed.pdfscissors.model.PdfFile;
import bd.amazed.pdfscissors.model.TaskPdfOpen;
import bd.amazed.pdfscissors.model.TaskPdfSave;
import bd.amazed.pdfscissors.model.TempFileManager;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.KeyEvent;
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
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

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
	private ButtonGroup rectButtonGroup = null; // @jve:decl-index=0:
	/** Contains all components that are disabled until file open. */
	private Vector<Component> openFileDependendComponents = null;
	/**
	 * Keeps track of already registered listeners. Used to re-register listners.
	 */
	private Vector<ModelListener> modelRegisteredListeners;
	private JToolBar toolBar = null;
	private UIHandler uiHandler = null;
	private JToggleButton buttonDraw = null;
	private JToggleButton buttonSelect = null;
	private JButton buttonDeleteRect = null;
	private JButton buttonDelAll = null;
	private JButton buttonSave = null;
	private JButton buttonSplitHorizontal = null;
	private JButton buttonSplitVertical = null;
	private JPanel bottomPanel;
	private JComboBox pageSelectionCombo = null;
	private JMenuBar jJMenuBar = null;
	private JMenu menuFile = null;
	private JMenuItem menuFileOpen = null;
	private JMenuItem menuSave = null;
	private JMenu menuEdit = null;
	private JMenuItem menuCopy = null;
	private JMenuItem menuCut = null;
	private JMenuItem menuPaste = null;
	private JMenuItem menuDelete = null;
	private JButton buttonEqualWidth = null;
	private JButton buttonEqualHeight = null;
	private JMenu menuHelp = null;
	private JMenuItem menuAbout = null;
	private JScrollPane pageGroupScrollPanel = null;
	private JList pageGroupList = null;
	private JButton forwardButton = null;
	private JButton backButton = null;

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
			if (url != null) {
				setIconImage(ImageIO.read(url));
			}
		} catch (IOException e) {
			System.err.println("Failed to get frame icon");
		}

		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
		} catch (Throwable e) {
			System.err.println("Failed to set exit on close." + e);
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				Model.getInstance().close();
				getDefaultPdfPanel().closePdfFile(); // TODO may be implement a better way to notify to close
				TempFileManager.getInstance().clean();
			}
		});
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
		this.setJMenuBar(getJJMenuBar());
		this.setTitle("PDF Scissors");
		this.setSize(new Dimension(800, 600));
		this.setMinimumSize(new Dimension(200, 200));
		Dimension screen = getToolkit().getScreenSize();
		this.setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
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
		boolean shouldEnable = Model.getInstance().getPdf().getNormalizedFile() != null;
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
			jContentPane.add(getPageGroupPanel(), BorderLayout.WEST);
		}
		return jContentPane;
	}

	private JScrollPane getPageGroupPanel() {
		if (pageGroupScrollPanel == null) {
			JList list = getPageGroupList();
			pageGroupScrollPanel = new JScrollPane(list);
			openFileDependendComponents.add(pageGroupScrollPanel);
		}
		return pageGroupScrollPanel;
	}
	
	private JList getPageGroupList() {
		if (pageGroupList == null) {
			pageGroupList = new JList();
			pageGroupList.setMinimumSize(new Dimension(200,100));
			openFileDependendComponents.add(pageGroupList);
			pageGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			pageGroupList.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					int selectedIndex = pageGroupList.getSelectedIndex();
					if (selectedIndex >= 0) {
						PageGroup currentGroup = Model.getInstance().getPageGroups().elementAt(selectedIndex);
						uiHandler.setPageGroup(currentGroup);
					}
				}
			});
		}
		return pageGroupList;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton("Open"); // a string literal is here only for eclipse visual editor.
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
		if (imageURL != null) { // image found
			button.setIcon(new ImageIcon(imageURL, tooltip));
			button.setText(null);
		} else { // no image found
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
		String lastFile = Model.getInstance().getProperties().getProperty(Model.PROPERTY_LAST_FILE);
		if (lastFile != null) {
			System.out.println("Last opened directory rememberred: " + lastFile);
			fileChooser.setCurrentDirectory(new File(lastFile));
		}

		int retval = fileChooser.showOpenDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			currentFile = fileChooser.getSelectedFile();
			Model.getInstance().getProperties().setProperty(Model.PROPERTY_LAST_FILE, fileChooser.getCurrentDirectory().getAbsolutePath());

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
			
			launchOpenTask(currentFile, PageGroup.GROUP_TYPE_ODD_EVEN, "Reading pdf...");
		}
	}

	private PdfPanel getDefaultPdfPanel() {
		if (defaultPdfPanel == null) {
			defaultPdfPanel = new PdfPanel(uiHandler);
		}
		return defaultPdfPanel;
	}

	private void launchOpenTask(File file, int groupType, String string) {
		final TaskPdfOpen task = new TaskPdfOpen(file, groupType, this);
		final StackViewCreationDialog stackViewCreationDialog = new StackViewCreationDialog(this);
		stackViewCreationDialog.setModal(true);
		stackViewCreationDialog.enableProgress(task, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// what happens on cancel
				task.cancel();
				stackViewCreationDialog.dispose();

			}
		});
		// what happens on ok
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
		JOptionPane.showMessageDialog(this, "Oops! " + userFriendlyMessage + "\n\n\nTechnical details:\n--------------------------------\n" + ex.getMessage());
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
			toolBar.add(getButtonEqualWidth());
			toolBar.add(getButtonEqualHeight());
			toolBar.add(getButtonSplitHorizontal());
			toolBar.add(getButtonSplitVertical());
		}
		return toolBar;
	}

	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel();

			backButton = new JButton("<");
			openFileDependendComponents.add(backButton);
			backButton.setToolTipText("Back One page");
			bottomPanel.add(backButton);
			backButton.addActionListener(new PageChangeHandler(false));

			forwardButton = new JButton(">");
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
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(createFileFilter());
		File originalPdf = Model.getInstance().getPdf().getOriginalFile();
		// find file name without extension
		String filePath = originalPdf.getAbsolutePath();
		int dot = filePath.lastIndexOf('.');
		int separator = filePath.lastIndexOf(File.separator);
		filePath = filePath.substring(0, separator + 1) + filePath.substring(separator + 1, dot) + "_scissored.pdf";
		fileChooser.setSelectedFile(new File(filePath));
		int retval = fileChooser.showSaveDialog(this);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File targetFile = fileChooser.getSelectedFile();
			if (targetFile.equals(originalPdf)) {
				if (0 != JOptionPane.showConfirmDialog(this, "You are trying to overwrite the original pdf file.\nYou will permanently loose your original pdf format.\n\nSure to overwrite?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			} else if (targetFile.exists()) {
				// confirm overwrite
				if (0 != JOptionPane.showConfirmDialog(this, targetFile.getName() + " already exists, overwrite?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION)) {
					return; // overwrite not allowed by user
				}
			}
			launchSaveTask(Model.getInstance().getPdf(), targetFile);
		}
	}

	private void launchSaveTask(PdfFile pdfFile, File targetFile) {
		PageRectsMap pageRectsMap = Model.getInstance().getPageRectsMap();
		new TaskPdfSave(pdfFile, targetFile, pageRectsMap , defaultPdfPanel.getWidth(), defaultPdfPanel.getHeight(), this).execute();

	}

	/**
	 * This method initializes buttonDraw
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getButtonDraw() {
		if (buttonDraw == null) {
			buttonDraw = new JToggleButton("Draw", true); // selected initially
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
			buttonDeleteRect = new JButton("Delete");
			setButton(buttonDeleteRect, "/del.png", "Delete selected crop area", true);
			buttonDeleteRect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.deleteSelected();
					} else {
						showDialogNoRectYet();
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
			buttonDelAll = new JButton("Delete All");
			setButton(buttonDelAll, "/delAll.png", "Delete all crop areas.", true);
			buttonDelAll.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() <= 0) {
						showDialogNoRectYet();
					} else if (JOptionPane.showConfirmDialog(MainFrame.this, "Delete " + uiHandler.getRectCount() + " crop area" + (uiHandler.getRectCount() > 1 ? "s" : "") // singular/plural
							+ "?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
						uiHandler.deleteAll();
					}
				}
			});
		}
		return buttonDelAll;
	}

	/**
	 * This method initializes buttonEqualWidth
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getButtonEqualWidth() {
		if (buttonEqualWidth == null) {
			buttonEqualWidth = new JButton("Equal Width");
			setButton(buttonEqualWidth, "/sameWidth.png", "Set width of all areas same.", true);
			buttonEqualWidth.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() > 0) {
						uiHandler.equalizeWidthOfSelected(defaultPdfPanel.getWidth());
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonEqualWidth;
	}

	/**
	 * This method initializes buttonEqualHeight
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getButtonEqualHeight() {
		if (buttonEqualHeight == null) {
			buttonEqualHeight = new JButton("Equal Height");
			setButton(buttonEqualHeight, "/sameHeight.png", "Set Heights of crop areas same", true);
			buttonEqualHeight.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getRectCount() > 0) {
						uiHandler.equalizeHeightOfSelected(defaultPdfPanel.getHeight());
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonEqualHeight;
	}

	private JButton getButtonSplitHorizontal() {
		if (buttonSplitHorizontal == null) {
			buttonSplitHorizontal = new JButton("Split Horizontal");
			setButton(buttonSplitHorizontal, "/splitHorizontal.png", "Split area in two equals horizontal areas", true);
			buttonSplitHorizontal.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.splitHorizontalSelected(defaultPdfPanel);
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonSplitHorizontal;
	}

	private JButton getButtonSplitVertical() {
		if (buttonSplitVertical == null) {
			buttonSplitVertical = new JButton("Split Vertical");
			setButton(buttonSplitVertical, "/splitVertical.png", "Split area in two equals vertical areas", true);
			buttonSplitVertical.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (uiHandler.getSelectedRect() != null) {
						uiHandler.splitVerticalSelected(defaultPdfPanel);
					} else {
						showDialogNoRectYet();
					}
				}
			});
		}
		return buttonSplitVertical;
	}

	/**
	 * Ensures other buttons in the group will be unselected when given button is selected.
	 * 
	 * @param button
	 * @param group
	 */
	private void setToggleButtonGroup(final JToggleButton button, final ButtonGroup group) {
		group.add(button);
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean otherButtonMode = !button.isSelected();
				Enumeration<AbstractButton> otherButtons = group.getElements();
				while (otherButtons.hasMoreElements()) {
					otherButtons.nextElement().setSelected(otherButtonMode);
				}

			}
		});
	}

	@Override
	public void newPdfLoaded() {
		debug("listening to new pdf loaded.");
		updateOpenFileDependents();
	}

	@Override
	public void pdfLoadFailed(File failedFile, Throwable cause) {
		handleException("Failed to load pdf file.", cause);
	}
	
	@Override
	public void pageGroupChanged(Vector<PageGroup> pageGroups) {
		JList list = getPageGroupList();
		list.removeAll();
		DefaultListModel listModel = new DefaultListModel();
		for (int i = 0; i < pageGroups.size(); i++) {
			listModel.add(i, pageGroups.elementAt(i));
		}
		list.setModel(listModel); 
		list.setSelectedIndex(0);
		getPageGroupPanel().getViewport().removeAll();
		getPageGroupPanel().getViewport().add(list);
		
	}

	@Override
	public void zoomChanged(double oldZoomFactor, double newZoomFactor) {
		Rectangle oldView = scrollPanel.getViewport().getViewRect();
		Point newViewPos = new Point();
		newViewPos.x = Math.max(0, (int) ((oldView.x + oldView.width / 2) * newZoomFactor - oldView.width / 2));
		newViewPos.y = Math.max(0, (int) ((oldView.y + oldView.height / 2) * newZoomFactor - oldView.height / 2));
		scrollPanel.getViewport().setViewPosition(newViewPos);
		scrollPanel.revalidate();
	}

	@Override
	public void clipboardCopy(boolean isCut, Rect onClipboard) {
		getMenuPaste().setEnabled(true);
	}

	@Override
	public void clipboardPaste(boolean isCut, Rect onClipboard) {
		if (isCut) {
			getMenuPaste().setEnabled(false);
		}
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

			// to align text to center
			DefaultListCellRenderer comboCellRenderer = new DefaultListCellRenderer();
			comboCellRenderer.setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			pageSelectionCombo.setRenderer(comboCellRenderer);

			pageSelectionCombo.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					String pageIndex = (String) pageSelectionCombo.getSelectedItem();
					if (pageIndex != null && pageIndex.length() > 0 && Character.isDigit(pageIndex.charAt(0))) { // page
						// number
						uiHandler.setMergeMode(false);
						int pageNumber = Integer.valueOf(pageIndex);
						uiHandler.setPage(pageNumber); // we dont have to add +1, cause first time is all page
						try {
							defaultPdfPanel.decodePage(pageNumber);
						} catch (Exception ex) {
							handleException("Failed to decode page " + pageNumber, ex);
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
			if (forward && currentIndex < getPageSelectionCombo().getItemCount() - 1) {
				currentIndex++;
				getPageSelectionCombo().setSelectedIndex(currentIndex);
			} else if (!forward && currentIndex > 0) {
				currentIndex--;
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
			while (otherButtons.hasMoreElements()) {
				AbstractButton otherButton = otherButtons.nextElement();
				if (selectedButton != otherButton) {
					otherButton.setSelected(false);
				}
			}
		}

		@Override
		public void pageChanged(int index) {

		}
		
		@Override
		public void pageGroupSelected(PageGroup pageGroup) {
			// update combo page list
			int pageCount = pageGroup.getPageCount();
			JComboBox combo = getPageSelectionCombo();
			combo.removeAllItems();
			if (pageCount > 1) {
				combo.addItem("All pages stacked");
			}
			for (int i = 0; i < pageCount; i++) {
				combo.addItem(String.valueOf(pageGroup.getPageNumberAt(i)));
			}
			combo.setSelectedIndex(0);
			if (pageCount <= 1) {
				forwardButton.setVisible(false);
				forwardButton.setEnabled(false);
				backButton.setVisible(false);
				backButton.setEnabled(false);
			} else {
				forwardButton.setVisible(true);
				forwardButton.setEnabled(true);
				backButton.setVisible(true);
				backButton.setEnabled(true);
			}
			getScrollPanel().revalidate();
			
			if(uiHandler.getRectCount() > 0) {
				uiHandler.setEditingMode(UIHandler.EDIT_MODE_SELECT);
			} else {
				uiHandler.setEditingMode(uiHandler.EDIT_MODE_DRAW);
			}
		}

	}

	/**
	 * This method initializes jJMenuBar
	 * 
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getJJMenuBar() {
		if (jJMenuBar == null) {
			jJMenuBar = new JMenuBar();
			jJMenuBar.add(getMenuFile());
			jJMenuBar.add(getMenuEdit());
			jJMenuBar.add(getMenuHelp());
		}
		return jJMenuBar;
	}

	/**
	 * This method initializes menuFile
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuFile() {
		if (menuFile == null) {
			menuFile = new JMenu("File");
			menuFile.setMnemonic(KeyEvent.VK_F);
			menuFile.add(getMenuFileOpen());
			menuFile.add(getMenuSave());
		}
		return menuFile;
	}

	/**
	 * This method initializes menuFileOpen
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuFileOpen() {
		if (menuFileOpen == null) {
			menuFileOpen = new JMenuItem("Open", KeyEvent.VK_O);
			menuFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			menuFileOpen.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					openFile();
				}
			});
		}
		return menuFileOpen;
	}

	/**
	 * This method initializes menuSave
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuSave() {
		if (menuSave == null) {
			menuSave = new JMenuItem("Crop & Save", KeyEvent.VK_S);
			menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			menuSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveFile();
				}
			});
		}
		return menuSave;
	}

	/**
	 * This method initializes menuEdit
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuEdit() {
		if (menuEdit == null) {
			menuEdit = new JMenu("Edit");
			menuEdit.setMnemonic(KeyEvent.VK_E);
			menuEdit.add(getMenuCopy());
			menuEdit.add(getMenuCut());
			menuEdit.add(getMenuPaste());
			menuEdit.add(getMenuDelete());
		}
		return menuEdit;
	}

	/**
	 * This method initializes menuCopy
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuCopy() {
		if (menuCopy == null) {
			menuCopy = new JMenuItem("Copy", KeyEvent.VK_C);
			menuCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
			menuCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().copyToClipboard(false, uiHandler.getSelectedRect());
				}
			});
			openFileDependendComponents.add(menuCopy);
		}
		return menuCopy;
	}

	/**
	 * This method initializes menuCut
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuCut() {
		if (menuCut == null) {
			menuCut = new JMenuItem("Cut", KeyEvent.VK_X);
			menuCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
			menuCut.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().copyToClipboard(true, uiHandler.getSelectedRect());
				}
			});
			openFileDependendComponents.add(menuCut);

		}
		return menuCut;
	}

	/**
	 * This method initializes menuPaste
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuPaste() {
		if (menuPaste == null) {
			menuPaste = new JMenuItem("Paste", KeyEvent.VK_V);
			menuPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
			if (Model.getInstance().getClipboardRect() == null) {
				menuPaste.setEnabled(false);
			}
			menuPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Model.getInstance().pasteFromClipboard();
				}
			});
			openFileDependendComponents.add(menuPaste);
		}
		return menuPaste;
	}

	/**
	 * This method initializes menuDelete
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuDelete() {
		if (menuDelete == null) {
			menuDelete = new JMenuItem("Delete", KeyEvent.VK_D);
			menuDelete.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
			if (Model.getInstance().getClipboardRect() == null) {
				menuPaste.setEnabled(false);
			}
			menuDelete.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					uiHandler.deleteSelected();
				}
			});
			openFileDependendComponents.add(menuDelete);
		}
		return menuDelete;
	}

	private void showDialogNoRectYet() {
		JOptionPane.showMessageDialog(MainFrame.this, "Select a rectangle first using 'select' tool");
	}

	/**
	 * This method initializes menuHelp
	 * 
	 * @return javax.swing.JMenu
	 */
	private JMenu getMenuHelp() {
		if (menuHelp == null) {
			menuHelp = new JMenu("Help");
			menuHelp.setMnemonic(KeyEvent.VK_H);
			menuHelp.add(getMenuAbout());
		}
		return menuHelp;
	}

	/**
	 * This method initializes menuAbout
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuAbout() {
		if (menuAbout == null) {
			menuAbout = new JMenuItem("About", KeyEvent.VK_A);
			menuAbout.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AboutView(MainFrame.this).setVisible(true);
				}
			});
		}
		return menuAbout;
	}

}
