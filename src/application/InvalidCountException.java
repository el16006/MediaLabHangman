package application;
public class InvalidCountException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidCountException (String word) {
		super("Found duplicate of " + word);
	}
}