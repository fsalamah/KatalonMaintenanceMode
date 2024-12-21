package com.salama.common;



import java.util.Scanner;

public class ScannerSingleton {
	// Private static instance of Scanner
	private static Scanner instance;

	// Private constructor to prevent instantiation
	private ScannerSingleton() {
		// Prevent instantiation
	}

	// Public method to provide access to the instance
	public static Scanner getInstance() {
		// Lazy initialization
		if (instance == null) {
			instance = new Scanner(System.in);
		}
		return instance;
	}

	// Optionally, provide a method to close the scanner
	public static void closeScanner() {
		if (instance != null) {
			instance.close();
			instance = null; // Allow for GC
		}
	}
}