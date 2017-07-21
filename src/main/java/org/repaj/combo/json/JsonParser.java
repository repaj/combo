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

package org.repaj.combo.json;

import groovy.json.StringEscapeUtils;
import org.repaj.combo.Parser;

import java.util.AbstractMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import static java.util.function.Function.identity;
import static org.repaj.combo.RepetitionParsers.separatedByZeroOrMore;
import static org.repaj.combo.RepetitionParsers.surroundedWith;

/**
 * @author Konrad Kleczkowski
 */
public interface JsonParser<I> {
    Parser<String, I> next(Pattern pattern);

    default Parser<String, I> token(String regex) {
        return next(Pattern.compile("\\s*")).flatMap(s -> next(Pattern.compile(regex)));
    }

    default <O> Parser<O, I> jsonNull() {
        return token("null").map(s -> null);
    }

    default Parser<Boolean, I> jsonBoolean() {
        return token("(true)|(false)").map(Boolean::parseBoolean);
    }

    default Parser<Integer, I> jsonInteger() {
        return token("[+-]?[0-9]+").map(Integer::parseInt);
    }

    default Parser<Double, I> jsonDouble() {
        return token("[+-]?[0-9]+(\\.[0-9]+([eE]?[0-9]+)?)?").map(Double::parseDouble);
    }

    default Parser<String, I> jsonString() {
        return token("\"([^\"\\\\]|\\\\.)*\"")
                .map(StringEscapeUtils::unescapeJavaScript);
    }

    default Parser<Object, I> jsonPrimitive() {
        return jsonNull().map(identity())
                .orElse(() -> jsonBoolean().map(identity()))
                .orElse(() -> jsonInteger().map(identity()))
                .orElse(() -> jsonDouble().map(identity()))
                .orElse(() -> jsonString().map(identity()));
    }

    default <A, R, B, P> Parser<R, I> jsonArray(Collector<Object, A, R> arrayCollector,
                                                Collector<Map.Entry<String, Object>, B, P> objectCollector) {
        return surroundedWith(separatedByZeroOrMore(jsonValue(arrayCollector, objectCollector), token(",")), token("\\["), token("]"))
                .map(objectStream -> objectStream.collect(arrayCollector));
    }


    default <A, R, B, P> Parser<Map.Entry<String, Object>, I> jsonProperty(Collector<Object, A, R> arrayCollector,
                                                                           Collector<Map.Entry<String, Object>, B, P> objectCollector) {
        return jsonString().flatMap(key -> token(":").flatMap(colon ->
                jsonValue(arrayCollector, objectCollector).map(value ->
                        new AbstractMap.SimpleEntry<>(key, value))));
    }

    default <A, R, B, P> Parser<P, I> jsonObject(Collector<Object, A, R> arrayCollector,
                                                 Collector<Map.Entry<String, Object>, B, P> objectCollector) {
        return surroundedWith(separatedByZeroOrMore(jsonProperty(arrayCollector, objectCollector), token(",")), token("\\{"), token("}"))
                .map(entryStream -> entryStream.collect(objectCollector));
    }

    default <A, R, B, P> Parser<Object, I> jsonValue(Collector<Object, A, R> arrayCollector,
                                                     Collector<Map.Entry<String, Object>, B, P> objectCollector) {
        return jsonPrimitive()
                .orElse(() -> jsonArray(arrayCollector, objectCollector).map(identity()))
                .orElse(() -> jsonObject(arrayCollector, objectCollector).map(identity()));
    }
}
