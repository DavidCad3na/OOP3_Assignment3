package appDomain;

import implementations.BSTree;
import utilities.BSTreeADT;
import utilities.Iterator;

import java.io.*;
import java.util.Scanner;
import java.util.List;

public class WordTracker {
    private static final String REPOSITORY_FILE = "repository.ser";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar WordTracker.jar <input.txt> -pf/-pl/-po [-f<output.txt>]");
            return;
        }

        String inputFile = args[0];
        String option = args[1];
        String outputFile = null;
        if (args.length == 3 && args[2].startsWith("-f")) {
            outputFile = args[2].substring(2);
        }

        BSTreeADT<TrackedWord> tree = loadTree();

        processFile(inputFile, tree);

        saveTree(tree);

        StringBuilder report = new StringBuilder();
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

        Iterator<TrackedWord> it = tree.inorderIterator();
        while (it.hasNext()) {
            TrackedWord word = it.next();
            report.append("Key : ===").append(word.getDisplayWord()).append("===");
            if (option.equals("-po")) {
                int total = word.getFiles().values().stream().mapToInt(TrackedWord.FileInfo::getCount).sum();
                report.append(" number of entries: ").append(total);
            }
            boolean firstFile = true;
            for (String file : word.getFileList()) {
                if (!firstFile || option.equals("-pl") || option.equals("-po")) {
                    report.append(" ");
                }
                report.append("found in file: ").append(file);
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

    private static BSTreeADT<TrackedWord> loadTree() {
        File file = new File(REPOSITORY_FILE);
        if (file.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                return (BSTreeADT<TrackedWord>) in.readObject();
            } catch (Exception e) {
                System.err.println("Failed to load repository, starting new tree.");
            }
        }
        return new BSTree<>();
    }

    private static void saveTree(BSTreeADT<TrackedWord> tree) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(REPOSITORY_FILE))) {
            out.writeObject(tree);
        } catch (IOException e) {
            System.err.println("Failed to save repository.");
        }
    }

    private static void processFile(String filename, BSTreeADT<TrackedWord> tree) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            int lineNum = 1;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                for (String word : line.split("[^a-zA-Z0-9']+")) {
                    if (word.isEmpty()) continue;
                    String key = word.toLowerCase();
                    TrackedWord searchWord = new TrackedWord(key);
                    TrackedWord found = null;
                    if (tree.contains(searchWord)) {
                        found = tree.search(searchWord).getElement();
                    }
                    if (found == null) {
                        found = new TrackedWord(key, word);
                        tree.add(found);
                    }
                    found.addOccurrence(filename, lineNum);
                }
                lineNum++;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filename);
        }
    }
}


