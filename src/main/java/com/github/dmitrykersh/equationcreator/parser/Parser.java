package com.github.dmitrykersh.equationcreator.parser;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
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

    // VARIABLES
    // define: a=<[1..10]> -- a is random number in 1-9
    // use: <a> -- this will be replaced with value of a
    private static final Pattern VAR_DEFINITION_PATTERN = Pattern.compile("[a-zA-Z]+=<[^<>]+>");
    private static final Pattern VAR_USAGE_PATTERN = Pattern.compile("<[a-zA-Z]+>");

    private static final String VAR_NOT_DEFINED_WARN = "UNDEFINED VARIABLE: ";

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

        // parse variable definition
        Map<String, String> variables = new HashMap<>();
        Matcher var_def_matcher = VAR_DEFINITION_PATTERN.matcher(equation);

        while (var_def_matcher.find()){
            int start = var_def_matcher.start();
            int end = var_def_matcher.end();

            String range = equation.substring(start, end);

            // words[0] - variable name, words[1] - value
            String[] words = range.split("=");

            // cutting braces <>
            words[1] = words[1].substring(1, words[1].length() - 1);

            Parser valueParser = new Parser(words[1]);
            String value = valueParser.createEquation(random);

            variables.put(words[0], value);

            equation = var_def_matcher.replaceFirst(value);
            var_def_matcher.reset(equation);
        }

        // parse variable usage
        Matcher var_use_matcher = VAR_USAGE_PATTERN.matcher(equation);

        while (var_use_matcher.find()){
            int start = var_use_matcher.start();
            int end = var_use_matcher.end();

            String var_name = equation.substring(start + 1, end - 1);
            String replacement = "";

            if (variables.containsKey(var_name)){
                replacement = variables.get(var_name);
            } else {
                replacement = VAR_NOT_DEFINED_WARN.concat(var_name);
            }

            equation = var_use_matcher.replaceFirst(replacement);
            var_use_matcher.reset(equation);
        }
        Matcher float_range_matcher = FLOAT_RANGE_PATTERN.matcher(equation);
        Matcher int_range_matcher = INT_RANGE_PATTERN.matcher(equation);
        Matcher list_matcher = LIST_PATTERN.matcher(equation);

        boolean anyPatternFound = true;

        while (anyPatternFound){
            anyPatternFound = false;

            float_range_matcher.reset(equation);
            int_range_matcher.reset(equation);
            list_matcher.reset(equation);

            // parse float ranges
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
                    int minQuotient = Math.floor(val0 / divisor) * divisor < val0 ? (int) Math.ceil(val0 / divisor)
                            : (int) Math.floor(val0 / divisor);
                    int maxQuotient = Math.round(val1 / divisor) * divisor > val1 ? (int) Math.round(val1 / divisor)
                            : (int) Math.round(val1 / divisor) + 1;

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
                anyPatternFound = true;
            }

            // parse integer ranges
            while (int_range_matcher.find()) {
                int start = int_range_matcher.start();
                int end = int_range_matcher.end();

                String range = equation.substring(start + 1, end - 1); // cutting [ and ]
                String[] values = range.split(RANGE_DELIM);

                int randomInt = Integer.parseInt(values[0]) +
                        random.nextInt(Integer.parseInt(values[1]) - Integer.parseInt(values[0]));

                equation = int_range_matcher.replaceFirst(String.valueOf(randomInt));
                int_range_matcher.reset(equation);
                anyPatternFound = true;
            }

            // parse lists
            while (list_matcher.find()) {
                int start = list_matcher.start();
                int end = list_matcher.end();

                String list = equation.substring(start + 1, end - 1); // cutting { and }
                String[] values = list.split(LIST_DELIM);
                int randomIndex = random.nextInt(values.length);

                equation = list_matcher.replaceFirst(values[randomIndex]);
                list_matcher.reset(equation);
                anyPatternFound = true;
            }
        }

        return equation;
    }
}
