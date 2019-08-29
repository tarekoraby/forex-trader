package v3;

public class Trigger {

	Module[] inputModules;
	Trigger[] inputTriggers;
	int[] function;
	double fitness;
	double[] arrayOutput;
	boolean output;
	int PC, primitiveInputID;

	Trigger(Module[] inputs, int[] function) {
		this.inputModules = inputs;
		this.function = function;
		inputTriggers = null;
		fitness = 0;
	}

	Trigger(Trigger[] inputs, int[] function) {
		this.inputTriggers = inputs;
		this.function = function;
		inputModules = null;
		fitness = 0;
	}

	boolean isActive() {

		double output;
		if (inputModules != null) {
			PC = 0;
			output = run();
		} else {
			double input1 = 0, input2 = 0;
			if (inputTriggers[0].isActive())
				input1 = 1;
			if (inputTriggers[1].isActive())
				input1 = 1;
			output = MathCalculator.calculate(function[0], input1, input2);
		}
		if (output != 0 && output != 1) {
			System.out.println("Trigger class error !!! isActive method");
			System.exit(0);
		}
		if (output == 0)
			return false;
		else
			return true;
	}

	private double[] runArray() {
		int primitive = function[PC++];

		if (primitive <= MV.FSET_4_START && primitive >= MV.FSET_4_END)
			return MathCalculator.calculate(primitive, runArray(), run());
		if (primitive <= MV.FSET_5_START && primitive >= MV.FSET_5_END)
			return MathCalculator.calculate(primitive, runArray(), runArray());
		if (primitive <= MV.FSET_6_START && primitive >= MV.FSET_6_END)
			return MathCalculator.calculate(primitive, run(), run(), run());

		System.out.println("Trigger class error !!! runArray method");
		System.out.println(primitive);
		System.exit(0);
		return null;
	}

	private double run() {
		int primitive = function[PC++];

		if (primitive < 0) {
			if ((primitive <= MV.FSET_2_START && primitive >= MV.FSET_2_END)
					|| (primitive <= MV.FSET_7_START && primitive >= MV.FSET_7_END)
					|| (primitive <= MV.FSET_8_START && primitive >= MV.FSET_8_END))
				return MathCalculator.calculate(primitive, run(), run());

			if (primitive <= MV.FSET_1_START && primitive >= MV.FSET_1_END)
				return MathCalculator.calculate(primitive, run());

			if (primitive <= MV.FSET_3_START && primitive >= MV.FSET_3_END)
				return MathCalculator.calculate(primitive, runArray());

			System.out.println("Trigger class error !!! ");
			System.exit(0);
			return -99;
		} else
			return inputModules[primitive].getOutput();
	}

	Trigger deepCopy() {
		Trigger copy;
		if (inputModules != null) {
			Module[] newInputs = new Module[inputModules.length];
			for (int i = 0; i < inputModules.length; i++) {
				newInputs[i] = inputModules[i].deepCopy();
				newInputs[i].length = inputModules[i].length;
			}
			copy = new Trigger(newInputs, function.clone());
		} else {
			Trigger[] newInputs = new Trigger[inputTriggers.length];
			for (int i = 0; i < inputTriggers.length; i++) {
				newInputs[i] = inputTriggers[i].deepCopy();
			}
			copy = new Trigger(newInputs, function.clone());
		}
		return copy;
	}
}
