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

import java.util.Vector;

public class Plugin extends ETAType{
	private static final long serialVersionUID = 8766217124588209758L;
	
	private String name;
	private String author;
	private String version;
	private String description;
	private String type;
	private String email;
	private String index;
	private String icon;
	private String identifier;
	private Vector<String> permissions;
	private Vector<String> fileTypes;
	
	
	/**
	 * @param id the id of the plugin from the sql database
	 * @param name the name of the plugin
	 * @param author the name of the person who created this plugin
	 * @param version the version of this plugin
	 * @param description the description of the plugin
	 * @param type can be: viewer|workspace| more will be added
	 * @param icon the name of the file of the icon image
	 * @param identifier a randomly generated identifier that the system created. This is also the name of the folder of the contents of this plugin
	 * @param permissions a vector of strings of all the permissions this plugin has access to do. can be view|write|run program __ | more will be added
	 * @param fileTypes a vector of strings of all the types of files that this plugin can open
	 */
	public Plugin(int id, String name, String author, String version, String description, String type, String icon, String identifier, Vector<String> permissions, Vector<String> fileTypes) {
		super(id);
		this.name = name;
		this.author = author;
		this.version = version;
		this.description = description;
		this.type = type;
		this.icon = icon;
		this.identifier = identifier;
		this.permissions = permissions;
		this.fileTypes = fileTypes;
	}
	public Plugin(){}
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Vector<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(Vector<String> permissions) {
		this.permissions = permissions;
	}
	public Vector<String> getFileTypes() {
		return fileTypes;
	}
	public void setFileTypes(Vector<String> fileTypes) {
		this.fileTypes = fileTypes;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
}
