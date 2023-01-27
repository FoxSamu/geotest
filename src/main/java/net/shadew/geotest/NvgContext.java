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

import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NVGColor;

import java.lang.Math;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryStack.*;

class NvgContext implements GeometryContext {
    private static final float SQRT_2 = 1.41421356237f;
    private static final float SQRT_3 = 1.73205080757f;
    private static final float HALF_SQRT_2 = 0.70710678118f;
    private static final float HALF_SQRT_3 = 0.86602540378f;
    private static final float PI = 3.14159265359f;

    private final long window;
    private final long nvg;
    private final Space space;
    private final BooleanSupplier allowInput;

    private final Vector2f vec = new Vector2f();
    private final LineIntersection isc = new LineIntersection();
    private final ScreenRaycast rc = new ScreenRaycast();
    private final TextBox tbox = new TextBox();

    private AlignX alignX = AlignX.LEFT;
    private AlignY alignY = AlignY.TOP;

    private float screenL, screenT, screenR, screenB;
    private float mouseX, mouseY;
    private float mouseWX, mouseWY;

    private int bg = 0x00000000;

    private long timeStart = System.currentTimeMillis();

    NvgContext(long window, long nvg, Space space, BooleanSupplier allowInput) {
        this.window = window;
        this.nvg = nvg;
        this.space = space;
        this.allowInput = allowInput;
    }

    LineIntersection isc(float a1x, float a1y, float a2x, float a2y, float b1x, float b1y, float b2x, float b2y) {
        isc.a1.set(a1x, a1y);
        isc.a2.set(a2x, a2y);
        isc.b1.set(b1x, b1y);
        isc.b2.set(b2x, b2y);
        isc.compute();

        return isc;
    }

    LineIntersection isc(Vector2fc a1, Vector2fc a2, Vector2fc b1, Vector2fc b2) {
        isc.a1.set(a1);
        isc.a2.set(a2);
        isc.b1.set(b1);
        isc.b2.set(b2);
        isc.compute();

        return isc;
    }

    int bg() {
        return bg;
    }

    void update(float mx, float my) {
        space.worldTopLeft(vec);
        float sl = vec.x;
        float st = vec.y;

        space.worldBottomRight(vec);
        float sr = vec.x;
        float sb = vec.y;

        screenL = Math.min(sl, sr);
        screenR = Math.max(sl, sr);
        screenB = Math.min(sb, st);
        screenT = Math.max(sb, st);

        space.posToWorld(mx, my, vec);
        mouseX = vec.x;
        mouseY = vec.y;

        mouseWX = mx;
        mouseWY = my;
    }

    int getFont(String name) {
        int i = nvgFindFont(nvg, name);
        if (i < 0) {
            return Font.load(nvg, name);
        }
        return i;
    }

    @Override
    public long nvg() {
        return nvg;
    }

    @Override
    public void bg(int argb) {
        bg = argb;
    }

    @Override
    public void bg(float r, float g, float b) {
        bg(r, g, b, 1);
    }

    @Override
    public void bg(float r, float g, float b, float a) {
        int ir = (int) (r * 255);
        int ig = (int) (g * 255);
        int ib = (int) (b * 255);
        int ia = (int) (a * 255);
        bg(ia << 24 | ir << 16 | ig << 8 | ib);
    }

    @Override
    public void resetTime() {
        timeStart = System.currentTimeMillis();
    }

    @Override
    public long millis() {
        return System.currentTimeMillis() - timeStart;
    }

    @Override
    public float seconds() {
        return millis() / 1000f;
    }

