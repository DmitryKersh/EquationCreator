package com.github.dmitrykersh.equationcreator.parser;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

public class Parser {
    private String format;


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
        Matcher varUseMatcher = ParserConstants.ParserRegexPatterns.VAR_USAGE_PATTERN.matcher(equation);
        Matcher varDefMatcher = ParserConstants.ParserRegexPatterns.VAR_DEFINITION_PATTERN.matcher(equation);

        boolean anyVariableFound = true;

        while (anyVariableFound){
            anyVariableFound = false;

            varDefMatcher.reset(equation);
            while (varDefMatcher.find()){
                int start = varDefMatcher.start();
                int end = varDefMatcher.end();

                String range = equation.substring(start, end);

                // words[0] - variable name, words[1] - value
                String[] words = range.split("=");

                // cutting braces <>
                words[1] = words[1].substring(1, words[1].length() - 1);

                Parser valueParser = new Parser(words[1]);
                String value = valueParser.createEquation(random);

                variables.put(words[0], value);

                equation = varDefMatcher.replaceFirst(value);
                varDefMatcher.reset(equation);
                anyVariableFound = true;
            }

            // parse variable usage
            varUseMatcher.reset(equation);
            while (varUseMatcher.find()) {
                int start = varUseMatcher.start();
                int end = varUseMatcher.end();

                boolean isNegative = false;

                String var_name = equation.substring(start + 1, end - 1);

                if (var_name.startsWith("!")) {
                    var_name = var_name.substring(1);
                    isNegative = true;
                }

                if (variables.containsKey(var_name)) {
                    String var_value = variables.get(var_name);
                    equation = varUseMatcher.replaceFirst(isNegative ? negateValue(var_value) : var_value);
                    anyVariableFound = true;
                } else break;

                varUseMatcher.reset(equation);
            }
        }

        Matcher floatRangeMatcher = ParserConstants.ParserRegexPatterns.FLOAT_RANGE_PATTERN.matcher(equation);
        Matcher intRangeMatcher = ParserConstants.ParserRegexPatterns.INT_RANGE_PATTERN.matcher(equation);
        Matcher listMatcher = ParserConstants.ParserRegexPatterns.LIST_PATTERN.matcher(equation);
        Matcher arithBracketsMatcher = ParserConstants.ParserRegexPatterns.ARITH_BRACKETS_PATTERN.matcher(equation);
        Matcher arithNoBracketsMatcher = ParserConstants.ParserRegexPatterns.ARITH_NO_BRACKETS_PATTERN.matcher(equation);

        boolean anyPatternFound = true;

        while (anyPatternFound){
            anyPatternFound = false;

            // parse float ranges
            floatRangeMatcher.reset(equation);
            while (floatRangeMatcher.find()) {
                int start = floatRangeMatcher.start();
                int end = floatRangeMatcher.end();

                String range = equation.substring(start + 1, end - 1); // cutting [ and ]
                String[] stepSplit = range.split(ParserConstants.ParserRegexStrings.FLOAT_RANGE_STEP_DELIM);
                String[] values = stepSplit[0].split(ParserConstants.ParserRegexStrings.RANGE_DELIM);

                boolean stepIsDivisor = stepSplit[1].startsWith(":");

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
                    stepSplit[1] = stepSplit[1].substring(1);
                    double divisor = Double.parseDouble(stepSplit[1]);
                    int minQuotient = Math.floor(val0 / divisor) * divisor < val0 ? (int) Math.ceil(val0 / divisor)
                            : (int) Math.floor(val0 / divisor);
                    int maxQuotient = Math.round(val1 / divisor) * divisor > val1 ? (int) Math.round(val1 / divisor)
                            : (int) Math.round(val1 / divisor) + 1;

                    int quotient = minQuotient + random.nextInt(Math.max(maxQuotient - minQuotient, 1));

                    randomInRange = quotient * divisor;

                    // if there's no divisor in range, i.e. [1..4|:5], it'll be parsed to lower bound: 1
                    if (randomInRange > val1 || randomInRange < val0) randomInRange = val0;

                } else {
                    double step = Double.parseDouble(stepSplit[1]);
                    int bound = (int) (1 + (val1 - val0) / step);
                    int stepCount = random.nextInt(bound);
                    randomInRange = val0 + stepCount * step;
                }

                String decimalFormatPattern = String.valueOf(stepSplit[1]).replaceAll(ParserConstants.ParserRegexStrings.DIGIT, "#");

                DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

                equation = floatRangeMatcher.replaceFirst(decimalFormat.format(randomInRange));
                floatRangeMatcher.reset(equation);
                anyPatternFound = true;
            }
            // if (anyPatternFound) continue;

