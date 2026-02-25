package bd.amazed.pdfscissors.model;

import javax.swing.SwingWorker;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TaskCheckUpdate extends SwingWorker<UpdateInfo, Void> {

	private static final String API_URL =
		"https://api.github.com/repos/abdullah-mazed/pdfscissors/releases/latest";
	private static final int TIMEOUT_MS = 8_000;

	@Override
	protected UpdateInfo doInBackground() throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
		conn.setRequestProperty("Accept", "application/vnd.github+json");
		conn.setConnectTimeout(TIMEOUT_MS);
		conn.setReadTimeout(TIMEOUT_MS);
		conn.setInstanceFollowRedirects(true);

		if (conn.getResponseCode() != 200) return null;

		StringBuilder sb = new StringBuilder();
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
			String line;
			while ((line = r.readLine()) != null) sb.append(line);
		} finally {
			conn.disconnect();
		}

		String json = sb.toString();
		String tag      = extractString(json, "tag_name");
		String linuxUrl = findAssetUrl(json, ".deb");
		String macUrl   = findAssetUrl(json, ".dmg");
		String winUrl   = findAssetUrl(json, ".msi");
		return (tag == null) ? null : new UpdateInfo(tag, linuxUrl, macUrl, winUrl);
	}

	@Override
	protected void done() {
		try {
			UpdateInfo info = get();
			if (info != null && AppVersion.isNewer(AppVersion.CURRENT, info.tagName)) {
				firePropertyChange("latestRelease", null, info);
			}
		} catch (Exception e) {
			// network / timeout -- silently ignore; update check is best-effort
		}
	}

	static String extractString(String json, String key) {
		String search = "\"" + key + "\"";
		int ki = json.indexOf(search);
		if (ki < 0) return null;
		int colon = json.indexOf(':', ki + search.length());
		if (colon < 0) return null;
		int vs = json.indexOf('"', colon + 1);
		if (vs < 0) return null;
		int ve = json.indexOf('"', vs + 1);
		return (ve < 0) ? null : json.substring(vs + 1, ve);
	}

	static String findAssetUrl(String json, String suffix) {
		String key = "\"browser_download_url\"";
		int from = 0;
		while (true) {
			int ki = json.indexOf(key, from);
			if (ki < 0) return null;
			int colon = json.indexOf(':', ki + key.length());
			if (colon < 0) return null;
			int vs = json.indexOf('"', colon + 1);
			if (vs < 0) return null;
			int ve = json.indexOf('"', vs + 1);
			if (ve < 0) return null;
			String url = json.substring(vs + 1, ve);
			if (url.endsWith(suffix)) return url;
			from = ve + 1;
		}
	}
}
