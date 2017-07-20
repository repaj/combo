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

import java.lang.reflect.Field;
import java.util.Scanner;

/**
 * @author Konrad Kleczkowski
 */
public class ScannerParsers {
    public static Parser<String, Scanner> pattern(String regex) {
        return input -> {
            input.useDelimiter("");
            int position = getScannerPosition(input);
            String match = input.findWithinHorizon(regex, 0);
            if (match != null && input.match().start() == position) {
                return Try.success(new Parser.Result<>(match, input));
            } else {
                return Try.fail(new ParseException(regex + " expected", input.ioException()));
            }
        };
    }

    private static int getScannerPosition(Scanner scanner) {
        try {
            Class<? extends Scanner> scannerClass = scanner.getClass();
            Field field = scannerClass.getDeclaredField("position");
            field.setAccessible(true);
            return (int) field.get(scanner);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
