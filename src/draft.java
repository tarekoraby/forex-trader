import java.util.Arrays;
import java.util.Random;

import javax.naming.ldap.Rdn;

public class draft {

	static Random rd = new Random();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// castCharInt();
		// cloneTest();
		// absTest();
		//rangeTest();
		double[] x = {0.2, 0.1, 0.1, 0.1};	
		System.out.print(mode(x));

	}
	
	public static double mode(double a[]) {
		double maxValue=-99;
		int maxCount = 0;

		for (int i = 0; i < a.length; ++i) {
			int count = 0;
			for (int j = 0; j < a.length; ++j) {
				if (a[j] == a[i])
					++count;
			}
			if (count > maxCount) {
	            maxCount = count;
	            maxValue = a[i];
	        }
	    }

	    return maxValue;
	}

	private static void rangeTest() {
		int FSET_5_START = -7;
		int FSET_5_END = -7;

		for (int i = 0; i < 1000; i++) {
			int x = FSET_5_START - ( rd.nextInt(FSET_5_START - FSET_5_END + 1));

			System.out.println(x);
		}
	}

	private static void absTest() {
		double x = 1.24435345345;
		double y = 0.123243534534536;
		double z = Math.abs(y - x);
		System.out.println(z);

	}

	private static void cloneTest() {
		int[] a = { 1, 1, 1 };
		int[] b = a.clone();
		b[0] = 10;
		System.out.println(a[0]);
		System.out.println(b[0]);

	}

	private static void castCharInt() {
		char x = '1';
		int y = x;

	}

}
