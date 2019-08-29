package v3;

import java.util.Random;

public class Simulator {

	Evolver evolver = new Evolver();
	static Random rd = new Random();

	public Simulator() {
		createInitialPop();
	}

	private void createInitialPop() {
		// TODO Auto-generated method stub

	}

	public void simulate() {
		int roundCounter = 0;
		while (MV.RUNFOREVER || roundCounter < MV.maxSimRounds) {
			for (MV.currentLevel = 0; MV.currentLevel < MV.DEMES; MV.currentLevel++) {
				for (MV.currentDeme = 0; MV.currentDeme < MV.DEMES; MV.currentDeme++) {
					initializeDeme();
					evolver.evolve();
				}
				roundCounter++;
			}
		}
	}

	private void initializeDeme() {

		for (int i = 0; i < MV.POPSIZE_PER_LEVEL_DEME; i++)
			MV.masterModules[MV.currentLevel][MV.currentDeme][i] = evolver.createRandomModule();

		for (int i = 0; i < MV.POPSIZE_PER_LEVEL_DEME; i++)
			MV.masterTriggers[MV.currentLevel][MV.currentDeme][i] = evolver.createRandomTrigger();

	}
}
