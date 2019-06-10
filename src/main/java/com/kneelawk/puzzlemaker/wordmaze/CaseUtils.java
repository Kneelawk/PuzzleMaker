package com.kneelawk.puzzlemaker.wordmaze;

public class CaseUtils {
	public static String toUpperCase(String input) {
		StringBuilder sb = new StringBuilder();

		for (char c : input.toCharArray()) {
			sb.append(Character.toUpperCase(c));
		}

		return sb.toString();
	}
}
