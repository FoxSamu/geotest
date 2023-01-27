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

import org.joml.Vector4f;

import java.lang.reflect.InvocationTargetException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public abstract class GeometryTest {
    private static final Input ZOOM_IN = new Input(GLFW_MOD_CONTROL, GLFW_KEY_EQUAL, false);
    private static final Input ZOOM_OUT = new Input(GLFW_MOD_CONTROL, GLFW_KEY_MINUS, false);
    private static final Input ZOOM_RESET = new Input(GLFW_MOD_CONTROL, GLFW_KEY_0, false);
    private static final Input TRANSLATE_RESET = new Input(GLFW_MOD_CONTROL | GLFW_MOD_SHIFT, GLFW_KEY_0, false);
    private static final Input TRANSLATE_LEFT = new Input(GLFW_MOD_CONTROL, GLFW_KEY_LEFT, true);
    private static final Input TRANSLATE_RIGHT = new Input(GLFW_MOD_CONTROL, GLFW_KEY_RIGHT, true);
    private static final Input TRANSLATE_UP = new Input(GLFW_MOD_CONTROL, GLFW_KEY_UP, true);
    private static final Input TRANSLATE_DOWN = new Input(GLFW_MOD_CONTROL, GLFW_KEY_DOWN, true);
    private static final Input OPEN_CLI = new Input(0, GLFW_KEY_T, false);

    private final TestRuntime rt = new TestRuntime(this::init0, this::loop0, this::stop0);
    private final Space space = new Space();
    private final Vector4f bg = new Vector4f();
    private final DragAndDrop dnd = new DragAndDrop();
    private CommandLine cli;

    private NvgContext ctx;

    private TestScene scene;

    private final CursorManager cursorManager = new CursorManager();

    protected abstract void init();

    protected CommandHandler commandHandler() {
        if (this instanceof CommandHandler handler)
            return handler;
        return null;
    }

    private void init0() {
        ctx = new NvgContext(rt.window(), rt.nvg(), space, () -> !cli.focused());

        space.screenSize(rt.windowW(), rt.windowH());
        ctx.update(rt.mouseX(), rt.mouseY());

        cli = new CommandLine(ctx, rt.window(), cursorManager);
        cli.handler(commandHandler());

        cursorManager.init();

        init();

        if (this.scene != null)
            this.scene.init(ctx);

        glfwSetMouseButtonCallback(rt.window(), (window, button, action, mods) -> {
            boolean press = action == GLFW_PRESS;
            if (cli.focused()) {
                if (press)
                    cli.mouseDown(button, mods);
                else
                    cli.mouseUp(button, mods);
                return;
            }

            if (action == GLFW_PRESS) {
                if (button == GLFW_MOUSE_BUTTON_MIDDLE || button == GLFW_MOUSE_BUTTON_LEFT && (mods & GLFW_MOD_SHIFT) != 0) {
                    dnd.grab(0);
                } else if (button == GLFW_MOUSE_BUTTON_LEFT && (mods & GLFW_MOD_ALT) != 0) {
                    dnd.grab(1);
                } else {
                    ctx.mouseDown(button, mods);
                }
            } else {
                if (!dnd.drop()) {
                    ctx.mouseUp(button, mods);
                }
            }
        });

        glfwSetScrollCallback(rt.window(), (window, xoffset, yoffset) -> {
            zoom((float) yoffset * 0.1f);
        });

        glfwSetCursorPosCallback(rt.window(), (window, xpos, ypos) -> {
            if (cli.focused())
                cli.mouseMove();
            ctx.mouseMove();
        });

        glfwSetKeyCallback(rt.window(), (window, key, scancode, action, mods) -> {
            boolean press = action == GLFW_PRESS || action == GLFW_REPEAT;
            if (cli.focused()) {
                if (press)
                    cli.keyDown(key, mods, action == GLFW_REPEAT);
                else
                    cli.keyUp(key, mods);

                return;
            }

            if (ZOOM_IN.matches(key, mods)) {
                if (press)
                    space.zoom(0.5f);
                return;
            }
            if (ZOOM_OUT.matches(key, mods)) {
                if (press)
                    space.zoom(-0.5f);
                return;
            }
            if (ZOOM_RESET.matches(key, mods)) {
                if (press)
                    space.scale(1);
                return;
            }
            if (TRANSLATE_RESET.matches(key, mods)) {
                if (press) {
                    space.scale(1);
                    space.pos(0, 0);
                }
                return;
            }
            if (TRANSLATE_LEFT.matches(key, mods))
                return;
            if (TRANSLATE_RIGHT.matches(key, mods))
                return;
            if (TRANSLATE_UP.matches(key, mods))
                return;
            if (TRANSLATE_DOWN.matches(key, mods))
                return;
            if (OPEN_CLI.matches(key, mods)) {
                if (!press)
                    cli.focus();
                return;
            }

            if (press)
                ctx.keyDown(key, mods, action == GLFW_REPEAT);
            else
                ctx.keyUp(key, mods);
        });

        glfwSetCharCallback(rt.window(), (window, codepoint) -> {
            if (cli.focused()) {
                cli.charInput(codepoint);
            }
        });
    }

    private void loop0() {
        dnd.drag();
        if (!cli.focused())
            glfwSetCursor(rt.window(), NULL);

        if (!cli.focused()) {
            if (TRANSLATE_LEFT.isHeld(rt.window())) {
                space.pan(+0.1f / space.scale(), 0);
            }
            if (TRANSLATE_RIGHT.isHeld(rt.window())) {
                space.pan(-0.1f / space.scale(), 0);
            }
            if (TRANSLATE_UP.isHeld(rt.window())) {
                space.pan(0, -0.1f / space.scale());
            }
            if (TRANSLATE_DOWN.isHeld(rt.window())) {
                space.pan(0, +0.1f / space.scale());
            }
        }

        space.screenSize(rt.windowW(), rt.windowH());
        ctx.update(rt.mouseX(), rt.mouseY());

        NvgContext.argb(ctx.bg(), bg);

        glViewport(0, 0, rt.windowW(), rt.windowH());
        glClearColor(bg.x, bg.y, bg.z, bg.w);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);

        nvgBeginFrame(ctx.nvg(), rt.windowW(), rt.windowH(), rt.pixelRatio());

        if (this.scene != null)
            this.scene.draw(ctx);

        cli.draw();

        nvgEndFrame(ctx.nvg());
    }

    private void stop0() {
        if (this.scene != null)
            this.scene.stop(ctx);

        cursorManager.stop();
    }

    public void scene(TestScene scene) {
        if (this.scene != null)
            this.scene.stop(ctx);

        this.scene = scene;

        if (this.scene != null)
            this.scene.init(ctx);
    }

    public static void start(Class<? extends GeometryTest> test) {
        try {
            GeometryTest instance = test.getDeclaredConstructor().newInstance();
            instance.rt.run();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void zoom(float delta) {
        float screenX = rt.mouseX();
        float screenY = rt.mouseY();
        float worldX = space.posXToWorld(screenX);
        float worldY = space.posYToWorld(screenY);
        float translateX = space.x();
        float translateY = space.y();
        float zoom = space.scale();

        float scale = (float) Math.pow(2, delta);
        space.scale(zoom * scale);
        float nz = space.scale();
        scale = nz / zoom;

        float cwx = worldX - (worldX + translateX) / scale;
        float cwy = worldY - (worldY + translateY) / scale;
        space.pos(-cwx, -cwy);
    }

    private class DragAndDrop {
        static final int TRANSLATE = 0;
        static final int ZOOM = 1;

        float screenX, screenY;
        float worldX, worldY;
        float translateX, translateY;
        float zoom;
        boolean dragging;
        int mode;

        void grab(int m) {
            if (!dragging) {
                screenX = rt.mouseX();
                screenY = rt.mouseY();
                worldX = space.posXToWorld(screenX);
                worldY = space.posYToWorld(screenY);
                translateX = space.x();
                translateY = space.y();
                zoom = space.scale();
                dragging = true;
                mode = m;
            }
        }

        void drag() {
            if (dragging) {
                float dx = rt.mouseX() - screenX;
                float dy = rt.mouseY() - screenY;

                if (mode == TRANSLATE) {
                    space.pos(translateX + space.vecXToWorld(dx), translateY + space.vecYToWorld(dy));
                }
                if (mode == ZOOM) {
                    float scale = (float) Math.pow(2, 0.01 * (dy + dx));
                    space.scale(zoom * scale);
                    float nz = space.scale();
                    scale = nz / zoom;

                    float cwx = worldX - (worldX + translateX) / scale;
                    float cwy = worldY - (worldY + translateY) / scale;
                    space.pos(-cwx, -cwy);
                }
            }
        }

        boolean drop() {
            boolean d = dragging;
            dragging = false;
            return d;
        }
    }
}
