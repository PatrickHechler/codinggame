package de.hechler.patrick.codingame.tictactoe.training;


@SuppressWarnings("javadoc")
public interface Player {
	
	void initGame(TTTField field);
	
	TTTPos doTurn(int outerX, int outerY, TTTPos lastEnemyTurn);
	
}
