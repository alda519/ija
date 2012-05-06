/**
 * IJA - Simulator Petriho siti
 *
 * Klientska cast aplikace.
 *
 * @author xcupak04
 * @author xdujic01
 */

package client;

import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.List;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.awt.*;
import java.awt.event.*;

import protocol.Protocol;
import client.editor.Editor;
import client.editor.Theme;
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
	protected Client client;

	protected JFrame window;
	protected JFrame dialog;
	protected JTabbedPane tabs;

	/** Zda uz jsem pripojen */
	protected boolean connectedFlag = false;

	/**
	 * Vytovri okno klienta, menu, pripravi zalozky atd.
	 */
	public Client()
	{
		this.client = this;
		// vytvoreni hlavniho okna
		window = new JFrame(APPLICATION_TITLE);
		window.setSize(DEFAULT_WIDTH, DEFAULT_HEIGTH);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setLocationRelativeTo(null);

		// menu bar
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("Soubor");
		menuFile.setMnemonic('S');
		JMenuItem newFile = new JMenuItem("Nový");
		JMenuItem openFile = new JMenuItem("Otevřít");
		JMenuItem saveFile = new JMenuItem("Uložit");
		JMenuItem closeFile = new JMenuItem("Zavřít");
		newFile.addActionListener(new AddNetTab());
		newFile.setMnemonic('N');
		openFile.addActionListener(new OpenFile());
		openFile.setMnemonic('O');
		saveFile.addActionListener(new SaveFile());
		saveFile.setMnemonic('U');
		closeFile.addActionListener(new CloseFile());
		closeFile.setMnemonic('Z');
		menuFile.add(newFile);
		menuFile.add(openFile);
		menuFile.add(saveFile);
		menuFile.add(closeFile);
		menuFile.add(new JSeparator());
		
		JMenuItem theme = new JMenuItem("Změnit vzhled");
		theme.setMnemonic('v');
		theme.addActionListener(new ChangeTheme());
		menuFile.add(theme);
		menuFile.add(new JSeparator());

		JMenuItem konecMenu = new JMenuItem("Konec");
		konecMenu.setMnemonic('K');
		konecMenu.addActionListener(new KonecTlacitko());
		menuFile.add(konecMenu);

		JMenu menuServer = new JMenu("Server");
		menuServer.setMnemonic('r');
		connect = new JMenuItem("Připojit");
		connect.setMnemonic('P');
		connect.addActionListener(new ClientConnect());
		menuServer.add(connect);
		menuServer.add(new JSeparator());
		JMenuItem serDown= new JMenuItem("Stáhnou síť");
		serDown.setMnemonic('S');
		serDown.addActionListener(new ListServerNets());
		JMenuItem serUp = new JMenuItem("Nahrát síť");
		serUp.setMnemonic('N');
		serUp.addActionListener(new UploadNet());
		JMenuItem serSim = new JMenuItem("Simulovat síť");
		serSim.setMnemonic('M');
		menuServer.add(serDown);
		menuServer.add(serUp);
		menuServer.add(serSim);

		JMenu menuHelp = new JMenu("Nápověda");
		menuHelp.setMnemonic('N');
		JMenuItem helpItem = new JMenuItem("Nápověda");
		helpItem.addActionListener(new ShowHelp());
		menuHelp.add(helpItem);

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
			tabs.addTab("Síť " + c, new Editor(new PetriNet(), client));
			tabs.setSelectedIndex(tabs.getTabCount()-1);
			c += 1;
		}
	}

	/**
	 * Obsluha zmeny vzhledu.
	 */
	class ChangeTheme implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser fileChooser = new JFileChooser("examples/themes");
			fileChooser.setApproveButtonText("Otevřít");
			int status = fileChooser.showOpenDialog(null);
			if (status == JFileChooser.APPROVE_OPTION) {
				Theme.loadTheme(fileChooser.getSelectedFile());
				tabs.repaint();
			}
		}
	}
	
	/**
	 * Obsluha otevreni souboru z menu.
	 */
	class OpenFile implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// prompt o jmeno souboru
			JFileChooser fileChooser = new JFileChooser("examples");
			fileChooser.setApproveButtonText("Otevřít");
			int status = fileChooser.showOpenDialog(null);
			if (status == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				int i = tabs.getSelectedIndex();
				// pokud neni zadny tab, vytvori se
				if(i == -1) {
					tabs.addTab(selectedFile.getName(), new Editor(PetriNet.PetriNetFactory(selectedFile), client));
				} else {
					// jinak se jen nastavi nova sit
					tabs.remove(i);
					tabs.add(new Editor(PetriNet.PetriNetFactory(selectedFile), client), selectedFile.getName(), i);
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
				JFileChooser fileChooser = new JFileChooser("examples");
				fileChooser.setApproveButtonText("Uložit");
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
	 * Obsluha tlacitka pripojit z menu. Vytvori okno konfigurace prihlaseni na server.
	 */
	class ClientConnect implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			// hackity hack, pokud uz jsem pripojen, bylo tlacitko zmneno na odpojit, tedy se odpoji
			if(connectedFlag) {
				try { protocol.close(); } catch (Exception e) {}
				connectedFlag = false;
				protocol = null;
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
			confirm.addActionListener(new ProceedConnection());
			JButton cancel = new JButton("Zrušit");
			cancel.addActionListener(new CloseConnectDialog());

			prompt.add(confirm);
			prompt.add(cancel);

			prompt.pack();
			prompt.setVisible(true);
		}
	}

	/**
	 * Uzavre okynko.
	 */
	class CloseConnectDialog implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			dialog.dispose();
		}
	}

	/**
	 * Obsluha potvrzeni zadosti o pripojeni na server z konfig. okna.
	 */
	class ProceedConnection implements ActionListener
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
			sock.connect(sockaddr, 5000); // to je timeout tech 5000
			//sock = new Socket(sockaddr, port);
			protocol = new Protocol(sock);
		} catch (IOException e){ 
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(window, "Nepovedlo se připojit k serveru.", "Problém s připojením", JOptionPane.ERROR_MESSAGE);
			protocol = null;
			return;
		}

		// podle zaskrtnuti checkboxu se registruje/prihlasuje
		if(register.isSelected()) {
			protocol.sendRegister(logn.getText(), new String(pass.getPassword()));
		} else {
			protocol.sendLogin(logn.getText(), new String(pass.getPassword()));
		}

		// podle odpovedi se pozna, zda se prihlasit povedlo nebo ne
		Document response = protocol.getMessage();
		Element root = response.getRootElement();

		if(root.getName().equals("ok")) {
			connect.setText("Odpojit");
			dialog.dispose();
			connectedFlag = true;
		} else {
			JOptionPane.showMessageDialog(window, "Přihlášení se nezdařilo.\n" + root.attributeValue("expl") , "Chyba", JOptionPane.ERROR_MESSAGE);
			try {
				protocol.close();
				protocol = null;
			} catch (Exception e) {}
		}
	}

	JTree tree;
	JFrame netlist;
	/**
	 * Zobrazeni seznamu siti ze serveru.
	 */
	class ListServerNets implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			if(!connectedFlag)
				return;
			protocol.sendMeNetsList();
			Document doc = protocol.getMessage();
			Element root = doc.getRootElement();
			if(root.getName().equals("netslist")) {
				netlist = new JFrame("Sítě na serveru");
				netlist.setSize(280, 320);
				netlist.setLocationRelativeTo(null);
				netlist.setLayout(new FlowLayout());
				// sestaveni stromu siti a jejich verzi
				DefaultMutableTreeNode top = new DefaultMutableTreeNode("Seznam sítí");
				for(Element net : (List<Element>) root.elements("net")) {
					DefaultMutableTreeNode item = new DefaultMutableTreeNode(net.attributeValue("name"));
					for(Element version : (List<Element>)net.elements("version")) {
						item.add(new DefaultMutableTreeNode(version.attributeValue("name")));
					}
					top.add(item);
				}
				tree = new JTree(top);
		        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
				//JList list = new JList(new String [] {"1", "2", "3" });

				JScrollPane scpane = new JScrollPane(tree);
				scpane.setPreferredSize(new Dimension(250, 250));
				netlist.add(scpane);
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new DownloadNet());
				netlist.add(okButton);
				netlist.setResizable(false);
				netlist.setVisible(true);
			}
		}
	}

	/**
	 * Ulozeni site na server.
	 */
	class UploadNet implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(!connectedFlag)
				return;
			int i = tabs.getSelectedIndex();
			if(i == -1) {
				return;
			}
			Editor editor = (Editor) tabs.getComponentAt(i);
			protocol.sendDocument(editor.getNet().toXML());
		}
	}

	/**
	 * Obsluha udalosti zadosti o sit ze serveru.
	 */
	class DownloadNet implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if(!connectedFlag)
				return;
			DefaultMutableTreeNode select = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			// nic neni vybrano? konec
			if(select == null)
				return;
			TreeNode path [] = select.getPath();
			// pokud je vybrana verze, odesle se zadost a sit se zobrazi
			if(path.length == 3) {
				DefaultMutableTreeNode net = (DefaultMutableTreeNode) path[1];
				DefaultMutableTreeNode version = (DefaultMutableTreeNode) path[2];
				Document doc = DocumentHelper.createDocument();
				Element querry = doc.addElement("getnet");
				querry.addAttribute("name", (String)net.getUserObject());
				querry.addAttribute("version", (String)version.getUserObject());
				protocol.sendDocument(doc);
				doc = protocol.getMessage();
				// tedka tu sit vzit a napchat do editoru
				int i = tabs.getSelectedIndex();
				// pokud neni zadny tab, vytvori se
				Editor editor = new Editor(new PetriNet(doc), client);
				editor.enableSimulation();
				if(i == -1) {
					tabs.addTab((String)net.getUserObject(), editor);
				} else {
					// jinak se jen nastavi nova sit
					tabs.remove(i);
					tabs.add(editor, (String)net.getUserObject(), i);
					tabs.setSelectedIndex(i);
				}
				netlist.dispose();
			}
		}
	}

	/**
	 * Obsluha zobrazeni napovedy
	 */
	class ShowHelp implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JFrame help = new JFrame("Nápověda");
			help.setSize(500, 500);
			help.setLocationRelativeTo(null);
			help.setVisible(true);
		}
	}

	/** Ziskani komunikacniho protokolu */
	public Protocol getProtocol() {
		return this.protocol;
	}

	/**
	 * Spousteci bod klienta.
	 */
	public static void main(String[] args)
	{
		// defaultni tema je hardcoded, takze netreba nacitat
		//Theme.loadTheme(new File("examples/themes/default.xml"));
        System.setProperty("file.encoding", "UTF-8");
		SwingUtilities.invokeLater(new Client());
	}

}
