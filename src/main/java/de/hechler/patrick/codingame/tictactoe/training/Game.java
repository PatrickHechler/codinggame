package de.hechler.patrick.codingame.tictactoe.training;


@SuppressWarnings("javadoc")
public class Game {
	
	private final TTTField field;
	private final Player   playera;
	private final Player   playerb;
	
	public Game(Player playera, Player playerb) {
		this.field  = new Field();
		this.playera = playera;
		this.playerb = playerb;
	}
	
	public Player playerA() {
		return this.playera;
	}
	
	public Player playerB() {
		return this.playerb;
	}
	
	public int runGame() {
		this.playera.initGame(this.field.readOnly());
		this.playerb.initGame(this.field.reverse().readOnly());
		int x = -1;
		int y = -1;
		TTTPos prev = null;
		while (true) {
			try {
				prev = this.playera.doTurn(x, y, prev);
				if (x != -1 && prev.outerX != x || prev.outerY != y) {
					return -2;
				}
				int res = this.field.place(prev, 1);
				if (res != 0) {
					if (res < 0) return 0;
					return 1;
				}
				x = prev.innerX;
				y = prev.innerY;
				if (this.field.subField(x, y) == null) {
					x = -1;
					y = -1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return -2;
			}
			try {
				prev = this.playerb.doTurn(x, y, prev);
				if (x != -1 && prev.outerX != x || prev.outerY != y) {
					return 2;
				}
				int res = this.field.place(prev, 1);
				if (res != 0) {
					if (res < 0) return 0;
					return -1;
				}
				x = prev.innerX;
				y = prev.innerY;
				if (this.field.subField(x, y) == null) {
					x = -1;
					y = -1;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return 2;
			}
		}
	}
	
}
