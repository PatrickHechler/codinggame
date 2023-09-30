package de.hechler.patrick.codingame.tictactoe;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.Function;

class Player {
	
	public static final PrintStream SOUT = System.out;
	public static final PrintStream SERR = System.err;
	
	private static TTTField field = new TTTField();
	
	public static void main(String[] args) {
		try (Scanner in = new Scanner(System.in)) {
			while (true) {
				int x = in.nextInt();
				int y = in.nextInt();
				if (x == -1) { // hard coded start
					field.place(3 + 2, 3 + 1, Boolean.TRUE);
					SOUT.println((3 + 2) + " " + (3 + 1));
					continue;
				} else if (field.place(x, y, Boolean.FALSE)) {
					SERR.println("well, thats it, I lost");
					return;
				}
				x %= 3;
				y %= 3;
				TTT<Boolean> val = field.value(x, y);
				if (val.winner() == null) {
					place((TTTSubField) val);
				} else {
					place();
				}
			}
		}
	}
	
	private static void place() {
		// TODO Auto-generated method stub
	}
	
	private static void place(TTTSubField sub) {
		// TODO Auto-generated method stub
	}
	
}

class TTTField implements TTT<TTT<Boolean>> {
	
	@SuppressWarnings("unchecked")
	private final TTT<Boolean>[][] field = new TTT[3][3];
	
	public TTTField() {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.field[x][y] = new TTTSubField(x, y);
			}
		}
	}
	
	private TTTField(TTT<Boolean>[][] copyField) {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				this.field[x][y] = copyField[x][y].copy();
			}
		}
	}
	
	@Override
	public TTT<Boolean> value(int x, int y) {
		return this.field[x][y];
	}
	
	@Override
	public boolean place(int lx, int ly, Boolean val) {
		int     dx = lx / 3;
		int     mx = lx % 3;
		int     dy = ly / 3;
		int     my = ly % 3;
		boolean b  = this.field[dx][dy].place(mx, my, val);
		if (!b) return false;// NOSONAR
		this.field[dx][dy] = val.booleanValue() ? TTT.ME_WON : TTT.ENEMY_WON;
		return TTT.checkWon(this, t -> t instanceof TTTFinish ? ((TTTFinish<Boolean>) t).winner() : null, dx, dy);
	}
	
	@Override
	public TTTField copy() {
		return new TTTField(this.field);
	}
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + Arrays.deepHashCode(this.field);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TTTField)) return false;
		TTTField other = (TTTField) obj;
		if (!Arrays.deepEquals(this.field, other.field)) return false;
		return true;
	}
	
}

class TTTSubField implements TTT<Boolean> {
	
	public final int          x;
	public final int          y;
	private final Boolean[][] field = new Boolean[3][3];
	
	public TTTSubField(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public Boolean value(int x, int y) {
		return this.field[x][y];
	}
	
	@Override
	public boolean place(int x, int y, Boolean val) {
		this.field[x][y] = val;
		return TTT.checkWon(this, Function.identity(), x, y);
	}
	
	@Override
	public TTTSubField copy() {
		TTTSubField c = new TTTSubField(this.x, this.y);
		for (int i = 0; i < 3; i++) {
			c.field[i][0] = this.field[i][0];
			c.field[i][1] = this.field[i][1];
			c.field[i][2] = this.field[i][2];
		}
		return c;
	}
	
	@Override
	public int hashCode() {
		final int prime  = 31;
		int       result = 1;
		result = prime * result + Arrays.deepHashCode(this.field);
		result = prime * result + this.x;
		result = prime * result + this.y;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TTTSubField)) return false;
		TTTSubField other = (TTTSubField) obj;
		if (!Arrays.deepEquals(this.field, other.field)) return false;
		if (this.x != other.x) return false;
		if (this.y != other.y) return false;
		return true;
	}
	
}

@SuppressWarnings("unused")
class TTTFinish<T> implements TTT<T> {
	
	private final Boolean winner;
	
	public TTTFinish(Boolean winner) {
		this.winner = winner;
	}
	
	@Override
	public T value(int x, int y) {
		throw new IllegalStateException("the game has already ended");
	}
	
	@Override
	public boolean place(int lx, int ly, Boolean val) {
		throw new IllegalStateException("the game has already ended");
	}
	
	@Override
	public Boolean winner() {
		return this.winner;
	}
	
	@Override
	public TTTFinish<T> copy() {
		return this;
	}
	
	@Override
	public int hashCode() {
		return this.winner.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TTTFinish)) return false;
		TTTFinish<?> other = (TTTFinish<?>) obj;
		return this.winner.equals(other.winner);
	}
	
}

interface TTT<T> {
	
	static final TTT<Boolean> ENEMY_WON = new TTTFinish<>(Boolean.FALSE);
	static final TTT<Boolean> ME_WON    = new TTTFinish<>(Boolean.TRUE);
	
	T value(int x, int y);
	
	boolean place(int lx, int ly, Boolean val);
	
	default Boolean winner() {
		return null;// NOSONAR
	}
	
	static <T> boolean checkWon(TTT<T> ttt, Function<T, Boolean> f, int x, int y) {// NOSONAR
		Boolean c = f.apply(ttt.value(x, y));
		// check horizontal
		// check vertical
		// check diagonal \
		// check diagonal /
		final int oc = (x << 4) | y;
		l0: {// NOSONAR
			for (int x2 = 0; x2 < 3; x2++) {
				Boolean b = f.apply(ttt.value(x2, y));
				if (!c.equals(b)) {
					break l0;
				}
			}
			return true;
		}
		l1: {// NOSONAR
			for (y = 0; y < 3; y++) {
				Boolean b = f.apply(ttt.value(x, y));
				if (!c.equals(b)) break l1;
			}
			return true;
		}
		l2: switch (oc) {// NOSONAR
		case 0x00, 0x11, 0x22:
			for (x = 0, y = 0; x < 3; x++, y++) {
				Boolean b = f.apply(ttt.value(x, y));
				if (!c.equals(b)) break l2;
			}
			return true;
		}
		l3: switch (oc) {// NOSONAR
		case 0x02, 0x11, 0x20:
			for (x = 0, y = 2; x < 3; x++, y--) {
				Boolean b = f.apply(ttt.value(x, y));
				if (!c.equals(b)) break l3;
			}
			return true;
		}
		return false;
	}
	
	TTT<T> copy();
	
}
