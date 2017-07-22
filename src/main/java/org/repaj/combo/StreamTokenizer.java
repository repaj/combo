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
    private static final int BUFFER_SIZE = 8192;

    private Readable source;
    private CharBuffer buffer = CharBuffer.allocate(BUFFER_SIZE);
    private boolean init;

    public StreamTokenizer(Readable source) {
        this.source = source;
    }

    public String next(Pattern pattern) throws IOException {
        Matcher matcher = pattern.matcher(buffer);
        if (hasNext(matcher)) {
            buffer.position(buffer.position() + matcher.end());
            return matcher.group();
        }
        throw new NoSuchElementException(pattern.pattern());
    }

    public boolean hasNext(Pattern pattern) throws IOException {
        return hasNext(pattern.matcher(buffer));
    }

    private boolean hasNext(Matcher matcher) throws IOException {
        initIfNeeded();
        return (matcher.lookingAt() && !matcher.hitEnd()) || refreshAndAttempt(matcher);
    }

    private void initIfNeeded() throws IOException {
        if (!init) {
            buffer.clear();
            source.read(buffer);
            buffer.flip();
            init = true;
        }
    }

    private boolean refreshAndAttempt(Matcher matcher) throws IOException {
        matcher.reset();
        refreshBuffer();
        return matcher.lookingAt() && !matcher.hitEnd();
    }

    private void refreshBuffer() throws IOException {
        buffer.compact();
        source.read(buffer);
        buffer.flip();
    }
}
