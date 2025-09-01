package theknife;

/**
 * Dati anagrafici di un utente.
 * La password è memorizzata come hash SHA-256.
 */
public class Utente {
    public final String username;
    public String passwordHash;
    public String nome;
    public String cognome;
    public String dataNascita; // yyyy-MM-dd
    public String domicilio;   // città
    public Ruolo ruolo;

    public Utente(String username, String passwordHash, String nome, String cognome, String dataNascita, String domicilio, Ruolo ruolo) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.domicilio = domicilio;
        this.ruolo = ruolo;
    }
}
