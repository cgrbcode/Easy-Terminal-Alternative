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
package cgrb.eta.server.remote.etastart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.codec.binary.Hex;

import cgrb.eta.server.FileWatcherService;
import cgrb.eta.server.FolderChangedListener;
import cgrb.eta.server.LocalETAConnectionServer;
import cgrb.eta.server.WrapperUtils;
import cgrb.eta.server.remote.MalformedQueryException;
import cgrb.eta.server.remote.ProgramManager;
import cgrb.eta.server.remote.RemoteJob;
import cgrb.eta.server.remote.RemoteSGEJob;
import cgrb.eta.server.remote.TabQueryer;
import cgrb.eta.server.rmi.ConnectionListener;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.settings.Setting;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.EventOccuredListener;
import cgrb.eta.shared.FileBrowserEvent;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.UserWrapper;

public class ETAStart implements ConnectionListener, EventOccuredListener, RemoteUserService {
	private static byte[] empty = new byte[] { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	public static void main(String[] arg) {
		new ETAStart();
	}

	private RemoteETAConnectionService etaInstance;
	private HashMap<Integer, RemoteJob> jobs = new HashMap<Integer, RemoteJob>();
	private String lastPath = System.getenv("HOME");
	private int port;
	private String serverMachine;
	protected Socket sock;
	private int reconnectAttempts = 0;
	private HashMap<String, Vector<cgrb.eta.shared.etatype.File>> fileHistory = new HashMap<String, Vector<cgrb.eta.shared.etatype.File>>();
	private HashMap<String, DataInputStream> inputStreams = new HashMap<String, DataInputStream>();
	private HashMap<String, DataOutputStream> outputStreams = new HashMap<String, DataOutputStream>();
	private String userName;
	private HashMap<String, Integer> jobIds = new HashMap<>();
	private File ETADrive;
	private File ETAJobDir;

	public ETAStart() {
		lastPath = (lastPath == null) ? "/" : lastPath;
		this.serverMachine = "localhost";
		this.port = 3256;
		userName = System.getenv("USER");
		connect();
	}

	public boolean canSee(File file) {
		return true;
	}

	private void connect() {
		ProgramManager.getInstance();
		if (sock != null && sock.isConnected()) {
			return;
		}
		while (true) {
			try {
				sock = new Socket(serverMachine, port);
				reconnectAttempts = 0;
				break;
			} catch (Exception e) {
				System.out.println("trying agin in 10 sec");
				reconnectAttempts++;
				if (reconnectAttempts > 40)
					System.exit(0);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		connected(sock);
	}

	private void connected(Socket sock) {
		File ETADir = new File(System.getenv("HOME") + "/ETA");
		if (!ETADir.exists())
			ETADir.mkdirs();
		ETAJobDir = new File(ETADir.getPath() + "/jobs");
		if (!ETAJobDir.exists())
			ETAJobDir.mkdirs();
		ETADrive = new File(System.getenv("HOME") + "/ETADrive");
		if (!ETADrive.exists())
			ETADrive.mkdirs();
		File etaSettings = new File(System.getenv("HOME") + "/ETA/.settings");
		if (!etaSettings.exists()) {
			try {
				etaSettings.createNewFile();
			} catch (IOException e) {
				System.out.println("For some reason I can't create the file " + etaSettings.getAbsolutePath());
				System.exit(0);
			}
		}
		File etaRunFile = new File(System.getenv("HOME") + "/ETA/.running");
		try {
			etaRunFile.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		etaRunFile.deleteOnExit();

		if (etaSettings.canRead()) {
			Settings settings = Settings.getInstance(etaSettings);
			String token = settings.getSetting("token").getStringValue();
			if (token.length() != 30) {
				try {
					sock.getOutputStream().write(empty);
					userName = new com.sun.security.auth.module.UnixSystem().getUsername();
					System.out.println(userName);
					sock.getOutputStream().write((userName + "\n").getBytes());
					byte[] newToken = new byte[30];
					sock.getInputStream().read(newToken);
					String nTok = new String(newToken);
					System.out.println(nTok);
					settings.putSetting("token", new Setting(nTok));
					settings.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				byte[] sending = (" " + token).getBytes();
				sending[0] = LocalETAConnectionServer.ETA_START;
				try {
					sock.getOutputStream().write(sending);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				etaInstance = (RemoteETAConnectionService) new RMIConnection(sock, this, true, this).getService(RemoteETAConnectionService.class);
			} catch (Exception e) {
				System.out.println("Sorry can't connect for a good reason. It may be that you already have a connection open");
				System.exit(1);
			}
		} else {
			System.out.println("Error it looks like you aren't who you seem you are. Exiting");
			System.exit(0);
		}
		lastPath = (lastPath == null) ? "/" : lastPath;
		watchForJobs();
	}

	private boolean watching = false;

	private void watchForJobs() {
		if (watching)
			return;
		watching = true;
		FileWatcherService service = FileWatcherService.getInstance();
		service.watchFolder(ETAJobDir, new FolderChangedListener() {
			@Override
			public void fileRemoved(File file, File folder) {
			}

			@Override
			public void fileModified(File file, File folder) {
			}

			@Override
			public void fileCreated(File file, File folder) {
				addJob(file.toPath());
			}
		});

		service.watchFolder(ETADrive, new FolderChangedListener() {
			@Override
			public void fileRemoved(File file, File folder) {
				etaInstance.fileRemoved(ETADrive.toPath().relativize(file.toPath()).toString());
				updateBrowsers(file);
			}

			@Override
			public void fileModified(File file, File folder) {

			}

			@Override
			public void fileCreated(File file, File folder) {
				updateBrowsers(file);
				if (outputStreams.get(file) == null)
					etaInstance.newFile(ETADrive.toPath().relativize(file.toPath()).toString());
			}
		});
	}

	public void updateBrowsers(File file) {
		String parentFolder = file.getParent();
		System.out.println("browser event at " + parentFolder);
		etaInstance.eventOccured(new ETAEvent(ETAEvent.FILE_BROWSER, new FileBrowserEvent(parentFolder)));
	}

	private void addJob(Path filename) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename.toFile()));
			Vector<String> command = new Vector<>();
			Job job = new Job();
			job.setId(-1);
			job.setName("command line job");
			boolean isParent = false;
			String request = "-V ";
			for (String line = ""; (line = reader.readLine()) != null;) {
				if (line.startsWith("#working-folder")) {
					job.setWorkingDir(line.replaceFirst("#working-folder\t", ""));
				} else if (line.startsWith("#parent-name")) {
					job.setName(line.replaceFirst("#parent-name\t", ""));
					isParent = true;
					job.setPipeline(-1);
				} else if (line.startsWith("#job-name")) {
					job.setName((line.replaceFirst("#job-name\t", "")));
				} else if (line.startsWith("#std-out-path")) {
					job.setStdoutPath(line.replaceFirst("#std-out-path\t", ""));
				} else if (line.startsWith("#native-request")) {
					request += line.replaceFirst("#native-request\t", "") + " ";
				} else if (line.startsWith("#threads")) {
					request += "-pe thread " + line.replaceFirst("#threads\t", "") + " ";
				} else if (line.startsWith("#memory")) {
					request += " -l mem_free=" + line.replaceFirst("#memory\t", "") + " ";
				} else if (line.startsWith("#queue")) {
					request += " -q " + line.replaceFirst("#queue\t", "") + " ";
				} else if (!line.startsWith("#") && !line.equals("")) {
					command.add(line);
				} else if (line.startsWith("#priority")) {
					// request+= " -p " +line.replaceFirst("#priority\t", "")+" ";
				} else if (line.startsWith("#notify")) {

				} else if (line.startsWith("#wait-for")) {
					String ident = line.replaceFirst("#wait-for\t", "");
					job.setWaitingFor(jobIds.get(ident));
				} else if (line.startsWith("#parent")) {
					String ident = line.replaceFirst("#parent\t", "");
					job.setParent(jobIds.get(ident));
				} else if (line.startsWith("#command")) {
					String[] commandTemp = line.replaceFirst("#command\t", "").split(" ");
					for (String temp : commandTemp)
						command.add(temp);
				}
			}
			job.setSpecs(request);
			if (job.getWorkingDir() != null && command.size() > 0 || isParent) {
				if (!isParent) {
					String[] comm = new String[command.size()];
					for (int i = 0; i < comm.length; i++) {
						comm[i] = command.get(i);
					}
					jobIds.put(filename.toFile().getName(), etaInstance.runJob(job, comm));
				} else {
					jobIds.put(filename.toFile().getName(), etaInstance.runJob(job, new String[] {}));
				}
			}
			reader.close();
			filename.toFile().delete();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void connectionLost() {
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		sock = null;
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				connect();
			}
		});
		t.setName("ETAStart connecting thread");
		t.start();
	}

	public void eventOccured(ETAEvent event, int user) {
		etaInstance.eventOccured(event);
	}

	public String getFileContents(String file, long startByte, long bytes) {
		String ret = "";
		file = file.replaceAll("\\$HOME", System.getenv("HOME"));
		File f = new File(file);
		if (f.exists()) {
			try {
				RandomAccessFile fileA = new RandomAccessFile(f, "r");
				fileA.seek(startByte);
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
		return ret;
	}

	public byte[] getFileBuffer(String file) {
		byte[] bbuf = new byte[1024 * 512];
		try {
			DataInputStream in = inputStreams.get(file);
			if (in == null) {
				in = new DataInputStream(new FileInputStream(new File(file)));
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
		}
		return bbuf;
	}

	public String saveFileBuffer(String file, byte[] buff, int length) {
		DataOutputStream out = outputStreams.get(file);
		try {
			if (out == null) {
				out = new DataOutputStream(new FileOutputStream(new File(file)));
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
		}
		return "";
	}

	private FileFilter filter = new FileFilter() {

		public boolean accept(File pathname) {
			try {
				return pathname.canRead();
			} catch (Exception e) {
				return false;
			}
		}
	};

	public Vector<cgrb.eta.shared.etatype.File> back(String browser, String path) {
		Vector<cgrb.eta.shared.etatype.File> history = fileHistory.get(browser);
		if (path == null || path.equals("")) {
			if (history.size() == 0)
				return navigateTo(browser, System.getenv("HOME"));
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

	public Vector<cgrb.eta.shared.etatype.File> getHistory(String browser) {
		if (!fileHistory.containsKey(browser))
			fileHistory.put(browser, new Vector<cgrb.eta.shared.etatype.File>());
		Vector<cgrb.eta.shared.etatype.File> ret = new Vector<cgrb.eta.shared.etatype.File>();
		for (cgrb.eta.shared.etatype.File file : fileHistory.get(browser)) {
			ret.add(file);
		}
		return ret;
	}

	public Vector<cgrb.eta.shared.etatype.File> navigateTo(String browser, String path) {
		path = path.replaceAll("\\$HOME", getHomePath());
		if (!fileHistory.containsKey(browser))
			fileHistory.put(browser, new Vector<cgrb.eta.shared.etatype.File>());
		Vector<cgrb.eta.shared.etatype.File> history = fileHistory.get(browser);
		if (path == null || path.trim().equals("")) {
			if (fileHistory.get(browser).size() == 0) {
				path = System.getenv("HOME");
				File f = new File(path);
				history.add(0, new cgrb.eta.shared.etatype.File(f.getName(), f.getAbsolutePath(), "Folder", 0, 0, 0));
			} else {
				path = fileHistory.get(browser).get(0).getPath();
			}
		} else {
			File f = new File(path);
			if (history.size() > 0 && !history.get(0).getPath().equals(path))
				history.add(0, new cgrb.eta.shared.etatype.File(f.getName(), f.getAbsolutePath(), "Folder", 0, 0, 0));
		}
		if (history.size() > 10)
			history.remove(10);
		fileHistory.put(browser, history);

		return getFiles(path);
	}

	public Vector<cgrb.eta.shared.etatype.File> getFiles(String path) {
		Vector<cgrb.eta.shared.etatype.File> list = new Vector<cgrb.eta.shared.etatype.File>();

		list.add(new cgrb.eta.shared.etatype.File("cwd", path, "Folder", 0, 0, 0));

		if (!new File(path).exists()) {
			return new Vector<cgrb.eta.shared.etatype.File>();
		}
		lastPath = path;
		File[] files = new File(path).listFiles(filter);
		if (files == null) {
			return list;
		}
		// try to open the .ETAtypes file and load the filetypes into memory
		// File typesFile = new File(path + "/.ETAtypes");
		// HashMap<String, String> fileTypes = new HashMap<String, String>();
		// if (typesFile.exists() && typesFile.canRead()) {
		// try {
		// BufferedReader reader = new BufferedReader(new FileReader(typesFile));
		// while (true) {
		// String line = reader.readLine();
		// if (line == null)
		// break;
		// String[] split = line.split("~");
		// if (split.length == 2) {
		// fileTypes.put(split[0], split[1]);
		// }
		// }
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
		// }
		path = path.replaceAll("//", "/");
		for (int i = 0; i < files.length; i++) {
			String canicalPath = null;
			if (Files.isSymbolicLink(files[i].toPath())) {
				try {
					Path linkPath = Files.readSymbolicLink(files[i].toPath());
					canicalPath = linkPath.toString();
				} catch (IOException e) {
				}
			}
			cgrb.eta.shared.etatype.File temp = new cgrb.eta.shared.etatype.File(files[i].getName(), files[i].getAbsolutePath(), (files[i].isDirectory() ? "Folder" : "File"), files[i].length(), i, canicalPath, files[i].lastModified());
			try {
				temp.setMime(Files.probeContentType(files[i].toPath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			list.add(temp);
		}
		return list;
	}

	public String runSystemCommand(String[] args, String workingDir) {
		String ret = "";
		try {
			// if (!ProgramManager.getInstance().isInstalled((String) args[0]))
			// return "";
			Process p;
			if (workingDir == null || workingDir.equals(""))
				p = Runtime.getRuntime().exec(args);
			else
				p = Runtime.getRuntime().exec(args, null, new File(workingDir));

			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = r.readLine()) != null) {
				ret += line + "\n";
			}
			r.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public String runPluginCommand(String[] command, String workingDir) {
		String ret = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(new File(workingDir));
			Process p = builder.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public int runJob(Job job) {
		RemoteJob rJob;
		job.getWrapper().addVar("ETAJOB", job.getId() + "");
		job.getWrapper().addVar("ETAHOST", System.getenv("HOSTNAME"));
		rJob = new RemoteSGEJob(job, this);
		try {
			rJob.start();
		} catch (Exception e) {
		}
		jobs.put(job.getId(), rJob);
		return 0;
	}

	@Override
	public void link(cgrb.eta.shared.etatype.File target, cgrb.eta.shared.etatype.File src) {
		try {
			Files.createSymbolicLink(new File(target.getPath()).toPath(), new File(src.getPath()).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String hashWrapper(Wrapper wrapper) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(wrapper.getProgram().getBytes());
			for (Input par : wrapper.getInputs()) {
				if (par.getValue() != null && !par.getValue().equals("") && !par.getName().toLowerCase().replaceAll(" ", "").equals("outputname")) {
					if (par.getType().startsWith("File")) {
						hashFile(new File(par.getValue()), md);
					} else if (par.getType().startsWith("List:File")) {
						String[] strings = par.getValue().split(" ");
						for (String string : strings) {
							hashFile(new File(string), md);
						}
					} else {
						md.update(par.getValue().getBytes());
					}
				}
			}
			String hash = new String(Hex.encodeHex(md.digest()));
			return hash;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void hashFile(File file, MessageDigest md) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File fileIn : files) {
					if (!fileIn.isDirectory()) {

						FileInputStream is;
						try {
							is = new FileInputStream(fileIn);
							FileChannel f = is.getChannel();
							ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
							while (f.read(buf) != -1) {
								buf.flip();
								byte[] reading = new byte[buf.remaining()];
								buf.get(reading);
								md.update(reading);
								buf.clear();
							}
							f.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				FileInputStream is;
				try {
					is = new FileInputStream(file);
					FileChannel f = is.getChannel();
					ByteBuffer buf = ByteBuffer.allocateDirect(64 * 1024);
					while (f.read(buf) != -1) {
						buf.flip();
						byte[] reading = new byte[buf.remaining()];
						buf.get(reading);
						md.update(reading);
						buf.clear();
					}
					f.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String[] getCommandsInPath() {
		return ProgramManager.getInstance().getCommandsInPath();
	}

	public boolean isInPath(String program) {
		return ProgramManager.getInstance().isInstalled(program);
	}

	public void runQmod(Vector<String> jobs, String command) {
		for (String job : jobs) {
			try {
				Runtime.getRuntime().exec(command + " " + job);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String generatePerlTemplate(Wrapper wrapper, cgrb.eta.shared.etatype.File where) {
		WrapperUtils util = new WrapperUtils(wrapper);
		try {
			util.createPerlTemplate(new File(where.getPath()));
		} catch (IOException e) {
			return e.getLocalizedMessage();
		}
		return "";
	}

	@Override
	public String makedir(String where) {
		String randomFolder = "" + (int) (Math.random() * 1000000);
		File workingDir = new File(where + "/" + randomFolder);
		while (workingDir.exists()) {
			randomFolder = "" + (int) (Math.random() * 1000000);
			workingDir = new File(where + "/" + randomFolder);
		}
		workingDir.mkdir();
		return workingDir.getAbsolutePath();
	}

	@Override
	public void saveEmail(String emailAddress) {
		String home = System.getenv("HOME");
		File temp = new File(home + "/.forward");
		if (temp.exists()) {
			temp.delete();
		}
		try {
			temp.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			w.write(emailAddress);
			w.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void terminate() {
		System.exit(1);
	}

	@Override
	public String[][] runQuery(HashMap<String, String> query, String filepath) throws MalformedQueryException {
		return TabQueryer.getInstance().runQuery(query, filepath);
	}

	@Override
	public String cancelJob(int jobId) {
		RemoteJob job = jobs.get(jobId);
		if (job == null) {
			return "Lost control of job";
		} else {
			try {
				job.cancel();
			} catch (Exception e) {
				return "Lost control of job";
			}
			return "sucess";
		}
	}

	// d41d8cd98f00b204e9800998ecf8427e
	@Override
	public String hashFile(cgrb.eta.shared.etatype.File file) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			hashFile(new File(file.getPath()), md);
			String ret = new String(Hex.encodeHex(md.digest()));
			if (ret.equals("d41d8cd98f00b204e9800998ecf8427e"))
				return null;
			return ret;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void removeResult(int jobId) {
		String home = System.getenv("HOME");
		File temp = new File(home);
		String jobNumber = "" + jobId;
		File std = new File(temp.getAbsolutePath() + "/ETA/" + jobNumber + ".std");
		if (std.exists())
			std.delete();
		File ste = new File(temp.getAbsolutePath() + "/ETA/" + jobNumber + ".err");
		if (ste.exists())
			ste.delete();
	}

	@Override
	public long getFileSize(String path) {
		return new File(path).length();
	}

	@Override
	public String downloadFile(String url) {
		String home = System.getenv("HOME");
		String file = url;
		File temp = new File(home + "/ETA/downloads");
		if (!temp.exists()) {
			temp.mkdirs();
		}
		try {
			Runtime.getRuntime().exec(new String[] { "wget", "-P", temp.getAbsolutePath(), url });
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp.getAbsolutePath() + file.substring(file.lastIndexOf('/'));
	}

	@Override
	public String getHomePath() {
		return System.getenv("HOME");
	}

	@Override
	public void makeFolder(String string) {
		new File(string).mkdirs();
	}

	public String tail(String file, int lines) {
		try {
			java.io.RandomAccessFile fileHandler = new java.io.RandomAccessFile(file, "r");
			long fileLength = file.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (line == lines) {
						if (filePointer == fileLength) {
							continue;
						} else {
							break;
						}
					}
				} else if (readByte == 0xD) {
					line = line + 1;
					if (line == lines) {
						if (filePointer == fileLength - 1) {
							continue;
						} else {
							break;
						}
					}
				}
				sb.append((char) readByte);
			}

			sb.deleteCharAt(sb.length() - 1);
			fileHandler.close();
			String lastLine = sb.reverse().toString();
			return lastLine;
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Vector<UserWrapper> getUserWrappers() {
		return null;
	}

	@Override
	public Vector<Wrapper> getPublicWrappers() {
		return null;
	}

	@Override
	public void statusChanged(int jobId) {
	}

	@Override
	public void removeFiles(Vector<cgrb.eta.shared.etatype.File> files) {
		for(cgrb.eta.shared.etatype.File file:files){
			runSystemCommand(new String[] { "rm", "-rf", file.getPath() }, "");
		}
	}

	@Override
	public boolean moveFile(String from, String to) {
		runSystemCommand(new String[] { "mv", from, to }, "");
		return true;
	}

	@Override
	public boolean linkFile(String from, String to) {
		link(new cgrb.eta.shared.etatype.File(from), new cgrb.eta.shared.etatype.File(to));
		return true;
	}

	@Override
	public boolean copyFile(String from, String to) {
		runSystemCommand(new String[] { "cp", from, to }, "");
		return true;
	}

	@Override
	public boolean compressFiles(String type, String[] files, String to) {
		String[] command = null;
		int i = 0;
		switch (type) {
		case "gzip":
			command = new String[files.length + 1];
			command[i++] = "gzip";
			break;
		case "zip":
			command = new String[files.length + 2];
			command[i++] = "zip";
			command[i++] = files[0];
			break;
		case "tar":
			command = new String[files.length + 3];
			command[i++] = "tar";
			command[i++] = "-czf";
			command[i++] = files[0];
			break;
		default:
			return false;
		}
		for (int z = 0; z < files.length; z++) {
			command[i++] = files[z];
		}
		runSystemCommand(command, to);
		return true;
	}

	@Override
	public boolean deCompressFile(String mime, String archive, String where) {
		String[] command = null;
    if (mime.equals("application/x-tar")) {
            command = new String[] { "tar", "-xf", archive };
    } else if (mime.equals("application/x-compressed-tar")) {
            command = new String[] { "tar", "-zxf", archive };
    } else if (mime.equals("application/zip")) {
            command = new String[] { "unzip", archive };
    } else if (mime.equals("application/x-gzip")) {
            command = new String[] { "gzip", "-d", archive };
    }else{
    	return false;
    }
    runSystemCommand(command, where);
		return true;
	}

	@Override
	public String[] getQueues() {
		return runSystemCommand(new String[] { "qconf", "-sql" }, null).split("\n");
	}

	@Override
	public String[] getThreadEnviroments() {
		return runSystemCommand(new String[] { "qconf", "-spl" }, null).split("\n");
	}
}
