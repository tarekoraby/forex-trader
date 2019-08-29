package v3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;

public class Evolver {

	static Random rd = new Random();
	static FunctionCreator FC = new FunctionCreator();
	double bestFit, bestFitSubmut, bestFitSubsum, bestFitCliqueborrow;
	double[] prevBestFit;
	Module[] currentModules, futureModules, futureMasterModules;
	Trigger[] currentTriggers, futureTriggers, futureMasterTriggers;
	int triggeredCount;
	static int prevDeme = -1, prevLevel = -1;
	static double[] worstFitnesses, bestFitnesses;
	static WriteFile fileWriter;

	void initializeEvolver() {
		MV.SUB_MUT_PROB = (double) 1 / 3;
		MV.CLIQUE_BORROW_PROB = (double) 1 / 3;
		MV.SUBSUME_PROB = (double) 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
		bestFitSubmut = -1.0e34;
		bestFitSubsum = -1.0e34;
		bestFitCliqueborrow = -1.0e34;

		MV.TRIGGER_CREATE_PROB = (double) 1/3;
		MV.TRIGGER_REPLICATE_PROB = (double) 1/3;
		MV.TRIGGER_COMBINE_PROB = 1 - MV.TRIGGER_CREATE_PROB - MV.TRIGGER_REPLICATE_PROB;

		prevBestFit = new double[MV.REQUIRED_IMPROVEMENTS_ROUNDS];
		MV.currentRound = 0;
	}

