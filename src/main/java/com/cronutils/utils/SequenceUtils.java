package com.cronutils.utils;

import java.lang.Integer;

public class SequenceUtils {

	private SequenceUtils() {
		//Private constructor to avoid instantiation
	}

	//https://en.wikipedia.org/wiki/Extended_Euclidean_algorithm#Pseudocode
	public static Integer[] extendedEuclideanAlgorithm(final Integer a, final Integer b) {

		int x = 0, y = 1, lastX = 1, lastY = 0, temp;
		int a1 = a, b1 = b;
		while (b1 != 0) {
			int q = a1 / b1;
			int r = a1 % b1;

			a1 = b1;
			b1 = r;

			temp = x;
			x = lastX - q * x;
			lastX = temp;

			temp = y;
			y = lastY - q * y;
			lastY = temp;
		}
		if (a1 > 0) {
			return new Integer[] {a1, lastX, lastY};
		} else {
			return new Integer[] {a1 * -1, lastX * -1, lastY * -1};
		}
	}

	//https://math.stackexchange.com/questions/1656120/formula-to-find-the-first-intersection-of-two-arithmetic-progressions
	public static boolean seriesOverlap(final Integer a1, final Integer d, final Integer b1, final Integer e, final Integer incrementsGcd) {

		boolean possibleOverlap = false;
		if (d == 0 && e != 0) {
			possibleOverlap = (a1 - b1) / e + 1 > 0;
		} else if (e == 0 && d != 0) {
			possibleOverlap = (b1 - a1) / d + 1 > 0;
		}
		else {
			possibleOverlap = true;
		}

		boolean overlap = false;
		if(possibleOverlap) {

			final Integer c = a1 - b1;

			if (!incrementsGcd.equals(0)) {
				final Integer modulo = c % incrementsGcd;
				overlap = modulo.equals(0);
			} else {
				overlap = c.equals(0);
			}
		}

		return overlap;
	}

	public static Integer firstOverlap(final Integer a1, final Integer d, final Integer b1, final Integer e) {

		Integer firstOverlap = Integer.MAX_VALUE;
		final Integer[] egcd = extendedEuclideanAlgorithm(-d, e);
		if (seriesOverlap(a1, d, b1, e, egcd[0])) {
			if (d != 0 && e != 0) {
				final int c = a1 - d + e - b1;
				final double t1 = Math.floor(-((double) c / e * egcd[1])) + 1;
				final double t2 = Math.floor(-((double) c / d * egcd[2])) + 1;
				final double maxT = Math.max(t1, t2);
				final double minN = ((double) c / egcd[0]) * egcd[1] + ((double) e / egcd[0]) * maxT;
				//				final Double minM = (c/egcd[0])*egcd[2] + (d/egcd[0])*maxT;
				firstOverlap = Double.valueOf(a1 + (minN - 1) * d).intValue();
			} else {
				if (d == 0) {
					firstOverlap = a1;
				}
				if (e == 0) {
					firstOverlap = b1;
				}
			}
		}
		return firstOverlap;
	}
}
