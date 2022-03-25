package application;
public class InvalidRangeException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidRangeException (int length) {
		super("Found a word with a length of " + length);
	}
}