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
package cgrb.eta.shared.etatype;

public class File extends ETAType {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5275854307346920829L;
	private String name;
	private String path;
	private String type;
	private long modifiedDate;
	private long size;
	private int user;
	private String canonicalPath;
	private String mime;

	public File() {
	}

	public File(String name, String path, String type, long size, int id, long lastModified) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.size = size;
		this.id = id;
		modifiedDate = lastModified;
	}

	public File(String path) {
		this.path = path;
		name=path.substring(path.lastIndexOf('/')+1);
	}

	public File(String name, String path, String type, long size, int id, String canonicalPath, long lastModified) {
		this.name = name;
		this.path = path;
		this.type = type;
		this.size = size;
		this.id = id;
		this.canonicalPath = canonicalPath;
		modifiedDate = lastModified;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public long getSize() {
		return size;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public String getFolder() {
		int lastIndex = path.lastIndexOf('/');
		if (lastIndex > 0 || type.startsWith("Folder"))
			return path.substring(0, lastIndex);
		return path;
	}

	public String getCanonicalPath() {
		return canonicalPath;
	}

	public void setCanonicalPath(String canonicalPath) {
		this.canonicalPath = canonicalPath;
	}

	public boolean isLink() {
		return !(canonicalPath == null || canonicalPath.equals(path));
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(long modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public void setMime(String probeContentType) {
		this.mime = probeContentType;
	}

	public String getMime() {
		return mime;
	}
	
	public void setType(String type){
		this.type=type;
	}
	public void setSize(long size){
		this.size=size;
	}
}
