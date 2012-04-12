/* IJA - Simulator Petriho siti
 * xcupak04
 * xdujic01
 */

package protocol;

import java.net.Socket;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;

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
			/*
			int len;
			len = dis.readInt();
			byte [] msg = new byte [len];
			dis.read(msg);
			*/
			//return new String(msg);
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
			/*
			byte [] msgBytes = msg.getBytes();  
			int len = msgBytes.length;
			dos.writeInt(len);
			dos.write(msgBytes);
			*/
			dos.writeUTF(msg);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException
	{
		socket.close();
		socket = null;
		dis = null;
		dos = null;
	}
}