/**
 * 
 */
package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import bd.amazed.pdfscissors.model.CropRect;
import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.ModelListener;
import bd.amazed.pdfscissors.model.PageGroup;
import bd.amazed.pdfscissors.model.PageRectsMap;
import bd.amazed.pdfscissors.model.PdfFile;
import bd.amazed.pdfscissors.model.RectChangeListener;
import bd.amazed.pdfscissors.model.TaskPdfOpen;
import bd.amazed.pdfscissors.model.TaskPdfSave;
import bd.amazed.pdfscissors.model.TempFileManager;

/**
 * @author Gagan
 * 
 */
public class MainFrame extends JFrame implements ModelListener {

	private PdfPanel defaultPdfPanel;
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JButton jButton = null;
	private JScrollPane scrollPanel = null;
	/** Panel containing PdfPanels. */
	private JPanel pdfPanelsContainer = null;
	private ButtonGroup rectButtonGroup = null; // @jve:decl-index=0:
	
	//MOD russa: horizontal SplitPane for pageGroupScrollPanel (left) and scrollPanel (right)
	private JSplitPane spPdfPanel; 
	
	/** Contains all components that are disabled until file open. */
	private Vector<Component> openFileDependendComponents = null;
	

	/** MOD russa: Contains all components that are disabled if no (cropping) rectangle is selected. */
	private Vector<Component> selectedRectDependendComponents = null;
	
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
	private JButton buttonSaveCurrent = null;//MOD russa
	private JButton buttonOpenExtended = null;//MOD russa
	private JButton buttonSplitHorizontal = null;
	private JButton buttonSplitVertical = null;
	private JPanel bottomPanel;
	private JComboBox pageSelectionCombo = null;
	private JMenuBar jJMenuBar = null;
	private JMenu menuFile = null;
	private JMenuItem menuFileOpen = null;
	private JMenuItem menuFileOpenExtended = null;//MOD russa
	private JMenuItem menuSave = null;
	private JMenuItem menuSaveCurrent = null;//MOD russa
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
	private PageGroupRenderer pageGroupListRenderer;
	private JButton forwardButton = null;
	private JButton backButton = null;
	
	private JFormattedTextField tfEditX = null;
	private JFormattedTextField tfEditY = null;
	private JFormattedTextField tfEditWidth = null;
	private JFormattedTextField tfEditHeight = null;
	

	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		modelRegisteredListeners = new Vector<ModelListener>();
		openFileDependendComponents = new Vector<Component>();
		selectedRectDependendComponents = new Vector<Component>();//MOD russa
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

