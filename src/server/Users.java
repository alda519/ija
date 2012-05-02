/* IJA - Simulator Petriho s	iti
 * xcupak04
 * xdujic01
 */

package server;

//import java.io.IOException;
import java.io.File;


import java.util.*;
//import java.io.*;
import org.dom4j.*;
//import org.dom4j.dom.*;
import org.dom4j.io.*;


/**
 * Trida implementuje uloziste prihlasovacich udaju.
 * Nacte loginy a hesla ze souboru, nabizi metodu overeni uzivatele.
 * Uzivatele jdou za behu pridavat.
 */
public class Users
{
	
	protected String filename;
	
    /**
     * Mapa login -: heslo
     */
    protected Map<String, String> users;

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
     */
    public void loadUsers(String filename)
    {
    	this.filename = filename;

        File input = new File(filename);

        try {
            // vytvoreni readeru
            SAXReader xmlReader = new SAXReader();
            // precteni dokumentu
            Document doc = xmlReader.read(input);

            Element root = doc.getRootElement();
            // TODO: root by mel byt users!

            Iterator elementIterator = root.elementIterator();
            // iterace pres obsah zpravy
            while(elementIterator.hasNext()){
                Element element = (Element) elementIterator.next();
                // TODO: element ma byt user!

                Iterator innerIt = element.elementIterator();

                // jeden z jich je login a druhe heslo
                Element inElem1 = (Element) innerIt.next();
                Element inElem2 = (Element) innerIt.next();

                if(inElem1.getName().equals("login")) {
                    // prvni je login
                    // TODO kontrola toho druheho
                    add(inElem1.getText(), inElem2.getText());
                } else if(inElem2.getName().equals("login")) {
                    // prvni je heslo
                    // TODO kontrola toho druheho
                    add(inElem2.getText(), inElem1.getText());
                }

                // TODO: uz by tam nemel byt zadny binec navic
                //innerIt.hasNext();
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
     * Uzivatel bude pridan jako do prave nastene databaze, tak bude
     * zapsan do souboru, nejak.
     */
    public boolean register(String name, String password)
    {
        if(users.containsKey(name)) {
            return false;
        } else {
            add(name, password);
            // TODO: zapsat i do souboru
            return true;
        }
    }
    
    /**
     * Autentizace uzivatele.
     * @param name Pozadovane jmeno k overeni
     * @param password Zadane heslo k overeni
     * @return true pokud zadany uzivatel existuje a odpovida heslo, jinak false 
     */
     public boolean authenticate(String name, String password)
     {
        if(users.containsKey(name))
            return users.get(name).equals(password);
        return false;
     }

 }