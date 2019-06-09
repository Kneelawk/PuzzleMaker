package com.kneelawk.puzzlemaker.wordmaze;

public enum BoxSide {
	TOP(new Vec2i(0, -1)),
	RIGHT(new Vec2i(1, 0)),
	BOTTOM(new Vec2i(0, 1)),
	LEFT(new Vec2i(-1, 0));

	private final Vec2i vec;

	BoxSide(Vec2i vec) {
		this.vec = vec;
	}

	public Vec2i getVec() {
		return vec;
	}
}
