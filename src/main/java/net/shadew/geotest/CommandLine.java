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

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

class CommandLine implements CommandHandler.Processor {
    private static final String PLACEHOLDER = "Type a command...";
    private static final String ENDCHAR = "_";

    private static final Input LEFT = new Input(0, Input.KEY_LEFT, false);
    private static final Input RIGHT = new Input(0, Input.KEY_RIGHT, false);
    private static final Input UP = new Input(0, Input.KEY_UP, false);
    private static final Input DOWN = new Input(0, Input.KEY_DOWN, false);
    private static final Input START = new Input(0, Input.KEY_HOME, false);
    private static final Input END = new Input(0, Input.KEY_END, false);
    private static final Input START_2 = new Input(Input.MOD_CONTROL, Input.KEY_LEFT, false);
    private static final Input END_2 = new Input(Input.MOD_CONTROL, Input.KEY_RIGHT, false);
    private static final Input ENTER = new Input(0, Input.KEY_ENTER, true);
    private static final Input NP_ENTER = new Input(0, Input.KEY_KP_ENTER, true);

    private static final Input TAB = new Input(0, Input.KEY_TAB, true);
    private static final Input ESC = new Input(0, Input.KEY_ESCAPE, true);
    private static final Input FOCUS_SUGGS = new Input(Input.MOD_CONTROL, Input.KEY_SPACE, true);

    private static final Input SEL_ALL = new Input(Input.MOD_CONTROL, Input.KEY_A, false);
    private static final Input SEL_RIGHT = new Input(Input.MOD_SHIFT, Input.KEY_RIGHT, false);
    private static final Input SEL_LEFT = new Input(Input.MOD_SHIFT, Input.KEY_LEFT, false);
    private static final Input SEL_START = new Input(Input.MOD_SHIFT, Input.KEY_HOME, false);
    private static final Input SEL_END = new Input(Input.MOD_SHIFT, Input.KEY_END, false);
    private static final Input SEL_START_2 = new Input(Input.MOD_SHIFT | Input.MOD_CONTROL, Input.KEY_LEFT, false);
    private static final Input SEL_END_2 = new Input(Input.MOD_SHIFT | Input.MOD_CONTROL, Input.KEY_RIGHT, false);

    private static final Input BACKSPACE = new Input(0, Input.KEY_BACKSPACE, true);
    private static final Input DELETE = new Input(0, Input.KEY_DELETE, true);
    private static final Input INS = new Input(0, Input.KEY_INSERT, true);
    private static final Input COPY = new Input(Input.MOD_CONTROL, Input.KEY_C, false);
    private static final Input CUT = new Input(Input.MOD_CONTROL, Input.KEY_X, false);
    private static final Input PASTE = new Input(Input.MOD_CONTROL, Input.KEY_V, false);
    private static final Input UNDO = new Input(Input.MOD_CONTROL, Input.KEY_Z, false);
    private static final Input REDO = new Input(Input.MOD_CONTROL | Input.MOD_SHIFT, Input.KEY_Z, false);

    private final GeometryContext ctx;
    private final long window;
    private final CursorManager cursorManager;
    private final Editor editor = new Editor();
    private final TextBox tbox = new TextBox();
    private boolean focused;

    private String text;
    private float[] charPos = new float[16];
    private int grabbedChar = 0;
    private boolean mouseDown;

    private final List<Highlight> highlights = new ArrayList<>();
    private final List<Highlight> processedHighlights = new ArrayList<>();

    private final List<Suggestion> suggestions = new ArrayList<>();
    private final List<Suggestion> processedSuggestions = new ArrayList<>();
    private int displaySuggs, suggOffset, selectedSugg;
    private boolean suggsFocused, navigating;
    private int displayErrs;

    private final List<Error> errors = new ArrayList<>();
    private final List<Error> processedErrors = new ArrayList<>();

    private final List<String> previous = new ArrayList<>();
    private int prevPos;
    private String navBackup;

    private float scrollX;

    private long blinkTimeOrig = System.currentTimeMillis();

    private CommandHandler handler;

    CommandLine(GeometryContext ctx, long window, CursorManager cursorManager) {
        this.ctx = ctx;
        this.window = window;
        this.cursorManager = cursorManager;
    }

