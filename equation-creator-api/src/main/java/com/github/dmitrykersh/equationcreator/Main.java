package com.github.dmitrykersh.equationcreator;

import com.github.dmitrykersh.equationcreator.parser.Parser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;


public class Main {
   private static final String FORMAT_HINT = "Enter format string.\n"
           + "Formatting:"
           + "\n\tVariable definition: name=<value>"
           + "\n\tVariable usage: <name>"
           + "\n\tIf your variable <sign> is [+ - * /] and you need opposite sign: <!sign>"
           + "\n\t--------------------------------------------------------"
           + "\n\tTo perform in-line math operations as [+ - * / ^] "
           + "\n\tuse $ prefix and with spaces: 10 $+ 11 -> 21"
           + "\n\t--------------------------------------------------------"
           + "\n\tRandom integer between X and Y: [X..Y]"
           + "\n\tRandom float between X and Y dividable by D: [X..Y|:D]"
           + "\n\tRandom float between X and Y wits step S: [X..Y|S]"
           + "\n\tRandom item from list: {string1|char2|[1..10]}";

   private static final String TASKNUM_HINT = "\nEnter number of tasks.\n";

    public static void main(String[] args) {
       Scanner scanner = new Scanner(System.in);

       System.out.println(FORMAT_HINT);
       String format = scanner.nextLine();

       System.out.println(TASKNUM_HINT);
       int numberOfTasks = scanner.nextInt();

       Random r = new Random();
       Parser p = new Parser(format);

       try (FileOutputStream fileOut = new FileOutputStream("output.txt")){
           fileOut.write(("FORMAT:\n" + format + "\n\nTASKS:\n").getBytes(StandardCharsets.UTF_8));
           for (int i = 1; i <= numberOfTasks; i++){
               fileOut.write((i + ". " + p.parseWithEscaping(r, format) + "\n").getBytes(StandardCharsets.UTF_8));
               System.out.println(i + ". " + p.parseWithEscaping(r, format) + "\n");
           }
       } catch (IOException exception){
           System.out.println(exception.getMessage());
       }
    }
}
