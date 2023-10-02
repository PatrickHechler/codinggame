package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.hechler.patrick.codingame.tictactoe.training.GUIPlayer;
import de.hechler.patrick.codingame.tictactoe.training.env.Game;
import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import de.hechler.patrick.codingame.tictactoe.training.trainer.Trainer.TrainConsumer;

@SuppressWarnings("javadoc")
public class TTTTMain {
	
	static final class Acceptor implements TrainConsumer, Runnable {
		
		private static volatile NeuralNet best;
		private static volatile int       generation;
		
		@Override
		public void accept(NeuralNet t, int gen) {
			best       = t;
			generation = gen;
			if ((gen & 0x3F) == 0x3F) {
				SwingUtilities.invokeLater(() -> {
					try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream("./ttt-saves/gen-" + gen + ".save"))) {
						oo.writeObject(t);
					} catch (IOException e) {
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(null, "the generation " + gen + " is now trained and ready to play against you", "another 256 generations trained",
							JOptionPane.INFORMATION_MESSAGE);
				});
			}
		}
		
		@Override
		public void run() {
			NeuralPlayer bot = null;
			while (best == null) {
				try {
					Thread.sleep(100L);
				} catch (@SuppressWarnings("unused") InterruptedException e) {
					return;
				}
			}
			while (true) {
				bot = new NeuralPlayer.NeuralPlayerName(best, "gen-" + generation);
				bot.logTurns();
				JOptionPane.showMessageDialog(null, "figth now against " + bot, "new enemy", JOptionPane.INFORMATION_MESSAGE);
				String name  = "human against " + bot;
				Player human = f -> new GUIPlayer(f, name).load();
				Game   g     = new Game(human, bot);
				g.runGame();
				g = new Game(bot, human);
				g.runGame();
			}
		}
		
	}
	
	public static void main(String[] args) {
		Trainer  t    = new Trainer(new Random());
		Tester   test = new TesterImpl();
		Acceptor acc  = new Acceptor();
		Thread.ofVirtual().start(acc);
		t.train(test, NeuralPlayer.NEURAL_INPUT_COUNT, NeuralPlayer.NEURAL_OUTPUT_COUNT, acc);
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
