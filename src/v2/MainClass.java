package v2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.StringTokenizer;

import org.w3c.dom.css.Counter;

public class MainClass {

	public static void main(String[] args) {
		initialize();
		simulate();
	}

	private static void simulate() {

		Simulator simulator = new Simulator();
		simulator.simulate();

	}

	private static void initialize() {
		String line;
		BufferedReader in;
		try {
			in = new BufferedReader(new FileReader(
					"C:\\Users\\taor9299\\Box Sync\\Java workspace\\ForexTrader\\input_file_config.txt"));
			line = in.readLine();
			StringTokenizer tokens = new StringTokenizer(line);
			int numOfInputs = Integer.parseInt(tokens.nextToken().trim());
			int fitnesscases = Integer.parseInt(tokens.nextToken().trim());
			double[][] fitnessCases = new double[fitnesscases][numOfInputs + 1];
			in = new BufferedReader(new FileReader(
					"C:\\Users\\taor9299\\Box Sync\\Java workspace\\ForexTrader\\fitnessCases.txt"));
			for (int i = 0; i < fitnesscases; i++) {
				line = in.readLine();
				tokens = new StringTokenizer(line);
				for (int j = 0; j < numOfInputs + 1; j++) {
					fitnessCases[i][j] = Double.parseDouble(tokens.nextToken().trim());
				}
			}
			MV.initialize(numOfInputs, fitnessCases);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR: Please provide a setup and data files");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("ERROR: Incorrect data format");
			System.exit(0);
		}
	}
}
