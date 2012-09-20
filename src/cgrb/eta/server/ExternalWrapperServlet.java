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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.remote.etastart.RemoteUserService;
import cgrb.eta.server.services.UserManagerService;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.etatype.User;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Output;
import cgrb.eta.shared.wrapper.Wrapper;

public class ExternalWrapperServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -295900621901159821L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doing get");
		String key = req.getParameter("apikey");
		String resultKey = req.getParameter("resultkey");
		String function = req.getParameter("function");
		String callBack = req.getParameter("callback");
		resp.setContentType("text/javascript");

		if (key == null) {
			resp.getWriter().println("<div class=\"error\">Sorry it looks like you have accessed an invalid or expired page.</div>");
			return;
		}

		SqlManager sql = SqlManager.getInstance();
		Vector<String[]> wrappers = sql.runQuery(sql.getPreparedStatement("select * from external_wrapper e where e.key=?", key));
		if (wrappers.size() != 1) {
			resp.getWriter().write(callBack + "(\"" + escape("<div class=\"error\">Sorry it looks like you have accesses an invalid or expired page.</div>") + "\");");
			return;
		}
		// else if (!req.getHeader("Referer").startsWith(wrappers.get(0)[2])) {
		// resp.getWriter().write(callBack + "(\"" + escape("<div class=\"error\">Sorry it looks like you have accesses an invalid or expired page. the allowed host is not " + req.getHeader("Host") + " it is " + wrappers.get(0)[2] + "</div>") + "\");");
		// return;
		// }

		if (resultKey != null) {
			// get the job status
			Vector<String[]> jobs = sql.runQuery(sql.getPreparedStatement("select j.status,j.time,j.id from public_result p left join job j on j.id=p.job left join external_wrapper e on e.id=p.external_wrapper where p.key=? and e.key=?", resultKey, key));
			if (jobs.size() != 1) {
				String html = "<div class=\"error\">Sorry it looks like you have accessed an invalid or expired page.</div>";
				resp.getWriter().write(callBack + "(\"" + escape(html) + "\");");

				return;
			}
			Job job = sql.getJob(Integer.parseInt(jobs.get(0)[2]));
			Wrapper w = job.getWrapper();
			Vector<Output> outputs = w.getOutputs();
			// check to see if the request is to download or view a file
			if (function != null && (function.equals("view") || function.equals("download"))) {
				String name = req.getParameter("name");
				if (name == null)
					return;
				String filePath = null;
				if (name.equals("stdout")) {
					// print stdout
					filePath = job.getWorkingDir() + "/stdout";
				} else if (name.equals("stderr")) {
					filePath = job.getWorkingDir() + "/stderr";

				}
				// go though all the outputs and see if the name matches up and get the filepath
				for (Output output : outputs) {
					if (output.getName().equals(name)) {
						filePath = output.getValue();
						String file = "";
						if (!job.getWorkingDir().equals("")) {
							file = job.getWorkingDir() + "/";
						}
						char[] tempValue = output.getValue().toCharArray();
						String realValue = "";
						String var = "";
						boolean inInput = false;
						for (char ch : tempValue) {
							if (inInput) {
								if (ch == '\'' && !var.equals("")) {
									// get the value for the input named var
									Vector<Input> pars = w.getInputs();
									for (Input p : pars) {
										if (p.getName().equals(var)) {
											realValue += p.getValue();
											break;
										}
									}
									var = "";
									inInput = false;
								} else {
									if (ch != '\'') {
										var += ch;
									}
								}
							} else if (ch == '$') {
								inInput = true;
							} else {
								realValue += ch;
							}
						}
						file += realValue;
						if (realValue.startsWith("/"))
							file = realValue;
						filePath = file;

					}
				}
				if (filePath != null) {
					if (function.equals("view")) {
						String html = "";
						BufferedReader reader = new BufferedReader(new FileReader(new File(filePath)));
						String line = "";
						while ((line = reader.readLine()) != null) {
							html += line + "\n";
						}
						resp.setContentType("text/html");
						resp.getWriter().write(callBack + "(\"" + escape(html) + "\");");
						reader.close();
					} else {
						// open an octect stream and download the file
						File f = new File(filePath);
						ServletOutputStream op = resp.getOutputStream();
						ServletContext context = getServletConfig().getServletContext();
						String mimetype = context.getMimeType(f.getName());
						resp.setContentType((mimetype != null) ? mimetype : "application/octet-stream");
						resp.setContentLength((int) f.length());
						resp.setHeader("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"");
						FileInputStream inputStream = new FileInputStream(f);
						byte[] buffer = new byte[1024 * 512];
						int len;
						while ((len = inputStream.read(buffer)) > 0) {
							op.write(buffer, 0, len);
						}
						inputStream.close();
						op.flush();
						op.close();
					}
				}

				return;
			}
			// print the results
			String html;
			if (jobs.get(0)[0].contains("Finished")) {
				html = "<div id=\"results\"><div id=\"left_result\">Program:" + w.getProgram() + "<br>Status:" + jobs.get(0)[0] + " since " + jobs.get(0)[1]
						+ "<br>Std Out:<a href=\"#\" onclick=\"wrapper.view('stdout');\">View</a> <a href=\"#\" onclick=\"wrapper.download('stdout');\">Download</a><br>Std Err:<a href=\"#\" onclick=\"wrapper.view('stderr');\">View</a> <a href=\"#\" onclick=\"wrapper.download('stderr');\">Download</a><br>";
				for (Output output : outputs) {
					html += "<br>" + output.getName() + ":<a href=\"#\" onclick=\"wrapper.view('" + output.getName() + "');\">View</a> <a href=\"#\" onclick=\"wrapper.download('" + output.getName() + "');\">Download</a>";
				}
			} else {
				html = "<div id=\"results\"><div id=\"left_result\">Program:" +w.getProgram() + "<br>Status:" + jobs.get(0)[0] + " since " + jobs.get(0)[1] + "<script type='text/javascript'>setTimeout('window.location.reload()',30000);</script>";
			}


			html += "</div><div id=\"right_result\"></div></div>";
			resp.getWriter().write(callBack + "(\"" + escape(html) + "\");");

			return;
		}

		Wrapper wrapper = sql.getWrapperFromId(Integer.parseInt(wrappers.get(0)[1]));
		Vector<Input> parameters = wrapper.getInputs();
		// <style type=\"text/css\">.req{color:red;float:left;}</sheet>
		String html = " <style type=\"text/css\">.req{color:red;float:left;}</style><div>Please fill in the following fields and click submit.<br> <div class=\"req\">*</div> are required.</div><form id=\"wrapper_form\" method=post target=\"hiddenframe\" enctype=\"multipart/form-data\" action=\""
				+ Settings.getInstance().getSetting("etaHost").getStringValue() + "/external\"><table>\n<tr style=\"background-color:#81BEF7;\"><td>Name</td><td>Description</td><td>Value</td></tr>";
		boolean gray = true;
		for (Input p : parameters) {
			boolean required = p.isRequired();
			if (p.getDisplayType().equals("Hidden")) {
				html += "<input name=\"" + p.getName() + "\" id=\"" + p.getName() + "\" type=\"hidden\"";
				if (p.getValue() != null && !p.getValue().equals("")) {
					html += " value=\"" + p.getValue() + "\"";
				}
				html += "/>";
			} else {
				html += "<tr id=\"" + p.getName() + "_row\" class=\"" + (required ? "required" : "optional") + (gray ? "\" style=\"background-color: #E6E6E6;" : "") + "\"><td>" + (required ? "<div class=\"req\">*</div>" : "") + p.getName() + "</td><td>" + p.getDescription() + "</td><td>";
				String type = p.getType();
				if (type.equals("String")) {
					html += "<input name=\"" + p.getName() + "\" id=\"" + p.getName() + "\" type=\"text\"";
					if (p.getValue() != null && !p.getValue().equals("")) {
						html += " value=\"" + p.getValue() + "\"";
					}
					html += "/>";
				} else if (type.equals("Number")) {
					html += "<input name=\"" + p.getName() + "\" id=\"" + p.getName() + "\" class=\"number\" type=\"text\"";
					if (p.getValue() != null && !p.getValue().equals("")) {
						html += " value=\"" + p.getValue() + "\"";
					}
					html += "/>";
				} else if (type.equals("Flag")) {
					html += "<input name=\"" + p.getName() + "\" id=\"" + p.getName() + "\" type=\"checkbox\"";
					if (p.getValue() != null && !p.getValue().equals("")) {
						html += " value=\"" + p.getValue() + "\"";
					}
					html += "/>";
				} else if (type.startsWith("Selection")) {
					html += "<select name=\"" + p.getName() + "\" id=\"" + p.getName() + "\">";
					String[] options = type.split(":")[1].split(",");
					for (String option : options) {
						html += "<option value=\"" + option + "\">" + option + "</option>";
					}
					html += "</select>";
				} else if (type.startsWith("File")) {
					html += "<input name=\"" + p.getName() + "\" id=\"" + p.getName() + "\" type=\"file\">";
					html += "</input>";
					// add an option for text entering
					html += "</td></tr>";
					html += "<tr id=\"" + p.getName() + "@_row\" class=\"" + (required ? "required" : "optional") + (gray ? "\" style=\"background-color: #E6E6E6;" : "") + "\"><td>Or enter text:</td><td colspan=2> <textarea name=\"" + p.getName() + "@"
							+ "\" style=\"width:98%;height:90px\" ></textarea></td><td>";

				}
				html += "</td></tr>";
				gray = !gray;
			}
		}
		html += "<tr style=\"background-color:#81BEF7;\"><td><input type=\"hidden\" name=\"apikey\" value=\""
				+ key
				+ "\"</td><td><div class=\"req\">*</div>Email Address:<input type=\"text\" name=\"email\" id=\"email\" class=\"required\"/></td><td><button type=\"button\" onclick=\"wrapper.submitjob();\">submit</button></td></tr></table></form><iframe name=\"hiddensubmit\" id=\"hiddensubmit\" style=\"display:none;\"></iframe>";
		resp.getWriter().write(callBack + "(\"" + escape(html) + "\");");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doing post");
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		HashMap<String, String> parms = new HashMap<String, String>();
		String publicFolder = Settings.getInstance().getSetting("publicWorkingDir").getStringValue();

		// create a random folder to put the input files and results

		RemoteUserService publicConnection = UserManagerService.getService().getUserService(0);
		if (publicConnection == null) {
			User publicUser = new User();
			publicUser.setId(0);
			publicUser.setUsername(Settings.getInstance().getSetting("publicUser").getStringValue());
			publicUser.setPassword(Settings.getInstance().getSetting("publicPassword").getStringValue());
			UserManagerService.getService().userLogedIn(publicUser);

			// delay a little then get publicConnection again
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			publicConnection =UserManagerService.getService().getUserService(0);
			if (publicConnection == null) {
				System.out.println("can't spawn the public user :(");
				return;
			}
		}
		File workingDir = new File( publicConnection.makedir( publicFolder));

		try {
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(req);
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();

				if (item.isFormField()) {
					parms.put(item.getFieldName(), item.getString());
				} else {
					System.out.println("file name=" + item.getName());
					System.out.println("file size=" + item.getSize());
					// its a file!!
					// add a check for file sizetrue
					if (item.getSize() > 100 * 1024 * 1024) {
						// too big do something
					} else if (item.getSize() > 0) {
						InputStream uploadedStream = item.getInputStream();
						parms.put(item.getFieldName(), workingDir.getAbsolutePath() + "/" + item.getName());
						File file = new File((workingDir.getAbsolutePath() + "/" + item.getName()));
						byte[] buffer = new byte[512 * 1024];
						int length;
						while ((length = uploadedStream.read(buffer)) != -1) {
							if (length != buffer.length) {
								byte[] newBuffer = new byte[length];
								for (int i = 0; i < newBuffer.length; i++) {
									newBuffer[i] = buffer[i];
								}
								buffer = newBuffer;
							}
							String ret = publicConnection.saveFileBuffer(file.getAbsolutePath(), buffer, length);
							if (!ret.equals("")) {
								System.out.println("error:" + ret);
								break;
							}
						}
						uploadedStream.close();
						publicConnection.saveFileBuffer(file.getAbsolutePath(), new byte[] {}, 0);
					}

				}
			}

			SqlManager sql = SqlManager.getInstance();
			String key = parms.get("apikey");
			Vector<String[]> wrappers = sql.runQuery(sql.getPreparedStatement("select * from external_wrapper e where e.key=?", key));
			if (wrappers.size() != 1)
				return;
			Wrapper wrapper = sql.getWrapperFromId(Integer.parseInt(wrappers.get(0)[1]));
			Vector<Input> parameters = wrapper.getInputs();
			Wrapper newWrapper = new Wrapper();
			newWrapper.setName(wrapper.getName());
			newWrapper.setProgram(wrapper.getProgram());
			newWrapper.setId(wrapper.getId());
			for (Input p : parameters) {
				String newValue = parms.get(p.getName());
				if (p.getType().startsWith("File")) {
					if (parms.containsKey(p.getName() + "@")) {
						String tempValue = parms.get(p.getName() + "@");
						if (tempValue != null && !tempValue.equals("")) {
							File file = new File((workingDir.getAbsolutePath() + "/" + p.getName().replaceAll(" ", "_")));
							publicConnection.saveFileBuffer(file.getAbsolutePath(), tempValue.getBytes(), tempValue.length());
							publicConnection.saveFileBuffer( file.getAbsolutePath(), new byte[0], 0);
							p.setValue(file.getAbsolutePath());
						}
					}
				}
				if (newValue != null && !newValue.equals("")) {
					p.setValue(newValue);
				}
				newWrapper.addInput(p);
			}
			wrapper = newWrapper;
			Job job = new Job();
			job.setWorkingDir(workingDir.getAbsolutePath());
			job.setWrapper(wrapper);
			job.setUserId(0);
			job.setName(wrapper.getName());
			if(!(wrappers.get(0)[4]==null||wrappers.get(0)[4].equals("")))
			job.setSpecs("-q " + wrappers.get(0)[4]);
			else 
				job.setSpecs("");

			// submit job to the cloud!
			int jobid = CommunicationImpl.getInstance().runJob(job,0);
			sql.executeUpdate(sql.getPreparedStatement("insert into external_notifications values(null," + jobid + ",?) ", parms.get("email")));
			// generate a random 30 digit key
			Random r = new Random();
			String token = Long.toString(Math.abs(r.nextLong()), 36) + Long.toString(Math.abs(r.nextLong()), 36);
			sql.executeUpdate(sql.getPreparedStatement("insert into public_result values(null," + jobid + ",?," + wrappers.get(0)[0] + ",?)", token, req.getHeader("Referer")));
			Notifier.sendEmail(parms.get("email"), parms.get("email"), "Job submitted", "You job has been submitted, you can view the progress here:" + req.getHeader("Referer") + "?job=" + token);
			String html = "	<html><head><meta http-equiv=\"REFRESH\" content=\"0;url=" + req.getHeader("Referer") + "?job=" + token + "\"></HEAD></HTML>";
			resp.getWriter().write(html);

		} catch (FileUploadException e) {
			e.printStackTrace();
		}
	}

	public String escape(String s) {
		if (s == null)
			return null;
		StringBuffer sb = new StringBuffer();
		escape(s, sb);
		return sb.toString();
	}

	public void escape(String s, StringBuffer sb) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}// for
	}
}