    public CommandHandler handler() {
        return handler;
    }

    public void handler(CommandHandler handler) {
        this.handler = handler;
    }

    public void focus() {
        if (handler == null)
            return;

        editor.reset();
        focused = true;
        blinkTimeOrig = System.currentTimeMillis();

        text = null;
        suggsFocused = false;
        selectedSugg = 0;
        processInput();
    }

    public boolean focused() {
        return focused;
    }

    public void charInput(int cp) {
        if (focused) {
            if (cp < 0x20)
                return;

            int next = editor.selection() || editor.selFrom == editor.text.length() ? -1 : editor.text.charAt(editor.selFrom);
            int prev = editor.selection() || editor.selFrom == 0 ? -1 : editor.text.charAt(editor.selFrom - 1);
            if (cp == '(') {
                if (prev == '\\')
                    editor.type('(');
                else
                    editor.wrap('(', ')');
            } else if (cp == '[') {
                if (prev == '\\')
                    editor.type('[');
                else
                    editor.wrap('[', ']');
            } else if (cp == '{') {
                if (prev == '\\')
                    editor.type('{');
                else
                    editor.wrap('{', '}');
            } else if (cp == '"' && next != '"') {
                if (prev == '\\')
                    editor.type('"');
                else
                    editor.wrap('"', '"');
            } else if (cp == '\'' && next != '\'') {
                if (prev == '\\')
                    editor.type('\'');
                else
                    editor.wrap('\'', '\'');
            } else if ((cp == ')' || cp == ']' || cp == '}' || cp == '"' || cp == '\'') && next == cp && prev != '\\') {
                editor.right();
            } else if (Character.isSupplementaryCodePoint(cp)) {
                String text = new String(new int[] {cp}, 0, 1);
                editor.type(text);
            } else {
                editor.type((char) cp);
            }
            processInput();
            getInView(editor.selFrom);
            suggsFocused = false;
            navigating = false;
        }
    }

