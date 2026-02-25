package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.TaskDownloadUpdate;
import bd.amazed.pdfscissors.model.UpdateInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;

public class UpdateBanner extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color BG     = new Color(0xFF, 0xF3, 0xCD);
	private static final Color BORDER = new Color(0xFF, 0xC1, 0x07);

	private final JFrame     owner;
	private final UpdateInfo info;

	public UpdateBanner(JFrame owner, UpdateInfo info) {
		this.owner = owner;
		this.info  = info;
		buildUi();
	}

	private void buildUi() {
		setLayout(new BorderLayout(8, 0));
		setBackground(BG);
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
			BorderFactory.createEmptyBorder(4, 8, 4, 8)));

		String downloadHint = (info.getPlatformUrl() != null)
			? "Click 'Download & Install' to update automatically."
			: "Visit GitHub to download the latest release.";
		add(new JLabel("PDF Scissors " + info.displayVersion() + " is available.  " + downloadHint),
			BorderLayout.CENTER);

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		buttons.setOpaque(false);

		if (info.getPlatformUrl() != null) {
			JButton btn = new JButton("Download & Install");
			btn.addActionListener(e -> startDownload());
			buttons.add(btn);
		}

		JButton dismiss = new JButton("Dismiss");
		dismiss.addActionListener(e -> dismiss());
		buttons.add(dismiss);

		add(buttons, BorderLayout.EAST);
	}

	private void startDownload() {
		TaskDownloadUpdate task = new TaskDownloadUpdate(info.getPlatformUrl());

		// Simple progress dialog
		JDialog progress = new JDialog(owner, "Downloading update...", true);
		JProgressBar bar = new JProgressBar(0, 100);
		bar.setStringPainted(true);
		bar.setString("Connecting...");
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(e -> task.cancel(true));

		JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(new JLabel("Downloading PDF Scissors " + info.displayVersion() + "..."), BorderLayout.NORTH);
		content.add(bar, BorderLayout.CENTER);
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		btnPanel.add(cancelBtn);
		content.add(btnPanel, BorderLayout.SOUTH);

		progress.setContentPane(content);
		progress.pack();
		progress.setResizable(false);

		task.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if ("progress".equals(evt.getPropertyName())) {
					bar.setValue((Integer) evt.getNewValue());
				} else if ("message".equals(evt.getPropertyName())) {
					bar.setString((String) evt.getNewValue());
				} else if ("done".equals(evt.getPropertyName())) {
					progress.dispose();
				}
			}
		});

		task.execute();
		progress.setLocationRelativeTo(owner);
		progress.setVisible(true);  // blocks until "done" fires dispose()

		handleDownloadDone(task);
	}

	private void handleDownloadDone(TaskDownloadUpdate task) {
		File installer;
		try {
			installer = task.get();
		} catch (CancellationException e) {
			return;  // user cancelled -- no message needed
		} catch (Exception e) {
			JOptionPane.showMessageDialog(owner,
				"Download failed: " + e.getMessage()
				+ "\n\nPlease download manually from:\n" + info.getPlatformUrl(),
				"Update failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 1. Try to open/run the installer directly
		try {
			Desktop.getDesktop().open(installer);
			System.exit(0);
			return;
		} catch (Exception ex) { /* fall through */ }

		// 2. Try to open the containing folder so the user can run it
		try {
			Desktop.getDesktop().open(installer.getParentFile());
			JOptionPane.showMessageDialog(owner,
				"The installer could not be launched automatically.\n\n"
				+ "The folder containing the installer has been opened.\n"
				+ "Please double-click the file to install:\n"
				+ installer.getName(),
				"Manual installation required", JOptionPane.INFORMATION_MESSAGE);
			return;
		} catch (Exception ex) { /* fall through */ }

		// 3. Show manual instructions
		JOptionPane.showMessageDialog(owner,
			"The installer was downloaded but could not be opened automatically.\n\n"
			+ "Please navigate to the following location and run the installer manually:\n"
			+ installer.getAbsolutePath(),
			"Manual installation required", JOptionPane.INFORMATION_MESSAGE);
	}

	public void dismiss() {
		Container parent = getParent();
		if (parent != null) {
			parent.remove(this);
			parent.revalidate();
			parent.repaint();
		}
	}
}
