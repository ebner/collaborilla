/*
 $Id: $
 
 This file is part of the project Collaborilla (http://collaborilla.sf.net)
 Copyright (c) 2006 Hannes Ebner
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package se.kth.nada.kmr.collaborilla.util;

import java.util.Properties;
import java.io.*;

/**
 * Provides easy access to configuration files. Basically wraps around
 * java.util.Properties
 * 
 * @author Hannes Ebner
 * @see java.util.Properties
 */
public class Configuration {

    private Properties properties = new Properties();

    private String filePath;

    private String comment;

    private boolean xmlFormat = false;

    /**
         * Initializes private fields.
         * 
         * @param filePath
         *                Filename (including path) to the configuration
         *                file.&nbsp;If given without name, the file will be
         *                taken from the current working directory.
         */
    public Configuration(String filePath) {
	this.filePath = filePath;
    }

    /**
         * Initializes private fields.
         * 
         * @param filePath
         *                Filename (including path) to the configuration
         *                file.&nbsp;If given without name, the file will be
         *                taken from the current working directory.
         * @param xml
         *                Decides about the format: XML or normal.
         */
    public Configuration(String filePath, boolean xml) {
	this(filePath);
	this.xmlFormat = xml;
    }

    /**
         * Initializes private fields.
         * 
         * @param filePath
         *                Filename (including path) to the configuration
         *                file.&nbsp;If given without name, the file will be
         *                taken from the current working directory.
         * @param xml
         *                Decides about the format: XML or normal.
         * @param comment
         *                Heading in the configuration file.
         */
    public Configuration(String filePath, boolean xml, String comment) {
	this(filePath, xml);
	this.comment = comment;
    }

    /**
         * Reads all properties from a file.
         * 
         * @throws FileNotFoundException
         * @throws IOException
         */
    public void load() throws FileNotFoundException, IOException {
	FileInputStream inFile = new FileInputStream(filePath);

	if (this.xmlFormat) {
	    // just >= JDK1.5
	    // this.properties.loadFromXML(inFile);
	} else {
	    this.properties.load(inFile);
	}

	inFile.close();
    }

    /**
         * Writes all properties to a file.
         * 
         * @throws IOException
         */
    public void save() throws IOException {
	FileOutputStream outFile = new FileOutputStream(filePath);

	if (this.xmlFormat) {
	    // just >= JDK1.5
	    // this.properties.storeToXML(outFile, this.comment);
	} else {
	    this.properties.store(outFile, this.comment);
	}

	outFile.close();
    }

    /**
         * Gets the value of a property.
         * 
         * @param key
         *                Name of the property
         * @return Value of the property
         */
    public String getProperty(String key) {
	return this.properties.getProperty(key);
    }

    /**
         * Gets the value of a property.
         * 
         * @param key
         *                Name of the property
         * @param defaultValue
         *                Default value in case the property does not exist
         * @return Value of the property
         */
    public String getProperty(String key, String defaultValue) {
	return this.properties.getProperty(key, defaultValue);
    }

    /**
         * Sets the value of a property.
         * 
         * @param key
         *                Name of the property
         * @param value
         *                Value of the property
         */
    public void setProperty(String key, String value) {
	this.properties.setProperty(key, value);
    }

}
