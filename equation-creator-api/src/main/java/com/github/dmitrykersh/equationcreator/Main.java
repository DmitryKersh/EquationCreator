package com.github.dmitrykersh.equationcreator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
//    private static final String FORMAT_HINT = "Enter format string.\n"
//            + "Formatting:"
//            + "\n\tVariable definition: name=<value>"
//            + "\n\tVariable usage: <name>"
//            + "\n\tIf your variable <sign> is [+ - * /] and you need opposite sign: <!sign>"
//            + "\n\t--------------------------------------------------------"
//            + "\n\tTo perform in-line math operations as [+ - * / ^] "
//            + "\n\tuse $ prefix and no spaces between anything: 10$+11 -> 21"
//            + "\n\t--------------------------------------------------------"
//            + "\n\tRandom integer between X and Y: [X..Y]"
//            + "\n\tRandom float between X and Y dividable by D: [X..Y|:D]"
//            + "\n\tRandom float between X and Y wits step S: [X..Y|S]"
//            + "\n\tRandom item from list: {string1|char2|[1..10]}";
//
//    private static final String TASKNUM_HINT = "\nEnter number of tasks.\n";
//
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println(FORMAT_HINT);
//        String format = scanner.nextLine();
//
//        System.out.println(TASKNUM_HINT);
//        int numberOfTasks = scanner.nextInt();
//
//        Random r = new Random();
//        Parser p = new Parser(format);
//
//        try {
//            FileOutputStream fileOut = new FileOutputStream("output.txt");
//            fileOut.write(("FORMAT:\n" + format + "\n\nTASKS:\n").getBytes(StandardCharsets.UTF_8));
//            for (int i = 1; i <= numberOfTasks; i++){
//                fileOut.write((i + ". " + p.createEquation(r) + "\n").getBytes(StandardCharsets.UTF_8));
//            }
//        } catch (IOException exception){
//            System.out.println(exception.getMessage());
//        }

        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Label l = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        Scene scene = new Scene(new StackPane(l), 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}
