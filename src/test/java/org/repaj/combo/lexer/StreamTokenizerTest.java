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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Konrad Kleczkowski
 */
@DisplayName("A stream tokenizer")
class StreamTokenizerTest {
    StreamTokenizer tokenizer;
    @Nested
    @DisplayName("when source is empty")
    class WhenEmpty {
        @BeforeEach
        void setUp() {
            tokenizer = new StreamTokenizer(cb -> -1);
        }

        @Test
        @DisplayName("should fail while attempting to get token")
        void shouldFailWhenNext() {
            assertThrows(NoSuchElementException.class, () -> tokenizer.next("jabba"));
        }
    }

    @Nested
    @DisplayName("when source is closed")
    class WhenClosed {
        @BeforeEach
        void setUp() {
            StringReader reader = new StringReader("");
            tokenizer = new StreamTokenizer(reader);
            reader.close();
        }

        @Test
        @DisplayName("should fail when attempting to get token")
        void shouldFailWhenNext() {
            assertThrows(IllegalStateException.class, () -> tokenizer.next("jabba"));
        }
    }

    @Nested
    @DisplayName("when has some tokens shorter than buffer")
    class WhenHasTokens {
        @BeforeEach
        void setUp() {
            tokenizer = new StreamTokenizer(new StringReader("foo + bar - buzz * eggs"));
        }

        @Test
        @DisplayName("should return tokens")
        void shouldReturnTokens() {
            assertEquals(tokenizer.next("\\w+"), "foo");
            assertEquals(tokenizer.next("[+\\-*]"), "+");
            assertEquals(tokenizer.next("\\w+"), "bar");
            assertEquals(tokenizer.next("[+\\-*]"), "-");
            assertEquals(tokenizer.next("\\w+"), "buzz");
            assertEquals(tokenizer.next("[+\\-*]"), "*");
            assertEquals(tokenizer.next("\\w+"), "eggs");
        }

        @Test
        @DisplayName("should fail when token is mismatched")
        void shouldFailWhenNext() {
            assertThrows(InputMismatchException.class, () -> tokenizer.next("jabba"));
        }
    }

    @Nested
    @DisplayName("when tokenizer has small buffer with partial tokens")
    class WhenHasSmallBuffer {
        @BeforeEach
        void setUp() {
            tokenizer = new StreamTokenizer(new StringReader("savbhk 215745364675"), Pattern.compile("\\s*"), 8);
        }

        @Test
        @DisplayName("should return partial tokens")
        void shouldReturnToken() {
            assertEquals(tokenizer.next("savbhk"), "savbhk");
            assertEquals(tokenizer.next("[0-9]+"), "21574536");
        }
    }

    @Nested
    @DisplayName("when tokenizer has too small buffer to handle whole token")
    class WhenTokenTooBigInBuffer {
        @BeforeEach
        void setUp() {
            tokenizer = new StreamTokenizer(new StringReader("\"76349132613251\""), Pattern.compile("\\s*"), 8);
        }

        @Test
        @DisplayName("should throw an exception")
        void shouldFailWhenNext() {
            assertThrows(NoSuchElementException.class, () -> tokenizer.next("\"[0-9]*\""));
        }
    }
}