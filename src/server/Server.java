/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import protocol.Protocol;
//import server.simulator.Simulator;
//import server.Users;

/**
 * Hlavni trida serveru.
 * Pripojuje klienty, precte od nich vsechny zpravy, nic neodpovida, zatim.
 */
public class Server implements Runnable
{
	/** Implicitni cislo portu */
	static final int DEFAULT_PORT = 3030;
	
	/** Server kunikuje pres Protocol */
	protected Protocol protocol;
	/** Databaze uzivatelu */
	protected Users users; 
	
	/**
	 * Vytvoreni nove instance Serveru pro obsluhu klienta.
	 * @param clientsock Socket, na kterem je pripojeny klient.
	 */
	public Server(Socket clientsock, Users users)
	{
		this.users = users;
		// vytvoreni instance protokolu
		try {
			protocol = new Protocol(clientsock);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tak a toooto je konecne bezici zpracovavac klienta.
	 */
	@Override
	public void run()
	{
		System.out.println("Client thread "+ Thread.currentThread().getName() +" spawned...");
		
		// no a tady teda konecne budu prijimat zpravy a nejak to sezvejkavat
		String msg;
		// smycka zpracovani zprav
		while((msg = protocol.getMessage()) != null){

			// zpracovani typu zpravy
			String msgType = Protocol.getMessageType(msg);
			if(msgType.equals("login")) {
				// vycucat login a heslo a overit
				System.out.println("Uzivatel se chce prihlasit... " + msg);
			} else if(msgType.equals("register")) {
				// pro kazdou zpravu nejaka akce...
				System.out.println("Noo toz to bych ho mel registrovat ..." + msg);
			} else if(msgType.equals("...")) {
				// pro kazdou zpravu nejaka akce...
				System.out.println("Nechapu ..." + msg);
			} else {
				// Nerozumim takej zprave
			}
		}

		System.out.println("Client disconneted");
		try {
			protocol.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metoda vyzaduje prijeti jmena a hesla k autentizaci, pripadne registraci noveho uzivatele.
	 */
	protected boolean authorize() {
		return false;
	}


	/**
	 * Server se spusti bud na portu zadanem jako parametr nebo na defaultnim.
	 * Prijima klienty a vytvari pro ne instance serveru.
	 * @param args Jedinym parametrem je cislo portu, defaultne 3030
	 */
	public static void main(String[] args) throws IOException
	{
        // nacte se databaze uzivatelu
        Users users = new Users();
        users.loadUsers("users.xml");

        // vytvoreni serveroveho socketu na portu
        ServerSocket socket;
        int port = (args.length > 0)?Integer.parseInt(args[0]):DEFAULT_PORT;
        socket = new ServerSocket(port);

        System.out.println("Startuju server na " + port);
        
        // vytvoreni thread poolu
        Executor service = Executors.newCachedThreadPool();

        // Spawnovani vlaken do poolu, to se deje automagickly
        while(true) {
            Socket clientsck = socket.accept();
            service.execute(new Server(clientsck, users));
        }
	}

}