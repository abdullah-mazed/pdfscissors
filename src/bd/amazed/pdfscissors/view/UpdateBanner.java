package bd.amazed.pdfscissors.view;

import bd.amazed.pdfscissors.model.TaskDownloadUpdate;
import bd.amazed.pdfscissors.model.UpdateInfo;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.FlowLayout;
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

		StackViewCreationDialog progress = new StackViewCreationDialog(owner);
		progress.setTitle("Downloading update...");
		progress.setModal(true);
		progress.enableProgress(task, e -> task.cancel(true));

		task.execute();
		progress.setLocationRelativeTo(owner);
		progress.setVisible(true);  // blocks until enableProgress disposes it on "done"

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

		try {
			Desktop.getDesktop().open(installer);
			System.exit(0);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(owner,
				"Installer downloaded but could not be launched automatically.\n\n"
				+ "Please open it manually:\n" + installer.getAbsolutePath(),
				"Manual installation required", JOptionPane.INFORMATION_MESSAGE);
		}
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