	void evolve() {

		initializeEvolver();

		while (MV.currentRound < MV.MIN_ROUNDS || evolutionIsImproving()) {
			futureMasterModules = new Module[MV.POPSIZE_PER_LEVEL_DEME];
			futureMasterTriggers = new Trigger[MV.POPSIZE_PER_LEVEL_DEME];

			for (int i = 0; i < MV.POPSIZE_PER_LEVEL_DEME; i++) {
				MV.masterModules[MV.currentLevel][MV.currentDeme][i].recentlyTriggered = false;
			}

			worstFitnesses = new double[MV.SAMPLE_SIZE];
			bestFitnesses = new double[MV.SAMPLE_SIZE];
			for (int k = 0; k < MV.SAMPLE_SIZE; k++) {
				worstFitnesses[k] = Double.MAX_VALUE;
				bestFitnesses[k] = -1 * Double.MAX_VALUE;
			}

			for (int k = 0; k < MV.SAMPLE_SIZE; k++) {
				MV.retreiveRandommInput();
				currentModules = new Module[MV.POPSIZE_PER_LEVEL_DEME];
				currentTriggers = new Trigger[MV.POPSIZE_PER_LEVEL_DEME];

				// count the num of triggered
				triggeredCount = 0;
				for (int i = 0; i < MV.POPSIZE_PER_LEVEL_DEME; i++) {
					if (MV.masterTriggers[MV.currentLevel][MV.currentDeme][i].isActive()) {
						MV.masterModules[MV.currentLevel][MV.currentDeme][i].recentlyTriggered = true;
						currentModules[triggeredCount] = MV.masterModules[MV.currentLevel][MV.currentDeme][i];
						currentTriggers[triggeredCount] = MV.masterTriggers[MV.currentLevel][MV.currentDeme][i];
						triggeredCount++;
					}
				}
				//System.out.println(triggeredCount);

				// create random modules if low num of triggered
				if (triggeredCount < MV.POPSIZE_PER_CASE_LEVEL_DEME) {
					for (int i = triggeredCount; i < MV.POPSIZE_PER_LEVEL_DEME; i++) {
						currentModules[triggeredCount] = createRandomModule();
						currentTriggers[triggeredCount] = createTrigger(true);
					}
					triggeredCount = MV.POPSIZE_PER_CASE_LEVEL_DEME;
				}
				currentModules = Arrays.copyOf(currentModules, triggeredCount);
				currentTriggers = Arrays.copyOf(currentTriggers, triggeredCount);
				double[] fitnessArray = new double[triggeredCount];

				futureModules = new Module[MV.POPSIZE_PER_CASE_LEVEL_DEME];
				futureTriggers = new Trigger[MV.POPSIZE_PER_CASE_LEVEL_DEME];

				double output;
				for (int i = 0; i < triggeredCount; i++) {
					output = currentModules[i].getOutput();

					if (output == 0) {
						currentModules[i].fitness = 0;
					} else {
						currentModules[i].fitness = MV.currentWin;
					}

					if (currentModules[i].fitness != currentModules[i].fitness
							|| currentModules[i].fitness == Double.MIN_VALUE
							|| currentModules[i].fitness == Double.POSITIVE_INFINITY
							|| currentModules[i].fitness == Double.NEGATIVE_INFINITY)
						System.out.println("ERRRRRRRORRRR!!!!!!!!!");
					fitnessArray[i] = currentModules[i].fitness;
				}

				Arrays.sort(fitnessArray);
				fitnessArray = Arrays.copyOfRange(fitnessArray, fitnessArray.length - MV.POPSIZE_PER_CASE_LEVEL_DEME,
						fitnessArray.length);
				//System.out.println(fitnessArray[9]);

				int counter = MV.POPSIZE_PER_CASE_LEVEL_DEME;
				for (int i = 0; i < triggeredCount; i++) {
					if (currentModules[i].fitness > fitnessArray[0]) {
						counter--;
						futureModules[counter] = currentModules[i].deepCopy();
						futureTriggers[counter] = currentTriggers[i].deepCopy();
					}
				}

				if (counter > 0)
					for (int i = 0; i < triggeredCount; i++) {
						if (currentModules[i].fitness == fitnessArray[0]) {
							counter--;
							futureModules[counter] = currentModules[i].deepCopy();
							futureTriggers[counter] = currentTriggers[i].deepCopy();
							if (counter == 0)
								break;
						}
					}

				if (counter > 0) {
					counter--;
					int rand = rd.nextInt(currentModules.length);
					futureModules[counter] = currentModules[rd.nextInt(currentModules.length)].deepCopy();
					futureTriggers[counter] = currentTriggers[rd.nextInt(currentTriggers.length)].deepCopy();
				}

				evolveModules();
				evolveTriggers();

				for (int i = k * MV.POPSIZE_PER_CASE_LEVEL_DEME; i < (k + 1) * MV.POPSIZE_PER_CASE_LEVEL_DEME; i++) {
					MV.masterModules[MV.currentLevel][MV.currentDeme][i] = futureModules[i - k
							* MV.POPSIZE_PER_CASE_LEVEL_DEME];
					MV.masterTriggers[MV.currentLevel][MV.currentDeme][i] = futureTriggers[i - k
							* MV.POPSIZE_PER_CASE_LEVEL_DEME];

				}

				for (int i = 0; i < MV.POPSIZE_PER_CASE_LEVEL_DEME; i++) {
					if (futureModules[i].fitness < worstFitnesses[k])
						worstFitnesses[k] = futureModules[i].fitness;
					if (futureModules[i].fitness > bestFitnesses[k])
						bestFitnesses[k] = futureModules[i].fitness;
				}

			}

			for (int i = 0; i < MV.POPSIZE_PER_LEVEL_DEME; i++) {
				if (MV.masterModules[MV.currentLevel][MV.currentDeme][i].recentlyTriggered == false) {
					MV.masterModules[MV.currentLevel][MV.currentDeme][i] = createRandomModule();
					MV.masterTriggers[MV.currentLevel][MV.currentDeme][i] = createRandomTrigger();
				}

			}

			printStats();
			// reAdjustProb();
			MV.currentRound++;
			if (MV.currentRound > 20) {
				MV.TRIGGER_CREATE_PROB = 0;
				MV.TRIGGER_COMBINE_PROB = 0;
				MV.TRIGGER_REPLICATE_PROB = 1;
			}
			if (MV.currentRound > 3) {
				test();
				System.exit(0);
			}
		}

	}

