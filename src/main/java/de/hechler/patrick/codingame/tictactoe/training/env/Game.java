package de.hechler.patrick.codingame.tictactoe.training.env;

import de.hechler.patrick.codingame.tictactoe.training.env.Player.InitilizedPlayer;

@SuppressWarnings("javadoc")
public class Game {
	
	private static final boolean LOG_TURNS     = false;
	private static final boolean TRUST_PLAYERS = true;
	private static final boolean SHOW_TRACE    = false;
	
	private final TTTField field;
	private final Player   playera;
	private final Player   playerb;
	
	private final boolean logTurns;
	private final boolean trustPlayers;
	private final boolean showTrace;
	
	public Game(Player playera, Player playerb) {
		this(playera, playerb, LOG_TURNS, TRUST_PLAYERS, SHOW_TRACE);
	}
	
	public Game(Player playera, Player playerb, boolean logTurns, boolean trustPlayers, boolean showTrace) {
		this.field        = new Field();
		this.playera      = playera;
		this.playerb      = playerb;
		this.logTurns     = logTurns;
		this.trustPlayers = trustPlayers;
		this.showTrace    = showTrace;
	}
	
	public Player playerA() {
		return this.playera;
	}
	
	public Player playerB() {
		return this.playerb;
	}
	
	public int runGame() {
		InitilizedPlayer a    = this.playera.initGame(this.trustPlayers ? this.field : this.field.readOnly());
		InitilizedPlayer b    = this.playerb.initGame(this.trustPlayers ? this.field.reverse() : this.field.reverse().readOnly());
		int              x    = -1;
		int              y    = -1;
		TTTPos           prev = null;
		while (true) {
			try {
				prev = a.doTurn(x, y, prev);
				if (this.logTurns) {
					System.out.println("[" + a + "]: " + prev);
				}
				if (x != -1 && (prev.outerX != x || prev.outerY != y)) {
					return fin(a, b, -2);
				}
				int res = this.field.place(prev, 1);
				if (res != 0) {
					if (res < 0) return fin(a, b, 0);
					return fin(a, b, 1);
				}
				x = prev.innerX;
				y = prev.innerY;
				if (this.field.subField(x, y) == null) {
					x = -1;
					y = -1;
				}
			} catch (Exception e) {
				if (this.showTrace) {
					e.printStackTrace();
				}
				return fin(a, b, -2);
			}
			try {
				prev = b.doTurn(x, y, prev);
				if (this.logTurns) {
					System.out.println("[" + b + "]: " + prev);
				}
				if (x != -1 && (prev.outerX != x || prev.outerY != y)) {
					return fin(a, b, 2);
				}
				int res = this.field.place(prev, -1);
				if (res != 0) {
					if (res < 0) return fin(a, b, 0);
					return fin(a, b, -1);
				}
				x = prev.innerX;
				y = prev.innerY;
				if (this.field.subField(x, y) == null) {
					x = -1;
					y = -1;
				}
			} catch (Exception e) {
				if (this.showTrace) {
					e.printStackTrace();
				}
				return fin(a, b, 2);
			}
		}
	}
	
	private static int fin(InitilizedPlayer a, InitilizedPlayer b, int res) {
		a.finish(res);
		b.finish(-res);
		return res;
	}
	
}
