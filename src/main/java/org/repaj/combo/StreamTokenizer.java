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

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konrad Kleczkowski
 */
public class StreamTokenizer {
    private Readable source;
    private CharBuffer buffer;

    public StreamTokenizer(Readable source) {
        this(source, CharBuffer.allocate(4096));
    }

    private StreamTokenizer(Readable source, CharBuffer buffer) {
        this.source = source;
        this.buffer = buffer;
    }

    public String next(Pattern pattern) throws IOException {
        Matcher matcher = pattern.matcher(buffer);
        if (hasNext(matcher)) {
            buffer.position(matcher.end());
            return matcher.group();
        }
        throw new NoSuchElementException(pattern.pattern());
    }

    public boolean hasNext(Pattern pattern) throws IOException {
        return hasNext(pattern.matcher(buffer));
    }

    private boolean hasNext(Matcher matcher) throws IOException {
        matcher.region(buffer.position(), buffer.limit());
        if (matcher.lookingAt()) {
            return !(matcher.hitEnd() && !matcher.requireEnd()) || matchAgain(matcher);
        } else if (matcher.hitEnd()) {
            return matchAgain(matcher);
        }
        return false;
    }

    private boolean matchAgain(Matcher matcher) throws IOException {
        readSome();
        matcher.region(buffer.position(), buffer.limit());
        return matcher.lookingAt();
    }

    private void readSome() throws IOException {
        buffer.compact();
        source.read(buffer);
        buffer.flip();
    }
}
