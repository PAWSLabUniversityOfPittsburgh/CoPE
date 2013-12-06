package edu.pitt.sis.paws.cope;

import java.util.*;
import java.io.*;

/**
 * Wrap a properties object:
 *      load properties from file or string
 *      get properties as String, int or double
 *      set properties
 */


public class StringProperties extends Properties
{
    /**
     * use serialVersionUID from JDK 1.1.X for interoperability
     */
     private static final long serialVersionUID = 4112578634029874840L;
	
    String propertiesFilename = "default.cfg";
    boolean loaded = false;


    public StringProperties(InputStream in) {
        if (in != null) {
            loadFrom(in);
        }
    }

    public StringProperties() {
    }


    public Properties getPropertiesObject() {
        if (loaded) {
            return this;
        }
        return null;
    }


    public boolean loadFrom(InputStream fileIn) {
        loaded = false;
        if (fileIn != null) {
            try {
                super.load(fileIn);
                fileIn.close();
                loaded = true;
            }
            catch (Exception e) {
                System.out.println("PropertiesFile.Load InputStream (" +fileIn+ "): " + e);
                loaded = false;
            }
        }
        return loaded;
    }


    /**
     * Load properties from a string.
     * First replace semicolons (;) with carriage return (\n) as a
     * convenience, so multiple properties can be defined without
     * needing to delimit with a \n.  Ie.  "varOne=true;varTwo=nope"
     * is the same as the file:
     *             varOne=true
     *             varTwo=nope
     */
    public boolean loadFrom(String propString) {
        if (propString != null) {
            String propsWithCR = propString.replace(';','\n');
            byte[] bytes = propsWithCR.getBytes();
            return loadFrom(new ByteArrayInputStream(bytes));
        }
        return false;
    }


    public boolean save() {
        try {
            FileOutputStream FileOut = new FileOutputStream(propertiesFilename);
            super.store((OutputStream)FileOut, "Property file");
            FileOut.close();
        }
        catch (Exception e) {
            System.out.println("PropertiesFile.Save(" +propertiesFilename+ "): " + e);
            return false;
        }
        return true;
    }


    public String getProperty(String PropertyName) {
        String p = super.getProperty(PropertyName);
        if (p != null) {
            p.trim();
        }
        return p;
    }


    public int getPropertyInt(String PropertyName) {
        String sVal = getProperty(PropertyName);
        int iVal = 0;
        try {
            iVal = Integer.parseInt(sVal);
        }
        catch (Exception e) {
            System.out.println("Properties.getPropertyInt(" + PropertyName+ ") failed: " + e);
        }
        return iVal;
    }


    public double getPropertyDouble(String PropertyName) {
        String sVal = getProperty(PropertyName);
        double dVal = 0;
        try {
            dVal = Double.valueOf(sVal).doubleValue();
        }
        catch (Exception e) {
            System.out.println("Properties.getPropertyInt(" + PropertyName+ ") failed: " + e);
        }
        return dVal;
    }


    public Enumeration getAllProperties() {
        if (loaded) {
            return super.propertyNames();
        }
        return null;
    }


    public void setProperty(Object key, Object value) {
        super.put(key,value);
    }


    /**
     * Return true if property is defined and has the given value.
     * Case insensitive.
     */
    public boolean hasProperty(String PropertyName, String value) {
        String p = super.getProperty(PropertyName);
        if (p != null) {
            if (p.toUpperCase().equals(value.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

}

