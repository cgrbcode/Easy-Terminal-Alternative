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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.MalformedQueryException;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.shared.wrapper.Wrapper;

/**
 * @author Alexander Boyd alexeb112@gmail.com AuthServlet.java
 */

public class AuthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public String isAuthenticated(String token) {
		Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select t.user from external_token e left join token t on t.id=e.token where external_token=?", token));
		if (temp.size() > 0 && temp.get(0)[0] != null) {
			return temp.get(0)[0];
		}
		return "-1";
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String token = req.getParameter("token");
		String callback = req.getParameter("callback");
		String request = req.getParameter("request");
		String sessionId = req.getParameter("session");
		String query = req.getParameter("query");
		String internalToken = "";
		String site = req.getParameter("site");
		resp.setContentType("application/x-javascript");
		String user = isAuthenticated(token);

		Cookie[] cookies = req.getCookies();
		if (cookies != null)
			for (Cookie cook : cookies) {
				if (cook.getName().equals("token")) {
					internalToken = cook.getValue();
					user = SqlManager.getInstance().getUser(internalToken).getId()+"";
					if (user == "-1") {
						SqlManager.getInstance().executeUpdate(SqlManager.getInstance().getPreparedStatement("insert into external_token (token,external_token,site) select id,?,? from token t where token=? on duplicate key update token=t.id", token, site, internalToken));
						user = isAuthenticated(token);
					}
				}
			}
		if (callback == null)
			callback = "";
		if(!user.equals("-1")&&request!=null&&request.equals("download")){
			File file = new File(query);
			resp.setContentType("application/octet-stream");
			downloadFile(req, resp, file, Integer.parseInt(user), false);
			return;
		}
		
		
		
		
		if (sessionId != null) {
			// this is a plugin request check to see if the session is public
			PluginSession sess = PluginService.getInstance().getSession(sessionId);
			if (sess.isPublic() || sess.getUser() == Integer.parseInt(user) || SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select * from shares where user=" + user + " and session=?", sessionId)).size() > 0) {
				// sess is public or user owns this session return or user has access
				// its ok do what the plugin wants
				if (query == null)
					query = "select filename";

				if (query.startsWith("run command")) {
					String command[] = query.substring(12).split(" ");
					RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
					if (con == null) {
						resp.getWriter().println(callback + "({\"response\":\"false\"});");
						return;
					} else {
						con.runPluginCommand(command, sess.getFilenames());
						resp.getWriter().println(callback + "({\"response\":\"true\"});");
					}
				} else if (query.startsWith("download")) {
					RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
					if (con == null) {
						resp.getWriter().println(callback + "({\"response\":\"false\"});");
						return;
					} else {
						String file = sess.getFilenames();
						if (query.length() > 10)
							file += "/" + query.substring(10);
						file = sess.getFilenames() + "/" + query.substring(9);
						resp.setContentType("application/octet-stream");
						downloadFile(req, resp, new File(file), sess.getUser(), false);
						return;
					}
				} else if (query.startsWith("view file")) {
					RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
					if (con == null) {
						resp.getWriter().println(callback + "({\"response\":\"false\"});");
						return;
					} else {
						String file = sess.getFilenames();
						if (query.length() > 9) {
							String tempFile = query.substring(9);
							if (tempFile.contains(" as ")) {
								String[] asS = tempFile.split(" as ");
								tempFile = asS[0];
								resp.setContentType(asS[1].split(" ")[0] + "/" + asS[1].split(" ")[1]);
							}
							if (tempFile != null && !tempFile.equals(""))
								file += "/" + tempFile;
						}
						downloadFile(req, resp, new File(file), sess.getUser(), false);
						return;
					}
				} else {
					resp.getWriter().print(callback + "({\"response\":");
					if (query != null) {
						query(sess, req, resp, query);
					}
					resp.getWriter().print("});");
				}
			} else {
				resp.getWriter().println(callback + "({\"response\":\"false\"});");
			}
		} else if (user.equals("-1")) {
			resp.getWriter().println(callback + "({\"response\":\"false\"});");
		} else {
			if (request == null) {
				resp.getWriter().println(callback + "({\"response\":\"true\"});");
			} else if (request.equals("jobs")) {
				Vector<String[]> jobs = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select j.name,j.id from job j where j.status<>'Finished' and j.user=" + user));
				String ret = callback + "({\"response\":[";
				for (int i = 0; i < jobs.size(); i++) {
					String[] job = jobs.get(i);
					ret += "{\"name\":\"" + job[0] + "\",\"id\":\"" + job[1] + "\"}";
					if (i + 1 != jobs.size()) {
						ret += ",";
					}
				}
				ret += "]});";
				resp.getWriter().println(ret);
			} else if (request.equals("userName")) {
				Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select name from user where id=" + user));
				resp.getWriter().println(callback + "({\"response\":\"" + temp.get(0)[0] + "\"});");

			} else if (request.equals("user")) {
				Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select name,username,permission,email from user where id=" + user));
				resp.getWriter().println(callback + "({\"response\":{\"name\":\"" + temp.get(0)[0] + "\",\"username\":\"" + temp.get(0)[1] + "\",\"permission\":" + temp.get(0)[2] + ",\"email\":\"" + temp.get(0)[3] + "\"}});");

			} else if (request.equals("installWrapper")) {
				try {
					SqlManager sql = SqlManager.getInstance();
					// TODO: get the public wrapper from eta-dev.cgrb.oregonstate.edu
					HashMap<String, String> parms = new HashMap<String, String>();
					FileItemFactory factory = new DiskFileItemFactory();
					ServletFileUpload upload = new ServletFileUpload(factory);

					@SuppressWarnings("unchecked")
					List<FileItem> items = upload.parseRequest(req);
					Iterator<FileItem> iter = items.iterator();
					while (iter.hasNext()) {
						FileItem item = iter.next();
						if (item.isFormField()) {
							parms.put(item.getFieldName(), item.getString());
						}
					}

					String publicId = parms.get("publicId");
					Vector<String[]> temp = sql.runQuery(sql.getPreparedStatement("select id from command where public_id=" + Integer.parseInt(publicId)));
					Wrapper wrap = getWrapperFromJson(parms.get("wrapper"));
					if (temp.size() > 0) {
						sql.executeUpdate(sql.getPreparedStatement("insert into wrappers values (null,0," + user + ",?," + temp.get(0)[0] + ")", wrap.getName()));
						resp.getWriter().println(callback + "({\"response\":\"true\"});");
						return;
					}
					wrap.setCreator(user);
					String localId = sql.saveAsWrapper(wrap).getId() + "";

					sql.executeUpdate(sql.getPreparedStatement("update command set public=1, public_id=" + Integer.parseInt(publicId) + " where id=" + Integer.parseInt(localId)));
					resp.getWriter().println(callback + "({\"response\":\"true\"});");

				} catch (FileUploadException e) {
					e.printStackTrace();
				}

			} else if (request.equals("setPublicId")) {
				String localId = req.getParameter("localId");
				String publicId = req.getParameter("publicId");
				SqlManager sql = SqlManager.getInstance();
				sql.executeUpdate(sql.getPreparedStatement("update command set public_id=" + Integer.parseInt(publicId) + " where id=" + Integer.parseInt(localId)));
				resp.getWriter().println(callback + "({\"response\":\"true\"});");

			} else if (request.equals("wrappers")) {
				Vector<String[]> temp = SqlManager.getInstance().runQuery(SqlManager.getInstance().getPreparedStatement("select w.id,w.parent,w.name,w.command from wrappers w left join command c on c.id=w.command  where( w.user=" + user + " or w.user=0) and (c.creator=" + user + " or w.command=0)"));
				resp.getWriter().print(callback + "({\"response\":[");
				for (int i = 0; i < temp.size(); i++) {
					resp.getWriter().print("{\"id\":\"" + temp.get(i)[0] + "\",\"parent\":\"" + temp.get(i)[1] + "\",\"name\":\"" + temp.get(i)[2] + "\",\"command\":\"" + temp.get(i)[3] + "\"}");
					if (i < temp.size() - 1)
						resp.getWriter().print(",");
				}
				resp.getWriter().println("]});");

			} else if (request.equals("getWrapper")) {
				resp.getWriter().print(callback + "({\"response\":");
				resp.getWriter().println(SqlManager.getInstance().getWrapperFromId(Integer.parseInt(req.getParameter("wrapperId"))).toString() + "});");
			} else if (request.equals("logout")) {
				CommunicationImpl.getInstance().removeTokens(Integer.parseInt(user));
				resp.getWriter().println(callback + "({\"response\":\"true\"});");
			} else if (request.equals("download")) {
				String file =  UserManagerService.getService().getUserService(Integer.parseInt(user)).downloadFile( req.getParameter("file"));
				resp.getWriter().println(callback + "({\"response\":\"" + file + "\"});");
			}
		}

	};

	private void downloadFile(HttpServletRequest req, HttpServletResponse resp, File f, int user, boolean asText) throws IOException {
		ServletOutputStream op = resp.getOutputStream();
		ServletContext context = getServletConfig().getServletContext();
		RemoteUserService con = UserManagerService.getService().getUserService(user);
		String mimetype = context.getMimeType(f.getName());
		if (!asText) {
			if (resp.getContentType() == null || resp.getContentType().equals("application/x-javascript"))
				resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
			resp.setContentLength(Integer.parseInt("" + (Long) con.getFileSize(f.getAbsolutePath())));
			resp.setHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
		}
		while (true) {
			byte[] bbuf = (byte[]) con.getFileBuffer( f.getAbsolutePath());
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

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		doGet(req, resp);
	}

	private void query(PluginSession sess, HttpServletRequest req, HttpServletResponse resp, String query) throws IOException {
		RemoteUserService con = UserManagerService.getService().getUserService(sess.getUser());
		if (con == null) {
			resp.getWriter().println(" for user: " + sess.getUser() + " not running");
			return;
		}
		HashMap<String, String> queryAsHash = query(query);
		Object result=null;
		try {
			result = con.runQuery( queryAsHash, sess.getFilenames());
		} catch (MalformedQueryException e) {
			result=e;
		}

		if (result instanceof String[][]) {
			String[][] res = (String[][]) result;
			if (res == null || res.length == 0) {
				resp.getWriter().println("\"error in query!\"");
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
				resp.getWriter().println("\"error in query\"");

			return;
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
				if (on.equals("where")) {
					keys.put(on, keys.get(on) + " " + temp);
				} else {
					keys.put(on, keys.get(on) + temp);
				}
			}
		}
		return keys;
	}

	private Wrapper getWrapperFromJson(String json) {
		Wrapper ret = new Wrapper();
		ret = new Gson().fromJson(json, Wrapper.class);
		return ret;
	}

}
