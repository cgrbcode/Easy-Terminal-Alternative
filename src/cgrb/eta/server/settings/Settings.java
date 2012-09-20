/* * Copyright 2012 Oregon State University.
 * All Rights Reserved. 
 *  
 * Permission to use, copy, modify, and distribute this software and its 
 * documentation for educational, research and non-profit purposes, without fee, 
 * and without a written agreement is hereby granted, provided that the above 
 * copyright notice, this paragraph and the following three paragraphs appear in 
 * all copies. 
 *
 * Permission to incorporate this software into commercial products may be 
 * obtained by contacting OREGON STATE UNIVERSITY Office for 
 * Commercialization and Corporate Development.
 *
 * This software program and documentation are copyrighted by OREGON STATE
 * UNIVERSITY. The software program and documentation are supplied "as is", 
 * without any accompanying services from the University. The University does 
 * not warrant that the operation of the program will be uninterrupted or errorfree. 
 * The end-user understands that the program was developed for research 
 * purposes and is advised not to rely exclusively on the program for any reason. 
 *
 * IN NO EVENT SHALL OREGON STATE UNIVERSITY BE LIABLE TO ANY PARTY 
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL
 * DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS 
 * SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE OREGON STATE  
 * UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * OREGON STATE UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE AND ANY 
 * STATUTORY WARRANTY OF NON-INFRINGEMENT. THE SOFTWARE PROVIDED 
 * HEREUNDER IS ON AN "AS IS" BASIS, AND OREGON STATE UNIVERSITY HAS 
 * NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 * ENHANCEMENTS, OR MODIFICATIONS. 
 * 
 */
package cgrb.eta.server.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;

public class Settings {
	private static Settings instance;
	private static File settingsFile;

	public static Settings getInstance() {
		if (instance == null) {
			String rootDir= Settings.class.getProtectionDomain().getCodeSource().getLocation().getFile().replace(Settings.class.getName().replaceAll("\\.", "/"), "").replace("classes/","").replaceAll("\\.class", "");
			if(rootDir.contains("jar")){
				rootDir=rootDir.substring(0,rootDir.lastIndexOf('/'));
			}
			settingsFile=new File(rootDir+"/settings");
			instance = new Settings();
		}
		return instance;
	}
	public static Settings getInstance(File file) {
		settingsFile=file;
		if (instance == null) {
			instance = new Settings();
		}
		return instance;
	}
	private HashMap<String, Setting> settings = new HashMap<String, Setting>();

	private Settings() {
		load();
	}

	public Setting getSetting(String name) {
		Setting s = settings.get(name);
		if (s == null) {
			s = new Setting();
			settings.put(name, s);
		}
		return s;
	}

	public Setting getSetting(String name, String value) {
		Setting s = settings.get(name);
		if (s == null) {
			s = new Setting(value);
			settings.put(name, s);
			save();
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (!settingsFile.exists()) {
			try {
				settingsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("settings file couldn't be created at:" + settingsFile.getAbsolutePath());
			}
		}
		try {
			FileInputStream fin = new FileInputStream(settingsFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			settings = (HashMap<String, Setting>) ois.readObject();
			ois.close();
			fin.close();
			//now save it in the new format
			save();
		} catch (Exception e) {
			//okay this must be the newer type of settings file, read it in as just text
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(settingsFile));
			String line;
			while((line=reader.readLine())!=null){
				if(line.contains(":")){
					String[] temp=line.split(":");
					if(temp.length>1)
					settings.put(temp[0], new Setting(temp[1]));
				}
			}
			reader.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
	}

	public String putSetting(String name, Setting set) {
		settings.put(name, set);
		return save();
	}

	public String save() {
		if (!settingsFile.exists()) {
			try {
				settingsFile.createNewFile();
			} catch (IOException e) {
				return "settings couldn't be created:" + settingsFile.getAbsolutePath();
			}
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile));
			Iterator<String> it = settings.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				writer.write(key+":"+settings.get(key).getStringValue());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			return "counldn't save settings file";
		}
		return "";
	}
}
