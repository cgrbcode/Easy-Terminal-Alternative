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

import java.util.HashMap;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

public class IPlantQueryer extends TabQueryer {
	protected static IPlantQueryer instance;

	public static IPlantQueryer getInstance() {
		return instance == null ? instance = new IPlantQueryer() : instance;
	}

	public String[][] runQuery(HashMap<String, String> query, String filepath, IRODSFileFactory factory) throws MalformedQueryException {
		String file = filepath + "/" + query.get("from");
		String dilimeter = query.get("using");
		if (dilimeter == null || dilimeter.equals(""))
			dilimeter = "\t";
		IRODSFile f;
		try {
			f = factory.instanceIRODSFile("/iplant/home" + filepath);
			// do some garbage collecting and get rid of old cached results
			new Thread(new Runnable() {
				public void run() {
					trash();
				}
			}).start();

			if (f.exists()) {
				if (f.isFile()) {
					SqlQueryFile queryFile = files.containsKey(file) ? files.get(file) : new IPlantSQLQueryFile(f, dilimeter, factory);
					queryFile.setDelimeter(dilimeter);
					files.put(file, queryFile);
					return queryFile.query(query);
				} else {
					return new IPlantQueryFolder(f).query(query);
				}
			}
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return null;
	}
}
