package bd.amazed.pdfscissors.model;

public final class AppVersion {
	/** Must match the git tag being pushed (without 'v'), e.g. "1.0.1". */
	public static final String CURRENT = "1.0.0";

	private AppVersion() {}

	/**
	 * Returns true if remote is strictly newer than local.
	 * Accepts "v"-prefixed strings. Falls back to false on parse errors.
	 */
	public static boolean isNewer(String local, String remote) {
		try {
			int[] l = parse(local);
			int[] r = parse(remote);
			for (int i = 0; i < 3; i++) {
				if (r[i] > l[i]) return true;
				if (r[i] < l[i]) return false;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static int[] parse(String v) {
		if (v != null && v.startsWith("v")) v = v.substring(1);
		String[] parts = (v == null ? "0.0.0" : v).split("\\.");
		int[] nums = new int[3];
		for (int i = 0; i < 3; i++) {
			nums[i] = (i < parts.length) ? Integer.parseInt(parts[i].trim()) : 0;
		}
		return nums;
	}
}
