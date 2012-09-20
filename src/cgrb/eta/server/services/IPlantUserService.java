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
package cgrb.eta.server.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.FileIOOperations;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.irods.jargon.core.pub.io.IRODSFileOutputStream;
import org.irods.jargon.core.pub.io.IRODSRandomAccessFile;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cgrb.eta.server.CommunicationImpl;
import cgrb.eta.server.HTTPSPost;
import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.IPlantQueryer;
import cgrb.eta.server.remote.MalformedQueryException;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.JobEvent;
import cgrb.eta.shared.etatype.File;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.UserWrapper;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

public class IPlantUserService implements RemoteUserService {

	private String username;
	private String token;
	private static Vector<Wrapper> wrappers;
	private HashMap<Integer, Integer> jobIdMap = new HashMap<Integer, Integer>();
	private HashMap<String, Vector<cgrb.eta.shared.etatype.File>> fileHistory = new HashMap<String, Vector<cgrb.eta.shared.etatype.File>>();
	private IRODSFileFactory factory;
	private HashMap<String, IRODSFileInputStream> inputStreams = new HashMap<String, IRODSFileInputStream>();
	private HashMap<String, IRODSFileOutputStream> outputStreams = new HashMap<String, IRODSFileOutputStream>();
	
	public IPlantUserService(String username, String token) {
		this.username = username;
		this.token = token;
//		Timer timer = new Timer();
//		timer.scheduleAtFixedRate(new TimerTask() {
//			
//			@Override
//			public void run() {
//				renewToken();
//			}
//		}, new Date(new Date().getTime()+(60*60*1000)), 60*60*1000);
	}

	@Override
	public Vector<File> getFiles(String path) {
		Vector<File> ret = new Vector<File>();
		if (path.equals("/")) {
			File newFile0 = new File("/");
			newFile0.setId(1);
			newFile0.setType("Folder");
			ret.add(newFile0);
			File newFile = new File(getHomePath());
			newFile.setId(1);
			newFile.setType("Folder");
			ret.add(newFile);
			File newFile2 = new File("/shared");
			newFile2.setId(2);
			newFile2.setType("Folder");
			ret.add(newFile2);
			return ret;
		}
		if (path.equals("") || path.equals("$HOME"))
			path = getHomePath();
		JsonArray obj = get("io-v1/io/list" + path).getAsJsonArray("result");
		for (int i = 0; i < obj.size(); i++) {
			JsonObject temp = obj.get(i).getAsJsonObject();
			File newFile = new File(temp.get("path").getAsString());
			newFile.setId(i);
			newFile.setType(temp.get("format").getAsString().equals("folder") ? "Folder" : "File");
			newFile.setMime(temp.get("mimeType").getAsString());
			newFile.setModifiedDate(temp.get("lastModified").getAsLong());
			newFile.setSize(temp.get("length").getAsLong());
			ret.add(newFile);
		}
		return ret;
	}
	
//	private void renewToken(){
//		post("auth-v1/renew","token="+token);
//	}

	@Override
	public Vector<File> navigateTo(String browser, String path) {
		if (!fileHistory.containsKey(browser))
			fileHistory.put(browser, new Vector<cgrb.eta.shared.etatype.File>());
		Vector<cgrb.eta.shared.etatype.File> history = fileHistory.get(browser);
		if (path == null || path.trim().equals("")) {
			if (fileHistory.get(browser).size() == 0) {
				path = getHomePath();
				File f = new File(path);
				history.add(0, new cgrb.eta.shared.etatype.File(f.getName(), f.getPath(), "Folder", 0, 0, 0));
			} else {
				path = fileHistory.get(browser).get(0).getPath();
			}
		} else {
			File f = new File(path);
			if (history.size() > 0 && !history.get(0).getPath().equals(path))
				history.add(0, new cgrb.eta.shared.etatype.File(f.getName(), f.getPath(), "Folder", 0, 0, 0));
		}
		if (history.size() > 10)
			history.remove(10);
		fileHistory.put(browser, history);

		return getFiles(path);
	}

	@Override
	public Vector<File> back(String browser, String path) {
		Vector<cgrb.eta.shared.etatype.File> history = fileHistory.get(browser);
		if (path == null || path.equals("")) {
			if (history.size() == 0)
				return navigateTo(browser, getHomePath());
			history.remove(0);
			cgrb.eta.shared.etatype.File last = history.remove(0);
			return navigateTo(browser, last.getPath());
		}
		while (history.size() > 1 && !history.get(0).getPath().equals(path))
			history.remove(0);
		cgrb.eta.shared.etatype.File last = history.remove(0);
		fileHistory.put(browser, history);
		return navigateTo(browser, last.getPath());
	}

