package v2;

import java.util.Random;

// MASTER VARIABLES CLASS
public final class MV {

	static final boolean RUNFOREVER = true, BOOLEAN_OUTPUT = false;
	static final int maxSimRounds = 1000000, MAX_NEW_FUNC_LEN = 10000, MAX_SUB_MODULES = 5, MAX_NEW_FUNC_DEPTH = 5,
			TSIZE = 2, POPSIZE_PER_CASE_LEVEL_DEME = 100, SAMPLE_SIZE = 100,
			POPSIZE_PER_LEVEL_DEME = POPSIZE_PER_CASE_LEVEL_DEME * SAMPLE_SIZE, RANDOMNUMBERS = 50, DEMES = 10,
			LEVELS = 5, REQUIRED_IMPROVEMENTS_ROUNDS = 10, IMPROVEMENT_FACTOR = 2, MIN_ROUNDS = 10,
			EVO_ROUNDS_PER_CASE = 1000, REPLACE_TRIALS = 10;

	static final double MIN_PROB = 0.1;

	// FUNCTIONS THAT TAKE A DOUBLE AND RETURNS A DOUBLE
	static final int ABS = -1, ACOS = ABS - 1, ASIN = ACOS - 1, ATAN = ASIN - 1, CBRT = ATAN - 1, CEIL = CBRT - 1,
			COS = CEIL - 1, COSH = COS - 1, EXP = COSH - 1, EXPM1 = EXP - 1, FLOOR = EXPM1 - 1, LOG = FLOOR - 1,
			LOG10 = LOG - 1, LOG1P = LOG10 - 1, GETEXP = LOG1P - 1, NEXTUP = GETEXP - 1, RINT = NEXTUP - 1,
			SIGNUM = RINT - 1, SIN = SIGNUM - 1, SINH = SIN - 1, SQRT = SINH - 1, TAN = SQRT - 1, TANH = TAN - 1,
			TODEGREES = TANH - 1, TORADIANS = TODEGREES - 1, ULP = TORADIANS - 1;
	static final int FSET_1_START = ABS, FSET_1_END = ULP;

	// FUNCTIONS THAT TAKE TWO DOUBLES AND RETURNS DOUBLE
	static final int ADD = FSET_1_END - 1, SUB = ADD - 1, MUL = SUB - 1, DIV = MUL - 1, MOD = DIV - 1, POW = MOD - 1,
			ATAN2 = POW - 1, COPYSIGN = ATAN2 - 1, HYPOT = COPYSIGN - 1, IEEEREMAINDER = HYPOT - 1,
			NEXTAFTER = IEEEREMAINDER - 1, MAX = NEXTAFTER - 1, MIN = MAX - 1;
	static final int FSET_2_START = ADD, FSET_2_END = MIN;

	// FUNCTIONS THAT TAKE AN ARRAY AND RETURN DOUBLE
	static final int MEAN = FSET_2_END - 1, MEDIAN = MEAN - 1, P25 = MEDIAN - 1, P75 = P25 - 1, MAX_AR = P75 - 1,
			MIN_AR = MAX_AR - 1;
	static final int FSET_3_START = MEAN, FSET_3_END = MIN_AR;

	// FUNCTIONS THAT TAKE AN ARRAY AND DOUBLE AND RETURN ARRAY
	static final int ARR_GT = FSET_3_END - 1, ARR_LT = ARR_GT - 1;
	static final int FSET_4_START = ARR_GT, FSET_4_END = ARR_LT;

	// FUNCTIONS THAT TAKE TWO ARRAYS AND RETURN ONE ARRAY
	static final int ARR_COMBINE = FSET_4_END - 1;
	static final int FSET_5_START = ARR_COMBINE, FSET_5_END = ARR_COMBINE;

	// FUNCTIONS THAT TAKE THREE DOUBLE AND RETURN ONE ARRAY
	static final int ARR_CREATE = FSET_5_END - 1;
	static final int FSET_6_START = ARR_CREATE, FSET_6_END = ARR_CREATE;

	// FUNCTIONS THAT TAKE TWO DOUBLES AND RETURN BOOLEAN
	static final int GT = FSET_6_END - 1, LT = GT - 1, EQ = LT - 1;
	static final int FSET_7_START = GT, FSET_7_END = EQ;

	// FUNCTIONS THAT TAKE TWO BOOLEANS AND RETURN BOOLEAN
	static final int AND = FSET_7_END - 1, OR = AND - 1, XOR = OR - 1, XNOR = XOR - 1, NAND = XNOR - 1;
	static final int FSET_8_START = AND, FSET_8_END = NAND;

	static double SUB_MUT_PROB, CLIQUE_BORROW_PROB, SUBSUME_PROB;
	static double TRIGGER_CREATE_PROB, TRIGGER_REPLICATE_PROB, TRIGGER_COMBINE_PROB;
	static int numOfInputs, numOfConstants, numOfFitnessCases, primitiveInputsLength, currentDeme, currentLevel,
			currentRound;
	static double[] primitiveInputs;

	static Module[][][] masterModules = new Module[LEVELS][DEMES][POPSIZE_PER_LEVEL_DEME];
	static Trigger[][][] masterTriggers = new Trigger[LEVELS][DEMES][POPSIZE_PER_LEVEL_DEME];
	static Module[] primitiveModules;

	static double[][] fitnessCases;
	static double currentOutput;
	static Random rd = new Random();

	public static void initialize(int numOfInputs, double[][] fitnessCases) {
		MV.numOfInputs = numOfInputs;
		MV.numOfFitnessCases = fitnessCases.length;
		MV.fitnessCases = fitnessCases;

		numOfConstants = RANDOMNUMBERS + 5;
		primitiveInputsLength = numOfConstants + numOfInputs;

		primitiveInputs = new double[primitiveInputsLength];
		primitiveInputs[0] = -1;
		primitiveInputs[1] = 0;
		primitiveInputs[2] = Math.E;
		primitiveInputs[3] = Math.PI;
		primitiveInputs[4] = -987654321.123456789;
		Random rd = new Random();
		for (int i = 5; i < numOfConstants; i++)
			// create random numbers between -10 and 10
			primitiveInputs[i] = 20 * (rd.nextDouble() - 1) + 10;

		primitiveModules = new Module[primitiveInputsLength];
		for (int i = 0; i < primitiveInputsLength; i++) {
			primitiveModules[i] = new Module(i);
		}
	}

	static void retreiveRandommInput() {
		int index = rd.nextInt(numOfFitnessCases);
		for (int i = numOfConstants; i < primitiveInputsLength; i++) {
			primitiveInputs[i] = fitnessCases[index][i - numOfConstants];
		}
		currentOutput = fitnessCases[index][numOfInputs];
	}

	public static void retreiveInput(int index) {
		for (int i = numOfConstants; i < primitiveInputsLength; i++) {
			primitiveInputs[i] = fitnessCases[index][i - numOfConstants];
		}
		currentOutput = fitnessCases[index][numOfInputs];
	}
}
