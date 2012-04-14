/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package protocol;

import java.net.Socket;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.util.Iterator;
import org.dom4j.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;

/**
 * Trida zastresujici abstrakci nad komunikaci klienta se serverem.
 * 
 */
public class Protocol
{

	Socket socket;
	DataOutputStream dos;
	DataInputStream dis;
	
	/**
	 * Konstruktor.
	 * @param socket Socket, ze ktereho se budou prijimat/odesilat zpravy.
	 * @throws IOException
	 */
	public Protocol(Socket socket) throws IOException
	{
		this.socket = socket;
		dos = new DataOutputStream(socket.getOutputStream());
		dis = new DataInputStream(socket.getInputStream());
	}
	
	/**
	 * Prijme zpravu.
	 * @return Vraci string se zpravou.
	 */
	public String getMessage()
	{
		try {
			return dis.readUTF();
		} catch (IOException e) {
			// proste uz dosla data na vstupu, tak to zamlcim a reknu, ze nic 
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * Odesle zpravu.
	 * @param msg Zprava k odeslani.
	 */
	public void sendMessage(String msg)
	{
		try {
			dos.writeUTF(msg);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ukonceni komunikace
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		socket.close();
		socket = null;
		dis = null;
		dos = null;
	}

	/**
	 * Zjisti typ zpravy.
	 * @param msg Zprava
	 * @return Nazev root elementu ze zpravy
	 */
	public static String getMessageType(String msg)
	{
		try {
			Document doc = DocumentHelper.parseText(msg);
			return doc.getRootElement().getName();
		} catch (DocumentException e) {
			// blba zprava, nerozumim
		}
		return null;
	}

	/**
	 * Vrací obsah taky property, který je hned v root.
	 * @param msg
	 * @param property
	 * @return
	 */
	public static String getProperty(String msg, String property)
	{
		try {
			Document doc = DocumentHelper.parseText(msg);
			Element root = doc.getRootElement();
			Iterator i = root.elementIterator();
			while(i.hasNext()) {
				Element element = (Element)i.next();
				if(element.getName().equals(property)) {
					return element.getText();
				}
			}
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	/**
	 * Vrací obsah root elementu
	 * @param msg
	 * @return
	 */
	public static String getContent(String msg)
	{
		try {
			Document doc = DocumentHelper.parseText(msg);
			return doc.getRootElement().getText();
		} catch (DocumentException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
}