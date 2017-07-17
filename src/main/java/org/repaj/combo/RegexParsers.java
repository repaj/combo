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

import java.util.regex.Pattern;

/**
 * A set of parsers that uses regular expressions.
 *
 * @param <I> type of input
 * @author Konrad Kleczkowski
 */
public interface RegexParsers<I> {
    /**
     * Creates parser that uses regex pattern and returns matched string.
     *
     * @param pattern a regex pattern
     * @return parser that returns matched string by given pattern
     */
    Parser<String, I> pattern(Pattern pattern);

    /**
     * Creates parser that uses regex pattern and returns matched string.
     *
     * @param regex a regex pattern
     * @return parser that returns matched string by given pattern
     */
    default Parser<String, I> pattern(String regex) {
        return pattern(Pattern.compile(regex));
    }

    /**
     * Creates parser that trims whitespace on the beginning of input.
     *
     * @param parser a parser
     * @param <O> type of output
     * @return parser that trims input
     */
    default <O> Parser<O, I> trim(Parser<O, I> parser) {
        return pattern("\\s*").flatMap(s -> parser);
    }

    /**
     * Creates parser that returns trimmed and matched pattern.
     *
     * @param regex a regex pattern
     * @return parser that return token
     */
    default Parser<String, I> token(String regex) {
        return trim(pattern(regex));
    }
}
