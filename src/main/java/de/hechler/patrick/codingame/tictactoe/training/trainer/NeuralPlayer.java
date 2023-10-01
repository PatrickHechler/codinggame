package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Arrays;

import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTField;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTPos;


@SuppressWarnings("javadoc")
public class NeuralPlayer implements Player {
	
	private final NeuralNet net;
	
	public NeuralPlayer(NeuralNet net) {
		this.net = net;
	}
	
	@Override
	public InitilizedPlayer initGame(TTTField field) {
		return new InitNeuralPlayer(this.net, field);
	}
	
	public static final int NEURAL_INPUT_COUNT  = 9 + (9 * 9) + (9 * 10);
	public static final int NEURAL_OUTPUT_COUNT = (9 * 9);
	
	static class InitNeuralPlayer implements InitilizedPlayer {
		
		private final NeuralNet net;
		private final TTTField  field;
		
		public InitNeuralPlayer(NeuralNet net, TTTField field) {
			this.net   = net;
			this.field = field;
		}
		
		@Override
		public TTTPos doTurn(int outerX, int outerY, TTTPos lastEnemyTurn) {
			double[] arr = new double[NEURAL_INPUT_COUNT];
			if (outerX == -1) {
				Arrays.fill(arr, 0, 9, 1d);
			} else {
				arr[outerX * 3 + outerY] = 1d;
			}
			if (lastEnemyTurn != null) {
				arr[9 + (lastEnemyTurn.outerX * 3 + lastEnemyTurn.outerY) * 3 + lastEnemyTurn.innerX * 3 + lastEnemyTurn.outerY] = 1d;
			} else {
				lastEnemyTurn = new TTTPos();
			}
			for (int x = 0, off = 9 + (9 * 9); x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					Object obj = this.field.value(x, y);
					if (obj instanceof int[] a) {
						off++;
						for (int i = 0; i < a.length; i++) {
							arr[off++] = switch (a[i]) {
							case 0 -> 0;
							case -1 -> 0.6d;
							case 1 -> 1;
							default -> throw new AssertionError(obj);
							};
						}
					} else {
						double val  = switch (((Integer) obj).intValue()) {
									case 0 -> 0.2;
									case -1 -> 0.6d;
									case 1 -> 1;
									default -> throw new AssertionError(obj);
									};
						int    noff = off + 10;
						Arrays.fill(arr, off, noff, val);
						off = noff;
					}
				}
			}
			arr = this.net.calculate(arr);
			int    maxi = -1;
			double maxv = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < arr.length; i++) {
				double val = arr[i];
				if (val < maxv) continue;
				maxi = i;
				maxv = val;
			}
			lastEnemyTurn.innerY = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.innerX = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.outerY = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.outerX = maxi % 3;
			return lastEnemyTurn;
		}
		
	}
	
}
