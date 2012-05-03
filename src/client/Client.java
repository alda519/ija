/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package client;

import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import protocol.Protocol;
import client.editor.Editor;
import petrinet.PetriNet;

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
	protected JTabbedPane tabs;
	
	/** Zda uz jsem pripojen */
	protected boolean connectedFlag = false;
	/**
	 * Konstruktor.
	 */
	public Client()
	{
		// vytvoreni hlavniho okna
		window = new JFrame(APPLICATION_TITLE);
		window.setSize(DEFAULT_WIDTH, DEFAULT_HEIGTH);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);

		// menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("Soubor");
		JMenuItem newFile = new JMenuItem("Nový");
		JMenuItem openFile = new JMenuItem("Otevřít");
		JMenuItem saveFile = new JMenuItem("Uložit");
		JMenuItem closeFile = new JMenuItem("Zavřít");
		newFile.addActionListener(new AddNetTab());
		openFile.addActionListener(new OpenFile());
		saveFile.addActionListener(new SaveFile());
		closeFile.addActionListener(new CloseFile());
		menuFile.add(newFile);
		menuFile.add(openFile);
		menuFile.add(saveFile);
		menuFile.add(closeFile);
		menuFile.add(new JSeparator());
		
		JMenuItem konecMenu = new JMenuItem("Konec");
		konecMenu.addActionListener(new KonecTlacitko());
		menuFile.add(konecMenu);

		JMenu menuServer = new JMenu("Server");
		connect = new JMenuItem("Připojit");
		connect.addActionListener(new ClientConnect());
		menuServer.add(connect);
		menuServer.add(new JSeparator());
		JMenuItem serDown= new JMenuItem("Stáhnou síť");
		JMenuItem serUp = new JMenuItem("Nahrát síť");
		JMenuItem serSim = new JMenuItem("Simulovat síť");
		serDown.setEnabled(false);
		serUp.setEnabled(false);
		serSim.setEnabled(false);
		menuServer.add(serDown);
		menuServer.add(serUp);
		menuServer.add(serSim);

		JMenu menuHelp = new JMenu("Nápověda");
		menuHelp.setEnabled(false);

		menuBar.add(menuFile);
		menuBar.add(menuServer);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(menuHelp);
		window.setJMenuBar(menuBar);

		tabs = new JTabbedPane();
		window.add(tabs);
		// zobrazit a hura
		window.setVisible(true);
	}

	/**
	 * Pridani nove zalozky
	 */
	public static int c = 0; // docasne pocitadlo zalozek
	class AddNetTab implements ActionListener
	{
		public void actionPerformed(ActionEvent event) {
			tabs.addTab("Síť " + c, new Editor(new PetriNet()));
			tabs.setSelectedIndex(tabs.getTabCount()-1);
			c += 1;
		}
	}

	/**
	 * Obsluha otevreni souboru z menu.
	 */
	class OpenFile implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// prompt o jmeno souboru
			JFileChooser fileChooser = new JFileChooser(".");
			int status = fileChooser.showOpenDialog(null);
			if (status == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				int i = tabs.getSelectedIndex();
				// pokud neni zadny tab, vytvori se
				if(i == -1) {
					tabs.addTab(selectedFile.getName(), new Editor(PetriNet.PetriNetFactory(selectedFile)));
				} else {
					// jinak se jen nastavi nova sit
					tabs.remove(i);
					tabs.add(new Editor(PetriNet.PetriNetFactory(selectedFile)), selectedFile.getName(), i);
					tabs.setSelectedIndex(i);
				}
		    }
		}
	}

	/**
	 * Obsluha ulozeni souboru z menu.
	 */
	class SaveFile implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			int i = tabs.getSelectedIndex();
			if(i != -1) {
				// prompt o jmeno souboru
				JFileChooser fileChooser = new JFileChooser(".");
				int status = fileChooser.showOpenDialog(null);
				if (status == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					Editor e = (Editor) tabs.getComponentAt(i);
					e.saveNet(selectedFile);
					tabs.setTitleAt(i, selectedFile.getName());
			    }
			}
		}
	}

	/**
	 * Obsluha zavreni zalozky.
	 */
	class CloseFile implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			int i = tabs.getSelectedIndex();
			if(i != -1) {
				tabs.remove(i);
			}
		}
	}

	/**
	 * Reakce na tlacitko ukonceni.
	 */
	class KonecTlacitko implements ActionListener
	{
		public void actionPerformed(ActionEvent event	) {
			window.dispose();
		}
	}

	protected JTextField addr;
	protected JTextField port;
	protected JTextField logn;
	protected JPasswordField pass;
	protected JCheckBox register;
	protected JMenuItem connect;
	/**
	 * Udalost pripojeni.
	 */
	class ClientConnect implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			// hackity hack, pokud uz jsem pripojen, nepujde to znovu
			if(connectedFlag) {
				try { protocol.close(); } catch (Exception e) {}
				connectedFlag = false;
				connect.setText("Připojit");
				return;
			}

			JFrame prompt = new JFrame("Připojit se ...");
			dialog = prompt;
			prompt.setLayout(new GridLayout(6, 2));
			prompt.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			prompt.setAlwaysOnTop(true);
			prompt.setResizable(false);
			prompt.setLocationRelativeTo(null);

			addr = new JTextField(DEFAULT_HOSTNAME, 20);
			port = new JTextField(""+DEFAULT_PORT, 5);
			logn = new JTextField("loo", 20);
			pass = new JPasswordField("pop");

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

			register = new JCheckBox("Registrovat nového uživatele");
			prompt.add(register);
			prompt.add(new JLabel("")); // hackity hack, jen vycpavka
			
			JButton confirm = new JButton("Připojit");
			confirm.addActionListener(new ClientReallyWannaConnect());
			JButton cancel = new JButton("Zrušit");
			cancel.addActionListener(new KillMePlease());

			prompt.add(confirm);
			prompt.add(cancel);

			prompt.pack();
			prompt.setVisible(true);

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
			connect(addr.getText(), Integer.parseInt(port.getText()));
		}
	}

	/**
	 * Tak tohle pobezi v tom threadu. Asi teda smycka prijimajici zpravy.
	 */
	public void run()
	{
	}

	/**
	 * Pripojeni na server.
	 */
	public void connect(String hostname, int port)
	{
		Socket sock;
		try {
			SocketAddress sockaddr = new InetSocketAddress(hostname, port);
			sock = new Socket();
			sock.connect(sockaddr, 5000);
			//sock = new Socket(sockaddr, port);
			protocol = new Protocol(sock);
		} catch (IOException e){ 
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(window, "Nepovedlo se připojit k serveru.", "Problém s připojením", JOptionPane.ERROR_MESSAGE);
			return;
		} /*catch ( e) {
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(window, "Nepovedlo se připojit k serveru.", "Problém s připojením", JOptionPane.ERROR_MESSAGE);
		}*/

		// podle zaskrtnuti checkboxu se registruje/prihlasuje
		if(register.isSelected()) {
			System.out.println("A chci se regnout!");
			register(logn.getText(), new String(pass.getPassword()));
		} else {
			System.out.println("A chci se jen prihlasit");
			login(logn.getText(), new String(pass.getPassword()));
		}
		
		// podle odpovedi se pozna, zda se prihlasit povedlo nebo ne
		String response = protocol.getMessage();
		if(Protocol.getMessageType(response).equals("ok")) {
			connect.setText("Odpojit");
			dialog.dispose();
			connectedFlag = true;
		} else {
			JOptionPane.showMessageDialog(window, "Přihlášení se nezdařilo.\n" + Protocol.getContent(response), "Chyba", JOptionPane.ERROR_MESSAGE);
			try { protocol.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Prihlaseni na server.
	 */
	public void login(String login, String password)
	{
		protocol.sendMessage("<login> <name>" + login + "</name> \n <password>" + password + "</password> </login>");
	}

	/**
	 * Registrace.
	 */
	public void register(String login, String password)
	{
		protocol.sendMessage("<register> <name>"+login+"</name> \n <password>"+password+"</password> </register>");
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
