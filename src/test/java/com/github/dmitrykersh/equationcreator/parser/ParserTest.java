package com.github.dmitrykersh.equationcreator.parser;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParserTest {
    private static Random random = new Random();

    @Test
    public void negateValue_ALL_IN_ONE() {
        Map<String, String> cases = new HashMap<>();

        cases.put("+", "-");
        cases.put("-", "+");
        cases.put("*", "/");
        cases.put("/", "*");
        cases.put("^", "^");
        cases.put("+-=", "+-=");

        for (Map.Entry<String, String> testcase : cases.entrySet())
            assertEquals(testcase.getKey(), Parser.negateValue(testcase.getValue()));
    }

    @Test
    public void evaluateSimpleEquation_VALID_INPUT() {
        Map<String, String> cases = new HashMap<>();

        cases.put("5", "2 $+ 3");
        cases.put("6", "12 $- 6");
        cases.put("8", "2 $^ 3");
        cases.put("-6", "12 $- 6 $* 3");
        cases.put("-1", "12 $/ 6 $- 3");

        for (Map.Entry<String, String> testcase : cases.entrySet())
            assertEquals(testcase.getKey(), Parser.evaluateSimpleEquation(testcase.getValue()));
    }

    @Test
    public void evaluateSimpleEquation_WRONG_INPUT() {
        Map<String, String> cases = new HashMap<>();

        cases.put("2 + 3", "2 + 3");
        cases.put("12 - 6as", "12 - 6as");
        cases.put("2 ^$ 3", "2 ^$ 3");
        cases.put("2$^3", "2$^3");

        for (Map.Entry<String, String> testcase : cases.entrySet())
            assertEquals(testcase.getKey(), Parser.evaluateSimpleEquation(testcase.getValue()));

    }

    @Test
    public void evaluateSimpleEquation_MIXED_INPUT() {
        // mixed $-operations, arithmetic signs and other shit
        // only $-operations should be touched

        Map<String, String> cases = new HashMap<>();

        cases.put("7 * 3", "13 $- 6 * 3");
        cases.put("12a $+ 12 - 1", "12a $+ 12 - 1");
        cases.put("12d $+ 3", "12d $+ 4 $- 1");

        for (Map.Entry<String, String> testcase : cases.entrySet())
            assertEquals(testcase.getKey(), Parser.evaluateSimpleEquation(testcase.getValue()));
    }

    @Test
    public void evaluateSimpleEquation_LONG_EQUATIONS() {
        Map<String, String> cases = new HashMap<>();

        // 2^5 - 10*2 + 2 + 3^2 * 2 (= 32)
        // 13 - 12 + 11*10 - 22*5 + 99 + 2^2^2^2 (= 356)
        cases.put("32","2 $^ 5 $- 10 $* 2 $+ 2 $+ 3 $^ 2 $* 2");
        cases.put("356", "13 $- 12 $+ 11 $* 10 $- 22 $* 5 $+ 99 $+ 2 $^ 2 $^ 2 $^ 2");

        for (Map.Entry<String, String> testcase : cases.entrySet())
            assertEquals(testcase.getKey(), Parser.evaluateSimpleEquation(testcase.getValue()));
    }

    @Test
    public void createEquation_NO_PARSING() {
        Parser parser = new Parser("");
        List<String> cases = new LinkedList<>();

        cases.add("Foo Bar Baz");
        cases.add("Sample Text...");
        cases.add("Not a range: [1..a]");
        cases.add("Not a float range: [1..2|s]");
        cases.add("31$-31");
        cases.add("qwe rty uiop");
        cases.add("Not a range: [1...4]");
        cases.add("<thisIsUndefinedVariable>");

        for (String testcase : cases) {
            parser.setFormat(testcase);
            assertEquals(testcase, parser.createEquation(random));
        }
    }

    @RepeatedTest(3)
    public void createEquation_INT_RANGE() {
        Parser parser = new Parser("");
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

        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_FLOAT_RANGE_STEP() {
        Parser parser = new Parser("");
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

        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @RepeatedTest(3)
    public void createEquation_FLOAT_RANGE_DIVISOR() {
        Parser parser = new Parser("");
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

        for (Map.Entry<String, Set<String>> testcase : cases.entrySet()) {
            parser.setFormat(testcase.getKey());
            assertTrue(testcase.getValue().contains(parser.createEquation(random)));
        }
    }

    @Test
    public void createEquation_LIST() {

    }

    @Test
    public void createEquation_INNER_ELEMENTS() {

    }

    @Test
    public void createEquation_VARIABLES() {

    }

    @Test
    public void createEquation_INNER_VARIABLES() {

    }

    @Test
    public void createEquation_EVERYTHING() {

    }
}