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
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public interface GeometryContext {
    long nvg();

    // TODO
    //   Zigzag lines
    //   Direct shapes
    //   Events for pan/zoom
    //   Register scene automatically as event handlers

    void bg(int argb);
    void bg(float r, float g, float b);
    void bg(float r, float g, float b, float a);

    void resetTime();
    long millis();
    float seconds();

    void drawText(String text, float x, float y, float ox, float oy, int col, float size);
    void drawTextBg(String text, float x, float y, float ox, float oy, int col, float size, float margin, float cr);
    void drawTextBox(String text, float x, float y, float ox, float oy, int col, float wrapWdt, float size);
    void drawTextBoxBg(String text, float x, float y, float ox, float oy, int col, float wrapWdt, float size, float margin, float cr);
    void locateText(String text, float x, float y, float ox, float oy, float size, TextBox out);
    void locateTextBox(String text, float x, float y, float ox, float oy, float wrapWdt, float size, TextBox out);

    void drawHudText(String text, float x, float y, int col, float size);
    void drawHudTextBg(String text, float x, float y, int col, float size, float margin, float cr);
    void drawHudTextBox(String text, float x, float y, int col, float wrapWdt, float size);
    void drawHudTextBoxBg(String text, float x, float y, int col, float wrapWdt, float size, float margin, float cr);
    void locateHudText(String text, float x, float y, float size, TextBox out);
    void locateHudTextBox(String text, float x, float y, float wrapWdt, float size, TextBox out);

    void drawTextBg(TextBox box, int col, float margin, float cr);


    void textAlign(AlignX alignX, AlignY alignY);
    AlignX alignX();
    AlignY alignY();
    void font(String font);

    void drawPointCircle(float x, float y, int col, float s);
    void drawPointCircle(Vector2fc pt, int col, float s);
    void drawPointSquare(float x, float y, int col, float s);
    void drawPointSquare(Vector2fc pt, int col, float s);
    void drawPointDiamond(float x, float y, int col, float s);
    void drawPointDiamond(Vector2fc pt, int col, float s);
    void drawPointHexagon(float x, float y, int col, float s);
    void drawPointHexagon(Vector2fc pt, int col, float s);
    void drawPointCircleOut(float x, float y, int col, float s, float wdt);
    void drawPointCircleOut(Vector2fc pt, int col, float s, float wdt);
    void drawPointSquareOut(float x, float y, int col, float s, float wdt);
    void drawPointSquareOut(Vector2fc pt, int col, float s, float wdt);
    void drawPointDiamondOut(float x, float y, int col, float s, float wdt);
    void drawPointDiamondOut(Vector2fc pt, int col, float s, float wdt);
    void drawPointHexagonOut(float x, float y, int col, float s, float wdt);
    void drawPointHexagonOut(Vector2fc pt, int col, float s, float wdt);
    void drawPointPlus(float x, float y, int col, float s, float wdt);
    void drawPointPlus(Vector2fc pt, int col, float s, float wdt);
    void drawPointX(float x, float y, int col, float s, float wdt);
    void drawPointX(Vector2fc pt, int col, float s, float wdt);
    void drawPointStar(float x, float y, int col, float s, float wdt);
    void drawPointStar(Vector2fc pt, int col, float s, float wdt);

    void drawSegment(float x1, float y1, float x2, float y2, int col, float wdt);
    void drawSegment(Vector2fc p1, Vector2fc p2, int col, float wdt);
    void drawRay(float x1, float y1, float x2, float y2, int col, float wdt);
    void drawRay(Vector2fc p1, Vector2fc p2, int col, float wdt);
    void drawLine(float x1, float y1, float x2, float y2, int col, float wdt);
    void drawLine(Vector2fc p1, Vector2fc p2, int col, float wdt);

    void drawNormal(float x, float y, float nx, float ny, float l, int col, float wdt);
    void drawNormal(Vector2fc src, Vector2fc norm, float l, int col, float wdt);

    void drawArrowEnd(float x, float y, float nx, float ny, float s, int col, float wdt);
    void drawArrowEnd(Vector2fc src, Vector2fc dir, float s, int col, float wdt);
    void drawLineDash(float x, float y, float nx, float ny, float s, int col, float wdt);
    void drawLineDash(Vector2fc src, Vector2fc dir, float s, int col, float wdt);
    void drawLineCross(float x, float y, float nx, float ny, float s, int col, float wdt);
    void drawLineCross(Vector2fc src, Vector2fc dir, float s, int col, float wdt);

    void begin();
    void close();
    void moveTo(float x, float y);
    void moveTo(Vector2fc p);
    void lineTo(float x, float y);
    void lineTo(Vector2fc p);
    void quadTo(float cx, float cy, float x, float y);
    void quadTo(Vector2fc c, Vector2fc p);
    void cubicTo(float c1x, float c1y, float c2x, float c2y, float x, float y);
    void cubicTo(Vector2fc c1, Vector2fc c2, Vector2fc p);
    void arcTo(float x, float y, float x2, float y2, float r);
    void arcTo(Vector2fc p, Vector2fc p2, float r);
    void moveToHud(float x, float y);
    void moveToHud(Vector2fc p);
    void lineToHud(float x, float y);
    void lineToHud(Vector2fc p);
    void quadToHud(float cx, float cy, float x, float y);
    void quadToHud(Vector2fc c, Vector2fc p);
    void cubicToHud(float c1x, float c1y, float c2x, float c2y, float x, float y);
    void cubicToHud(Vector2fc c1, Vector2fc c2, Vector2fc p);
    void arcToHud(float x, float y, float x2, float y2, float r);
    void arcToHud(Vector2fc p, Vector2fc p2, float r);

    void circle(float cx, float cy, float r);
    void circle(Vector2fc c, float r);
    void circleHud(float cx, float cy, float r);
    void circleHud(Vector2fc c, float r);

    void ellipse(float cx, float cy, float rx, float ry);
    void ellipse(Vector2fc c, float rx, float ry);
    void ellipse(Vector2fc c, Vector2fc r);
    void ellipseHud(float cx, float cy, float rx, float ry);
    void ellipseHud(Vector2fc c, float rx, float ry);
    void ellipseHud(Vector2fc c, Vector2fc r);

    void rect(float x, float y, float w, float h);
    void rect(Vector2fc p, float w, float h);
    void rect(Vector2fc p, Vector2fc s);
    void rectHud(float x, float y, float w, float h);
    void rectHud(Vector2fc p, float w, float h);
    void rectHud(Vector2fc p, Vector2fc s);

    void roundRect(float x, float y, float w, float h, float cr);
    void roundRect(Vector2fc p, float w, float h, float cr);
    void roundRect(Vector2fc p, Vector2fc s, float cr);
    void roundRectHud(float x, float y, float w, float h, float cr);
    void roundRectHud(Vector2fc p, float w, float h, float cr);
    void roundRectHud(Vector2fc p, Vector2fc s, float cr);

    void arc(float cx, float cy, float r, float from, float to);
    void arc(Vector2fc c, float r, float from, float to);
    void arcHud(float cx, float cy, float r, float from, float to);
    void arcHud(Vector2fc c, float r, float from, float to);

    void fill(int argb);
    void fill(float r, float g, float b);
    void fill(float r, float g, float b, float a);
    void fill(Vector3fc rgb);
    void fill(Vector4fc rgba);

    void stroke(float wdt, int argb);
    void stroke(float wdt, float r, float g, float b);
    void stroke(float wdt, float r, float g, float b, float a);
    void stroke(float wdt, Vector3fc rgb);
    void stroke(float wdt, Vector4fc rgba);

    void lineJoin(LineJoin join);
    void lineEnd(LineEnd end);
    void miterLimit(float lim);

    boolean onScreen(float x, float y);
    boolean onScreen(Vector2fc vec);
    boolean onScreen(float x, float y, float margin);
    boolean onScreen(Vector2fc vec, float margin);

    boolean raycastScreen(float in1x, float in1y, float in2x, float in2y, float margin, ScreenRaycast out);
    boolean raycastScreen(Vector2fc in1, Vector2fc in2, float margin, ScreenRaycast out);

    float windowW();
    float windowH();
    float screenL();
    float screenT();
    float screenR();
    float screenB();
    float mouseWindowX();
    float mouseWindowY();
    float mouseX();
    float mouseY();

    float scale();
    float translateX();
    float translateY();

    float unitLength();

    float lenToScreen(float len);
    float lenToWorld(float len);

    Vector2f vecToScreen(float x, float y, Vector2f out);
    Vector2f vecToScreen(Vector2fc vec, Vector2f out);
    Vector2f vecToWorld(float x, float y, Vector2f out);
    Vector2f vecToWorld(Vector2fc vec, Vector2f out);
    float vecXToScreen(float x);
    float vecYToScreen(float y);
    float vecXToWorld(float x);
    float vecYToWorld(float y);

    Vector2f posToScreen(float x, float y, Vector2f out);
    Vector2f posToScreen(Vector2fc vec, Vector2f out);
    Vector2f posToWorld(float x, float y, Vector2f out);
    Vector2f posToWorld(Vector2fc vec, Vector2f out);
    float posXToScreen(float x);
    float posYToScreen(float y);
    float posXToWorld(float x);
    float posYToWorld(float y);

    boolean keyDown(Input input);
    boolean keyDown(int key);
    boolean mouseDown(int btn);
    void onKeyDown(KeyDown l);
    void removeKeyDown(KeyDown l);
    void onKeyUp(KeyUp l);
    void removeKeyUp(KeyUp l);
    void onMouseDown(MouseDown l);
    void removeMouseDown(MouseDown l);
    void onMouseUp(MouseUp l);
    void removeMouseUp(MouseUp l);
    void onMouseMove(MouseMove l);
    void removeMouseMove(MouseMove l);
}
