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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class SqlQueryFile {
	private Date lastAccessed = new Date();
	private HashMap<String, Integer> header = new HashMap<String, Integer>(); // First line of file
	private RandomAccessFile raf;
	private HashMap<String, SqlResults> cache = new HashMap<String, SqlResults>();
	private String delimiter;
	private File file;
	private String firstLine;
	private long lastSaved = 0;
	
	protected SqlQueryFile(){
		
	}

	public SqlQueryFile(File file) {
		this(file, "\t");
	}

	public SqlQueryFile(File file, String delimiter) {
		this.file = file;
		this.delimiter = delimiter;
		try {
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		lastSaved = file.lastModified();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			line = reader.readLine();
			firstLine = line;
			if(this.delimiter==null)
				this.delimiter="\t";
			String[] row = line.split(this.delimiter);
			for (int i = 0; i < row.length; i++) {
				header.put(row[i], i);
				header.put("" + i, i);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[][] query(HashMap<String, String> query) throws MalformedQueryException {
		long last = file.lastModified();
		if (last > lastSaved) {
			last = lastSaved;
			cache.clear();

			try {
				raf.close();
				raf = new RandomAccessFile(file, "r");
			} catch (IOException e) {
				e.printStackTrace();
			}
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(file));
				String line;
				line = reader.readLine();
				firstLine = line;
				String[] row = line.split(this.delimiter);
				for (int i = 0; i < row.length; i++) {
					header.put(row[i], i);
					header.put("" + i, i);
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		lastAccessed = new Date();
		SqlResults cachedResult = null;
		if (cache.containsKey(query.get("where") + "&" + query.get("order"))) {
			cachedResult = cache.get(query.get("where") + "&" + query.get("order"));
		} else {
			// open the file and cache the results and return the results
			long onByte = 0;
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				onByte += line.length() + 1;
				Vector<SqlResult> results = new Vector<SqlResult>();
				while ((line = reader.readLine()) != null) {
					if (isInResult(query.get("where"), line.split(delimiter))) {
						SqlResult res = new SqlResult(onByte, line.length());
						if (!query.get("order").equals("")) {
							String[] lineA = line.split(delimiter);
							String[] sortVal = getSortValue(query.get("order"), lineA).split(",");
							for (int i = 0; i < sortVal.length; i++) {
								res.addSortValue(sortVal[i]);
							}
						}
						results.add(res);
					}
					onByte += (line.getBytes().length + 1);
				}
				cachedResult = new SqlResults(results);
				if (!query.get("order").equals("")) {
					cachedResult.sort(query.get("desc").equals("1"));
				}
				reader.close();
			} catch (FileNotFoundException e) {
				throw new MalformedQueryException("file not found");
			} catch (IOException e) {
				throw new MalformedQueryException("unknow IO exception");
			}
		}
		return getResults(query.get("limit"), cachedResult, query.get("select"));
	}

	public boolean isInResult(String where, String[] line) throws MalformedQueryException {
		if (where == null || where.equals("")) {
			return true;
		}
		return evaluate(where, line);
	}

	public boolean evaluate(String statement, String[] line) throws MalformedQueryException {
		//get rid of double spaces
		statement=statement.replace("[ ]+", " ").trim();
		String[] segments = statement.split(" ");
		String segment = "";
		int charOn = 0;
		statement=statement.trim();

		for (int i = 0; i < segments.length; i++) {
			String seg = segments[i];
			if (seg.startsWith("(")) {
				// go to the next ) and eval whats in it
				String str = statement.substring(charOn +i+1);
				charOn-=(seg.length()-1);
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
						charOn++;
						// reached the end break;
						break;
					}else if(at==' '){
						i++;
					}
					if(at!=' ')
						charOn++;
					newStr += at;
				}
				segment = "" + evaluate(newStr, line);
			}else if(seg.equals(")")){
			} else if (seg.equals("and") || seg.equals("or")) {
				// evaluacharOnte the previous stuff and
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
	private boolean evalSegment(String segment, String[] line) throws MalformedQueryException {
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
		}  else if (segment.contains("contains")) {
			String col = segment.split("contains")[0];
			String val = segment.split("contains")[1];
			col = getColValue(line, col);
			return col.toLowerCase().contains(val.toLowerCase());
		} else if (segment.contains("ends")) {
			String col = segment.split("ends")[0];
			String val = segment.split("ends")[1];
			col = getColValue(line, col);
			return col.endsWith(val);
		} else if (segment.contains(">=")) {
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
			val = getValValue(line, val);
			return col.equals(val);
		}
		throw new MalformedQueryException("couldn't evaluate: " + segment);
	}

	private String getValValue(String[] line, String col)  throws MalformedQueryException {
		try{
		return ""+Integer.parseInt(col);
		}catch(Exception e){
		}
		if (header.containsKey(col)) {
			return line[header.get(col)];
		} else {
			if(col.contains("+")){
				String[] plus = col.split("\\+");
				int total =0;
				for(String val:plus){
					total+=Integer.parseInt(getValValue(line, val));
				}
				return total+"";
			}
			return col;
		}
	}

	public String getColValue(String[] line, String col) throws MalformedQueryException {
		if (header.containsKey(col)) {
			return line[header.get(col)];
		} else {
			// col could be a function so figure out what function it is
			return getFunctionValue(line, col);
		}
	}

	public String[][] getResults(String limit, SqlResults results, String select) throws MalformedQueryException {
		int from = 0;
		int to = results.getResults().size();
		if (limit.contains(",")) {
			from = Integer.parseInt(limit.split(",")[0]);
			to = Integer.parseInt(limit.split(",")[1]);
		} else if (!limit.equals("")) {
			to = Integer.parseInt(limit);
		}

		if (to > results.getResults().size())
			to = results.getResults().size();
		if(from>to){
			from=to;
		}
		String[][] ret = new String[to - from + 1][1];
		ret[0] = getResults(firstLine, select);
		for (int i = from; i < to; i++) {
			ret[i - from + 1] = getResults(getLine(results.getResults().get(i).getStartByte(), results.getResults().get(i).getLength()), select);
		}
		return ret;
	}

	public String[] getResults(String line, String select) throws MalformedQueryException {
		if (select.equals("*")) {
			// return the entire line
			return line.split(delimiter);
		}

		// otherwise return what the select says too
		String[] cols = select.split(",");
		String[] ret = new String[cols.length];
		String[] lineA = line.split(delimiter);
		for (int i = 0; i < cols.length; i++) {
			ret[i] = getColValue(lineA, cols[i]);
		}

		return ret;
	}

	public Date getLastAccessed() {
		return lastAccessed;
	}

	/**
	 * 
	 * @param start
	 *          - where to start reading from
	 * @param length
	 *          - amount of information to read from file
	 * @return String from file
	 */
	private String getLine(long start, int length) {
		byte[] ret = new byte[length];
		try {
			raf.seek(start);
			raf.read(ret);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(ret);
	}

	public String getSortValue(String sortString, String[] row) throws MalformedQueryException {
		String[] coloumns = sortString.split(" ");
		String ret = "";
		for (int i = 0; i < coloumns.length; i++) {
			String col = coloumns[i];
			if (!ret.equals("")) {
				ret += ",";
			}
			ret += getColValue(row, col);
		}
		return ret;
	}

	private String getFunctionValue(String[] line, String function) throws MalformedQueryException {
		if (function.startsWith("sum(")) {
			return "" + sum(function.split("sum\\(")[1].split("\\)")[0], line);
		}else{
			//check for math operators
		}
		throw new MalformedQueryException("unidentified coloumn or function'" + function + "'");
	}

	private int sum(String rows, String[] row) {
		int sum = 0;
		if (rows.contains(",")) {
			String[] col = rows.split(",");
			for (int i = 0; i < col.length; i++) {
				try {
					sum += Integer.parseInt(row[header.get(col[i])]);
				} catch (NumberFormatException e) {
				}
			}
		} else {
			int startRow = header.get(rows);
			for (int i = startRow; i < row.length; i++) {
				try {
					sum += Integer.parseInt(row[i]);
				} catch (NumberFormatException e) {
				}
			}
		}
		return sum;
	}

	public void setDelimeter(String dilimeter) {
		delimiter = dilimeter;
	}

	public void close() {
		
	}

}
