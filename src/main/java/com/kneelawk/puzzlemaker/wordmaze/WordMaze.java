package com.kneelawk.puzzlemaker.wordmaze;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WordMaze {
	private static final float MAZE_CHANCE_TO_SPLIT = 0.2f;

	private Random random;
	private int width;
	private int height;
	private int boxWidth;
	private int boxHeight;
	private int startPosition;
	private int endPosition;
	private boolean[][] verticals;
	private boolean[][] horizontals;
	private char[][] letters;
	private char extraLetter;

	public WordMaze(Random random, int width, int height, int boxWidth, int boxHeight, int startPosition,
					int endPosition) {
		this.random = random;
		this.width = width;
		this.height = height;
		this.boxWidth = boxWidth;
		this.boxHeight = boxHeight;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		verticals = new boolean[height][width + 1];
		horizontals = new boolean[height + 1][width];
		letters = new char[height][width];
	}

	private void setupBoundary() {
		for (int y = 0; y < height; y++) {
			verticals[y][0] = true;
			verticals[y][width] = true;
		}
		for (int x = 0; x < width; x++) {
			horizontals[0][x] = true;
			horizontals[height][x] = true;
		}
	}

	public void setPerimeter(int circumference, boolean value) {
		if (circumference < 0) {
			throw new IndexOutOfBoundsException("Circumference cannot be negative");
		}
		if (circumference < width) {
			horizontals[0][circumference] = value;
		} else if (circumference < width + height) {
			verticals[circumference - width][width] = value;
		} else if (circumference < 2 * width + height) {
			horizontals[height][2 * width + height - circumference - 1] = value;
		} else if (circumference < 2 * width + 2 * height) {
			verticals[2 * width + 2 * height - circumference - 1][0] = value;
		} else {
			throw new IndexOutOfBoundsException("Circumference is greater than that of this maze");
		}
	}

	public void fillBarriers() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				verticals[y][x] = true;
				horizontals[y][x] = true;
			}
			verticals[y][width] = true;
		}
		for (int x = 0; x < width; x++) {
			horizontals[height][x] = true;
		}
	}

	public void setHorizontal(int x, int y, boolean value) {
		horizontals[y][x] = value;
	}

	public void setVertical(int x, int y, boolean value) {
		verticals[y][x] = value;
	}

	public void setBoxSide(Vec2i loc, BoxSide side, boolean value) {
		switch (side) {
			case TOP:
				horizontals[loc.y][loc.x] = value;
				break;
			case RIGHT:
				verticals[loc.y][loc.x + 1] = value;
				break;
			case BOTTOM:
				horizontals[loc.y + 1][loc.x] = value;
				break;
			case LEFT:
				verticals[loc.y][loc.x] = value;
				break;
		}
	}

	public List<BoxSide> getAvailableDirections(Vec2i loc) {
		List<BoxSide> list = Lists.newArrayList();
		if (loc.y > 0 && !horizontals[loc.y][loc.x]) {
			list.add(BoxSide.TOP);
		}
		if (loc.x < width - 1 && !verticals[loc.y][loc.x + 1]) {
			list.add(BoxSide.RIGHT);
		}
		if (loc.y < height - 1 && !horizontals[loc.y + 1][loc.x]) {
			list.add(BoxSide.BOTTOM);
		}
		if (loc.x > 0 && !verticals[loc.y][loc.x]) {
			list.add(BoxSide.LEFT);
		}
		return list;
	}

	public boolean getBoxSide(Vec2i loc, BoxSide side) {
		switch (side) {
			case TOP:
				return horizontals[loc.y][loc.x];
			case RIGHT:
				return verticals[loc.y][loc.x + 1];
			case BOTTOM:
				return horizontals[loc.y + 1][loc.x];
			case LEFT:
				return verticals[loc.y][loc.x];
		}
		return false;
	}

	public void setCharacter(Vec2i loc, char c) {
		letters[loc.y][loc.x] = c;
	}

	public void clearBarriersForMaze() {
		new MazeGenerator().generate();
	}

	public void clearRandomBarriers(int barrierCount) {
		for (int i = 0; i < barrierCount; i++) {
			if (random.nextBoolean()) {
				int x, y;
				do {
					y = random.nextInt(height - 2) + 1;
					x = random.nextInt(width - 1) + 1;
				} while (!verticals[y][x]);
				verticals[y][x] = false;
			} else {
				int x, y;
				do {
					y = random.nextInt(height - 1) + 1;
					x = random.nextInt(width - 2) + 1;
				} while (!horizontals[y][x]);
				horizontals[y][x] = false;
			}
		}
	}

	public void generateMaze(int barrierRemovals) {
		fillBarriers();
		clearBarriersForMaze();
		setPerimeter(startPosition, false);
		setPerimeter(endPosition, false);
		clearRandomBarriers(barrierRemovals);
	}

	public List<Vec2i> solve(Vec2i start, Vec2i end, int index, String wordString,
							 Map<Vec2i, Character> letters) {
		if (index >= wordString.length() - 2 && start.equals(end)) {
			return ImmutableList.of(start);
		} else if (index >= wordString.length() - 1) {
			return null;
		} else {
			Map<Vec2i, Character> newLetters = letters;
			if (!letters.containsKey(start)) {
				newLetters = ImmutableMap.<Vec2i, Character>builder().putAll(letters)
						.put(start, wordString.charAt(index)).build();
			}
			List<BoxSide> availableDirections = getAvailableDirections(start);
			while (!availableDirections.isEmpty()) {
				BoxSide direction = availableDirections.remove(random.nextInt(availableDirections.size()));
				Vec2i child = start.add(direction.getVec());
				if (!letters.containsKey(child) ||
						(letters.containsKey(child) && letters.get(child) == wordString.charAt(index + 1))) {
					List<Vec2i> path = solve(child, end, index + 1, wordString, newLetters);
					if (path != null) {
						return ImmutableList.<Vec2i>builder().add(start).addAll(path).build();
					}
				}
			}
			return null;
		}
	}

	public Vec2i getPerimeterVec(int circumference) {
		if (circumference < 0) {
			throw new IndexOutOfBoundsException("Circumference cannot be negative");
		}
		if (circumference < width) {
			return new Vec2i(circumference, 0);
		} else if (circumference < width + height) {
			return new Vec2i(width - 1, circumference - width);
		} else if (circumference < 2 * width + height) {
			return new Vec2i(2 * width + height - circumference - 1, height - 1);
		} else if (circumference < 2 * width + 2 * height) {
			return new Vec2i(0, 2 * width + 2 * height - circumference - 1);
		} else {
			throw new IndexOutOfBoundsException("Circumference is larger than that of this maze");
		}
	}

	public BoxSide getPerimeterSide(int circumference) {
		if (circumference < 0) {
			throw new IndexOutOfBoundsException("Circumference cannot be negative");
		}
		if (circumference < width) {
			return BoxSide.TOP;
		} else if (circumference < width + height) {
			return BoxSide.RIGHT;
		} else if (circumference < 2 * width + height) {
			return BoxSide.BOTTOM;
		} else if (circumference < 2 * width + 2 * height) {
			return BoxSide.LEFT;
		} else {
			throw new IndexOutOfBoundsException("Circumference is larger than that of this maze");
		}
	}

	public List<List<Vec2i>> pathWordString(List<String> words) throws UnsolvableException {
		String wordString = words.stream().reduce("", String::concat);

		List<Vec2i> path = solve(getPerimeterVec(startPosition), getPerimeterVec(endPosition), 0, wordString,
				ImmutableMap.of());

		if (path == null) {
			throw new UnsolvableException(
					"This maze cannot be solved with a path of length: " + wordString.length());
		}

		int i = 0, pathSize = path.size();
		List<List<Vec2i>> alternateLocationsList = Lists.newArrayList();
		Vec2i lastVec = null;

		outerFor:
		for (String word : words) {
			List<Vec2i> alternateLocations = Lists.newArrayList();
			Vec2i loc = path.get(i);
			if (lastVec == null) {
				alternateLocations.add(loc);
			} else {
				List<BoxSide> availableDirections = getAvailableDirections(lastVec);
				for (BoxSide direction : availableDirections) {
					alternateLocations.add(lastVec.add(direction.getVec()));
				}
			}
			alternateLocationsList.add(alternateLocations);
			for (char c : word.toCharArray()) {
				if (i >= pathSize) {
					break outerFor;
				}
				loc = path.get(i);
				setCharacter(loc, c);
				i++;
				lastVec = loc;
			}
		}

		if (i < wordString.length()) {
			extraLetter = wordString.charAt(wordString.length() - 1);
		}

		return alternateLocationsList;
	}

	public List<Vec2i> findExactLetterPath(Vec2i start, int index, String wordString,
										   Map<Vec2i, Character> letterMap) {
		if (index >= wordString.length() - 1) {
			return ImmutableList.of(start);
		} else {
			Map<Vec2i, Character> newLetters = letterMap;
			if (!letterMap.containsKey(start)) {
				newLetters = ImmutableMap.<Vec2i, Character>builder().putAll(letterMap)
						.put(start, wordString.charAt(index)).build();
			}
			List<BoxSide> availableDirections = getAvailableDirections(start);
			while (!availableDirections.isEmpty()) {
				BoxSide direction = availableDirections.remove(random.nextInt(availableDirections.size()));
				Vec2i child = start.add(direction.getVec());
				char childChar = wordString.charAt(index + 1);
				if ((!letterMap.containsKey(child) ||
						(letterMap.containsKey(child) && letterMap.get(child) == childChar)) &&
						(letters[child.y][child.x] == 0 || letters[child.y][child.x] == childChar)) {
					List<Vec2i> path = findExactLetterPath(child, index + 1, wordString, newLetters);
					if (path != null) {
						return ImmutableList.<Vec2i>builder().add(start).addAll(path).build();
					}
				}
			}
			return null;
		}
	}

	public List<Vec2i> findLetterPath(Vec2i start, int index, String wordString, Map<Vec2i, Character> letterMap) {
		if (index >= wordString.length() - 1) {
			return ImmutableList.of(start);
		} else {
			Map<Vec2i, Character> newLetters = letterMap;
			if (!letterMap.containsKey(start)) {
				newLetters = ImmutableMap.<Vec2i, Character>builder().putAll(letterMap)
						.put(start, wordString.charAt(index)).build();
			}
			List<BoxSide> availableDirections = getAvailableDirections(start);
			while (!availableDirections.isEmpty()) {
				BoxSide direction = availableDirections.remove(random.nextInt(availableDirections.size()));
				Vec2i child = start.add(direction.getVec());
				char childChar = wordString.charAt(index + 1);
				if ((!letterMap.containsKey(child) ||
						(letterMap.containsKey(child) && letterMap.get(child) == childChar)) &&
						(letters[child.y][child.x] == 0 || letters[child.y][child.x] == childChar)) {
					List<Vec2i> path = findLetterPath(child, index + 1, wordString, newLetters);
					return ImmutableList.<Vec2i>builder().add(start).addAll(path).build();
				}
			}
			return ImmutableList.of(start);
		}
	}

	public void drawAlternateAnswer(Vec2i start, String answer) {
		List<Vec2i> path = findExactLetterPath(start, 0, answer, ImmutableMap.of());
		if (path == null) {
			path = findLetterPath(start, 0, answer, ImmutableMap.of());
		}

		int pathSize = path.size();
		for (int i = 0; i < pathSize; i++) {
			Vec2i loc = path.get(i);
			letters[loc.y][loc.x] = answer.charAt(i);
		}
	}

	public void addAlternateAnswers(List<List<Vec2i>> alternateLocations, List<List<String>> alternateAnswers) {
		if (alternateAnswers.size() != alternateLocations.size()) {
			throw new IllegalArgumentException(
					"the number of location alternate splits does not equal the number of answer alternate splits");
		}
		int alternateSize = alternateAnswers.size();
		for (int i = 0; i < alternateSize; i++) {
			List<String> answers = alternateAnswers.get(i);
			List<Vec2i> locations = alternateLocations.get(i);
			for (String answer : answers) {
				char firstChar = answer.charAt(0);
				List<Vec2i> availableLocations = locations.stream()
						.filter(vec -> letters[vec.y][vec.x] == 0 || letters[vec.y][vec.x] == firstChar)
						.collect(Collectors.toList());
				if (!availableLocations.isEmpty()) {
					drawAlternateAnswer(availableLocations.get(random.nextInt(availableLocations.size())), answer);
				}
			}
		}
	}

	public void fillRandomCharacters(String alphabet) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (letters[y][x] == 0) {
					letters[y][x] = alphabet.charAt(random.nextInt(alphabet.length()));
				}
			}
		}
	}

	public void draw(Graphics2D graphics2D) {
		graphics2D.setStroke(new BasicStroke(2));
		graphics2D.setColor(Color.BLACK);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x <= width; x++) {
				if (verticals[y][x]) {
					graphics2D.drawLine(x * boxWidth, y * boxHeight, x * boxWidth, y * boxHeight + boxHeight);
				}
			}
		}
		for (int y = 0; y <= height; y++) {
			for (int x = 0; x < width; x++) {
				if (horizontals[y][x]) {
					graphics2D.drawLine(x * boxWidth, y * boxHeight, x * boxWidth + boxWidth, y * boxHeight);
				}
			}
		}
		FontMetrics metrics = graphics2D.getFontMetrics();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (letters[y][x] != 0) {
					graphics2D.drawChars(letters[y], x, 1,
							x * boxWidth + (boxWidth - metrics.charWidth(letters[y][x])) / 2,
							y * boxHeight + (boxHeight - metrics.getHeight()) / 2 + metrics.getAscent());
				}
			}
		}
		if (extraLetter != 0) {
			Vec2i loc = getPerimeterVec(endPosition).add(getPerimeterSide(endPosition).getVec());
			graphics2D.drawChars(new char[]{extraLetter}, 0, 1,
					loc.x * boxWidth + (boxWidth - metrics.charWidth(extraLetter)) / 2,
					loc.y * boxHeight + (boxHeight - metrics.getHeight()) / 2 + metrics.getAscent());
		}
	}

	private class MazeGenerator {
		boolean[][] grown = new boolean[height][width];

		void generate() {
			growFromPoint(new Vec2i(random.nextInt(width), random.nextInt(height)));

			List<Vec2i> blanks = Lists.newArrayList();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (!grown[y][x]) {
						blanks.add(new Vec2i(x, y));
					}
				}
			}

			while (!blanks.isEmpty()) {
				Vec2i current = blanks.get(random.nextInt(blanks.size()));
				List<BoxSide> adjacentGrowths = getAdjacentGrowths(current);
				if (!adjacentGrowths.isEmpty()) {
					blanks.remove(current);
					setBoxSide(current, adjacentGrowths.get(random.nextInt(adjacentGrowths.size())),
							false);
					blanks.removeAll(growFromPoint(current));
				}
			}
		}

		List<BoxSide> getAdjacentGrowths(Vec2i loc) {
			List<BoxSide> adjacentGrowths = Lists.newArrayList();
			if (loc.y > 0 && grown[loc.y - 1][loc.x]) {
				adjacentGrowths.add(BoxSide.TOP);
			}
			if (loc.x < width - 1 && grown[loc.y][loc.x + 1]) {
				adjacentGrowths.add(BoxSide.RIGHT);
			}
			if (loc.y < height - 1 && grown[loc.y + 1][loc.x]) {
				adjacentGrowths.add(BoxSide.BOTTOM);
			}
			if (loc.x > 0 && grown[loc.y][loc.x - 1]) {
				adjacentGrowths.add(BoxSide.LEFT);
			}
			return adjacentGrowths;
		}

		List<Vec2i> growFromPoint(Vec2i growthOrigin) {
			List<Vec2i> newGrowth = Lists.newArrayList();
			newGrowth.add(growthOrigin);
			grown[growthOrigin.y][growthOrigin.x] = true;

			while (!newGrowth.isEmpty()) {
				Vec2i current = newGrowth.get(random.nextInt(newGrowth.size()));
				newGrowth.remove(current);

				List<BoxSide> availableDirections = Lists.newArrayList();
				if (current.y > 0 && !grown[current.y - 1][current.x]) {
					availableDirections.add(BoxSide.TOP);
				}
				if (current.x < width - 1 && !grown[current.y][current.x + 1]) {
					availableDirections.add(BoxSide.RIGHT);
				}
				if (current.y < height - 1 && !grown[current.y + 1][current.x]) {
					availableDirections.add(BoxSide.BOTTOM);
				}
				if (current.x > 0 && !grown[current.y][current.x - 1]) {
					availableDirections.add(BoxSide.LEFT);
				}

				if (!availableDirections.isEmpty()) {
					do {
						BoxSide direction = availableDirections.get(random.nextInt(availableDirections.size()));
						setBoxSide(current, direction, false);

						Vec2i childGrowth = current.add(direction.getVec());
						availableDirections.remove(direction);

						newGrowth.add(childGrowth);
						grown[childGrowth.y][childGrowth.x] = true;
					} while (!availableDirections.isEmpty() && random.nextFloat() < MAZE_CHANCE_TO_SPLIT);
				}
			}

			return newGrowth;
		}
	}
}
