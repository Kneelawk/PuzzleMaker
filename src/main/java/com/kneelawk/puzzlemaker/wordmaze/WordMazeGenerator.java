package com.kneelawk.puzzlemaker.wordmaze;

import com.google.common.collect.Lists;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2DFontTextDrawer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.encoding.WinAnsiEncoding;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class WordMazeGenerator {
	private static final String FONT_RESOURCE = "NotoMono-Regular.ttf";

	public static void main(String[] args) {
		WordMazeGeneratorArguments arguments = new WordMazeGeneratorArguments();
		arguments.parseArguments(args);

		Random random = new Random();
		int mazeWidth = arguments.getMazeWidth();
		int mazeHeight = arguments.getMazeHeight();
		int boxWidth = arguments.getBoxWidth();
		int boxHeight = arguments.getBoxHeight();

		// load csv
		StringBuilder wordString = new StringBuilder();
		List<String> questions = Lists.newArrayList();
		try {
			CSVParser questionCSV = CSVFormat.RFC4180.parse(new FileReader(arguments.getInputCSV()));
			for (CSVRecord questionRecord : questionCSV) {
				questions.add(questionRecord.get(0));
				wordString.append(questionRecord.get(1).trim().toUpperCase());
			}
		} catch (IOException e) {
			System.err.println("Unable to load CSV file: " + arguments.getInputCSV());
			System.exit(-1);
		}

		WordMaze maze = new WordMaze(random, mazeWidth, mazeHeight, boxWidth, boxHeight, arguments.getStartPosition(),
				arguments.getEndPosition());
		System.out.println("Generating maze...");
		maze.generateMaze(arguments.getBarrierRemovals());

		System.out.println("Solving maze...");
		try {
			maze.pathWordString(wordString.toString());
		} catch (UnsolvableException e) {
			System.err.println(
					"Unable to solve the randomly generated maze for a path of length: " + wordString.length());
			System.err.println(
					"Perhaps there is an issue with the maze-generator settings (maze too large for word-string path, maze too small for word-string path, too few alternate removed barriers).");
			System.err.println(
					"Perhaps this execution was just unlucky and the generated maze couldn't be solved the right way.");
			System.err.println("Either way, you will need to re-execute the program.");
			System.exit(2);
		}

		System.out.println("Writing answer pdf...");
		writePDF(maze, questions, arguments.getAnswerPDF(), mazeWidth, mazeHeight, boxWidth, boxHeight);

		System.out.println("Filling the maze with extra letters...");
		maze.fillRandomCharacters(arguments.getAlphabet());

		System.out.println("Writing resulting pdf...");
		writePDF(maze, questions, arguments.getOutputPDF(), mazeWidth, mazeHeight, boxWidth, boxHeight);
	}

	private static void writePDF(WordMaze maze, List<String> questions, File output, int mazeWidth, int mazeHeight,
								 int boxWidth, int boxHeight) {
		int canvasWidth = boxWidth * mazeWidth + 80;
		int canvasHeight = boxHeight * mazeHeight + 80;
		try (PdfBoxGraphics2DFontTextDrawer fontTextDrawer = new PdfBoxGraphics2DFontTextDrawer()) {
			// setup document
			PDDocument document = new PDDocument();

			// setup fonts
			PDFont textFont = PDTrueTypeFont.load(document, WordMazeGenerator.class.getResourceAsStream(FONT_RESOURCE),
					WinAnsiEncoding.INSTANCE);
			Font graphicsFont =
					Font.createFont(Font.TRUETYPE_FONT, WordMazeGenerator.class.getResourceAsStream(FONT_RESOURCE))
							.deriveFont(boxHeight - 6f);
			fontTextDrawer.registerFont(graphicsFont.getFontName(), textFont);

			// setup page
			PDPage mazePage = new PDPage(new PDRectangle(canvasWidth,
					canvasHeight));
			document.addPage(mazePage);

			// setup content stream
			PDPageContentStream mazeContentStream = new PDPageContentStream(document, mazePage);

			// setup the maze canvas
			PdfBoxGraphics2D graphics2D = new PdfBoxGraphics2D(document, canvasWidth, canvasHeight);

			// draw the maze
			graphics2D.setFont(graphicsFont);
			graphics2D.setFontTextDrawer(fontTextDrawer);
			graphics2D.translate(40, 40);
			maze.draw(graphics2D);
			graphics2D.dispose();

			// draw the maze canvas to the content stream
			PDFormXObject xObject = graphics2D.getXFormObject();
			mazeContentStream.drawForm(xObject);

			// close the maze stream
			mazeContentStream.close();

			// split wrap question text
			float leftMargin = 72, rightMargin = 72, topMargin = 72, bottomMargin = 72;
			float fontSize = 12;
			int pageWidth = (int) ((PDRectangle.A4.getWidth() - leftMargin - rightMargin) /
					(textFont.getFontDescriptor().getFontBoundingBox().getWidth() / 1000f * fontSize));
			int pageHeight = (int) ((PDRectangle.A4.getHeight() - topMargin - bottomMargin) /
					(textFont.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize));
			List<String> lines = Lists.newArrayList();
			for (int i = 0; i < questions.size(); i++) {
				String question = String.format("% 2d. %s", i, questions.get(i));
				while (question.length() > pageWidth) {
					int split = question.lastIndexOf(' ', pageWidth);
					lines.add(question.substring(0, split));
					question = "    " + question.substring(split + 1);
				}
				lines.add(question);
			}

			while (lines.size() > pageHeight) {
				writePage(document, textFont, rightMargin, topMargin, fontSize, lines.subList(0, pageHeight));
				lines = lines.subList(pageHeight, lines.size());
			}
			writePage(document, textFont, rightMargin, topMargin, fontSize, lines);

			// save the pdf
			document.save(output);
			document.close();
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
	}

	private static void writePage(PDDocument document, PDFont textFont, float rightMargin, float topMargin,
								  float fontSize, List<String> lines) throws IOException {
		// start the question page and stream
		PDPage questionPage = new PDPage(PDRectangle.A4);
		document.addPage(questionPage);
		PDPageContentStream questionContentStream = new PDPageContentStream(document, questionPage);

		// add question text
		questionContentStream.beginText();
		drawText(questionContentStream, textFont, fontSize, rightMargin, PDRectangle.A4.getHeight() - topMargin, lines);
		questionContentStream.endText();
		questionContentStream.close();
	}

	private static void drawText(PDPageContentStream stream, PDFont font, float fontSize, float x, float y,
								 List<String> lines)
			throws IOException {
		stream.setFont(font, fontSize);
		float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000f * fontSize;
		float lineOffset = textHeight * 1.15f;

		stream.newLineAtOffset(x, y - textHeight);
		stream.setLeading(lineOffset);
		stream.showText(lines.get(0));
		for (int i = 1; i < lines.size(); i++) {
			stream.newLine();
			stream.showText(lines.get(i));
		}
	}
}
