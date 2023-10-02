package de.hechler.patrick.codingame.tictactoe.training.env;


@FunctionalInterface
@SuppressWarnings("javadoc")
public interface Player {
	
	InitilizedPlayer initGame(TTTField field);
	
	@FunctionalInterface
	interface InitilizedPlayer {
		
		TTTPos doTurn(int outerX, int outerY, TTTPos lastEnemyTurn);
		
		default void finish(@SuppressWarnings("unused") int won) {/**/}
	
	}

}
