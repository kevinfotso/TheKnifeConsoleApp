package theknife;

/** Modello dati per recensione. */
public class Recensione {
    public int id;
    public int ristoranteId;
    public String username; // autore
    public int stelle; // 1..5
    public String testo;
    public String risposta; // del ristoratore

    public Recensione(int id, int ristoranteId, String username, int stelle, String testo, String risposta) {
        this.id = id;
        this.ristoranteId = ristoranteId;
        this.username = username;
        this.stelle = stelle;
        this.testo = testo;
        this.risposta = risposta;
    }
}
