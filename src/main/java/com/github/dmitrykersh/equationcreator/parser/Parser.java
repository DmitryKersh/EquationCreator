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
    private static final String DIGIT = "[\\d\\-]";

    private static final String DECIMAL = "-?\\d+(\\.\\d+)?";

    // [1.1..2.55|0.2] = 1.1, 1.3, ... , 2.5 (step 0.2)
    // [1.1..2.55|:0.2] = 1.2, 1.4, ... , 2.4 (dividable by 0.2)
    private static final Pattern FLOAT_RANGE_PATTERN =
            Pattern.compile("\\[" + DECIMAL + "\\.\\." + DECIMAL +"\\|:?" + DECIMAL + "]");
    private static final String FLOAT_RANGE_STEP_DELIM = "\\|";

    // [12..23]
    private static final Pattern INT_RANGE_PATTERN = Pattern.compile("\\[-?\\d+\\.\\.-?\\d+]");

    // {a|bc|def|{x|y|z}}
    private static final Pattern LIST_PATTERN = Pattern.compile("\\{([^|{}]+?\\|?)+?}");
    private static final String LIST_DELIM = "\\|";

    // VARIABLES
    // define: a=<[1..10]> -- a is random number in 1-9
    // use: <a> -- this will be replaced with value of a
    private static final Pattern VAR_DEFINITION_PATTERN = Pattern.compile("[a-zA-Z]+[_0-9]?=<[^<>]+>");
    private static final Pattern VAR_USAGE_PATTERN = Pattern.compile("<!?[a-zA-Z]+[_0-9]?>");

    // arithmetics
    // to use arithmetic signs for their purpose, use $
    // 12$+23 will be replaced with 35
    private static final Pattern ARITH_BRACKETS_PATTERN = Pattern
            .compile("\\(" + DECIMAL + "(\\s\\$[+\\-/*\\^]\\s" + DECIMAL + ")+\\)");
    private static final Pattern ARITH_NO_BRACKETS_PATTERN = Pattern
            .compile(DECIMAL + "(\\s\\$[+\\-/*\\^]\\s" + DECIMAL + ")+");

    private static final Pattern ARITH_PRIOR_1 = Pattern.compile(DECIMAL + "\\s\\$\\^\\s" + DECIMAL);
    private static final Pattern ARITH_PRIOR_2 = Pattern.compile(DECIMAL + "\\s\\$[*/]\\s" + DECIMAL);
    private static final Pattern ARITH_PRIOR_3 = Pattern.compile(DECIMAL + "\\s\\$[+-]\\s" + DECIMAL);

    private static final String ARITH_SIGN_PREFIX = "\\$";

    private static final String VAR_NOT_DEFINED_WARN = "UNDEFINED VARIABLE: ";

    public Parser(final @NotNull String format) {
        this.format = format;
    }

    public void setFormat(final @NotNull String format) {
        this.format = format;
    }

    public String createEquation(Random random) {
        String equation = format;

        // parse variable definition
        Map<String, String> variables = new HashMap<>();
        Matcher var_use_matcher = VAR_USAGE_PATTERN.matcher(equation);
        Matcher var_def_matcher = VAR_DEFINITION_PATTERN.matcher(equation);

        boolean anyVariableFound = true;

        while (anyVariableFound){
            anyVariableFound = false;

            var_def_matcher.reset(equation);
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
                anyVariableFound = true;
            }

            // parse variable usage
            var_use_matcher.reset(equation);
            while (var_use_matcher.find()) {
                int start = var_use_matcher.start();
                int end = var_use_matcher.end();

                boolean isNegative = false;

                String var_name = equation.substring(start + 1, end - 1);

                if (var_name.startsWith("!")) {
                    var_name = var_name.substring(1);
                    isNegative = true;
                }

                if (variables.containsKey(var_name)) {
                    String var_value = variables.get(var_name);
                    equation = var_use_matcher.replaceFirst(isNegative ? negateValue(var_value) : var_value);
                    anyVariableFound = true;
                } else break;

                var_use_matcher.reset(equation);
            }
        }

        Matcher float_range_matcher = FLOAT_RANGE_PATTERN.matcher(equation);
        Matcher int_range_matcher = INT_RANGE_PATTERN.matcher(equation);
        Matcher list_matcher = LIST_PATTERN.matcher(equation);
        Matcher arith_brackets_matcher = ARITH_BRACKETS_PATTERN.matcher(equation);
        Matcher arith_no_brackets_matcher = ARITH_NO_BRACKETS_PATTERN.matcher(equation);

        boolean anyPatternFound = true;

        while (anyPatternFound){
            anyPatternFound = false;

            // parse float ranges
            float_range_matcher.reset(equation);
            while (float_range_matcher.find()) {
                int start = float_range_matcher.start();
                int end = float_range_matcher.end();

                String range = equation.substring(start + 1, end - 1); // cutting [ and ]
                String[] step_split = range.split(FLOAT_RANGE_STEP_DELIM);
                String[] values = step_split[0].split(RANGE_DELIM);

                boolean stepIsDivisor = step_split[1].startsWith(":");

                double val0 = Double.parseDouble(values[0]);
                double val1 = Double.parseDouble(values[1]);

                // sort numbers in ascending order to avoid user's mistakes
                if (val1 < val0){
                    double tmp = val0;
                    val0 = val1;
                    val1 = tmp;
                }

                Double randomInRange;
                if (stepIsDivisor) {
                    // delete ':'
                    step_split[1] = step_split[1].substring(1);
                    double divisor = Double.parseDouble(step_split[1]);
                    int minQuotient = Math.floor(val0 / divisor) * divisor < val0 ? (int) Math.ceil(val0 / divisor)
                            : (int) Math.floor(val0 / divisor);
                    int maxQuotient = Math.round(val1 / divisor) * divisor > val1 ? (int) Math.round(val1 / divisor)
                            : (int) Math.round(val1 / divisor) + 1;

                    int quotient = minQuotient + random.nextInt(Math.max(maxQuotient - minQuotient, 1));

                    randomInRange = quotient * divisor;

                    // if there's no divisor in range, i.e. [1..4|:5], it'll be parsed to lower bound: 1
                    if (randomInRange > val1 || randomInRange < val0) randomInRange = val0;

                } else {
                    double step = Double.parseDouble(step_split[1]);
                    int bound = (int) (1 + (val1 - val0) / step);
                    int stepCount = random.nextInt(bound);
                    randomInRange = val0 + stepCount * step;
                }

                String decimalFormatPattern = String.valueOf(step_split[1]).replaceAll(DIGIT, "#");

                DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

                equation = float_range_matcher.replaceFirst(decimalFormat.format(randomInRange));
                float_range_matcher.reset(equation);
                anyPatternFound = true;
            }
            // if (anyPatternFound) continue;

            // parse integer ranges
            int_range_matcher.reset(equation);
            while (int_range_matcher.find()) {
                int start = int_range_matcher.start();
                int end = int_range_matcher.end();

                String range = equation.substring(start + 1, end - 1); // cutting [ and ]
                String[] values = range.split(RANGE_DELIM);

                int val0 = Integer.parseInt(values[0]);
                int val1 = Integer.parseInt(values[1]);

                // sort numbers in ascending order to avoid user's mistakes
                if (val1 < val0){
                    int tmp = val0;
                    val0 = val1;
                    val1 = tmp;
                }

                int randomInt = val0 + random.nextInt(val1 - val0 + 1);

                equation = int_range_matcher.replaceFirst(String.valueOf(randomInt));
                int_range_matcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            // compute arithmetics
            arith_brackets_matcher.reset(equation);
            while (arith_brackets_matcher.find()){
                int start = arith_brackets_matcher.start();
                int end = arith_brackets_matcher.end();

                String bracket_part = equation.substring(start + 1, end - 1);

                equation = arith_brackets_matcher.replaceFirst(evaluateSimpleEquation(bracket_part));
                arith_brackets_matcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            arith_no_brackets_matcher.reset(equation);
            while (arith_no_brackets_matcher.find()){
                int start = arith_no_brackets_matcher.start();
                int end = arith_no_brackets_matcher.end();

                String equation_part = equation.substring(start, end);

                equation = arith_no_brackets_matcher.replaceFirst(evaluateSimpleEquation(equation_part));
                arith_no_brackets_matcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            // parse lists
            list_matcher.reset(equation);
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

    static String negateValue(final @NotNull String value){
        switch (value){
            case "+": return "-";
            case "-": return "+";
            case "/": return "*";
            case "*": return "/";
            default: return value;
        }
    }

    static String evaluateSimpleEquation(final @NotNull String str){
        String s = new String(str);
        Matcher prior1_matcher = ARITH_PRIOR_1.matcher(s);

        while (prior1_matcher.find()){
            int start = prior1_matcher.start();
            int end = prior1_matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);

            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1].substring(1));

            Double res = Math.pow(op1, op2);

            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior1_matcher.replaceFirst(decimalFormat.format(res));
            prior1_matcher.reset(s);
        }

        Matcher prior2_matcher = ARITH_PRIOR_2.matcher(s);

        while (prior2_matcher.find()){
            int start = prior2_matcher.start();
            int end = prior2_matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);


            char operator = values[1].charAt(0);
            values[1] = values[1].substring(1);
            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1]);

            Double res = 0.0;

            switch (operator){
                case '*':{
                    res = op1 * op2;
                    break;
                }
                case '/':{
                    res = op1 / op2;
                    break;
                }
            }
            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior2_matcher.replaceFirst(decimalFormat.format(res));
            prior2_matcher.reset(s);
        }

        Matcher prior3_matcher = ARITH_PRIOR_3.matcher(s);

        while (prior3_matcher.find()){
            int start = prior3_matcher.start();
            int end = prior3_matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ARITH_SIGN_PREFIX);


            char operator = values[1].charAt(0);
            values[1] = values[1].substring(1);
            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1]);

            Double res = 0.0;

            switch (operator){
                case '+':{
                    res = op1 + op2;
                    break;
                }
                case '-':{
                    res = op1 - op2;
                    break;
                }
            }
            String decimalFormatPattern = String.valueOf(res).replaceAll(DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior3_matcher.replaceFirst(decimalFormat.format(res));
            prior3_matcher.reset(s);
        }

        return s;
    }
}
