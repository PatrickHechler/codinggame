package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;


@SuppressWarnings("javadoc")
public class Trainer {
	
	private static final int SOME_GENS = 0x1F;
	
	private static final float DEF_MAX_RND         = 0.0125f;
	private static final float DEF_RND_PROPAB      = 0.25f;
	private static final int   DEF_LAYER_SIZE      = 128;
	private static final int   DEF_LAYER_COUNT     = 64;
	private static final int   DEF_AGENT_CNT       = 1024;
	private static final int   DEF_DIRECT_SURIVORS = 32;
	
	private final Random rnd;
	private final float  maxRandom;
	private final float  randPropablility;
	private final int    layers;
	private final int    layerSize;
	private final int    agentCount;
	private final int    directSurivors;
	
	public Trainer(Random rnd) {
		this(rnd, DEF_MAX_RND, DEF_RND_PROPAB, DEF_LAYER_COUNT, DEF_LAYER_SIZE, DEF_AGENT_CNT, DEF_DIRECT_SURIVORS);
	}
	
	public Trainer(Random rnd, float maxRandom, float randPropablility, int layers, int layerSize, int agentCount, double directSurivors) {
		this(rnd, maxRandom, randPropablility, layers, layerSize, agentCount, (int) (agentCount * directSurivors));
	}
	
	public Trainer(Random rnd, float maxRandom, float randPropablility, int layers, int layerSize, int agentCount, int directSurivors) {
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
	
	interface TrainConsumer {
		
		void accept(NeuralNet currentBest, int generation);
		
	}
	
	public void train(Tester t, int inputCount, int outputCount, TrainConsumer best) {
		NeuralNet[] nets = new NeuralNet[this.agentCount];
		for (int i = 0; i < nets.length; i++) {
			nets[i] = new NeuralNet(this.rnd, inputCount, this.layers, this.layerSize, outputCount);
			if ((i & 0x3F) == 0x3F) {
				System.out.println(i);
			}
		}
		System.out.println("finished init, test/train now");
		printMeminfo();
		long start = System.currentTimeMillis();
		long last  = start;
		for (int g = 0;; g++) {
			Stream.of(nets).parallel().forEach(a -> {
				Stream.of(nets).forEach(b -> {
					if (a == b) return;
					int val = t.whoIsBetter(a, b);
					a.score += val;
					b.score -= val;
				});
			});
			Arrays.sort(nets, (a, b) -> Integer.compare(b.score, a.score));
			if ((g & SOME_GENS) == SOME_GENS) {
				best.accept(nets[0], g);
				long time = System.currentTimeMillis();
				System.out.println("It took " + (time - last) + " ms to test generations " + (g - SOME_GENS) + " .. " + g + " (a total of " + (time - start)
						+ " ms) (scores are from " + nets[nets.length - 1].score + " to " + nets[0].score + ")");
				printMeminfo();
				last = time;
			}
			Arrays.fill(nets, this.directSurivors, nets.length - this.directSurivors, null);
			fillRest(nets);
		}
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
			nets[i] = new NeuralNet(this.rnd, orig, this.randPropablility, this.maxRandom);
		}
	}
	
}
