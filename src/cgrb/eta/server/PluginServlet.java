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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import cgrb.eta.server.remote.MalformedQueryException;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.UploadEvent;

public class PluginServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4036831651438937511L;
	static final byte[] buffer = new byte[1024 * 512];

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		String id = req.getParameter("id");
		PluginSession sess = PluginService.getInstance().getSession(id);
		try {
			if (sess != null) {
				String remote = req.getRemoteHost();
				if (req.getParameter("function") == null && req.getParameter("query") == null) {
					String file = sess.getFilenames();
					File f = new File(file);
					if (f.isDirectory()) {
						File[] files = f.listFiles();
						for (File fi : files) {
							resp.getWriter().println(fi.getName());
						}
					} else {
						resp.getWriter().println(f.getName());
					}
					return;

				} else if (req.getParameter("query") != null) {
					String query = req.getParameter("query");
					RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
					HashMap<String, String> queryAsHash = query(query);
					Object result;
					try {
						result = con.runQuery(queryAsHash, sess.getFilenames());
					} catch (MalformedQueryException e) {
						result = e;
					}

					if (result instanceof String[][]) {
						String[][] res = (String[][]) result;
						if (res == null || res.length == 0) {
							resp.getWriter().println("error in query");
							return;
						}
						if (queryAsHash.get("as").equals("json") || queryAsHash.get("as").equals("")) {
							resp.getWriter().print("{\"rows\":" + (res.length - 1) + ",\"cols\":" + (res[0].length) + ",\"data\":[");
							for (int i = 0; i < res.length; i++) {
								resp.getWriter().print("[");
								for (int i2 = 0; i2 < res[i].length; i2++) {
									resp.getWriter().print("\"" + res[i][i2] + "\"");
									if (i2 != res[i].length - 1)
										resp.getWriter().print(",");
								}
								resp.getWriter().print("]");
								if (i != res.length - 1)
									resp.getWriter().print(",");
							}
							resp.getWriter().print("]}");
						} else if (queryAsHash.get("as").equals("raw")) {
							for (int i = 0; i < res.length; i++) {
								for (int i2 = 0; i2 < res[i].length; i2++) {
									resp.getWriter().print(res[i][i2]);
								}
								resp.getWriter().println();
							}
						} else if (queryAsHash.get("as").equals("tab")) {
							for (int i = 0; i < res.length; i++) {
								for (int i2 = 0; i2 < res[i].length; i2++) {
									resp.getWriter().print(res[i][i2]);
									if (i2 != res[i].length - 1)
										resp.getWriter().print("\t");
								}
								resp.getWriter().println();
							}
						}
					} else {
						if (result instanceof MalformedQueryException) {
							resp.getWriter().println(((MalformedQueryException) result).getLocalizedMessage());
						} else
							resp.getWriter().println("error in query");

						return;
					}
				}
				if (req.getParameter("function") == null)
					return;
				if (remote.equals(sess.getUrl())) {
					if (req.getParameter("function").equals("download")) {
						File f = new File(sess.getFilenames());
						downloadFile(req, resp, f, sess.getUser());
					}

				}
				if (req.getParameter("function").equals("download")) {
					File f = new File(sess.getFilenames());
					downloadFile(req, resp, f, sess.getUser());
					PluginService.getInstance().removeSession(id);
				}
			} else {

				resp.getWriter().println("auth was null!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void downloadFile(HttpServletRequest req, HttpServletResponse resp, File f, int user) throws IOException {
		ServletOutputStream op = resp.getOutputStream();
		ServletContext context = getServletConfig().getServletContext();
		RemoteUserService con = UserManagerService.getService().getUserService(user);
		String mimetype = context.getMimeType(f.getName());
		resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
		resp.setContentLength(Integer.parseInt("" + con.getFileSize(f.getAbsolutePath())));
		resp.setHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");

		while (true) {
			byte[] bbuf = (byte[]) con.getFileBuffer(f.getAbsolutePath());
			if (bbuf.length == 0) {
				op.flush();
				op.close();
				return;
			} else {
				try {
					op.write(bbuf);
				} catch (Exception e) {
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doing post");
		DiskFileItemFactory factory = new DiskFileItemFactory();
		String publicFolder = Settings.getInstance().getSetting("publicWorkingDir").getStringValue();
		if(publicFolder.equals(""))publicFolder="/tmp";
		if (publicFolder != null)
			factory.setRepository(new File(publicFolder));
		ServletFileUpload upload = new ServletFileUpload(factory);

		HashMap<String, String> parms = new HashMap<String, String>();
		String id = null;
		for (Cookie cookie : req.getCookies()) {
			if (cookie.getName().equals("session-eta")) {
				id = cookie.getValue();
			}
		}

		try {
			List<FileItem> items;
			final PluginSession sess;
			if (id != null) {
				sess = PluginService.getInstance().getSession(id);
				upload.setProgressListener(new ProgressListener() {
					private double lastPer = 0;

					public void update(long totalRecieved, long size, int arg2) {
						double percent = (totalRecieved * 1.0) / (size * 1.0);
						if (percent - lastPer > .05) {
							CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.UPLOAD, new UploadEvent(sess.getId(), (int) (percent * 100))), sess.getUser());
							lastPer = percent;
						}
					}
				});
			} else
				sess = null;
			items = upload.parseRequest(req);

			Iterator<FileItem> iter = items.iterator();
			FileItem uploadingFile = null;
			while (iter.hasNext()) {
				FileItem item = iter.next();
				if (item.isFormField()) {
					parms.put(item.getFieldName(), item.getString());
				} else {
					uploadingFile = item;
				}
			}

			if (sess != null && uploadingFile != null) {
				if ("upload".equals(parms.get("function"))) {
					String filename = sess.getFilenames().replaceAll("\\$localpath", Settings.getInstance().getSetting("localPath").getStringValue());
					RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
					InputStream stream = uploadingFile.getInputStream();
					int length;
					double size = uploadingFile.getSize();
					System.out.println("size=" + size);
					byte[] buffer = new byte[5 * 1024 * 1024];
					if(con==null){
						OutputStream writer = new FileOutputStream(new File(filename));
						while ((length = stream.read(buffer)) > 0) {
							if (length != buffer.length) {
								byte[] newBuffer = new byte[length];
								for (int i = 0; i < newBuffer.length; i++) {
									newBuffer[i] = buffer[i];
								}
								buffer = newBuffer;
							}
							writer.write(buffer);
						}
						CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.UPLOAD, new UploadEvent(id, 100)), sess.getUser());
						stream.close();
						writer.close();
					}else{
					while ((length = stream.read(buffer)) > 0) {
						if (length != buffer.length) {
							byte[] newBuffer = new byte[length];
							for (int i = 0; i < newBuffer.length; i++) {
								newBuffer[i] = buffer[i];
							}
							buffer = newBuffer;
						}
						String ret = con.saveFileBuffer(filename, buffer, length);
						if (!ret.equals("")) {
							System.out.println("error:" + ret);
							break;
						}
					}
					CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.UPLOAD, new UploadEvent(id, 100)), sess.getUser());
					stream.close();
					con.saveFileBuffer(filename, new byte[] {}, 0);
					}
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, String> query(String query) {
		String[] q = query.split(" ");
		HashMap<String, String> keys = new HashMap<String, String>();
		keys.put("select", "");
		keys.put("where", "");
		keys.put("order", "");
		keys.put("limit", "");
		keys.put("desc", "");
		keys.put("asc", "");
		keys.put("from", "");
		keys.put("using", "");
		keys.put("as", "");
		String on = null;
		for (String temp : q) {
			if (keys.containsKey(temp.toLowerCase())) {
				on = temp.toLowerCase();
				if (temp.equals("desc")) {
					keys.put("desc", "1");
				}
			} else {
				if (on != null) {
					if (on.equals("where")) {
						keys.put(on, keys.get(on) + " " + temp);
					} else {
						keys.put(on, keys.get(on) + temp);
					}
				}
			}
		}
		return keys;
	}

}
