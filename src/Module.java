public class Module {

	Module[] inputs;
	int[] function;
	double output, fitness;
	boolean isProcessed, isPrimitive, returnsBoolean, receivesBoolean;
	int PC, primitiveInputID, length;

	Module(Module[] inputs, int[] function, boolean returnsBoolean, boolean receivesBoolean) {
		this.inputs = inputs;
		this.function = function;
		this.receivesBoolean = returnsBoolean;
		this.receivesBoolean = receivesBoolean;
		isProcessed = false;
		isPrimitive = false;
		fitness = 0;
		length = inputs.length;
		for (int i = 0; i < inputs.length; i++)
			length += inputs[i].length;
	}

	Module(int primitiveInputID) {
		this.primitiveInputID = primitiveInputID;
		returnsBoolean = false;
		receivesBoolean = false;
		isProcessed = false;
		isPrimitive = true;
		fitness = 0;
		inputs = new Module[0];
		function = null;
		length = 1;
	}

	double getOutput() {
		if (isProcessed)
			return output;

		if (isPrimitive) {
			output = MasterVariables.primitiveInputs[primitiveInputID];
			// isProcessed = true;
			return output;
		} else {
			PC = 0;
			output = run();
			// isProcessed = true;
			return output;
		}
	}

	void reset() {
		isProcessed = false;
	}

	private double run() {
		int primitive = function[PC++];

		switch (primitive) {
		case MasterVariables.GT:
			if (run() > run())
				return (1);
			else
				return (0);
		case MasterVariables.LT:
			if (run() < run())
				return (1);
			else
				return (0);
		case MasterVariables.EQ:
			if (Math.abs(run() - run()) < 1E-10)
				return (1);
			else
				return (0);
		case MasterVariables.AND:
			if (run() == 1 && run() == 1)
				return (1);
			else
				return (0);
		case MasterVariables.OR:
			if (run() == 1 || run() == 1)
				return (1);
			else
				return (0);
		case MasterVariables.ADD:
			return (run() + run());
		case MasterVariables.SUB:
			return (run() - run());
		case MasterVariables.MUL:
			return (run() * run());
		case MasterVariables.DIV: {
			double num = run(), den = run();
			if (Math.abs(den) <= 0.00001)
				return (num);
			else
				return (num / den);
		}

		default:
			return inputs[primitive].getOutput();
		}
	}

	Module deepCopy() {
		Module copy;
		if (isPrimitive) {
			copy = new Module(primitiveInputID);
			return copy;
		} else {
			Module[] newInputs = new Module[inputs.length];
			for (int i = 0; i < inputs.length; i++) {
				newInputs[i] = inputs[i].deepCopy();
				newInputs[i].length = inputs[i].length;
			}
			copy = new Module(newInputs, function.clone(), returnsBoolean, receivesBoolean);
			return copy;
		}
	}
}
