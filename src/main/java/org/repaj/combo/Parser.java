/*
 * Copyright (c) 2017 Konrad Kleczkowski
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.repaj.combo;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An interface describing parser combinator.
 *
 * @author Konrad Kleczkowski
 */
@FunctionalInterface
public interface Parser<O, I> {
    /**
     * Parses an input.
     *
     * @param input an input
     * @return a {@code Try} of parse result
     */
    Try<Result<O, I>> parse(I input);

    /**
     * Creates parser that sequentially parses using this parser and result of {@code function}.
     *
     * @param function a mapping function
     * @param <P>      type parameter for newly created {@code Parser}'s output
     * @return newly created {@code Parser} that parses sequentially
     */
    default <P> Parser<P, I> flatMap(Function<? super O, Parser<P, I>> function) {
        return input -> parse(input).flatMap(oiResult -> function.apply(oiResult.output).parse(oiResult.input));
    }

    /**
     * Creates parser that parses using this parser and then uses {@code function} to map.
     *
     * @param function a mapping function
     * @param <P>      type of mapping result
     * @return newly created {@code Parser} that maps output
     */
    default <P> Parser<P, I> map(Function<? super O, ? extends P> function) {
        return flatMap(o -> input -> Try.success(new Result<>(function.apply(o), input)));
    }

    /**
     * Creates parser that checks if an output value satisfies predicate. If it does,
     * returns output successfully, otherwise fails.
     *
     * @param predicate an output value predicate
     * @return newly created {@code Parser} that filters output
     */
    default Parser<O, I> filter(Predicate<? super O> predicate) {
        return input -> parse(input).filter(oiResult -> predicate.test(oiResult.output));
    }

    /**
     * Creates parser that attempt to parse using this parser, and if this parser
     * fails, falls into supplied parser. If both parser fail, then newly created parser fails
     * using last fail message.
     *
     * @param parserSupplier a {@code Parser} supplier
     * @return newly created {@code Parser} that is alternative of this and supplied parser
     */
    default Parser<O, I> orElse(Supplier<Parser<O, I>> parserSupplier) {
        return input -> parse(input).orElseTry(() -> parserSupplier.get().parse(input));
    }

    /**
     * Creates parser that attempt to parse using this parser, and if this parser fails,
     * newly created parser fails with given message.
     *
     * @param message a fail message
     * @return newly created {@code Parser} that attempts to parse
     */
    default Parser<O, I> orElseFail(String message) {
        return input -> parse(input).recoverWith(cause -> Try.fail(new ParseException(message, cause)));
    }

    /**
     * A class representing result of parse process.
     *
     * @param <O> type of output
     * @param <I> type of input
     */
    class Result<O, I> {
        O output;
        I input;

        /**
         * Creates {@code Result}
         *
         * @param output an output
         * @param input  an input
         */
        Result(O output, I input) {
            this.output = output;
            this.input = input;
        }

        /**
         * Gets input.
         *
         * @return an input
         */
        public I getInput() {
            return input;
        }

        /**
         * Gets output.
         *
         * @return an output
         */
        public O getOutput() {
            return output;
        }
    }
}
