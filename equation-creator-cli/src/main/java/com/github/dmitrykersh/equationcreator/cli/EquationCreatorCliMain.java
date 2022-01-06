package com.github.dmitrykersh.equationcreator.cli;

import com.github.dmitrykersh.equationcreator.api.parser.Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public final class EquationCreatorCliMain {
    private static final String FORMAT_HINT = """
        Enter format string.
        Formatting:
        \tVariable definition: name=<value>
        \tVariable usage: <name>
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
        Enter number of tasks.""";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println(FORMAT_HINT);
        String format = scanner.nextLine();

        System.out.println(TASKNUM_HINT);
        int numberOfTasks = scanner.nextInt();

        Random random = new Random();
        Parser parser = new Parser(format);

        try (FileOutputStream fileOut = new FileOutputStream("output.txt")) {
            fileOut.write(("FORMAT:\n" + format + "\n\nTASKS:\n").getBytes(StandardCharsets.UTF_8));
            for (int i = 1; i <= numberOfTasks; i++) {
                fileOut.write((i + ". " + parser.parseWithEscaping(random, format) + "\n").getBytes(StandardCharsets.UTF_8));
                // System.out.println(i + ". " + p.parseWithEscaping(r, format) + "\n");
            }
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }
}
