package com.github.dmitrykersh.equationcreator.parser;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String format;

    private static final Pattern RANGE_PATTERN = Pattern.compile("\\[\\d+\\.\\.\\d+]");
    private static final String RANGE_DELIM = "\\.\\.";

    private static final Pattern LIST_PATTERN = Pattern.compile("\\{([^|{}]+?\\|?)+?}");
    private static final String LIST_DELIM = "\\|";

    public Parser(final @NotNull String format) {
        this.format = format;
    }

    public void setFormat(final @NotNull String format){
        this.format = format;
    }

    /*
    Format:
        random integer from a to b: [a..b]
        random item from list: {+|abc|*|:|qwerty|asd}
     */

    public String createEquation(Random random) {
        String equation = format;
        Matcher range_matcher = RANGE_PATTERN.matcher(equation);

        // process ranges
        while (range_matcher.find()) {
            int start = range_matcher.start();
            int end = range_matcher.end();

            String range = equation.substring(start + 1, end - 1); // cutting [ and ]
            String[] values = range.split(RANGE_DELIM);

            int randomInt = Integer.parseInt(values[0]) +
                    random.nextInt(Integer.parseInt(values[1]) - Integer.parseInt(values[0]));

            equation = range_matcher.replaceFirst(String.valueOf(randomInt));
            range_matcher.reset(equation);
        }

        // process lists
        Matcher list_matcher = LIST_PATTERN.matcher(equation);
        while (list_matcher.find()) {
            int start = list_matcher.start();
            int end = list_matcher.end();

            String list = equation.substring(start + 1, end - 1); // cutting [ and ]
            String[] values = list.split(LIST_DELIM);
            int randomIndex = random.nextInt(values.length);

            equation = list_matcher.replaceFirst(values[randomIndex]);
            list_matcher.reset(equation);
        }
        return equation;
    }
}
