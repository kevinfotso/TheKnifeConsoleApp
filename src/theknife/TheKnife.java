package theknife;
/**
 * Autori:
 * TCHIDJO FOTSO KEVIN - 755906 -  VA
 * Massanova Alessandro - 760228 - VA
 * Progetto: TheKnife - Console App (Lab A)
 */
import java.nio.file.Path;
import java.util.*;

/**
 * Entry point dell'applicazione console TheKnife.
 * Mostra i menu (guest/cliente/ristoratore) e invoca le API del GestoreDati.
 * I dati sono salvati automaticamente alla chiusura (shutdown hook).
 */
public class TheKnife {
    private static final Scanner in = new Scanner(System.in);
    private static GestoreDati db;

    public static void main(String[] args) throws Exception {
        // La cartella data è al livello superiore rispetto a /bin o /src durante l'esecuzione
        Path base = Path.of(System.getProperty("user.dir"));
        Path data = base.resolve("../data").normalize();
        db = new GestoreDati(data);
        try {
            db.carica();
        } catch (Exception e) {
            System.out.println("Errore nel caricamento dati: " + e.getMessage());
        }
        // Salvataggio automatico all'uscita
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { db.salva(); } catch (Exception e) { System.err.println("Errore salvataggio: "+e.getMessage()); }
        }));

        menuIniziale();
        System.out.println("Arrivederci!");
    }

    /** Menu iniziale: login/registrazione/guest. */
    private static void menuIniziale() {
        while (true) {
            System.out.println("\n=== TheKnife ===");
            System.out.println("1) Login");
            System.out.println("2) Registrazione");
            System.out.println("3) Continua come guest");
            System.out.println("0) Esci");
            System.out.print("> ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1": {
                    Utente u = login();
                    if (u != null) {
                        if (u.ruolo == Ruolo.CLIENTE) menuCliente(u);
                        else menuRistoratore(u);
                    }
                    break;
                }
                case "2": {
                    Utente u = registrazione();
                    if (u != null) System.out.println("Registrazione completata. Effettua il login.");
                    break;
                }
                case "3": menuGuest(); break;
                case "0": return;
                default: System.out.println("Scelta non valida.");
            }
        }
    }

    private static Utente login() {
        System.out.print("Username: "); String u = in.nextLine().trim();
        System.out.print("Password: "); String p = in.nextLine().trim();
        Utente user = db.login(u, p);
        if (user == null) System.out.println("Credenziali non valide.");
        return user;
    }

    /** registrazione() secondo specifica. */
    private static Utente registrazione() {
        System.out.println("\n== Registrazione ==");
        System.out.print("Username: "); String username = in.nextLine().trim();
        System.out.print("Password: "); String password = in.nextLine().trim();
        System.out.print("Nome: "); String nome = in.nextLine().trim();
        System.out.print("Cognome: "); String cognome = in.nextLine().trim();
        System.out.print("Data nascita (yyyy-MM-dd, vuoto se non nota): "); String dn = in.nextLine().trim();
        System.out.print("Domicilio (città): "); String dom = in.nextLine().trim();
        System.out.print("Ruolo (1=CLIENTE, 2=RISTORATORE): "); String r = in.nextLine().trim();
        Ruolo ruolo = "2".equals(r) ? Ruolo.RISTORATORE : Ruolo.CLIENTE;
        Utente u = db.registrazione(username, password, nome, cognome, dn, dom, ruolo);
        if (u == null) System.out.println("Username già esistente.");
        return u;
    }

    // ==== MENU GUEST ====
    private static void menuGuest() {
        while (true) {
            System.out.println("\n== Guest ==");
            System.out.println("1) Cerca ristoranti");
            System.out.println("0) Indietro");
            System.out.print("> ");
            String s = in.nextLine().trim();
            if ("1".equals(s)) cercaRistorantiFlow(false, null);
            else if ("0".equals(s)) return;
            else System.out.println("Scelta non valida.");
        }
    }

    // ==== MENU CLIENTE ====
    
    private static void menuCliente(Utente u) {
        while (true) {
            System.out.println("\n== Menu Cliente ("+u.username+") ==");
            System.out.println("1) Cerca ristoranti");
            System.out.println("2) Visualizza preferiti");
            System.out.println("3) Aggiungi preferito (ID ristorante)");
            System.out.println("4) Modifica nota preferito");
            System.out.println("5) Rimuovi preferito");
            System.out.println("6) Aggiungi recensione");
            System.out.println("7) Modifica recensione");
            System.out.println("8) Elimina recensione");
            System.out.println("9) Visualizza le mie recensioni");
            System.out.println("0) Logout");
            System.out.print("> ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1": cercaRistorantiFlow(true, u); break;
                case "2": {
                    System.out.println(db.visualizzaPreferitiTable(u.username));
                    break;
                }
                case "3": {
                    int rid = leggiInt("ID ristorante da aggiungere ai preferiti: ");
                    boolean ok = db.aggiungiPreferito(u.username, rid);
                    System.out.println(ok ? "Aggiunto ai preferiti." : "Impossibile aggiungere (ID errato o già presente).");
                    break;
                }
                case "4": {
                    int rid = leggiInt("ID ristorante preferito: ");
                    System.out.print("Nuova nota (può essere vuota): ");
                    String nota = in.nextLine();
                    boolean ok = db.aggiornaNotaPreferito(u.username, rid, nota);
                    System.out.println(ok ? "Nota aggiornata." : "Preferito non trovato.");
                    break;
                }
                case "5": {
                    int rid = leggiInt("ID ristorante da rimuovere dai preferiti: ");
                    boolean ok = db.rimuoviPreferito(u.username, rid);
                    System.out.println(ok ? "Rimosso dai preferiti." : "Preferito non trovato.");
                    break;
                }
                case "6": { // aggiungi recensione
                    int rid = leggiInt("ID ristorante: ");
                    int st = leggiIntRange("Stelle (1-5): ", 1, 5);
                    System.out.print("Testo: ");
                    String testo = in.nextLine();
                    db.aggiungiRecensione(u.username, rid, st, testo);
                    System.out.println("Recensione aggiunta.");
                    break;
                }
                case "7": { // modifica recensione
                    int id = leggiInt("ID recensione: ");
                    int st = leggiIntRange("Nuove stelle (1-5): ", 1, 5);
                    System.out.print("Nuovo testo: ");
                    String t = in.nextLine();
                    boolean ok = db.modificaRecensione(u.username, id, st, t);
                    System.out.println(ok ? "Modificata." : "Impossibile modificare (ID errato o non tua).");
                    break;
                }
                case "8": { // elimina recensione
                    int id = leggiInt("ID recensione: ");
                    boolean ok = db.eliminaRecensione(u.username, id);
                    System.out.println(ok ? "Eliminata." : "Impossibile eliminare (ID errato o non tua).");
                    break;
                }
                case "9": {
                    visualizzaRecensioniCliente(u);
                    break;
                }
                case "0": return;
                default: System.out.println("Scelta non valida.");
            }
        }
    }


    // ==== MENU RISTORATORE ====
    private static void menuRistoratore(Utente u) {
        while (true) {
            System.out.println("\n== Menu Ristoratore ("+u.username+") ==");
            System.out.println("1) Aggiungi ristorante");
            System.out.println("2) Visualizza riepilogo recensioni");
            System.out.println("3) Visualizza recensioni di un ristorante");
            System.out.println("4) Rispondi a una recensione");
            System.out.println("0) Logout");
            System.out.print("> ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1": aggiungiRistoranteFlow(u); break;
                case "2": System.out.println(db.visualizzaRiepilogo(u.username)); break;
                case "3": {
                    int rid = leggiInt("ID ristorante: ");
                    if (!db.ristoranti.containsKey(rid)) { System.out.println("Ristorante inesistente."); break; }
                    System.out.println(db.visualizzaRecensioni(rid));
                    break;
                }
                case "4": {
                    int id = leggiInt("ID recensione: ");
                    System.out.print("Risposta: "); String resp = in.nextLine();
                    System.out.println(db.rispostaRecensioni(u.username, id, resp) ? "Risposta inserita." : "Operazione non permessa (ID errato o non tuo ristorante o già risposto).");
                    break;
                }
                case "0": return;
                default: System.out.println("Scelta non valida.");
            }
        }
    }

    // ==== Flows ====
    private static void cercaRistorantiFlow(boolean logged, Utente u) {
        System.out.println("\n== Ricerca Ristoranti ==");
        String loc = leggiNonVuota("Paese o Città (obbligatorio): ");
        System.out.print("Tipo cucina (invio per qualsiasi): "); String cucina = emptyToNull(in.nextLine());
        Double pmin = parseDoubleOrNullPrompt("Prezzo minimo (invio per nessuno): ");
        Double pmax = parseDoubleOrNullPrompt("Prezzo massimo (invio per nessuno): ");
        Boolean del = parseBoolOrNullPrompt("Delivery (si/no/invio): ");
        Boolean pre = parseBoolOrNullPrompt("Prenotazione online (si/no/invio): ");
        Double med = parseDoubleOrNullPrompt("Media minima stelle (invio per nessuna): ");

        List<Ristorante> res = db.cercaRistorante(loc, cucina, pmin, pmax, del, pre, med);
        if (res.isEmpty()) { System.out.println("Nessun risultato."); return; }

        // Ordinamenti
        System.out.print("Ordina per (1=Prezzo 2=Media 3=Nome 0=Nessuno): ");
        String ord = in.nextLine().trim();
        if ("1".equals(ord)) res = db.ordinaPerPrezzo(res);
        else if ("2".equals(ord)) res = db.ordinaPerMediaRecensioni(res);
        else if ("3".equals(ord)) res = db.ordinaPerNome(res);

        int choice = stampaRistoranti(res);
        if (choice <= 0) return;

        Ristorante rSel = db.ristoranti.get(choice);
        if (rSel == null) { System.out.println("ID non valido."); return; }

        System.out.println(db.visualizzaRistorante(rSel));
        System.out.println("-- Recensioni --");
        System.out.println(db.visualizzaRecensioni(rSel.id));

        if (logged && u != null && u.ruolo == Ruolo.CLIENTE) {
            System.out.print("Aggiungere ai preferiti? (y/N): ");
            String ans = in.nextLine().trim().toLowerCase();
            if (ans.equals("y") || ans.equals("s")) {
                if (db.aggiungiPreferito(u.username, rSel.id)) System.out.println("Aggiunto ai preferiti.");
            }
        }
    }

    /** Stampa lista ristoranti in tabella e chiede un ID per dettagli. */
    private static int stampaRistoranti(List<Ristorante> res) {
        List<String> h = Arrays.asList("ID","Nome","Luogo","Prezzo","Media","Cucina","Servizi");
        List<List<String>> rows = new ArrayList<>();
        for (Ristorante r : res) {
            rows.add(Arrays.asList(
                    String.valueOf(r.id),
                    r.nome,
                    r.citta+" ("+r.nazione+")",
                    r.prezzoMedio+"€",
                    String.format(java.util.Locale.US,"%.2f", db.mediaRistorante(r.id)),
                    r.tipoCucina,
                    (r.delivery?"Delivery ":"")+(r.prenotazione?"Prenotaz.":"")
            ));
        }
        System.out.println(AsciiTable.render(h, rows));
        return leggiInt("ID ristorante per dettagli (0 per uscire): ");
    }

    /** Flow guidato per l'aggiunta di un ristorante (ristoratore). */
    private static void aggiungiRistoranteFlow(Utente u) {
        System.out.println("\n== Aggiungi Ristorante ==");
        String nome = leggiNonVuota("Nome: ");
        String naz = leggiNonVuota("Nazione: ");
        String citta = leggiNonVuota("Città: ");
        String indir = leggiNonVuota("Indirizzo: ");
        double lat = leggiDouble("Latitudine: ");
        double lon = leggiDouble("Longitudine: ");
        double prezzo = leggiDouble("Prezzo medio: ");
        boolean del = leggiBoolean("Delivery (si/no): ");
        boolean pre = leggiBoolean("Prenotazione online (si/no): ");
        String cucina = leggiNonVuota("Tipo cucina: ");

        Ristorante r = db.aggiungiRistorante(u.username, nome, naz, citta, indir, lat, lon, prezzo, del, pre, cucina);
        System.out.println("Creato ristorante con ID: " + r.id);
    }

    // ==== Nuova funzionalità: visualizzare ristoranti recensiti dal cliente ====
    private static void visualizzaRecensioniCliente(Utente u) {
        System.out.println("\n== Le mie recensioni ==");
        Map<Integer, List<Recensione>> perRisto = new LinkedHashMap<>();
        for (Recensione r : db.recensioni.values()) {
            if (r.username.equals(u.username)) {
                perRisto.computeIfAbsent(r.ristoranteId, k -> new ArrayList<>()).add(r);
            }
        }
        if (perRisto.isEmpty()) { System.out.println("Non hai ancora inserito recensioni."); return; }

        List<String> h = Arrays.asList("ID","Nome","Luogo","#Recensioni Mie","Media Totale");
        List<List<String>> rows = new ArrayList<>();
        for (Map.Entry<Integer, List<Recensione>> e : perRisto.entrySet()) {
            Ristorante r = db.ristoranti.get(e.getKey());
            if (r == null) continue;
            rows.add(Arrays.asList(
                String.valueOf(r.id),
                r.nome,
                r.citta + " (" + r.nazione + ")",
                String.valueOf(e.getValue().size()),
                String.format(java.util.Locale.US, "%.2f", db.mediaRistorante(r.id))
            ));
        }
        System.out.println(AsciiTable.render(h, rows));

        int rid = leggiInt("ID ristorante per vedere le TUE recensioni (0 per uscire): ");
        if (rid == 0) return;
        if (!perRisto.containsKey(rid)) { System.out.println("Non hai recensioni per questo ristorante."); return; }

        List<String> h2 = Arrays.asList("ID Rec.", "Stelle", "Testo", "Risposta");
        List<List<String>> rows2 = new ArrayList<>();
        for (Recensione r : perRisto.get(rid)) {
            rows2.add(Arrays.asList(String.valueOf(r.id), String.valueOf(r.stelle), r.testo, r.risposta==null?"":r.risposta));
        }
        System.out.println(AsciiTable.render(h2, rows2));
    }

    // ==== Helpers di input robusti ====
    private static int leggiInt(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            String s = in.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero intero valido.");
            }
        }
    }

    private static int leggiIntRange(String messaggio, int min, int max) {
        while (true) {
            int v = leggiInt(messaggio);
            if (v < min || v > max) System.out.println("Valore fuori range ("+min+"-"+max+").");
            else return v;
        }
    }

    private static double leggiDouble(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            String s = in.nextLine().trim();
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                System.out.println("Inserisci un numero (es. 45.2).");
            }
        }
    }

    private static boolean leggiBoolean(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("true") || s.equals("t") || s.equals("si") || s.equals("s") || s.equals("y")) return true;
            if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n")) return false;
            System.out.println("Inserisci true/false (o s/si/n/no).");
        }
    }

    private static String leggiNonVuota(String messaggio) {
        while (true) {
            System.out.print(messaggio);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Il valore non può essere vuoto.");
        }
    }

    private static Integer parseIntOrNullPrompt(String msg) {
        while (true) {
            System.out.print(msg);
            String s = in.nextLine().trim();
            if (s.isEmpty()) return null;
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.println("Numero non valido oppure lascia vuoto."); }
        }
    }
    private static Double parseDoubleOrNullPrompt(String msg) {
        while (true) {
            System.out.print(msg);
            String s = in.nextLine().trim();
            if (s.isEmpty()) return null;
            try { return Double.parseDouble(s); }
            catch (NumberFormatException e) { System.out.println("Numero non valido oppure lascia vuoto."); }
        }
    }
    private static Boolean parseBoolOrNullPrompt(String msg) {
        while (true) {
            System.out.print(msg);
            String s = in.nextLine().trim().toLowerCase();
            if (s.isEmpty()) return null;
            if (s.equals("true") || s.equals("t") || s.equals("si") || s.equals("s") || s.equals("y")) return true;
            if (s.equals("false") || s.equals("f") || s.equals("no") || s.equals("n")) return false;
            System.out.println("Valore non valido (usa true/false, si/no oppure lascia vuoto).");
        }
    }

    private static String emptyToNull(String s) { return s==null||s.trim().isEmpty()?null:s.trim(); }
}
