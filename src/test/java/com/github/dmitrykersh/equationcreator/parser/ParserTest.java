package com.github.dmitrykersh.equationcreator.parser;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ParserTest {
    private static final Random random = new Random();
    private static final Parser parser = new Parser("");

    @ParameterizedTest
    @CsvSource({
                       "+, -",
                       "-, +",
                       "*, /",
                       "/, *",
                       "abc, abc"
               })
    public void negateValue_ALL_IN_ONE(String arg, String res) {
        assertEquals(res, Parser.negateValue(arg));
    }

    @ParameterizedTest
    @CsvSource({
                       "2 $+ 3, 5",
                       "12 $- 6, 6",
                       "2 $^ 3, 8",
                       "12 $- 6 $* 3, -6",
                       "12 $/ 6 $- 3, -1",
               })
    public void evaluateSimpleEquation_VALID_INPUT(String arg, String res) {
        assertEquals(res, Parser.evaluateSimpleEquation(arg));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2 + 3", "12 - 6as", "2 ^$ 3", "2$^3"})
    public void evaluateSimpleEquation_WRONG_INPUT(String arg) {
        assertEquals(arg, Parser.evaluateSimpleEquation(arg));
    }

    @ParameterizedTest
    @CsvSource({
                       "13 $- 6 * 3, 7 * 3",
                       "12a $+ 12 - 1, 12a $+ 12 - 1",
                       "12d $+ 4 $- 1, 12d $+ 3",
               })
    public void evaluateSimpleEquation_MIXED_INPUT(String arg, String res) {
        // mixed $-operations, arithmetic signs and other shit
        // only $-operations should be touched
        assertEquals(res, Parser.evaluateSimpleEquation(arg));
    }

    @ParameterizedTest
    @CsvSource({
                       // 2^5 - 10*2 + 2 + 3^2 * 2 (= 32)
                       // 13 - 12 + 11*10 - 22*5 + 99 + 2^2^2^2 (= 356)
                       "2 $^ 5 $- 10 $* 2 $+ 2 $+ 3 $^ 2 $* 2, 32",
                       "13 $- 12 $+ 11 $* 10 $- 22 $* 5 $+ 99 $+ 2 $^ 2 $^ 2 $^ 2, 356",
               })
    public void evaluateSimpleEquation_LONG_EQUATIONS(String arg, String res) {
        assertEquals(res, Parser.evaluateSimpleEquation(arg));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Foo Bar Baz", "Sample Text...", "Not a range: [1..a]", "Not a float range: [1..2|s]",
            "31$-31", "qwe rty uiop", "Not a range: [1...4]", "<thisIsUndefinedVariable>",
    })
    public void createEquation_NO_PARSING(String arg) {
        parser.setFormat(arg);
        assertEquals(arg, parser.createEquation(random));
    }

    private static Stream<Arguments> argumentsStreamForIntRange() {
        return Stream.of(
                arguments("[1..1]", Arrays.asList("1")),
                arguments("[1..3]", Arrays.asList("1", "2", "3")),
                arguments("[-1..0]", Arrays.asList("-1", "0")),
                arguments("[-1..-3]", Arrays.asList("-1", "-2", "-3")),
                arguments("qwe[1..-1]", Arrays.asList("qwe-1", "qwe0", "qwe1")),
                arguments("a[-1..-3]a", Arrays.asList("a-1a", "a-2a", "a-3a"))
        );
    }

    private static Stream<Arguments> argumentsStreamForFloatRangeStep() {
        return Stream.of(
                arguments("[1..2|0.5]", Arrays.asList("1", "1.5", "2")),
                arguments("[1..1.3|0.11]", Arrays.asList("1", "1.11", "1.22")),
                arguments("[1..1.2|0.1]", Arrays.asList("1", "1.1", "1.2")),
                arguments("[1..0|0.3]", Arrays.asList("0", "0.3", "0.6", "0.9")),
                arguments("[1..1.2|3]", Arrays.asList("1")),
                arguments("[1..0.1|0.4]", Arrays.asList("0.1", "0.5", "0.9"))
        );
    }

    private static Stream<Arguments> argumentsStreamForFloatRangeDivisor() {
        return Stream.of(
                arguments("[1..2|:0.5]", Arrays.asList("1", "1.5", "2")),
                arguments("[1..1.3|:0.11]", Arrays.asList("1.1", "1.21")),
                arguments("[1..1.2|:0.1]", Arrays.asList("1", "1.1", "1.2")),
                arguments("[25..1|:7]", Arrays.asList("7", "14", "21")),
                arguments("[1..1.2|:3]", Collections.emptyList()),
                arguments("[1..0.1|:0.4]", Arrays.asList("0", "0.4", "0.8"))
        );
    }

    private static Stream<Arguments> argumentsStreamForList() {
        return Stream.of(
                arguments("{abc|q|1q2w|1423}", Arrays.asList("abc", "q", "1q2w", "1423")),
                arguments("{D|Nat|M}asha {Andr|Serg}eeva",
                        Arrays.asList("Dasha Andreeva", "Masha Andreeva", "Natasha Andreeva", "Dasha Sergeeva",
                                "Masha Sergeeva", "Natasha Sergeeva"))
        );
    }

    private static Stream<Arguments> argumentsStreamForComplexSyntax() {
        return Stream.of(
                arguments("{[1..2]|[7..7.5|0.2]}", Arrays.asList("1", "2", "7", "7.2", "7.4")),
                arguments("[1..2|:{0.3|0.5}]", Arrays.asList("1", "1.5", "2", "1.2", "1.8")),
                arguments("{abc|[1..3]|[5..6]{a|b}F}", Arrays.asList("abc", "1", "2", "3", "5aF", "6aF", "5bF", "6bF"))
        );
    }

    private static Stream<Arguments> argumentsStreamForVariables() {
        return Stream.of(
                arguments("var=<Foo>: <var>", Arrays.asList("Foo: Foo")),
                arguments("var=<Foo> var2=<Bar> <var><var2><var>", Arrays.asList("Foo Bar FooBarFoo")),
                arguments("var=<Foo> var2=<Bar> {<var>|<var2>}", Arrays.asList("Foo Bar Foo", "Foo Bar Bar")),
                arguments("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>",
                        Arrays.asList("1 Foo 1 11", "1 Foo Foo FooFoo", "2 Foo 2 22", "2 Foo Foo FooFoo", "3 Foo 3 33",
                                "3 Foo Foo FooFoo"))
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForIntRange")
    public void createEquation_INT_RANGE(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForFloatRangeStep")
    public void createEquation_FLOAT_RANGE_STEP(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForFloatRangeDivisor")
    public void createEquation_FLOAT_RANGE_DIVISOR(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForList")
    public void createEquation_LIST(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForComplexSyntax")
    public void createEquation_COMPLEX_SYNTAX(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamForVariables")
    public void createEquation_VARIABLES(final String format, final List<String> allowedValues) {
        parser.setFormat(format);
        assertTrue(allowedValues.contains(parser.createEquation(random)));
    }

    @Test
    public void createEquation_INNER_VARIABLES() {

    }

    @Test
    public void createEquation_EVERYTHING() {

    }
}