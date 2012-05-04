/**
 * IJA - Simulator Petriho siti
 *
 * Databaze uzivatelu a autentizace.
 *
 * @author xcupak04
 * @author xdujic01
 */

package server;

//import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import java.util.*;
import org.dom4j.*;
import org.dom4j.io.*;


/**
 * Trida implementuje uloziste prihlasovacich udaju.
 * Nacte loginy a hesla ze souboru, nabizi metodu overeni uzivatele.
 * Uzivatele jdou za behu pridavat.
 */
public class Users
{
	/** Jmeno souboru s uzivateli */
	protected String filename;
	
    /** Mapa login - heslo */
    protected Map<String, String> users;
    
    /** Dokument pro znovuulozeni pridanych uzivatelu. */
    protected Document doc;
    
    /**
     * Konstruktor
     */
    public Users()
    {
        // TODO: synchronizvana kolekce?
        users = new HashMap<String, String>();
    }

    /**
     * Nacte uzivatele ze souboru.
     * @param filename jmeno souboru s uzivateli
     */
    public void loadUsers(String filename)
    {
    	this.filename = filename;
        File input = new File(filename);
        try {
            SAXReader xmlReader = new SAXReader();
            this.doc = xmlReader.read(input);

            Element root = this.doc.getRootElement();
            if(! root.getName().equals("users"))
            	throw new DocumentException("Neplatny soubor s uzivateli");
            Iterator elementIterator = root.elementIterator();
            // iterace pres obsah dokumentu
            while(elementIterator.hasNext()){
                Element element = (Element) elementIterator.next();
                if(! element.getName().equals("user"))
                	throw new DocumentException("Neplatny soubor s uzivateli");
                
                // zjisteni jmena a hesla z atributu
                String login = element.attributeValue("login");
                String password = element.attributeValue("password");
                // pridani uzivatele do seznamu
                if(login == null || password == null)
                    throw new DocumentException("Neplatny soubor s uzivateli");
                add(login, password);
            }
        } catch(DocumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Pridani uzivatele do kolekce.
     */
    protected void add(String name, String password)
    {
        //System.out.println("Pridavam " + name + ":" + password);
        users.put(name, password);
    }

    /**
     * Registrace noveho uzivatele.
     * Uzivatel bude pridan do prave nactene databaze, tak bude zapsan do souboru na disk.
     * @param name jmeno noveho uzivatele
     * @param password heslo noveho uzivatele
     * @reutrn vraci true, pokud byl uzivatrel pridan, jinak false
     */
    public boolean register(String name, String password)
    {
        if(users.containsKey(name)) {
            return false;
        } else {
            add(name, password);
            // TODO: zapsat noveho uzivatele i do souboru
            Element root = this.doc.getRootElement();
            Element newUser = root.addElement("user");
            newUser.addAttribute("login", name);
            newUser.addAttribute("password", password);
            
    	    try {
    	    	FileWriter out = new FileWriter(this.filename);
    	        OutputFormat format = OutputFormat.createPrettyPrint();
    	        XMLWriter writer = new XMLWriter(out, format);
    	        writer.write(this.doc);
    	    	out.close();    
    	    } catch (IOException w) {
    	    	System.out.println("Sit se nepovedlo ulozit!");
    	    	return false;
    	    }
            return true;
        }
    }

    /**
     * Autentizace uzivatele.
     * @param name Pozadovane jmeno k overeni
     * @param password Zadane heslo k overeni
     * @return vraci true pokud zadany uzivatel existuje a odpovida heslo, jinak false 
     */
     public boolean authenticate(String name, String password)
     {
        if(users.containsKey(name))
            return users.get(name).equals(password);
        return false;
     }

 }