    public void keyDown(int key, int mods, boolean rep) {
        blinkTimeOrig = System.currentTimeMillis();
        if (ESC.matches(key, mods)) {
            if (suggsFocused)
                suggsFocused = false;
            else
                focused = false;
        }
        if (LEFT.matches(key, mods)) {
            editor.left();
            getInView(editor.selFrom);
            return;
        }
        if (RIGHT.matches(key, mods)) {
            editor.right();
            getInView(editor.selFrom);
            return;
        }
        if (START.matches(key, mods) || START_2.matches(key, mods)) {
            editor.start();
            getInView(editor.selFrom);
            return;
        }
        if (END.matches(key, mods) || END_2.matches(key, mods)) {
            editor.end();
            getInView(editor.selFrom);
            return;
        }
        if (SEL_LEFT.matches(key, mods)) {
            editor.selLeft();
            getInView(editor.selFrom);
            return;
        }
        if (SEL_RIGHT.matches(key, mods)) {
            editor.selRight();
            getInView(editor.selTo);
            return;
        }
        if (SEL_START.matches(key, mods) || SEL_START_2.matches(key, mods)) {
            editor.selStart();
            getInView(0);
            return;
        }
        if (SEL_END.matches(key, mods) || SEL_END_2.matches(key, mods)) {
            editor.selEnd();
            getInView(text.length());
            return;
        }
        if (SEL_ALL.matches(key, mods)) {
            editor.selAll();
            getInView(0);
            return;
        }
        if (BACKSPACE.matches(key, mods)) {
            if (!editor.selection() && editor.selFrom > 0 && editor.selFrom < editor.text.length()) {
                char before = editor.text.charAt(editor.selFrom - 1);
                char after = editor.text.charAt(editor.selFrom);

                if (before == '(' && after == ')')
                    editor.delete();
                if (before == '[' && after == ']')
                    editor.delete();
                if (before == '{' && after == '}')
                    editor.delete();
                if (before == '"' && after == '"')
                    editor.delete();
                if (before == '\'' && after == '\'')
                    editor.delete();
            }
            editor.backspace();
            processInput();
            getInView(editor.selTo);
            return;
        }
        if (DELETE.matches(key, mods)) {
            editor.delete();
            processInput();
            getInView(editor.selTo);
            return;
        }
        if (INS.matches(key, mods)) {
            editor.ins = !editor.ins;
            return;
        }
        if (COPY.matches(key, mods)) {
            editor.copy(window);
            return;
        }
        if (CUT.matches(key, mods)) {
            editor.cut(window);
            processInput();
            getInView(editor.selTo);
            return;
        }
        if (PASTE.matches(key, mods)) {
            editor.paste(window);
            processInput();
            getInView(editor.selTo);
            return;
        }
        if (UNDO.matches(key, mods)) {
            editor.undo();
            processInput();
            getInView(text.length());
            return;
        }
        if (REDO.matches(key, mods)) {
            editor.redo();
            processInput();
            getInView(text.length());
            return;
        }
        if (ENTER.matches(key, mods) || NP_ENTER.matches(key, mods)) {
            int suggMode = suggMode();
            if (suggMode == SUGG_FOCUSED) {
                Suggestion s = processedSuggestions.get(selectedSugg);
                if (editor.selFrom < s.from)
                    return;

                editor.complete(s.from, s.sugg);
                suggsFocused = false;
                return;
            }
            enter(editor.text.toString());
            if ((mods & GLFW_MOD_SHIFT) == 0)
                focused = false;
            else
                focus();
            return;
        }
        if (UP.matches(key, mods)) {
            int suggMode = suggMode();
            if (suggMode != SUGG_HIDDEN) {
                selectedSugg--;
                if (selectedSugg < 0) {
                    selectedSugg = processedSuggestions.size() - 1;
                }
                fixSuggs();
                suggsFocused = true;
            } else if (prevPos > 0) {
                if (prevPos >= previous.size()) {
                    navBackup = editor.text.toString();
                }
                prevPos--;
                String text = previous.get(prevPos);
                editor.set(text);
                navigating = true;
            }
            return;
        }
        if (DOWN.matches(key, mods)) {
            int suggMode = suggMode();
            if (suggMode != SUGG_HIDDEN) {
                selectedSugg++;
                if (selectedSugg >= processedSuggestions.size()) {
                    selectedSugg = 0;
                }
                fixSuggs();
                suggsFocused = true;
            } else if (prevPos < previous.size()) {
                prevPos++;
                if (prevPos < previous.size()) {
                    String text = previous.get(prevPos);
                    editor.set(text);
                } else {
                    editor.set(navBackup);
                }
                navigating = true;
            }
            return;
        }
        if (TAB.matches(key, mods)) {
            int suggMode = suggMode();
            if (suggMode != SUGG_HIDDEN) {
                Suggestion s = processedSuggestions.get(selectedSugg);
                if (editor.selFrom < s.from)
                    return;

                editor.complete(s.from, s.sugg);
                suggsFocused = false;
                processInput();
                getInView(editor.selFrom);
                fixScroll();
            }
            return;
        }
        if (FOCUS_SUGGS.matches(key, mods)) {
            suggsFocused = true;
            navigating = false;
        }
    }

    private static final int SUGG_HIDDEN = 0;
    private static final int SUGG_UNFOCUSED = 1;
    private static final int SUGG_FOCUSED = 2;

    private int suggMode() {
        if (!processedErrors.isEmpty() || processedSuggestions.isEmpty() || navigating || editor.selection())
            return SUGG_HIDDEN;

        if (text.isEmpty()) {
            if (suggsFocused)
                return SUGG_FOCUSED;
            else
                return SUGG_HIDDEN;
        } else {
            if (suggsFocused)
                return SUGG_FOCUSED;
            else
                return SUGG_UNFOCUSED;
        }
    }

    private void fixSuggs() {
        if (selectedSugg < suggOffset) {
            suggOffset = selectedSugg;
        }

        if (selectedSugg >= suggOffset + 8) {
            suggOffset = selectedSugg - 7;
        }

        if (suggOffset < 0)
            suggOffset = 0;
        if (suggOffset + 8 >= processedSuggestions.size()) {
            if (processedSuggestions.size() < 8)
                suggOffset = 0;
            else
                suggOffset = processedSuggestions.size() - 8;
        }
    }

