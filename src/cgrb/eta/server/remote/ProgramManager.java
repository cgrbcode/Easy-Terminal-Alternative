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
import java.util.HashMap;
import java.util.Iterator;

public class ProgramManager {
	private static ProgramManager instance = null;

	public static ProgramManager getInstance() {
		return instance == null ? instance = new ProgramManager() : instance;
	}

	private HashMap<String, String> paths = new HashMap<String, String>();

	private ProgramManager() {
		String[] folders = System.getenv("PATH").split(":");
		for (String file : folders) {
			File f = new File(file);
			if (f.isDirectory()) {
				File[] list = f.listFiles();
				for (File fi : list) {
					if (fi.isFile()) {

						paths.put(fi.getName(), fi.getAbsolutePath());

					}
				}
			} else {
				paths.put(f.getName(), f.getAbsolutePath());
			}
		}
	}

	public String[] getCommandsInPath() {
		String[] ret = new String[paths.size()];
		Iterator<String> it = paths.keySet().iterator();
		int on = 0;
		while (it.hasNext()) {
			ret[on++] = it.next();
		}
		return ret;
	}

	public boolean isInstalled(String program) {
		return paths.containsKey(program);
	}
}
