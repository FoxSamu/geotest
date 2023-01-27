/*
 * Copyright (C) 2023 Samū
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.shadew.geotest;

import org.joml.Vector2f;
import org.joml.Vector2fc;

class Space {
    private static final float SCALE_MAX = 100f;
    private static final float SCALE_MIN = 0.02f;

    private float scale = 1;
    private float x = 0, y = 0;
    private float screenW, screenH;
    private float unitLength = 50;

    public void screenSize(float w, float h) {
        screenW = w;
        screenH = h;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public void pos(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void pan(float x, float y) {
        this.x += x;
        this.y += y;
    }

    public void zoom(float delta) {
        this.scale *= Math.pow(2, delta);

        if (scale > SCALE_MAX)
            scale = SCALE_MAX;

        if (scale < SCALE_MIN)
            scale = SCALE_MIN;
    }

    public void scale(float scale) {
        if (scale > SCALE_MAX)
            scale = SCALE_MAX;

        if (scale < SCALE_MIN)
            scale = SCALE_MIN;

        this.scale = scale;
    }

    public float scale() {
        return scale;
    }

    public float screenW() {
        return screenW;
    }

    public float screenH() {
        return screenH;
    }

    public float worldW() {
        return lenToWorld(screenW);
    }

    public float worldH() {
        return lenToWorld(screenH);
    }

    public Vector2f worldTopLeft(Vector2f out) {
        return posToWorld(0, 0, out);
    }

    public Vector2f worldTopRight(Vector2f out) {
        return posToWorld(screenW, 0, out);
    }

    public Vector2f worldBottomLeft(Vector2f out) {
        return posToWorld(0, screenH, out);
    }

    public Vector2f worldBottomRight(Vector2f out) {
        return posToWorld(screenW, screenH, out);
    }

    public void unitLength(float unitLength) {
        this.unitLength = unitLength;
    }

    public float unitLength() {
        return unitLength;
    }

    public float lenToScreen(float len) {
        return len * scale * unitLength;
    }

    public float lenToWorld(float len) {
        return len / unitLength / scale;
    }

    public Vector2f vecToScreen(float x, float y, Vector2f out) {
        return out.set(
            vecXToScreen(x),
            vecYToScreen(y)
        );
    }

    public Vector2f vecToScreen(Vector2fc vec, Vector2f out) {
        return vecToScreen(vec.x(), vec.y(), out);
    }

    public Vector2f vecToWorld(float x, float y, Vector2f out) {
        return out.set(
            vecXToWorld(x),
            vecYToWorld(y)
        );
    }

    public Vector2f vecToWorld(Vector2fc vec, Vector2f out) {
        return vecToWorld(vec.x(), vec.y(), out);
    }

    public float vecXToScreen(float x) {
        return x * scale * unitLength;
    }

    public float vecYToScreen(float y) {
        return y * -scale * unitLength;
    }

    public float vecXToWorld(float x) {
        return x / unitLength / scale;
    }

    public float vecYToWorld(float y) {
        return y / unitLength / -scale;
    }

    public Vector2f posToScreen(float x, float y, Vector2f out) {
        return out.set(
            posXToScreen(x),
            posYToScreen(y)
        );
    }

    public Vector2f posToScreen(Vector2fc vec, Vector2f out) {
        return posToScreen(vec.x(), vec.y(), out);
    }

    public Vector2f posToWorld(float x, float y, Vector2f out) {
        return out.set(
            posXToWorld(x),
            posYToWorld(y)
        );
    }

    public Vector2f posToWorld(Vector2fc vec, Vector2f out) {
        return posToWorld(vec.x(), vec.y(), out);
    }

    public float posXToScreen(float x) {
        return (x + this.x) * scale * unitLength + screenW / 2f;
    }

    public float posYToScreen(float y) {
        return (y + this.y) * -scale * unitLength + screenH / 2f;
    }

    public float posXToWorld(float x) {
        return (x - screenW / 2f) / unitLength / scale - this.x;
    }

    public float posYToWorld(float y) {
        return (y - screenH / 2f) / unitLength / -scale - this.y;
    }
}