    public void keyUp(int key, int mods) {
    }

    public void mouseDown(int btn, int mods) {
        float mx = ctx.mouseWindowX();
        float my = ctx.mouseWindowY();

        if (my < ctx.windowH() - 40) {
            return;
        }

        if (btn == GLFW_MOUSE_BUTTON_LEFT) {
            int c = hoverChar(mx);
            if ((mods & GLFW_MOD_SHIFT) != 0) {
                if (c < editor.selFrom) {
                    grabbedChar = editor.selTo;
                    editor.selFrom = c;
                } else {
                    grabbedChar = editor.selFrom;
                    editor.selTo = c;
                }
            } else {
                grabbedChar = c;
                editor.cursor(c);
            }
            mouseDown = true;
        }
    }

    public void mouseUp(int btn, int mods) {
        mouseDown = false;
        float my = ctx.mouseWindowY();

        if (my < ctx.windowH() - 40) {
            focused = false;
        }
    }

    public void mouseMove() {
        float mx = ctx.mouseWindowX();
        float my = ctx.mouseWindowY();

        if (mouseDown) {
            blinkTimeOrig = System.currentTimeMillis();
            int c = hoverChar(mx);
            if (c > grabbedChar) {
                editor.selFrom = grabbedChar;
                editor.selTo = c;
                editor.selLeft = false;
            } else {
                editor.selTo = grabbedChar;
                editor.selFrom = c;
                editor.selLeft = true;
            }
        }

        if (my < ctx.windowH() - 40 && !mouseDown) {
            glfwSetCursor(window, NULL);
        } else {
            glfwSetCursor(window, cursorManager.ibeam());
        }
    }

    private int hoverChar(float mx) {
        float dist = Float.POSITIVE_INFINITY;
        int nearest = 0;

        int len = text.length();
        for (int i = 0; i <= len; i++) {
            float left = charPos[i] - scrollX + 20;

            float ld = Math.abs(left - mx);
            if (ld < dist) {
                dist = ld;
                nearest = i;
            }

            if (left > mx) {
                break;
            }
        }
        return nearest;
    }

    private void enter(String command) {
        prevPos = previous.size();
        if (prevPos > 0 && previous.get(prevPos - 1).equals(command)) {
            handler.onCommand(command);
            return;
        }
        previous.add(command);
        prevPos = previous.size();
        handler.onCommand(command);
    }

    private void processInput() {
        String str = editor.text.toString();
        if (!str.equals(text)) {
            text = str;

            processPositions();

            Suggestion previouslySelectedSugg = processedSuggestions.isEmpty() ? null : processedSuggestions.get(selectedSugg);
            if (handler != null) {
                highlights.clear();
                suggestions.clear();
                errors.clear();
                handler.process(text, editor.selFrom, this);
            }

            processColors();
            processSuggestions(previouslySelectedSugg);
            processErrors();
        }
    }

    private void processColors() {
        int len = text.length();
        processedHighlights.clear();
        int lastChange = 0;
        int lastCol = 0xFFFFFFFF;
        for (int i = 0; i < len; i++) {
            int col = 0xFFFFFFFF;
            for (Highlight h : highlights) {
                if (h.in(i)) {
                    col = h.col;
                }
            }

            if (col != lastCol) {
                processedHighlights.add(new Highlight(lastChange, i, lastCol));
                lastChange = i;
                lastCol = col;
            }
        }
        processedHighlights.add(new Highlight(lastChange, len, lastCol));
    }

    private void processSuggestions(Suggestion selected) {
        processedSuggestions.clear();
        processedSuggestions.addAll(suggestions);
        processedSuggestions.sort(Comparator.comparing(Suggestion::sugg));

        displaySuggs = processedSuggestions.size();
        if (displaySuggs > 8) displaySuggs = 8;

        int n = processedSuggestions.indexOf(selected);
        if (n < 0)
            n = 0;

        selectedSugg = n;
        fixSuggs();
    }

