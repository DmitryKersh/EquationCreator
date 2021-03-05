package com.github.dmitrykersh.equationcreator.parser;

import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    public void negateValue_ALL_IN_ONE() {
        Parser parser = new Parser("abc");

        // these should change
        assertEquals(parser.negateValue("+"), "-");
        assertEquals(parser.negateValue("-"), "+");
        assertEquals(parser.negateValue("*"), "/");
        assertEquals(parser.negateValue("/"), "*");

        // these remain unchanged
        assertEquals(parser.negateValue("^"), "^");
        assertEquals(parser.negateValue("a+bc"), "a+bc");
    }

    @Test
    public void evaluateSimpleEquation_VALID_INPUT() {
        Parser parser = new Parser("abc");

        // only $-operations
        assertEquals("5", parser.evaluateSimpleEquation("2 $+ 3"));
        assertEquals("6", parser.evaluateSimpleEquation("12 $- 6"));
        assertEquals("8", parser.evaluateSimpleEquation("2 $^ 3"));
        assertEquals("-6", parser.evaluateSimpleEquation("12 $- 6 $* 3"));
        assertEquals("6", parser.evaluateSimpleEquation("12 $/ 6 $* 3"));
    }

    @Test
    public void evaluateSimpleEquation_WRONG_INPUT() {
        Parser parser = new Parser("abc");

        // invalid input:
        assertEquals("2 + 3", parser.evaluateSimpleEquation("2 + 3"));
        assertEquals("12 - 6as", parser.evaluateSimpleEquation("12 - 6as"));
        assertEquals("2 ^$ 3", parser.evaluateSimpleEquation("2 ^$ 3"));
        assertEquals("2$^3", parser.evaluateSimpleEquation("2$^3"));
    }

    @Test
    public void evaluateSimpleEquation_MIXED_INPUT() {
        Parser parser = new Parser("abc");

        // mixed $-operations, arithmetic signs and other shit
        // only $-operations should be touched
        assertEquals("7 * 3", parser.evaluateSimpleEquation("13 $- 6 * 3"));
        assertEquals("12a $+ 12 - 1", parser.evaluateSimpleEquation("12a $+ 12 - 1"));
        assertEquals("12d $+ 3", parser.evaluateSimpleEquation("12d $+ 4 $- 1"));
    }

    @Test
    public void evaluateSimpleEquation_LONG_EQUATIONS() {
        Parser parser = new Parser("abc");

        // 2^5 - 10*2 + 2 + 3^2 * 2 (= 32)
        assertEquals("32", parser.evaluateSimpleEquation("2 $^ 5 $- 10 $* 2 $+ 2 $+ 3 $^ 2 $* 2"));

        // 13 - 12 + 11*10 - 22*5 + 99 + 2^2^2^2 (= 356)
        assertEquals("356", parser.evaluateSimpleEquation("13 $- 12 $+ 11 $* 10 $- 22 $* 5 $+ 99 $+ 2 $^ 2 $^ 2 $^ 2"));
    }

    @Test
    public void createEquation_NO_PARSING() {
        Parser parser = new Parser("");

        parser.setFormat("");
    }

    @Test
    public void createEquation_INT_RANGE() {

    }

    @Test
    public void createEquation_FLOAT_RANGE_STEP() {

    }

    @Test
    public void createEquation_FLOAT_RANGE_DIVISOR() {

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