package de.hechler.patrick.codingame.tictactoe.training.trainer;

import java.io.Serializable;
import java.util.Random;

import de.hechler.patrick.codingame.tictactoe.training.env.Player;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

@SuppressWarnings("javadoc")
public class NeuralNet implements Serializable {
	
	private static final long serialVersionUID = -7516615694977050732L;
	
	private static final VectorSpecies<Float> SPECS = FloatVector.SPECIES_PREFERRED;
	
	private static final int SPEC_LEN = SPECS.length();
	
	transient int    score;
	transient Player p;
	
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
		ns.inputMultiplicators = new float[layerSize][inputSize];
		ns.importanceMinimum   = new float[layerSize];
		ns.nodeMultiplicator   = new float[layerSize];
		for (int ii = 0; ii < ns.inputMultiplicators.length; ii++) {
			fillArr(rnd, ns.inputMultiplicators[ii]);
		}
		fillArr(rnd, ns.importanceMinimum);
		fillArr(rnd, ns.nodeMultiplicator);
		return ns;
	}
	
	public NeuralNet(Random rnd, NeuralNet orig, float randPropablility, float maxRandom) {
		this.inputCount = orig.inputCount;
		this.values     = new Nodes[orig.values.length];
		for (int i = 0; i < this.values.length; i++) {
			this.values[i]                   = new Nodes();
			this.values[i].importanceMinimum = orig.values[i].importanceMinimum.clone();
			this.values[i].nodeMultiplicator = orig.values[i].nodeMultiplicator.clone();
			for (int ii = 0; ii < this.values.length; ii++) {
				this.values[i].importanceMinimum[ii] = random1(rnd, randPropablility, maxRandom, this.values[i].importanceMinimum[ii]);
				this.values[i].nodeMultiplicator[ii] = random2(rnd, randPropablility, maxRandom, this.values[i].nodeMultiplicator[ii]);
			}
			this.values[i].inputMultiplicators = new float[orig.values[i].inputMultiplicators.length][];
			for (int ii = 0; ii < this.values.length; ii++) {
				this.values[i].inputMultiplicators[ii] = orig.values[i].inputMultiplicators[ii].clone();
				for (int iii = 0; iii < this.values[i].inputMultiplicators[ii].length; iii++) {
					this.values[i].inputMultiplicators[ii][iii] = random2(rnd, randPropablility, maxRandom, this.values[i].inputMultiplicators[ii][iii]);
				}
			}
		}
	}
	
	private static float random2(Random rnd, float randPropablility, float maxRandom, float value) {
		if (rnd.nextFloat() >= randPropablility) {
			return value;
		}
		float val0 = value + rnd.nextFloat(-maxRandom, maxRandom);
		if (val0 < 0f) return 0f;
		if (val0 > 2d) return 2f;
		return val0;
	}
	
	private static float random1(Random rnd, float randPropablility, float maxRandom, float value) {
		if (rnd.nextFloat() >= randPropablility) {
			return value;
		}
		float val0 = value + rnd.nextFloat(-maxRandom, maxRandom);
		if (val0 < 0f) return 0f;
		if (val0 > 1d) return 1f;
		return val0;
	}
	
	private static void fillArr(Random rnd, float[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = rnd.nextFloat();
		}
	}
	
	float[] calculate(float[] input) {// the final keyword is used like the const keyword in this method
		if (this.inputCount != input.length) {
			throw new IllegalArgumentException();
		}
		float[]      res        = null;
		final Nodes[] layers     = this.values;
		final int     layerCount = layers.length;
		for (int i = 0; i < layerCount; i++) {
			Nodes     n         = layers[i];
			final int nodecount = n.nodeMultiplicator.length;
			res = new float[nodecount];
			final float[][] inmuls = n.inputMultiplicators;
			for (int ii = 0; ii < nodecount; ii++) {
				res[ii] = 0f;
				final float[] curInmul    = inmuls[ii];
				final int      curInmulLen = curInmul.length;
				int            curInOff    = 0;
				for (final int inBound = SPECS.loopBound(curInmulLen); curInOff < inBound; curInOff += SPEC_LEN) {
					FloatVector ivec   = FloatVector.fromArray(SPECS, input, curInOff);
					FloatVector mvec   = FloatVector.fromArray(SPECS, curInmul, curInOff);
					FloatVector finvec = mvec.mul(ivec);
					res[ii] += finvec.reduceLanes(VectorOperators.ADD);
				}
				for (; curInOff < curInmulLen; curInOff++) {
					res[ii] += input[curInOff] * curInmul[curInOff];
				}
			}
			final float[] nodeMuls  = n.nodeMultiplicator;
			final float[] importMin = n.importanceMinimum;
			int            resOff    = 0;
			for (final int resBound = SPECS.loopBound(nodecount); resOff < resBound; resOff += SPEC_LEN) {
				FloatVector       rvec   = FloatVector.fromArray(SPECS, res, resOff);
				FloatVector       minvec = FloatVector.fromArray(SPECS, importMin, resOff);
				FloatVector       mulvec = FloatVector.fromArray(SPECS, nodeMuls, resOff);
				VectorMask<Float> mask   = rvec.lt(minvec);
				FloatVector       finvec = rvec.mul(mulvec, mask);
				finvec = finvec.blend(0f, mask);
				VectorMask<Float> mask2   = rvec.compare(VectorOperators.GT, 1f);
				finvec = finvec.blend(1f, mask2);
				finvec.intoArray(res, resOff);
			}
			for (; resOff < nodecount; resOff++) {
				float rval = res[resOff];
				if (rval < importMin[resOff]) {
					res[resOff] = 0f;
				} else {
					res[resOff] = rval * nodeMuls[resOff];
				}
			}
			input = res;
		}
		return res;
	}
	
	static class Nodes implements Serializable {
		
		private static final long serialVersionUID = 3012965959450914873L;
		
		float[][] inputMultiplicators;
		float[]   importanceMinimum;
		float[]   nodeMultiplicator;
		
	}
	
}
