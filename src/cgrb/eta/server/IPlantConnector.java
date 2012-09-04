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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Vector;

import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;

public class IPlantConnector {

	private static IPlantConnector instance;
	private String username = "";
	private String pass = "";
	private String token = null;

	public static IPlantConnector getInstance() {
		return instance == null ? instance = new IPlantConnector() : instance;
	}

	static {
		// for localhost testing only
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				if (hostname.equals("foundation.iplantc.org")) {
					return true;
				}
				return false;
			}
		});
	}

	private IPlantConnector() {
		// first we need to get a token for the user instead of the password
		getToken();
	}

	public void runJob(final Job job) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// create a new folder for this job
				put("io-v1/io/" + username + "/", "action", "mkdir", "dirName", "" + job.getId());
				Vector<Input> inputs = job.getWrapper().getInputs();
				HashMap<String, String> map = new HashMap<>();

				map.put("jobName", "ETA_" + job.getId());
				map.put("softwareName", job.getWrapper().getProgram().replaceFirst("iplant:", ""));
				map.put("processorCount", "1");
				map.put("requestedTime", "02:00:00");
				map.put("archive", "1");
				map.put("callbackUrl", Settings.getInstance().getSetting("etaHost").getStringValue() + "/jobcallback/${JOB_NAME}/${JOB_ID}");
				map.put("archivePath", "/" + username + "/" + job.getId());
				map.put("outputPath", "/" + username + "/" + job.getId());
				JobEvent event = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
				event.setChange("Copying files to TACC");
				CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
				for (Input input : inputs) {
					if (!(input.getValue() == null || input.getValue().equals(""))) {
						if (input.getType().startsWith("File")) {
							// we have to upload the file!
							File f = new File(input.getValue());
							uploadFile(job, f.getAbsolutePath());
							input.setValue("/" + username + "/" + job.getId() + "/" + f.getName());
						}
						map.put(input.getFlag(), input.getValue());
					}
				}
				// now submit the job
				if (null == post("apps-v1/job/", map)) {
					event.setChange("Could not submit job");
					CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
				} else {
					event.setChange("Pending");
					CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
				}

			}
		}).start();
	}

	private void getToken() {
		token = null;
		JsonObject ob = post("auth-v1/", "lifetime", "172800");
		token = ob.getAsJsonPrimitive("token").getAsString();
	}

	private JsonObject put(String url, String... args) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < args.length; i += 2) {
			map.put(args[i], args[i + 1]);
		}

		String ret = HTTPSPost.put("https://foundation.iplantc.org/" + url, map, username, (token == null ? pass : token));
		return new JsonParser().parse(ret).getAsJsonObject().getAsJsonObject("result");

	}
	private JsonObject post(String url, String... args) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < args.length; i += 2) {
			map.put(args[i], args[i + 1]);
		}
		String ret = HTTPSPost.post("https://foundation.iplantc.org/" + url, map, username, (token == null ? pass : token));
		return new JsonParser().parse(ret).getAsJsonObject().getAsJsonObject("result");
	}

	private JsonObject post(String url, HashMap<String, String> map) {
		String ret = HTTPSPost.post("https://foundation.iplantc.org/" + url, map, username, (token == null ? pass : token));
		if (ret == null)
			return null;
		return new JsonParser().parse(ret).getAsJsonObject().getAsJsonObject("result");
	}

	private void uploadFile(Job job, String file) {
		File binaryFile = new File(file);
		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
		String CRLF = "\r\n"; // Line separator required by multipart/form-data.
		URL url;
		PrintWriter writer = null;
		try {
			url = new URL("https://foundation.iplantc.org/io-v1/io/" + username + "/" + job.getId());
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			String userPassword = username + ":" + (token == null ? pass : token);
			System.out.println(userPassword);
			byte[] encoding = Base64.encodeBase64(userPassword.getBytes());
			connection.setRequestProperty("Authorization", "Basic " + new String(encoding));
			connection.setChunkedStreamingMode(102400);
			OutputStream output = connection.getOutputStream();
			writer = new PrintWriter(new OutputStreamWriter(output), true); // true = autoFlush, important!

			// Send binary file.
			writer.append("--" + boundary).append(CRLF);
			writer.append("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
			writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
			writer.append("Content-Transfer-Encoding: binary").append(CRLF);
			writer.append(CRLF).flush();

			byte[] bbuf;
			RemoteUserService localConnection = UserManagerService.getService().getUserService(job.getUserId());
			try {
				do {
					bbuf = (byte[]) localConnection.getFileBuffer( file);
					output.write(bbuf, 0, bbuf.length);
				} while (bbuf.length > 0);

				output.flush(); // Important! Output cannot be closed. Close of writer will close output as well.
			} finally {

			}
			writer.append(CRLF).flush(); // CRLF is important! It indicates end of binary boundary.

			// End of multipart/form-data.
			writer.append("--" + boundary + "--").append(CRLF);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null)
				writer.close();
		}
	}



}
