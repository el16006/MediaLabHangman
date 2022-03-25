package application;
public class UndersizeException extends Exception {
	private static final long serialVersionUID = 1L;

	public UndersizeException (int size) {
		super("Only " + size + " words found.");
	}
}