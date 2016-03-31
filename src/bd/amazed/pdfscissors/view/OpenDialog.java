package bd.amazed.pdfscissors.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.model.PageGroup;

import javax.swing.JCheckBox;

public class OpenDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
//	private JTextField filePath;//MOD russa: immediately load file
	private MainFrame mainFrame;
	protected File file;
	private Vector<Component> advancedOptions;
	
	private ButtonGroup stackGroupTypeChoices;//MOD russa: refactored
	private JCheckBox chckbxCreateStackedView;//MOD russa: refactored
	private JButton btnBrowse;//MOD russa: refactored
	
	public static final int DEFAULT_OPEN_OPTION_STACKED_TYPE = PageGroup.GROUP_TYPE_INDIVIDUAL;//MOD russa: default opening option (CONSTANT)
	public static final boolean DEFAULT_OPEN_OPTION_ADD_STACKED_VIEW_MODE = true;//MOD russa: default opening option (CONSTANT)
	private int defaultOpenOptionStackType;//MOD russa: default opening option
	private boolean defaultOpenOptionAddStackedView;//MOD russa: default opening option
	

//	/**
//	 * Launch the application.
//	 */
//	public static void main(String[] args) {
//		try {
//			OpenDialog dialog = new OpenDialog();
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */
	public OpenDialog() {
		
		//MOD russa: set default options
		defaultOpenOptionStackType = getPageGroupTypeSetting();
		defaultOpenOptionAddStackedView = getStackedViewSetting();
				
		advancedOptions = new Vector<Component>();
		setTitle("Open PDF");		
		setBounds(100, 100, 509, 299);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{427, 56, 0};
		gbl_contentPanel.rowHeights = new int[]{14, 23, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
//		{//MOD russa: immediately load file (no file-input / path field needed anymore)
//			JLabel lblFileLocation = new JLabel("File location");
//			GridBagConstraints gbc_lblFileLocation = new GridBagConstraints();
//			gbc_lblFileLocation.anchor = GridBagConstraints.NORTHWEST;
//			gbc_lblFileLocation.insets = new Insets(0, 0, 5, 5);
//			gbc_lblFileLocation.gridx = 0;
//			gbc_lblFileLocation.gridy = 0;
//			contentPanel.add(lblFileLocation, gbc_lblFileLocation);
//		}
//		{
//			filePath = new JTextField();
//			filePath.setEditable(false);
//			GridBagConstraints gbc_filePath = new GridBagConstraints();
//			gbc_filePath.fill = GridBagConstraints.HORIZONTAL;
//			gbc_filePath.insets = new Insets(0, 0, 5, 5);
//			gbc_filePath.gridx = 0;
//			gbc_filePath.gridy = 1;
//			contentPanel.add(filePath, gbc_filePath);
//			filePath.setColumns(10);
//		}
		
		//MOD russa: re-factored, i.e. extracted file open/browse as action
		AbstractAction browseAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showFileChooserDialog(true, false);//false, false);//MOD russa: immediately load file
			}
		};
		
		//MOD russa: add "open" for hot-key CTRL + O
		contentPanel.getActionMap().put("Open", browseAction);
		contentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK ), "Open"
		);
		
		stackGroupTypeChoices = new ButtonGroup();
		String lastSelectionOption = Model.getInstance().getProperties().getProperty(Model.PROPERTY_LAST_STACK_TYPE);
		{
			btnBrowse = new JButton("Open...");
			btnBrowse.setMnemonic('O');
//			GridBagConstraints gbc_btnBrowse = new GridBagConstraints();//MOD russa: immediately load file (re-locate open button to be next to cancel-button)
//			gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
//			gbc_btnBrowse.anchor = GridBagConstraints.NORTHEAST;
//			gbc_btnBrowse.gridx = 1;
//			gbc_btnBrowse.gridy = 1;
//			contentPanel.add(btnBrowse, gbc_btnBrowse);
//			btnBrowse.addActionListener(new ActionListener() {
//				
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					showFileChooserDialog(true, false);//false, false);//MOD russa: immediately load file
//				}
//				
//			});
			btnBrowse.addActionListener(browseAction);
		}
