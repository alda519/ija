/**
 * IJA - Simulator Petriho siti
 *
 * Serverova cast aplikace.
 *
 * @author xcupak04
 * @author xdujic01
 */

package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.dom4j.Document;
import org.dom4j.Element;
import protocol.Protocol;
//import server.simulator.Simulator;
//import server.Users;
import petrinet.PetriNet;

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
		Document doc;
		// smycka zpracovani zprav
		while((doc = protocol.getMessage()) != null) {
			//System.out.println(msg);
			Element root = doc.getRootElement();
			String msgType = root.getName();
			if(msgType.equals("login")) {
				login(root);
			} else if(msgType.equals("register")) {
				register(root);
			} else if(msgType.equals("petrinet")) {
				saveNet(doc);
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
			} else if(msgType.equals("...")) {
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
	 * Prihlaceni uzivatele.
	 * @param root root element zpravy od klienta
	 */
	protected void login(Element root) {
		String name = root.attributeValue("name");
		String password = root.attributeValue("password");
		// overeni uzivatele
		if(users.authenticate(name, password)) {
			protocol.sendOk();
		} else {
			protocol.sendError("Neexistující uživatel, nebo chybné heslo.");
		}
	}

	/**
	 * Prihlaceni uzivatele.
	 * @param root root element zpravy od klienta
	 */
	protected void register(Element root) {
		String name = root.attributeValue("name");
		String password = root.attributeValue("password");
		// snad se ho povede registrovat
		if(users.register(name, password)) {
			protocol.sendOk();
		} else {
			protocol.sendError("Nebylo možné uživatele registrovat.");
		}
	}

	/**
	 * Ulozeni prijate site.
	 * @param doc dokument se siti k ulozeni
	 */
	protected void saveNet(Document doc) {
		PetriNet pn = new PetriNet(doc);
		pn.toXML();
	}

	/**
	 * Server se spusti bud na portu zadanem jako parametr nebo na defaultnim.
	 * Prijima klienty a vytvari pro ne instance serveru.
	 * @param args Jedinym parametrem je cislo portu, defaultne 3030
	 */
	public static void main(String[] args) throws IOException
	{
        System.setProperty("file.encoding", "UTF-8");
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
