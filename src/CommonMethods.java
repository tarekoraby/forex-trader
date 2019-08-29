import v1.MasterVariables;



public class CommonMethods {

	int PC;
	char[] strategy;
	double[] inputVariables;
	
	double runStrategy(char[] strategy, double[] inputVariables){
		PC = 0;
		this.strategy = strategy;
		this.inputVariables = inputVariables;
		return(run());
	}
	
	private double run() {
		char primitive = strategy[PC++];

		switch (primitive) {
		case MasterVariables.CAPMED:
			return (capMed);
		case MasterVariables.CAPSTD:
			return (capStd);
		case MasterVariables.CAPMIN:
			return (capMin);
		case MasterVariables.CAPMAX:
			return (capMax);
		case MasterVariables.MYCAP:
			return (myCap);
		case MasterVariables.OPPCAP:
			return (oppCap);
		case MasterVariables.MYSIDECAP:
			return (mySideCap);
		case MasterVariables.LEFTCAPSUM:
			return (leftCapSum);
		case MasterVariables.MYENMITY:
			return (myEnmity);
		case MasterVariables.OPPENMITY:
			return (oppEnmity);
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
			return (MasterVariables.randNum[primitive]);
		}

	}
}
