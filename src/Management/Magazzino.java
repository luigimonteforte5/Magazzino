package Management;
import Exceptions.ExceptionHandler;
import Exceptions.ProdottoNonTrovatoException;
import Products.ProdottoElettronico;
import Utility.MagazzinoReader;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Magazzino {

    //Accetta tipi Products.ProdottoElettronico

    private Set<ProdottoElettronico> magazzino;

    public Magazzino() {
        try{
            this.magazzino = MagazzinoReader.leggiMagazzinoDaFile();
        } catch (IOException e){
            System.err.println(e.getMessage());
        }

    }

    //Ritorna il totale degli articoli presenti in magazzino.
    public int totaleProdotti(){
        return magazzino.size();
    }


    //Lista filtrata per tipo
    public Set<ProdottoElettronico> filtredBytype(String type){
       return magazzino.stream()
               .filter(d->d.getTipoElettronico().toString().equalsIgnoreCase(type))
               .collect(Collectors.toSet());
    }
    public Set<ProdottoElettronico> filtredByModel(String type){
        return magazzino.stream()
                .filter(d->d.getModello().equalsIgnoreCase(type))
                .collect(Collectors.toSet());
    }
    public Set<ProdottoElettronico> filtredByProducer(String type){
       return magazzino.stream()
               .filter(d->d.getMarca().equalsIgnoreCase(type))
               .collect(Collectors.toSet());
    }
    public Set<ProdottoElettronico> filtredBySellPrice(double price){
        return magazzino.stream()
                .filter(d->d.getPrezzoVendita() == price)
                .collect(Collectors.toSet());
    }
    // Filtrato per prezzo magazzino
    public Set<ProdottoElettronico> filtredByWhareHousePurchasePrice(double price){
        return magazzino.stream()
                .filter(d-> d.getPrezzoAcquisto() == price)
                .collect(Collectors.toSet());
    }
    public Set<ProdottoElettronico> filtredByRangePrice(double price, double secondPrice){
        return magazzino.stream()
                .filter(d->d.getPrezzoVendita() > price && d.getPrezzoVendita() < secondPrice)
                .collect(Collectors.toSet());
    }
    public void addProductToMagazzino(ProdottoElettronico dispositivo){
        boolean found = magazzino.stream().anyMatch(d->d.getId() == dispositivo.getId());
        if(found){
            dispositivo.setQuantitaMagazzino(dispositivo.getQuantitaMagazzino() + 1);
            MagazzinoReader.aggiornaMagazzino(magazzino);
        } else{
            MagazzinoReader.aggiungiProdottoAlMagazzino(dispositivo);
            magazzino = MagazzinoReader.leggiMagazzinoDaFile();
        }
    }

    public void removeProductFromMagazzino(int id){
        ExceptionHandler.handlexception(()-> { boolean isPresent = magazzino.removeIf(d->d.getId() == id);
            if (!isPresent){
                throw new ProdottoNonTrovatoException("Impossibile procedere: prodotto non trovato");
            } else {
                ProdottoElettronico tmp = filteredById(id);
                MagazzinoReader.rimuoviProdottoMagazzino(tmp);
                magazzino = MagazzinoReader.leggiMagazzinoDaFile();
            }
            return null;
        });
    }

    public ProdottoElettronico filteredById( int id){
         return ExceptionHandler.handlexception(()-> magazzino.stream()
                  .filter(d-> d.getId() == id)
                  .findFirst().orElseThrow(()-> new ProdottoNonTrovatoException("Nessuna corrispondenza nel magazzino")));
    }

    public Set<ProdottoElettronico> getMagazzino() {
        return magazzino;
    }

    public void setMagazzino(Set<ProdottoElettronico> magazzino) {

        this.magazzino = magazzino;
    }

    public void decrementaQuantita(int id, int amount){
        ProdottoElettronico prodotto = filteredById(id);
        int nuovaQuantita = prodotto.getQuantitaMagazzino() - amount;
        if (nuovaQuantita < 0) {
            throw new IllegalArgumentException("Quantità non può essere negativa");
        } else if (nuovaQuantita == 0) {
            MagazzinoReader.rimuoviProdottoMagazzino(prodotto);
            magazzino = MagazzinoReader.leggiMagazzinoDaFile(); //aggiorna set con modifica fatta al file
        } else{
            prodotto.setQuantitaMagazzino(nuovaQuantita);
            MagazzinoReader.aggiornaMagazzino(magazzino); // sovrascrive file con lista passata come parametro
        }
    }

    public void incrementaQuantita(int id, int amount){
        ExceptionHandler.handlexception(()-> {
            ProdottoElettronico prodotto = filteredById(id);
            int nuovaQuantita = prodotto.getQuantitaMagazzino() + amount;
            if (nuovaQuantita < 0) {
                throw new IllegalArgumentException("Quantità non può essere negativa");
            } else{
                prodotto.setQuantitaMagazzino(nuovaQuantita);
                MagazzinoReader.aggiornaMagazzino(magazzino);
            }
            return null;
        });
    }

    @Override
    public String toString() {
        if(magazzino == null | magazzino.isEmpty()){
            return "Magazzino vuoto";
        }
        return "Magazzino: " + magazzino;
    }
}
