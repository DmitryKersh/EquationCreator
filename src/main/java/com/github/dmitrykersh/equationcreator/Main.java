package com.github.dmitrykersh.equationcreator;

import com.github.dmitrykersh.equationcreator.parser.Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        try {
            FileOutputStream fileOut = new FileOutputStream("output.txt");
            fileOut.write(("FORMAT:\n" + format + "\n\nTASKS:\n").getBytes(StandardCharsets.UTF_8));
            for (int i = 1; i <= numberOfTasks; i++){
                fileOut.write((i + ". " + p.createEquation(r) + "\n").getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException exception){
            System.out.println(exception.getMessage());
        }
    }
}
