/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package client;

import java.io.IOException;

import java.net.ConnectException;
import java.net.Socket;

// grafika
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.SwingUtilities;

import protocol.Protocol;
import client.petrinet.GPlace;


/**
 * Trida klienta.
 * Zatim velmi provizorni. Klient se pripoji na server, odesle par zprav a skonci.
 */
public class Client implements Runnable
{

	/** Implicitni hostname pripojeni */
	static final String DEFAULT_HOSTNAME = "localhost";
	/** Implicitni cislo portu */
	static final int DEFAULT_PORT = 3030;
	/** Nazev aplikace */
	static final String APPLICATION_TITLE = "IJA - Simulátor Petriho sítí";
	/** Implicitni sirka okna */
	public final int DEFAULT_WIDTH = 800;
	/** Implicitni vyska okna */
	public final int DEFAULT_HEIGTH = 600;
	
	/** Kazdy klient se bude dorozumivat se serverem skrz Protocol */
	protected Protocol protocol;
	
	protected JFrame window;
	protected JFrame dialog;

	/**
	 * Konstruktor.
	 */
	public Client()
	{
		// vytvoreni hlavniho okna
		window = new JFrame(APPLICATION_TITLE);
		window.setSize(400, 400);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("Soubor");
		JMenuItem openFile = new JMenuItem("Otevřít");
		JMenuItem saveFile = new JMenuItem("Uložit");
		openFile.setEnabled(false);
		saveFile.setEnabled(false);
		menuFile.add(openFile);
		menuFile.add(saveFile);
		menuFile.add(new JSeparator());
		
		JMenuItem konecMenu = new JMenuItem("Konec");
		konecMenu.addActionListener(new KonecTlacitko());
		menuFile.add(konecMenu);

		JMenu menuServer = new JMenu("Server");
		JMenuItem pripojit = new JMenuItem("Připojit");
		pripojit.addActionListener(new ClientConnect());
		menuServer.add(pripojit);

		JMenu menuHelp = new JMenu("Nápověda");
		menuHelp.setEnabled(false);

		menuBar.add(menuFile);
		menuBar.add(menuServer);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(menuHelp);
		window.setJMenuBar(menuBar);
		
		// zobrazit a hura
		window.setVisible(true);
	}

	/**
	 * Reakce na tlacitko ukonceni.
	 */
	class KonecTlacitko implements ActionListener
	{
		public void actionPerformed(ActionEvent event	) {
			System.out.println("Ted by to melo chcipnout!");
			dialog.dispose();
		}
	}

	/**
	 * Udalost pripojeni.
	 */
	class ClientConnect implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			// TODO: vytvorit okno s promptem na login a heslo
			// na OK tlacitko pripojit
			JFrame prompt = new JFrame("Připojit se ...");
			dialog = prompt;
			prompt.setLayout(new GridLayout(5, 2));
			prompt.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			prompt.setAlwaysOnTop(true);
			prompt.setResizable(false);
			//prompt.setLocationRelativeTo(null);

			JTextField addr = new JTextField(20);
			JTextField port = new JTextField(5);
			JTextField logn = new JTextField(20);
			JPasswordField pass = new JPasswordField();

			JLabel addrLab = new JLabel("Adresa:");
			JLabel portLab = new JLabel("Port:");
			JLabel textLab = new JLabel("Login:");
			JLabel passLab = new JLabel("Heslo:");

			prompt.add(addrLab);
			prompt.add(addr);
			prompt.add(portLab);
			prompt.add(port);
			prompt.add(textLab);
			prompt.add(logn);
			prompt.add(passLab);
			prompt.add(pass);

			JButton confirm = new JButton("Připojit");
			confirm.addActionListener(new ClientReallyWannaConnect());
			JButton cancel = new JButton("Zrušit");
			cancel.addActionListener(new KillMePlease());

			prompt.add(confirm);
			prompt.add(cancel);

			prompt.setVisible(true);
			prompt.setSize(400, 100);

			prompt.pack();
		}
	}

	/**
	 * Uzavre okynko.
	 */
	class KillMePlease implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			dialog.dispose();
		}
	}

	/**
	 * Tohle se stane po potvrzeni tlacitka pripojit.
	 */
	class ClientReallyWannaConnect implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			connect();
		}
	}

	/**
	 * Tak tohle pobezi v tom threadu. Asi teda smycka prijimajici zpravy.
	 */
	public void run()
	{
		//window.set
		Container cont = window.getContentPane();
		// TODO: tady to kresleni funguje nejak uplne blbe, 
		cont.setLayout(new GridLayout(4,4));
		cont.add(new GPlace(0, 0));
		cont.add(new GPlace(100, 0));
		window.validate();
	}

	/**
	 * Pripojeni na server.
	 */
	public void connect()
	{
		Socket sock;
		try {
			sock = new Socket(DEFAULT_HOSTNAME, DEFAULT_PORT);
			protocol = new Protocol(sock);
			
		} catch (ConnectException e){ 
			System.err.println(e.getMessage());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

		System.out.println("Chci se pripojit na " + DEFAULT_PORT);
		login();
	}
	
	/**
	 * Prihlaseni na server.
	 */
	public void login()
	{
		protocol.sendMessage("<login> <name>alfa</name> \n <password>beta</password> </login>");
	}

	public void logout()
	{
		protocol.sendMessage("<logout/>");
	}

	/**
	 * Spousteci bod klienta.
	 */
	public static void main(String[] args)
	{
        System.setProperty("file.encoding", "UTF-8");
		SwingUtilities.invokeLater(new Client());
	}

}
