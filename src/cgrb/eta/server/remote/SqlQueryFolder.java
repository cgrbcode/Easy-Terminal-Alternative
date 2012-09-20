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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Vector;

public class SqlQueryFolder {
	private File folder;
	
	public SqlQueryFolder(File folder){
		this.folder=folder;
	}
	
	FilenameFilter filter = new FilenameFilter() {
		
		public boolean accept(File dir, String name) {
			return !name.startsWith(".");
		}
	};
	public String[][] query(HashMap<String, String> query) throws MalformedQueryException {
		File[] files = folder.listFiles(filter);
		Vector<String[]> results = new Vector<String[]>();
		for(File file:files){
			if(isInResult(query.get("where"), file)){
				results.add(getResult(query.get("select"), file));
			}
		}
		String[][] ret = new String[results.size()+1][];
		ret[0]=query.get("select").split(",");
		for(int i=1;i<ret.length;i++){
			ret[i]=results.get(i-1);
		}
		return ret;
	}
	
	public String[] getResult(String select,File file) throws MalformedQueryException{
		String[] cols = select.split(",");
		String[] ret = new String[cols.length];
		for (int i = 0; i < cols.length; i++) {
			ret[i] = getColValue(file, cols[i]);
		}
		return ret;
	}
	public boolean isInResult(String where,File line) throws MalformedQueryException {
		if (where == null || where.equals("")) {
			return true;
		}
		return evaluate(where, line);
	}
	
	public String getColValue(File file, String col) throws MalformedQueryException {
			if(col.equals("count")){
				if(file.isFile())return "1";
				return ""+file.list(filter).length;
			}else if(col.equals("filesize")){
				return ""+file.length();
			}else if(col.equals("filename")){
				return file.getName();
			}else if(col.equals("lastmodified")){
				return ""+file.lastModified();
			}else if(col.equals("filetype")){
				return file.isFile()?"file":"folder";
			}else if(col.equals("lines")){
				try {
					LineNumberReader lineReader = new LineNumberReader(new FileReader(file));
					while (( lineReader.readLine()) != null);
					lineReader.close();
          return ""+lineReader.getLineNumber();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			throw new MalformedQueryException("column:"+ col+" doesn't exist");
	}
	
	public boolean evaluate(String statement, File line) throws MalformedQueryException {
		String[] segments = statement.split(" ");
		String segment = "";
		int charOn = 0;
		for (int i = 0; i < segments.length; i++) {
			String seg = segments[i];
			
			if (seg.startsWith("(")) {
				// go to the next ) and eval whats in it
				String str = statement.substring(charOn + i+1);
				int inPar = 0;
				String newStr = "";
				for (int i2 = 0; i2 < str.length(); i2++) {
					char at = str.charAt(i2);
					if (inPar > 0 && at == ')') {
						if (newStr.charAt(newStr.length() - 1) != '\\')
							inPar--;
					} else if (at == '(') {
						inPar++;
					} else if (at == ')') {
						// reached the end break;
						break;
					}
					newStr += at;
				}
				if(newStr.endsWith(" "))
				i+=newStr.split(" ").length;
				else
					i+=newStr.split(" ").length-1;
				segment = "" + evaluate(newStr, line);
			} else if (seg.equals("and") || seg.equals("or")) {
				// evaluate the previous stuff and
				if (seg.equals("and")) {
					if (!evalSegment(segment, line))
						return false;
				} else if (seg.equals("or")) {
					if (evalSegment(segment, line)) {
						return true;
					}
				}
				segment = "";
			} else {
				segment += seg;
			}
			charOn += seg.length();
		}
		if (!segment.equals("")) {
			return evalSegment(segment, line);
		}
		return true;
	}

	// things that can be between the column identifer and the eval are: = > < <= >= != like starts ends
	private boolean evalSegment(String segment, File line) throws MalformedQueryException {
		if (segment.equals("true"))
			return true;
		if (segment.equals("false"))
			return false;
		if (segment.contains("!=")) {
			String col = segment.split("!=")[0];
			String val = segment.split("!=")[1];
			col = getColValue(line, col);
			return !col.equals(val);
		} else if (segment.contains("like")) {
			String col = segment.split("like")[0];
			String val = segment.split("like")[1];
			col = getColValue(line, col);
			return col.matches(val);
		} else if (segment.contains("starts")) {
			String col = segment.split("starts")[0];
			String val = segment.split("starts")[1];
			col = getColValue(line, col);
			return col.startsWith(val);
		} else if (segment.contains("ends")) {
			String col = segment.split("ends")[0];
			String val = segment.split("ends")[1];
			col = getColValue(line, col);
			return col.endsWith(val);
		}  else if (segment.contains("contains")) {
			String col = segment.split("contains")[0];
			String val = segment.split("contains")[1];
			col = getColValue(line, col);
			return col.contains(val);
		}else if (segment.contains(">=")) {
			String col = segment.split(">=")[0];
			String val = segment.split(">=")[1];
			col = getColValue(line, col);
			return Double.parseDouble(col) >= Double.parseDouble(val);
		} else if (segment.contains("<=")) {
			String col = segment.split("<=")[0];
			String val = segment.split("<=")[1];
			col = getColValue(line, col);
			return Double.parseDouble(col) <= Double.parseDouble(val);
		} else if (segment.contains(">")) {
			String col = segment.split(">")[0];
			String val = segment.split(">")[1];
			col = getColValue(line, col);
			return Double.parseDouble(col) > Double.parseDouble(val);
		} else if (segment.contains("<")) {
			String col = segment.split("<")[0];
			String val = segment.split("<")[1];
			col = getColValue(line, col);
			return Double.parseDouble(col) < Double.parseDouble(val);
		} else if (segment.contains("=")) {
			String col = segment.split("=")[0];
			String val = segment.split("=")[1];
			col = getColValue(line, col);
			return col.equals(val);
		}
		throw new MalformedQueryException("couldn't evaluate: " + segment);
	}
	
}
