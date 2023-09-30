package de.hechler.patrick.codingame.tictactoe.training;


@SuppressWarnings("javadoc")
public class ReadOnlyField implements TTTField {
	
	private final TTTField orig;
	
	public ReadOnlyField(TTTField orig) {
		this.orig = orig;
	}
	
	@Override
	public Object value(int x, int y) {
		Object val = this.orig.value(x, y);
		if (val instanceof int[] a) return a.clone();
		return val;
	}
	
	@Override
	public int[] subField(int x, int y) {
		int[] a = this.orig.subField(x, y);
		if (a != null) return a.clone();
		return null;//NOSONAR
	}
	
	@Override
	public int winner(int x, int y) {
		return this.orig.winner(x, y);
	}
	
	@Override
	public int place(@SuppressWarnings("unused") TTTPos pos, @SuppressWarnings("unused") int val) {
		throw new UnsupportedOperationException("this is a read only field");
	}
	
	@Override
	public TTTField copy() {
		return this;
	}
	
	@Override
	public TTTField reverse() {
		return new ReverseField(this);
	}
	
}