            // parse integer ranges
            intRangeMatcher.reset(equation);
            while (intRangeMatcher.find()) {
                int start = intRangeMatcher.start();
                int end = intRangeMatcher.end();

                String range = equation.substring(start + 1, end - 1); // cutting [ and ]
                String[] values = range.split(ParserConstants.ParserRegexStrings.RANGE_DELIM);

                int val0 = Integer.parseInt(values[0]);
                int val1 = Integer.parseInt(values[1]);

                // sort numbers in ascending order to avoid user's mistakes
                if (val1 < val0){
                    int tmp = val0;
                    val0 = val1;
                    val1 = tmp;
                }

                int randomInt = val0 + random.nextInt(val1 - val0 + 1);

                equation = intRangeMatcher.replaceFirst(String.valueOf(randomInt));
                intRangeMatcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            // compute arithmetics
            arithBracketsMatcher.reset(equation);
            while (arithBracketsMatcher.find()){
                int start = arithBracketsMatcher.start();
                int end = arithBracketsMatcher.end();

                String bracket_part = equation.substring(start + 1, end - 1);

                equation = arithBracketsMatcher.replaceFirst(evaluateSimpleEquation(bracket_part));
                arithBracketsMatcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            arithNoBracketsMatcher.reset(equation);
            while (arithNoBracketsMatcher.find()){
                int start = arithNoBracketsMatcher.start();
                int end = arithNoBracketsMatcher.end();

                String equation_part = equation.substring(start, end);

                equation = arithNoBracketsMatcher.replaceFirst(evaluateSimpleEquation(equation_part));
                arithNoBracketsMatcher.reset(equation);
                anyPatternFound = true;
            }
            if (anyPatternFound) continue;

            // parse lists
            listMatcher.reset(equation);
            while (listMatcher.find()) {
                int start = listMatcher.start();
                int end = listMatcher.end();

                String list = equation.substring(start + 1, end - 1); // cutting { and }
                String[] values = list.split(ParserConstants.ParserRegexPatterns.LIST_DELIM);
                int randomIndex = random.nextInt(values.length);

                equation = listMatcher.replaceFirst(values[randomIndex]);
                listMatcher.reset(equation);
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
        Matcher prior1Matcher = ParserConstants.ParserRegexPatterns.ARITH_PRIOR_1.matcher(s);

        while (prior1Matcher.find()){
            int start = prior1Matcher.start();
            int end = prior1Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ParserConstants.ParserRegexStrings.ARITH_SIGN_PREFIX);

            double op1 = Double.parseDouble(values[0]);
            double op2 = Double.parseDouble(values[1].substring(1));

            Double res = Math.pow(op1, op2);

            String decimalFormatPattern = String.valueOf(res).replaceAll(ParserConstants.ParserRegexStrings.DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior1Matcher.replaceFirst(decimalFormat.format(res));
            prior1Matcher.reset(s);
        }

        Matcher prior2Matcher = ParserConstants.ParserRegexPatterns.ARITH_PRIOR_2.matcher(s);

        while (prior2Matcher.find()){
            int start = prior2Matcher.start();
            int end = prior2Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ParserConstants.ParserRegexStrings.ARITH_SIGN_PREFIX);


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
            String decimalFormatPattern = String.valueOf(res).replaceAll(ParserConstants.ParserRegexStrings.DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior2Matcher.replaceFirst(decimalFormat.format(res));
            prior2Matcher.reset(s);
        }

        Matcher prior3Matcher = ParserConstants.ParserRegexPatterns.ARITH_PRIOR_3.matcher(s);

        while (prior3Matcher.find()){
            int start = prior3Matcher.start();
            int end = prior3Matcher.end();

            String operation = s.substring(start, end);
            String[] values = operation.split(ParserConstants.ParserRegexStrings.ARITH_SIGN_PREFIX);


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
            String decimalFormatPattern = String.valueOf(res).replaceAll(ParserConstants.ParserRegexStrings.DIGIT, "#");
            DecimalFormat decimalFormat = new DecimalFormat(decimalFormatPattern);

            s = prior3Matcher.replaceFirst(decimalFormat.format(res));
            prior3Matcher.reset(s);
        }

        return s;
    }
}