				storeWindowPosition();//rusaa MOD: store for next execution
				
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
//		Dimension screen = getToolkit().getScreenSize();
//		this.setBounds((screen.width - getWidth()) / 2, (screen.height - getHeight()) / 2, getWidth(), getHeight());
//		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		restoreWindowPosition();//rusaa MOD: restore from previous execution
	}
	
	//MOD russa: store window position
	private void storeWindowPosition(){
		
		Properties prop = Model.getInstance().getProperties();
		
		prop.setProperty("x", Integer.toString(this.getX()) );
		prop.setProperty("y", Integer.toString(this.getY()) );
		prop.setProperty("w", Integer.toString(this.getWidth()) );
		prop.setProperty("h", Integer.toString(this.getHeight()) );
		prop.setProperty("ext", Integer.toString(this.getExtendedState() ));
		
	}
	//MOD russa: HELPER for retrieving INT values from properties
	private int doGetProperty(String name, int defaultValue, Properties properties){
		
		if(!properties.containsKey(name))
			return defaultValue;
		
		String str = properties.getProperty(name);
		
		int value;
		try {
			value = Integer.valueOf(str, 10);	
		} catch (NumberFormatException e){
			value = defaultValue;
		}
		
		return value;
	}
	//MOD russa: restore window position
	private void restoreWindowPosition(){
		
		Properties prop = Model.getInstance().getProperties();

		Dimension screen = getToolkit().getScreenSize();
		int defX = (screen.width - getWidth()) / 2;
		int defY = (screen.height - getHeight()) / 2;
		int defW = getWidth();
		int defH = getHeight();
		int defState = JFrame.MAXIMIZED_BOTH;
		
		int x,y;
		x = doGetProperty("x", defX, prop);
		y = doGetProperty("y", defY, prop);
		
		int w,h;
		w = doGetProperty("w", defW, prop);
		h = doGetProperty("h", defH, prop);
		
		int ext = doGetProperty("ext", defState, prop);
		
		
		if(w < 200 || w >= screen.width){
			w = defW;
		}
		
		if(h < 200 || h >= screen.height){
			h = defH;
		}
		
		if(ext < 0){
			ext = defState;
		}
		
		this.setLocation(x, y);
		this.setSize(w, h);
		this.setExtendedState(ext);
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
	 * MOD russa: Enable/disable buttons etc which should be disabled when there is no (cropping) rectangle selected.
	 */
	private void updateRectSelectedDependents(boolean isEnable) {
		for (Component component : selectedRectDependendComponents) {
			if(component.isEnabled() != isEnable)
				component.setEnabled(isEnable);
		}
	}
	
	/**
	 * MOD russa: Enable/disable buttons etc which should be disabled when there is no (cropping) rectangle selected.
	 */
	private void updateRectEditor(Rect rect) {
		int x,y,w,h;
		if(rect == null){
			x = y = w = h = 0;
		}
		else {
			Rectangle b = rect.getRectangleBound();
			x = b.x;
			y = b.y;
			w = b.width;
			h = b.height;
		}
		
		tfEditX.setValue(x);
		tfEditY.setValue(y);
		tfEditWidth.setValue(w);
		tfEditHeight.setValue(h);
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
//			jContentPane.add(getScrollPanel(), BorderLayout.CENTER);
			jContentPane.add(getToolBar(), BorderLayout.NORTH);
			jContentPane.add(getBottomPanel(), BorderLayout.SOUTH);
//			jContentPane.add(getPageGroupPanel(), BorderLayout.WEST);
			
			spPdfPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getPageGroupPanel(), getScrollPanel());
			jContentPane.add(spPdfPanel, BorderLayout.CENTER);
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
			pageGroupList.setCellRenderer(getPageGroupListCellRenderer());
			pageGroupList.setBackground(this.getBackground());
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

	private PageGroupRenderer getPageGroupListCellRenderer() {
		if (this.pageGroupListRenderer == null) {
			this.pageGroupListRenderer = new PageGroupRenderer();
		}
		return this.pageGroupListRenderer;
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
					showFileOpenDialog(false);
				}

			});
		}
		return jButton;
	}
	
	/**
	 * MOD russa: additional open-file button (opens extended file dialog)
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getButtonOpenExtended() {
		if (buttonOpenExtended == null) {
			buttonOpenExtended = new JButton("Open (Extended)"); // a string literal is here only for eclipse visual editor.
			String imageFile = "/openExtended.png";
			String text = "Open a PDF file with extenden options";
			setButton(buttonOpenExtended, imageFile, text, false);
			buttonOpenExtended.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					showFileOpenDialog(true);
				}

			});
		}
		return buttonOpenExtended;
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

	public void showFileOpenDialog(boolean isExtendedOptions) {//MOD russa: added boolean argument
		OpenDialog openDialog = new OpenDialog();
		openDialog.seMainFrame(this);
		openDialog.setModal(true);
		openDialog.setLocationRelativeTo(this);//MOD russa
		
		if(isExtendedOptions){
			openDialog.setVisible(true);
		} else {
			openDialog.showFileChooserDialog(true, true);
		}
		
	}
	
	public void openFile(File file, int pageGroupType, boolean shouldCreateStackView) {

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
		
		launchOpenTask(file, pageGroupType, shouldCreateStackView, "Reading pdf...");
	}

	private PdfPanel getDefaultPdfPanel() {
		if (defaultPdfPanel == null) {
			defaultPdfPanel = new PdfPanel(uiHandler);
		}
		return defaultPdfPanel;
	}

	private void launchOpenTask(File file, int groupType, boolean shouldCreateStackView, String string) {
		final TaskPdfOpen task = new TaskPdfOpen(file, groupType, shouldCreateStackView, this);
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
		
		task.addPropertyChangeListener(new PropertyChangeListener() {
			private boolean isThumbnailStarted = false;
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
//				System.err.println(evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName() + ": "+ evt.getOldValue() +" -> "+evt.getNewValue());
				
				if ("done".equals(evt.getPropertyName())) {


					// update (the size for) the thumbnail list view
					// (but only after the thumbnail-task has started)
					if(isThumbnailStarted){
						invalidatePdfThumbnailView();
						refreshPdfThumbnailView();
					}
					
				} else if ("message".equals(evt.getPropertyName())) {
					
					invalidatePdfThumbnailView();
					
					//"message" is send by the thumbnail-task:
					//	remember, when it is started -> now we should update the size for the thumbnail list view
					if(!isThumbnailStarted){
						isThumbnailStarted = true;
						refreshPdfThumbnailView();
					}
				}
				
			}
		});
		
		// what happens on ok
		task.execute();
		
		stackViewCreationDialog.setLocationRelativeTo(this);

		stackViewCreationDialog.setVisible(true);

	}

	public FileFilter createFileFilter() {
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
	
	public void invalidatePdfThumbnailView(){//MOD russa
		//update thumbnail view when new images were loaded / created
		getPageGroupList().invalidate();
		getPageGroupList().repaint();
	}
	
	public void refreshPdfThumbnailView(){//MOD russa
		
		//update thumbnail view when new images were loaded / created
		getPageGroupList().invalidate();
		getPageGroupList().repaint();
		
		//adjust width, if necessary
//		Dimension imageDim = getPageGroupListCellRenderer().getPreferredSize();
//		Rectangle viewRect = getPageGroupPanel().getViewportBorderBounds();
		int adjustWidth = 2; //Math.max(0, imageDim.width - viewRect.width);
		
		boolean isScrollBar = getPageGroupPanel().getVerticalScrollBar().isVisible();
		int adjustForScrollBar = isScrollBar? getPageGroupPanel().getVerticalScrollBar().getSize().width : 0;
		
		
		Dimension d = getPageGroupListCellRenderer().getPreferredSize();
		d.width += adjustWidth + adjustForScrollBar;
		getPageGroupPanel().setPreferredSize(d);
		
		spPdfPanel.resetToPreferredSizes();
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
			toolBar.add(getButtonOpenExtended());//MOD russa
			toolBar.add(getButtonSave());
			toolBar.add(getButtonSaveCurrent());//MOD russa
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
			
			//MOD add controls for rect-editing
			
			JPanel pnlRectEditor = new JPanel();
			
			JLabel l = new JLabel();
			l.setIcon(new ImageIcon(MainFrame.class.getResource("/draw.png"), "edit current selection"));
			pnlRectEditor.add(l);
			
			NumberFormat format = NumberFormat.getNumberInstance();
			format.setParseIntegerOnly(true);
			format.setMinimumIntegerDigits(0);
			
			//change listener that transfers changes from input-fields to the currently selected (cropping) rectangle
			PropertyChangeListener changeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					
//					System.out.println(((JFormattedTextField)evt.getSource()).getName() + " \t" +evt.getOldValue() + " -> " + evt.getNewValue() + "\t"+(evt.getNewValue() != null?evt.getNewValue().getClass():"NULL"));
					
					if(evt.getNewValue() == null){
						return;
					}
					
					String name = ((JFormattedTextField)evt.getSource()).getName();
					
					try{
						
						int value = ((Number) evt.getNewValue()).intValue();
						Rect rect = uiHandler.getSelectedRect();
						if(rect != null){
							
							int targetValue = -1;
							Rectangle bounds = rect.getRectangleBound();
							
							if(name == "x"){
								targetValue = bounds.x;
							} else if(name == "y"){
								targetValue = bounds.y;
							} else if(name == "width"){
								targetValue = bounds.width;
							} else if(name == "height"){
								targetValue = bounds.height;
							}
							
							if(targetValue != value){
								
								if(name == "x"){
									bounds.x = value;
								} else if(name == "y"){
									bounds.y = value;
								} else if(name == "width"){
									bounds.width = value;
								} else if(name == "height"){
									bounds.height = value;
								}

								rect.setBounds(bounds);
								getScrollPanel().repaint();
							}
							
						}
						
					} catch(Exception e){
						e.printStackTrace();
					}
					
				}
			};
			
			tfEditX = createRectInputField("x", 'x', pnlRectEditor, format, changeListener);
			tfEditY = createRectInputField("y", 'y', pnlRectEditor, format, changeListener);
			tfEditWidth  = createRectInputField("width",  'w', pnlRectEditor, format, changeListener);
			tfEditHeight = createRectInputField("height", 'h', pnlRectEditor, format, changeListener);
						
			bottomPanel.add(pnlRectEditor);
			
//			JButton btn = new JButton("refresh");
//			btn.addActionListener(new ActionListener() {
//				
//				@Override
//				public void actionPerformed(ActionEvent paramActionEvent) {
//					invalidatePdfThumbnailView();
//					getPageGroupList().revalidate();
//					getJContentPane().invalidate();
//					getJContentPane().repaint();
//
////					System.out.println(getPageGroupList().getSize());
//					System.out.println(getPageGroupList().getPreferredSize());
////					System.out.println(getPageGroupPanel().getSize());
////					System.out.println(getPageGroupPanel().getPreferredSize());
//
//					System.out.println(getPageGroupPanel().getViewportBorderBounds());
//					System.out.println(getPageGroupPanel().getVisibleRect());
//					
//					System.out.println(getPageGroupListCellRenderer().getPreferredSize());
//					System.out.println(Model.getInstance().getPdf().getNormalizedPdfWidth());
//					
//
//					System.out.println(getPageGroupPanel().getVerticalScrollBar().getSize() +", "+getPageGroupPanel().getVerticalScrollBar().isVisible());
////					System.out.println(getPageGroupPanel().getVerticalScrollBar().getInsets());
////					System.out.println(getPageGroupPanel().getVerticalScrollBar().getBorder());
//					System.out.println(getPageGroupPanel().getHorizontalScrollBar().getSize() +", "+getPageGroupPanel().getHorizontalScrollBar().isVisible());
//					
//					//adjust width, if necessary
////					Dimension imageDim = getPageGroupListCellRenderer().getPreferredSize();
////					Rectangle viewRect = getPageGroupPanel().getViewportBorderBounds();
//					int adjustWidth = 2; //Math.max(0, imageDim.width - viewRect.width);
//					
//					boolean isScrollBar = getPageGroupPanel().getVerticalScrollBar().isVisible();
//					int adjustForScrollBar = isScrollBar? getPageGroupPanel().getVerticalScrollBar().getSize().width : 0;
//					
//					System.out.println("adjust " + adjustWidth +", sc "+adjustForScrollBar);
//					
//					
//					Dimension d = getPageGroupListCellRenderer().getPreferredSize();//getPageGroupPanel().getPreferredSize();//
//					d.width += adjustWidth + adjustForScrollBar;
////					getPageGroupPanel().invalidate();
//					getPageGroupPanel().setPreferredSize(d);
//					
//					sp.resetToPreferredSizes();
//					
//
//					System.out.println(getPageGroupPanel().getViewportBorderBounds());
//					System.out.println(getPageGroupPanel().getVisibleRect());
//					
//					boolean enable = false;
//					if(enable){
//						getPageGroupPanel().invalidate();
//						getPageGroupPanel().repaint();
//						System.out.println(getPageGroupPanel().getVerticalScrollBar().getSize());
////						PdfFile pdfFile = Model.getInstance().getPdf();
////						getPageGroupListCellRenderer().setPageSize(pdfFile.getNormalizedPdfWidth(), pdfFile.getNormalizedPdfHeight());
////						getPageGroupList().invalidate(); // to recalulate size etc
////						getPageGroupListCellRenderer().repaint();
////						getPageGroupList().repaint();
//					}
//					
//					System.out.println("------------------------------");
//
//				}
//			});
//			bottomPanel.add(btn);
			
		}
		return bottomPanel;
	}
	
	private JFormattedTextField createRectInputField(String name, char mnemonic, JPanel pnlRectEditor,
			NumberFormat format, PropertyChangeListener changeListener){
		
		JFormattedTextField f = new JFormattedTextField(format);
		f.setColumns(5);
		f.setName(name);
		f.addPropertyChangeListener("value", changeListener);
		JLabel l = new JLabel(name + ": ");
		l.setLabelFor(f);
		l.setDisplayedMnemonic(mnemonic);
		
		pnlRectEditor.add(l);
		pnlRectEditor.add(f);
		
		selectedRectDependendComponents.add(l);
		openFileDependendComponents.add(l);
		selectedRectDependendComponents.add(f);
		openFileDependendComponents.add(f);
		
		return f;
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
	
	/**
	 * MOD russa: additional save option for saving only current selection
	 * 
	 * This method initializes buttonSaveCurrent
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getButtonSaveCurrent() {
		if (buttonSaveCurrent == null) {
			buttonSaveCurrent = new JButton("Save Current");
			setButton(buttonSaveCurrent, "/cropCurrent.png", "Crop current selection and save to another PDF", true);
			buttonSaveCurrent.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					saveFile(true);
				}
			});
			
			selectedRectDependendComponents.add(buttonSaveCurrent);
			openFileDependendComponents.add(buttonSaveCurrent);
		}
		return buttonSaveCurrent;
	}

	private void saveFile() {//MOD russa: as "proxy"
		saveFile(false);
	}
	
	private void saveFile(boolean isSaveCurrentSelection) {//MOD russa: generalized/generic method
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(createFileFilter());
		File originalPdf = Model.getInstance().getPdf().getOriginalFile();
		
		//MOD russa: if only current selection is saved -> get corresponding page and prepare appropriate file name
		int targetPageIndex = -1;
		String namePostFix;
		if(isSaveCurrentSelection){
			
			targetPageIndex = this.uiHandler.getPage() - 1;//"convert" page-number to index
			
			String strCount = Integer.toString(Model.getInstance().getPdf().getPageCount());
			String strPage = Integer.toString(targetPageIndex + 1);//page number string
			while(strCount.length() > strPage.length()){
				strPage = "0" + strPage;
			}
			
			namePostFix = "_croppedpage"+strPage;
		}
		else {
			namePostFix = "_scissored";
		}
		
		// find file name without extension
		String fileName = originalPdf.getName();
		int dot = fileName.lastIndexOf('.');
		if(dot > -1)//remove file extension:
			fileName = fileName.substring(0, dot);

		fileName = fileName.substring(0, dot) + namePostFix + ".pdf";
		fileChooser.setSelectedFile(new File(originalPdf.getParentFile(), fileName));
		
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
			launchSaveTask(Model.getInstance().getPdf(), targetFile, targetPageIndex);
		}
	}

	private void launchSaveTask(PdfFile pdfFile, File targetFile, int targetPageIndex) {//MOD russa: additional 3rd argument -> IF != -1, the only this page is saved
		ArrayList<CropRect> cropRects = Model.getInstance().getCropRects(targetPageIndex, this.uiHandler.rectsInsertionOrder);
		new TaskPdfSave(pdfFile, targetFile, cropRects, defaultPdfPanel.getWidth(), defaultPdfPanel.getHeight(), this).execute();

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
			
			selectedRectDependendComponents.add(buttonDeleteRect);
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
			
			selectedRectDependendComponents.add(buttonSplitHorizontal);
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
			
			selectedRectDependendComponents.add(buttonSplitVertical);
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
	public void newPdfLoaded(PdfFile pdfFile) {
		debug("listening to new pdf loaded.");
		updateOpenFileDependents();
		getPageGroupListCellRenderer().setPageSize(pdfFile.getNormalizedPdfWidth(), pdfFile.getNormalizedPdfHeight());
		getPageGroupList().invalidate(); // to recalculate size etc

		uiHandler.notifySelectionChanged(null, null);//MOD russa: "initialize" UI controls with empty selection
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

		
		private RectChangeListener updateSelectedRectListener = new RectChangeListener() {//MOD russa
			@Override
			public void rectUpdated(Rect updatedRect, Rectangle repaintArea) {
				updateRectEditor(updatedRect);
			}
		};
		
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
		public void selectionChanged(Rect newSelection, Rect oldSelection) {//MOD russa
			
			//MOD russa: enable/disable buttons based on whether or not there is a current selection
			updateRectSelectedDependents(newSelection != null);
			
			if(oldSelection != null){
				oldSelection.removeListener(updateSelectedRectListener);
			}
			if(newSelection != null){
				newSelection.addListener(updateSelectedRectListener);
			}
			updateRectEditor(newSelection);
			
			
		}
		
		@Override
		public void pageGroupSelected(PageGroup pageGroup) {
			// update combo page list
			int pageCount = pageGroup.getPageCount();
			JComboBox combo = getPageSelectionCombo();
			combo.removeAllItems();
			if (pageCount > 1) {
				combo.addItem("Stacked view");
				combo.setEnabled(true);//re-enable combo-box
			} else {
				//if combo-box has only one item/page, disable it
				combo.setEnabled(false);
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
		
		@Override
		public void rectsStateChanged() {
			getPageGroupList().repaint();
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
			menuFile.add(getMenuFileOpenExtended());//MOD russa: additional open option (which was previously the normal open option)
			menuFile.add(getMenuSave());
			menuFile.add(getMenuSaveCurrent());//MOD russa: additional save option
			menuFile.addSeparator();
			menuFile.add(createMenuDonate());
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
					showFileOpenDialog(false);
				}
			});
		}
		return menuFileOpen;
	}
	
	/**
	 * This method initializes menuFileOpen
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuFileOpenExtended() {
		if (menuFileOpenExtended == null) {
			menuFileOpenExtended = new JMenuItem("Open (Extended)", KeyEvent.VK_O);
			menuFileOpenExtended.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			menuFileOpenExtended.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					showFileOpenDialog(true);
				}
			});
		}
		return menuFileOpenExtended;
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
	 * MOD russa: additional save option for saving only current selection
	 * This method initializes menuSaveCurrent
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getMenuSaveCurrent() {
		if (menuSaveCurrent == null) {
			menuSaveCurrent = new JMenuItem("Crop & Save Current Selection", KeyEvent.VK_S);
			menuSaveCurrent.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
			menuSaveCurrent.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					saveFile(true);
				}
			});
			
			selectedRectDependendComponents.add(menuSaveCurrent);
			openFileDependendComponents.add(menuSaveCurrent);
		}
		return menuSaveCurrent;
	}
	
	/**
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem createMenuDonate() {
		JMenuItem menuDonate = new JMenuItem("Donate", KeyEvent.VK_D);
		menuDonate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=SYDQUCMNMFMR8&lc=FI&item_name=Pdf%20scissors&item_number=pdfscissors&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted"));
					} catch (URISyntaxException e) {
						JOptionPane.showMessageDialog(MainFrame.this, "Ops! Failed to launch browser. Please visit www.pdfscissors.com to donate.");
						e.printStackTrace();
					} catch (IOException e) {
						JOptionPane.showMessageDialog(MainFrame.this, "Ops! Failed to launch browser. Please visit www.pdfscissors.com to donate.");
						e.printStackTrace();
					}
				}
			}
		});
		return menuDonate;
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
			menuHelp.addSeparator();
			menuHelp.add(createMenuDonate());
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
					AboutView about = new AboutView(MainFrame.this);
					about.setLocationRelativeTo(MainFrame.this);
					about.setVisible(true);
				}
			});
		}
		return menuAbout;
	}

}
