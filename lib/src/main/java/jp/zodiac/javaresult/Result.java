package jp.zodiac.javaresult;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A type representing either a successful value ({@link Ok}) or an error ({@link Err}).
 * This is a Java port of Rust's Result type.
 *
 * @param <T> the type of the success value
 * @param <E> the type of the error, must extend Exception
 */
public sealed interface Result<T, E extends Exception> {

    /**
     * Represents a successful result containing a value.
     *
     * @param value the success value (may be null)
     */
    public record Ok<T, E extends Exception>(T value) implements Result<T, E> {
    }

    /**
     * Represents a failed result containing an error.
     *
     * @param error the error (should not be null)
     */
    public record Err<T, E extends Exception>(E error) implements Result<T, E> {
    }

    /**
     * Returns {@code true} if this is an {@link Ok} value.
     *
     * @return {@code true} if Ok, {@code false} if Err
     */
    default boolean isOk() {
        return this instanceof Ok;
    }

    /**
     * Returns {@code true} if this is an {@link Ok} value and the predicate returns {@code true} for the contained value.
     *
     * @param predicate the predicate to test the value
     * @return {@code true} if Ok and the predicate passes, {@code false} otherwise
     */
    default boolean isOkAnd(Predicate<? super T> predicate) {
        return this instanceof Ok<T, E> ok && predicate.test(ok.value);
    }

    /**
     * Returns {@code true} if this is an {@link Err} value.
     *
     * @return {@code true} if Err, {@code false} if Ok
     */
    default boolean isErr() {
        return this instanceof Err<T, E>;
    }

    /**
     * Returns {@code true} if this is an {@link Err} value and the predicate returns {@code true} for the contained error.
     *
     * @param predicate the predicate to test the error
     * @return {@code true} if Err and the predicate passes, {@code false} otherwise
     */
    default boolean isErrAnd(Predicate<? super E> predicate) {
        return this instanceof Err<T, E> err && predicate.test(err.error);
    }

    /**
     * Converts from {@code Result<T, E>} to {@code Optional<T>}.
     * Returns the value if Ok, empty if Err.
     *
     * @return Optional containing the value if Ok, empty if Err
     */
    default Optional<T> ok() {
        return this instanceof Ok<T, E> ok ? Optional.ofNullable(ok.value) : Optional.empty();
    }

    /**
     * Converts from {@code Result<T, E>} to {@code Optional<E>}.
     * Returns the error if Err, empty if Ok.
     *
     * @return Optional containing the error if Err, empty if Ok
     */
    default Optional<E> err() {
        return this instanceof Err<T, E> err ? Optional.ofNullable(err.error) : Optional.empty();
    }

    /**
     * Maps a {@code Result<T, E>} to {@code Result<U, E>} by applying the function to the contained Ok value,
     * transforming the value type from T to U. Err values are passed through unchanged.
     *
     * @param mapper the function to apply to the Ok value
     * @param <U> the type of the new Ok value
     * @return Result with mapped value if Ok, unchanged if Err
     */
    default <U> Result<U, E> map(Function<? super T, ? extends U> mapper) {
        return this instanceof Ok<T, E> ok
                ? new Ok<U, E>(mapper.apply(ok.value))
                : new Err<U, E>(((Err<T, E>) this).error);
    }

    /**
     * Applies a function to the contained Ok value and returns the result, or returns the default value.
     *
     * @param mapper the function to apply to the Ok value
     * @param defaultValue the value to return if Err
     * @param <U> the type of the result
     * @return the mapped value if Ok, defaultValue if Err
     */
    default <U> U mapOr(Function<? super T, ? extends U> mapper, U defaultValue) {
        return this instanceof Ok<T, E> ok ? mapper.apply(ok.value) : defaultValue;
    }

    /**
     * Applies a function to the contained Ok value, or applies a fallback function to the Err value.
     *
     * @param mapper the function to apply to the Ok value
     * @param defaultMapper the function to apply to the Err value
     * @param <U> the type of the result
     * @return the result of mapper if Ok, the result of defaultMapper if Err
     */
    default <U> U mapOrElse(Function<? super T, ? extends U> mapper, Function<? super E, ? extends U> defaultMapper) {
        return this instanceof Ok<T, E> ok ? mapper.apply(ok.value) : defaultMapper.apply(((Err<T, E>) this).error);
    }

    /**
     * Maps a {@code Result<T, E>} to {@code Result<T, F>} by applying a function to the contained Err value.
     * Ok values are passed through unchanged.
     *
     * @param mapper the function to apply to the Err value
     * @param <F> the type of the new Err value
     * @return Result with mapped error if Err, unchanged if Ok
     */
    default <F extends Exception> Result<T, F> mapErr(Function<? super E, ? extends F> mapper) {
        return this instanceof Ok<T, E> ok ? new Ok<T, F>(ok.value)
                : new Err<T, F>(mapper.apply(((Err<T, E>) this).error));
    }

