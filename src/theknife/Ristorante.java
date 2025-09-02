package theknife;
/**
 * Autori:
 * TCHIDJO FOTSO KEVIN - 755906 -  VA
 * Massanova Alessandro - 760228 - VA
 * Progetto: TheKnife - Console App (Lab A)
 */

/** Modello dati per ristorante. */
public class Ristorante {
    public int id;
    public String nome;
    public String nazione;
    public String citta;
    public String indirizzo;
    public double lat;
    public double lon;
    public double prezzoMedio;
    public boolean delivery;
    public boolean prenotazione;
    public String tipoCucina;
    public String proprietario; // username ristoratore

    public Ristorante(int id, String nome, String nazione, String citta, String indirizzo,
                      double lat, double lon, double prezzoMedio, boolean delivery, boolean prenotazione,
                      String tipoCucina, String proprietario) {
        this.id = id;
        this.nome = nome;
        this.nazione = nazione;
        this.citta = citta;
        this.indirizzo = indirizzo;
        this.lat = lat;
        this.lon = lon;
        this.prezzoMedio = prezzoMedio;
        this.delivery = delivery;
        this.prenotazione = prenotazione;
        this.tipoCucina = tipoCucina;
        this.proprietario = proprietario;
    }
}
