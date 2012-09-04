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

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class QstatParser {

	public class Job {
		private String jobNumber;
		private String name;
		private String owner;
		private String pritory;
		private String slots;
		private String state;
		private String time;

		public Job(Element element) {
			state = getContent(element, "state");
			jobNumber = getContent(element, "JB_job_number");
			pritory = getContent(element, "JAT_prio");
			owner = getContent(element, "JB_owner");
			slots = getContent(element, "slots");
			time = getContent(element, "JAT_start_time");
			if (time.equals("")) {
				time = getContent(element, "JB_submission_time");
			}
			name = getContent(element, "JB_name");
		}

		private String getContent(Element e, String name) {
			if (e.getElementsByTagName(name).item(0) == null) {
				return "";
			}
			return e.getElementsByTagName(name).item(0).getTextContent();
		}

		public String getJobNumber() {
			return jobNumber;
		}

		public String getName() {
			return name;
		}

		public String getOwner() {
			return owner;
		}

		public String getPritory() {
			return pritory;
		}

		public String getSlots() {
			return slots;
		}

		public String getState() {
			return state;
		}

		public String getTime() {
			return time;
		}

	}

	public class Queue {

		private String arch;
		private Vector<Job> jobs = new Vector<Job>();
		private String name;
		private HashMap<String, String> resources = new HashMap<String, String>();

		private int slots;

		private int slotsUsed;

		private String state;

		private String type;

		public Queue(Element element) {
			name = getContent(element, "name");
			type = getContent(element, "qtype");
			if (!getContent(element, "slots_total").equals("")) {
				slots = Integer.parseInt(getContent(element, "slots_total"));
				slotsUsed = Integer.parseInt(getContent(element, "slots_used"));
			}
			arch = getContent(element, "arch");
			state = getContent(element, "state");
			NodeList nl = element.getElementsByTagName("resource");
			for (int i = 0; i < nl.getLength(); i++) {
				String name = (((Element) nl.item(i)).getAttribute("name"));
				if (name.equals("mem_used") || name.equals("swap_used")) {
					String value = ((Element) nl.item(i)).getTextContent();
					if (value.endsWith("M")) {
						double val = Double.parseDouble(value.replaceAll("M", ""));
						value = "" + (val / 1024) + "G";
					} else if (value.endsWith("k")) {
						double val = Double.parseDouble(value.replaceAll("k", ""));
						value = "" + (val / (1024 * 1024)) + "G";
					}
					resources.put(name, value);
				} else if (name.equals("cpu")) {
					resources.put(name, ((Element) nl.item(i)).getTextContent());
				}
			}

			nl = element.getElementsByTagName("job_list");
			// System.out.println(name+" has "+nl.getLength()+" jobs");
			for (int i = 0; i < nl.getLength(); i++) {
				jobs.add(new Job((Element) nl.item(i)));
			}
		}

		public String getArch() {
			return arch;
		}

		private String getContent(Element e, String name) {
			if (e.getElementsByTagName(name).item(0) == null) {
				return "";
			}
			return e.getElementsByTagName(name).item(0).getTextContent();
		}

		public Vector<Job> getJobs() {
			return jobs;
		}

		public String getName() {
			return name;
		}

		public String getResources(String name) {
			return resources.get(name);
		}

		public int getSlots() {
			return slots;
		}

		public int getSlotsUsed() {
			return slotsUsed;
		}

		public String getState() {
			return state;
		}

		public String getType() {
			return type;
		}

	}


	Vector<Queue> queues = new Vector<Queue>();
	Vector<Job> waitingJob = new Vector<Job>();

	public Vector<Queue> getQueues() {
		return queues;
	}

	public Vector<Job> getWaitingJob() {
		return waitingJob;
	}

	public void parse() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			queues = new Vector<Queue>();
			db = dbf.newDocumentBuilder();
			Process p = Runtime.getRuntime().exec(new String[] { "qstat", "-u", "\"*\"", "-F", "mem_total,mem_used,swap_total,swap_used,cpu", "-xml" });
			Document dom = db.parse(p.getInputStream());
			// Document dom = db.parse(new File("/Users/boyda/Downloads/qstatFile"));
			Element el = dom.getDocumentElement();

			NodeList nlist = el.getElementsByTagName("queue_info");
			NodeList nl = ((Element) nlist.item(0)).getElementsByTagName("Queue-List");

			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element element = (Element) nl.item(i);
					queues.add(new Queue(element));
					// System.out.println(element.getElementsByTagName("name").item(0).getTextContent());
				}
			}
			nlist = el.getElementsByTagName("job_info");
			nl = ((Element) nlist.item(0)).getElementsByTagName("job_list");

			if (nl != null && nl.getLength() > 0) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element element = (Element) nl.item(i);
					new Job(element);
				}
			}
			p.destroy();

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
