import java.util.Random;

public class Evolver {

	static MasterVariables mvClass = new MasterVariables();
	static Random rd = new Random();
	static FunctionCreator FC = new FunctionCreator();

	void evolve() {
		int index;

		double totalBestFit = +mvClass.submutFitness + mvClass.borrowFitness + mvClass.subsmeFitness;

		mvClass.SUB_MUT_PROB = mvClass.submutFitness / totalBestFit;
		mvClass.BORROW_PROB = mvClass.borrowFitness / totalBestFit;
		mvClass.SUBSUME_PROB = mvClass.subsmeFitness / totalBestFit;

		if (mvClass.BORROW_PROB < mvClass.MIN_PROB)
			mvClass.BORROW_PROB = mvClass.MIN_PROB;
		if (mvClass.SUBSUME_PROB < mvClass.MIN_PROB)
			mvClass.SUBSUME_PROB = mvClass.MIN_PROB;
		mvClass.SUB_MUT_PROB = 1 - mvClass.SUBSUME_PROB - mvClass.BORROW_PROB;
		if (mvClass.SUB_MUT_PROB < mvClass.MIN_PROB) {
			mvClass.SUB_MUT_PROB = mvClass.MIN_PROB;
			mvClass.BORROW_PROB = mvClass.borrowFitness / (totalBestFit - mvClass.submutFitness);
			mvClass.SUBSUME_PROB = 1 - mvClass.SUB_MUT_PROB - mvClass.BORROW_PROB;
		}

		mvClass.submutFitness = -1.0e34;
		mvClass.borrowFitness = -1.0e34;
		mvClass.subsmeFitness = -1.0e34;

		for (int i = 1; i < mvClass.POPSIZE; i++) {
			double newFitness = Double.MIN_VALUE, replaceFit= Double.MAX_VALUE;
			Module newModule = null;
			index = -1;

			while (newFitness < replaceFit) {
				double random = rd.nextDouble();
				if (random < mvClass.BORROW_PROB) {
					newModule = borrow();
					calculateFitness(newModule);
					if (newModule.fitness > mvClass.submutFitness)
						mvClass.submutFitness = newModule.fitness;
				} else if (random < mvClass.BORROW_PROB + mvClass.SUBSUME_PROB) {
					newModule = subsume();
					calculateFitness(newModule);
					if (newModule.fitness > mvClass.borrowFitness)
						mvClass.borrowFitness = newModule.fitness;
				} else if (random < mvClass.BORROW_PROB + mvClass.SUBSUME_PROB + mvClass.SUB_MUT_PROB) {
					newModule = subtree_mut();
					calculateFitness(newModule);
					if (newModule.fitness > mvClass.subsmeFitness)
						mvClass.subsmeFitness = newModule.fitness;
				} else {
					System.out.println("Evolution Error!!!");
					System.exit(0);
				}

				newFitness = newModule.fitness;
				index = negativeTournament(mvClass.TSIZE, mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
				replaceFit = mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index].fitness;
			}
			mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index] = newModule;
		}
	}

	private Module subtree_mut() {
		int index = tournament(mvClass.TSIZE, mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
		Module newModule = mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index].deepCopy();

		int[] function = newModule.function.clone();
		function = subtreeMutation(function, newModule.inputs.length, newModule.returnsBoolean);
		newModule.function = function;

		return newModule;
	}

	private Module borrow() {
		int index;
		int size = 1 + rd.nextInt(mvClass.MAX_MODULES);

		Module[] subModules = new Module[size];
		for (int j = 0; j < size; j++) {
			index = tournament(mvClass.TSIZE, mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
			int rand = rd
					.nextInt(mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index].inputs.length);
			subModules[j] = mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index].inputs[rand]
					.deepCopy();
		}

		Module newModule = new Module(subModules, FC.create_random_indiv(mvClass.DEPTH, size, false), false, false);
		return newModule;
	}

	private Module subsume() {
		int index = -1;
		int size = 1 + rd.nextInt(mvClass.MAX_MODULES);
		Module[] subModules = new Module[size];
		for (int j = 0; j < size; j++) {
			int rand = rd.nextInt(4);
			if (rand == 0 || mvClass.currentLevel == 0) {
				rand = rd.nextInt(2);
				if (rand == 0)
					index = mvClass.RANDOMNUMBERS + rd.nextInt(mvClass.numOfInputs);
				else
					index = rd.nextInt(mvClass.RANDOMNUMBERS);
				subModules[j] = mvClass.primitiveModules[index].deepCopy();
			} else {
				rand = rd.nextInt(mvClass.currentLevel);
				int rand2 = rd.nextInt(mvClass.demes);
				index = tournament(mvClass.TSIZE, mvClass.masterModules[rand][rand2]);
				subModules[j] = mvClass.masterModules[rand][rand2][index].deepCopy();
			}
		}
		Module newModule = new Module(subModules, FC.create_random_indiv(mvClass.DEPTH, size, false), false, false);
		return newModule;
	}

	private Module replicate() {
		int index = tournament(mvClass.TSIZE, mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme]);
		return (mvClass.masterModules[mvClass.currentLevel][mvClass.currentDeme][index].deepCopy());
	}

	static int[] subtreeMutation(int[] parent, int numOfInputs, boolean returnBoolean) {
		int mutStart, mutEnd, parentLen = traverse(parent, 0), subtreeLen, lenOff;
		int[] newSubtree, offspring;

		// Calculate the mutation starting point.
		mutStart = rd.nextInt(parentLen);
		mutEnd = traverse(parent, mutStart);

		newSubtree = FC.create_random_indiv(mvClass.DEPTH, numOfInputs, returnBoolean);

		lenOff = mutStart + newSubtree.length + (parentLen - mutEnd);

		offspring = new int[lenOff];

		System.arraycopy(parent, 0, offspring, 0, mutStart);
		System.arraycopy(newSubtree, 0, offspring, mutStart, newSubtree.length);
		System.arraycopy(parent, mutEnd, offspring, (mutStart + newSubtree.length), (parentLen - mutEnd));

		return (offspring);
	}

	static int traverse(int[] buffer, int buffercount) {
		if (buffer[buffercount] > mvClass.FSET_1_START)
			return (++buffercount);
		else
			return (traverse(buffer, traverse(buffer, ++buffercount)));
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

	static void calculateFitness(Module testedModule) {
		double diff;
		testedModule.fitness = 0;

		for (int i = 0; i < mvClass.data.length; i++) {
			mvClass.retreiveInputs(i);
			for (int j = 0; j < mvClass.numOfOutputs; j++) {
				diff = Math.abs(mvClass.data[i][mvClass.numOfInputs + j] - testedModule.getOutput());
				testedModule.fitness -= diff;
			}
		}

	}

}