    @Override
    public void drawText(String text, float x, float y, float ox, float oy, int col, float size) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        drawHudText(text, x, y, col, size);
    }

    @Override
    public void drawTextBg(String text, float x, float y, float ox, float oy, int col, float size, float margin, float cr) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        drawHudTextBg(text, x, y, col, size, margin, cr);
    }

    @Override
    public void drawTextBox(String text, float x, float y, float ox, float oy, int col, float wrapWdt, float size) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        drawHudTextBox(text, x, y, col, wrapWdt, size);
    }

    @Override
    public void drawTextBoxBg(String text, float x, float y, float ox, float oy, int col, float wrapWdt, float size, float margin, float cr) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        drawHudTextBoxBg(text, x, y, col, wrapWdt, size, margin, cr);
    }

    @Override
    public void locateText(String text, float x, float y, float ox, float oy, float size, TextBox out) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        locateHudText(text, x, y, size, out);
    }

    @Override
    public void locateTextBox(String text, float x, float y, float ox, float oy, float wrapWdt, float size, TextBox out) {
        x = posXToScreen(x) + ox;
        y = posYToScreen(y) + oy;
        locateHudTextBox(text, x, y, wrapWdt, size, out);
    }

    @Override
    public void drawHudText(String text, float x, float y, int col, float size) {
        try (var stack = stackPush()) {
            NVGColor c = NVGColor.malloc(stack);
            argb(col, c);
            nvgFillColor(nvg, c);
        }
        setAlign();
        nvgFontSize(nvg, size);
        nvgText(nvg, x, y, text);
    }

    @Override
    public void drawHudTextBg(String text, float x, float y, int col, float size, float margin, float cr) {
        locateHudText(text, x, y, size, tbox);
        drawTextBg(tbox, col, margin, cr);
    }

    @Override
    public void drawHudTextBox(String text, float x, float y, int col, float wrapWdt, float size) {
        locateHudTextBox(text, x, y, wrapWdt, size, tbox);
        float hgt = tbox.hi.y - tbox.lo.y;

        x -= switch (alignX) {
            case LEFT -> 0;
            case CENTER -> wrapWdt / 2;
            case RIGHT -> wrapWdt;
        };
        y -= switch (alignY) {
            case TOP -> 0;
            case MIDDLE -> hgt / 2;
            case BOTTOM -> hgt;
        };

        try (var stack = stackPush()) {
            NVGColor c = NVGColor.malloc(stack);
            argb(col, c);
            nvgFillColor(nvg, c);
        }
        setAlign(alignX, AlignY.TOP);
        nvgFontSize(nvg, size);
        nvgTextBox(nvg, x, y, wrapWdt, text);
    }

    @Override
    public void drawHudTextBoxBg(String text, float x, float y, int col, float wrapWdt, float size, float margin, float cr) {
        locateHudTextBox(text, x, y, wrapWdt, size, tbox);
        drawTextBg(tbox, col, margin, cr);
    }

    @Override
    public void locateHudText(String text, float x, float y, float size, TextBox out) {
        nvgFontSize(nvg, size);
        setAlign();

        try (var stack = stackPush()) {
            FloatBuffer bounds = stack.mallocFloat(4);
            nvgTextBounds(nvg, x, y, text, bounds);

            out.lo.set(0, bounds);
            out.hi.set(2, bounds);
        }
    }

    @Override
    public void locateHudTextBox(String text, float x, float y, float wrapWdt, float size, TextBox out) {
        nvgFontSize(nvg, size);
        nvgTextAlign(nvg, NVG_ALIGN_TOP | NVG_ALIGN_LEFT);

        try (var stack = stackPush()) {
            FloatBuffer bounds = stack.mallocFloat(4);
            nvgTextBoxBounds(nvg, x, y, wrapWdt, text, bounds);

            out.lo.set(0, bounds);
            out.hi.set(2, bounds);
        }

        float wdt = out.hi.x - out.lo.x, hgt = out.hi.y - out.lo.y;

        float bx = switch (alignX) {
            case LEFT -> x;
            case CENTER -> x - wdt / 2;
            case RIGHT -> x - wdt;
        };
        float by = switch (alignY) {
            case TOP -> y;
            case MIDDLE -> y - hgt / 2;
            case BOTTOM -> y - hgt;
        };

        out.lo.set(bx, by);
        out.hi.set(bx + wdt, by + hgt);
    }

    @Override
    public void drawTextBg(TextBox box, int col, float margin, float cr) {
        nvgBeginPath(nvg);
        nvgRoundedRect(nvg, box.lo.x - margin, box.lo.y - margin, box.hi.x - box.lo.x + 2 * margin, box.hi.y - box.lo.y + 2 * margin, cr);
        fill(col);
    }

    @Override
    public void textAlign(AlignX alignX, AlignY alignY) {
        this.alignX = alignX;
        this.alignY = alignY;
    }

    @Override
    public AlignX alignX() {
        return alignX;
    }

    @Override
    public AlignY alignY() {
        return alignY;
    }

    private void setAlign() {
        setAlign(alignX, alignY);
    }

    private void setAlign(AlignX alignX, AlignY alignY) {
        int align = 0;

        align |= switch (alignX) {
            case LEFT -> NVG_ALIGN_LEFT;
            case CENTER -> NVG_ALIGN_CENTER;
            case RIGHT -> NVG_ALIGN_RIGHT;
        };
        align |= switch (alignY) {
            case TOP -> NVG_ALIGN_TOP;
            case MIDDLE -> NVG_ALIGN_MIDDLE;
            case BOTTOM -> NVG_ALIGN_BOTTOM;
        };

        nvgTextAlign(nvg, align);
    }

    @Override
    public void font(String font) {
        nvgFontFaceId(nvg, getFont(font));
    }

    @Override
    public void drawPointCircle(float x, float y, int col, float s) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgCircle(nvg, x, y, s / 2);
        fill(col);
    }

    @Override
    public void drawPointCircle(Vector2fc pt, int col, float s) {
        drawPointCircle(pt.x(), pt.y(), col, s);
    }

    @Override
    public void drawPointSquare(float x, float y, int col, float s) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        s *= HALF_SQRT_2;
        nvgBeginPath(nvg);
        nvgRect(nvg, x - s / 2, y - s / 2, s, s);
        fill(col);
    }

    @Override
    public void drawPointSquare(Vector2fc pt, int col, float s) {
        drawPointSquare(pt.x(), pt.y(), col, s);
    }

    @Override
    public void drawPointDiamond(float x, float y, int col, float s) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;
        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x, y + r);
        nvgLineTo(nvg, x - r, y);
        nvgLineTo(nvg, x, y - r);
        nvgLineTo(nvg, x + r, y);
        nvgClosePath(nvg);
        fill(col);
    }

    @Override
    public void drawPointDiamond(Vector2fc pt, int col, float s) {
        drawPointDiamond(pt.x(), pt.y(), col, s);
    }

    @Override
    public void drawPointHexagon(float x, float y, int col, float s) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;
        float hs3r = r * HALF_SQRT_3;
        float hr = r * 0.5f;

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x + r, y);
        nvgLineTo(nvg, x + hr, y - hs3r);
        nvgLineTo(nvg, x - hr, y - hs3r);
        nvgLineTo(nvg, x - r, y);
        nvgLineTo(nvg, x - hr, y + hs3r);
        nvgLineTo(nvg, x + hr, y + hs3r);
        nvgClosePath(nvg);
        fill(col);
    }

    @Override
    public void drawPointHexagon(Vector2fc pt, int col, float s) {
        drawPointHexagon(pt.x(), pt.y(), col, s);
    }

    @Override
    public void drawPointCircleOut(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgCircle(nvg, x, y, s / 2);
        stroke(wdt, col);
    }

    @Override
    public void drawPointCircleOut(Vector2fc pt, int col, float s, float wdt) {
        drawPointCircleOut(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointSquareOut(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        s *= HALF_SQRT_2;
        nvgBeginPath(nvg);
        nvgRect(nvg, x - s / 2, y - s / 2, s, s);
        stroke(wdt, col);
    }

    @Override
    public void drawPointSquareOut(Vector2fc pt, int col, float s, float wdt) {
        drawPointSquareOut(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointDiamondOut(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;
        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x, y + r);
        nvgLineTo(nvg, x - r, y);
        nvgLineTo(nvg, x, y - r);
        nvgLineTo(nvg, x + r, y);
        nvgClosePath(nvg);
        stroke(wdt, col);
    }

    @Override
    public void drawPointDiamondOut(Vector2fc pt, int col, float s, float wdt) {
        drawPointDiamondOut(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointHexagonOut(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;
        float hs3r = r * HALF_SQRT_3;
        float hr = r * 0.5f;

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x + r, y);
        nvgLineTo(nvg, x + hr, y - hs3r);
        nvgLineTo(nvg, x - hr, y - hs3r);
        nvgLineTo(nvg, x - r, y);
        nvgLineTo(nvg, x - hr, y + hs3r);
        nvgLineTo(nvg, x + hr, y + hs3r);
        nvgClosePath(nvg);
        stroke(wdt, col);
    }

    @Override
    public void drawPointHexagonOut(Vector2fc pt, int col, float s, float wdt) {
        drawPointHexagonOut(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointPlus(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x - r, y);
        nvgLineTo(nvg, x + r, y);
        nvgMoveTo(nvg, x, y - r);
        nvgLineTo(nvg, x, y + r);
        stroke(wdt, col);
    }

    @Override
    public void drawPointPlus(Vector2fc pt, int col, float s, float wdt) {
        drawPointPlus(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointX(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2 * HALF_SQRT_2;

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x - r, y - r);
        nvgLineTo(nvg, x + r, y + r);
        nvgMoveTo(nvg, x + r, y - r);
        nvgLineTo(nvg, x - r, y + r);
        stroke(wdt, col);
    }

    @Override
    public void drawPointX(Vector2fc pt, int col, float s, float wdt) {
        drawPointX(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawPointStar(float x, float y, int col, float s, float wdt) {
        x = posXToScreen(x);
        y = posYToScreen(y);

        float r = s / 2;
        float hs3r = r * HALF_SQRT_3;
        float hr = r * 0.5f;

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x, y + r);
        nvgLineTo(nvg, x, y - r);
        nvgMoveTo(nvg, x - hs3r, y + hr);
        nvgLineTo(nvg, x + hs3r, y - hr);
        nvgMoveTo(nvg, x + hs3r, y + hr);
        nvgLineTo(nvg, x - hs3r, y - hr);
        stroke(wdt, col);
    }

    @Override
    public void drawPointStar(Vector2fc pt, int col, float s, float wdt) {
        drawPointStar(pt.x(), pt.y(), col, s, wdt);
    }

    @Override
    public void drawSegment(float x1, float y1, float x2, float y2, int col, float wdt) {
        drawLine(x1, y1, x2, y2, true, true, col, wdt);
    }

    @Override
    public void drawSegment(Vector2fc p1, Vector2fc p2, int col, float wdt) {
        drawSegment(p1.x(), p1.y(), p2.x(), p2.y(), col, wdt);
    }

    @Override
    public void drawRay(float x1, float y1, float x2, float y2, int col, float wdt) {
        drawLine(x1, y1, x2, y2, true, false, col, wdt);
    }

    @Override
    public void drawRay(Vector2fc p1, Vector2fc p2, int col, float wdt) {
        drawRay(p1.x(), p1.y(), p2.x(), p2.y(), col, wdt);
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, int col, float wdt) {
        drawLine(x1, y1, x2, y2, false, false, col, wdt);
    }

    @Override
    public void drawLine(Vector2fc p1, Vector2fc p2, int col, float wdt) {
        drawLine(p1.x(), p1.y(), p2.x(), p2.y(), col, wdt);
    }

    @Override
    public void drawNormal(float x, float y, float nx, float ny, float l, int col, float wdt) {
        nx = vecXToScreen(nx);
        ny = vecYToScreen(ny);

        float nm = (float) Math.sqrt(nx * nx + ny * ny);
        nx /= nm;
        ny /= nm;
        if (nm == 0) {
            nx = 1;
            ny = 0;
        }

        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x, y);
        nvgLineTo(nvg, x + l * nx, y + l * ny);
        stroke(wdt, col);
    }

    @Override
    public void drawNormal(Vector2fc src, Vector2fc norm, float l, int col, float wdt) {
        drawNormal(src.x(), src.y(), norm.x(), norm.y(), l, col, wdt);
    }

    @Override
    public void drawArrowEnd(float x, float y, float nx, float ny, float s, int col, float wdt) {
        nx = vecXToScreen(nx);
        ny = vecYToScreen(ny);

        float nm = (float) Math.sqrt(nx * nx + ny * ny);
        nx /= nm;
        ny /= nm;
        if (nm == 0) {
            nx = 1;
            ny = 0;
        }

        float tx = ny;
        float ty = -nx;

        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x - nx * s - tx * s * HALF_SQRT_2, y - ny * s - ty * s * HALF_SQRT_2);
        nvgLineTo(nvg, x, y);
        nvgLineTo(nvg, x - nx * s + tx * s * HALF_SQRT_2, y - ny * s + ty * s * HALF_SQRT_2);
        stroke(wdt, col);
    }

    @Override
    public void drawArrowEnd(Vector2fc src, Vector2fc dir, float s, int col, float wdt) {
        drawArrowEnd(src.x(), src.y(), dir.x(), dir.y(), s, col, wdt);
    }

    @Override
    public void drawLineDash(float x, float y, float nx, float ny, float s, int col, float wdt) {
        nx = vecXToScreen(nx);
        ny = vecYToScreen(ny);

        float nm = (float) Math.sqrt(nx * nx + ny * ny);
        nx /= nm;
        ny /= nm;
        if (nm == 0) {
            nx = 1;
            ny = 0;
        }

        float tx = ny;
        float ty = -nx;

        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x - tx * s, y - ty * s);
        nvgLineTo(nvg, x + tx * s, y + ty * s);
        stroke(wdt, col);
    }

    @Override
    public void drawLineDash(Vector2fc src, Vector2fc dir, float s, int col, float wdt) {
        drawLineDash(src.x(), src.y(), dir.x(), dir.y(), s, col, wdt);
    }

    @Override
    public void drawLineCross(float x, float y, float nx, float ny, float s, int col, float wdt) {
        nx = vecXToScreen(nx);
        ny = vecYToScreen(ny);

        float nm = (float) Math.sqrt(nx * nx + ny * ny);
        nx /= nm;
        ny /= nm;
        if (nm == 0) {
            nx = 1;
            ny = 0;
        }

        float tx = ny;
        float ty = -nx;

        x = posXToScreen(x);
        y = posYToScreen(y);

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, x - tx * s - nx * s * 0.5f, y - ty * s - ny * s * 0.5f);
        nvgLineTo(nvg, x + tx * s + nx * s * 0.5f, y + ty * s + ny * s * 0.5f);
        nvgMoveTo(nvg, x - tx * s + nx * s * 0.5f, y - ty * s + ny * s * 0.5f);
        nvgLineTo(nvg, x + tx * s - nx * s * 0.5f, y + ty * s - ny * s * 0.5f);
        stroke(wdt, col);

    }

    @Override
    public void drawLineCross(Vector2fc src, Vector2fc dir, float s, int col, float wdt) {
        drawLineCross(src.x(), src.y(), dir.x(), dir.y(), s, col, wdt);
    }

    private void drawLine(float x1, float y1, float x2, float y2, boolean loEnd, boolean hiEnd, int col, float wdt) {
        if (!raycastScreen(x1, y1, x2, y2, lenToWorld(wdt), rc))
            return;

        ScreenRaycast rc = this.rc;

        if (rc.u2 > 1 && hiEnd)
            rc.u2 = 1;

        if (rc.u1 < 0 && loEnd)
            rc.u1 = 0;

        if (rc.u1 > rc.u2)
            return;

        float lx1 = posXToScreen(x1 + rc.u1 * (x2 - x1));
        float lx2 = posXToScreen(x1 + rc.u2 * (x2 - x1));
        float ly1 = posYToScreen(y1 + rc.u1 * (y2 - y1));
        float ly2 = posYToScreen(y1 + rc.u2 * (y2 - y1));

        nvgBeginPath(nvg);
        nvgMoveTo(nvg, lx1, ly1);
        nvgLineTo(nvg, lx2, ly2);
        stroke(wdt, col);
    }

    @Override
    public void begin() {
        nvgBeginPath(nvg);
    }

    @Override
    public void close() {
        nvgClosePath(nvg);
    }

    @Override
    public void moveTo(float x, float y) {
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        nvgMoveTo(nvg, x, y);
    }

    @Override
    public void moveTo(Vector2fc p) {
        moveTo(p.x(), p.y());
    }

    @Override
    public void lineTo(float x, float y) {
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        nvgLineTo(nvg, x, y);
    }

    @Override
    public void lineTo(Vector2fc p) {
        lineTo(p.x(), p.y());
    }

    @Override
    public void quadTo(float cx, float cy, float x, float y) {
        vec.set(cx, cy);
        posToScreen(vec, vec);
        cx = vec.x;
        cy = vec.y;
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        nvgQuadTo(nvg, cx, cy, x, y);
    }

    @Override
    public void quadTo(Vector2fc c, Vector2fc p) {
        quadTo(c.x(), c.y(), p.x(), p.y());
    }

    @Override
    public void cubicTo(float c1x, float c1y, float c2x, float c2y, float x, float y) {
        vec.set(c1x, c1y);
        posToScreen(vec, vec);
        c1x = vec.x;
        c1y = vec.y;
        vec.set(c2x, c2y);
        posToScreen(vec, vec);
        c2x = vec.x;
        c2y = vec.y;
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        nvgBezierTo(nvg, c1x, c1y, c2x, c2y, x, y);
    }

    @Override
    public void cubicTo(Vector2fc c1, Vector2fc c2, Vector2fc p) {
        cubicTo(c1.x(), c1.y(), c2.x(), c2.y(), p.x(), p.y());
    }

    @Override
    public void arcTo(float x, float y, float x2, float y2, float r) {
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        vec.set(x2, y2);
        posToScreen(vec, vec);
        x2 = vec.x;
        y2 = vec.y;
        r = lenToScreen(r);
        nvgArcTo(nvg, x, y, x2, y2, r);
    }

    @Override
    public void arcTo(Vector2fc p, Vector2fc p2, float r) {
        arcTo(p.x(), p.y(), p2.x(), p2.y(), r);
    }

    @Override
    public void moveToHud(float x, float y) {
        nvgMoveTo(nvg, x, y);
    }

    @Override
    public void moveToHud(Vector2fc p) {
        moveToHud(p.x(), p.y());
    }

    @Override
    public void lineToHud(float x, float y) {
        nvgLineTo(nvg, x, y);
    }

    @Override
    public void lineToHud(Vector2fc p) {
        lineToHud(p.x(), p.y());
    }

    @Override
    public void quadToHud(float cx, float cy, float x, float y) {
        nvgQuadTo(nvg, cx, cy, x, y);
    }

    @Override
    public void quadToHud(Vector2fc c, Vector2fc p) {
        quadToHud(c.x(), c.y(), p.x(), p.y());
    }

    @Override
    public void cubicToHud(float c1x, float c1y, float c2x, float c2y, float x, float y) {
        nvgBezierTo(nvg, c1x, c1y, c2x, c2y, x, y);
    }

    @Override
    public void cubicToHud(Vector2fc c1, Vector2fc c2, Vector2fc p) {
        cubicToHud(c1.x(), c1.y(), c2.x(), c2.y(), p.x(), p.y());
    }

    @Override
    public void arcToHud(float x, float y, float x2, float y2, float r) {
        nvgArcTo(nvg, x, y, x2, y2, r);
    }

    @Override
    public void arcToHud(Vector2fc p, Vector2fc p2, float r) {
        arcToHud(p.x(), p.y(), p2.x(), p2.y(), r);
    }

    @Override
    public void circle(float cx, float cy, float r) {
        vec.set(cx, cy);
        posToScreen(vec, vec);
        cx = vec.x;
        cy = vec.y;
        r = lenToScreen(r);
        nvgCircle(nvg, cx, cy, r);
    }

    @Override
    public void circle(Vector2fc c, float r) {
        circle(c.x(), c.y(), r);
    }

    @Override
    public void circleHud(float cx, float cy, float r) {
        nvgCircle(nvg, cx, cy, r);
    }

    @Override
    public void circleHud(Vector2fc c, float r) {
        circleHud(c.x(), c.y(), r);
    }

    @Override
    public void ellipse(float cx, float cy, float rx, float ry) {
        vec.set(cx, cy);
        posToScreen(vec, vec);
        cx = vec.x;
        cy = vec.y;
        rx = lenToScreen(rx);
        ry = lenToScreen(ry);
        nvgEllipse(nvg, cx, cy, rx, ry);
    }

    @Override
    public void ellipse(Vector2fc c, float rx, float ry) {
        ellipse(c.x(), c.y(), rx, ry);
    }

    @Override
    public void ellipse(Vector2fc c, Vector2fc r) {
        ellipse(c, r.x(), r.y());
    }

    @Override
    public void ellipseHud(float cx, float cy, float rx, float ry) {
        nvgEllipse(nvg, cx, cy, rx, ry);
    }

    @Override
    public void ellipseHud(Vector2fc c, float rx, float ry) {
        ellipseHud(c.x(), c.y(), rx, ry);
    }

    @Override
    public void ellipseHud(Vector2fc c, Vector2fc r) {
        ellipseHud(c, r.x(), r.y());
    }

    @Override
    public void rect(float x, float y, float w, float h) {
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        w = lenToScreen(w);
        h = lenToScreen(h);
        nvgRect(nvg, x, y - h, w, h);
    }

    @Override
    public void rect(Vector2fc p, float w, float h) {
        rect(p.x(), p.y(), w, h);
    }

    @Override
    public void rect(Vector2fc p, Vector2fc s) {
        rect(p, s.x(), s.y());
    }

    @Override
    public void rectHud(float x, float y, float w, float h) {
        nvgRect(nvg, x, y, w, h);
    }

    @Override
    public void rectHud(Vector2fc p, float w, float h) {
        rectHud(p.x(), p.y(), w, h);
    }

    @Override
    public void rectHud(Vector2fc p, Vector2fc s) {
        rectHud(p, s.x(), s.y());
    }

    @Override
    public void roundRect(float x, float y, float w, float h, float cr) {
        vec.set(x, y);
        posToScreen(vec, vec);
        x = vec.x;
        y = vec.y;
        w = lenToScreen(w);
        h = lenToScreen(h);
        cr = lenToScreen(cr);
        nvgRoundedRect(nvg, x, y - h, w, h, cr);
    }

    @Override
    public void roundRect(Vector2fc p, float w, float h, float cr) {
        roundRect(p.x(), p.y(), w, h, cr);
    }

    @Override
    public void roundRect(Vector2fc p, Vector2fc s, float cr) {
        roundRect(p, s.x(), s.y(), cr);
    }

    @Override
    public void roundRectHud(float x, float y, float w, float h, float cr) {
        nvgRoundedRect(nvg, x, y, w, h, cr);
    }

    @Override
    public void roundRectHud(Vector2fc p, float w, float h, float cr) {
        roundRectHud(p.x(), p.y(), w, h, cr);
    }

    @Override
    public void roundRectHud(Vector2fc p, Vector2fc s, float cr) {
        roundRectHud(p, s.x(), s.y(), cr);
    }

    @Override
    public void arc(float cx, float cy, float r, float from, float to) {
        vec.set(cx, cy);
        posToScreen(vec, vec);
        cx = vec.x;
        cy = vec.y;
        r = lenToScreen(r);
        nvgArc(nvg, cx, cy, r, from, to, NVG_CCW);
    }

    @Override
    public void arc(Vector2fc c, float r, float from, float to) {
        arc(c.x(), c.y(), r, from, to);
    }

    @Override
    public void arcHud(float cx, float cy, float r, float from, float to) {
        nvgArc(nvg, cx, cy, r, from, to, NVG_CCW);
    }

    @Override
    public void arcHud(Vector2fc c, float r, float from, float to) {
        arcHud(c.x(), c.y(), r, from, to);
    }

    @Override
    public void fill(int argb) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            argb(argb, col);
            nvgFillColor(nvg, col);
        }

        nvgFill(nvg);
    }

    @Override
    public void fill(float r, float g, float b) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBf(r, g, b, col);
            nvgFillColor(nvg, col);
        }

        nvgFill(nvg);
    }

    @Override
    public void fill(float r, float g, float b, float a) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBAf(r, g, b, a, col);
            nvgFillColor(nvg, col);
        }

        nvgFill(nvg);
    }

    @Override
    public void fill(Vector3fc rgb) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBf(rgb.x(), rgb.y(), rgb.z(), col);
            nvgFillColor(nvg, col);
        }

        nvgFill(nvg);
    }

    @Override
    public void fill(Vector4fc rgba) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBAf(rgba.x(), rgba.y(), rgba.z(), rgba.w(), col);
            nvgFillColor(nvg, col);
        }

        nvgFill(nvg);
    }

    @Override
    public void stroke(float wdt, int argb) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            argb(argb, col);
            nvgStrokeColor(nvg, col);
            nvgStrokeWidth(nvg, wdt);
        }

        nvgStroke(nvg);
    }

    @Override
    public void stroke(float wdt, float r, float g, float b) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBf(r, g, b, col);
            nvgStrokeColor(nvg, col);
            nvgStrokeWidth(nvg, wdt);
        }

        nvgStroke(nvg);
    }

    @Override
    public void stroke(float wdt, float r, float g, float b, float a) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBAf(r, g, b, a, col);
            nvgStrokeColor(nvg, col);
            nvgStrokeWidth(nvg, wdt);
        }

        nvgStroke(nvg);
    }

    @Override
    public void stroke(float wdt, Vector3fc rgb) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBf(rgb.x(), rgb.y(), rgb.z(), col);
            nvgStrokeColor(nvg, col);
            nvgStrokeWidth(nvg, wdt);
        }

        nvgStroke(nvg);
    }

    @Override
    public void stroke(float wdt, Vector4fc rgba) {
        try (var stack = stackPush()) {
            NVGColor col = NVGColor.malloc(stack);
            nvgRGBAf(rgba.x(), rgba.y(), rgba.z(), rgba.w(), col);
            nvgStrokeColor(nvg, col);
            nvgStrokeWidth(nvg, wdt);
        }

        nvgStroke(nvg);
    }

    @Override
    public void lineJoin(LineJoin join) {
        switch (join) {
            case BUTT -> nvgLineJoin(nvg, NVG_BUTT);
            case MITER -> nvgLineJoin(nvg, NVG_MITER);
            case ROUND -> nvgLineJoin(nvg, NVG_ROUND);
        }
    }

    @Override
    public void lineEnd(LineEnd end) {
        switch (end) {
            case FLAT -> nvgLineCap(nvg, NVG_BUTT);
            case SQUARE -> nvgLineCap(nvg, NVG_SQUARE);
            case ROUND -> nvgLineCap(nvg, NVG_ROUND);
        }
    }

    @Override
    public void miterLimit(float lim) {
        nvgMiterLimit(nvg, lim);
    }

    @Override
    public boolean onScreen(float x, float y) {
        return x >= screenL && x <= screenR && y >= screenT && y <= screenB;
    }

    @Override
    public boolean onScreen(Vector2fc vec) {
        return onScreen(vec.x(), vec.y());
    }

    @Override
    public boolean onScreen(float x, float y, float margin) {
        return x >= screenL - margin && x <= screenR + margin && y >= screenT - margin && y <= screenB + margin;
    }

    @Override
    public boolean onScreen(Vector2fc vec, float margin) {
        return onScreen(vec.x(), vec.y(), margin);
    }

    @Override
    public boolean raycastScreen(float in1x, float in1y, float in2x, float in2y, float margin, ScreenRaycast out) {
        float maxU = Float.NEGATIVE_INFINITY;
        float minU = Float.POSITIVE_INFINITY;

        isc(in1x, in1y, in2x, in2y, screenL - margin, screenT + margin, screenR + margin, screenT + margin);
        if (isc.onB()) {
            minU = Math.min(isc.au, minU);
            maxU = Math.max(isc.au, maxU);
        }

        isc(in1x, in1y, in2x, in2y, screenL - margin, screenB - margin, screenR + margin, screenB - margin);
        if (isc.onB()) {
            minU = Math.min(isc.au, minU);
            maxU = Math.max(isc.au, maxU);
        }

        isc(in1x, in1y, in2x, in2y, screenL - margin, screenT + margin, screenL - margin, screenB - margin);
        if (isc.onB()) {
            minU = Math.min(isc.au, minU);
            maxU = Math.max(isc.au, maxU);
        }

        isc(in1x, in1y, in2x, in2y, screenR + margin, screenT + margin, screenR + margin, screenB - margin);
        if (isc.onB()) {
            minU = Math.min(isc.au, minU);
            maxU = Math.max(isc.au, maxU);
        }

        if (maxU != Float.NEGATIVE_INFINITY && minU != Float.POSITIVE_INFINITY) {
            out.u1 = minU;
            out.u2 = maxU;

            out.isc1.set(in1x + minU * (in2x - in1x), in1y + minU * (in2y - in1y));
            out.isc2.set(in1x + maxU * (in2x - in1x), in1y + maxU * (in2y - in1y));

            return true;
        }

        return false;
    }

    @Override
    public boolean raycastScreen(Vector2fc in1, Vector2fc in2, float margin, ScreenRaycast out) {
        return raycastScreen(in1.x(), in1.y(), in2.x(), in2.y(), margin, out);
    }

    @Override
    public float windowW() {
        return space.screenW();
    }

    @Override
    public float windowH() {
        return space.screenH();
    }

    @Override
    public float screenL() {
        return screenL;
    }

    @Override
    public float screenT() {
        return screenT;
    }

    @Override
    public float screenR() {
        return screenR;
    }

    @Override
    public float screenB() {
        return screenB;
    }

    @Override
    public float mouseWindowX() {
        return mouseWX;
    }

    @Override
    public float mouseWindowY() {
        return mouseWY;
    }

    @Override
    public float mouseX() {
        return mouseX;
    }

    @Override
    public float mouseY() {
        return mouseY;
    }

    @Override
    public float scale() {
        return space.scale();
    }

    @Override
    public float translateX() {
        return -space.x();
    }

    @Override
    public float translateY() {
        return -space.y();
    }

    @Override
    public float unitLength() {
        return space.unitLength();
    }

    @Override
    public float lenToScreen(float len) {
        return space.lenToScreen(len);
    }

    @Override
    public float lenToWorld(float len) {
        return space.lenToWorld(len);
    }

    @Override
    public Vector2f vecToScreen(float x, float y, Vector2f out) {
        return space.vecToScreen(x, y, out);
    }

    @Override
    public Vector2f vecToScreen(Vector2fc vec, Vector2f out) {
        return space.vecToScreen(vec, out);
    }

    @Override
    public Vector2f vecToWorld(float x, float y, Vector2f out) {
        return space.vecToWorld(x, y, out);
    }

    @Override
    public Vector2f vecToWorld(Vector2fc vec, Vector2f out) {
        return space.vecToWorld(vec, out);
    }

    @Override
    public float vecXToScreen(float x) {
        return space.vecXToScreen(x);
    }

    @Override
    public float vecYToScreen(float y) {
        return space.vecYToScreen(y);
    }

    @Override
    public float vecXToWorld(float x) {
        return space.vecXToWorld(x);
    }

    @Override
    public float vecYToWorld(float y) {
        return space.vecYToWorld(y);
    }

    @Override
    public Vector2f posToScreen(float x, float y, Vector2f out) {
        return space.posToScreen(x, y, out);
    }

    @Override
    public Vector2f posToScreen(Vector2fc vec, Vector2f out) {
        return space.posToScreen(vec, out);
    }

    @Override
    public Vector2f posToWorld(float x, float y, Vector2f out) {
        return space.posToWorld(x, y, out);
    }

    @Override
    public Vector2f posToWorld(Vector2fc vec, Vector2f out) {
        return space.posToWorld(vec, out);
    }

    @Override
    public float posXToScreen(float x) {
        return space.posXToScreen(x);
    }

    @Override
    public float posYToScreen(float y) {
        return space.posYToScreen(y);
    }

    @Override
    public float posXToWorld(float x) {
        return space.posXToWorld(x);
    }

    @Override
    public float posYToWorld(float y) {
        return space.posYToWorld(y);
    }

    @Override
    public boolean keyDown(Input input) {
        if (!allowInput.getAsBoolean())
            return false;
        return input.isHeld(window);
    }

    @Override
    public boolean keyDown(int key) {
        if (!allowInput.getAsBoolean())
            return false;
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean mouseDown(int btn) {
        if (!allowInput.getAsBoolean())
            return false;
        return GLFW.glfwGetMouseButton(window, btn) == GLFW.GLFW_PRESS;
    }

    private final Set<KeyDown> keyDown = new HashSet<>();

    void keyDown(int key, int mods, boolean repeat) {
        keyDown.forEach(l -> l.keyDown(this, key, mods, repeat));
    }

    @Override
    public void onKeyDown(KeyDown l) {
        keyDown.add(l);
    }

    @Override
    public void removeKeyDown(KeyDown l) {
        keyDown.remove(l);
    }

    private final Set<KeyUp> keyUp = new HashSet<>();

    void keyUp(int key, int mods) {
        keyUp.forEach(l -> l.keyUp(this, key, mods));
    }

    @Override
    public void onKeyUp(KeyUp l) {
        keyUp.add(l);
    }

    @Override
    public void removeKeyUp(KeyUp l) {
        keyUp.remove(l);
    }

    private final Set<MouseDown> mouseDown = new HashSet<>();

    void mouseDown(int btn, int mods) {
        mouseDown.forEach(l -> l.mouseDown(this, btn, mods));
    }

    @Override
    public void onMouseDown(MouseDown l) {
        mouseDown.add(l);
    }

    @Override
    public void removeMouseDown(MouseDown l) {
        mouseDown.remove(l);
    }

    private final Set<MouseUp> mouseUp = new HashSet<>();

    void mouseUp(int btn, int mods) {
        mouseUp.forEach(l -> l.mouseUp(this, btn, mods));
    }

    @Override
    public void onMouseUp(MouseUp l) {
        mouseUp.add(l);
    }

    @Override
    public void removeMouseUp(MouseUp l) {
        mouseUp.remove(l);
    }

    private final Set<MouseMove> mouseMove = new HashSet<>();

    void mouseMove() {
        mouseMove.forEach(l -> l.mouseMove(this));
    }

    @Override
    public void onMouseMove(MouseMove l) {
        mouseMove.add(l);
    }

    @Override
    public void removeMouseMove(MouseMove l) {
        mouseMove.remove(l);
    }

    static void argb(int argb, Vector4f out) {
        out.x = (argb >>> 16 & 0xFF) / 255f;
        out.y = (argb >>> 8 & 0xFF) / 255f;
        out.z = (argb & 0xFF) / 255f;
        out.w = (argb >>> 24 & 0xFF) / 255f;
    }

    static void argb(int argb, NVGColor out) {
        out.r((argb >>> 16 & 0xFF) / 255f);
        out.g((argb >>> 8 & 0xFF) / 255f);
        out.b((argb & 0xFF) / 255f);
        out.a((argb >>> 24 & 0xFF) / 255f);
    }
}
