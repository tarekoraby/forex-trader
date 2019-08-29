import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.StringTokenizer;

public class MainClass {

	static MasterVariables mvClass = new MasterVariables();
	static Random rd = new Random();
	static FunctionCreator FC = new FunctionCreator();
	static double bestFit = -1.0e34;
	static int fitIndex, levelGeneration, prevLevel = -1, prevDeme = -1;
	static double[] prevBestFit = new double[10];
	static Evolver evolver = new Evolver();

	public static void main(String[] args) {
		initialize();
		simulate();
	}

	private static void simulate() {
		System.out.println("Start of program");
		levelGeneration = 0;
		initializeLevel(mvClass.currentLevel);

		for (int i = 0; i < mvClass.MASTER_GENERATIONS; i++) {
			bestFit = -1.0e34;
			evolver.evolve();
			findBesdFit();
			printStats();
			levelGeneration++;
			if (levelGeneration > prevBestFit.length) {
				if (bestFit * 2 > prevBestFit[levelGeneration % prevBestFit.length]) {
					prevBestFit[levelGeneration % prevBestFit.length] = bestFit;
				} else {
					//purify(prevBestFit.length);
					mvClass.currentDeme++;
					levelGeneration = 0;
					if (mvClass.currentDeme == mvClass.demes) {
						mvClass.currentDeme = 0;
						mvClass.currentLevel++;
						if (mvClass.currentLevel == mvClass.MAX_MODULES) {
							System.out.println("End of program");
							System.exit(0);
						}
					}
					initializeLevel(mvClass.currentLevel);
					for (int j = 0; j < prevBestFit.length; j++)
						prevBestFit[j] = -1.0e34;
				}
			} else {
				prevBestFit[levelGeneration % prevBestFit.length] = bestFit;
			}
		}
	}

	private static void purify(int rounds) {
		for (int i = 0; i < rounds; i++) {
			for (int j = 0; j < mvClass.POPSIZE; j++) {
				int positive = evolver.tournament(mvClass.TSIZE,
						mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
				int negative = evolver.negativeTournament(mvClass.TSIZE,
						mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
				mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][negative] = mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][positive];
			}
		}
	}

	private static void initializeLevel(int level) {
		mvClass.masterModules[level][mvClass.currentDeme] = new Module[mvClass.POPSIZE];

		FunctionCreator FC = new FunctionCreator();
		int index, rand1, rand2;
		for (int i = 0; i < mvClass.POPSIZE; i++) {
			int size = 1 + rd.nextInt(mvClass.MAX_MODULES);
			Module[] subModules = new Module[size];
			index = mvClass.RANDOMNUMBERS + rd.nextInt(mvClass.numOfInputs);
			subModules[0] = mvClass.primitiveModules[index].deepCopy();
			for (int j = 1; j < size; j++) {
				rand1 = rd.nextInt(2);
				if (rand1 == 0 || level == 0) {
					rand1 = rd.nextInt(2);
					if (rand1 == 0)
						index = mvClass.RANDOMNUMBERS + rd.nextInt(mvClass.numOfInputs);
					else
						index = rd.nextInt(mvClass.RANDOMNUMBERS);
					subModules[j] = mvClass.primitiveModules[index].deepCopy();
				} else {
					rand1 = rd.nextInt(level);
					rand2 = rd.nextInt(MasterVariables.MAX_MODULES);
					index = evolver.tournament(mvClass.TSIZE, mvClass.masterModules[rand1][rand2]);
					subModules[j] = mvClass.masterModules[rand1][rand2][index].deepCopy();
				}
			}
			mvClass.masterModules[level][mvClass.currentDeme][i] = new Module(subModules, FC.create_random_indiv(
					mvClass.DEPTH, size, false), false, false);
		}

		for (int i = 0; i < mvClass.POPSIZE; i++) {
			evolver.calculateFitness(mvClass.masterModules[level][mvClass.currentDeme][i]);
		}
	

		MasterVariables.evoOperationsProbInit();

		mvClass.submutFitness = 1;
		mvClass.borrowFitness = 1;
		mvClass.subsmeFitness = 1;

	}

