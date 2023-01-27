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

public class LineIntersection {
    final Vector2f a1 = new Vector2f(), a2 = new Vector2f();
    final Vector2f b1 = new Vector2f(), b2 = new Vector2f();
    float au, bu;
    float det;
    final Vector2f isc = new Vector2f();

    public void compute() {
        float x1 = a1.x;
        float x2 = a2.x;
        float x3 = b1.x;
        float x4 = b2.x;
        float y1 = a1.y;
        float y2 = a2.y;
        float y3 = b1.y;
        float y4 = b2.y;

        det = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        au = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / det;
        bu = ((x1 - x3) * (y1 - y2) - (y1 - y3) * (x1 - x2)) / det;

        isc.x = x1 + au * (x2 - x1);
        isc.y = y1 + au * (y2 - y1);
    }

    public boolean parallel() {
        return det == 0;
    }

    public boolean onA() {
        return au >= 0 && au <= 1;
    }

    public boolean onB() {
        return bu >= 0 && bu <= 1;
    }
}
