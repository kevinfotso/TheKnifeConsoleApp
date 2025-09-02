package theknife;

/**
 * Autori:
 * TCHIDJO FOTSO KEVIN - 755906 -  VA
 * Massanova Alessandro - 760228 - VA
 * Progetto: TheKnife - Console App (Lab A)
 */

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestisce caricamento/salvataggio dei CSV e fornisce le API richieste dalla specifica.
 * File: ristoranti.csv, utenti.csv, recensioni.csv, preferiti.csv
 */
public class GestoreDati {
    private final Path dataDir;
    public final Map<Integer, Ristorante> ristoranti = new LinkedHashMap<>();
    public final Map<String, Utente> utenti = new LinkedHashMap<>();
    public final Map<Integer, Recensione> recensioni = new LinkedHashMap<>();
    public final Map<String, Map<Integer,String>> preferiti = new LinkedHashMap<>(); // username -> (ristoranteId -> nota)
    private int nextRistoranteId = 1;
    private int nextRecensioneId = 1;

    public GestoreDati(Path dataDir) { this.dataDir = dataDir; this.dataDir.toFile().mkdirs(); }

    /** Carica tutti i dati dai CSV. */
    public void carica() throws Exception {
        caricaUtenti();
        caricaRistoranti();
        caricaRecensioni();
        caricaPreferiti();
        if (!ristoranti.isEmpty()) nextRistoranteId = Collections.max(ristoranti.keySet()) + 1;
        if (!recensioni.isEmpty()) nextRecensioneId = Collections.max(recensioni.keySet()) + 1;
    }

    /** Salva tutti i dati sui CSV. */
    public void salva() throws Exception {
        salvaRistoranti();
        salvaUtenti();
        salvaRecensioni();
        salvaPreferiti();
    }

