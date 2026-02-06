package jp.zodiac.javaresult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ResultTest {
    @Nested
    @DisplayName("Basic type checking")
    class TypeChecking {

        @Test
        @DisplayName("isOk returns true for Ok")
        void isOkReturnsTrueForOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertTrue(result.isOk());
        }

        @Test
        @DisplayName("isOk returns false for Err")
        void isOkReturnsFalseForErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException());
            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("isErr returns false for Ok")
        void isErrReturnsFalseForOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertFalse(result.isErr());
        }

        @Test
        @DisplayName("isErr returns true for Err")
        void isErrReturnsTrueForErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException());
            assertTrue(result.isErr());
        }

        @Test
        @DisplayName("isOkAnd returns true when Ok and predicate passes")
        void isOkAndReturnsTrueWhenPredicatePasses() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertTrue(result.isOkAnd(s -> s.length() > 3));
        }

        @Test
        @DisplayName("isOkAnd returns false when Ok but predicate fails")
        void isOkAndReturnsFalseWhenPredicateFails() {
            Result<String, Exception> result = new Result.Ok<>("hi");
            assertFalse(result.isOkAnd(s -> s.length() > 3));
        }

        @Test
        @DisplayName("isOkAnd returns false when Err")
        void isOkAndReturnsFalseWhenErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException());
            assertFalse(result.isOkAnd(s -> true));
        }

        @Test
        @DisplayName("isErrAnd returns true when Err and predicate passes")
        void isErrAndReturnsTrueWhenPredicatePasses() {
            RuntimeException error = new RuntimeException("test");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            assertTrue(result.isErrAnd(e -> e.getMessage().equals("test")));
        }

        @Test
        @DisplayName("isErrAnd returns false when Err but predicate fails")
        void isErrAndReturnsFalseWhenPredicateFails() {
            RuntimeException error = new RuntimeException("test");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            assertFalse(result.isErrAnd(e -> e.getMessage().equals("error")));
        }

        @Test
        @DisplayName("isErrAnd returns false when Ok")
        void isErrAndReturnsFalseWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertFalse(result.isErrAnd(e -> true));
        }
    }

    @Nested
    @DisplayName("Value extraction")
    class ValueExtraction {

        @Test
        @DisplayName("ok returns Optional with value when Ok")
        void okReturnsOptionalWithValueWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            Optional<String> opt = result.ok();
            assertTrue(opt.isPresent());
            assertEquals("test", opt.get());
        }

        @Test
        @DisplayName("ok returns empty Optional when Err")
        void okReturnsEmptyOptionalWhenErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException());
            Optional<String> opt = result.ok();
            assertTrue(opt.isEmpty());
        }

        @Test
        @DisplayName("ok returns empty Optional when Ok with null value")
        void okReturnsEmptyOptionalWhenOkWithNull() {
            Result<String, Exception> result = new Result.Ok<>(null);
            Optional<String> opt = result.ok();
            assertTrue(opt.isEmpty());
        }

        @Test
        @DisplayName("err returns Optional with error when Err")
        void errReturnsOptionalWithErrorWhenErr() {
            RuntimeException error = new RuntimeException("test");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            Optional<RuntimeException> opt = result.err();
            assertTrue(opt.isPresent());
            assertEquals(error, opt.get());
        }

        @Test
        @DisplayName("err returns empty Optional when Ok")
        void errReturnsEmptyOptionalWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            Optional<Exception> opt = result.err();
            assertTrue(opt.isEmpty());
        }
    }

    @Nested
    @DisplayName("Stream conversion")
    class StreamConversion {

        @Test
        @DisplayName("stream returns stream with value when Ok")
        void streamReturnsStreamWithValueWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            Stream<String> stream = result.stream();
            assertEquals("test", stream.findFirst().orElse(null));
        }

        @Test
        @DisplayName("stream returns empty stream when Err")
        void streamReturnsEmptyStreamWhenErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException());
            Stream<String> stream = result.stream();
            assertEquals(0, stream.count());
        }

        @Test
        @DisplayName("stream returns empty stream when Ok with null")
        void streamReturnsEmptyStreamWhenOkWithNull() {
            Result<String, Exception> result = new Result.Ok<>(null);
            Stream<String> stream = result.stream();
            assertEquals(0, stream.count());
        }
    }

    @Nested
    @DisplayName("Null handling")
    class NullHandling {

        @Test
        @DisplayName("Function parameters throw NPE when null")
        void functionParametersThrowNPEWhenNull() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException());
            List<Executable> cases = List.of(
                    () -> okResult.isOkAnd(null),
                    () -> errResult.isOkAnd(null),
                    () -> okResult.isErrAnd(null),
                    () -> errResult.isErrAnd(null),
                    () -> okResult.map(null),
                    () -> errResult.map(null),
                    () -> okResult.mapOr(null, 0),
                    () -> errResult.mapOr(null, 0),
                    () -> okResult.mapOrElse(null, e -> -1),
                    () -> errResult.mapOrElse(null, e -> -1),
                    () -> okResult.mapOrElse(String::length, null),
                    () -> errResult.mapOrElse(String::length, null),
                    () -> okResult.mapErr(null),
                    () -> errResult.mapErr(null),
                    () -> okResult.inspect(null),
                    () -> errResult.inspect(null),
                    () -> okResult.inspectErr(null),
                    () -> errResult.inspectErr(null),
                    () -> okResult.unwrapOrElse(null),
                    () -> errResult.unwrapOrElse(null),
                    () -> okResult.and(null),
                    () -> errResult.and(null),
                    () -> okResult.andThen(null),
                    () -> errResult.andThen(null),
                    () -> okResult.or(null),
                    () -> errResult.or(null),
                    () -> okResult.orElse(null),
                    () -> errResult.orElse(null));
            cases.forEach(testCase -> assertThrows(NullPointerException.class, testCase));
        }

        @Test
        @DisplayName("andThen throws NPE when mapper returns null on Ok")
        void andThenThrowsNPEWhenMapperReturnsNullOnOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            NullPointerException thrown = assertThrows(NullPointerException.class, () -> result.andThen(value -> null));
            assertEquals("mapper result must not be null", thrown.getMessage());
        }

        @Test
        @DisplayName("orElse throws NPE when mapper returns null on Err")
        void orElseThrowsNPEWhenMapperReturnsNullOnErr() {
            Result<String, Exception> result = new Result.Err<>(new RuntimeException("error"));
            NullPointerException thrown = assertThrows(NullPointerException.class, () -> result.orElse(error -> null));
            assertEquals("mapper result must not be null", thrown.getMessage());
        }

        @Test
        @DisplayName("mapErr throws NPE when mapper returns null on Err")
        void mapErrThrowsNPEWhenMapperReturnsNullOnErr() {
            Result<String, RuntimeException> result = new Result.Err<>(new RuntimeException("error"));
            NullPointerException thrown = assertThrows(NullPointerException.class, () -> result.mapErr(error -> null));
            assertEquals("mapper result must not be null", thrown.getMessage());
        }

        @Test
        @DisplayName("Err rejects null error")
        void errRejectsNullError() {
            NullPointerException thrown = assertThrows(NullPointerException.class, () -> new Result.Err<>(null));
            assertEquals("error must not be null", thrown.getMessage());
        }

        @Test
        @DisplayName("Null messages are accepted")
        void nullMessagesAreAccepted() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertEquals("test", result.expect(null));
        }
    }

    @Nested
    @DisplayName("Transformation methods")
    class Transformation {

        @Test
        @DisplayName("map transforms Ok value")
        void mapTransformsOkValue() {
            Result<String, Exception> result = new Result.Ok<>("test");
            Result<Integer, Exception> mapped = result.map(String::length);
            assertTrue(mapped.isOk());
            assertEquals("test".length(), mapped.unwrapOr(0));
        }

        @Test
        @DisplayName("map preserves Err")
        void mapPreservesErr() {
            RuntimeException error = new RuntimeException("error");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            Result<Integer, RuntimeException> mapped = result.map(String::length);
            assertTrue(mapped.isErr());
            assertEquals(error, mapped.err().get());
        }

        @Test
        @DisplayName("mapOr and mapOrElse handle Ok and Err cases")
        void mapOrAndMapOrElseHandleOkAndErrCases() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException());

            // mapOr tests
            assertEquals("test".length(), okResult.mapOr(String::length, 0));
            assertEquals(0, errResult.mapOr(String::length, 0));

            // mapOrElse tests
            assertEquals("test".length(), okResult.mapOrElse(String::length, e -> -1));
            assertEquals(-1, errResult.mapOrElse(String::length, e -> -1));
        }

        @Test
        @DisplayName("mapErr transforms Err and preserves Ok")
        void mapErrTransformsErrAndPreservesOk() {
            Result<String, RuntimeException> errResult = new Result.Err<>(new RuntimeException("error"));
            Result<String, RuntimeException> okResult = new Result.Ok<>("test");

            // mapErr transforms Err with type change
            Result<String, IllegalArgumentException> mappedErr = errResult
                    .mapErr(e -> new IllegalArgumentException("fallback"));
            assertTrue(mappedErr.isErr());
            assertEquals("fallback", mappedErr.err().get().getMessage());
            assertTrue(mappedErr.err().get() instanceof IllegalArgumentException);

            // mapErr preserves Ok
            Result<String, IllegalArgumentException> mappedOk = okResult
                    .mapErr(e -> new IllegalArgumentException("fallback"));
            assertTrue(mappedOk.isOk());
            assertEquals("test", mappedOk.ok().get());
        }
    }

    @Nested
    @DisplayName("Side effect methods")
    class SideEffects {

        @Test
        @DisplayName("inspect and inspectErr call actions appropriately")
        void inspectAndInspectErrCallActionsAppropriately() {
            AtomicBoolean okCalled = new AtomicBoolean(false);
            AtomicBoolean errCalled = new AtomicBoolean(false);

            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException());

            // inspect calls action on Ok, not on Err
            okResult.inspect(s -> okCalled.set(true));
            errResult.inspect(s -> okCalled.set(false)); // Should not be called
            assertTrue(okCalled.get());

            // inspectErr calls action on Err, not on Ok
            errResult.inspectErr(e -> errCalled.set(true));
            okResult.inspectErr(e -> errCalled.set(false)); // Should not be called
            assertTrue(errCalled.get());
        }
    }

    @Nested
    @DisplayName("Unwrapping methods")
    class Unwrapping {

        @Test
        @DisplayName("expect returns value when Ok")
        void expectReturnsValueWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertEquals("test", result.expect("custom message"));
        }

        @Test
        @DisplayName("expect throws IllegalStateException when Err")
        void expectThrowsIllegalStateExceptionWhenErr() {
            RuntimeException error = new RuntimeException("error");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> result.expect("custom message"));
            assertEquals("custom message", thrown.getMessage());
            assertSame(error, thrown.getCause());
        }

        @Test
        @DisplayName("unwrap returns value when Ok")
        void unwrapReturnsValueWhenOk() throws Exception {
            Result<String, Exception> result = new Result.Ok<>("test");
            assertEquals("test", result.unwrap());
        }

        @Test
        @DisplayName("unwrap throws error when Err")
        void unwrapThrowsErrorWhenErr() {
            RuntimeException error = new RuntimeException("error");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            RuntimeException thrown = assertThrows(RuntimeException.class, result::unwrap);
            assertSame(error, thrown);
        }

        @Test
        @DisplayName("unwrapOr and unwrapOrElse provide fallback values")
        void unwrapOrAndUnwrapOrElseProvideFallbackValues() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException("error"));

            // unwrapOr tests
            assertEquals("test", okResult.unwrapOr("default"));
            assertEquals("default", errResult.unwrapOr("default"));

            // unwrapOrElse tests
            assertEquals("test", okResult.unwrapOrElse(e -> "fallback"));
            assertEquals("error", errResult.unwrapOrElse(e -> e.getMessage()));
        }

        @Test
        @DisplayName("expectErr returns error when Err")
        void expectErrReturnsErrorWhenErr() {
            RuntimeException error = new RuntimeException("test");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            assertEquals(error, result.expectErr("custom message"));
        }

        @Test
        @DisplayName("expectErr throws IllegalStateException when Ok")
        void expectErrThrowsIllegalStateExceptionWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> result.expectErr("custom message"));
            assertEquals("custom message", thrown.getMessage());
        }

        @Test
        @DisplayName("unwrapErr returns error when Err")
        void unwrapErrReturnsErrorWhenErr() {
            RuntimeException error = new RuntimeException("test");
            Result<String, RuntimeException> result = new Result.Err<>(error);
            assertEquals(error, result.unwrapErr());
        }

        @Test
        @DisplayName("unwrapErr throws IllegalStateException when Ok")
        void unwrapErrThrowsIllegalStateExceptionWhenOk() {
            Result<String, Exception> result = new Result.Ok<>("test");
            IllegalStateException thrown = assertThrows(IllegalStateException.class, result::unwrapErr);
            assertEquals("No error present", thrown.getMessage());
        }
    }

    @Nested
    @DisplayName("Logical operations")
    class LogicalOperations {

        @Test
        @DisplayName("and chains results appropriately")
        void andChainsResultsAppropriately() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, RuntimeException> errResult = new Result.Err<>(new RuntimeException("error"));
            Result<Integer, Exception> otherResult = new Result.Ok<>("test".length());

            // and returns other when Ok
            Result<Integer, Exception> combined1 = okResult.and(otherResult);
            assertTrue(combined1.isOk());
            assertEquals("test".length(), combined1.unwrapOr(0));

            // and returns this Err when Err
            Result<Integer, RuntimeException> combined2 = errResult.and(new Result.Ok<>("test".length()));
            assertTrue(combined2.isErr());
            assertEquals("error", combined2.err().get().getMessage());
        }

        @Test
        @DisplayName("andThen chains computations")
        void andThenChainsComputations() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            RuntimeException originalError = new RuntimeException("error");
            Result<String, RuntimeException> errResult = new Result.Err<>(originalError);

            // andThen applies mapper when Ok
            Result<Integer, Exception> mapped1 = okResult.andThen(s -> new Result.Ok<>(s.length()));
            assertTrue(mapped1.isOk());
            assertEquals("test".length(), mapped1.unwrapOr(0));

            // andThen can return Err from mapper
            RuntimeException mapperError = new RuntimeException("error");
            Result<Integer, Exception> mapped2 = okResult.andThen(s -> new Result.Err<>(mapperError));
            assertTrue(mapped2.isErr());
            assertEquals(mapperError, mapped2.err().get());

            // andThen passes through Err
            AtomicBoolean mapperCalled = new AtomicBoolean(false);
            Result<Integer, RuntimeException> mapped3 = errResult.andThen(s -> {
                mapperCalled.set(true);
                return new Result.Ok<>(s.length());
            });
            assertTrue(mapped3.isErr());
            assertFalse(mapperCalled.get());
            assertSame(originalError, mapped3.err().get());
        }

        @Test
        @DisplayName("or provides alternative results")
        void orProvidesAlternativeResults() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException());
            Result<String, Exception> alternative = new Result.Ok<>("fallback");

            // or returns this when Ok
            Result<String, Exception> combined1 = okResult.or(alternative);
            assertTrue(combined1.isOk());
            assertEquals("test", combined1.unwrapOr(""));

            // or returns other when Err
            Result<String, Exception> combined2 = errResult.or(alternative);
            assertTrue(combined2.isOk());
            assertEquals("fallback", combined2.unwrapOr(""));
        }

        @Test
        @DisplayName("orElse provides error recovery")
        void orElseProvidesErrorRecovery() {
            Result<String, Exception> okResult = new Result.Ok<>("test");
            Result<String, Exception> errResult = new Result.Err<>(new RuntimeException());

            // orElse returns this when Ok
            Result<String, Exception> mapped1 = okResult.orElse(e -> new Result.Ok<>("fallback"));
            assertTrue(mapped1.isOk());
            assertEquals("test", mapped1.unwrapOr(""));

            // orElse applies mapper when Err
            Result<String, Exception> mapped2 = errResult.orElse(e -> new Result.Ok<>("fallback"));
            assertTrue(mapped2.isOk());
            assertEquals("fallback", mapped2.unwrapOr(""));
        }
    }
}
