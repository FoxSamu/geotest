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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;

import java.util.function.Function;

public class CommandManager implements CommandHandler {
    private final CommandDispatcher<SimpleTest> dispatcher;
    private final SimpleTest test;

    public CommandManager(SimpleTest test) {
        this.test = test;
        this.dispatcher = new CommandDispatcher<>();
        setup(dispatcher);
    }

    @Override
    public void onCommand(String input) {
        try {
            dispatcher.execute(input, test);
        } catch (CommandSyntaxException e) {
            System.err.println(e.getMessage());
        }
    }

    private static final int[] colours = {
        0xFFFFFF00,
        0xFFFF2222,
        0xFF00BBFF,
        0xFFFF88AA,
        0xFF00FFFF
    };

    @Override
    public void process(String input, int cursor, Processor processor) {
        ParseResults<SimpleTest> results = dispatcher.parse(input, test);

        for (CommandSyntaxException exc : results.getExceptions().values()) {
            processor.problem(exc.getCursor(), exc.getCursor() + 1, exc.getMessage());
        }

        CommandContext<SimpleTest> context = results.getContext().build(input);

        int i = 0;
        for (ParsedCommandNode<SimpleTest> node : context.getNodes()) {
            processor.highlight(node.getRange().getStart(), node.getRange().getEnd(), i == 0 ? 0xFF00DD33 : colours[i % colours.length]);
            i++;
        }

        try {
            SuggestionContext<SimpleTest> ctx = results.getContext().findSuggestionContext(cursor);

            SuggestionsBuilder builder = new SuggestionsBuilder(input, ctx.startPos);

            for (CommandNode<SimpleTest> child : ctx.parent.getChildren()) {
                child.listSuggestions(results.getContext().build(input), builder);
            }

            Suggestions suggs = builder.build();
            for (Suggestion sugg : suggs.getList()) {
                processor.suggest(sugg.getRange().getStart(), sugg.getText());
            }
        } catch (Exception ignored) {
        }
    }

    private static void setup(CommandDispatcher<SimpleTest> dispatcher) {
        setupEasings(dispatcher);
        setupScenes(dispatcher);
    }

    private static void setupScenes(CommandDispatcher<SimpleTest> dispatcher) {
        dispatcher.register(
            literal("scene")
                .then(literal("arrow").executes(ctx -> {
                    ctx.getSource().scene(new ArrowScene());
                    return 0;
                }))
                .then(literal("circle").executes(ctx -> {
                    ctx.getSource().scene(new DrawCircleScene());
                    return 0;
                }))
                .then(literal("easing").redirect(dispatcher.getRoot().getChild("easing")))
        );
    }

    private static void setupEasings(CommandDispatcher<SimpleTest> dispatcher) {
        dispatcher.register(
            literal("easing")
                .then(cb1("bezier_1d"))
                .then(cb2("bezier_2d"))
                .then(argEase("hyper_in", 4, Animatable::hyperIn))
                .then(argEase("hyper_out", 4, Animatable::hyperOut))
                .then(argEase("hyper_in_out", 4, Animatable::hyperInOut))
                .then(argEase("poly_in", 2, Animatable::polyIn))
                .then(argEase("poly_out", 2, Animatable::polyOut))
                .then(argEase("poly_in_out", 2, Animatable::polyInOut))
                .then(easing("linear", Animatable.LINEAR))
                .then(easing("sin_in", Animatable.SIN_IN))
                .then(easing("sin_out", Animatable.SIN_OUT))
                .then(easing("sin_in_out", Animatable.SIN_IN_OUT))
                .then(easing("quad_in", Animatable.QUAD_IN))
                .then(easing("quad_out", Animatable.QUAD_OUT))
                .then(easing("quad_in_out", Animatable.QUAD_IN_OUT))
                .then(easing("cubic_in", Animatable.CUBIC_IN))
                .then(easing("cubic_out", Animatable.CUBIC_OUT))
                .then(easing("cubic_in_out", Animatable.CUBIC_IN_OUT))
                .then(easing("quart_in", Animatable.QUART_IN))
                .then(easing("quart_out", Animatable.QUART_OUT))
                .then(easing("quart_in_out", Animatable.QUART_IN_OUT))
                .then(easing("quint_in", Animatable.QUINT_IN))
                .then(easing("quint_out", Animatable.QUINT_OUT))
                .then(easing("quint_in_out", Animatable.QUINT_IN_OUT))
                .then(easing("circ_in", Animatable.CIRC_IN))
                .then(easing("circ_out", Animatable.CIRC_OUT))
                .then(easing("circ_in_out", Animatable.CIRC_IN_OUT))
        );
    }

    private static LiteralArgumentBuilder<SimpleTest> easing(String name, Animatable.Easing easing) {
        return literal(name).executes(ctx -> {
            ctx.getSource().scene(new EasingScene(easing));
            return 0;
        });
    }

    private static LiteralArgumentBuilder<SimpleTest> cb1(String name) {
        return literal(name)
                   .executes(ctx -> {
                       ctx.getSource().scene(new EasingScene(0, 1));
                       return 0;
                   })
                   .then(
                       argument("p1y", FloatArgumentType.floatArg()).then(
                           argument("p2y", FloatArgumentType.floatArg()).executes(ctx -> {
                               ctx.getSource().scene(new EasingScene(
                                   FloatArgumentType.getFloat(ctx, "p1y"),
                                   FloatArgumentType.getFloat(ctx, "p2y")
                               ));
                               return 0;
                           })
                       )
                   )

            ;
    }

    private static LiteralArgumentBuilder<SimpleTest> cb2(String name) {
        return literal(name)
                   .executes(ctx -> {
                       ctx.getSource().scene(new EasingScene(0.5f, 0, 0.5f, 1));
                       return 0;
                   })
                   .then(
                       argument("p1x", FloatArgumentType.floatArg(0, 1)).then(
                           argument("p1y", FloatArgumentType.floatArg()).then(
                               argument("p2x", FloatArgumentType.floatArg(0, 1)).then(
                                   argument("p2y", FloatArgumentType.floatArg()).executes(ctx -> {
                                       ctx.getSource().scene(new EasingScene(
                                           FloatArgumentType.getFloat(ctx, "p1x"),
                                           FloatArgumentType.getFloat(ctx, "p1y"),
                                           FloatArgumentType.getFloat(ctx, "p2x"),
                                           FloatArgumentType.getFloat(ctx, "p2y")
                                       ));
                                       return 0;
                                   })
                               )
                           )
                       )
                   )

            ;
    }

    private static LiteralArgumentBuilder<SimpleTest> argEase(String name, float def, Function<Float, Animatable.Easing> easingGen) {
        return literal(name)
                   .executes(ctx -> {
                       ctx.getSource().scene(new EasingScene(easingGen.apply(def)));
                       return 0;
                   })
                   .then(
                       argument("arg", FloatArgumentType.floatArg(0)).executes(ctx -> {
                           ctx.getSource().scene(new EasingScene(
                               easingGen.apply(FloatArgumentType.getFloat(ctx, "arg"))
                           ));
                           return 0;
                       })
                   )

            ;
    }

    private static LiteralArgumentBuilder<SimpleTest> literal(String id) {
        return LiteralArgumentBuilder.literal(id);
    }

    private static <T> RequiredArgumentBuilder<SimpleTest, T> argument(String id, ArgumentType<T> arg) {
        return RequiredArgumentBuilder.argument(id, arg);
    }
}
