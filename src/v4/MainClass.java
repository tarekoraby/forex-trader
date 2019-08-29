package v4;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

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
			int numOfInputFiles = countLines("C:\\Users\\taor9299\\Box Sync\\Java workspace\\ForexTrader\\v3-input_file_config.txt");
			String[] fileNames = new String[numOfInputFiles];
			int[] numOfCases = new int[numOfInputFiles];
			System.out.println(numOfInputFiles + " input files");
			in = new BufferedReader(new FileReader(
					"C:\\Users\\taor9299\\Box Sync\\Java workspace\\ForexTrader\\v3-input_file_config.txt"));
			for (int i = 0; i < numOfInputFiles; i++) {
				fileNames[i] = in.readLine();
				numOfCases[i] = countLines(fileNames[i]);
				System.out.println(fileNames[i] + " has " + numOfCases[i] + " cases");
			}

			MV.rawData = new double[numOfInputFiles][][];
			for (int i = 0; i < numOfInputFiles; i++) {
				MV.rawData[i] = new double[numOfCases[i]][3];
				String currentFile = fileNames[i];
				in = new BufferedReader(new FileReader(currentFile));
				for (int j = 0; j < numOfCases[i]; j++) {
					line = in.readLine();
					StringTokenizer tokens = new StringTokenizer(line);
					for (int k = 0; k < 3; k++) {
						MV.rawData[i][j][k] = Double.parseDouble(tokens.nextToken().trim());
					}
				}
			}

			MV.initialize();
			in.close();

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public static int countLines(String filename) throws IOException {
		InputStream is = new BufferedInputStream(new FileInputStream(filename));
		try {
			byte[] c = new byte[1024];
			int count = 0;
			int readChars = 0;
			boolean empty = true;
			while ((readChars = is.read(c)) != -1) {
				empty = false;
				for (int i = 0; i < readChars; ++i) {
					if (c[i] == '\n') {
						++count;
					}
				}
			}
			return (count == 0 && !empty) ? 1 : count;
		} finally {
			is.close();
		}
	}
}
