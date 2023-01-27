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

public class ArrowScene implements TestScene {
    private final Grid grid = new Grid();

    @Override
    public void init(GeometryContext ctx) {

    }

    @Override
    public void draw(GeometryContext ctx) {
        ctx.bg(0xFF111111);
        ctx.font("JetBrainsMono-Bold");
        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE);
        grid.draw(ctx);

        ctx.drawSegment(0, 0, ctx.mouseX(), ctx.mouseY(), 0xFFFF0000, 4);
        ctx.drawArrowEnd(ctx.mouseX(), ctx.mouseY(), ctx.mouseX(), ctx.mouseY(), 16, 0xFFFF0000, 4);
        ctx.drawLineCross(ctx.mouseX() / 2, ctx.mouseY() / 2, ctx.mouseX(), ctx.mouseY(), 16, 0xFFFF0000, 4);
        ctx.drawLineDash(0, 0, ctx.mouseX(), ctx.mouseY(), 16, 0xFFFF0000, 4);

        String text = "%.2f, %.2f".formatted(ctx.mouseX(), ctx.mouseY());
        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE);
        ctx.drawTextBg(text, ctx.mouseX(), ctx.mouseY(), 0, -30, 0xAA000000, 16, 10, 5);
        ctx.drawText(text, ctx.mouseX(), ctx.mouseY(), 0, -30, 0xFFFFFFFF, 16);
        text = "Mouse: %.2f, %.2f\nScale: %.0f%%\nTranslate: %.2f, %.2f".formatted(ctx.mouseX(), ctx.mouseY(), ctx.scale() * 100, ctx.translateX(), ctx.translateY());
        ctx.textAlign(AlignX.RIGHT, AlignY.TOP);
        ctx.drawHudTextBoxBg(text, ctx.windowW() - 30, 30, 0xAA000000, ctx.windowW() - 60, 16, 10, 5);
        ctx.drawHudTextBox(text, ctx.windowW() - 30, 30, 0xFFFFFFFF, ctx.windowW() - 60, 16);
    }

    @Override
    public void stop(GeometryContext ctx) {

    }
}