	private void test() {

		try {

			String outputString;
			fileWriter = new WriteFile("output.txt", false);
			for (int i = 0; i < 1000; i++) {
				MV.retreiveRandommInput();
				double output;
				double[] x;
				int numberOfPredict = 0;
				for (int k = 0; k < MV.POPSIZE_PER_LEVEL_DEME; k++) {
					if (MV.masterTriggers[MV.currentLevel][MV.currentDeme][k].isActive()) {
						output = MV.masterModules[MV.currentLevel][MV.currentDeme][k].getOutput();
						numberOfPredict++;
					}
				}
				x = new double[numberOfPredict];
				int y = numberOfPredict;
				for (int k = 0; k < MV.POPSIZE_PER_LEVEL_DEME; k++) {
					if (MV.masterTriggers[MV.currentLevel][MV.currentDeme][k].isActive()) {
						x[--y] = MV.masterModules[MV.currentLevel][MV.currentDeme][k].getOutput();

					}
				}

				// System.out.println(x[numberOfPredict/2] + " " +
				// numberOfPredict + " " + MV.currentWin);
				outputString = Double.toString(x[numberOfPredict / 2]) + " " + MV.currentWin;
				fileWriter.writeToFile(outputString + " \n");
			}

		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Please provide a setup and data files");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("ERROR: Incorrect data format");
			System.exit(0);
		}

	}

	private static void printStats() {
		if (MV.currentDeme != prevDeme) {
			prevDeme = MV.currentDeme;
			System.out.println("***************************************");
			System.out.println("***************************************");
			System.out.println("***************************************");
			System.out.println("Working on deme " + MV.currentDeme);
		}
		if (MV.currentLevel != prevLevel) {
			prevLevel = MV.currentLevel;
			System.out.println("***************************************");
			System.out.println("Working on level " + MV.currentLevel);
		}

		System.out.println("\nDeme " + MV.currentDeme + " level " + MV.currentLevel + " gen " + MV.currentRound);
		double sumWorstFitnesses = 0, sumBestFitnesses = 0;
		for (int i = 0; i < MV.SAMPLE_SIZE; i++) {
			sumWorstFitnesses += worstFitnesses[i];
			sumBestFitnesses += bestFitnesses[i];
		}
		System.out.println("Sum worst fitnesses is " + sumWorstFitnesses);
		System.out.println("Sum best fitnesses is " + sumBestFitnesses);
		/*
		 * System.out.println("best fit is " + bestFit + " its index is " +
		 * fitIndex + " and length " +
		 * MV.masterModules[MV.currentLevel][MV.currentDeme][fitIndex].length +
		 * " Prev best fit is " + prevBestFit[levelGeneration %
		 * prevBestFit.length]); if
		 * (MV.masterModules[MV.currentLevel][MV.currentDeme
		 * ][fitIndex].isPrimitive) System.out.print(" IS PRIMITIVE " +
		 * MV.masterModules
		 * [MV.currentLevel][MV.currentDeme][fitIndex].getOutput()); else
		 * print_indiv
		 * (MV.masterModules[MV.currentLevel][MV.currentDeme][fitIndex
		 * ].function, 0, fitIndex);
		 */

	}

