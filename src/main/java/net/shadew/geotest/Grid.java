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

public class Grid {
    public int base = 5;
    public float cellW = 1;
    public float cellH = 1;
    public float originX;
    public float originY;
    public float lineWidth = 1;
    public int color = 0xFFFFFFFF;

    public float detail = 0;
    public float smallAlpha = 0.3f;

    public boolean vertical = true;
    public boolean horizontal = true;

    public boolean grid = true;
    public boolean xAxis = true;
    public boolean yAxis = true;
    public float axisWidth = 2;
    public int xAxisColor = 0xFFFFFFFF;
    public int yAxisColor = 0xFFFFFFFF;

    public Grid base(int base) {
        this.base = base;
        return this;
    }

    public Grid axes(boolean axes) {
        xAxis = axes;
        yAxis = axes;
        return this;
    }

    public Grid grid(boolean grid) {
        this.grid = grid;
        return this;
    }

    public Grid cellSize(float s) {
        cellW = s;
        cellH = s;
        return this;
    }

    public Grid cellSize(float w, float h) {
        cellW = w;
        cellH = h;
        return this;
    }

    public Grid origin(float x, float y) {
        originX = x;
        originY = y;
        return this;
    }

    public Grid color(int color) {
        this.color = color;
        return this;
    }

    public void draw(GeometryContext ctx) {
        if (grid) {
            float len = ctx.lenToWorld(ctx.unitLength());
            float log = (float) (Math.log(len) / Math.log(base)) - (detail + 0.5f);

            int n = Math.round(log);
            float logdif = log + 0.5f - n;
            float scale = (float) Math.pow(base, n);
            draw(ctx, smallAlpha * (1 - logdif), scale, base);
            draw(ctx, (1 - smallAlpha) * (1 - logdif) + smallAlpha, scale * base, base);
            draw(ctx, 1, scale * base * base, -1);
        }

        if (xAxis) {
            ctx.drawLine(0, 0, 1, 0, xAxisColor, axisWidth);
        }

        if (yAxis) {
            ctx.drawLine(0, 0, 0, 1, yAxisColor, axisWidth);
        }
    }

    private void draw(GeometryContext ctx, float alpha, float scale, int skipEvery) {
        int a = color >>> 24;
        a *= alpha;
        int c = color & 0xFFFFFF | a << 24;

        float lw = ctx.lenToWorld(lineWidth);

        if (vertical) {
            float s = scale * cellW;

            float l = (ctx.screenL() - lw) / s - 2;
            float r = (ctx.screenR() + lw) / s + 2;

            long li = (long) l;
            long ri = (long) r;

            for (long i = li; i <= ri; i++) {
                if (skipEvery > 1 && i % skipEvery == 0)
                    continue;

                if (yAxis && i == 0)
                    continue;

                if (i >= l && i <= r) {
                    ctx.drawLine(i * scale, 0, i * scale, 1, c, lineWidth);
                }
            }
        }

        if (horizontal) {
            float s = scale * cellH;

            float l = (ctx.screenB() - lw) / s - 2;
            float r = (ctx.screenT() + lw) / s + 2;

            long li = (long) l;
            long ri = (long) r;

            for (long i = li; i <= ri; i++) {
                if (skipEvery > 1 && i % skipEvery == 0)
                    continue;

                if (xAxis && i == 0)
                    continue;

                if (i >= l && i <= r) {
                    ctx.drawLine(0, i * scale, 1, i * scale, c, lineWidth);
                }
            }
        }
    }
}
