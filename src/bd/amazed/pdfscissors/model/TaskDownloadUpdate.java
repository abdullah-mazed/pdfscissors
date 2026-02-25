package bd.amazed.pdfscissors.model;

import javax.swing.SwingWorker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TaskDownloadUpdate extends SwingWorker<File, Void> {

	private final String downloadUrl;

	public TaskDownloadUpdate(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	@Override
	protected File doInBackground() throws Exception {
		firePropertyChange("message", null, "Connecting...");
		setProgress(0);

		HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
		conn.setConnectTimeout(15_000);
		conn.setReadTimeout(60_000);
		conn.setInstanceFollowRedirects(true);
		conn.connect();
		if (conn.getResponseCode() != 200)
			throw new IOException("Server returned HTTP " + conn.getResponseCode());

		long total = conn.getContentLengthLong();
		String path = new URL(downloadUrl).getPath();
		String name = path.substring(path.lastIndexOf('/') + 1);
		File target = resolveDownloadTarget(name);

		firePropertyChange("message", null, "Downloading " + name + "...");

		try (InputStream in = conn.getInputStream();
			 OutputStream out = new FileOutputStream(target)) {
			byte[] buf = new byte[8192];
			long received = 0;
			int read;
			while ((read = in.read(buf)) != -1) {
				out.write(buf, 0, read);
				received += read;
				if (total > 0) {
					setProgress((int)(received * 100L / total));
					firePropertyChange("message", null,
						String.format("Downloading... %.1f MB / %.1f MB", received / 1048576.0, total / 1048576.0));
				}
			}
		}
		setProgress(100);
		return target;
	}

	private static File resolveDownloadTarget(String name) {
		File downloads = new File(System.getProperty("user.home"), "Downloads");
		if (downloads.isDirectory() && downloads.canWrite()) {
			return new File(downloads, name);
		}
		return new File(System.getProperty("java.io.tmpdir"), name);
	}

	@Override
	protected void done() {
		firePropertyChange("done", false, true);
	}
}