    private void processErrors() {
        processedErrors.clear();
        processedErrors.addAll(errors);
        processedErrors.sort(Comparator.comparing(Error::from));

        displayErrs = processedErrors.size();
        if (displayErrs > 8) displayErrs = 8;
    }

    private void processPositions() {
        int len = text.length() + 1;
        if (len > charPos.length) {
            charPos = new float[len + 32];
        }

        ctx.locateHudText(ENDCHAR, 0, 0, 16, tbox);
        float pw = tbox.hi.x - tbox.lo.x;

        for (int i = 0; i < len; i++) {
            String sub = text.substring(0, i) + ENDCHAR;

            ctx.locateHudText(sub, 0, 0, 16, tbox);
            float off = tbox.hi.x - tbox.lo.x - pw;
            charPos[i] = off;
        }
    }


    private void getInView(int pos) {
        float off = charPos[pos] - scrollX;
        float boxwdt = ctx.windowW() - 40;

        int x = mouseDown ? 0 : 100;

        if (off < 20) {
            float distance = 20 - off;
            scrollX -= distance + x;
        }

        if (off > boxwdt) {
            float distance = off - boxwdt;
            scrollX += distance + x;
        }

        fixScroll();
    }

    private void fixScroll() {
        float wdt = charPos[text.length()] + 100;
        float boxwdt = ctx.windowW() - 40;
        float scrollwdt = wdt - boxwdt;

        if (scrollwdt <= 0) {
            scrollX = 0;
            return;
        }

        if (scrollX < 0) {
            scrollX = 0;
        }
        if (scrollX > scrollwdt) {
            scrollX = scrollwdt;
        }
    }

    private void drawSugg(String sugg, int from, int col, float y) {
        float x = 20 + charPos[from] - scrollX;
        ctx.drawHudText(sugg, x, y, col, 16);
    }

    private void drawError(String err, int col, float y) {
        float x = 20;
        ctx.drawHudText(err, x, y, col, 16);
    }

    private void drawSubstring(int from, int to, int col, float y) {
        float x = 20 + charPos[from] - scrollX;
        ctx.drawHudText(editor.text.substring(from, to), x, y, col, 16);
    }

