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
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

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

	/** Jmeno prave prihlaseneho uzivatele. Pokud je null, neni prihlaseny. */
	protected String username;

	/** Seznam aktivnich simulaci */
	protected List<PetriNet> simulations = new ArrayList<PetriNet>();

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
			if(username == null) {
				// prihlasit a registrovat se muze jen neprihlaseny
				if(msgType.equals("login")) {
					login(root); // prihlaseni uzivatele
				} else if(msgType.equals("register")) {
					register(root); // registrace uzivatele
				}
			} else {
				// pracovat se sitemi smi jen prihlaseny
				if(msgType.equals("petrinet")) {
					saveNet(doc); // ulozeni prijite site
				} else if(msgType.equals("netslist")) {
					sendNetsList(); // seznam siti
				} else if(msgType.equals("getnet")) {
					sendNet(root); // odeslat konretni verzi site
				} else if(msgType.equals("sim-start")) {
					simStart(root); // zahajeni simulace
				} else if(msgType.equals("sim-step")) {
					simStep(root, 1);// krok simulace
				} else if(msgType.equals("sim-run")) {
					simStep(root, 100);// cela simulace
				} else if(msgType.equals("sim-end")) {
					simEnd(root);// ukonceni simulace
				} else if(msgType.equals("...")) {
					// ???
				} else if(msgType.equals("...")) {
				}
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
			username = name;
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
			username = name;
		} else {
			protocol.sendError("Nebylo možné uživatele registrovat.");
		}
	}

	/**
	 * Ulozeni prijate site.
	 * @param doc dokument se siti k ulozeni
	 */
	protected void saveNet(Document doc) {
		// nacteni site
		PetriNet pn = new PetriNet(doc);
		// nacteni jmena
		String name = pn.getName();
		// pokud neni zadano jmeno, bude vymysleno
		if(name.equals("")) {
			name = "noname";
			pn.setName("noname");
		}
		// nastaveni autora
		pn.setAuthor(username);
		// zjisteni a nastaveni verze
		File dir = new File("examples/storage/" + name);
		String version;
		if(dir.exists()) {
			version = "" + (dir.listFiles().length + 1);
		} else {
			dir.mkdir();
			version = "1";
		}
		pn.setVersion(version);
		// a konecne ulozeni
		File file = new File("examples/storage/" + name + "/" + version);
		try {
	    	FileWriter out = new FileWriter(file);
	        OutputFormat format = OutputFormat.createPrettyPrint();
	        XMLWriter writer = new XMLWriter(out, format);
	        writer.write(pn.toXML());
	    	out.close();
    	} catch (IOException e) {
    		System.err.println("Nelze soubor ulozit.");
    	}
	}

	/**
	 * Odeslani seznamu siti ulozenych na serveru klientovi.
	 */
	protected void sendNetsList() {
		File path = new File("examples/storage/");
		Document doc = DocumentHelper.createDocument();
		Element netslist = doc.addElement("netslist");
		// projit vsechny slozky - site
		for(File folder : path.listFiles()) {
			Element net = netslist.addElement("net");
			net.addAttribute("name", folder.getName());
			if(!folder.isDirectory()) {
				continue;
			}
			// a v nich soubory - verze
			for(File file : folder.listFiles()) {
				Element version = net.addElement("version");
				version.addAttribute("name", file.getName());
			}
		}
		protocol.sendDocument(doc);
	}

	/**
	 * Odeslani klientovi vybrane site.
	 * @param root root element zpravy zadajici o sit
	 */
	protected void sendNet(Element root) {
		// ziskat nazev a verzi;
		String version = root.attributeValue("version");
		String name = root.attributeValue("name");
		File file = new File("examples/storage/" + name + "/" + version);
		PetriNet pn = PetriNet.PetriNetFactory(file);
		protocol.sendDocument(pn.toXML());
	}

	/**
	 * Obsluza zadosti klienta o simulaci
	 */
	public void simStart(Element root) {
		// ziskat nazev a verzi;
		String version = root.attributeValue("version");
		String name = root.attributeValue("name");
		File file = new File("examples/storage/" + name + "/" + version);
		PetriNet pn = PetriNet.PetriNetFactory(file);
		simulations.add(pn);
		int num = simulations.size()-1;
		// TODO odeslat klientovi potvrzeni s cislem poradne!
		protocol.sendMessage("<newsim number=\"" + num + "\" />");
	}

	/**
	 * Ukonceni simulace klietem
	 * @param root element zpravy od klienta
	 */
	public void simEnd(Element root) {
		int num = Integer.parseInt(root.attributeValue("number"));
		// nahrazeni simulace null, kvuli posunu indexu nemohu jen odebrat
		simulations.remove(num);
		simulations.add(num, null);
		// ale mohu od konce odebirat null
		for(int i = simulations.size(); i > 0; --i) {
			if(simulations.get(i-1) == null) {
				simulations.remove(i-1);
			} else {
				break;
			}
		}
	}

	/**
	 * Odsimulovani `n` kroku klientem urcene simulace 
	 * @param root element zpravy
	 * @param n pocet pozadovanych kroku simulace
	 */
	public void simStep(Element root, int n) {
		int num = Integer.parseInt(root.attributeValue("number"));
		PetriNet pNet = simulations.get(num);
		// kolik kroku chci krat se zkusi udelat prechod
		for(int i = 0; i < n; ++i) {
			// pokud se nepovede najit prechod, nepujde to ani priste
			if(! pNet.stepSim() ) {
				break;
			}
		}
		// odeslani vysledneho stavu ke klientovi
		protocol.sendDocument(pNet.toXML());
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
