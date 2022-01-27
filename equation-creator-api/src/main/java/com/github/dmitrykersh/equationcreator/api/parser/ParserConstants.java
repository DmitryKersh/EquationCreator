package com.github.dmitrykersh.equationcreator.api.parser;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.regex.Pattern;

import static com.github.dmitrykersh.equationcreator.api.parser.ParserConstants.ParserRegexStrings.DECIMAL;

@UtilityClass
public class ParserConstants {
    @UtilityClass
    public static class ParserRegexStrings {
        public static final String RANGE_DELIM = "\\.\\.";
        public static final String DIGIT = "[\\d\\-]";
        public static final String FLOAT_RANGE_STEP_DELIM = "\\|";
        public static final String DECIMAL = "-?\\d+(\\.\\d+)?";
        public static final String ARITH_SIGN_PREFIX = "\\$";
    }

    @UtilityClass
    public static class ParserRegexPatterns {
        public static final Pattern FLOAT_RANGE_PATTERN = Pattern.compile("\\[" + DECIMAL + "\\.\\." + DECIMAL + "\\|:?" + DECIMAL + "]");


        // [12..23]
        public static final Pattern INT_RANGE_PATTERN = Pattern.compile("\\[-?\\d+\\.\\.-?\\d+]");

        // {a|bc|def|{x|y|z}}
        public static final Pattern LIST_PATTERN = Pattern.compile("\\{([^|{}]+?\\|?)+?}");
        public static final String LIST_DELIM = "\\|";

        // VARIABLES
        // define: a=<[1..10]> -- a is random number in 1-9
        // use: <a> -- this will be replaced with value of a
        public static final Pattern VAR_DEFINITION_PATTERN = Pattern.compile("[a-zA-Z]+[_0-9]?=<[^<>]+>");
        public static final Pattern VAR_USAGE_PATTERN = Pattern.compile("<!?[a-zA-Z]+[_0-9]?>");

        // arithmetics
        // to use arithmetic signs for their purpose, use $
        // 12$+23 will be replaced with 35
        public static final Pattern ARITH_BRACKETS_PATTERN = Pattern.compile(
            "\\(" + DECIMAL + "(\\s\\$[+\\-/*\\^]\\s" + DECIMAL + ")+\\)");
        public static final Pattern ARITH_NO_BRACKETS_PATTERN = Pattern.compile(
            DECIMAL + "(\\s\\$[+\\-/*\\^]\\s" + DECIMAL + ")+");

        public static final Pattern ARITH_PRIOR_1 = Pattern.compile(DECIMAL + "\\s\\$\\^\\s" + DECIMAL);
        public static final Pattern ARITH_PRIOR_2 = Pattern.compile(DECIMAL + "\\s\\$[*/]\\s" + DECIMAL);
        public static final Pattern ARITH_PRIOR_3 = Pattern.compile(DECIMAL + "\\s\\$[+-]\\s" + DECIMAL);

        public static final Map<Pattern, String> ESCAPE_SYMBOLS_ENCODE = Map.of(
            Pattern.compile("\\\\\\{"), "\\\\OCB",
            Pattern.compile("\\\\}"), "\\\\CCB",
            Pattern.compile("\\\\\\["), "\\\\OSB",
            Pattern.compile("\\\\]"), "\\\\CSB",
            Pattern.compile("\\\\\\("), "\\\\ORB",
            Pattern.compile("\\\\\\)"), "\\\\CRB",
            Pattern.compile("\\\\<"), "\\\\OTB",
            Pattern.compile("\\\\>"), "\\\\CTB",
            Pattern.compile("\\\\\\\\"), "\\\\BSH",
            Pattern.compile("\\\\\\|"), "\\\\PIP"
        );

        public static final Map<Pattern, String> ESCAPE_SYMBOLS_DECODE = Map.of(
            Pattern.compile("\\\\OCB"), "{",
            Pattern.compile("\\\\CCB"), "}",
            Pattern.compile("\\\\OSB"), "[",
            Pattern.compile("\\\\CSB"), "]",
            Pattern.compile("\\\\ORB"), "(",
            Pattern.compile("\\\\CRB"), ")",
            Pattern.compile("\\\\OTB"), "<",
            Pattern.compile("\\\\CTB"), ">",
            Pattern.compile("\\\\BSH"), "\\\\",
            Pattern.compile("\\\\PIP"), "|"
        );
    }

    @UtilityClass
    public static class ParserConstStrings {
        public static final String VAR_NOT_DEFINED_WARN = "UNDEFINED VARIABLE: ";
    }
}
