package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Random;

import de.hechler.patrick.codingame.tictactoe.training.GUIPlayer;
import de.hechler.patrick.codingame.tictactoe.training.env.Game;
import de.hechler.patrick.codingame.tictactoe.training.env.Player;

@SuppressWarnings("javadoc")
public class TTTTMain {
	
	public static void main(String[] args) {
		Trainer t = new Trainer(new Random());
		Tester test = new TesterImpl();
		NeuralNet[] val = t.train(test, 1 << 10, NeuralPlayer.NEURAL_INPUT_COUNT, NeuralPlayer.NEURAL_OUTPUT_COUNT);
		Player human = f -> new GUIPlayer(f, "human").load();
		if (val[0].p == null) {
			 val[0].p = new NeuralPlayer(val[0]);
		}
		Game g = new Game(human, val[0].p);
		g.runGame();
		g = new Game(val[0].p, human);
		g.runGame();
	}
	
	static class TesterImpl implements Tester {

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
		
	}
	
}