	private Trigger createTrigger(boolean fitsCurrentInput) {
		int index, rand1, rand2;

		int triggerSize = 1 + rd.nextInt(MV.MAX_SUB_MODULES);
		Module[] subModules = new Module[triggerSize];
		// takes at least one input
		index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
		subModules[0] = MV.primitiveModules[index].deepCopy();
		for (int j = 1; j < triggerSize; j++) {
			rand1 = rd.nextInt(2);
			if (rand1 == 0 || MV.currentLevel == 0) {
				rand1 = rd.nextInt(2);
				if (rand1 == 0)
					index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
				else
					index = rd.nextInt(MV.numOfConstants);
				subModules[j] = MV.primitiveModules[index].deepCopy();
			} else {
				rand1 = rd.nextInt(MV.currentLevel);
				rand2 = rd.nextInt(MV.DEMES);
				index = tournament(MV.TSIZE, MV.masterModules[rand1][rand2]);
				subModules[j] = MV.masterModules[rand1][rand2][index].deepCopy();
			}
		}
		Trigger newTrigger = new Trigger(subModules, FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, triggerSize, true,
				false));
		while (newTrigger.isActive() != fitsCurrentInput)
			newTrigger = createTrigger(fitsCurrentInput);
		return newTrigger;
	}

	Trigger createRandomTrigger() {
		int index, rand1, rand2;

		int triggerSize = 1 + rd.nextInt(MV.MAX_SUB_MODULES);
		Module[] subModules = new Module[triggerSize];
		// takes at least one input
		index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
		subModules[0] = MV.primitiveModules[index].deepCopy();
		for (int j = 1; j < triggerSize; j++) {
			rand1 = rd.nextInt(2);
			if (rand1 == 0 || MV.currentLevel == 0) {
				rand1 = rd.nextInt(2);
				if (rand1 == 0)
					index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
				else
					index = rd.nextInt(MV.numOfConstants);
				subModules[j] = MV.primitiveModules[index].deepCopy();
			} else {
				rand1 = rd.nextInt(MV.currentLevel);
				rand2 = rd.nextInt(MV.DEMES);
				index = tournament(MV.TSIZE, MV.masterModules[rand1][rand2]);
				subModules[j] = MV.masterModules[rand1][rand2][index].deepCopy();
			}
		}
		Trigger newTrigger = new Trigger(subModules, FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, triggerSize, true,
				false));
		return newTrigger;
	}

	Module createRandomModule() {
		int index, rand1, rand2;

		int moduleSize = 1 + rd.nextInt(MV.MAX_SUB_MODULES);
		Module[] subModules = new Module[moduleSize];
		// takes at least one input
		index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
		subModules[0] = MV.primitiveModules[index].deepCopy();
		for (int j = 1; j < moduleSize; j++) {
			rand1 = rd.nextInt(2);
			if (rand1 == 0 || MV.currentLevel == 0) {
				rand1 = rd.nextInt(2);
				if (rand1 == 0)
					index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
				else
					index = rd.nextInt(MV.numOfConstants);
				subModules[j] = MV.primitiveModules[index].deepCopy();
			} else {
				rand1 = rd.nextInt(MV.currentLevel);
				rand2 = rd.nextInt(MV.DEMES);
				index = tournament(MV.TSIZE, MV.masterModules[rand1][rand2]);
				subModules[j] = MV.masterModules[rand1][rand2][index].deepCopy();
			}
		}
		Module newModule = new Module(subModules, FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, moduleSize,
				MV.BOOLEAN_OUTPUT, false));

		return newModule;
	}

	private void evolveTriggers() {
		int index;
		double random;
		for (int i = 0; i < MV.POPSIZE_PER_CASE_LEVEL_DEME; i++) {
			random = rd.nextDouble();
			Trigger newTrigger = null;

			if (random < MV.TRIGGER_CREATE_PROB) {
				newTrigger = createTrigger(true);

			} else if (random < MV.TRIGGER_CREATE_PROB + MV.TRIGGER_REPLICATE_PROB) {
				newTrigger = triggerReplicate();

			} else if (random < MV.TRIGGER_CREATE_PROB + MV.TRIGGER_REPLICATE_PROB + MV.TRIGGER_COMBINE_PROB) {
				newTrigger = triggerCombine();
			} else {
				System.out.println("Evolution Error - evolveTriggers method!!!");
				System.exit(0);
			}

			futureTriggers[i] = newTrigger;
		}

	}

	private Trigger triggerCombine() {
		int random = rd.nextInt(5);
		Trigger newTrigger, oldTrigger1, oldTrigger2;
		Trigger[] inputTriggers;
		int[] function;
		int index1, index2;
		switch (random) {
		case 0:
			// AND
			index1 = tournament(MV.TSIZE, currentModules);
			oldTrigger1 = currentTriggers[index1];
			index2 = tournament(MV.TSIZE, currentModules);
			oldTrigger2 = currentTriggers[index2];
			inputTriggers = new Trigger[2];
			inputTriggers[0] = oldTrigger1.deepCopy();
			inputTriggers[1] = oldTrigger2.deepCopy();
			function = new int[1];
			function[0] = MV.AND;
			newTrigger = new Trigger(inputTriggers, function);
			return newTrigger;
		case 1:
			// OR
			index1 = tournament(MV.TSIZE, currentModules);
			oldTrigger1 = currentTriggers[index1];
			index2 = tournament(MV.TSIZE, currentModules);
			oldTrigger2 = currentTriggers[index2];
			inputTriggers = new Trigger[2];
			inputTriggers[0] = oldTrigger1.deepCopy();
			inputTriggers[1] = oldTrigger2.deepCopy();
			function = new int[1];
			function[0] = MV.OR;
			newTrigger = new Trigger(inputTriggers, function);
			return newTrigger;
		case 2:
			// XOR
			index1 = tournament(MV.TSIZE, currentModules);
			oldTrigger1 = currentTriggers[index1];
			inputTriggers = new Trigger[2];
			inputTriggers[0] = oldTrigger1.deepCopy();
			inputTriggers[1] = createTrigger(false);
			function = new int[1];
			function[0] = MV.XOR;
			newTrigger = new Trigger(inputTriggers, function);
			return newTrigger;
		case 3:
			// XNOR
			index1 = tournament(MV.TSIZE, currentModules);
			oldTrigger1 = currentTriggers[index1];
			index2 = tournament(MV.TSIZE, currentModules);
			oldTrigger2 = currentTriggers[index2];
			inputTriggers = new Trigger[2];
			inputTriggers[0] = oldTrigger1.deepCopy();
			inputTriggers[1] = oldTrigger2.deepCopy();
			function = new int[1];
			function[0] = MV.XNOR;
			newTrigger = new Trigger(inputTriggers, function);
			return newTrigger;
		case 4:
			// NAND
			index1 = tournament(MV.TSIZE, currentModules);
			oldTrigger1 = currentTriggers[index1];
			inputTriggers = new Trigger[2];
			inputTriggers[0] = oldTrigger1.deepCopy();
			inputTriggers[1] = createTrigger(false);
			function = new int[1];
			function[0] = MV.NAND;
			newTrigger = new Trigger(inputTriggers, function);
			return newTrigger;
		}

		System.out.println("Evolver class - triggerCombine method error!!!");
		System.exit(0);
		return null;
	}

	private Trigger triggerReplicate() {
		int index = tournament(MV.TSIZE, currentModules);
		return currentTriggers[index].deepCopy();
	}

	private void evolveModules() {
		int index;
		for (int i = 0; i < MV.EVO_ROUNDS_PER_CASE; i++) {
			double random = rd.nextDouble();
			double output;
			Module newModule = null;

			if (random < MV.CLIQUE_BORROW_PROB) {
				newModule = cliqueBorrow();
				output = newModule.getOutput();
				if (output == 0) {
					newModule.fitness = 0;
				} else {
					newModule.fitness = MV.currentWin;
				}
				if (newModule.fitness > bestFitCliqueborrow)
					bestFitCliqueborrow = newModule.fitness;
			} else if (random < MV.CLIQUE_BORROW_PROB + MV.SUBSUME_PROB) {
				newModule = subsume();
				output = newModule.getOutput();
				if (output == 0) {
					newModule.fitness = 0;
				} else {
					newModule.fitness = MV.currentWin;
				}
				if (newModule.fitness > bestFitSubsum)
					bestFitSubsum = newModule.fitness;
			} else if (random < MV.CLIQUE_BORROW_PROB + MV.SUBSUME_PROB + MV.SUB_MUT_PROB) {
				newModule = subtree_mut();
				output = newModule.getOutput();
				if (output == 0) {
					newModule.fitness = 0;
				} else {
					newModule.fitness = MV.currentWin;
				}
				if (newModule.fitness > bestFitSubmut)
					bestFitSubmut = newModule.fitness;
			} else {
				System.out.println("Evolution Error - evolveModules method!!!");
				System.out.println(MV.CLIQUE_BORROW_PROB + " " + MV.SUBSUME_PROB + " " + MV.SUB_MUT_PROB);
				System.exit(0);
			}

			int counter = 0;
			while (counter < MV.REPLACE_TRIALS && newModule.fitness > -1E300) {
				counter++;
				index = negativeTournament(MV.TSIZE, futureModules);
				if (newModule.fitness > futureModules[index].fitness) {
					newModule.recentlyTriggered = true;
					futureModules[index] = newModule;
					break;
				}
			}
		}
	}

	private Module cliqueBorrow() {
		int index;
		int size = 1 + rd.nextInt(MV.MAX_SUB_MODULES);

		Module[] subModules = new Module[size];
		index = MV.numOfConstants + rd.nextInt(MV.numOfInputs);
		subModules[0] = MV.primitiveModules[index].deepCopy();
		for (int j = 1; j < size; j++) {
			index = tournament(MV.TSIZE, currentModules);
			int rand = rd.nextInt(currentModules[index].inputs.length);
			subModules[j] = currentModules[index].deepCopy();
		}

		Module newModule = new Module(subModules, FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, size, MV.BOOLEAN_OUTPUT,
				false));
		return newModule;
	}

	private boolean evolutionIsImproving() {
		if (MV.currentRound < 1000)
			return true;
		else
			return false;
	}

	private void reAdjustProb() {
		// bestFitSubmut, bestFitSubsum, bestFitCliqueborrow
		double totalBestFit = bestFitSubmut + bestFitSubsum + bestFitCliqueborrow;

		MV.CLIQUE_BORROW_PROB = bestFitCliqueborrow / totalBestFit;
		MV.SUBSUME_PROB = bestFitSubsum / totalBestFit;
		MV.SUB_MUT_PROB = 1 - MV.CLIQUE_BORROW_PROB - MV.SUBSUME_PROB;
		System.out.println("***************************************");
		System.out.println(bestFitCliqueborrow + " " + bestFitSubsum + " " + bestFitSubmut);
		System.out.println(MV.CLIQUE_BORROW_PROB + " " + MV.SUBSUME_PROB + " " + MV.SUB_MUT_PROB);

		if (MV.CLIQUE_BORROW_PROB != MV.CLIQUE_BORROW_PROB) {
			MV.SUB_MUT_PROB = (double) 1 / 3;
			MV.CLIQUE_BORROW_PROB = (double) 1 / 3;
			MV.SUBSUME_PROB = (double) 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
		}
		if (MV.SUBSUME_PROB != MV.SUBSUME_PROB) {
			MV.SUB_MUT_PROB = (double) 1 / 3;
			MV.CLIQUE_BORROW_PROB = (double) 1 / 3;
			MV.SUBSUME_PROB = (double) 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
		}
		if (MV.SUB_MUT_PROB != MV.SUB_MUT_PROB) {
			MV.SUB_MUT_PROB = (double) 1 / 3;
			MV.CLIQUE_BORROW_PROB = (double) 1 / 3;
			MV.SUBSUME_PROB = (double) 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
		}

		if (MV.CLIQUE_BORROW_PROB < MV.MIN_PROB) {
			MV.CLIQUE_BORROW_PROB = MV.MIN_PROB;
			if (MV.SUB_MUT_PROB < MV.MIN_PROB) {
				MV.SUB_MUT_PROB = MV.MIN_PROB;
				MV.SUBSUME_PROB = 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
			} else if (MV.SUBSUME_PROB < MV.MIN_PROB) {
				MV.SUBSUME_PROB = MV.MIN_PROB;
				MV.SUB_MUT_PROB = 1 - MV.SUBSUME_PROB - MV.CLIQUE_BORROW_PROB;
			} else {
				MV.SUB_MUT_PROB = bestFitSubmut / (totalBestFit - bestFitCliqueborrow);
				MV.SUBSUME_PROB = 1 - MV.SUB_MUT_PROB - MV.CLIQUE_BORROW_PROB;
			}
		} else if (MV.SUB_MUT_PROB < MV.MIN_PROB) {
			MV.SUB_MUT_PROB = MV.MIN_PROB;
			if (MV.SUBSUME_PROB < MV.MIN_PROB) {
				MV.SUBSUME_PROB = MV.MIN_PROB;
			} else {
				MV.SUBSUME_PROB = bestFitSubsum / (totalBestFit - bestFitSubmut);
			}
			MV.CLIQUE_BORROW_PROB = 1 - MV.SUBSUME_PROB - MV.SUB_MUT_PROB;
		} else if (MV.SUBSUME_PROB < MV.MIN_PROB) {
			MV.SUBSUME_PROB = MV.MIN_PROB;
			MV.SUB_MUT_PROB = bestFitSubmut / (totalBestFit - bestFitSubsum);
			MV.CLIQUE_BORROW_PROB = 1 - MV.SUBSUME_PROB - MV.SUB_MUT_PROB;
		}

		bestFitSubmut = -1.0e34;
		bestFitSubsum = -1.0e34;
		bestFitCliqueborrow = -1.0e34;

		System.out.println(MV.CLIQUE_BORROW_PROB + " " + MV.SUBSUME_PROB + " " + MV.SUB_MUT_PROB);

	}

	private Module subtree_mut() {
		int index = tournament(MV.TSIZE, currentModules);
		Module newModule = currentModules[index].deepCopy();

		int[] function = newModule.function.clone();
		function = subtreeMutation(function, newModule.inputs.length);
		newModule.function = function;

		return newModule;
	}

	private Module subsume() {
		if (MV.currentLevel == 0)
			return createRandomModule();

		int index = -1;
		int size = 1 + rd.nextInt(MV.MAX_SUB_MODULES);
		Module[] subModules = new Module[size];
		for (int j = 0; j < size; j++) {
			int rand = rd.nextInt(MV.currentLevel);
			int rand2 = rd.nextInt(MV.DEMES);
			index = tournament(MV.TSIZE, MV.masterModules[rand][rand2]);
			subModules[j] = MV.masterModules[rand][rand2][index].deepCopy();
		}
		Module newModule = new Module(subModules, FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, size, MV.BOOLEAN_OUTPUT,
				false));
		return newModule;
	}

	static int[] subtreeMutation(int[] parent, int numOfInputs) {
		int mutStart, mutEnd, parentLen = parent.length, subtreeLen, lenOff;
		int[] newSubtree, offspring;

		// Calculate the mutation starting point.

		mutStart = rd.nextInt(parentLen);
		mutEnd = traverse(parent, mutStart);

		if (parent[mutStart] <= MV.FSET_4_START && parent[mutStart] >= MV.FSET_6_END)
			newSubtree = FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, numOfInputs, false, true);
		else if (parent[mutStart] <= MV.FSET_7_START && parent[mutStart] >= MV.FSET_8_END)
			newSubtree = FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, numOfInputs, true, false);
		else
			newSubtree = FC.create_random_func(MV.MAX_NEW_FUNC_DEPTH, numOfInputs, false, false);

		if (mutStart == 0)
			return newSubtree;
		else {
			lenOff = mutStart + newSubtree.length + (parentLen - mutEnd) - 1;

			offspring = new int[lenOff];

			System.arraycopy(parent, 0, offspring, 0, mutStart);
			System.arraycopy(newSubtree, 0, offspring, mutStart, newSubtree.length);
			System.arraycopy(parent, mutEnd + 1, offspring, (mutStart + newSubtree.length), (parentLen - mutEnd) - 1);

			return (offspring);
		}

	}

	static int traverse(int[] buffer, int buffercount) {

		if (buffer[buffercount] > MV.FSET_1_START)
			return (buffercount);
		else if ((buffer[buffercount] <= MV.FSET_1_START && buffer[buffercount] >= MV.FSET_1_END)
				|| ((buffer[buffercount] <= MV.FSET_3_START && buffer[buffercount] >= MV.FSET_3_END)))
			return (traverse(buffer, traverse(buffer, ++buffercount)));
		else if (buffer[buffercount] <= MV.FSET_6_START && buffer[buffercount] >= MV.FSET_6_END)
			return (traverse(buffer, 1 + traverse(buffer, 1 + traverse(buffer, ++buffercount))));
		else
			return (traverse(buffer, 1 + traverse(buffer, ++buffercount)));

	}

	static int tournament(int tsize, Module[] population) {
		int best = rd.nextInt(population.length), i, competitor;
		double fbest = -1.0e34;

		for (i = 0; i < tsize; i++) {
			competitor = rd.nextInt(population.length);
			if (population[competitor].fitness > fbest) {
				fbest = population[competitor].fitness;
				best = competitor;
			}

		}
		return (best);
	}

	static int negativeTournament(int tsize, Module[] population) {
		int best = rd.nextInt(population.length), i, competitor;
		double fbest = 1.0e34;

		for (i = 0; i < tsize; i++) {
			competitor = rd.nextInt(population.length);
			if (population[competitor].fitness < fbest) {
				fbest = population[competitor].fitness;
				best = competitor;
			}

		}
		return (best);
	}

}
