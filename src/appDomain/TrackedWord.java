package appDomain;

import java.io.Serializable;
import java.util.*;

public class TrackedWord implements Comparable<TrackedWord>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String key; // lowercase key for ordering
    private String displayWord; // first-seen case for display
    private final LinkedHashMap<String, FileInfo> files = new LinkedHashMap<>();

    public TrackedWord(String key) {
        this(key, key);
    }

    public TrackedWord(String key, String displayWord) {
        this.key = key;
        this.displayWord = displayWord;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayWord() {
        return displayWord;
    }

    public void setDisplayWord(String displayWord) {
        this.displayWord = displayWord;
    }

    public Map<String, FileInfo> getFiles() {
        return files;
    }

    public List<String> getFileList() {
        return new ArrayList<>(files.keySet());
    }

    public void addOccurrence(String filename, int lineno) {
        FileInfo fi = files.get(filename);
        if (fi == null) {
            fi = new FileInfo();
            files.put(filename, fi);
        }
        fi.addLine(lineno);
    }

    @Override
    public int compareTo(TrackedWord other) {
        if (other == null) return 1;
        return this.key.compareTo(other.key);
    }

    @Override
    public String toString() {
        return displayWord;
    }

    public static class FileInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final LinkedHashSet<Integer> lines = new LinkedHashSet<>();
        private int count = 0;

        public void addLine(int lineno) {
            lines.add(lineno);
            count++;
        }

        public int getCount() {
            return count;
        }

        public List<Integer> getLines() {
            return new ArrayList<>(lines);
        }

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
