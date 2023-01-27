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

public class EasingScene implements TestScene, KeyDown {
    Animatable.Easing easing;

    float p1x = 0.5f, p1y = 0;
    float p2x = 0.5f, p2y = 1;
    int showHandles;

    long playTime = System.currentTimeMillis();

    public EasingScene(Animatable.Easing easing) {
        this.easing = easing;
        this.showHandles = 0;
    }

    public EasingScene(float p1x, float p1y, float p2x, float p2y) {
        this.easing = Animatable.cubicBezier2D(p1x, p1y, p2x, p2y);
        this.showHandles = 1;
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
    }

    public EasingScene(float p1y, float p2y) {
        this.easing = Animatable.cubicBezier1D(p1y, p2y);
        this.showHandles = 2;
        this.p1y = p1y;
        this.p2y = p2y;
    }

    @Override
    public void init(GeometryContext ctx) {
        ctx.onKeyDown(this);
    }

    @Override
    public void draw(GeometryContext ctx) {
        ctx.bg(0xFF111111);

        ctx.begin();
        ctx.moveTo(map(0), map(0));
        ctx.lineTo(map(1), map(0));
        ctx.lineTo(map(1), map(1));
        ctx.lineTo(map(0), map(1));
        ctx.close();
        ctx.stroke(2, 0x88FFFFFF);

        if (showHandles == 1) {
            if (ctx.mouseDown(Input.BTN_LEFT)) {
                p1x = unmap(ctx.mouseX());
                p1y = unmap(ctx.mouseY());

                p1x = Math.min(Math.max(p1x, 0), 1);

                easing = Animatable.cubicBezier2D(p1x, p1y, p2x, p2y);
            }

            if (ctx.mouseDown(Input.BTN_RIGHT)) {
                p2x = unmap(ctx.mouseX());
                p2y = unmap(ctx.mouseY());

                p2x = Math.min(Math.max(p2x, 0), 1);

                easing = Animatable.cubicBezier2D(p1x, p1y, p2x, p2y);
            }

            ctx.begin();
            ctx.moveTo(map(0), map(0));
            ctx.lineTo(map(p1x), map(p1y));
            ctx.lineTo(map(p2x), map(p2y));
            ctx.lineTo(map(1), map(1));
            ctx.stroke(4, 0x8800FFFF);
        }

        if (showHandles == 2) {
            if (ctx.mouseDown(Input.BTN_LEFT)) {
                p1y = unmap(ctx.mouseY());
                easing = Animatable.cubicBezier1D(p1y, p2y);
            }

            if (ctx.mouseDown(Input.BTN_RIGHT)) {
                p2y = unmap(ctx.mouseY());
                easing = Animatable.cubicBezier1D(p1y, p2y);
            }

            ctx.begin();
            ctx.moveTo(map(0), map(p1y));
            ctx.lineTo(map(1), map(p1y));
            ctx.moveTo(map(0), map(p2y));
            ctx.lineTo(map(1), map(p2y));
            ctx.stroke(4, 0x8800FFFF);
        }

        ctx.begin();
        ctx.moveTo(map(0), map(0));
        for (int i = 0; i <= 400; i++) {
            float t = i / 400f;
            ctx.lineTo(map(t), map(easing.ease(t)));
        }
        ctx.lineTo(map(1), map(1));
        ctx.stroke(4, 0xFF00FFFF);


        long millis = System.currentTimeMillis() - playTime;
        if (millis < 3000) {
            float t = (millis - 1000) / 1000f;

            float e;
            if (t <= 0) e = 0;
            else if (t >= 1) e = 1;
            else e = easing.ease(t);

//            if (t <= 0) t = 0;
//            if (t >= 1) t = 1;

            ctx.drawSegment(map(t), map(0), map(t), map(1), 0x88FF0000, 4);
            ctx.drawLine(map(0), map(e), map(1), map(e), 0xFF0033FF, 6);
            ctx.drawPointCircle(map(t), map(e), 0xFF00FFFF, 8);
        }

    }

    @Override
    public void stop(GeometryContext ctx) {
        ctx.removeKeyDown(this);
    }

    private static float unmap(float t) {
        return (t + 2) / 4f;
    }

    private static float map(float t) {
        return 4 * t - 2;
    }

    @Override
    public void keyDown(GeometryContext ctx, int key, int mods, boolean repeat) {
        if (key == Input.KEY_SPACE)
            playTime = System.currentTimeMillis();
    }
}
