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

import org.lwjgl.opengl.GL;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

class TestRuntime {
    private long window = NULL;
    private long nvg = NULL;

    private int windowW, windowH;
    private float pixelRatio;

    private final Runnable init;
    private final Runnable loop;
    private final Runnable stop;
    private float mouseX, mouseY;

    TestRuntime(Runnable init, Runnable loop, Runnable stop) {
        this.init = init;
        this.loop = loop;
        this.stop = stop;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public void run() {
        try {
            init();

            while (loop()) {
                // That's it
            }
        } finally {
            stop();
        }
    }

    public long window() {
        return window;
    }

    public long nvg() {
        return nvg;
    }

    public float mouseX() {
        return mouseX;
    }

    public float mouseY() {
        return mouseY;
    }

    public int windowW() {
        return windowW;
    }

    public int windowH() {
        return windowH;
    }

    public float pixelRatio() {
        return pixelRatio;
    }

    private void init() {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to init GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_ANY_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_SAMPLES, 16);

        window = glfwCreateWindow(960, 540, "Geometry test", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to init window");
        }

        glfwShowWindow(window);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        glfwSwapInterval(1);

        nvg = nvgCreate(0);

        updateWindowInfo();

        init.run();
    }

    private void updateWindowInfo() {
        try (var mem = stackPush()) {
            IntBuffer w = mem.mallocInt(1);
            IntBuffer ww = mem.mallocInt(1);
            IntBuffer h = mem.mallocInt(1);

            DoubleBuffer mx = mem.mallocDouble(1);
            DoubleBuffer my = mem.mallocDouble(1);

            glfwGetFramebufferSize(window, w, h);
            windowW = w.get(0);
            windowH = h.get(0);

            glfwGetWindowSize(window, ww, null);
            pixelRatio = (float) windowW / ww.get(0);

            glfwGetCursorPos(window, mx, my);

            mouseX = (float) mx.get(0);
            mouseY = (float) my.get(0);
        }

    }

    private boolean loop() {
        updateWindowInfo();
        loop.run();

        glfwSwapBuffers(window);
        glfwPollEvents();
        return !glfwWindowShouldClose(window);
    }

    private void stop() {
        try {
            stop.run();
        } finally {
            if (nvg != NULL)
                nvgDelete(nvg);
            if (window != NULL)
                glfwDestroyWindow(window);
            glfwTerminate();
        }
    }
}
