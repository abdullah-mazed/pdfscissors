package bd.amazed.pdfscissors.view;

public interface UIHandlerListener {
	public void editingModeChanged(int newMode);
	/**
	 * 
	 * @param index 0 = stacked, 1 = first page
	 */
	public void pageChanged(int index);
}