    /** Calcola l'hash SHA-256 di una stringa. */
    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    // ==== CSV helpers ====
    private List<String[]> leggiCsv(Path p) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(p)) return rows;
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String header = br.readLine(); // skip
            if (header == null) return rows;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                rows.add(parseCsvLine(line));
            }
        }
        return rows;
    }

    private void scriviCsv(Path p, String header, List<String[]> rows) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(p))) {
            pw.println(header);
            for (String[] r : rows) pw.println(toCsvLine(r));
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        boolean inQ = false;
        StringBuilder cur = new StringBuilder();
        for (int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if (c=='"') {
                inQ = !inQ;
            } else if (c==',' && !inQ) {
                out.add(cur.toString());
                cur.setLength(0);
            } else cur.append(c);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private String toCsvLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<fields.length;i++) {
            String f = fields[i] == null ? "" : fields[i];
            boolean needQ = f.contains(",") || f.contains("\"") || f.contains("\n");
            if (needQ) f = "\"" + f.replace("\"","\"\"") + "\"";
            sb.append(f);
            if (i < fields.length-1) sb.append(",");
        }
        return sb.toString();
    }

    private void caricaUtenti() throws Exception {
        Path p = dataDir.resolve("utenti.csv");
        for (String[] r : leggiCsv(p)) {
            Utente u = new Utente(r[0], r[1], r[2], r[3], r[4], r[5], Ruolo.valueOf(r[6]));
            utenti.put(u.username, u);
        }
    }

    private void salvaUtenti() throws Exception {
        Path p = dataDir.resolve("utenti.csv");
        List<String[]> rows = new ArrayList<>();
        for (Utente u : utenti.values()) {
            rows.add(new String[]{u.username, u.passwordHash, u.nome, u.cognome, u.dataNascita, u.domicilio, u.ruolo.name()});
        }
        scriviCsv(p, "username,password_hash,nome,cognome,data_nascita,domicilio,ruolo", rows);
    }

    private void caricaRistoranti() throws Exception {
        Path p = dataDir.resolve("ristoranti.csv");
        for (String[] r : leggiCsv(p)) {
            Ristorante rr = new Ristorante(Integer.parseInt(r[0]), r[1], r[2], r[3], r[4],
                    Double.parseDouble(r[5]), Double.parseDouble(r[6]),  (int)Double.parseDouble(r[7]) ,
                    Boolean.parseBoolean(r[8]), Boolean.parseBoolean(r[9]), r[10], r[11]);
            ristoranti.put(rr.id, rr);
        }
    }

    private void salvaRistoranti() throws Exception {
        Path p = dataDir.resolve("ristoranti.csv");
        List<String[]> rows = new ArrayList<>();
        for (Ristorante r : ristoranti.values()) {
            rows.add(new String[]{String.valueOf(r.id), r.nome, r.nazione, r.citta, r.indirizzo,
                    String.valueOf(r.lat), String.valueOf(r.lon), String.valueOf(r.prezzoMedio),
                    String.valueOf(r.delivery), String.valueOf(r.prenotazione), r.tipoCucina, r.proprietario});
        }
        scriviCsv(p, "id,nome,nazione,citta,indirizzo,lat,lon,prezzo_medio,delivery,prenotazione,tipo_cucina,proprietario", rows);
    }

    private void caricaRecensioni() throws Exception {
        Path p = dataDir.resolve("recensioni.csv");
        for (String[] r : leggiCsv(p)) {
            Recensione rec = new Recensione(Integer.parseInt(r[0]), Integer.parseInt(r[1]), r[2],
                    Integer.parseInt(r[3]), r[4], r.length>5 ? r[5] : "");
            recensioni.put(rec.id, rec);
        }
    }

    private void salvaRecensioni() throws Exception {
        Path p = dataDir.resolve("recensioni.csv");
        List<String[]> rows = new ArrayList<>();
        for (Recensione rec : recensioni.values()) {
            rows.add(new String[]{String.valueOf(rec.id), String.valueOf(rec.ristoranteId), rec.username,
                    String.valueOf(rec.stelle), rec.testo, rec.risposta == null ? "" : rec.risposta});
        }
        scriviCsv(p, "id,ristorante_id,username,stelle,testo,risposta", rows);
    }

    private void caricaPreferiti() throws Exception {
        Path p = dataDir.resolve("preferiti.csv");
        for (String[] r : leggiCsv(p)) {
            String user = r[0];
            int rid = Integer.parseInt(r[1]);
            String nota = r.length>2 ? r[2] : "";
            preferiti.computeIfAbsent(user, k-> new LinkedHashMap<>()).put(rid, nota);
        }
    }

    private void salvaPreferiti() throws Exception {
        Path p = dataDir.resolve("preferiti.csv");
        List<String[]> rows = new ArrayList<>();
        for (Map.Entry<String, Map<Integer,String>> e : preferiti.entrySet()) {
            for (Map.Entry<Integer,String> ridNote : e.getValue().entrySet()) {
                rows.add(new String[]{e.getKey(), String.valueOf(ridNote.getKey()), ridNote.getValue()==null?"":ridNote.getValue()});
            }
        }
        scriviCsv(p, "username,ristorante_id,note", rows);
    }

    // ====== API richieste ======

    public List<Ristorante> cercaRistorante(String paeseOCitta, String tipoCucina,
                                            Double prezzoMin, Double prezzoMax,
                                            Boolean delivery, Boolean prenotazione,
                                            Double mediaMin) {
        String q = paeseOCitta == null ? "" : paeseOCitta.trim().toLowerCase();
        return ristoranti.values().stream().filter(r ->
            (r.citta.toLowerCase().contains(q) || r.nazione.toLowerCase().contains(q)) &&
            (tipoCucina == null || r.tipoCucina.equalsIgnoreCase(tipoCucina)) &&
            (prezzoMin == null || r.prezzoMedio >= prezzoMin) &&
            (prezzoMax == null || r.prezzoMedio <= prezzoMax) &&
            (delivery == null || r.delivery == delivery) &&
            (prenotazione == null || r.prenotazione == prenotazione) &&
            (mediaMin == null || mediaRistorante(r.id) >= mediaMin)
        ).collect(Collectors.toList());
    }

    public String visualizzaRistorante(Ristorante r) {
        List<String> h = Arrays.asList("ID","Nome","Luogo","Prezzo","Servizi","Cucina","Proprietario");
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(
                String.valueOf(r.id), r.nome,
                r.citta+" ("+r.nazione+") - "+r.indirizzo,
                r.prezzoMedio+"€",
                (r.delivery?"Delivery ":"")+(r.prenotazione?"Prenotaz.":""),
                r.tipoCucina, r.proprietario
        ));
        return AsciiTable.render(h, rows);
    }

    public String visualizzaRecensioni(int ristoranteId) {
        List<String> h = Arrays.asList("ID","Utente","Stelle","Testo","Risposta");
        List<List<String>> rows = new ArrayList<>();
        for (Recensione rec : recensioni.values()) {
            if (rec.ristoranteId == ristoranteId) {
                rows.add(Arrays.asList(String.valueOf(rec.id), rec.username, String.valueOf(rec.stelle), rec.testo, rec.risposta==null?"":rec.risposta));
            }
        }
        return AsciiTable.render(h, rows);
    }

    public Utente registrazione(String username, String password, String nome, String cognome, String dataN, String domicilio, Ruolo ruolo) {
        if (utenti.containsKey(username)) return null;
        Utente u = new Utente(username, sha256(password), nome, cognome, dataN, domicilio, ruolo);
        utenti.put(username, u);
        return u;
    }

    public Utente login(String username, String password) {
        Utente u = utenti.get(username);
        if (u == null) return null;
        return u.passwordHash.equals(sha256(password)) ? u : null;
    }

    // ===== Preferiti (cliente) =====
    public boolean aggiungiPreferito(String username, int ristoranteId) {
        if (!ristoranti.containsKey(ristoranteId)) return false;
        Map<Integer,String> m = preferiti.computeIfAbsent(username, k-> new LinkedHashMap<>());
        if (m.containsKey(ristoranteId)) return false;
        m.put(ristoranteId, "");
        return true;
    }
    public boolean rimuoviPreferito(String username, int ristoranteId) {
        Map<Integer,String> m = preferiti.get(username);
        if (m == null) return false;
        return m.remove(ristoranteId) != null;
    }
    public List<Ristorante> visualizzaPreferiti(String username) {
        Map<Integer,String> m = preferiti.getOrDefault(username, Collections.emptyMap());
        List<Ristorante> out = new ArrayList<>();
        for (Integer id : m.keySet()) if (ristoranti.containsKey(id)) out.add(ristoranti.get(id));
        return out;
    }
    /** Aggiorna/Imposta una nota per un preferito dell'utente. */
    public boolean aggiornaNotaPreferito(String username, int ristoranteId, String nota) {
        Map<Integer,String> m = preferiti.get(username);
        if (m == null || !m.containsKey(ristoranteId)) return false;
        m.put(ristoranteId, nota == null ? "" : nota);
        return true;
    }

    /** Rappresentazione tabellare dei preferiti con eventuale nota. */
    public String visualizzaPreferitiTable(String username) {
        List<String> h = Arrays.asList("ID","Nome","Luogo","Nota");
        List<List<String>> rows = new ArrayList<>();
        Map<Integer,String> m = preferiti.getOrDefault(username, Collections.emptyMap());
        for (Map.Entry<Integer,String> e : m.entrySet()) {
            Ristorante r = ristoranti.get(e.getKey());
            if (r != null) {
                rows.add(Arrays.asList(String.valueOf(r.id), r.nome, r.citta + ", " + r.nazione, e.getValue()==null?"":e.getValue()));
            }
        }
        return AsciiTable.render(h, rows);
    }

    /** Dump raw del CSV preferiti per mostrare ciò che è stato scritto sul file. */
    public String dumpPreferitiCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("username,ristorante_id,note\n");
        for (Map.Entry<String, Map<Integer,String>> e : preferiti.entrySet()) {
            for (Map.Entry<Integer,String> x : e.getValue().entrySet()) {
                String nota = x.getValue()==null?"":x.getValue().replaceAll("\\n"," ");
                sb.append(e.getKey()).append(",").append(x.getKey()).append(",").append(nota).append("\n");
            }
        }
        return sb.toString();
    }



    // ===== Recensioni (cliente) =====
    public Recensione aggiungiRecensione(String username, int ristoranteId, int stelle, String testo) {
        Recensione r = new Recensione(nextRecensioneId++, ristoranteId, username, stelle, testo, "");
        recensioni.put(r.id, r);
        return r;
    }
    public boolean modificaRecensione(String username, int recensioneId, int nuoveStelle, String nuovoTesto) {
        Recensione r = recensioni.get(recensioneId);
        if (r == null || !r.username.equals(username)) return false;
        r.stelle = nuoveStelle;
        r.testo = nuovoTesto;
        return true;
    }
    public boolean eliminaRecensione(String username, int recensioneId) {
        Recensione r = recensioni.get(recensioneId);
        if (r == null || !r.username.equals(username)) return false;
        recensioni.remove(recensioneId);
        return true;
    }

    // ===== Ristoratore =====
    public Ristorante aggiungiRistorante(String proprietario, String nome, String nazione, String citta, String indirizzo,
                                         double lat, double lon, double prezzoMedio, boolean delivery, boolean prenotazione, String tipoCucina) {
        Ristorante r = new Ristorante(nextRistoranteId++, nome, nazione, citta, indirizzo, lat, lon, prezzoMedio, delivery, prenotazione, tipoCucina, proprietario);
        ristoranti.put(r.id, r);
        return r;
    }

    public String visualizzaRiepilogo(String proprietario) {
        List<String> h = Arrays.asList("ID","Nome","#Rec","Media");
        List<List<String>> rows = new ArrayList<>();
        for (Ristorante r : ristoranti.values()) {
            if (!r.proprietario.equals(proprietario)) continue;
            long count = recensioni.values().stream().filter(x->x.ristoranteId==r.id).count();
            double media = mediaRistorante(r.id);
            rows.add(Arrays.asList(String.valueOf(r.id), r.nome, String.valueOf(count), String.format(java.util.Locale.US, "%.2f", media)));
        }
        return AsciiTable.render(h, rows);
    }

    public boolean rispostaRecensioni(String proprietario, int recensioneId, String risposta) {
        Recensione rec = recensioni.get(recensioneId);
        if (rec == null) return false;
        Ristorante r = ristoranti.get(rec.ristoranteId);
        if (r == null || !r.proprietario.equals(proprietario)) return false;
        if (rec.risposta != null && !rec.risposta.isEmpty()) return false; // una sola risposta
        rec.risposta = risposta;
        return true;
    }

    // ===== Ordinamenti =====
    public List<Ristorante> ordinaPerPrezzo(List<Ristorante> in) {
        return in.stream().sorted(Comparator.comparingDouble(r->r.prezzoMedio)).collect(Collectors.toList());
    }
    public List<Ristorante> ordinaPerNome(List<Ristorante> in) {
        return in.stream().sorted(Comparator.comparing(r->r.nome.toLowerCase())).collect(Collectors.toList());
    }
    public List<Ristorante> ordinaPerMediaRecensioni(List<Ristorante> in) {
        return in.stream().sorted(Comparator.comparingDouble((Ristorante r)-> mediaRistorante(r.id))).collect(Collectors.toList());
    }

    public double mediaRistorante(int ristoranteId) {
        int sum=0, n=0;
        for (Recensione r : recensioni.values()) if (r.ristoranteId==ristoranteId) { sum+=r.stelle; n++; }
        return n==0 ? 0.0 : (double)sum/n;
    }
}
