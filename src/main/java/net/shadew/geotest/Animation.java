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

public class Animation {
    private static final int UNSTARTED = 0;
    private static final int STARTED = 1;
    private static final int FINISHED = 2;

    private int mode = UNSTARTED;
    private long startTime;
    private int duration;

    private final Animatable animatable;
    private final Animatable.Easing easing;

    public Animation(Animatable animatable, Animatable.Easing easing) {
        this.animatable = animatable;
        this.easing = easing;
    }

    public void reset() {
        mode = UNSTARTED;
    }

    public boolean finished() {
        return mode == FINISHED;
    }

    public void start(int duration) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.mode = STARTED;
    }

    public void start(int duration, int delay) {
        this.duration = duration;
        this.startTime = System.currentTimeMillis() + delay;
        this.mode = STARTED;
    }

    public void apply() {
        if (mode == UNSTARTED) {
            animatable.set(0);
        }
        if (mode == FINISHED) {
            animatable.set(1);
        }
        if (mode == STARTED) {
            long time = System.currentTimeMillis();
            if (time < startTime) {
                animatable.set(0);
            } else if (time > startTime + duration) {
                animatable.set(1);
                mode = FINISHED;
            } else {
                float t = (time - startTime) / (float) duration;
                float e = easing.ease(t);
                animatable.set(e);
            }
        }
    }
}