    /**
     * Calls the provided action with the contained Ok value (if any) and returns the Result unchanged.
     * This method is useful for side effects like logging.
     *
     * @param action the action to perform on the Ok value
     * @return this Result unchanged
     */
    default Result<T, E> inspect(Consumer<? super T> action) {
        if (this instanceof Ok<T, E> ok) {
            action.accept(ok.value);
        }
        return this;
    }

    /**
     * Calls the provided action with the contained Err value (if any) and returns the Result unchanged.
     * This method is useful for side effects like logging errors.
     *
     * @param action the action to perform on the Err value
     * @return this Result unchanged
     */
    default Result<T, E> inspectErr(Consumer<? super E> action) {
        if (this instanceof Err<T, E> err) {
            action.accept(err.error);
        }
        return this;
    }

    /**
     * Returns a sequential Stream containing the Ok value if present, otherwise returns an empty Stream.
     *
     * @return a Stream containing the Ok value, or empty if Err or value is null
     */
    default Stream<T> stream() {
        return this instanceof Ok<T, E> ok ? Stream.ofNullable(ok.value) : Stream.empty();
    }

    /**
     * Returns the contained Ok value or throws RuntimeException with the provided message.
     *
     * @param message the error message to use if this is an Err
     * @return the Ok value
     * @throws RuntimeException if this is an Err
     */
    default T expect(String message) {
        if (this instanceof Ok<T, E> ok) {
            return ok.value;
        } else {
            throw new RuntimeException(message);
        }
    }

    /**
     * Returns the contained Ok value or throws the contained error.
     *
     * @return the Ok value
     * @throws E the contained error if this is an Err
     */
    default T unwrap() throws E {
        if (this instanceof Ok<T, E> ok) {
            return ok.value;
        } else {
            throw ((Err<T, E>) this).error;
        }
    }

    /**
     * Returns the contained Ok value or the provided default value.
     *
     * @param defaultValue the value to return if this is an Err
     * @return the Ok value if Ok, defaultValue if Err
     */
    default T unwrapOr(T defaultValue) {
        return this instanceof Ok<T, E> ok ? ok.value : defaultValue;
    }

    /**
     * Returns the contained Ok value or computes a default value from the error.
     *
     * @param defaultMapper the function to compute a default value from the error
     * @return the Ok value if Ok, the result of defaultMapper if Err
     */
    default T unwrapOrElse(Function<? super E, ? extends T> defaultMapper) {
        return this instanceof Ok<T, E> ok ? ok.value : defaultMapper.apply(((Err<T, E>) this).error);
    }

    /**
     * Returns the contained Err value or throws RuntimeException with the provided message.
     *
     * @param message the error message to use if this is an Ok
     * @return the Err value
     * @throws RuntimeException if this is an Ok
     */
    default E expectErr(String message) {
        if (this instanceof Err<T, E> err) {
            return err.error;
        } else {
            throw new RuntimeException(message);
        }
    }

    /**
     * Returns the contained Err value or throws RuntimeException.
     *
     * @return the Err value
     * @throws RuntimeException if this is an Ok
     */
    default E unwrapErr() {
        if (this instanceof Err<T, E> err) {
            return err.error;
        } else {
            throw new RuntimeException("No error present");
        }
    }

    /**
     * Returns the other Result if this is Ok, otherwise returns this Err.
     *
     * @param other the Result to return if this is Ok
     * @param <U> the type of the other Result's Ok value
     * @return other if this is Ok, this Err otherwise
     */
    default <U> Result<U, E> and(Result<U, E> other) {
        return this instanceof Ok<T, E> ? other : new Err<U, E>(((Err<T, E>) this).error);
    }

    /**
     * Calls the mapper function with the contained Ok value and returns the result, or returns this Err.
     * This operation is also known as flatMap in functional programming, as it flattens nested Results.
     *
     * @param mapper the function to apply to the Ok value
     * @param <U> the type of the new Result's Ok value
     * @return the result of mapper if Ok, this Err otherwise
     */
    default <U> Result<U, E> andThen(Function<? super T, ? extends Result<U, E>> mapper) {
        return this instanceof Ok<T, E> ok ? mapper.apply(ok.value) : new Err<U, E>(((Err<T, E>) this).error);
    }

    /**
     * Returns this Result if it is Ok, otherwise returns the other Result.
     *
     * @param other the Result to return if this is Err
     * @return this if Ok, other if Err
     */
    default Result<T, E> or(Result<T, E> other) {
        return this instanceof Ok<T, E> ? this : other;
    }

    /**
     * Returns this Result if it is Ok, otherwise calls the mapper function with the Err value.
     *
     * @param mapper the function to apply to the Err value
     * @return this if Ok, the result of mapper if Err
     */
    default Result<T, E> orElse(Function<? super E, ? extends Result<T, E>> mapper) {
        return this instanceof Ok<T, E> ? this : mapper.apply(((Err<T, E>) this).error);
    }

}
