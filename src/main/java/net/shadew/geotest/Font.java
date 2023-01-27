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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Font {
    public static final String THIN = "JetBrainsMono-Thin";
    public static final String THIN_ITALIC = "JetBrainsMono-ThinItalic";
    public static final String EXTRA_LIGHT = "JetBrainsMono-ExtraLight";
    public static final String EXTRA_LIGHT_ITALIC = "JetBrainsMono-ExtraLightItalic";
    public static final String LIGHT = "JetBrainsMono-Light";
    public static final String LIGHT_ITALIC = "JetBrainsMono-LightItalic";
    public static final String REGULAR = "JetBrainsMono-Regular";
    public static final String ITALIC = "JetBrainsMono-Italic";
    public static final String MEDIUM = "JetBrainsMono-Medium";
    public static final String MEDIUM_ITALIC = "JetBrainsMono-MediumItalic";
    public static final String SEMI_BOLD = "JetBrainsMono-SemiBold";
    public static final String SEMI_BOLD_ITALIC = "JetBrainsMono-SemiBoldItalic";
    public static final String BOLD = "JetBrainsMono-Bold";
    public static final String BOLD_ITALIC = "JetBrainsMono-BoldItalic";
    public static final String EXTRA_BOLD = "JetBrainsMono-ExtraBold";
    public static final String EXTRA_BOLD_ITALIC = "JetBrainsMono-ExtraBoldItalic";

    static int load(long nvg, String resource) {
        InputStream in = Font.class.getClassLoader().getResourceAsStream("fonts/" + resource + ".ttf");
        if (in == null)
            throw new RuntimeException("No such font found in resources: " + resource);

        try (in) {
            byte[] bytes = in.readAllBytes();
            ByteBuffer ttf = memAlloc(bytes.length);
            ttf.put(bytes).flip();
            return nvgCreateFontMem(nvg, resource, ttf, 1);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + resource, e);
        }
    }
}
