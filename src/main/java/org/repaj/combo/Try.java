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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Container object that may contain value or exception during computation of it.
 *
 * @author Konrad Kleczkowski
 * @since 1.0
 */
public interface Try<T> {
    /**
     * Attempts to obtain a value from {@code callable}
     * and creates {@code Try}.
     *
     * @param callable a callable
     * @param <T>      type of value
     * @return a {@code Try}
     */
    static <T> Try<T> attempt(Callable<T> callable) {
        try {
            return success(callable.call());
        } catch (Exception e) {
            return fail(e);
        }
    }

    /**
     * Creates {@code Try} that successfully returns {@code value}.
     *
     * @param value a value
     * @param <T>   type of value
     * @return a successful {@code Try}
     */
    static <T> Try<T> success(T value) {
        return new Try<T>() {
            @Override
            public T get() throws Exception {
                return value;
            }

            @Override
            public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
                return Objects.requireNonNull(mapper).apply(value);
            }

            @Override
            public Try<T> recoverWith(Function<? super Throwable, Try<T>> mapper) {
                return this;
            }
        };
    }

    /**
     * Creates {@code Try} that fails by throwing {@code cause}.
     *
     * @param cause a cause
     * @param <T>   type of value
     * @return a failed {@code Try}
     * @throws NullPointerException if cause is {@code null}
     */
    static <T> Try<T> fail(Throwable cause) {
        Objects.requireNonNull(cause);
        return new Try<T>() {
            @Override
            public T get() throws Throwable {
                throw cause;
            }

            @SuppressWarnings("unchecked")
            @Override
            public <U> Try<U> flatMap(Function<? super T, Try<U>> mapper) {
                return (Try<U>) this;
            }

            @Override
            public Try<T> recoverWith(Function<? super Throwable, Try<T>> mapper) {
                return Objects.requireNonNull(mapper.apply(cause));
            }
        };
    }

    /**
     * If a value is present, return it, otherwise throw an exception
     * due to absence of a value.
     *
     * @return a value
     * @throws Throwable if value is absent or the computation of it
     *                   was failed
     */
    T get() throws Throwable;

    /**
     * If value is present, apply {@code Try}-bearing mapping function
     * to the value and return a result of mapping, otherwise return
     * this {@code Try}.
     *
     * @param mapper a {@code Try}-bearing mapping function
     * @param <U>    type parameter of {@code Try} returned by mapper
     * @return the result of mapper if a value is present
     * @throws NullPointerException if mapper is {@code null}
     */
    <U> Try<U> flatMap(Function<? super T, Try<U>> mapper);

    /**
     * If value is absent, apply {@code Try}-bearing mapping function
     * to the cause and return result of mapping, otherwise return
     * this {@code Try}.
     *
     * @param mapper a {@code Try}-bearing mapping function
     * @return the result of mapper if value is absent
     * @throws NullPointerException if mapper is {@code null}
     */
    Try<T> recoverWith(Function<? super Throwable, Try<T>> mapper);

    /**
     * If a value is present, return it, otherwise throw an wrapped
     * exception in {@link IllegalStateException}.
     *
     * @return a value
     * @throws IllegalStateException if value is absent or the computation of it
     *                               was failed
     */
    default T getUnchecked() throws IllegalStateException {
        try {
            return get();
        } catch (Throwable throwable) {
            throw new IllegalStateException(throwable);
        }
    }

    /**
     * If value is present, apply mapping function to the value,
     * apply result to{@link Try#success(Object)} and return it,
     * otherwise return this {@code Try}.
     *
     * @param mapper a mapper function
     * @param <U>    type of result
     * @return a {@link Try#success(Object)} if value is present,
     * otherwise this {@code Try}.
     * @throws NullPointerException if mapper is {@code null}
     */
    default <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        return flatMap(t -> success(mapper.apply(t)));
    }

    /**
     * If value is absent, apply mapping function to the value,
     * apply result to{@link Try#success(Object)} and return it,
     * otherwise return this {@code Try}.
     *
     * @param mapper a mapper function
     * @return a {@link Try#success(Object)} if value is absent,
     * otherwise this {@code Try}.
     * @throws NullPointerException if mapper is {@code null}
     */
    default Try<T> recover(Function<? super Throwable, ? extends T> mapper) {
        return recoverWith(throwable -> success(mapper.apply(throwable)));
    }

    /**
     * If value is present, and satisfies given predicate,
     * return this {@code Try}, otherwise return {@link Try#fail(Throwable)}
     * with {@link java.util.NoSuchElementException}
     *
     * @param predicate a predicate
     * @return this {@code Try}, otherwise {@link Try#fail(Throwable)}
     * with {@link java.util.NoSuchElementException}
     * @throws NullPointerException if predicate is {@code null}
     */
    default Try<T> filter(Predicate<? super T> predicate) {
        return flatMap(t -> predicate.test(t) ? this : fail(new NoSuchElementException()));
    }

    /**
     * Obtains {@code Try} from {@code trySupplier}
     * and recovers this {@code Try} with it.
     *
     * @param trySupplier a {@code Try} supplier
     * @return recovered {@code Try}
     * @throws NullPointerException if supplier or supplied
     *                              {@code Try} is {@code null}
     */
    default Try<T> orElseTry(Supplier<Try<T>> trySupplier) {
        return recoverWith(throwable -> trySupplier.get());
    }

    /**
     * Recovers this {@code Try} with {@code other}.
     *
     * @param other another {@code Try}
     * @return recovered this {@code Try}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Try<T> orElseTry(Try<T> other) {
        return orElseTry(() -> other);
    }

    /**
     * Recovers this {@code Try} with obtained value
     * from {@code supplier} and gets value.
     *
     * @param supplier a supplier
     * @return a value
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    default T orElseGet(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        return orElseTry(success(supplier.get())).getUnchecked();
    }

    /**
     * Recovers this {@code Try} with {@code other} and gets value.
     *
     * @param other another value
     * @return a value
     */
    default T orElse(T other) {
        return orElseGet(() -> other);
    }

    /**
     * Converts this {@code Try} to {@link Optional}.
     *
     * @return {@link Optional} describing this {@code Try} value
     */
    default Optional<T> toOptional() {
        return map(Optional::ofNullable).orElse(Optional.empty());
    }
}
