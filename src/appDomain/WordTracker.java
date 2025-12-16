package appDomain;

import implementations.BSTree;
import utilities.BSTreeADT;
import utilities.Iterator;

import java.io.*;
import java.util.Scanner;
import java.util.List;

// Main class for the WordTracker program
public class WordTracker {

    // File used to save and load the tree between program runs
    private static final String REPOSITORY_FILE = "repository.ser";

    // Program entry point
    public static void main(String[] args) {

        // Check that enough command line arguments were provided
        if (args.length < 2) {
            System.err.println("Usage: java -jar WordTracker.jar <input.txt> -pf/-pl/-po [-f<output.txt>]");
            return;
        }

        // Get input file and output option from arguments
        String inputFile = args[0];
        String option = args[1];
        String outputFile = null;

        // Check if an output file was specified
        if (args.length == 3 && args[2].startsWith("-f")) {
            outputFile = args[2].substring(2);
        }

        // Load existing tree or create a new one
        BSTreeADT<TrackedWord> tree = loadTree();

        // Read the file and update the tree
        processFile(inputFile, tree);

        // Save the updated tree
        saveTree(tree);

        StringBuilder report = new StringBuilder();

        // Determine which output format to use
        switch (option) {
            case "-pf":
                report.append("Displaying -pf format\n");
                break;
            case "-pl":
                report.append("Displaying -pl format\n");
                break;
            case "-po":
                report.append("Displaying -po format\n");
                break;
            default:
                System.err.println("Invalid option: " + option);
                return;
        }

        // Go through the tree in alphabetical order
        Iterator<TrackedWord> it = tree.inorderIterator();
        while (it.hasNext()) {
            TrackedWord word = it.next();

            // Print the word being displayed
            report.append("Key : ===")
                  .append(word.getDisplayWord())
                  .append("===");

            // -po option shows total number of times the word appears
            if (option.equals("-po")) {
                int total = word.getFiles().values()
                        .stream()
                        .mapToInt(TrackedWord.FileInfo::getCount)
                        .sum();
                report.append(" number of entries: ").append(total);
            }

            boolean firstFile = true;

            // Loop through each file where the word appears
            for (String file : word.getFileList()) {

                // Add spacing between file entries
                if (!firstFile || option.equals("-pl") || option.equals("-po")) {
                    report.append(" ");
                }

                report.append("found in file: ").append(file);

                // -pl and -po options also show line numbers
                if (option.equals("-pl") || option.equals("-po")) {
                    report.append(" on lines: ");
                    List<Integer> lines = word.getFiles().get(file).getLines();
                    for (int i = 0; i < lines.size(); i++) {
                        report.append(lines.get(i)).append(",");
                    }
                }

                firstFile = false;
            }

            report.append(System.lineSeparator());
        }

        // Output the report to a file or the console
        if (outputFile != null) {
            try (PrintWriter out = new PrintWriter(outputFile)) {
                out.print(report);
            } catch (IOException e) {
                System.err.println("Error writing to file: " + outputFile);
            }
        } else {
            System.out.print(report);
            System.out.println("Not exporting file.");
        }
    }

    // Loads the tree from the saved file if it exists
    private static BSTreeADT<TrackedWord> loadTree() {
        File file = new File(REPOSITORY_FILE);

        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return (BSTreeADT<TrackedWord>) in.readObject();
            } catch (Exception e) {
                System.err.println("Failed to load repository, starting new tree.");
            }
        }

        // Return a new empty tree if loading fails
        return new BSTree<>();
    }

    // Saves the tree to a file
    private static void saveTree(BSTreeADT<TrackedWord> tree) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(REPOSITORY_FILE))) {
            out.writeObject(tree);
        } catch (IOException e) {
            System.err.println("Failed to save repository.");
        }
    }

    // Reads the input file and tracks each word
    private static void processFile(String filename, BSTreeADT<TrackedWord> tree) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            int lineNum = 1;

            // Read file line by line
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // Split the line into words
                for (String word : line.split("[^a-zA-Z0-9']+")) {
                    if (word.isEmpty()) continue;

                    // Convert word to lowercase for searching
                    String key = word.toLowerCase();
                    TrackedWord searchWord = new TrackedWord(key);
                    TrackedWord found = null;

                    // Check if the word already exists in the tree
                    if (tree.contains(searchWord)) {
                        found = tree.search(searchWord).getElement();
                    }

                    // If the word is not found, add it
                    if (found == null) {
                        found = new TrackedWord(key, word);
                        tree.add(found);
                    }

                    // Record where the word appears
                    found.addOccurrence(filename, lineNum);
                }

                lineNum++;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
        }
    }
}
