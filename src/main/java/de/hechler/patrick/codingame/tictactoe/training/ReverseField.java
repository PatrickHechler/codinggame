package de.hechler.patrick.codingame.tictactoe.training;


@SuppressWarnings("javadoc")
public class ReverseField implements TTTField {
	
	private final TTTField orig;
	
	public ReverseField(TTTField orig) {
		this.orig = orig;
	}
	
	@Override
	public Object value(int x, int y) {
		Object val = this.orig.value(x, y);
		if (!(val instanceof int[] a)) {
			return val;
		}
		int[] c = a.clone();
		for (int i = 0; i < 9; i++) {
			c[i] = -c[i];
		}
		return c;
	}
	
	@Override
	public int[] subField(int x, int y) {
		int[] a = this.orig.subField(x, y);
		if (a == null) return null;//NOSONAR
		int[] c = a.clone();
		for (int i = 0; i < 9; i++) {
			c[i] = -c[i];
		}
		return c;
	}
	
	@Override
	public int winner(int x, int y) {
		return -this.orig.winner(x, y);
	}
	
	@Override
	public int place(TTTPos pos, int val) {
		return -this.orig.place(pos, -val);
	}
	
	@Override
	public TTTField copy() {
		return new ReverseField(this.orig.copy());
	}
	
	@Override
	public TTTField reverse() {
		return this.orig;
	}
	
}
