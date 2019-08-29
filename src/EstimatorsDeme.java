
public class EstimatorsDeme {
	private EstimatorStrategy[] EstimatorDeme;

	EstimatorsDeme(int demeSize){
		EstimatorDeme=new EstimatorStrategy[demeSize];
	}
	
	EstimatorsDeme(int demeSize, EstimatorStrategy[] estimatorStrategies){
		EstimatorDeme=estimatorStrategies;
	}
	
	EstimatorStrategy[] getEstimatorDeme(){
		return EstimatorDeme;
	}
}
