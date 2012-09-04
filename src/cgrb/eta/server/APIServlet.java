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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.server.services.AuthenticationService;

public class APIServlet extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5241290948379464149L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri=req.getRequestURI();
		System.out.println(uri);
		String[] uriBrokenDown = uri.split("\\/");
		switch (uriBrokenDown[2]) {
		case "auth":
			authenicate(req,resp);
			break;
		default:
			break;
		}
	}

	private void authenicate(HttpServletRequest req, HttpServletResponse resp) {
		String user = req.getParameter("user");
		String password = req.getParameter("pass");
		if(user!=null&&password!=null){
			if(AuthenticationService.getService().checkCredentials(user, password)){
				//now we have to issue a token that will be used to authenticate with the api connection when it tries to connect
				String token = generateToken();
				String cypher = generateToken();
				SqlManager sql = SqlManager.getInstance();
				String userId = sql.runQuery(sql.getPreparedStatement("select id from user where username=?", user)).get(0)[0];
				sql.executeUpdate(sql.getPreparedStatement("insert into user_api_connection values(?,?,"+userId+",null)",token,cypher));
				try {
					resp.getWriter().append("{\"response\":\"ok\",\"return\":{\"token\":\""+token+"\",\"cypher\":\""+cypher+"\"}}");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
		}
			try {
				resp.getWriter().append("{\"response\":\"failed\"}");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	public String generateToken() {
		String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
		int string_length = 30;
		String randomstring = "";
		for (int i = 0; i < string_length; i++) {
			int rnum = (int) Math.floor(Math.random() * chars.length());
			randomstring += chars.substring(rnum, rnum + 1);
		}
		return randomstring;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

}
