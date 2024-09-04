package Management;

import Exceptions.CarrelloVuotoException;
import Exceptions.ProdottoNonTrovatoException;
import Products.ProdottoElettronicoUtente;
import Utility.CarrelloReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import Exceptions.ExceptionHandler;

public class Carrello {

	HashSet< ProdottoElettronicoUtente > carrello;

	public Carrello(){
		carrello = new HashSet <>();
	}


	public void aggiungiProdotto ( ProdottoElettronicoUtente prodotto, int quantita) throws ProdottoNonTrovatoException, IOException {
		Optional < ProdottoElettronicoUtente > toAdd = carrello.stream()
				.filter(p -> p.getId() == prodotto.getId())
				.findFirst();
		if (toAdd.isPresent()){
			incrementaQuantita(prodotto.getId(), quantita);
			CarrelloReader.aggiornaCarrello(carrello);
		}
		else {
			prodotto.setQuantitaCarrello(quantita);
			CarrelloReader.aggiungiProdottoAlCarrello(prodotto);
			carrello = CarrelloReader.leggiCarrelloDaFile();
		}

	}

	public ProdottoElettronicoUtente ricercaPerId ( int id) throws ProdottoNonTrovatoException{
		return carrello.stream()
				.filter(p -> p.getId() == id)
				.findFirst().orElseThrow(() -> new ProdottoNonTrovatoException("Nessuna corrispondenza"));
	}

	public Set < ProdottoElettronicoUtente > ricercaPerMarca ( String marca) throws ProdottoNonTrovatoException{
		Set< ProdottoElettronicoUtente > tmp = carrello.stream()
				.filter(p -> p.getMarca().equalsIgnoreCase( marca))
				.collect(Collectors.toSet());
		if(tmp.isEmpty()){
			throw new ProdottoNonTrovatoException("Nessuna corrispondenza per Marca");
		}
		return tmp;
	}

	public Set< ProdottoElettronicoUtente > ricercaPerModello ( String modello) throws ProdottoNonTrovatoException{
		Set< ProdottoElettronicoUtente > tmp = carrello.stream()
				.filter(p -> p.getModello().equalsIgnoreCase(modello)).collect(Collectors.toSet());
		if(tmp.isEmpty()){
			throw new ProdottoNonTrovatoException("Nessuna corrispondenza per Modello");
		}
		return tmp;
	}

	public Set< ProdottoElettronicoUtente > ricercaPerPrezzoVendita ( double prezzo ) throws ProdottoNonTrovatoException {
		Set<ProdottoElettronicoUtente> res = carrello.stream()
				.filter(p -> p.getPrezzoVendita() == prezzo)
				.collect(Collectors.toSet());

		if(res.isEmpty()) throw new ProdottoNonTrovatoException("Nessuna corrispondenza");
		return res;
	}

	public Set< ProdottoElettronicoUtente > ricercaPerRange ( double prezzoMin, double prezzoMax) throws ProdottoNonTrovatoException{
		Set< ProdottoElettronicoUtente > tmp = carrello.stream()
				.filter(p -> p.getPrezzoVendita() > prezzoMin && p.getPrezzoVendita() < prezzoMax )
				.collect(Collectors.toSet());
		if(tmp.isEmpty()){
			throw new ProdottoNonTrovatoException("Nessuna corrispondenza trovata per range di prezzo");
		}
		return tmp;
	}

	public Set< ProdottoElettronicoUtente > ricercaPerTipo ( String tipo) throws ProdottoNonTrovatoException {
		Set <ProdottoElettronicoUtente> res = carrello.stream()
				.filter(p -> p.getTipoElettronico().name().equals(tipo))
				.collect(Collectors.toSet());

		if(res.isEmpty()) throw new ProdottoNonTrovatoException("Nessuna corrispondenza");
		return res;
	}

	public void stampaCarrello (){
		if(carrello.isEmpty()){
			System.out.println("Non ci sono articoli nel carrello");
		}else {
			System.out.println("Articoli nel carrello: " + carrello);
		}
	}

	public void rimozioneTramiteId(int id, int quantita) throws ProdottoNonTrovatoException, IOException {
		ProdottoElettronicoUtente prdToRemove = ricercaPerId(id);
		decrementaQuantita(id,quantita);
		CarrelloReader.aggiornaCarrello(carrello);
		if(ricercaPerId(id).getQuantitaCarrello() <= 0){
			CarrelloReader.rimuoviProdottoCarrello(prdToRemove);
			carrello = CarrelloReader.leggiCarrelloDaFile();
		}
	}

	public double calcoloTot() throws CarrelloVuotoException {
		double prezzoTot = 0.0;

		if ( carrello.isEmpty()) throw new CarrelloVuotoException("Non ci sono articoli nel carrello");

		for( ProdottoElettronicoUtente dispositivo : carrello){
			prezzoTot += dispositivo.getPrezzoVendita() * dispositivo.getQuantitaCarrello();
		}
		return prezzoTot;
	}

	public void svuotaCarrello() throws IOException {
		carrello.clear();
		CarrelloReader.aggiornaCarrello(carrello);
	}


	public void concludiAcquisto(  ) throws CarrelloVuotoException, IOException {
		if(carrello.isEmpty()) throw new CarrelloVuotoException("Non ci sono articoli nel carrello");
		System.out.println("Si è sicuro di voler concludere l'acquisto?");
		stampaCarrello();
		System.out.println(calcoloTot());
		System.out.println("Inserire si per continuare o no per annullare");
		Scanner sc = new Scanner(System.in);
		String conferma = sc.nextLine();
		sc.close();
		if(conferma.equalsIgnoreCase("si")){
			System.out.println("Acquisto effettuato, torna a trovarci!");
			svuotaCarrello();
		}else if(conferma.equalsIgnoreCase("no")){
			System.out.println("Acquisto annullato");
		}else System.err.println("Comando non riconosciuto");
	}

	public Set < ProdottoElettronicoUtente > getCarrello() {
		return carrello;
	}

	public void incrementaQuantita(int id, int amount) throws ProdottoNonTrovatoException, IOException {
		ProdottoElettronicoUtente prodotto = ricercaPerId(id);
		int nuovaQuantita = prodotto.getQuantitaCarrello() + amount;
		prodotto.setQuantitaCarrello(nuovaQuantita);
		CarrelloReader.aggiornaCarrello(carrello);
	}

	public void decrementaQuantita (int id, int amount) throws ProdottoNonTrovatoException, IOException {
		ProdottoElettronicoUtente prodotto = ricercaPerId(id);
		int quantita = prodotto.getQuantitaCarrello() - amount;
		prodotto.setQuantitaCarrello(quantita);
		CarrelloReader.aggiornaCarrello(carrello);
	}
}
