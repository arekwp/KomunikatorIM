package net.homeip.phini;

import java.util.ArrayList;

/**
 * Klasa, której zadaniem jest przechowywanie zalogowanych klientów
 * 
 * @author phini
 * 
 */
public class ClientList {

	private ArrayList<Client> clientList;

	/**
	 * Konstruktor klasy. Tworzy nowy obiekt typu ArrayList<Client>
	 */
	public ClientList() {
		clientList = new ArrayList<Client>();
	}

	/**
	 * Metoda dodająca nowego klienta do listy zalogowanych. Dodanie jest
	 * synchronizowane.
	 * 
	 * @param client
	 *            Dodawany klient.
	 */
	public void add(Client client) {
		synchronized (clientList) {
			if (isActive(client)) {
				clientList.remove(getClientIndex(client));

			}
			clientList.add(client);
			System.out.println("ilośc elem na lisice: " + clientList.size());
		}

	}

	/**
	 * Metoda zwracająca index na liście wskazanego klienta. Poszukiwanie jest
	 * synchroznizowane.
	 * 
	 * @param client
	 *            Szukany klient
	 * @return int - index szukanego klienta, jeśli klienta nie ma na liście,
	 *         zwraca -1
	 */
	public int getClientIndex(Client client) {
		synchronized (clientList) {
			for (int i = 0; i < clientList.size(); i++) {
				if (clientList.get(i).getLogin().equals(client.getLogin())) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Metoda sprawdzająca czy dany klient jest zalogowany.
	 * 
	 * @param client
	 *            Wskazany klient
	 * @return boolean - true jeśli jest zalogowany, false w przeciwnym wypadku
	 */
	public boolean isActive(Client client) {

		synchronized (clientList) {
			for (int i = 0; i < clientList.size(); i++) {
				if (clientList.get(i).getLogin().equals(client.getLogin())) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Metoda wypisująca zawartość listy zalogowanych do konsoli
	 */
	public void print() {

		synchronized (clientList) {
			System.out.println("------");
			for (int i = 0; i < clientList.size(); i++) {
				System.out.println(clientList.get(i).getLogin());
			}
			System.out.println("------");
		}
	}

	/**
	 * Metoda usuwająca wskazanego klienta z listy zalogowanych
	 * 
	 * @param client
	 *            Wskazany klient przeznaczony do usunięcia
	 */
	public void remove(Client client) {
		if (isActive(client)) {
			synchronized (clientList) {
				for (int i = 0; i < clientList.size(); i++) {
					if (clientList.get(i).getLogin().equals(client.getLogin())) {
						clientList.remove(i);
					}
				}
			}

		}
	}

	/**
	 * Zlicza zalogowanych klientów
	 * 
	 * @return int - ilość zalogowanych klientów
	 */
	public int countClients() {
		synchronized (clientList) {
			return clientList.size();
		}
	}

	/**
	 * Zwraca klienta znajdującego się pod wskazanych indeksem
	 * 
	 * @param i
	 *            indeks klienta na liście zalogowanych
	 * @return Client - klient znajdujący się pod wskazanym indeksem.
	 */
	public Client getClient(int i) {
		synchronized (clientList) {
			return clientList.get(i);
		}
	}

}
