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

package org.repaj.combo.lexer;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Stream tokenizer. Turns stream input into tokens predicted by regex pattern.
 * It can skip some given pattern, but it's optional.
 *
 * @author Konrad Kleczkowski
 */
public class StreamTokenizer implements AutoCloseable {
    private CharBuffer buffer = CharBuffer.allocate(65536);

    private Readable source;
    private Pattern skipPattern;

    private boolean needInput;
    private boolean init;
    private boolean closed;

    private LRUCache<String, Pattern> patternCache = new LRUCache<>(16);

    /**
     * Creates tokenizer with whitespace skip pattern.
     *
     * @param source a source of characters
     */
    public StreamTokenizer(Readable source) {
        this(source, Pattern.compile("\\s*"));
    }

    /**
     * Creates tokenizer with given skip pattern.
     *
     * @param source      a source of characters
     * @param skipPattern a skip pattern
     */
    public StreamTokenizer(Readable source, Pattern skipPattern) {
        this.source = Objects.requireNonNull(source);
        this.skipPattern = skipPattern;
    }

    /**
     * Changes skip pattern. If null, then skip is not preformed.
     *
     * @param skipPattern a skip pattern
     */
    public void useSkipPattern(Pattern skipPattern) {
        this.skipPattern = skipPattern;
    }

    /**
     * Attempts to obtain next token with skipping.
     *
     * @param regex a regex string
     * @return obtained next token
     * @throws InputMismatchException if there's no token satisfying pattern
     * @throws NoSuchElementException if source is closed or hit end of stream
     */
    public String next(String regex) {
        return next(patternCache.computeIfAbsent(regex, Pattern::compile));
    }

    /**
     * Attempts to obtain next token with skipping.
     *
     * @param pattern a pattern
     * @return obtained next token
     * @throws InputMismatchException if there's no token satisfying pattern
     * @throws NoSuchElementException if source is closed or hit end of stream
     */
    public String next(Pattern pattern) {
        if (skipPattern != null) {
            rawNext(skipPattern);
        }
        return rawNext(pattern);
    }

    /**
     * Attempts to obtain next token without skipping.
     *
     * @param pattern a pattern
     * @return obtained next token
     * @throws InputMismatchException if there's no token satisfying pattern
     * @throws NoSuchElementException if source is closed or hit end of stream
     */
    public String rawNext(Pattern pattern) {
        while (true) {
            String token = getTokenFromBuffer(pattern);
            if (!closed && token != null) {
                return token;
            }
            if (needInput && !closed) {
                readSome();
            } else {
                throwFor();
            }
        }
    }

    private String getTokenFromBuffer(Pattern pattern) {
        initBuffer();

        Matcher matcher = pattern.matcher(buffer);

        if (matcher.lookingAt()) {
            if (matcher.hitEnd() && !matcher.requireEnd()) {
                needInput = true;
                return null;
            }
            buffer.position(buffer.position() + matcher.end());
            return matcher.group();
        }

        if (matcher.hitEnd() && buffer.position() > 0) {
            needInput = true;
        }

        return null;
    }

    private void readSome() {
        buffer.compact();
        try {
            source.read(buffer);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        buffer.flip();
        needInput = false;
    }

    private void initBuffer() {
        if (!init) {
            buffer.clear();
            try {
                source.read(buffer);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            buffer.flip();
            init = true;
        }
    }

    private void throwFor() {
        if (needInput || (!closed && buffer.hasRemaining())) {
            throw new InputMismatchException();
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Closes underlying source of characters, if is implementing {@link AutoCloseable}.
     *
     * @throws Exception if source cannot be closed
     * @see AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        if (!closed && source instanceof AutoCloseable) {
            ((AutoCloseable) source).close();
            closed = true;
        }
    }
}
