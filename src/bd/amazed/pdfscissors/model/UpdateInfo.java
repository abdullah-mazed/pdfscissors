package bd.amazed.pdfscissors.model;

public final class UpdateInfo {
	public final String tagName;    // e.g. "v1.0.1"
	public final String linuxUrl;
	public final String macUrl;
	public final String windowsUrl;

	public UpdateInfo(String tagName, String linuxUrl, String macUrl, String windowsUrl) {
		this.tagName = tagName;
		this.linuxUrl = linuxUrl;
		this.macUrl = macUrl;
		this.windowsUrl = windowsUrl;
	}

	/** Returns the download URL for the current OS, or null if not found. */
	public String getPlatformUrl() {
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("win")) return windowsUrl;
		if (os.contains("mac")) return macUrl;
		return linuxUrl;
	}

	/** "v1.0.1" -> "1.0.1" */
	public String displayVersion() {
		return (tagName != null && tagName.startsWith("v")) ? tagName.substring(1) : tagName;
	}
}
