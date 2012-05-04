/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package protocol;

import java.net.Socket;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;

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
	public Document getMessage() {
		try {
			return DocumentHelper.parseText(dis.readUTF());
		} catch (DocumentException e) {
			return null;
		} catch (IOException e) {
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

	public void sendOk() {
		sendMessage("<ok />");
	}
	
	public void sendError(String expl) {
		sendMessage("<failed expl=\"" + expl + "\" />");
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
}