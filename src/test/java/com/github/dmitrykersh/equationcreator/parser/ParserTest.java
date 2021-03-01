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
        assertEquals(parser.evaluateSimpleEquation("2$+3"), "5");
        assertEquals(parser.evaluateSimpleEquation("12$-6"), "6");
        assertEquals(parser.evaluateSimpleEquation("2$^3"), "8");
        assertEquals(parser.evaluateSimpleEquation("12$-6$*3"), "-6");
        assertEquals(parser.evaluateSimpleEquation("12$/6$*3"), "6");
    }

    @Test
    public void evaluateSimpleEquation_WRONG_INPUT() {
        Parser parser = new Parser("abc");

        // invalid input:
        assertEquals(parser.evaluateSimpleEquation("2+3"), "2+3");
        assertEquals(parser.evaluateSimpleEquation("12-6as"), "12-6as");
        assertEquals(parser.evaluateSimpleEquation("2^$3"), "2^$3");
    }

    @Test
    public void evaluateSimpleEquation_MIXED_INPUT() {
        Parser parser = new Parser("abc");

        // mixed $-operations, arithmetic signs and other shit
        // only $-operations should be touched
        assertEquals(parser.evaluateSimpleEquation("13$-6*3"), "7*3");
        assertEquals(parser.evaluateSimpleEquation("12a$+12-1"), "12a$+12-1");
        assertEquals(parser.evaluateSimpleEquation("12d$+4$-1"), "12d$+3");
    }

    @Test
    public void evaluateSimpleEquation_LONG_EQUATIONS() {
        Parser parser = new Parser("abc");

        // 2^5 - 10*2 + 2 + 3^2 * 2 (= 32)
        assertEquals(parser.evaluateSimpleEquation("2$^5$-10$*2$+2$+3$^2$*2"), "32");

        // 13 - 12 + 11*10 - 22*5 + 99 + 2^2^2^2 (= 356)
        assertEquals(parser.evaluateSimpleEquation("13$-12$+11$*10$-22$*5$+99$+2$^2$^2$^2"), "356");
    }
}