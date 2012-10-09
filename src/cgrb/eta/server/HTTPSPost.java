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

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;

public class HTTPSPost {

	public static String post(String urlS, HashMap<String, String> props) {
		String ret = null;
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			Iterator<String> it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!post.equals(""))
					post += "&";
				post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

			// write out the data
			output.write(post);
			output.flush();
			// output.close();

			// ret= connection.getResponseMessage();
			try {
				// get ready to read the response from the cgi script
				DataInputStream input = new DataInputStream(connection.getInputStream());
				ret = "";
				// read in each character until end-of-stream is detected
				for (int c = input.read(); c != -1; c = input.read())
					ret += "" + (char) c;
				input.close();
			} catch (FileNotFoundException ex) {
				return "The address " + urlS + " was not found.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return ret;
	}

	public static String post(String urlS, HashMap<String, String> props, final String username, final String pass) {
		String ret = null;
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			Iterator<String> it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!post.equals(""))
					post += "&";
				post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String userPassword = username + ":" + pass;
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));
			connection.setConnectTimeout(10000);
			// connection.connect();

			OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

			// write out the data
			System.out.println(post);
			try {
				output.write(post);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			output.flush();
			// output.close();

			// ret= connection.getResponseMessage();
			try {
				// get ready to read the response from the cgi script
				DataInputStream input = new DataInputStream(connection.getInputStream());
				ret = "";
				// read in each character until end-of-stream is detected
				for (int c = input.read(); c != -1; c = input.read())
					ret += "" + (char) c;
				input.close();
			} catch (FileNotFoundException ex) {
				return "The address " + urlS + " was not found.";
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return ret;
	}

	public static String get(String urlS, HashMap<String, String> props, final String username, final String pass) {
		String ret = null;
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			if (props != null) {
				Iterator<String> it = props.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (!post.equals(""))
						post += "&";
					post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
				}
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			if (!post.equals(""))
				connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String userPassword = username + ":" + pass;
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));

			// connection.connect();
			if (!post.equals("")) {
				OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

				// write out the data
				output.write(post);
				output.flush();
			}
			// output.close();

			// ret= connection.getResponseMessage();
			try {
				// get ready to read the response from the cgi script
				DataInputStream input = new DataInputStream(connection.getInputStream());
				ret = "";
				// read in each character until end-of-stream is detected
				for (int c = input.read(); c != -1; c = input.read())
					ret += "" + (char) c;
				input.close();
			} catch (FileNotFoundException ex) {
				return "The address " + urlS + " was not found.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return ret;
	}

	public static InputStream get(String urlS, HashMap<String, String> props, final String username, final String pass, boolean hi) {
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			Iterator<String> it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!post.equals(""))
					post += "&";
				post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("GET");
			if (!post.equals(""))
				connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String userPassword = username + ":" + pass;
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));

			// connection.connect();
			if (!post.equals("")) {
				OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

				// write out the data
				output.write(post);
				output.flush();
			}
			// output.close();

			// ret= connection.getResponseMessage();
			return connection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return null;
	}

	public static String put(String urlS, HashMap<String, String> props, final String username, final String pass) {
		String ret = null;
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			Iterator<String> it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!post.equals(""))
					post += "&";
				post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("PUT");
			if (!post.equals(""))
				connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String userPassword = username + ":" + pass;
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));

			// connection.connect();
			if (!post.equals("")) {
				OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

				// write out the data
				output.write(post);
				output.flush();
			}
			// output.close();

			// ret= connection.getResponseMessage();
			try {
				// get ready to read the response from the cgi script
				DataInputStream input = new DataInputStream(connection.getInputStream());
				ret = "";
				// read in each character until end-of-stream is detected
				for (int c = input.read(); c != -1; c = input.read())
					ret += "" + (char) c;
				input.close();
			} catch (FileNotFoundException ex) {
				return "The address " + urlS + " was not found.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return ret;
	}

	public static String postWithExceptions(String urlS, HashMap<String, String> props) throws IOException {
		String ret = null;
		// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
		// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		String post = "";
		Iterator<String> it = props.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (!post.equals(""))
				post += "&";
			post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
		}
		URL url = new URL(urlS);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-length", String.valueOf(post.length()));
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

		// write out the data
		output.write(post);
		output.flush();
		// output.close();

		// ret= connection.getResponseMessage();
		// get ready to read the response from the cgi script
		DataInputStream input = new DataInputStream(connection.getInputStream());
		ret = "";
		// read in each character until end-of-stream is detected
		for (int c = input.read(); c != -1; c = input.read())
			ret += "" + (char) c;
		input.close();

		return ret;
	}

	public static String delete(String urlS, HashMap<String, String> props, String username, String pass) {
		String ret = null;
		try {
			// System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
			// java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			String post = "";
			Iterator<String> it = props.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				if (!post.equals(""))
					post += "&";
				post += key + "=" + URLEncoder.encode(props.get(key), "UTF-8");
			}
			URL url = new URL(urlS);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("DELETE");
			if (!post.equals(""))
				connection.setRequestProperty("Content-length", String.valueOf(post.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String userPassword = username + ":" + pass;
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));

			// connection.connect();
			if (!post.equals("")) {
				OutputStreamWriter output = new OutputStreamWriter(connection.getOutputStream());

				// write out the data
				output.write(post);
				output.flush();
			}
			// output.close();

			// ret= connection.getResponseMessage();
			try {
				// get ready to read the response from the cgi script
				DataInputStream input = new DataInputStream(connection.getInputStream());
				ret = "";
				// read in each character until end-of-stream is detected
				for (int c = input.read(); c != -1; c = input.read())
					ret += "" + (char) c;
				input.close();
			} catch (FileNotFoundException ex) {
				return "The address " + urlS + " was not found.";
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
		return ret;
	}
}
