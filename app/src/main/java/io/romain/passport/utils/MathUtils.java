package io.romain.passport.utils;

public final class MathUtils {


	public static float constrain(float min, float max, float v) {
		return Math.max(min, Math.min(max, v));
	}

	// Returns the next power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0 or
	// the answer overflows.
	public static int nextPowerOf2(int n) {
		if (n <= 0 || n > (1 << 30)) throw new IllegalArgumentException("n is invalid: " + n);
		n -= 1;
		n |= n >> 16;
		n |= n >> 8;
		n |= n >> 4;
		n |= n >> 2;
		n |= n >> 1;
		return n + 1;
	}

	// Returns the previous power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0
	public static int prevPowerOf2(int n) {
		if (n <= 0) throw new IllegalArgumentException();
		return Integer.highestOneBit(n);
	}

	public static int ceilLog2(float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if ((1 << i) >= value) break;
		}
		return i;
	}

	public static int floorLog2(float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if ((1 << i) > value) break;
		}
		return i - 1;
	}

	// Returns the input value x clamped to the range [min, max].
	public static int clamp(int x, int min, int max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	// Returns the input value x clamped to the range [min, max].
	public static float clamp(float x, float min, float max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	// Returns the input value x clamped to the range [min, max].
	public static long clamp(long x, long min, long max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

}
