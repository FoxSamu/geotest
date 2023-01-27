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

public class DrawCircleScene implements TestScene, KeyDown {
    private final Grid grid = new Grid().grid(false);

    float v = 0;
    float s = 2;

    private final Animation anim1 = new Animation(t -> v = t * 3.14159265358f * 2, Animatable.QUART_IN_OUT);
    private final Animation anim2 = new Animation(t -> s = t * 2 + 2, Animatable.QUART_IN_OUT);

    @Override
    public void init(GeometryContext ctx) {
        ctx.onKeyDown(this);
    }

    @Override
    public void draw(GeometryContext ctx) {
        anim1.apply();
        anim2.apply();

        ctx.bg(0xFF111111);
        ctx.font("JetBrainsMono-Bold");
        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE);
        grid.draw(ctx);

        float sin = (float) Math.sin(v) * s;
        float cos = (float) Math.cos(v) * s;

        ctx.drawSegment(0, 0, cos, sin, 0xFFFF0000, 2);
        ctx.drawArrowEnd(cos, sin, cos, sin, 8, 0xFFFF0000, 2);

        ctx.begin();
        ctx.arc(0, 0, s, 0, -v);
        ctx.stroke(2, 0xFFFF0000);

        String text = "%.2f, %.2f".formatted(cos, sin);
        ctx.textAlign(AlignX.CENTER, AlignY.MIDDLE);
        ctx.drawTextBg(text, cos, sin, 0, -30, 0xAA000000, 16, 10, 5);
        ctx.drawText(text, cos, sin, 0, -30, 0xFFFFFFFF, 16);

        ctx.textAlign(AlignX.CENTER, AlignY.TOP);
        ctx.drawHudTextBg("Press space to start animation", ctx.windowW() / 2, 15, 0xAA000000, 16, 10, 5);
        ctx.drawHudText("Press space to start animation", ctx.windowW() / 2, 15, 0xFFFFFFFF, 16);
    }

    @Override
    public void stop(GeometryContext ctx) {
        ctx.removeKeyDown(this);
    }

    @Override
    public void keyDown(GeometryContext ctx, int key, int mods, boolean repeat) {
        if (key == Input.KEY_SPACE) {
            anim1.start(2000, 0);
            anim2.start(1000, 500);
        }
    }
}