	@Override
	public Vector<File> getHistory(String browser) {
		if (!fileHistory.containsKey(browser))
			fileHistory.put(browser, new Vector<cgrb.eta.shared.etatype.File>());
		Vector<cgrb.eta.shared.etatype.File> ret = new Vector<cgrb.eta.shared.etatype.File>();
		for (cgrb.eta.shared.etatype.File file : fileHistory.get(browser)) {
			ret.add(file);
		}
		return ret;
	}

	@Override
	public String getFileContents(String file, long startByte, long bytes) {
		String ret = "";
		file = file.replaceAll("\\$HOME", getHomePath());
		IRODSFile f;
		try {
			f = factory.instanceIRODSFile("/iplant/home" + file);
			if (f.exists()) {
				try {
					IRODSRandomAccessFile fileA = factory.instanceIRODSRandomAccessFile(f);
					fileA.seek(startByte, FileIOOperations.SeekWhenceType.SEEK_START);
					byte[] re = new byte[(int) bytes];
					int read = fileA.read(re);
					fileA.close();
					if (read <= 0)
						return "";
					if (read != bytes) {
						byte[] tmp = new byte[read];
						for (int i = 0; i < read; i++) {
							tmp[i] = re[i];
						}
						return new String(tmp);
					}
					return new String(re);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (JargonException e1) {
			e1.printStackTrace();
		}
		return ret;
	}

	@Override
	public int runJob(Job job) {
		Vector<Input> inputs = job.getWrapper().getInputs();
		HashMap<String, String> map = new HashMap<>();
		map.put("jobName", "ETA_" + job.getId());
		map.put("softwareName", job.getWrapper().getProgram());
		map.put("processorCount", "1");
		map.put("requestedTime", "02:00:00");
		map.put("archive", "1");
		map.put("callbackUrl", "http://eta.boydscreations.com/jobcallback/" + job.getId());
		map.put("archivePath", job.getWorkingDir());
		map.put("outputPath", job.getWorkingDir());
		JobEvent event = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
		for (Input input : inputs) {
			if (!(input.getValue() == null || input.getValue().equals(""))) {
				map.put(input.getFlag(), input.getValue());
			}
		}
		// now submit the job
		JsonObject ret = post("apps-v1/job/", map);
		if (null == ret) {
			event.setChange("Could not submit job");
			CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
		} else {
			jobIdMap.put(job.getId(), ret.get("result").getAsJsonObject().get("id").getAsInt());
			event.setChange("Pending");
			CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
		}

		return 1;
	}

	@Override
	public void runQmod(Vector<String> jobs, String command) {
	}

	@Override
	public String getHomePath() {
		return "/" + username;
	}

	@Override
	public String tail(String file, int lines) {
		return null;
	}

	@Override
	public String[] getCommandsInPath() {
		return null;
	}

	@Override
	public boolean isInPath(String program) {
		return false;
	}

	@Override
	public String generatePerlTemplate(Wrapper wrapper, File where) {
		return null;
	}

	@Override
	public String runPluginCommand(String[] command, String workingDir) {
		return null;
	}

	@Override
	public byte[] getFileBuffer(String file) {
		byte[] bbuf = new byte[1024 * 512];
		try {
			IRODSFileInputStream in = inputStreams.get(file);
			if (in == null) {
				in = factory.instanceIRODSFileInputStream("/iplant/home"+file);
				inputStreams.put(file, in);
			}
			int length;
			if ((in != null) && ((length = in.read(bbuf)) != -1)) {
				if (length != bbuf.length) {
					byte[] ret = new byte[length];
					for (int i = 0; i < length; i++)
						ret[i] = bbuf[i];
					bbuf = ret;
				}
			} else {
				bbuf = new byte[0];
				in.close();
				inputStreams.remove(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[] {};
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return bbuf;
	}

	@Override
	public String saveFileBuffer(String file, byte[] buff, int length) {
		IRODSFileOutputStream out = outputStreams.get(file);

		try {
			if (out == null) {
				out=factory.instanceIRODSFileOutputStream("/iplant/home"+file);
				outputStreams.put(file, out);
			}
			if (buff == null || length == 0) {
				out.flush();
				out.close();
				outputStreams.remove(file);
				return "";
			}
			try {
				out.write(buff);
			} catch (IOException e) {
				e.printStackTrace();
				return e.getLocalizedMessage();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		} catch (IOException e) {
			e.printStackTrace();
			return e.getLocalizedMessage();
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String makedir(String where) {
		String randomFolder = "" + (int) (Math.random() * 1000000);
		makeFolder(where + "/" + randomFolder);
		return where + "/" + randomFolder;
	}

	@Override
	public void saveEmail(String emailAddress) {

	}

	@Override
	public void terminate() {
	}

	@Override
	public String hashWrapper(Wrapper wrapper) {
		return null;
	}

	@Override
	public String[][] runQuery(HashMap<String, String> query, String filepath) throws MalformedQueryException {
		return IPlantQueryer.getInstance().runQuery(query, filepath,factory);
	}

	@Override
	public String cancelJob(int jobId) {
		delete("apps-v1/job/" + jobIdMap.get(jobId));
		return "";
	}

	@Override
	public void link(File target, File src) {

	}

	@Override
	public String hashFile(File file) {
		return null;
	}

	@Override
	public void removeResult(int jobId) {

	}

	@Override
	public long getFileSize(String path) {
		IRODSFile file;
		try {
			file = factory.instanceIRODSFile("/iplant/home" + path);
			long ret = file.getTotalSpace();
			file.close();
			return ret;
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String downloadFile(String url) {
		return null;
	}

	@Override
	public void makeFolder(String string) {
		File file = new File(string);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("action", "mkdir");
		map.put("dirName", file.getName());
		put("io-v1/io" + file.getFolder(), map);
	}

	public JsonObject post(String url, String... parameters) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < parameters.length; i += 2) {
			map.put(parameters[i], parameters[i + 1]);
		}
		String ret = HTTPSPost.post(IPlantAuthenticator.ADDRESS + url, map, username, token);
		return new JsonParser().parse(ret).getAsJsonObject();
	}

	public JsonObject post(String url, HashMap<String, String> map) {
		String ret = HTTPSPost.post(IPlantAuthenticator.ADDRESS + url, map, username, token);
		return new JsonParser().parse(ret).getAsJsonObject();
	}

	public JsonObject put(String url, HashMap<String, String> map) {
		String ret = HTTPSPost.put(IPlantAuthenticator.ADDRESS + url, map, username, token);
		return new JsonParser().parse(ret).getAsJsonObject();
	}

	public JsonObject get(String url, String... parameters) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < parameters.length; i += 2) {
			map.put(parameters[i], parameters[i + 1]);
		}
		String ret = HTTPSPost.get(IPlantAuthenticator.ADDRESS + url, map, username, token);
		return new JsonParser().parse(ret).getAsJsonObject();
	}

	public JsonObject delete(String url, String... parameters) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < parameters.length; i += 2) {
			map.put(parameters[i], parameters[i + 1]);
		}
		String ret = HTTPSPost.delete(IPlantAuthenticator.ADDRESS + url, map, username, token);
		return new JsonParser().parse(ret).getAsJsonObject();
	}

	public Vector<UserWrapper> getUserWrappers() {
		return null;
	}

	@Override
	public Vector<Wrapper> getPublicWrappers() {
		if (wrappers != null)
			return wrappers;
		Vector<Wrapper> ret = new Vector<Wrapper>();
		JsonArray obj = get("apps-v1/apps/share/list").getAsJsonArray("result");
		int i = 0;
		for (JsonElement el : obj) {
			Wrapper wrap = new Wrapper();
			JsonObject tempObj = el.getAsJsonObject();
			wrap.setName(tempObj.getAsJsonPrimitive("name").getAsString());
			wrap.setId(i++);
			wrap.setDescription(tempObj.get("longDescription").isJsonNull() ? "" : tempObj.get("longDescription").getAsString());
			if (wrap.getDescription().equals("")) {
				wrap.setDescription(tempObj.get("shortDescription").isJsonNull() ? "" : tempObj.get("shortDescription").getAsString());
			}
			wrap.setProgram(tempObj.getAsJsonPrimitive("id").getAsString());
			JsonArray inputsI = tempObj.getAsJsonArray("inputs");
			if (inputsI != null)
				for (JsonElement elI : inputsI) {
					JsonObject tempInput = elI.getAsJsonObject();
					Input newInput = new Input();
					newInput.setName(tempInput.getAsJsonPrimitive("id").getAsString());
					newInput.setDescription(tempInput.get("details").getAsJsonObject().getAsJsonPrimitive("label") == null ? "" : tempInput.get("details").getAsJsonObject().getAsJsonPrimitive("label").getAsString());
					newInput.setFlag(tempInput.getAsJsonPrimitive("id").getAsString());
					newInput.setRequired(tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("required") == null ? false : tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("required").getAsBoolean());
					newInput.setType("File:");
					wrap.addInput(newInput);
				}
			JsonArray paramsI = tempObj.getAsJsonArray("parameters");
			if (paramsI != null)
				for (JsonElement elI : paramsI) {
					JsonObject tempInput = elI.getAsJsonObject();
					Input newInput = new Input();
					newInput.setName(tempInput.getAsJsonPrimitive("id").getAsString());
					newInput.setDescription(tempInput.get("details").getAsJsonObject().getAsJsonPrimitive("label") == null ? "" : tempInput.get("details").getAsJsonObject().getAsJsonPrimitive("label").getAsString());
					newInput.setFlag(tempInput.getAsJsonPrimitive("id").getAsString());
					newInput.setRequired(tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("required") == null ? false : tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("required").getAsBoolean());
					String type = tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("type") == null ? "string" : tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("type").getAsString();
					String validator = tempInput.get("value").getAsJsonObject().getAsJsonPrimitive("validator").getAsString();
					switch (type) {
					case "enumeration":
						validator = validator.replaceAll("[\\[\\]\\{\\}\\\"]", "").replaceAll(":", "~");
						newInput.setType("Selection:" + validator);
						break;
					case "number":
						newInput.setType("Number");
						newInput.setDefaultValue(tempInput.getAsJsonPrimitive("defaultValue").getAsString());
						break;
					case "bool":
						newInput.setType("Flag");
						newInput.setDefaultValue(tempInput.getAsJsonPrimitive("defaultValue").getAsString());
						break;
					case "string":
						newInput.setType("String");
						newInput.setDefaultValue(tempInput.getAsJsonPrimitive("defaultValue").getAsString());
						validator = validator.replaceAll("[\\{\\}]", "");
						if (validator.contains("|"))
							newInput.setType("Selection:" + validator.replaceAll("\\|", ","));
						break;
					default:
						System.out.println(type);
					}
					wrap.addInput(newInput);
				}
			JsonArray outputs = tempObj.getAsJsonArray("outputs");
			for (JsonElement elI : outputs) {
				JsonObject tempOutput = elI.getAsJsonObject();
				Output out = new Output();
				out.setValue(tempOutput.get("defaultValue").getAsString());
				out.setName(tempOutput.get("id").getAsString());
				out.setType("File");
				wrap.addOutput(out);
			}

			wrap.setCreatorId(1);
			ret.add(wrap);
		}
		wrappers = ret;
		return ret;
	}

	public void statusChanged(int jobId) {
		String status = get("apps-v1/job/" + jobIdMap.get(jobId)).getAsJsonPrimitive("result").getAsJsonObject().get("status").getAsString();
		System.out.println("eta job " + jobId + " has changed status: " + status);
		final Job job = SqlManager.getInstance().getJob(jobId);
		if (!job.getStatus().equals(status)) {
			if (job.getStatus().equals("success")) {
				job.setStatus("Finished");
			} else {
				job.setStatus(status);
				JobEvent event = new JobEvent(JobEvent.STATUS_CHANGED, job.getId());
				event.setChange(status);
				CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.JOB, event), job.getUserId());
			}
		}
	}

	@Override
	public void removeFiles(Vector<File> files) {
		for (File file : files)
			delete("io-v1/io" + file.getPath());
	}

	@Override
	public boolean moveFile(String from, String to) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("action", "move");
		map.put("newPath", to);
		put("io-v1/io" + from, map);
		return true;
	}

	@Override
	public boolean linkFile(String from, String to) {
		return false;
	}

	@Override
	public boolean copyFile(String from, String to) {
		return false;
	}

	@Override
	public boolean compressFiles(String type, String[] files, String to) {
		return false;
	}

	@Override
	public boolean deCompressFile(String type, String archive, String where) {
		return false;
	}

	@Override
	public String[] getQueues() {
		return new String[] {"queuesss"};
	}

	@Override
	public String[] getThreadEnviroments() {
		return new String[] {"threadsss"};
	}

	public boolean setupIRods(String password) {
		try {
			IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
			IRODSAccount account = new IRODSAccount("data.iplantcollaborative.org", 1247, username, password, "/home", "iplant", "/iplant/home");
			factory = irodsFileSystem.getIRODSFileFactory(account);
			return true;
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return false;
	}
}
