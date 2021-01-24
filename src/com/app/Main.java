package com.app;

import math_utils.Rational;
import parser.Parser;

import java.util.Random;
import java.util.Scanner;

public class Main {
    private static final String HINT = "Enter format string and number of tasks.\n"
            + "Formatting:\n"
            + "\tRandom number between X and Y: [X..Y]\n"
            + "\tRandom char/string from list: {string1|char2|string3}\n";

    public static void main(String[] args) {
        System.out.println(HINT);

        Scanner scanner = new Scanner(System.in);

        String format = scanner.nextLine();
        int numberOfTasks = scanner.nextInt();

        Random r = new Random();
        Parser p = new Parser(format);

        for (int i = 1; i <= numberOfTasks; i++){
            System.out.println(i + ". " + p.createEquation(r));
        }
    }
}
