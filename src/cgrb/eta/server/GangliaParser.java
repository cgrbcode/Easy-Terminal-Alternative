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
package cgrb.eta.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cgrb.eta.server.settings.Settings;

public class GangliaParser {

	HashMap<String, String[]> machineData = new HashMap<String, String[]>();
	private static GangliaParser instance;
	Date lastRetrived;
	Object semaphore = new Object();

	public static GangliaParser getInstance() {
		return instance == null ? instance = new GangliaParser() : instance;
	}

	private GangliaParser() {
		setData();
	}
	
	public String[] getMachines(){
		String[] ret;
		synchronized (semaphore) {
			ret= new String[machineData.size()];
			Iterator<String> it = machineData.keySet().iterator();
			int on =0;
			while(it.hasNext()){
				ret[on++]=it.next();
			}
		}
		Arrays.sort(ret);
		return ret;
	}

	public String[][] getData(String[] machines) {
		String[][] ret = new String[machines.length][2];
		if (lastRetrived == null) {
			lastRetrived = new Date();
			setData();
			// the thread isn't running so start it back up
			Thread t = new Thread(new Runnable() {
				public void run() {
					boolean run=true;
					while (run) {
						synchronized (semaphore) {
							if (new Date().getTime() - lastRetrived.getTime() < 5000) {
								setData();
							}else{
								run=false;
								lastRetrived = null;
							}
						}
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			t.start();
		}
		synchronized (semaphore) {
			for (int i = 0; i < machines.length; i++) {
				ret[i] = machineData.get(machines[i]);
			}
		}
		return ret;
	}

	private void setData() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			if(Settings.getInstance().getSetting("gangServer").getStringValue().equals(""))
				return;

			Document dom = db.parse(new Socket(Settings.getInstance().getSetting("gangServer").getStringValue(), Integer.parseInt(Settings.getInstance().getSetting("gangPort").getStringValue())).getInputStream());
			Element el = dom.getDocumentElement();

			NodeList nlist = el.getElementsByTagName("CLUSTER");
			NodeList nl = ((Element) nlist.item(0)).getElementsByTagName("HOST");
			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element element = (Element) nl.item(i);
					String[] ret = new String[4];
					ret[0] = element.getAttribute("NAME");
					NodeList nodes = element.getElementsByTagName("METRIC");
					if (nodes != null) {
						for (int i2 = 0; i2 < nodes.getLength(); i2++) {
							Element metric = (Element) nodes.item(i2);
							String name = metric.getAttribute("NAME");
							if (name.equals("mem_free")) {
								ret[2] = metric.getAttribute("VAL");
							} else if (name.equals("mem_total")) {
								ret[1] = metric.getAttribute("VAL");
							} else if (name.equals("cpu_user")) {
								ret[3] = metric.getAttribute("VAL");
							}
						}
					}
					machineData.put(ret[0], ret);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
