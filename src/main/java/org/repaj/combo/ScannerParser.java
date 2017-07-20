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

import java.util.Scanner;
import java.util.function.Function;

/**
 * A parser that uses {@link Scanner} as an input.
 * @author Konrad Kleczkowski
 */
public class ScannerParser {
    /**
     * Creates parser that attempt use {@link Scanner} to obtain an output value.
     * @param function a {@link Scanner}-operating function
     * @param <O> type of output
     * @return newly created parser
     */
    public static <O> Parser<O, Scanner> next(Function<Scanner, ? extends O> function) {
        return input -> Try.attempt(() -> new Parser.Result<>(function.apply(input), input));
    }
}