//		{
//			JLabel lblHowDoYou = new JLabel("How do you want to crop?");
//			GridBagConstraints gbc_lblHowDoYou = new GridBagConstraints();
//			gbc_lblHowDoYou.anchor = GridBagConstraints.WEST;
//			gbc_lblHowDoYou.insets = new Insets(0, 0, 5, 5);
//			gbc_lblHowDoYou.gridx = 0;
//			gbc_lblHowDoYou.gridy = 2;
//			contentPanel.add(lblHowDoYou, gbc_lblHowDoYou);
//		}
		{
			JRadioButton rdbtnAllPagesTogether = new JRadioButton("All pages together (Easiest way)");
			stackGroupTypeChoices.add(rdbtnAllPagesTogether);
			stackGroupTypeChoices.setSelected(rdbtnAllPagesTogether.getModel(), true);
			rdbtnAllPagesTogether.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_ALL));
			GridBagConstraints gbc_rdbtnAllPagesTogether = new GridBagConstraints();
			gbc_rdbtnAllPagesTogether.anchor = GridBagConstraints.WEST;
			gbc_rdbtnAllPagesTogether.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnAllPagesTogether.gridx = 0;
			gbc_rdbtnAllPagesTogether.gridy = 3;
			contentPanel.add(rdbtnAllPagesTogether, gbc_rdbtnAllPagesTogether);
		}
		{
			JRadioButton rdbtnOddAndEven = new JRadioButton("Odd and even pages separately");
			stackGroupTypeChoices.add(rdbtnOddAndEven);
			rdbtnOddAndEven.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_ODD_EVEN));
			if (rdbtnOddAndEven.getActionCommand().equals(lastSelectionOption)) {
				stackGroupTypeChoices.setSelected(rdbtnOddAndEven.getModel(), true);
			}
			GridBagConstraints gbc_rdbtnOddAndEven = new GridBagConstraints();
			gbc_rdbtnOddAndEven.anchor = GridBagConstraints.WEST;
			gbc_rdbtnOddAndEven.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnOddAndEven.gridx = 0;
			gbc_rdbtnOddAndEven.gridy = 4;
			contentPanel.add(rdbtnOddAndEven, gbc_rdbtnOddAndEven);
		}
		{
			JRadioButton rdbtnAllPagesSeparately = new JRadioButton("Every page separately ");
			stackGroupTypeChoices.add(rdbtnAllPagesSeparately);
			rdbtnAllPagesSeparately.setActionCommand(String.valueOf(PageGroup.GROUP_TYPE_INDIVIDUAL));
			if (rdbtnAllPagesSeparately.getActionCommand().equals(lastSelectionOption)) {
				stackGroupTypeChoices.setSelected(rdbtnAllPagesSeparately.getModel(), true);
			}
			GridBagConstraints gbc_rdbtnAllPagesSeparately = new GridBagConstraints();
			gbc_rdbtnAllPagesSeparately.anchor = GridBagConstraints.WEST;
			gbc_rdbtnAllPagesSeparately.insets = new Insets(0, 0, 5, 5);
			gbc_rdbtnAllPagesSeparately.gridx = 0;
			gbc_rdbtnAllPagesSeparately.gridy = 5;
			contentPanel.add(rdbtnAllPagesSeparately, gbc_rdbtnAllPagesSeparately);
		}
		{
			final String showText = "Show advanced options";
			final String hideText = "Hide advanced options";
			final JButton btnShowAdvancedOptions = new JButton(showText);
			btnShowAdvancedOptions.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean showNow = showText.equals(btnShowAdvancedOptions.getText());
					if (showNow) {
						btnShowAdvancedOptions.setText(hideText);
					} else {
						btnShowAdvancedOptions.setText(showText);
					}
					for (Component component : advancedOptions) {
						component.setVisible(showNow);
					}
				}
			});
			GridBagConstraints gbc_btnShowAdvancedOptions = new GridBagConstraints();
			gbc_btnShowAdvancedOptions.anchor = GridBagConstraints.WEST;
			gbc_btnShowAdvancedOptions.insets = new Insets(0, 0, 5, 5);
			gbc_btnShowAdvancedOptions.gridx = 0;
			gbc_btnShowAdvancedOptions.gridy = 7;
			contentPanel.add(btnShowAdvancedOptions, gbc_btnShowAdvancedOptions);
		}
		chckbxCreateStackedView = new JCheckBox("Create stacked view that helps cropping");
		{	
			chckbxCreateStackedView.setSelected(true);
			GridBagConstraints gbc_chckbxCreateStackedView = new GridBagConstraints();
			gbc_chckbxCreateStackedView.anchor = GridBagConstraints.WEST;
			gbc_chckbxCreateStackedView.insets = new Insets(0, 0, 0, 5);
			gbc_chckbxCreateStackedView.gridx = 0;
			gbc_chckbxCreateStackedView.gridy = 8;
			advancedOptions.add(chckbxCreateStackedView);
			chckbxCreateStackedView.setVisible(false);
			contentPanel.add(chckbxCreateStackedView, gbc_chckbxCreateStackedView);
		}
		
	
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
//			{//MOD russa: immediately load file (no OK-button needed anymore)
//				JButton okButton = new JButton("OK");
//				okButton.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent arg0) {
//						doLoadFile(false);
//					}
//				});
//				okButton.setActionCommand("OK");
//				buttonPane.add(okButton);
//				getRootPane().setDefaultButton(okButton);
//			}	
			
			//MOD russa: refactored cancel as AbstractAction
			AbstractAction cancelAction = new AbstractAction(){
				@Override
				public void actionPerformed(ActionEvent e) {
					OpenDialog.this.dispose();
				}
			};
			
			//MOD russa: add "cancel" for hot-key ESC
			buttonPane.getActionMap().put("Cancel", cancelAction);
			buttonPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Cancel"
			);
			
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setMnemonic('C');
				cancelButton.addActionListener(cancelAction);
