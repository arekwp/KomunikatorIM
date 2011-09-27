package net.homeip.phini;

import java.util.ArrayList;

public class ClientList {

	private ArrayList<Client> clientList;

	public ClientList() {
		clientList = new ArrayList<Client>();
	}

	public void add(Client client) {
		synchronized (clientList) {
			if (isActive(client)) {
				clientList.remove(getClientIndex(client));
				
			}
			clientList.add(client);
			System.out.println("ilośc elem na lisice: " + clientList.size());
		}

	}

	public int getClientIndex(Client client) {
		synchronized (clientList) {
			for(int i = 0; i < clientList.size(); i ++)
			{
				if(clientList.get(i).getLogin().equals(client.getLogin()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	public boolean isActive(Client client) {

		synchronized (clientList) {
			for(int i = 0; i < clientList.size(); i ++)
			{
				if(clientList.get(i).getLogin().equals(client.getLogin()))
				{
					return true;
				}
			}
			return false;
		}
	}
	
	public void print() {

		synchronized (clientList) {
			System.out.println("------");
			for(int i = 0; i < clientList.size(); i ++)
			{
				System.out.println(clientList.get(i).getLogin());
			}
			System.out.println("------");
		}
	}

	public void remove(Client client) {
		if (isActive(client)) {
			synchronized (clientList) {
				clientList.remove(client);
			}

		}
	}

	public int countClients() {
		synchronized (clientList) {
			return clientList.size();
		}
	}

	public Client getClient(int i) {
		synchronized (clientList) {
			return clientList.get(i);
		}
	}

}
