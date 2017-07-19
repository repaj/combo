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

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A set of parsers that handle repetition patterns.
 *
 * @author Konrad Kleczkowski
 */
public interface RepetitionParsers {
    /**
     * Creates parser that handles one or zero occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Optional<O>, I> zeroOrOne(Parser<O, I> parser) {
        return parser.map(Optional::ofNullable).orElseSucceed(Optional.empty());
    }

    /**
     * Creates parser that handles zero or more occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> zeroOrMore(Parser<O, I> parser) {
        return oneOrMore(parser).orElseSucceed(Stream.empty());
    }

    /**
     * Creates parser that handles one or more occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> oneOrMore(Parser<O, I> parser) {
        return parser.map(Stream::of).flatMap(o -> zeroOrMore(parser).map(os -> Stream.concat(o, os)));
    }

    /**
     * Creates parser that handles exactly {@code count} occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param count  count
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> exactly(Parser<O, I> parser, int count) {
        return Stream
                .<Parser<Stream<O>, I>>iterate(
                        Parser.succeed(Stream.empty()),
                        p -> parser.map(Stream::of).flatMap(o -> p.map(os -> Stream.concat(o, os))))
                .skip(count)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Creates parser that handles at least {@code count} occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param count  count
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> atLeast(Parser<O, I> parser, int count) {
        return exactly(parser, count).flatMap(os1 ->
                zeroOrMore(parser).map(os2 -> Stream.concat(os1, os2)));
    }

    /**
     * Creates parser that handles at least {@code from} and at most {@code to} occurrences of {@code parser} match.
     *
     * @param parser a parser
     * @param from   bottom boundary
     * @param to     upper boundary
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> between(Parser<O, I> parser, int from, int to) {
        return Stream
                .<Parser<Stream<O>, I>>iterate(
                        Parser.succeed(Stream.empty()),
                        p -> parser.map(Stream::of).flatMap(o -> p.map(os -> Stream.concat(o, os))))
                .skip(from)
                .limit(to - from)
                .reduce((a, b) -> b.orElse(() -> a))
                .orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Creates parser that handles zero or more occurrences of {@code parser} match separated by {@code separator}.
     *
     * @param parser    a parser
     * @param separator a separator parser
     * @param <O>       type of output
     * @param <I>       type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> separatedByZeroOrMore(Parser<O, I> parser, Parser<?, I> separator) {
        return separatedByOneOrMore(parser, separator).orElseSucceed(Stream.empty());
    }

    /**
     * Creates parser that handles one or more occurrences of {@code parser} match separated by {@code separator}.
     *
     * @param parser    a parser
     * @param separator a separator parser
     * @param <O>       type of output
     * @param <I>       type of input
     * @return described parser
     */
    default <O, I> Parser<Stream<O>, I> separatedByOneOrMore(Parser<O, I> parser, Parser<?, I> separator) {
        return parser.map(Stream::of).flatMap(o ->
                zeroOrMore(separator.flatMap(o1 -> parser)).map(os -> Stream.concat(o, os)));
    }

    /**
     * Creates parser that handles {@code parser} match that is surrounded with {@code begin} and {@code end}.
     *
     * @param parser a parser
     * @param begin  a begin of surrounding
     * @param end    an end of surrounding
     * @param <O>    type of output
     * @param <I>    type of input
     * @return described parser
     */
    default <O, I> Parser<O, I> surroundedWith(Parser<O, I> parser, Parser<?, I> begin, Parser<?, I> end) {
        return begin.flatMap(i -> parser.flatMap(o -> end.map(i1 -> o)));
    }
}
