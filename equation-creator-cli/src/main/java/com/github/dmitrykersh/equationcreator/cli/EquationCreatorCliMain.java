package com.github.dmitrykersh.equationcreator.cli;

import com.github.dmitrykersh.equationcreator.api.parser.Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;

public final class EquationCreatorCliMain {
    private static final String FORMAT_HINT = """
        Formatting:
        \tVariable definition: var_name=<value>
        \tVariable usage: <var_name>
        \tIf your variable <sign> is [+ - * /] and you need opposite sign: <!sign>
        \t--------------------------------------------------------
        \tTo perform in-line math operations as [+ - * / ^]
        \tuse $ prefix and with spaces: 10 $+ 11 -> 21
        \t--------------------------------------------------------
        \tRandom integer between X and Y: [X..Y]
        \tRandom float between X and Y dividable by D: [X..Y|:D]
        \tRandom float between X and Y wits step S: [X..Y|S]
        \tRandom item from list: {string1|char2|[1..10]}
        """;

    private static final String TASKNUM_HINT = """
        Enter number of tasks:""";

    public static final String OUTPUTFORMAT_ISSUE_HINT = """
        WARNING! When using -outputformat you should escape Backslash as \\\\\\\\ (4 times), not \\\\ (twice)
        """;
    private static final Pattern TASK_PLACEHOLDER = Pattern.compile("\\{TASK}");
    private static final Pattern NUM_PLACEHOLDER = Pattern.compile("\\{NUM}");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Options options = new Options();

        Option help = Option.builder("help").build();
        Option out = Option.builder("o").longOpt("out").argName("path").hasArg().build();
        Option outputFormat = Option.builder("outputformat").argName("fmt").hasArg().build();

        options.addOption(help);
        options.addOption(out);
        options.addOption(outputFormat);

        CommandLineParser cmdParser = new DefaultParser();

        // default output path
        String fileOutPath = "output.txt";

        String outputFmt = null;

        try {
            CommandLine line = cmdParser.parse(options, args);
            if (line.hasOption("help")) {
                System.out.println(FORMAT_HINT);
            }
            if (line.hasOption("out")) {
                fileOutPath = line.getOptionValue("out");
            }
            if (line.hasOption("outputformat")) {
                System.out.println(OUTPUTFORMAT_ISSUE_HINT);
                outputFmt = line.getOptionValue("outputformat");
            }
            System.out.println("Enter format string:");

        }
        catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }

        String format = scanner.nextLine();

        System.out.println(TASKNUM_HINT);
        int numberOfTasks = scanner.nextInt();

        Random random = new Random();

        try (FileOutputStream fileOut = new FileOutputStream(fileOutPath)) {
            fileOut.write(("FORMAT:\n" + format + "\n\n").getBytes(StandardCharsets.UTF_8));

            if (outputFmt != null) {
                fileOut.write(("OUTPUT FORMAT:\n-outputformat \"" + outputFmt +"\"\n\nTASKS:\n").getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < numberOfTasks; i++) {
                    String output = outputFmt;

                    Matcher taskMatcher = TASK_PLACEHOLDER.matcher(output);
                    while (taskMatcher.find()) {
                        output = taskMatcher.replaceFirst(Parser.parseWithEscaping(random, format));
                        taskMatcher.reset(output);
                    }

                    Matcher numMatcher = NUM_PLACEHOLDER.matcher(output);
                    while (numMatcher.find()) {
                        output = numMatcher.replaceFirst(Integer.toString(i+1));
                        numMatcher.reset(output);
                    }

                    fileOut.write((output + "\n").getBytes(StandardCharsets.UTF_8));
                }
            } else {
                fileOut.write("TASKS:\n".getBytes(StandardCharsets.UTF_8));
                for (int i = 0; i < numberOfTasks; i++) {
                    fileOut.write((Parser.parseWithEscaping(random, format) + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
