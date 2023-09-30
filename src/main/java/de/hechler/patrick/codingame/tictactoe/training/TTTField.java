package de.hechler.patrick.codingame.tictactoe.training;


@SuppressWarnings("javadoc")
public interface TTTField {

	Object value(int x, int y);

	int[] subField(int x, int y);
	
	int winner(int x, int y);
	
	int place(TTTPos pos, int val);

	TTTField copy();

	TTTField reverse();
	
	default TTTField readOnly() {
		return new ReadOnlyField(this);
	}
	
	interface TriIntFunction<A> {
		
		int apply(A arg0, int arg1, int arg2);
		
	}
	
	static <A> boolean checkWon(TriIntFunction<A> ttt, A arg, int x, int y) {// NOSONAR
		int c = ttt.apply(arg, x, y);
		// check horizontal
		// check vertical
		// check diagonal \
		// check diagonal /
		final int oc = (x << 4) | y;
		l0: {// NOSONAR
			for (int x2 = 0; x2 < 3; x2++) {
				int b = ttt.apply(arg, x2, y);
				if (c != b) {
					break l0;
				}
			}
			return true;
		}
		l1: {// NOSONAR
			for (y = 0; y < 3; y++) {
				int b = ttt.apply(arg, x, y);
				if (c != b) break l1;
			}
			return true;
		}
		l2: switch (oc) {// NOSONAR
		case 0x00, 0x11, 0x22:
			for (x = 0, y = 0; x < 3; x++, y++) {
				int b = ttt.apply(arg, x, y);
				if (c != b) break l2;
			}
			return true;
		}
		l3: switch (oc) {// NOSONAR
		case 0x02, 0x11, 0x20:
			for (x = 0, y = 2; x < 3; x++, y--) {
				int b = ttt.apply(arg, x, y);
				if (c != b) break l3;
			}
			return true;
		}
		return false;
	}
	
}
