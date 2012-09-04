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
package cgrb.eta.etadrive.desktop;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cgrb.eta.etadrive.ETADriveClientService;
import cgrb.eta.etadrive.ETADriveService;
import cgrb.eta.remote.api.InValidUserNameException;
import cgrb.eta.server.FileWatcherService;
import cgrb.eta.server.FolderChangedListener;
import cgrb.eta.server.HTTPSPost;
import cgrb.eta.server.rmi.AESRMIConnection;
import cgrb.eta.server.rmi.ConnectionListener;

public class ETADrive implements ETADriveClientService, FolderChangedListener {
	ETADriveService connection;
	private String cypher;
	private String token;
	private String ETAAddress;
	private HashMap<String, DataOutputStream> outputStreams = new HashMap<String, DataOutputStream>();
	boolean watching = false;
	private File driveFolder;

	public ETADrive() {
		final TrayIcon trayIcon;

		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/cgrb/eta/client/images/favicon.png"));
			MouseListener mouseListener = new MouseListener() {

				public void mouseClicked(MouseEvent e) {
					System.out.println("Tray Icon - Mouse clicked!");
				}

				public void mouseEntered(MouseEvent e) {
					System.out.println("Tray Icon - Mouse entered!");
				}

				public void mouseExited(MouseEvent e) {
					System.out.println("Tray Icon - Mouse exited!");
				}

				public void mousePressed(MouseEvent e) {
					System.out.println("Tray Icon - Mouse pressed!");
				}

				public void mouseReleased(MouseEvent e) {
					System.out.println("Tray Icon - Mouse released!");
				}
			};

			ActionListener exitListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Exiting...");
					System.exit(0);
				}
			};

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem("Exit");
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);

			trayIcon = new TrayIcon(image, "ETA Drive", popup);

			ActionListener actionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					trayIcon.displayMessage("Action Event", "An Action Event Has Been Performed!", TrayIcon.MessageType.INFO);
				}
			};

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(actionListener);
			trayIcon.addMouseListener(mouseListener);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println("TrayIcon could not be added.");
			}

		} else {

			// System Tray is not supported

		}
		final UserLogin login = new UserLogin();
		login.showMe();
		login.setActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					attemptConnection(login.getUsername(), login.getPassword());
					setConnection(connection);
					login.setVisible(false);
					login.dispose();
				} catch (InValidUserNameException e1) {
					JOptionPane.showMessageDialog(login, e1.getMessage(), "Failed to login", JOptionPane.WARNING_MESSAGE);
					e1.printStackTrace();
				}

			}
		});
	}

	public void attemptConnection(String userName, String password) throws InValidUserNameException {
		if (userName.contains("@")) {
			String[] temp = userName.split("@");
			String user = temp[0];
			String ETAUrl = temp[1];
			ETAAddress = ETAUrl.replaceFirst(":[0-9]*", "");
			System.out.println(ETAAddress);
			HashMap<String, String> props = new HashMap<String, String>();
			props.put("user", user);
			props.put("pass", password);
			// attempt to make a connection using HTTPSPost
			try {
				String response = HTTPSPost.postWithExceptions("https://" + ETAUrl + "/api/auth", props);
				JsonObject resp = new JsonParser().parse(response).getAsJsonObject();
				if (resp.get("response").getAsString().equals("ok")) {
					cypher = resp.get("return").getAsJsonObject().get("cypher").getAsString();
					token = resp.get("return").getAsJsonObject().get("token").getAsString();
					System.out.println(cypher);
					System.out.println(token);
				} else {
					throw new InValidUserNameException("username or password incorrect");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new InValidUserNameException("Url seems to be incorrect");
			}
		} else {
			throw new InValidUserNameException("User name: " + userName + " doesn't contain an @. It looks like you have input an invalid username");
		}
		try {
			Socket s = new Socket(ETAAddress, 3289);
			s.getOutputStream().write(token.getBytes());
			s.getOutputStream().write(new byte[] { 1, 0, 0 });
			AESRMIConnection connection = new AESRMIConnection(cypher, s, ETADrive.this, true, new ConnectionListener() {
				@Override
				public void connectionLost() {
				}
			});
			setConnection((ETADriveService) connection.getService(ETADriveService.class));
			System.out.println("connected!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setConnection(ETADriveService con) {
		connection = con;
		driveFolder= new File(System.getProperty("user.home") + "/ETADrive");
		if (!driveFolder.exists())
			driveFolder.mkdirs();
		FileWatcherService service = FileWatcherService.getInstance();
		service.watchFolder(driveFolder, this);
	}

	public void uploadFile(File file) {
		Path relativeToDrive = driveFolder.toPath().relativize(file.toPath());
		System.out.println(relativeToDrive);
		if(file.isDirectory()){
			connection.mkdir(relativeToDrive.toString());
			File[] files = file.listFiles();
			for(File fil:files){
				uploadFile(fil);
			}
			return;
		}
		if(outputStreams.get(file.getAbsolutePath())!=null)
			return;
		double size = file.length();
		System.out.println("size=" + size);
		int length;
		InputStream stream;
		try {
			stream = new FileInputStream(file);
			byte[] buffer = new byte[5 * 1024 * 1024];
			while ((length = stream.read(buffer)) > 0) {
				if (length != buffer.length) {
					byte[] newBuffer = new byte[length];
					for (int i = 0; i < newBuffer.length; i++) {
						newBuffer[i] = buffer[i];
					}
					buffer = newBuffer;
				}
				String ret = connection.saveFileBuffer(relativeToDrive.toString(), buffer, length);
				if (!ret.equals("")) {
					System.out.println("error:" + ret);
					break;
				}
			}
			connection.saveFileBuffer(relativeToDrive.toString(), new byte[] {}, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removeFile(String name) {
		connection.removeFile(name);
	}
	
	public void remoteRemoveFile(String file){
		new File(driveFolder.getAbsoluteFile()+"/"+file).delete();
	}

	public String saveFileBuffer(String file, byte[] buff, int length) {
		file = driveFolder+"/"+file;
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

	public static void main(String[] args) {
		new ETADrive();
	}

	@Override
	public void fileCreated(final File file, File folder) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				uploadFile(file);
			}
		}).start();
	}

	@Override
	public void fileModified(File file, File folder) {

	}

	@Override
	public void fileRemoved(File file, File folder) {
		connection.removeFile(driveFolder.toPath().relativize(file.toPath()).toString());
	}

	@Override
	public boolean fileExist(String file) {
		return new File(driveFolder.getAbsolutePath()+"/"+file).exists();
	}
	@Override
	public Vector<cgrb.eta.shared.etatype.File> getFiles(String path) {
		Vector<cgrb.eta.shared.etatype.File> list = new Vector<cgrb.eta.shared.etatype.File>();

		list.add(new cgrb.eta.shared.etatype.File("cwd", path, "Folder", 0, 0, 0));

		if (!new File(path).exists()) {
			return new Vector<cgrb.eta.shared.etatype.File>();
		}
		File[] files = new File(path).listFiles(filter);
		if (files == null) {
			return list;
		}
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
	private FileFilter filter = new FileFilter() {

		public boolean accept(File pathname) {
			try {
				return pathname.canRead();
			} catch (Exception e) {
				return false;
			}
		}
	};
}
