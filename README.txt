TheKnifeConsoleApp
-------------------------
- Salvataggio automatico CSV all'uscita
- Input robusto con gestione eccezioni
- Un'applicazione Java che simula TheFork
- Gestione di ristoranti e utenti (clienti e ristoratori)
- Sistema di recensioni e preferiti
- Interfaccia a menu testuale


# Compilazione
javac -d bin -cp src src/theknife/*.java

# Creazione JAR
jar cfe bin/TheKnife.jar theknife.TheKnife -C bin .

Run:
  cd ../bin
  java theknife.TheKnife

  oppure
  cd bin
  java -jar TheKnife.jar

Note:
  La cartella 'data' viene creata automaticamente a ../data rispetto alla cartella di esecuzione.