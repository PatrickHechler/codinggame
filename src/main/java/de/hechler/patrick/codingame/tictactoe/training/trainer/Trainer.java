package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;


@SuppressWarnings("javadoc")
public class Trainer {
	
	private static final double DEF_MAX_RND         = 0.0125;
	private static final double DEF_RND_PROPAB      = 0.025;
	private static final int    DEF_LAYER_SIZE      = 1 << 6;
	private static final int    DEF_LAYER_COUNT     = 1 << 6;
	private static final int    DEF_AGENT_CNT       = 1 << 7;
	private static final int    DEF_DIRECT_SURIVORS = 1 << 4;
	
	private final Random rnd;
	private final double maxRandom;
	private final double randPropablility;
	private final int    layers;
	private final int    layerSize;
	private final int    agentCount;
	private final int    directSurivors;
	
	public Trainer(Random rnd) {
		this(rnd, DEF_MAX_RND, DEF_RND_PROPAB, DEF_LAYER_COUNT, DEF_LAYER_SIZE, DEF_AGENT_CNT, DEF_DIRECT_SURIVORS);
	}
	
	public Trainer(Random rnd, double maxRandom, double randPropablility, int layers, int layerSize, int agentCount, double directSurivors) {
		this(rnd, maxRandom, randPropablility, layers, layerSize, agentCount, (int) (agentCount * directSurivors));
	}
	
	public Trainer(Random rnd, double maxRandom, double randPropablility, int layers, int layerSize, int agentCount, int directSurivors) {
		if (agentCount < directSurivors) {
			throw new IllegalArgumentException("more direct surivors than agents");
		}
		if (maxRandom <= 0 || maxRandom >= 1d) {
			throw new IllegalArgumentException("max random not between 0 and 1");
		}
		if (layers < 2 || layerSize < 2) {
			throw new IllegalArgumentException("layerSize/Count must be at least 2");
		}
		this.rnd              = rnd;
		this.layers           = layers;
		this.layerSize        = layerSize;
		this.maxRandom        = maxRandom;
		this.randPropablility = randPropablility;
		this.agentCount       = agentCount;
		this.directSurivors   = directSurivors;
	}
	
	public NeuralNet[] train(Tester t, int generations, int inputCount, int outputCount) {
		NeuralNet[] nets = new NeuralNet[this.agentCount];
		for (int i = 0; i < nets.length; i++) {
			nets[i] = new NeuralNet(this.rnd, inputCount, this.layers, this.layerSize, outputCount);
		}
		System.out.println("finished init, test/train now");
		printMeminfo();
		long start = System.currentTimeMillis();
		long last  = start;
		for (int g = 0; g < generations; g++) {
			Stream.of(nets).parallel().forEach(a -> {
				Stream.of(nets).forEach(b -> {
					if (a == b) return;
					int val = t.whoIsBetter(a, b);
					a.score += val;
					b.score -= val;
				});
			});
			Arrays.sort(nets, (a, b) -> Integer.compare(b.score, a.score));
			Arrays.fill(nets, this.directSurivors, nets.length - this.directSurivors, null);
			fillRest(nets);
			if ((g & 0x3F) == 0x3F) {
				long time = System.currentTimeMillis();
				System.out.println("It took " + (time - last) + " ms to test generations " + (g - 0x3F) + " .. " + g);
				printMeminfo();
				last = time;
			}
		}
		System.out.println("It took " + (System.currentTimeMillis() - start) + " ms to test all generations");
		printMeminfo();
		return nets;
	}
	
	private static final boolean PRINT_MEMINFO = false;
	
	private static void printMeminfo() {
		if (PRINT_MEMINFO) {
			System.out.println("  free mem:  " + Runtime.getRuntime().freeMemory());
			System.out.println("  total mem: " + Runtime.getRuntime().totalMemory());
			System.out.println("  max mem:   " + Runtime.getRuntime().maxMemory());
		}
	}
	
	private void fillRest(NeuralNet[] nets) {
		for (int i = this.directSurivors; i < nets.length; i++) {
			NeuralNet orig = nets[this.rnd.nextInt(i >> 1)];
			nets[i] = new NeuralNet(orig, this.randPropablility, this.maxRandom);
		}
	}
	
}
