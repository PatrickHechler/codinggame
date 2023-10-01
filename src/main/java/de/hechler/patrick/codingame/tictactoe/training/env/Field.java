package de.hechler.patrick.codingame.tictactoe.training.env;


@SuppressWarnings("javadoc")
public class Field implements TTTField {
	
	private Object[] fields = new Object[9];
	
	public Field() {
		for (int i = 0; i < 9; i++) {
			this.fields[i] = new int[9];
		}
	}
	
	private Field(Field copy) {
		for (int i = 0; i < 9; i++) {
			Object obj = copy.fields[i];
			if (obj instanceof int[] a) {
				this.fields[i] = a.clone();
			} else {
				this.fields[i] = obj; // Integer
			}
		}
	}
	
	@Override
	public Object value(int x, int y) {
		return this.fields[x * 3 + y];
	}
	
	@Override
	public int[] subField(int x, int y) {
		Object val = this.fields[x * 3 + y];
		if (val instanceof int[] a) return a;
		return null; // NOSONAR
	}
	
	@Override
	public int winner(int x, int y) {
		Object val = this.fields[x * 3 + y];
		if (val instanceof Integer i) return i.intValue();
		return 0;
	}
	
	@Override
	public int place(TTTPos pos, int val) {
		if (pos.innerX < 0 || pos.innerX >= 3 || pos.innerY < 0 || pos.innerY >= 3 || pos.outerX < 0 || pos.outerX >= 3 || pos.outerY < 0 || pos.outerY >= 3) {
			throw new IndexOutOfBoundsException("invalid position: " + pos + " (min: 0, max: 2)");
		}
		int   oi  = pos.outerX * 3 + pos.outerY;
		int[] arr = (int[]) this.fields[oi];
		int   ii  = pos.innerX * 3 + pos.innerY;
		if (arr[ii] != 0) {
			throw new IllegalStateException("place already filled");
		}
		arr[ii] = val;
		if (TTTField.checkWon((a, x, y) -> a[x * 3 + y], arr, pos.innerX, pos.innerY)) {
			this.fields[oi] = Integer.valueOf(val);
			if (TTTField.checkWon((a, x, y) -> a.fields[x * 3 + y] instanceof Integer ival ? ival.intValue() : 0, this, pos.innerX, pos.innerY)) {
				return val;
			}
			for (int i = 0; i < 9; i++) {
				if (this.fields[i] instanceof int[]) return 0;
			}
			return -val;
		}
		for (int i = 0; i < 9; i++) {
			if (arr[i] == 0) return 0;
		}
		this.fields[oi] = Integer.valueOf(0);
		return 0;
	}
	
	@Override
	public TTTField copy() {
		return new Field(this);
	}
	
	@Override
	public TTTField reverse() {
		return new ReverseField(this);
	}
	
}
