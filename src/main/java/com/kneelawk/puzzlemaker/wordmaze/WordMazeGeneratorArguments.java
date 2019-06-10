package com.kneelawk.puzzlemaker.wordmaze;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class WordMazeGeneratorArguments {
	private static final String HELP_TEXT = loadHelpText();
	private static final PropertiesConfiguration APPLICATION_PROPERTIES = loadApplicationProperties();
	private static final int DEFAULT_BOX_WIDTH = APPLICATION_PROPERTIES.getInt("boxWidth");
	private static final int DEFAULT_BOX_HEIGHT = APPLICATION_PROPERTIES.getInt("boxHeight");
	private static final String DEFAULT_ALPHABET = APPLICATION_PROPERTIES.getString("alphabet");

	private static String loadHelpText() {
		try {
			return IOUtils.toString(WordMazeGeneratorArguments.class.getResource("help.txt"), Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException("Unable to load help text", e);
		}
	}

	private static PropertiesConfiguration loadApplicationProperties() {
		try {
			PropertiesConfiguration props = new PropertiesConfiguration();
			props.read(new InputStreamReader(
					WordMazeGeneratorArguments.class.getResourceAsStream("application.properties")));
			return props;
		} catch (IOException | ConfigurationException e) {
			throw new RuntimeException("Unable to load application properties", e);
		}
	}

	private File outputPDF;
	private File answerPDF;
	private File inputCSV;
	private int mazeWidth;
	private int mazeHeight;
	private int boxWidth;
	private int boxHeight;
	private int startPosition;
	private int endPosition;
	private String alphabet;
	private int barrierRemovals;

	public File getOutputPDF() {
		return outputPDF;
	}

	public File getAnswerPDF() {
		return answerPDF;
	}

	public File getInputCSV() {
		return inputCSV;
	}

	public int getMazeWidth() {
		return mazeWidth;
	}

	public int getMazeHeight() {
		return mazeHeight;
	}

	public int getBoxWidth() {
		return boxWidth;
	}

	public int getBoxHeight() {
		return boxHeight;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getEndPosition() {
		return endPosition;
	}

	public String getAlphabet() {
		return alphabet;
	}

	public int getBarrierRemovals() {
		return barrierRemovals;
	}

	public void parseArguments(String[] args) {
		Parser parser = new Parser();
		parser.parse(args);

		if (parser.error) {
			printHelpAndExit(-1);
		}

		if (parser.outputPDF == null) {
			System.err.println("Missing --output option");
			printHelpAndExit(-1);
		}
		outputPDF = new File(parser.outputPDF).getAbsoluteFile();
		if (!outputPDF.getParentFile().exists()) {
			System.err.println("Output directory: \"" + outputPDF.getParent() + "\" does not exist.");
			printHelpAndExit(-1);
		}

		if (parser.answerPDF == null) {
			System.err.println("Missing --output-answer option");
			printHelpAndExit(-1);
		}
		answerPDF = new File(parser.answerPDF).getAbsoluteFile();
		if (!answerPDF.getParentFile().exists()) {
			System.err.println("Answer output directory: \"" + answerPDF.getParent() + "\" does not exist.");
			printHelpAndExit(-1);
		}

		if (parser.inputCSV == null) {
			System.err.println("Missing --intput option");
			printHelpAndExit(-1);
		}
		inputCSV = new File(parser.inputCSV).getAbsoluteFile();
		if (!inputCSV.exists()) {
			System.err.println("Input CSV file: \"" + inputCSV + "\" does not exist.");
			printHelpAndExit(-1);
		}

		if (parser.mazeWidth == null) {
			System.err.println("Missing --width option");
			printHelpAndExit(-1);
		}
		try {
			mazeWidth = Integer.parseInt(parser.mazeWidth);
		} catch (NumberFormatException e) {
			System.err.println("Width: " + parser.mazeWidth + " is not a valid integer.");
			printHelpAndExit(-1);
		}
		if (mazeWidth < 2) {
			System.err.println("Mazes must be at least 2 boxes wide");
			printHelpAndExit(-1);
		}

		if (parser.mazeHeight == null) {
			System.err.println("Missing --height option");
			printHelpAndExit(-1);
		}
		try {
			mazeHeight = Integer.parseInt(parser.mazeHeight);
		} catch (NumberFormatException e) {
			System.err.println("Height: " + parser.mazeHeight + " is not a valid integer.");
			printHelpAndExit(-1);
		}
		if (mazeHeight < 2) {
			System.err.println("Mazes must be at least 2 boxes high");
			printHelpAndExit(-1);
		}

		if (parser.boxWidth == null) {
			boxWidth = DEFAULT_BOX_WIDTH;
		} else {
			try {
				boxWidth = Integer.parseInt(parser.boxWidth);
			} catch (NumberFormatException e) {
				System.err.println("Box width: " + parser.boxWidth + " is not a valid integer.");
				printHelpAndExit(-1);
			}
		}
		if (boxWidth < 1) {
			System.err.println("Boxes must be at least 1 unit wide.");
			printHelpAndExit(-1);
		}

		if (parser.boxHeight == null) {
			boxHeight = DEFAULT_BOX_HEIGHT;
		} else {
			try {
				boxHeight = Integer.parseInt(parser.boxHeight);
			} catch (NumberFormatException e) {
				System.err.println("Box height: " + parser.boxHeight + " is not a valid integer.");
				printHelpAndExit(-1);
			}
		}
		if (boxHeight < 1) {
			System.err.println("Boxes must be at least 1 unit tall.");
			printHelpAndExit(-1);
		}

		if (parser.startPosition == null) {
			System.err.println("Missing --start option");
			printHelpAndExit(-1);
		}
		try {
			startPosition = Integer.parseInt(parser.startPosition);
		} catch (NumberFormatException e) {
			System.err.println("Start position: " + parser.startPosition + " is not a valid integer.");
			printHelpAndExit(-1);
		}
		if (startPosition < 0) {
			System.err.println("Mazes' start positions cannot be negative.");
			printHelpAndExit(-1);
		}
		if (startPosition >= 2 * mazeWidth + 2 * mazeHeight) {
			System.err.println("Mazes' start positions must be less than 2 * maze-width + 2 * maze-height");
			printHelpAndExit(-1);
		}

		if (parser.endPosition == null) {
			System.err.println("Missing --end option");
			printHelpAndExit(-1);
		}
		try {
			endPosition = Integer.parseInt(parser.endPosition);
		} catch (NumberFormatException e) {
			System.err.println("End position: " + parser.endPosition + " is not a valid integer.");
			printHelpAndExit(-1);
		}
		if (endPosition < 0) {
			System.err.println("Mazes' end positions cannot be negative.");
			printHelpAndExit(-1);
		}
		if (endPosition >= 2 * mazeWidth + 2 * mazeHeight) {
			System.err.println("Mazes' end positions must be less than 2 * maze-width + 2 * maze-height");
			printHelpAndExit(-1);
		}
		if (endPosition == startPosition) {
			System.err.println("The end position must be different from the start position.");
			printHelpAndExit(-1);
		}

		if (parser.alphabet == null) {
			alphabet = DEFAULT_ALPHABET;
		} else {
			alphabet = parser.alphabet;
		}

		if (parser.barrierRemovals == null) {
			System.err.println("Missing --barrier-removals option.");
			printHelpAndExit(-1);
		}
		try {
			barrierRemovals = Integer.parseInt(parser.barrierRemovals);
		} catch (NumberFormatException e) {
			System.err.println("Barrier removals: " + parser.barrierRemovals + " is not a valid integer");
			printHelpAndExit(-1);
		}
		if (barrierRemovals < 0) {
			System.err.println("Barrier removals must not be negative.");
			printHelpAndExit(-1);
		}
	}

	private void printHelpAndExit(int status) {
		System.err.println(HELP_TEXT);
		System.exit(status);
	}

	private class Parser {
		String outputPDF;
		String answerPDF;
		String inputCSV;
		String mazeWidth;
		String mazeHeight;
		String boxWidth;
		String boxHeight;
		String startPosition;
		String endPosition;
		String alphabet;
		String barrierRemovals;
		boolean error;

		void parse(String[] args) {
			boolean parsingOutputPDF = false, parsingAnswerPDF = false, parsingInputCSV = false, parsingMazeWidth =
					false, parsingMazeHeight = false, parsingBoxWidth = false, parsingBoxHeight = false,
					parsingStartPostioin = false, parsingEndPosition = false, parsingAlphabet = false,
					parsingBarrierRemovals = false;
			for (String arg : args) {
				if (parsingOutputPDF) {
					outputPDF = arg;
					parsingOutputPDF = false;
				} else if (parsingAnswerPDF) {
					answerPDF = arg;
					parsingAnswerPDF = false;
				} else if (parsingInputCSV) {
					inputCSV = arg;
					parsingInputCSV = false;
				} else if (parsingMazeWidth) {
					mazeWidth = arg;
					parsingMazeWidth = false;
				} else if (parsingMazeHeight) {
					mazeHeight = arg;
					parsingMazeHeight = false;
				} else if (parsingBoxWidth) {
					boxWidth = arg;
					parsingBoxWidth = false;
				} else if (parsingBoxHeight) {
					boxHeight = arg;
					parsingBoxHeight = false;
				} else if (parsingStartPostioin) {
					startPosition = arg;
					parsingStartPostioin = false;
				} else if (parsingEndPosition) {
					endPosition = arg;
					parsingEndPosition = false;
				} else if (parsingAlphabet) {
					alphabet = arg;
					parsingAlphabet = false;
				} else if (parsingBarrierRemovals) {
					barrierRemovals = arg;
					parsingBarrierRemovals = false;
				} else {
					if (arg.startsWith("-")) {
						if (arg.startsWith("--")) {
							String argValue = null;
							if (arg.contains("=")) {
								int equalsIndex = arg.indexOf('=');
								argValue = arg.substring(equalsIndex + 1);
								arg = arg.substring(equalsIndex);
							}

							switch (arg) {
								case "--help":
									printHelpAndExit(0);
									break;
								case "--output":
									if (argValue == null) {
										parsingOutputPDF = true;
									} else {
										outputPDF = argValue;
									}
									break;
								case "--output-answer":
									if (argValue == null) {
										parsingAnswerPDF = true;
									} else {
										answerPDF = argValue;
									}
									break;
								case "--input":
									if (argValue == null) {
										parsingInputCSV = true;
									} else {
										inputCSV = argValue;
									}
									break;
								case "--width":
									if (argValue == null) {
										parsingMazeWidth = true;
									} else {
										mazeWidth = argValue;
									}
									break;
								case "--height":
									if (argValue == null) {
										parsingMazeHeight = true;
									} else {
										mazeHeight = argValue;
									}
									break;
								case "--box-width":
									if (argValue == null) {
										parsingBoxWidth = true;
									} else {
										boxWidth = argValue;
									}
									break;
								case "--box-height":
									if (argValue == null) {
										parsingBoxHeight = true;
									} else {
										boxHeight = argValue;
									}
									break;
								case "--start":
									if (argValue == null) {
										parsingStartPostioin = true;
									} else {
										startPosition = argValue;
									}
									break;
								case "--end":
									if (argValue == null) {
										parsingEndPosition = true;
									} else {
										endPosition = argValue;
									}
									break;
								case "--alphabet":
									if (argValue == null) {
										parsingAlphabet = true;
									} else {
										alphabet = argValue;
									}
									break;
								case "--barrier-removals":
									if (argValue == null) {
										parsingBarrierRemovals = true;
									} else {
										barrierRemovals = argValue;
									}
									break;
								default:
									System.err.println("Unknown option: '" + arg + '\'');
									error = true;
									break;
							}
						} else {
							arg = arg.substring(1);
							while (!arg.isEmpty()) {
								char option = arg.charAt(0);
								arg = arg.substring(1);
								switch (option) {
									case 'o':
										if (arg.isEmpty()) {
											parsingOutputPDF = true;
										} else {
											outputPDF = arg;
											arg = "";
										}
										break;
									case 'O':
										if (arg.isEmpty()) {
											parsingAnswerPDF = true;
										} else {
											answerPDF = arg;
											arg = "";
										}
										break;
									case 'i':
										if (arg.isEmpty()) {
											parsingInputCSV = true;
										} else {
											inputCSV = arg;
											arg = "";
										}
										break;
									case 'w':
										if (arg.isEmpty()) {
											parsingMazeWidth = true;
										} else {
											mazeWidth = arg;
											arg = "";
										}
										break;
									case 'h':
										if (arg.isEmpty()) {
											parsingMazeHeight = true;
										} else {
											mazeHeight = arg;
											arg = "";
										}
										break;
									case 's':
										if (arg.isEmpty()) {
											parsingStartPostioin = true;
										} else {
											startPosition = arg;
											arg = "";
										}
										break;
									case 'e':
										if (arg.isEmpty()) {
											parsingEndPosition = true;
										} else {
											endPosition = arg;
											arg = "";
										}
										break;
									case 'a':
										if (arg.isEmpty()) {
											parsingAlphabet = true;
										} else {
											alphabet = arg;
											arg = "";
										}
										break;
									case 'b':
										if (arg.isEmpty()) {
											parsingBarrierRemovals = true;
										} else {
											barrierRemovals = arg;
											arg = "";
										}
										break;
									default:
										System.err.println("Unknown short option: '-" + option + '\'');
										error = true;
										break;
								}
							}
						}
					} else {
						System.err.println("Unknown option value: '" + arg + '\'');
						error = true;
					}
				}
			}
		}
	}
}
