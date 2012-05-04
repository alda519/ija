/**
 * IJA - Simulator Petriho siti
 *
 * Serverova cast aplikace.
 *
 * @author xcupak04
 * @author xdujic01
 */

package server;

import java.io.File;
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
		Document doc;
		// smycka zpracovani zprav
		while((doc = protocol.getMessage()) != null) {
			Element root = doc.getRootElement();
			String msgType = root.getName();
			if(msgType.equals("login")) {
				login(root); // prihlaseni uzivatele
			} else if(msgType.equals("register")) {
				register(root); // registrace uzivatele
			} else if(msgType.equals("petrinet")) {
				saveNet(doc); // ulozeni prijite site
			} else if(msgType.equals("netslist")) {
				sendNetsList(); // seznam siti
			} else if(msgType.equals("getnet")) {
				// odeslat konretni verzi site
			} else if(msgType.equals("sim-start")) {
				// zahajeni simulace
			} else if(msgType.equals("sim-step")) {
				// krok simulace
			} else if(msgType.equals("sim-run")) {
				// cela simulace
			} else if(msgType.equals("sim-end")) {
				// ukonceni simulace
			} else if(msgType.equals("...")) {
				// ???
			} else if(msgType.equals("...")) {
			}
		}
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
	 * Odeslani seznamu siti ulozenych na serveru klientovi.
	 */
	protected void sendNetsList() {
		File path = new File("examples/storage/");
		File files [] = path.listFiles();
		protocol.sendNetsList(files);
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

        // vytvoreni thread poolu
        Executor service = Executors.newCachedThreadPool();

        // Spawnovani vlaken do poolu, to se deje automagickly
        while(true) {
            Socket clientsck = socket.accept();
            service.execute(new Server(clientsck, users));
        }
	}
}
