package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

@SuppressWarnings("javadoc")
public class NeuralNet {
	
	private static final VectorSpecies<Double> SPECS = DoubleVector.SPECIES_PREFERRED;
	
	private static final int SPEC_LEN = SPECS.length();
	
	int    score;
	Player p;
	
	int     inputCount;
	Nodes[] values;
	
	public NeuralNet(Random rnd, int inputCount, int layerCount, int layerSize, int outputCount) {
		this.inputCount = inputCount;
		this.values     = new Nodes[layerCount];
		this.values[0]  = initNs(rnd, inputCount, layerSize);
		for (int i = 1; i < this.values.length - 1; i++) {
			this.values[i] = initNs(rnd, layerSize, layerSize);
		}
		this.values[this.values.length - 1] = initNs(rnd, layerSize, outputCount);
	}
	
	private static Nodes initNs(Random rnd, int inputSize, int layerSize) {
		Nodes ns = new Nodes();
		ns.inputMultiplicators = new double[layerSize][inputSize];
		ns.importanceMinimum   = new double[layerSize];
		ns.nodeMultiplicator   = new double[layerSize];
		for (int ii = 0; ii < ns.inputMultiplicators.length; ii++) {
			fillArr(rnd, ns.inputMultiplicators[ii]);
		}
		fillArr(rnd, ns.importanceMinimum);
		fillArr(rnd, ns.nodeMultiplicator);
		return ns;
	}
	
	public NeuralNet(NeuralNet orig, double randPropablility, double maxRandom) {
		this.inputCount = orig.inputCount;
		this.values     = new Nodes[orig.values.length];
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		for (int i = 0; i < this.values.length; i++) {
			this.values[i]                   = new Nodes();
			this.values[i].importanceMinimum = orig.values[i].importanceMinimum.clone();
			this.values[i].nodeMultiplicator = orig.values[i].nodeMultiplicator.clone();
			for (int ii = 0; ii < this.values.length; ii++) {
				this.values[i].importanceMinimum[ii] = random(rnd, randPropablility, maxRandom, this.values[i].importanceMinimum[ii]);
				this.values[i].nodeMultiplicator[ii] = random(rnd, randPropablility, maxRandom, this.values[i].nodeMultiplicator[ii]);
			}
			this.values[i].inputMultiplicators = new double[orig.values[i].inputMultiplicators.length][];
			for (int ii = 0; ii < this.values.length; ii++) {
				this.values[i].inputMultiplicators[ii] = orig.values[i].inputMultiplicators[ii].clone();
				for (int iii = 0; iii < this.values[i].inputMultiplicators[ii].length; iii++) {
					this.values[i].inputMultiplicators[ii][iii] = random(rnd, randPropablility, maxRandom, this.values[i].inputMultiplicators[ii][iii]);
				}
			}
		}
	}
	
	private static double random(ThreadLocalRandom rnd, double randPropablility, double maxRandom, double value) {
		if (rnd.nextDouble() >= randPropablility) {
			return value;
		}
		return Math.min(0d, value + rnd.nextDouble(-maxRandom, maxRandom));
	}
	
	private static void fillArr(Random rnd, double[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = rnd.nextDouble();
		}
	}
	
	double[] calculate(double[] input) {// the final keyword is used like the const keyword in this method
		if (this.inputCount != input.length) {
			throw new IllegalArgumentException();
		}
		double[] res      = null;
		final Nodes[] layers = this.values;
		final int      layerCount = layers.length;
		for (int i = 0; i < layerCount; i++) {
			Nodes     n         = layers[i];
			final int nodecount = n.nodeMultiplicator.length;
			res = new double[nodecount];
			final double[][] inmuls = n.inputMultiplicators;
			for (int ii = 0; ii < nodecount; ii++) {
				res[ii] = 0d;
				final double[] curInmul    = inmuls[ii];
				final int      curInmulLen = curInmul.length;
				int            curInOff    = 0;
				for (final int inBound = SPECS.loopBound(curInmulLen); curInOff < inBound; curInOff += SPEC_LEN) {
					DoubleVector ivec   = DoubleVector.fromArray(SPECS, input, curInOff);
					DoubleVector mvec   = DoubleVector.fromArray(SPECS, curInmul, curInOff);
					DoubleVector finvec = mvec.mul(ivec);
					res[ii] += finvec.reduceLanes(VectorOperators.ADD);
				}
				for (; curInOff < curInmulLen; curInOff++) {
					res[ii] += input[curInOff] * curInmul[curInOff];
				}
			}
			final double[] nodeMuls  = n.nodeMultiplicator;
			final double[] importMin = n.importanceMinimum;
			int            resOff    = 0;
			for (final int resBound = SPECS.loopBound(nodecount); resOff < resBound; resOff += SPEC_LEN) {
				DoubleVector       rvec   = DoubleVector.fromArray(SPECS, res, resOff);
				DoubleVector       minvec = DoubleVector.fromArray(SPECS, importMin, resOff);
				DoubleVector       mulvec = DoubleVector.fromArray(SPECS, nodeMuls, resOff);
				VectorMask<Double> mask   = rvec.lt(minvec);
				DoubleVector       finvec = rvec.mul(mulvec, mask);
				finvec = finvec.blend(0d, mask);
				finvec.intoArray(res, resOff);
			}
			for (; resOff < nodecount; resOff++) {
				double rval = res[resOff];
				if (rval < importMin[resOff]) {
					res[resOff] = 0d;
				} else {
					res[resOff] = rval * nodeMuls[resOff];
				}
			}
			input = res;
		}
		return res;
	}
	
	static class Nodes {
		
		double[][] inputMultiplicators;
		double[]   importanceMinimum;
		double[]   nodeMultiplicator;
		
	}
	
}
