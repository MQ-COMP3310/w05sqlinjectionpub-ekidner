package workshop05code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Scanner;
//Included for the logging exercise
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 *
 * @author sqlitetutorial.net
 */
public class App {
    static {
        // Load logging properties
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("resources/logging.properties"));
        } catch (SecurityException | IOException e1) {
            e1.printStackTrace();
        }
    }

    private static final Logger logger = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        SQLiteConnectionManager wordleDatabaseConnection = new SQLiteConnectionManager("words.db");

        wordleDatabaseConnection.createNewDatabase("words.db");
        if (wordleDatabaseConnection.checkIfConnectionDefined()) {
            System.out.println("Wordle created and connected.");
        } else {
            System.out.println("Not able to connect. Sorry!");
            return;
        }

        if (wordleDatabaseConnection.createWordleTables()) {
            System.out.println("Wordle structures in place.");
        } else {
            System.out.println("Not able to launch. Sorry!");
            return;
        }

        // Read words from file and log invalid words (non-4-letter words) at SEVERE level
        try (BufferedReader br = new BufferedReader(new FileReader("resources/data.txt"))) {
            String line;
            int i = 1;
            while ((line = br.readLine()) != null) {
                // Check if the word is exactly 4 letters long
                if (line.length() == 4) {
                    wordleDatabaseConnection.addValidWord(i, line);
                    logger.log(Level.INFO, "Valid word: {0}", line); // Log valid 4-letter word
                } else {
                    logger.log(Level.SEVERE, "Invalid word in data.txt: {0}", line); // Log invalid word
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Not able to load. Sorry!");
            System.out.println(e.getMessage());
            return;
        }

        // Let users guess words and log invalid guesses at WARNING level
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter a 4-letter word for a guess or q to quit: ");
            String guess = scanner.nextLine();

            while (!guess.equals("q")) {
                System.out.println("You've guessed '" + guess + "'.");

                if (wordleDatabaseConnection.isValidWord(guess)) {
                    System.out.println("Success! It is in the list.\n");
                } else {
                    System.out.println("Sorry. This word is NOT in the list.\n");
                    logger.log(Level.WARNING, "Invalid guess: {0}", guess); // Log invalid guess
                }

                System.out.print("Enter a 4-letter word for a guess or q to quit: ");
                guess = scanner.nextLine();
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            logger.log(Level.WARNING, "Error during user input", e);
        }
    }
}
