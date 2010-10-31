/*
 * PdfscissorsView.java
 */

package bd.amazed.pdfscissors;

import bd.amazed.pdfscissors.model.PdfCropper;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.EventObject;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.application.Application.ExitListener;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.Task;
import org.jdesktop.application.TaskService;
import org.jpedal.exception.PdfException;

/**
 * The application's main frame.
 */
public class PdfscissorsView extends FrameView {

    private File currentFile;
    private PdfCropper currentCropper;
    private PdfPanel defaultPdfPanel;

    public PdfscissorsView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        defaultPdfPanel = new PdfPanel();

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        

        this.getFrame().addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                System.out.println("Exiting...");
                closeCurrentFile();
            }
        });

        getApplication().addExitListener(new ExitListener() {
            public boolean canExit(EventObject e) {
                boolean bOkToExit = false;
                Component source = (Component) e.getSource();
                bOkToExit = JOptionPane.showConfirmDialog(source,
                                "Exit?") ==
                                JOptionPane.YES_OPTION;
                return bOkToExit;
            } 
            public void willExit(EventObject event) {

            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PdfscissorsApp.getApplication().getMainFrame();
            aboutBox = new PdfscissorsAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PdfscissorsApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        buttonOpen = new javax.swing.JButton();
        buttonSave = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jLabel9 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        rowInput = new javax.swing.JSpinner();
        colInput = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        orderingInput = new javax.swing.JComboBox();
        jToolBar3 = new javax.swing.JToolBar();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        contentAreaXInput = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        contentAreaYInput = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        contentAreaWInput = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        contentAreaHInput = new javax.swing.JSpinner();
        scrollPanel = new javax.swing.JScrollPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        jToolBar1.setRollover(true);
        jToolBar1.setName("toolbarFile"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(bd.amazed.pdfscissors.PdfscissorsApp.class).getContext().getResourceMap(PdfscissorsView.class);
        buttonOpen.setIcon(resourceMap.getIcon("buttonOpen.icon")); // NOI18N
        buttonOpen.setText(resourceMap.getString("buttonOpen.text")); // NOI18N
        buttonOpen.setToolTipText(resourceMap.getString("buttonOpen.toolTipText")); // NOI18N
        buttonOpen.setFocusable(false);
        buttonOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonOpen.setName("buttonOpen"); // NOI18N
        buttonOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpenActionPerformed(evt);
            }
        });
        jToolBar1.add(buttonOpen);

        buttonSave.setIcon(resourceMap.getIcon("buttonSave.icon")); // NOI18N
        buttonSave.setText(resourceMap.getString("buttonSave.text")); // NOI18N
        buttonSave.setToolTipText(resourceMap.getString("buttonSave.toolTipText")); // NOI18N
        buttonSave.setFocusable(false);
        buttonSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSave.setName("buttonSave"); // NOI18N
        buttonSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(buttonSave);

        jToolBar2.setRollover(true);
        jToolBar2.setEnabled(false);
        jToolBar2.setName("toolbarRowColum"); // NOI18N

        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N
        jToolBar2.add(jLabel9);

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        jToolBar2.add(jLabel1);

        rowInput.setModel(new SpinnerNumberModel(1, 1, Constants.MAX_ROW_COL, 1));
        rowInput.setToolTipText(resourceMap.getString("rowCount.toolTipText")); // NOI18N
        rowInput.setName("rowCount"); // NOI18N
        jToolBar2.add(rowInput);

        colInput.setText(resourceMap.getString("colInput.text")); // NOI18N
        colInput.setName("colInput"); // NOI18N
        jToolBar2.add(colInput);

        jSpinner2.setModel(new SpinnerNumberModel(1, 1, Constants.MAX_ROW_COL, 1));
        jSpinner2.setName("colCount"); // NOI18N
        jToolBar2.add(jSpinner2);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N
        jToolBar2.add(jLabel3);

        orderingInput.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "\"Top to bottom\"", "\"Left to right\"" }));
        orderingInput.setName("orderingInput"); // NOI18N
        jToolBar2.add(orderingInput);

        jToolBar3.setRollover(true);
        jToolBar3.setName("jToolBar3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N
        jToolBar3.add(jLabel4);

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N
        jToolBar3.add(jLabel5);

        contentAreaXInput.setName("cropAreaXInput"); // NOI18N
        jToolBar3.add(contentAreaXInput);

        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N
        jToolBar3.add(jLabel6);

        contentAreaYInput.setName("cropAreaYInput"); // NOI18N
        jToolBar3.add(contentAreaYInput);

        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N
        jToolBar3.add(jLabel7);

        contentAreaWInput.setName("cropAreaWidthInput"); // NOI18N
        jToolBar3.add(contentAreaWInput);

        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N
        jToolBar3.add(jLabel8);

        contentAreaHInput.setName("cropAreaHeightInput"); // NOI18N
        jToolBar3.add(contentAreaHInput);

        scrollPanel.setName("scrollPanel"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar3, javax.swing.GroupLayout.PREFERRED_SIZE, 344, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(108, Short.MAX_VALUE))
            .addComponent(scrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jToolBar3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 567, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(bd.amazed.pdfscissors.PdfscissorsApp.class).getContext().getActionMap(PdfscissorsView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 732, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void buttonOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpenActionPerformed
        // TODO add your handling code here:
        openFile();
    }//GEN-LAST:event_buttonOpenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonOpen;
    private javax.swing.JButton buttonSave;
    private javax.swing.JLabel colInput;
    private javax.swing.JSpinner contentAreaHInput;
    private javax.swing.JSpinner contentAreaWInput;
    private javax.swing.JSpinner contentAreaXInput;
    private javax.swing.JSpinner contentAreaYInput;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JComboBox orderingInput;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JSpinner rowInput;
    private javax.swing.JScrollPane scrollPanel;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(createFileFilter());
        int retval = fileChooser.showOpenDialog(mainPanel);        
        if (retval == JFileChooser.APPROVE_OPTION) {
            closeCurrentFile();
            currentFile = fileChooser.getSelectedFile();
            currentCropper = new PdfCropper(currentFile);

            

            //create new scrollpane content
            JPanel scrollPaneView = new JPanel();
            scrollPaneView.setLayout(new GridBagLayout());

            int pdfPanelCount = 1; //more to come
            for (int i = 0; i < pdfPanelCount; i++) {
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = i;
                constraints.anchor = GridBagConstraints.CENTER;
                constraints.insets = new Insets(2, 0, 2, 0);
                if (i == 0) {
                    scrollPaneView.add(defaultPdfPanel, constraints);
                }
            }
            
            scrollPanel.setViewportView(scrollPaneView);
            launchTask(new TaskPdfOpen(getApplication(), currentFile), "Reading pdf...");
        }
    }

    /**
     * Launches background thread.
     */
    private void launchTask(final Task task, final String message) {
        final ProgressMonitor progressMonitor = new ProgressMonitor(getFrame(),
                message,
                "Please wait", 0, 100);
        progressMonitor.setProgress(0);
        task.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("started".equals(evt.getPropertyName())) {
                    progressMonitor.setNote(message);
                } else if ("progress" == evt.getPropertyName()) {
                    int progress = (Integer) evt.getNewValue();
                    progressMonitor.setProgress(progress);
                    String message = String.format("Completed %d%%.\n", progress);
                    progressMonitor.setNote(message);
                    if (progressMonitor.isCanceled() || task.isDone()) {
                        if (progressMonitor.isCanceled()) {
                            task.cancel(true);
                        }
                    }
                }
            }
        });
        ApplicationContext appContext = getApplication().getContext();

        TaskMonitor taskMonitor = appContext.getTaskMonitor();
        TaskService taskService = appContext.getTaskService();
        taskService.execute(task);
        taskMonitor.setForegroundTask(task);
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

    private void closeCurrentFile() {
       if (currentCropper != null) {
           currentCropper.close();
       }
    }

    private void handleException(String userFriendlyMessage, PdfException ex) {
        JOptionPane.showMessageDialog(getFrame(), userFriendlyMessage + "\nDetails:" + ex);
        ex.printStackTrace();
    }

    private void message(String string) {
        JOptionPane.showMessageDialog(getFrame(), string);
    }


    class TaskPdfOpen extends org.jdesktop.application.Task <BufferedImage, Void> {

        private File file;

        
        public TaskPdfOpen(org.jdesktop.application.Application app, File file) {
            super(app);
            this.file = file;
        }

        
        @Override
        protected BufferedImage doInBackground() throws Exception {
            
            PdfscissorsApp.debug("Extracting pdf image...");
            for(int i = 0; i < 10; i++) {
                Thread.sleep(200);//TODO remove
                setProgress(i * 10);
            }
            PdfCropper cropper = new PdfCropper(file);
            setProgress(100);
            return cropper.getImage(1);            
        }

        @Override
        protected void succeeded(BufferedImage image) {
            super.succeeded(image);
            if (image == null) {
                message("Ups... Failed to extract pdf content. Possibly a bad or password protected pdf.");
            }
            defaultPdfPanel.setImage(image);
        }

        @Override
        protected void finished() {
            super.finished();
            scrollPanel.revalidate();
            PdfscissorsApp.debug("Extracting pdf image done!!");
        }
    }
   
}
