import java.util.Random;

public class FunctionCreator {
	static Random rd = new Random();

	static int[] create_random_indiv(int maxDepth, int numOfInputs, boolean returnBoolean) {
		int depth = maxDepth;
		int[] indiv = grow(depth, numOfInputs, returnBoolean);
		while (indiv.length > MasterVariables.MAX_LEN)
			indiv = grow(depth, numOfInputs, returnBoolean);
		return (indiv);
	}

	static int[] grow(int depth, int numOfInputs, boolean returnBoolean) {
		int[] buffer;
		if (returnBoolean) {
			int prim = rd.nextInt(2);
			if (prim == 0 || depth == 1) {
				int[] leftBuffer = grow(depth - 1, numOfInputs, false);
				int[] rightBuffer = grow(depth - 1, numOfInputs, false);
				buffer = new int[leftBuffer.length + rightBuffer.length + 1];
				prim = rd.nextInt(MasterVariables.FSET_2_END - MasterVariables.FSET_2_START + 1)
						+ MasterVariables.FSET_2_START;
				switch (prim) {
				case MasterVariables.GT:
				case MasterVariables.LT:
				case MasterVariables.EQ:
					buffer[0] = prim;
					System.arraycopy(leftBuffer, 0, buffer, 1, leftBuffer.length);
					System.arraycopy(rightBuffer, 0, buffer, (1 + leftBuffer.length), rightBuffer.length);
				}
			} else {
				int[] leftBuffer = grow(depth - 1, numOfInputs, true);
				int[] rightBuffer = grow(depth - 1, numOfInputs, true);
				buffer = new int[leftBuffer.length + rightBuffer.length + 1];
				prim = -1 * rd.nextInt((-1 * MasterVariables.FSET_3_END - MasterVariables.FSET_3_START - 1))
						+ MasterVariables.FSET_3_START;
				switch (prim) {
				case MasterVariables.AND:
				case MasterVariables.OR:
					buffer[0] = prim;
					System.arraycopy(leftBuffer, 0, buffer, 1, leftBuffer.length);
					System.arraycopy(rightBuffer, 0, buffer, (1 + leftBuffer.length), rightBuffer.length);
				}
			}
		} else {
			int prim = (int) rd.nextInt(2);
			if (prim == 0 || depth == 0) {
				prim = (int) rd.nextInt(numOfInputs);
				/*
				 * if (prim == 0) prim = MasterVariables.RANDOMNUMBERS +
				 * rd.nextInt(MasterVariables.numOfInputs); else prim =
				 * rd.nextInt(MasterVariables.RANDOMNUMBERS);
				 */
				buffer = new int[1];
				buffer[0] = prim;
			} else {
				int[] leftBuffer = grow(depth - 1, numOfInputs, false);
				int[] rightBuffer = grow(depth - 1, numOfInputs, false);
				buffer = new int[leftBuffer.length + rightBuffer.length + 1];
				prim = -1 * rd.nextInt(-1 * (MasterVariables.FSET_1_END - MasterVariables.FSET_1_START - 1))
						+ MasterVariables.FSET_1_START;
				switch (prim) {
				case MasterVariables.ADD:
				case MasterVariables.SUB:
				case MasterVariables.MUL:
				case MasterVariables.DIV:
					buffer[0] = prim;
					System.arraycopy(leftBuffer, 0, buffer, 1, leftBuffer.length);
					System.arraycopy(rightBuffer, 0, buffer, (1 + leftBuffer.length), rightBuffer.length);
				}
			}
		}

		return buffer;
	}

	private void checkErrors(int level, int popSize, int depth) {
		if (popSize < 1 || depth < 0) {
			System.out.println("StrategyCreator Class error!!!");
			System.exit(0);
		}
	}

	private static void checkErrors(int popSize, int depth) {
		if (popSize < 1 || depth < 0) {
			System.out.println("StrategyCreator Class error!!!");
			System.exit(0);
		}
	}
}
