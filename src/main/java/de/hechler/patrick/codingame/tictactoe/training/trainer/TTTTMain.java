package de.hechler.patrick.codingame.tictactoe.training.trainer;

import de.hechler.patrick.codingame.tictactoe.training.env.Game;
import de.hechler.patrick.codingame.tictactoe.training.env.Player;

@SuppressWarnings("javadoc")
public class TTTTMain {
	
	public static void main(String[] args) {
		Trainer t = new Trainer();
		
		Tester test = new Tester() {
			
			@Override
			public int whoIsBetter(NeuralNet an, NeuralNet bn) {
				Player a = an.p;
				if (a == null) {
					a    = new NeuralPlayer(an);
					an.p = a;
				}
				Player b = bn.p;
				if (b == null) {
					b    = new NeuralPlayer(bn);
					bn.p = b;
				}
				Game g = new Game(a, b);
				return g.runGame();
			}
			
		};
		
		t.train(test, 100, NeuralPlayer.NEURAL_INPUT_COUNT, NeuralPlayer.NEURAL_OUTPUT_COUNT);
	}
	
}
