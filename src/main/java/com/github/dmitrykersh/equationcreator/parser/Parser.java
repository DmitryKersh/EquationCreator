package com.github.dmitrykersh.equationcreator.parser;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String format;

    private static final String RANGE_DELIM = "\\.\\.";

    // [1.1..2.55|0.2] = 1.1, 1.3, ... , 2.5 (step 0.2)
    // [1.1..2.55|:0.2] = 1.2, 1.4, ... , 2.4 (dividable by 0.2)
    private static final Pattern FLOAT_RANGE_PATTERN =
            Pattern.compile("\\[\\d+(\\.\\d+)?\\.\\.\\d+(\\.\\d+)?\\|:?\\d+(\\.\\d+)?]");
    private static final String FLOAT_RANGE_STEP_DELIM = "\\|";

    // [12..23]
    private static final Pattern INT_RANGE_PATTERN = Pattern.compile("\\[\\d+\\.\\.\\d+]");

    // {a|bc|def|{x|y|z}}
    private static final Pattern LIST_PATTERN = Pattern.compile("\\{([^|{}]+?\\|?)+?}");
    private static final String LIST_DELIM = "\\|";

    public Parser(final @NotNull String format) {
        this.format = format;
    }

    public void setFormat(final @NotNull String format) {
        this.format = format;
    }

    /*
    Format:
        random integer from a to b: [a..b]
        random item from list: {+|abc|*|:|qwerty|asd}
     */

    public String createEquation(Random random) {
        String equation = format;

        // parse float ranges
        Matcher float_range_matcher = FLOAT_RANGE_PATTERN.matcher(equation);
        while (float_range_matcher.find()) {
            int start = float_range_matcher.start();
            int end = float_range_matcher.end();

            String range = equation.substring(start + 1, end - 1); // cutting [ and ]
            String[] step_split = range.split(FLOAT_RANGE_STEP_DELIM);
            String[] values = step_split[0].split(RANGE_DELIM);

            boolean stepIsDivisor = step_split[1].startsWith(":");

            Double randomInRange;

            double val0 = Double.parseDouble(values[0]);
            double val1 = Double.parseDouble(values[1]);


            if (stepIsDivisor) {
                // delete ':'
                step_split[1] = step_split[1].substring(1);
                double divisor = Double.parseDouble(step_split[1]);
                int minQuotient = Math.floor(val0 / divisor) * divisor < val0 ? (int)Math.ceil(val0 / divisor) : (int)Math.floor(val0 / divisor);
                int maxQuotient = Math.round(val1 / divisor) * divisor > val1 ? (int)Math.round(val1 / divisor) : (int)Math.round(val1 / divisor) + 1;

                int quotient = minQuotient + random.nextInt(maxQuotient - minQuotient);

                randomInRange = quotient * divisor;
            } else {
                double step = Double.parseDouble(step_split[1]);
                int stepCount = random.nextInt(1 + (int) ((val1 - val0) / step));
                randomInRange = val0 + stepCount * step;
            }

            String decimalFormatPattern = String.valueOf(step_split[1]).replaceAll("\\d", "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            equation = float_range_matcher.replaceFirst(decimalFormat.format(randomInRange));
            float_range_matcher.reset(equation);
        }

        // parse integer ranges
        Matcher int_range_matcher = INT_RANGE_PATTERN.matcher(equation);
        while (int_range_matcher.find()) {
            int start = int_range_matcher.start();
            int end = int_range_matcher.end();

            String range = equation.substring(start + 1, end - 1); // cutting [ and ]
            String[] values = range.split(RANGE_DELIM);

            int randomInt = Integer.parseInt(values[0]) +
                    random.nextInt(Integer.parseInt(values[1]) - Integer.parseInt(values[0]));

            equation = int_range_matcher.replaceFirst(String.valueOf(randomInt));
            int_range_matcher.reset(equation);
        }

        // parse lists
        Matcher list_matcher = LIST_PATTERN.matcher(equation);
        while (list_matcher.find()) {
            int start = list_matcher.start();
            int end = list_matcher.end();

            String list = equation.substring(start + 1, end - 1); // cutting { and }
            String[] values = list.split(LIST_DELIM);
            int randomIndex = random.nextInt(values.length);

            equation = list_matcher.replaceFirst(values[randomIndex]);
            list_matcher.reset(equation);
        }
        return equation;
    }
}
