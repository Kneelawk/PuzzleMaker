package com.kneelawk.puzzlemaker.wordmaze;

import java.util.Objects;

public class Vec2i {
	public final int x;
	public final int y;

	Vec2i(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Vec2i vec2i = (Vec2i) o;
		return x == vec2i.x &&
				y == vec2i.y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	public Vec2i add(Vec2i other) {
		return add(this, other);
	}

	public static Vec2i add(Vec2i a, Vec2i b) {
		return new Vec2i(a.x + b.x, a.y + b.y);
	}
}
