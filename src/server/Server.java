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
 * Pripojuje klienty, precte od nich vsechny zpravy, obcas neco odpovi.
 */
public class Server implements Runnable
{
	/** Implicitni cislo portu */
	static final int DEFAULT_PORT = 3030;
	
	/** Server komunikuje pres Protocol */
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
	 * Tak a toto je konecne obsluha klienta.
	 */
	@Override
	public void run()
	{
		System.out.println("Client thread "+ Thread.currentThread().getName() +" spawned...");
		
		// no a tady teda konecne budu prijimat zpravy a nejak to sezvejkavat
		String msg;
		// smycka zpracovani zprav
		while((msg = protocol.getMessage()) != null) {
			System.out.println(msg);
			// zpracovani typu zpravy
			String msgType = Protocol.getMessageType(msg);
			if(msgType.equals("login")) {
				String name = Protocol.getProperty(msg, "name");
				String password = Protocol.getProperty(msg, "password");
				// overeni uzivatele
				if(users.authenticate(name, password)) {
					protocol.sendMessage("<ok />");
				} else {
					protocol.sendMessage("<failed>Neexistující uživatel, nebo chybné heslo.</failed>");;
				}
			} else if(msgType.equals("register")) {
				String name = Protocol.getProperty(msg, "name");
				String password = Protocol.getProperty(msg, "password");
				// snad se ho povede registrovat
				if(users.register(name, password)) {
					protocol.sendMessage("<ok />");
				} else {
					protocol.sendMessage("<failed>Nebylo možné uživatele registrovat.</failed>");
				}
			} else if(msgType.equals("...")) {
				// pro kazdou zpravu nejaka akce...
				System.out.println("Nechapu ..." + msg);
			} else {
				// Nerozumim takej zprave
			}
			// getnets, getnet, putnet, simulate, -- neco jako <simstep>cislo</>
		}

		System.out.println("Client disconneted");
		try {
			protocol.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
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