    public void draw() {
        if (focused) {
            processInput();


            //
            // Scroll if user drags selection out of bounds
            //
            float mx = ctx.mouseWindowX();
            float my = ctx.mouseWindowY();

            if (mouseDown && mx > ctx.windowW() - 20) {
                float diff = mx - (ctx.windowW() - 20);
                scrollX += 0.1f * diff;
                mouseMove();
            }
            if (mouseDown && mx < 20) {
                float diff = 20 - mx;
                scrollX -= 0.1f * diff;
                mouseMove();
            }

            fixScroll();


            //
            // Draw text box background
            //
            ctx.begin();
            ctx.rectHud(0, ctx.windowH() - 40, ctx.windowW(), 40);
            ctx.fill(0xFF333333);


            //
            // Setup some stuff
            //
            AlignX ax = ctx.alignX();
            AlignY ay = ctx.alignY();

            ctx.font(Font.BOLD);
            ctx.textAlign(AlignX.LEFT, AlignY.MIDDLE);

            ctx.locateHudText(ENDCHAR, 20, ctx.windowH() - 20, 16, tbox);
            float pw = tbox.hi.x - tbox.lo.x;
            float loY = tbox.lo.y - 5;
            float hiY = tbox.hi.y + 3;


            //
            // Selection background
            //
            if (editor.selection()) {
                ctx.begin();
                ctx.moveToHud(20 + charPos[editor.selFrom] - scrollX, loY);
                ctx.lineToHud(20 + charPos[editor.selTo] - scrollX, loY);
                ctx.lineToHud(20 + charPos[editor.selTo] - scrollX, hiY);
                ctx.lineToHud(20 + charPos[editor.selFrom] - scrollX, hiY);
                ctx.close();
                ctx.fill(0x9900FF88);
            }


            //
            // Input text
            //
            if (editor.text.isEmpty()) {
                ctx.drawHudText(PLACEHOLDER, 20 - scrollX, ctx.windowH() - 20, 0x99FFFFFF, 16);
            } else {
                for (Highlight highlight : processedHighlights) {
                    float x = 20 + charPos[highlight.from] - scrollX;

                    if (editor.selection() && highlight.in(editor.selFrom) && highlight.in(editor.selTo)) {
                        drawSubstring(highlight.from, editor.selFrom, highlight.col, ctx.windowH() - 20);
                        drawSubstring(editor.selFrom, editor.selTo, 0xFFFFFFFF, ctx.windowH() - 20);
                        drawSubstring(editor.selTo, highlight.to, highlight.col, ctx.windowH() - 20);
                    } else if (editor.selection() && highlight.in(editor.selFrom)) {
                        drawSubstring(highlight.from, editor.selFrom, highlight.col, ctx.windowH() - 20);
                        drawSubstring(editor.selFrom, highlight.to, 0xFFFFFFFF, ctx.windowH() - 20);
                    } else if (editor.selection() && highlight.in(editor.selTo)) {
                        drawSubstring(highlight.from, editor.selTo, 0xFFFFFFFF, ctx.windowH() - 20);
                        drawSubstring(editor.selTo, highlight.to, highlight.col, ctx.windowH() - 20);
                    } else if (editor.selection() && editor.selFrom < highlight.from && editor.selTo >= highlight.to) {
                        drawSubstring(highlight.from, highlight.to, 0xFFFFFFFF, ctx.windowH() - 20);
                    } else {
                        drawSubstring(highlight.from, highlight.to, highlight.col, ctx.windowH() - 20);
                    }
                }
            }


            //
            // Error messages
            //
            if (errors.size() != 0) {
                int errHeight = (displayErrs - 1) * 25;
                float errY = ctx.windowH() - 50 - errHeight;

                ctx.begin();
                ctx.moveToHud(0, errY - 20);
                ctx.lineToHud(ctx.windowW(), errY - 20);
                ctx.lineToHud(ctx.windowW(), ctx.windowH() - 40);
                ctx.lineToHud(0, ctx.windowH() - 40);
                ctx.fill(0xFF333333);


                for (Error error : processedErrors) {
                    drawError(error.problem, 0xFFFF3333, errY);
                    errY += 25;
                }
            }


            //
            // Error highlights
            //
            for (Error error : errors) {
                int to = error.to;
                if (to > text.length())
                    to = text.length();
                float p1 = 20 + charPos[error.from] - scrollX;
                float p2 = 20 + charPos[to] - scrollX;
                if (to < error.to)
                    p2 += pw;

                ctx.begin();
                ctx.moveToHud(p1, hiY - 2);
                ctx.lineToHud(p2, hiY - 2);
                ctx.stroke(2, 0xFFFF0000);
            }


            //
            // Suggestions
            //
            int suggMode = suggMode();
            if (suggMode != SUGG_HIDDEN) {
                int suggHeight = (displaySuggs - 1) * 25;
                float suggY = ctx.windowH() - 50 - suggHeight;

                ctx.begin();
                ctx.moveToHud(0, suggY - 20);
                ctx.lineToHud(ctx.windowW(), suggY - 20);
                ctx.lineToHud(ctx.windowW(), ctx.windowH() - 40);
                ctx.lineToHud(0, ctx.windowH() - 40);
                ctx.fill(0xFF333333);

                for (int i = 0; i < displaySuggs; i++) {
                    if (i + suggOffset == selectedSugg) {
                        ctx.begin();
                        ctx.roundRectHud(5, suggY - 15, ctx.windowW() - 10, 27, 3);
//                        ctx.moveToHud(0, suggY - 15);
//                        ctx.lineToHud(ctx.windowW(), suggY - 15);
//                        ctx.lineToHud(ctx.windowW(), suggY + 12);
//                        ctx.lineToHud(0, suggY + 12);
                        ctx.fill(suggMode == SUGG_FOCUSED ? 0x9900AAFF : 0x99555555);
                        break;
                    }
                    suggY += 25;
                }

                suggY = ctx.windowH() - 50 - suggHeight;
                for (int i = 0; i < displaySuggs; i++) {
                    int col = suggMode == SUGG_FOCUSED ? 0xFFFFFFFF : 0x99FFFFFF;
                    if (i + suggOffset == selectedSugg) {
                        col = 0xFFFFFF00;
                    }
                    Suggestion sugg = processedSuggestions.get(i + suggOffset);
                    drawSugg(sugg.sugg, sugg.from, col, suggY);
                    suggY += 25;
                }
            }


            //
            // Cursor
            //
            int pos = editor.selLeft ? editor.selFrom : editor.selTo;
            if ((System.currentTimeMillis() - blinkTimeOrig) % 1000 <= 500) {
                if (editor.ins) {
                    ctx.begin();
                    ctx.moveToHud(20 + charPos[pos] - scrollX, loY);
                    ctx.lineToHud(20 + charPos[pos] - scrollX, hiY);
                    ctx.stroke(2, 0xFF00FF88);
                } else {
                    float off1 = charPos[pos];
                    float off2 = pos == text.length() ? off1 + pw : charPos[pos + 1];
                    ctx.begin();
                    ctx.moveToHud(20 + off1 - scrollX, hiY - 2);
                    ctx.lineToHud(20 + off2 - scrollX, hiY - 2);
                    ctx.stroke(2, 0xFF00FF88);
                }
            }

            ctx.textAlign(ax, ay);
        }
    }

