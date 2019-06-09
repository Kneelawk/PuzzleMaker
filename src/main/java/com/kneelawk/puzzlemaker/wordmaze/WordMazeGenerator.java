package com.kneelawk.puzzlemaker.wordmaze;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2DFontTextDrawer;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2DFontTextDrawerDefaultFonts;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class WordMazeGenerator {
	private static final String FONT_RESOURCE = "NotoMono-Regular.ttf";

	public static void main(String[] args) throws UnsolvableException {
		WordMazeGeneratorArguments arguments = new WordMazeGeneratorArguments();
		arguments.parseArguments(args);

		Random random = new Random();
		int mazeWidth = arguments.getMazeWidth();
		int mazeHeight = arguments.getMazeHeight();
		int boxWidth = arguments.getBoxWidth();
		int boxHeight = arguments.getBoxHeight();

		WordMaze maze = new WordMaze(random, mazeWidth, mazeHeight, boxWidth, boxHeight);
		System.out.println("Generating maze...");
		maze.generateMaze(arguments.getStartPosition(), arguments.getEndPosition(), arguments.getBarrierRemovals());

		System.out.println("Solving maze...");
		maze.addWordString(arguments.getStartPosition(), arguments.getEndPosition(), "HELLOWORLDTHISISATESTOFMYMAZEGENERATOR");

		int canvasWidth = boxWidth * mazeWidth + 100;
		int canvasHeight = boxHeight * mazeHeight + 100;

		System.out.println("Writing pdf...");
		try (PdfBoxGraphics2DFontTextDrawer fontTextDrawer = new PdfBoxGraphics2DFontTextDrawer()) {
			PDDocument document = new PDDocument();
			PDPage page = new PDPage(new PDRectangle(canvasWidth,
					canvasHeight));
			document.addPage(page);


			fontTextDrawer.registerFont(WordMazeGenerator.class.getResourceAsStream(FONT_RESOURCE));
			Font font = Font.createFont(Font.TRUETYPE_FONT, WordMazeGenerator.class.getResourceAsStream(FONT_RESOURCE)).deriveFont(boxHeight - 6f);

			PdfBoxGraphics2D graphics2D = new PdfBoxGraphics2D(document, canvasWidth, canvasHeight);

			graphics2D.setFont(font);
			graphics2D.setFontTextDrawer(fontTextDrawer);
			graphics2D.translate(20, 20);
			maze.draw(graphics2D, arguments.getEndPosition());
			graphics2D.dispose();

			PDFormXObject xObject = graphics2D.getXFormObject();
			PDPageContentStream contentStream = new PDPageContentStream(document, page);
			contentStream.drawForm(xObject);
			contentStream.close();

			document.save(arguments.getOutputPDF());
			document.close();
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
	}
}
