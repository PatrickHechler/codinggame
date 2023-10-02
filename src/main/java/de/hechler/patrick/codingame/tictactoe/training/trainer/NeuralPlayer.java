package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Arrays;

import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTField;
import de.hechler.patrick.codingame.tictactoe.training.env.TTTPos;


@SuppressWarnings("javadoc")
public class NeuralPlayer implements Player {
	
	public static class NeuralPlayerName extends NeuralPlayer {
		
		private final String name;
		
		public NeuralPlayerName(NeuralNet net, String name) {
			super(net);
			this.name = name;
		}
		
		@Override
		public InitilizedPlayer initGame(TTTField field) {
			return new InitNeuralPlayer.InitNeuralPlayerName(super.net, field, super.logTurns, this.name);
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
	}
	
	final NeuralNet net;
	private boolean logTurns;
	
	public NeuralPlayer(NeuralNet net) {
		this.net = net;
	}
	
	@Override
	public InitilizedPlayer initGame(TTTField field) {
		return new InitNeuralPlayer(this.net, field, this.logTurns);
	}
	
	public static final int NEURAL_INPUT_COUNT  = 9 + (9 * 9) + (9 * 10);
	public static final int NEURAL_OUTPUT_COUNT = (9 * 9);
	
	static class InitNeuralPlayer implements InitilizedPlayer {
		
		static class InitNeuralPlayerName extends InitNeuralPlayer {
			
			private final String name;
			
			public InitNeuralPlayerName(NeuralNet net, TTTField field, boolean logTurns, String name) {
				super(net, field, logTurns);
				this.name = name;
			}
			
			@Override
			public String toString() {
				return this.name;
			}
			
		}
		
		private final NeuralNet net;
		private final TTTField  field;
		private final boolean   logTurns;
		
		public InitNeuralPlayer(NeuralNet net, TTTField field, boolean logTurns) {
			this.net      = net;
			this.field    = field;
			this.logTurns = logTurns;
		}
		
		@Override
		public TTTPos doTurn(int outerX, int outerY, TTTPos lastEnemyTurn) {
			float[] arr = new float[NEURAL_INPUT_COUNT];
			if (outerX == -1) {
				Arrays.fill(arr, 0, 9, 1f);
			} else {
				arr[outerX * 3 + outerY] = 1f;
			}
			if (lastEnemyTurn != null) {
				arr[9 + (lastEnemyTurn.outerX * (3 * 3 * 3)) + (lastEnemyTurn.outerY * (3 * 3)) + (lastEnemyTurn.innerX * 3) + lastEnemyTurn.outerY] = 1f;
			} else {
				lastEnemyTurn = new TTTPos();
			}
			for (int x = 0, off = 9 + (9 * 9); x < 3; x++) {
				for (int y = 0; y < 3; y++) {
					Object obj = this.field.value(x, y);
					if (obj instanceof int[] a) {
						off++;
						for (int i = 0; i < 9; i++) {
							arr[off++] = switch (a[i]) {
							case 0 -> 0f;
							case -1 -> 0.6f;
							case 1 -> 1f;
							default -> throw new AssertionError(obj);
							};
						}
					} else {
						float val  = switch (((Integer) obj).intValue()) {
									case 0 -> 0.2f;
									case -1 -> 0.6f;
									case 1 -> 1f;
									default -> throw new AssertionError(obj);
									};
						int   noff = off + 10;
						Arrays.fill(arr, off, noff, val);
						off = noff;
					}
				}
			}
			arr = this.net.calculate(arr);
			int   maxi = -1;
			float maxv = Float.NEGATIVE_INFINITY;
			if (this.logTurns) {
				System.out.println("[" + this + "]: turn:");
				System.out.println("[" + this + "]:   [" + arr[0] + " ; " + arr[3] + " ; " + arr[6] + "] ;; [" + arr[27] + " ; " + arr[30] + " ; " + arr[33]
						+ "] ;; [" + arr[54] + " ; " + arr[57] + " ; " + arr[60] + "]");
				System.out.println("[" + this + "]:   [" + arr[1] + " ; " + arr[4] + " ; " + arr[7] + "] ;; [" + arr[28] + " ; " + arr[31] + " ; " + arr[34]
						+ "] ;; [" + arr[55] + " ; " + arr[58] + " ; " + arr[61] + "]");
				System.out.println("[" + this + "]:   [" + arr[3] + " ; " + arr[5] + " ; " + arr[8] + "] ;; [" + arr[29] + " ; " + arr[32] + " ; " + arr[35]
						+ "] ;; [" + arr[56] + " ; " + arr[59] + " ; " + arr[62] + "]");
				System.out.println("[" + this + "]:");
				System.out.println("[" + this + "]:   [" + arr[9] + " ; " + arr[12] + " ; " + arr[15] + "] ;; [" + arr[36] + " ; " + arr[39] + " ; " + arr[42]
						+ "] ;; [" + arr[63] + " ; " + arr[66] + " ; " + arr[69] + "]");
				System.out.println("[" + this + "]:   [" + arr[10] + " ; " + arr[13] + " ; " + arr[16] + "] ;; [" + arr[37] + " ; " + arr[40] + " ; " + arr[43]
						+ "] ;; [" + arr[64] + " ; " + arr[67] + " ; " + arr[70] + "]");
				System.out.println("[" + this + "]:   [" + arr[11] + " ; " + arr[14] + " ; " + arr[17] + "] ;; [" + arr[38] + " ; " + arr[41] + " ; " + arr[44]
						+ "] ;; [" + arr[65] + " ; " + arr[68] + " ; " + arr[71] + "]");
				System.out.println("[" + this + "]:");
				System.out.println("[" + this + "]:   [" + arr[18] + " ; " + arr[21] + " ; " + arr[24] + "] ;; [" + arr[45] + " ; " + arr[48] + " ; " + arr[51]
						+ "] ;; [" + arr[72] + " ; " + arr[75] + " ; " + arr[78] + "]");
				System.out.println("[" + this + "]:   [" + arr[19] + " ; " + arr[22] + " ; " + arr[25] + "] ;; [" + arr[46] + " ; " + arr[49] + " ; " + arr[52]
						+ "] ;; [" + arr[73] + " ; " + arr[76] + " ; " + arr[79] + "]");
				System.out.println("[" + this + "]:   [" + arr[20] + " ; " + arr[23] + " ; " + arr[26] + "] ;; [" + arr[47] + " ; " + arr[50] + " ; " + arr[53]
						+ "] ;; [" + arr[74] + " ; " + arr[77] + " ; " + arr[80] + "]");
			}
			for (int i = 0; i < arr.length; i++) {
				float val = arr[i];
				if (val < maxv) continue;
				maxi = i;
				maxv = val;
			} // (outX * 3*3*3) + (outY * 3*3) + (x * 3) + y
			lastEnemyTurn.innerY = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.innerX = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.outerY = maxi % 3;
			maxi                 = maxi / 3;
			lastEnemyTurn.outerX = maxi;
			return lastEnemyTurn;
		}
		
	}
	
	public void logTurns() {
		this.logTurns = true;
	}
	
}
