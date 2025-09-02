package theknife;

/**
 * Autori:
 * TCHIDJO FOTSO KEVIN - 755906 -  VA
 * Massanova Alessandro - 760228 - VA
 * Progetto: TheKnife - Console App (Lab A)
 */

import java.util.*;

/**
 * Utility minimale per generare tabelle ASCII in console.
 * Non dipende da librerie esterne.
 */
public class AsciiTable {
    /**
     * Rende una tabella ASCII con intestazioni e righe.
     * @param headers intestazioni
     * @param rows righe (ogni riga è una lista di celle in stringa)
     * @return tabella formattata
     */
    public static String render(List<String> headers, List<List<String>> rows) {
        List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int w = headers.get(i).length();
            for (List<String> row : rows) {
                if (i < row.size()) w = Math.max(w, row.get(i) == null ? 0 : row.get(i).length());
            }
            widths.add(w);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(line(widths)).append("\n");
        sb.append(row(widths, headers)).append("\n");
        sb.append(line(widths)).append("\n");
        for (List<String> r : rows) sb.append(row(widths, r)).append("\n");
        sb.append(line(widths));
        return sb.toString();
    }

    private static String line(List<Integer> widths) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : widths) {
            sb.append("-".repeat(w + 2)).append("+");
        }
        return sb.toString();
    }
    private static String row(List<Integer> widths, List<String> cells) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < widths.size(); i++) {
            String c = i < cells.size() ? (cells.get(i) == null ? "" : cells.get(i)) : "";
            sb.append(" ").append(pad(c, widths.get(i))).append(" |");
        }
        return sb.toString();
    }
    private static String pad(String s, int w) {
        if (s.length() >= w) return s;
        return s + " ".repeat(w - s.length());
    }
}
