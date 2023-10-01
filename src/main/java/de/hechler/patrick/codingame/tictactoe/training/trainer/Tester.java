package de.hechler.patrick.codingame.tictactoe.training.trainer;


@FunctionalInterface
@SuppressWarnings("javadoc")
public interface Tester {
	
	int whoIsBetter(NeuralNet a, NeuralNet b);
	
}
