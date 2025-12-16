package appDomain;

import java.io.*; import java.nio.file.*; import java.util.*;

public class WordTracker { 
	// Repository filename 
	private static final String REPO = "repository.ser";

public static void main(String[] args) {
    if (args.length < 2) {
        System.err.println("Usage: java -jar WordTracker.jar <input.txt> -pf|-pl|-po [-f<output.txt>]");
        System.exit(1);
    }

    String inputPath = args[0];
    String option = args[1];
    String outPath = null;
    if (args.length >= 3 && args[2].startsWith("-f")) {
        outPath = args[2].substring(2);
        if (outPath.isEmpty()) {
            System.err.println("Invalid -f argument.");
            System.exit(1);
        }
    }

    WordBST tree = null;
    // Restore repository if exists
    File repoFile = new File(REPO);
    if (repoFile.exists()) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(repoFile))) {
            tree = (WordBST) ois.readObject();
        } catch (Exception e) {
            System.err.println("Failed to restore repository: " + e.getMessage());
            tree = new WordBST();
        }
    } else {
        tree = new WordBST();
    }

    // Scan input file
    Path p = Paths.get(inputPath);
    if (!Files.exists(p)) {
        System.err.println("Input file does not exist: " + inputPath);
        System.exit(1);
    }

    String filename = p.getFileName().toString();
    try (BufferedReader br = Files.newBufferedReader(p)) {
        String line;
        int lineno = 0;
        while ((line = br.readLine()) != null) {
            lineno++;
            String[] tokens = line.split("[^A-Za-z]+");
            for (String tok : tokens) {
                if (tok == null || tok.isEmpty()) continue;
                String key = tok.toLowerCase();   // used for ordering and lookup
                String display = tok;             // preserve original case for output
                tree.insert(key, display, filename, lineno);
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading input file: " + e.getMessage());
        System.exit(1);
    }

    // Save repository
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REPO))) {
        oos.writeObject(tree);
    } catch (IOException e) {
        System.err.println("Failed to save repository: " + e.getMessage());
    }

    // Prepare output writer
    try (PrintWriter pw = (outPath == null) ? new PrintWriter(System.out, true)
            : new PrintWriter(new FileWriter(outPath))) {
        switch (option) {
            case "-pf":
                pw.println("Displaying -pf format");
                tree.printFilesOnly(pw);
                if (outPath == null) pw.println("\nNot exporting file.");
                break;
            case "-pl":
                tree.printFilesAndLines(pw);
                break;
            case "-po":
                tree.printFilesLinesAndCounts(pw);
                break;
            default:
                System.err.println("Unknown option: " + option);
                System.exit(1);
        }
    } catch (IOException e) {
        System.err.println("Failed to write output: " + e.getMessage());
        System.exit(1);
    }
}

// Binary search tree for words (case-insensitive ordering; preserve original display)
public static class WordBST implements Serializable {
    private static final long serialVersionUID = 1L;
    private Node root;

    public synchronized void insert(String key, String display, String filename, int lineno) {
        root = insertRec(root, key, display, filename, lineno);
    }

    private Node insertRec(Node node, String key, String display, String filename, int lineno) {
        if (node == null) {
            Node n = new Node(key, display);
            n.addOccurrence(filename, lineno);
            return n;
        }
        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            node.addOccurrence(filename, lineno);
        } else if (cmp < 0) {
            node.left = insertRec(node.left, key, display, filename, lineno);
        } else {
            node.right = insertRec(node.right, key, display, filename, lineno);
        }
        return node;
    }

    // -pf: print word and list of files in the example format
    public void printFilesOnly(PrintWriter pw) {
        inorder(root, (n) -> {
            pw.print("Key : ===" + n.displayWord + "=== found in file: ");
            pw.println(String.join(", ", n.getFileList()));
        });
    }

    // -pl: print word with files and line numbers
    public void printFilesAndLines(PrintWriter pw) {
        inorder(root, (n) -> {
            pw.print(n.displayWord + ": ");
            List<String> parts = new ArrayList<>();
            for (Map.Entry<String, FileInfo> e : n.files.entrySet()) {
                parts.add(e.getKey() + " " + e.getValue().linesString());
            }
            pw.println(String.join("; ", parts));
        });
    }

    // -po: print word with files, lines and counts (per file and total)
    public void printFilesLinesAndCounts(PrintWriter pw) {
        inorder(root, (n) -> {
            pw.print(n.displayWord + ": ");
            List<String> parts = new ArrayList<>();
            int total = 0;
            for (Map.Entry<String, FileInfo> e : n.files.entrySet()) {
                FileInfo fi = e.getValue();
                total += fi.count;
                parts.add(e.getKey() + " (count=" + fi.count + ") " + fi.linesString());
            }
            pw.println(String.join("; ", parts) + " ; total=" + total);
        });
    }

    private void inorder(Node node, java.util.function.Consumer<Node> consumer) {
        if (node == null) return;
        inorder(node.left, consumer);
        consumer.accept(node);
        inorder(node.right, consumer);
    }
}

// Node representing a single word (stores lowercase key for ordering and first-seen display form)
public static class Node implements Serializable {
    private static final long serialVersionUID = 1L;
    String key;           // lowercase key for comparisons
    String displayWord;   // original-case first-seen word for display
    Node left, right;
    // maintain insertion order of files
    LinkedHashMap<String, FileInfo> files = new LinkedHashMap<>();

    Node(String key, String display) {
        this.key = key;
        this.displayWord = display;
    }

    void addOccurrence(String filename, int lineno) {
        FileInfo fi = files.get(filename);
        if (fi == null) {
            fi = new FileInfo();
            files.put(filename, fi);
        }
        fi.addLine(lineno);
    }

    List<String> getFileList() {
        return new ArrayList<>(files.keySet());
    }
}

// Info per file: line numbers and count
public static class FileInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    ArrayList<Integer> lines = new ArrayList<>();
    int count = 0;

    void addLine(int lineno) {
        lines.add(lineno);
        count++;
    }

    String linesString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[lines: ");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(lines.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}

}
