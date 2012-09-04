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
package cgrb.eta.server.remote;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public class TabQueryer {
	protected HashMap<String, SqlQueryFile> files = new HashMap<String, SqlQueryFile>();

	protected static TabQueryer instance;

	public static TabQueryer getInstance() {
		return instance == null ? instance = new TabQueryer() : instance;
	}

	protected TabQueryer() {

	}

	protected void trash() {
		Iterator<String> it = files.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			SqlQueryFile temp = files.get(name);
			if (new Date().getTime() - temp.getLastAccessed().getTime() > 1800000) {
				files.remove(files);
				temp.close();
			}
		}
	}

	public String[][] runQuery(String query, String filepath) throws MalformedQueryException {
		return runQuery(query(query), filepath);
	}

	public String[][] runQuery(HashMap<String, String> query, String filepath) throws MalformedQueryException {
		String file = filepath + "/" + query.get("from");
		String dilimeter = query.get("using");
		if (dilimeter == null || dilimeter.equals(""))
			dilimeter = "\t";
		File f = new File(file);
		// do some garbage collecting and get rid of old cached results
		new Thread(new Runnable() {

			public void run() {
				trash();
			}
		}).start();

		if (f.exists()) {
			if (f.isFile()) {
				SqlQueryFile queryFile = files.containsKey(file) ? files.get(file) : new SqlQueryFile(f, dilimeter);
				queryFile.setDelimeter(dilimeter);
				files.put(file, queryFile);
				return queryFile.query(query);
			} else {
				return new SqlQueryFolder(f).query(query);
			}
		}
		return null;
	}

	// selects on folders select size, count, name
	// filename,filesize,filecount
	// select count() from . where filesize>809

	public static void main(String[] asdfa) {
		TabQueryer tq = TabQueryer.getInstance();
		// HashMap<String, String> query = query("select cluster,Ef,Fg,Fo from heatmap2 where Fo>=10 limit 100 order Fo using tab");
		
		if (asdfa.length == 0) {
			// go into shell mode
		} else {
			String qString = "";
			for (String temp : asdfa)
				qString += temp + " ";
			HashMap<String, String> query = query(qString);
			try {
				String[][] results = tq.runQuery(query, new File("").getAbsolutePath());
				for (int row = 0; row < results.length; row++) {
					for (int col = 0; col < results[row].length; col++) {
						System.out.print(results[row][col] + "\t");
					}
					System.out.println();
				}
			} catch (MalformedQueryException e) {
				e.printStackTrace();
			}
		}
	}

	protected static HashMap<String, String> query(String query) {
		String[] q = query.split(" ");
		HashMap<String, String> keys = new HashMap<String, String>();
		keys.put("select", "");
		keys.put("where", "");
		keys.put("order", "");
		keys.put("limit", "");
		keys.put("desc", "");
		keys.put("asc", "");
		keys.put("from", "");
		keys.put("using", "");

		String on = null;
		for (String temp : q) {
			if (keys.containsKey(temp.toLowerCase())) {
				on = temp.toLowerCase();
				if (temp.equals("desc")) {
					keys.put("desc", "1");
				}
			} else {
				if (on != null) {
					if (on.equals("where")) {
						keys.put(on, keys.get(on) + " " + temp);
					} else {
						keys.put(on, keys.get(on) + temp);
					}
				}
			}
		}
		return keys;
	}
}
