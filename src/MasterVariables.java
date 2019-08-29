import java.util.Random;
import java.util.concurrent.ExecutorService;

public final class MasterVariables {

	static final int MASTER_GENERATIONS = 1000000, MAX_LEN = 10000, MAX_MODULES = 5, DEPTH = 5, TSIZE = 2,
			POPSIZE = 100000, demes = 10;
	static final int RANDOMNUMBERS = 50, ADD = -1, SUB = ADD - 1, MUL = ADD - 2, DIV = ADD - 3, GT = ADD - 4,
			LT = ADD - 5, EQ = ADD - 6, AND = ADD - 7, OR = ADD - 8, FSET_1_START = ADD, FSET_1_END = DIV,
			FSET_2_START = GT, FSET_2_END = EQ, FSET_3_START = AND, FSET_3_END = OR;
	static double SUB_MUT_PROB, BORROW_PROB, SUBSUME_PROB;
	static double submutFitness, borrowFitness, subsmeFitness;
	static final double MIN_PROB= 0.05;
	static final Random rd = new Random();
	static int numOfInputs, numOfOutputs, primitiveInputsLength, currentLevel, currentDeme;
	static double[] primitiveInputs;
	static Module[][][] masterModules;
	static Module[] primitiveModules;
	static double[][] data;

	void initialize(int numOfInputs, int numOfOutputs, double[][] data) {
		this.numOfInputs = numOfInputs;
		this.numOfOutputs = numOfOutputs;
		this.data = data;
		primitiveInputsLength = RANDOMNUMBERS + numOfInputs;
		primitiveInputs = new double[primitiveInputsLength];
		primitiveInputs[0] = -1;
		primitiveInputs[1] = 0;
		primitiveInputs[2] = Math.E;
		primitiveInputs[3] = Math.PI;
		for (int i = 4; i < RANDOMNUMBERS; i++) {
			// create random numbers between -10 and 10
			primitiveInputs[i] = 20 * (rd.nextDouble() - 1) + 10;
		}

		primitiveModules = new Module[primitiveInputsLength];
		for (int i = 0; i < primitiveInputsLength; i++) {
			primitiveModules[i] = new Module(i);
		}

		masterModules = new Module[MAX_MODULES][demes][];

		evoOperationsProbInit();
	}

	static void evoOperationsProbInit() {
		SUB_MUT_PROB = 1 / 3;
		BORROW_PROB = 1 / 3;
		SUBSUME_PROB = 1 -  SUB_MUT_PROB - BORROW_PROB;
	}

	void retreiveInputs(int index) {
		for (int i = RANDOMNUMBERS; i < primitiveInputsLength; i++) {
			primitiveInputs[i] = data[index][i - RANDOMNUMBERS];
			// System.out.println(primitiveInputs[i]);
			// if (index==62)
			// System.exit(0);
		}
	}

}