    @Override
    public void highlight(int from, int to, int col) {
        highlights.add(new Highlight(from, to, col));
    }

    @Override
    public void suggest(int from, String suggestion) {
        suggestions.add(new Suggestion(from, suggestion));
    }

    @Override
    public void problem(int from, int to, String problem) {
        errors.add(new Error(from, to, problem));
    }


    static class Editor {
        final StringBuilder text = new StringBuilder();
        int selFrom;
        int selTo;
        boolean selLeft = false;
        boolean ins = true;

        final List<Undo> undoHistory = new ArrayList<>();
        int undoPos;

        long lastEdit = System.currentTimeMillis();
        int editsSinceLastSnapshot = 0;

        void edited(int edited) {
            long time = System.currentTimeMillis();
            long sinceLastEdit = time - lastEdit;
            if (sinceLastEdit >= 3000 || editsSinceLastSnapshot >= 10) {
                saveUndo();
            }

            lastEdit = time;
            editsSinceLastSnapshot += edited;
        }

        void saveUndo() {
            if (undoPos > 0) {
                Undo last = undoHistory.get(undoPos - 1);
                if (last.text.equals(text.toString()))
                    return;
            }

            editsSinceLastSnapshot = 0;

            while (undoHistory.size() > undoPos)
                undoHistory.remove(undoPos);

            undoHistory.add(new Undo(text.toString(), selFrom, selTo));
            undoPos++;
        }

        void undo() {
            if (undoPos == 0)
                return;

            if (undoPos == undoHistory.size()) {
                saveUndo();
                undoPos--;
            }

            undoPos--;
            Undo undo = undoHistory.get(undoPos);

            text.setLength(0);
            text.append(undo.text());

            selFrom = undo.selFrom();
            selTo = undo.selTo();
        }

        void redo() {
            if (undoPos >= undoHistory.size())
                return;

            Undo undo = undoHistory.get(undoPos);
            undoPos++;

            text.setLength(0);
            text.append(undo.text());

            selFrom = undo.selFrom();
            selTo = undo.selTo();
        }

        void clearUndo() {
            undoHistory.clear();
            undoPos = 0;
            saveUndo();
        }

        void reset() {
            text.setLength(0);
            selFrom = 0;
            selTo = 0;
            clearUndo();
        }

        void set(String s) {
            text.setLength(0);
            text.append(s);
            end();
            clearUndo();
        }

        void cursor(int pos) {
            selFrom = selTo = pos;
        }

        boolean selection() {
            return selFrom != selTo;
        }

        void fixSel() {
            if (selFrom > selTo) {
                int n = selFrom;
                selFrom = selTo;
                selTo = n;
            }

            if (selFrom > text.length())
                selFrom = text.length();
            if (selTo > text.length())
                selTo = text.length();
            if (selFrom < 0)
                selFrom = 0;
            if (selTo < 0)
                selTo = 0;
        }

