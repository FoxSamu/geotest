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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTest extends GeometryTest implements CommandHandler {
    public static void main(String[] args) {
        start(SimpleTest.class);
    }

    @Override
    protected CommandHandler commandHandler() {
        return new CommandManager(this);
    }

    @Override
    protected void init() {
        scene(new ArrowScene());
    }

    @Override
    public void onCommand(String input) {
        System.out.println("Command was entered: " + input);
    }

    private static final Pattern HIGHLIGHT = Pattern.compile(
        """
            ("(?:(?:\\\\"|.)*?)(?:"|$))\
            |([a-zA-Z_$-][0-9a-zA-Z_$-]*)\
            |([0-9][0-9a-zA-Z_$]*(?:\\.[0-9a-zA-Z_$]+)?)\
            |(\\?)\
            |(!+)"""
    );

    @Override
    public void process(String input, int cursor, Processor processor) {
        Matcher m = HIGHLIGHT.matcher(input);
        while (m.find()) {
            int f, t;
            f = m.start(1);
            t = m.end(1);

            if (f >= 0 && t >= 0) {
                processor.highlight(f, t, 0xFFFFFF00);
            }

            f = m.start(2);
            t = m.end(2);

            if (f >= 0 && t >= 0) {
                String n = m.group(2);
                if (f == 0)
                    processor.highlight(f, t, 0xFF00DD33);
                else if (n.equals("true") || n.equals("false") || n.equals("null"))
                    processor.highlight(f, t, 0xFFFF2222);
                else if (n.equals("inf") || n.equals("nan"))
                    processor.highlight(f, t, 0xFF00BBFF);
                else if (n.startsWith("-"))
                    processor.highlight(f, t, 0xFFFF88AA);
                else
                    processor.highlight(f, t, 0xFF88FFFF);
            }

            f = m.start(3);
            t = m.end(3);

            if (f >= 0 && t >= 0) {
                processor.highlight(f, t, 0xFF00BBFF);
            }

            f = m.start(4);
            t = m.end(4);

            if (f >= 0 && t >= 0) {
                processor.highlight(f, t, 0xFFFF33FF);
            }

            f = m.start(5);
            t = m.end(5);

            if (f >= 0 && t >= 0) {
                processor.problem(f, t, "Exclamation marks not allowed");
            }
        }

        int lastSpace = input.lastIndexOf(' ') + 1;
        String sub = input.substring(lastSpace);

        List<String> suggs = List.of(
            "hello", "hai", "world", "lorem", "ipsum", "foo",
            "bar", "baz", "gus", "dolor", "sit", "amet", "words",
            "byte", "short", "int", "long", "null", "true", "false",
            "pit", "suggestion", "list", "no_exclamations", "?", "help",
            "this", "while", "if", "geotest", "autocompletion",
            "opengl", "nanovg", "accurate", "3.141592"
        );
        for (String sugg : suggs) {
            if (sugg.startsWith(sub))
                processor.suggest(lastSpace, sugg);
        }
    }
}
