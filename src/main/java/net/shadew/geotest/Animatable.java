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

public interface Animatable {
    Easing LINEAR = t -> t;
    Easing SIN_IN_OUT = t -> (float) ((1 - Math.cos(Math.PI * t)) / 2d);
    Easing SIN_IN = t -> (float) (1 - Math.cos(Math.PI / 2 * t));
    Easing SIN_OUT = t -> (float) -Math.cos(Math.PI / 2 * (t + 1));
    Easing QUAD_IN_OUT = t -> postInOut(quad(inOut(t)), t);
    Easing QUAD_IN = t -> quad(t);
    Easing QUAD_OUT = t -> postOut(quad(out(t)));
    Easing CUBIC_IN_OUT = t -> postInOut(cubic(inOut(t)), t);
    Easing CUBIC_IN = t -> cubic(t);
    Easing CUBIC_OUT = t -> postOut(cubic(out(t)));
    Easing QUART_IN_OUT = t -> postInOut(quart(inOut(t)), t);
    Easing QUART_IN = t -> quart(t);
    Easing QUART_OUT = t -> postOut(quart(out(t)));
    Easing QUINT_IN_OUT = t -> postInOut(quint(inOut(t)), t);
    Easing QUINT_IN = t -> quint(t);
    Easing QUINT_OUT = t -> postOut(quint(out(t)));
    Easing CIRC_IN_OUT = t -> postInOut(circ(inOut(t)), t);
    Easing CIRC_IN = t -> circ(t);
    Easing CIRC_OUT = t -> postOut(circ(out(t)));


    static Easing polyInOut(float p) {
        return t -> postInOut((float) Math.pow(inOut(t), p), t);
    }

    static Easing polyIn(float p) {
        return t -> (float) Math.pow(t, p);
    }

    static Easing polyOut(float p) {
        return t -> postOut((float) Math.pow(out(t), p));
    }

    static Easing hyperInOut(float d) {
        if (d == 1) return LINEAR;
        return t -> postInOut(hyperbolic(inOut(t), d), t);
    }

    static Easing hyperIn(float d) {
        if (d == 1) return LINEAR;
        return t -> hyperbolic(t, d);
    }

    static Easing hyperOut(float d) {
        if (d == 1) return LINEAR;
        return t -> postOut(hyperbolic(out(t), d));
    }

    static Easing cubicBezier1D(float cp1, float cp2) {
        return t -> cubicBezier(0, cp1, cp2, 1, t);
    }

    static Easing cubicBezier2D(float cp1x, float cp1y, float cp2x, float cp2y) {
        float c1x = Math.min(Math.max(cp1x, 0), 1);
        float c2x = Math.min(Math.max(cp2x, 0), 1);

        return t -> {
            t = Math.min(Math.max(t, 0.001f), 0.999f);
            float xa = -t;
            float xb = c1x - t;
            float xc = c2x - t;
            float xd = 1 - t;

            float rt = (float) getFirstRoot(xa, xb, xc, xd);
            rt = Math.min(Math.max(rt, 0), 1);
            return cubicBezier(0, cp1y, cp2y, 1, rt);
        };
    }

    private static float hyperbolic(float t, float d) {
        float c = d - 1;

        return (c - (d / (c * t - d) + d)) / c;
    }

    private static float circ(float t) {
        return 1 - (float) Math.sqrt(1 - t * t);
    }

    private static float quad(float t) {
        return t * t;
    }

    private static float cubic(float t) {
        return t * t * t;
    }

    private static float quart(float t) {
        return t * t * t * t;
    }

    private static float quint(float t) {
        return t * t * t * t * t;
    }

    private static float out(float t) {
        return 1 - t;
    }

    private static float postOut(float v) {
        return 1 - v;
    }

    private static float inOut(float t) {
        return 1 - Math.abs(2 * t - 1);
    }

    private static float postInOut(float v, float t) {
        return t >= 0.5f ? (1 - v + 1) / 2f : v / 2f;
    }

    void set(float t);

    interface Easing {
        float ease(float t);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static float quadBezier(float a, float c, float b, float t) {
        return lerp(lerp(a, c, t), lerp(c, b, t), t);
    }

    private static float cubicBezier(float a, float c1, float c2, float b, float t) {
        return quadBezier(lerp(a, c1, t), lerp(c1, c2, t), lerp(c2, b, t), t);
    }

    // Cardano's algorithm

    private static double accept(double t) {
        return t >= 0 && t <= 1 ? t : -1;
    }

    private static double cbrt(double v) {
        if (v < 0)
            return -Math.cbrt(-v);
        else
            return Math.cbrt(v);
    }

    private static double getFirstRoot(double pa, double pb, double pc, double pd) {
        double // Polynomial coordinates
            a = 3 * pa - 6 * pb + 3 * pc,
            b = -3 * pa + 3 * pb,
            c = pa,
            d = -pa + 3 * pb - 3 * pc + pd;

        // check: are we actually a cubic curve?
        if (d == 0) {
            // not a cubic curve, are we quadratic?
            if (a == 0) {
                // neither a quadratic curve, are we linear?
                if (b == 0) {
                    // neither linear, there's no solution
                    return -1;
                }

                // we're linear
                return accept(-c / b);
            }

            // we're quadratic
            double q = Math.sqrt(b * b - 4 * a * c), a2 = 2 * a;
            double x = accept((q - b) / a2);
            if (x < 0) x = accept((-b - q) / a2);
            return x;
        }

        // we're cubic
        a /= d;
        b /= d;
        c /= d;

        double
            p = (3 * b - a * a) / 3,
            p3 = p / 3,
            q = (2 * a * a * a - 9 * a * b + 27 * c) / 27,
            q2 = q / 2,
            discriminant = q2 * q2 + p3 * p3 * p3;

        // three roots, find first one on range 0-1
        if (discriminant < 0) {
            double mp3 = -p3;
            double mp33 = mp3 * mp3 * mp3;
            double r = Math.sqrt(mp33);
            double t = -q / (2 * r);
            double cosphi = Math.max(Math.min(t, 1), -1); //clamp(t, -1, 1);
            double phi = Math.acos(cosphi);
            double crtr = cbrt(r);
            double t1 = 2 * crtr;

            double x = accept(t1 * Math.cos(phi / 3) - a / 3);
            if (x < 0) x = accept(t1 * Math.cos((phi + 2 * Math.PI) / 3) - a / 3);
            if (x < 0) x = accept(t1 * Math.cos((phi + 4 * Math.PI) / 3) - a / 3);
            return x;
        }

        // two roots: find first one on range 0-1
        if (discriminant == 0) {
            double u1 = q2 < 0 ? cbrt(-q2) : -cbrt(q2);
            double x = accept(2 * u1 - a / 3);
            if (x < 0) x = accept(-u1 - a / 3);
            return x;
        }

        // one root: return if on range 0-1
        double sd = Math.sqrt(discriminant);
        double u1 = cbrt(sd - q2);
        double v1 = cbrt(sd + q2);

        return accept(u1 - v1 - a / 3);
    }
}
