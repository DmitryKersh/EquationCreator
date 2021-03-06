package com.github.dmitrykersh.equationcreator.parser;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @RepeatedTest(3)
    public void createEquation_INT_RANGE() {
        Map<String, Set<String>> cases = new HashMap<>();

        cases.put("[1..1]", new HashSet<>());
        cases.get("[1..1]").add("1");

        cases.put("[1..3]", new HashSet<>());
        cases.get("[1..3]").add("1");
        cases.get("[1..3]").add("2");
        cases.get("[1..3]").add("3");

        cases.put("[-1..0]", new HashSet<>());
        cases.get("[-1..0]").add("-1");
        cases.get("[-1..0]").add("0");

        // unusual order, but it also must work
        cases.put("[-1..-3]", new HashSet<>());
        cases.get("[-1..-3]").add("-3");
        cases.get("[-1..-3]").add("-2");
        cases.get("[-1..-3]").add("-1");

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_FLOAT_RANGE_STEP() {
        Map<String, Set<String>> cases = new HashMap<>();

        cases.put("[1..2|0.5]", new HashSet<>());
        cases.get("[1..2|0.5]").add("1");
        cases.get("[1..2|0.5]").add("1.5");
        cases.get("[1..2|0.5]").add("2");

        cases.put("[1..1.3|0.11]", new HashSet<>());
        cases.get("[1..1.3|0.11]").add("1");
        cases.get("[1..1.3|0.11]").add("1.11");
        cases.get("[1..1.3|0.11]").add("1.22");

        cases.put("[1..1.2|0.1]", new HashSet<>());
        cases.get("[1..1.2|0.1]").add("1");
        cases.get("[1..1.2|0.1]").add("1.1");
        cases.get("[1..1.2|0.1]").add("1.2");

        cases.put("[-3..1|1.5]", new HashSet<>());
        cases.get("[-3..1|1.5]").add("-3");
        cases.get("[-3..1|1.5]").add("-1.5");
        cases.get("[-3..1|1.5]").add("0");

        cases.put("[1..1.2|3]", new HashSet<>());
        cases.get("[1..1.2|3]").add("1");

        cases.put("[1..0|0.3]", new HashSet<>());
        cases.get("[1..0|0.3]").add("0");
        cases.get("[1..0|0.3]").add("0.3");
        cases.get("[1..0|0.3]").add("0.6");
        cases.get("[1..0|0.3]").add("0.9");

        cases.put("[1..0.1|0.4]", new HashSet<>());
        cases.get("[1..0.1|0.4]").add("0.1");
        cases.get("[1..0.1|0.4]").add("0.5");
        cases.get("[1..0.1|0.4]").add("0.9");

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_FLOAT_RANGE_DIVISOR() {
        Map<String, Set<String>> cases = new HashMap<>();

        cases.put("[1..2|:0.5]", new HashSet<>());
        cases.get("[1..2|:0.5]").add("1");
        cases.get("[1..2|:0.5]").add("1.5");
        cases.get("[1..2|:0.5]").add("2");

        cases.put("[1..1.3|:0.11]", new HashSet<>());
        cases.get("[1..1.3|:0.11]").add("1.1");
        cases.get("[1..1.3|:0.11]").add("1.21");

        cases.put("[1..1.2|:0.55]", new HashSet<>());
        cases.get("[1..1.2|:0.55]").add("1.1");

        cases.put("[-3..1|:1.3]", new HashSet<>());
        cases.get("[-3..1|:1.3]").add("-2.6");
        cases.get("[-3..1|:1.3]").add("-1.3");
        cases.get("[-3..1|:1.3]").add("0");

        // no divisors
        cases.put("[1..1.2|:3]", new HashSet<>());
        cases.get("[1..1.2|:3]").add("1");

        cases.put("[1..0.1|:0.4]", new HashSet<>());
        cases.get("[1..0.1|:0.4]").add("0.4");
        cases.get("[1..0.1|:0.4]").add("0.8");

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_LIST() {
        Map<String, Set<String>> cases = new HashMap<>();

        cases.put("{abc|q|1q2w|1423}", new HashSet<>());
        cases.get("{abc|q|1q2w|1423}").add("abc");
        cases.get("{abc|q|1q2w|1423}").add("q");
        cases.get("{abc|q|1q2w|1423}").add("1q2w");
        cases.get("{abc|q|1q2w|1423}").add("1423");

        cases.put("{D|Nat|M}asha {Andr|Serg}eeva", new HashSet<>());
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Dasha Andreeva");
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Masha Andreeva");
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Natasha Andreeva");
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Dasha Sergeeva");
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Masha Sergeeva");
        cases.get("{D|Nat|M}asha {Andr|Serg}eeva").add("Natasha Sergeeva");

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(5)
    public void createEquation_COMPLEX_SYNTAX() {
        Map<String, Set<String>> cases = new HashMap<>();
        //--------------------------------------------------------------------------
        cases.put("{[1..2]|[7..7.5|0.2]}", new HashSet<>());

        cases.get("{[1..2]|[7..7.5|0.2]}").add("1");
        cases.get("{[1..2]|[7..7.5|0.2]}").add("2");

        cases.get("{[1..2]|[7..7.5|0.2]}").add("7");
        cases.get("{[1..2]|[7..7.5|0.2]}").add("7.2");
        cases.get("{[1..2]|[7..7.5|0.2]}").add("7.4");
        //--------------------------------------------------------------------------
        cases.put("[1..2|:{0.3|0.5}]", new HashSet<>());

        cases.get("[1..2|:{0.3|0.5}]").add("1");
        cases.get("[1..2|:{0.3|0.5}]").add("1.5");
        cases.get("[1..2|:{0.3|0.5}]").add("2");

        cases.get("[1..2|:{0.3|0.5}]").add("1.2");
        cases.get("[1..2|:{0.3|0.5}]").add("1.8");
        //--------------------------------------------------------------------------
        cases.put("{abc|[1..3]|[5..6]{a|b}F}", new HashSet<>());

        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("abc");

        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("1");
        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("2");
        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("3");

        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("5aF");
        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("6aF");
        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("5bF");
        cases.get("{abc|[1..3]|[5..6]{a|b}F}").add("6bF");
        //--------------------------------------------------------------------------

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_VARIABLES() {
        Map<String, Set<String>> cases = new HashMap<>();
        //--------------------------------------------------------------------------
        cases.put("var=<Foo>: <var>", new HashSet<>());
        cases.get("var=<Foo>: <var>").add("Foo: Foo");
        //--------------------------------------------------------------------------
        cases.put("var=<Foo> var2=<Bar> <var><var2><var>", new HashSet<>());
        cases.get("var=<Foo> var2=<Bar> <var><var2><var>").add("Foo Bar FooBarFoo");
        //--------------------------------------------------------------------------
        cases.put("var=<Foo> var2=<Bar> {<var>|<var2>}", new HashSet<>());
        cases.get("var=<Foo> var2=<Bar> {<var>|<var2>}").add("Foo Bar Foo");
        cases.get("var=<Foo> var2=<Bar> {<var>|<var2>}").add("Foo Bar Bar");
        //--------------------------------------------------------------------------
        cases.put("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>", new HashSet<>());
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("1 Foo 1 11");
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("1 Foo Foo FooFoo");
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("2 Foo 2 22");
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("2 Foo Foo FooFoo");
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("3 Foo 3 33");
        cases.get("var=<[1..3]> var2=<Foo> var3=<{<var>|<var2>}> <var3><var3>").add("3 Foo Foo FooFoo");

        Parser parser = new Parser("");
        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            String s = parser.createEquation(random);
            // System.out.println(s);
            assertTrue(testcase.getValue().contains(s));
        }
    }

    @Test
    public void createEquation_INNER_VARIABLES() {

    }

    @Test
    public void createEquation_EVERYTHING() {

    }
}