//				cancelButton.addActionListener(new ActionListener() {//MOD russa
//					public void actionPerformed(ActionEvent e) {
//						OpenDialog.this.dispose();
//					}
//				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
			
			
			
			//add browse button:
			buttonPane.add(btnBrowse);
			btnBrowse.requestFocusInWindow();
		}
	
	}


	/**
	 * MOD russa: getter for extended file open option "create stacked view"
	 * 
	 * Returns the setting from the properties or the default value.
	 */
	public static boolean getStackedViewSetting() {
		String defaultCreateStackedView = Model.getInstance().getProperties().getProperty(Model.PROPERTY_DEFAULT_CREATE_STACKED_VIEW, Boolean.toString(DEFAULT_OPEN_OPTION_ADD_STACKED_VIEW_MODE));//MOD russa: set default stack-type, if it was set
		try{
			return Boolean.valueOf(defaultCreateStackedView);
		} catch(Exception e){
			return DEFAULT_OPEN_OPTION_ADD_STACKED_VIEW_MODE;
		}
	}
	
	/**
	 * MOD russa: getter for extended file open option "page grouping type" (i.e. ALL, EVEN_AND_ODD, SINGLE_PAGES)
	 * 
	 * Returns the setting from the properties or the default value.
	 */
	public static int getPageGroupTypeSetting() {
		String defaultStackTypeOption = Model.getInstance().getProperties().getProperty(Model.PROPERTY_DEFAULT_STACK_TYPE, Integer.toString(DEFAULT_OPEN_OPTION_STACKED_TYPE));//MOD russa: set default stack-type, if it was set
		try{
			return Integer.valueOf(defaultStackTypeOption);
		} catch(NumberFormatException e){
			return DEFAULT_OPEN_OPTION_STACKED_TYPE;
		}
	}
	
	
	
	@Override
	public void setVisible(boolean b) {
		
		super.setVisible(b);
		
		//focus browse-button when dialog is shown:
		if(b)
			btnBrowse.requestFocusInWindow();
	}

	/**
	 * MOD russa: refactored (extracted method)
	 * 
	 * MOD russa: added boolean argument:
	 * 
	 * @param isUseDefaultOptions
	 * 			if TRUE the default settings for the extended options are used 
	 */
	private void doLoadFile(boolean isUseDefaultOptions){
		if (file == null) {
			JOptionPane.showMessageDialog(OpenDialog.this, "Select a pdf file first");
			return;
		}
		int type = Integer.valueOf(stackGroupTypeChoices.getSelection().getActionCommand());
		Model.getInstance().getProperties().setProperty(Model.PROPERTY_LAST_STACK_TYPE, stackGroupTypeChoices.getSelection().getActionCommand());
		OpenDialog.this.dispose();
		
		boolean isCreateStackedView = chckbxCreateStackedView.isSelected();
		
		if(isUseDefaultOptions){//MOD russa
			type = defaultOpenOptionStackType;
			isCreateStackedView = defaultOpenOptionAddStackedView;
		}
		
		mainFrame.openFile(file, type, isCreateStackedView);
	}
	
//	public void showFileChooserDialog(){
//		showFileChooserDialog(false, false);
//	}

	/**
	 * MOD russa: added two boolean arguments:
	 * 
	 * @param isCloseMainDialog
	 * 			if TRUE the selected file is loaded immediately (if no file was selected, only the internal file-selection is updated/cleared)
	 * @param isUseDefaultOptions
	 * 			if TRUE the default settings for the extended options are used 
	 */
	public void showFileChooserDialog(boolean isCloseMainDialog, boolean isUseDefaultOptions) {//MOD russa: added boolean argument -> if TRUE, loading the file is immediately triggered
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileFilter(mainFrame.createFileFilter());
		String lastFile = Model.getInstance().getProperties().getProperty(Model.PROPERTY_LAST_FILE);
		if (lastFile != null) {
			System.out.println("Last opened directory rememberred: " + lastFile);
			fileChooser.setCurrentDirectory(new File(lastFile));
		}

		int retval = fileChooser.showOpenDialog(mainFrame);
		if (retval == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
			if (file != null) {
				Model.getInstance().getProperties().setProperty(Model.PROPERTY_LAST_FILE, file.getParentFile().getAbsolutePath());
//				filePath.setText(file.getAbsolutePath());//MOD russa: immediately load file
				
				if(isCloseMainDialog){//MOD russa
					doLoadFile(isUseDefaultOptions);
				}
				
			} 
//			else {//MOD russa: immediately load file
//				filePath.setText("");
//			}
		}
		
	}

	public void seMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

}