	private static void printStats() {
		if (mvClass.currentDeme != prevDeme) {
			prevDeme = mvClass.currentDeme;
			System.out.println("***************************************");
			System.out.println("***************************************");
			System.out.println("***************************************");
			System.out.println("Working on deme " + mvClass.currentDeme);
		}
		if (mvClass.currentLevel != prevLevel) {
			prevLevel = mvClass.currentLevel;
			System.out.println("***************************************");
			System.out.println("Working on level " + mvClass.currentLevel);
		}

		System.out.println("\nDeme " + mvClass.currentDeme + " level " + mvClass.currentLevel + " gen " + levelGeneration);
		System.out.println("best fit is " + bestFit + " its index is " + fitIndex + " and length "
				+ mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][fitIndex].length
				+ " Prev best fit is " + prevBestFit[levelGeneration % prevBestFit.length]);
		if (mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][fitIndex].isPrimitive)
			System.out.print(" IS PRIMITIVE "
					+ mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][fitIndex].getOutput());
		else
			print_indiv(mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][fitIndex].function, 0,
					fitIndex);
		System.out.println("\nProbabilities of SUB_MUT " + mvClass.SUB_MUT_PROB + " BORROW " + mvClass.BORROW_PROB
				+ " SUBSUME " + mvClass.SUBSUME_PROB);

	}

	private static void findBesdFit() {

		for (int k = 0; k < mvClass.POPSIZE; k++) {
			// System.out.println(mvClass.masterModules[k].fitness);
			if (mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][k].fitness > bestFit) {
				bestFit = mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][k].fitness;
				fitIndex = k;
			}
			// System.out.println(mvClass.masterModules[k].length);
		}

	}

	private static void initialize() {
		String line;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(
					"C:\\Users\\taor9299\\Box Sync\\Java workspace\\ForexTrader\\test2.txt"));
			line = in.readLine();
			StringTokenizer tokens = new StringTokenizer(line);
			int numOfInputs = Integer.parseInt(tokens.nextToken().trim());
			int numOfOutputs = Integer.parseInt(tokens.nextToken().trim());
			int fitnesscases = Integer.parseInt(tokens.nextToken().trim());
			double[][] data = new double[fitnesscases][numOfInputs + numOfOutputs];
			for (int i = 0; i < fitnesscases; i++) {
				line = in.readLine();
				tokens = new StringTokenizer(line);
				for (int j = 0; j < numOfInputs + numOfOutputs; j++) {
					data[i][j] = Double.parseDouble(tokens.nextToken().trim());
				}
			}
			mvClass.initialize(numOfInputs, numOfOutputs, data);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Please provide a data file");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("ERROR: Incorrect data format");
			System.exit(0);
		}

		for (int i = 0; i < prevBestFit.length; i++)
			prevBestFit[i] = -1.0e34;
	}

	static int print_indiv(int[] buffer, int buffercounter, int index) {
		int a1 = 0, a2;
		if (buffer[buffercounter] > mvClass.FSET_1_START) {
			System.out.print((buffer[buffercounter]) + " ");
			return (++buffercounter);
		}
		switch (buffer[buffercounter]) {
		case MasterVariables.ADD:
			System.out.print("(");
			a1 = print_indiv(buffer, ++buffercounter, index);
			System.out.print(" + ");
			break;
		case MasterVariables.SUB:
			System.out.print("(");
			a1 = print_indiv(buffer, ++buffercounter, index);
			System.out.print(" - ");
			break;
		case MasterVariables.MUL:
			System.out.print("(");
			a1 = print_indiv(buffer, ++buffercounter, index);
			System.out.print(" * ");
			break;
		case MasterVariables.DIV:
			System.out.print("(");
			a1 = print_indiv(buffer, ++buffercounter, index);
			System.out.print(" / ");
			break;
		}
		a2 = print_indiv(buffer, a1, index);
		System.out.print(")");
		return (a2);
	}
}
