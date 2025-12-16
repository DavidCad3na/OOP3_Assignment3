package appDomain;

import java.io.Serializable;
import java.util.*;

// Represents a word that is being tracked in the file(s)
public class TrackedWord implements Comparable<TrackedWord>, Serializable {

    // Used for object serialization
    private static final long serialVersionUID = 1L;

    // Lowercase version of the word used for sorting and comparisons
    private final String key;

    // Original version of the word for displaying output
    private String displayWord;

    // Stores file names and their associated word info
    private final LinkedHashMap<String, FileInfo> files = new LinkedHashMap<>();

    // Constructor when only the key is provided
    public TrackedWord(String key) {
        this(key, key);
    }

    // Constructor that sets both the key and display word
    public TrackedWord(String key, String displayWord) {
        this.key = key;
        this.displayWord = displayWord;
    }

    // Returns the lowercase key
    public String getKey() {
        return key;
    }

    // Returns the word used for display
    public String getDisplayWord() {
        return displayWord;
    }

    // Updates the display version of the word
    public void setDisplayWord(String displayWord) {
        this.displayWord = displayWord;
    }

    // Returns the map of files and their info
    public Map<String, FileInfo> getFiles() {
        return files;
    }

    // Returns a list of file names where the word appears
    public List<String> getFileList() {
        return new ArrayList<>(files.keySet());
    }

    // Adds a new occurrence of the word in a file and line number
    public void addOccurrence(String filename, int lineno) {
        FileInfo fi = files.get(filename);

        // Create a new FileInfo if the file has not been seen before
        if (fi == null) {
            fi = new FileInfo();
            files.put(filename, fi);
        }

        // Record the line number
        fi.addLine(lineno);
    }

    // Compares two TrackedWord objects alphabetically
    @Override
    public int compareTo(TrackedWord other) {
        if (other == null) return 1;
        return this.key.compareTo(other.key);
    }

    // Returns the word when printed
    @Override
    public String toString() {
        return displayWord;
    }

    // Stores information about where a word appears in a single file
    public static class FileInfo implements Serializable {

        // Used for object serialization
        private static final long serialVersionUID = 1L;

        // Stores unique line numbers where the word appears
        private final LinkedHashSet<Integer> lines = new LinkedHashSet<>();

        // Total number of times the word appears in the file
        private int count = 0;

        // Adds a line number and increases the count
        public void addLine(int lineno) {
            lines.add(lineno);
            count++;
        }

        // Returns how many times the word appears in the file
        public int getCount() {
            return count;
        }

        // Returns the list of line numbers
        public List<Integer> getLines() {
            return new ArrayList<>(lines);
        }

        // Returns the line numbers as a formatted string
        public String linesString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[lines: ");
            int i = 0;
            for (Integer ln : lines) {
                if (i++ > 0) sb.append(", ");
                sb.append(ln);
            }
            sb.append("]");
            return sb.toString();
        }
    }
}