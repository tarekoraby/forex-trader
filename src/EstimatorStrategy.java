public class EstimatorStrategy {
	private char[] estimatorStrategy;
	double fitness;

	EstimatorStrategy() {
		fitness = 0;
	}

	EstimatorStrategy(char[] estimatorStrategy) {
		fitness = 0;
		this.estimatorStrategy = estimatorStrategy;
	}

	double estimate(double[] input) {
		double output = 0;
		// to do
		return output;
	}

	void setEstimatorStrategy(char[] estimatorStrategy) {
		this.estimatorStrategy = estimatorStrategy;
	}
	
	char[] getEstimatorStrategy() {
		return  estimatorStrategy;
	}
}
