package application;
public class UnbalancedException extends Exception {
	private static final long serialVersionUID = 1L;

	public UnbalancedException (int count, int size) {
		super("Only " + (int)(count*100/size) + "% of words contain 9 or more letters");
	}
}