        void deleteSel() {
            if (selection()) {
                saveUndo();
                edited(0);
                text.delete(selFrom, selTo);
                selTo = selFrom;
            }
        }

        void clearSelRight() {
            selFrom = selTo;
        }

        void clearSelLeft() {
            selTo = selFrom;
        }

        void type(char c) {
            if (selection())
                deleteSel();
            edited(1);
            if (!ins) {
                if (selFrom < text.length()) {
                    text.setCharAt(selFrom, c);
                } else {
                    text.append(c);
                }
            } else {
                text.insert(selFrom, c);
            }
            selFrom++;
            selTo++;
        }

        void wrap(char l, char r) {
            if (!ins) {
                type(l);
                return;
            }

            edited(1);
            text.insert(selFrom, l);
            text.insert(selTo + 1, r);
            selFrom++;
            selTo++;
        }

        void type(String s) {
            type(s, true);
        }

        void complete(int from, String s) {
            int to = selFrom;
            saveUndo();
            edited(0);
            text.replace(from, to, s);
            selFrom = selTo = from + s.length();
        }

        private void type(String s, boolean countAsEdit) {
            if (selection())
                deleteSel();
            if (countAsEdit)
                edited(1);
            if (!ins) {
                if (selFrom < text.length()) {
                    text.replace(selFrom, selFrom + s.length(), s);
                } else {
                    text.append(s);
                }
            } else {
                text.insert(selFrom, s);
            }
            selFrom += s.length();
            selTo += s.length();
        }

        String selected() {
            return text.substring(selFrom, selTo);
        }

        void paste(long clipboardWindow) {
            saveUndo();
            edited(0);
            type(GLFW.glfwGetClipboardString(clipboardWindow), false);
        }

        void copy(long clipboardWindow) {
            if (selection())
                GLFW.glfwSetClipboardString(clipboardWindow, selected());
        }

        void cut(long clipboardWindow) {
            copy(clipboardWindow);
            deleteSel();
        }

        void selectTo(int pos) {
            if (!selection()) {
                if (pos < selFrom) {
                    selFrom = pos;
                    selLeft = true;
                } else {
                    selTo = pos;
                    selLeft = false;
                }
            } else if (selLeft) {
                if (pos < selTo) {
                    selFrom = pos;
                } else {
                    selFrom = selTo;
                    selTo = pos;
                    selLeft = false;
                }
            } else {
                if (pos > selFrom) {
                    selTo = pos;
                } else {
                    selTo = selFrom;
                    selFrom = pos;
                    selLeft = true;
                }
            }
        }

        int selSide() {
            return selLeft ? selFrom : selTo;
        }

        void start() {
            selFrom = selTo = 0;
        }

        void end() {
            selFrom = selTo = text.length();
        }

        void selStart() {
            selectTo(0);
        }

        void selEnd() {
            selectTo(text.length());
        }

        void selAll() {
            selFrom = 0;
            selTo = text.length();
            selLeft = false;
        }

        void right() {
            if (selection()) {
                clearSelRight();
            } else {
                selFrom++;
                selTo++;
            }
            fixSel();
        }

        void left() {
            if (selection()) {
                clearSelLeft();
            } else {
                selFrom--;
                selTo--;
            }
            fixSel();
        }

        void selRight() {
            selectTo(selSide() + 1);
            fixSel();
        }

        void selLeft() {
            selectTo(selSide() - 1);
            fixSel();
        }

        void backspace() {
            if (selection()) {
                deleteSel();
            } else if (selFrom > 0) {
                selFrom--;
                selTo--;
                text.deleteCharAt(selFrom);
                fixSel();
                edited(1);
            }
        }

        void delete() {
            if (selection()) {
                deleteSel();
            } else if (selFrom < text.length()) {
                text.deleteCharAt(selFrom);
                fixSel();
                edited(1);
            }
        }
    }

    record Undo(String text, int selFrom, int selTo) {
    }

    record Highlight(int from, int to, int col) {
        public boolean in(int pos) {
            return from <= pos && pos < to;
        }
    }

    record Error(int from, int to, String problem) {
    }

    record Suggestion(int from, String sugg) {
        public int to() {
            return from + sugg.length();
        }
    }
}
