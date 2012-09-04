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
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cgrb.eta.server.mysql.SqlManager;
import cgrb.eta.shared.ETAEvent;
import cgrb.eta.shared.etatype.Cluster;
import cgrb.eta.shared.etatype.ETATypeEvent;
import cgrb.eta.shared.etatype.PendingCluster;

public class ClusterServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4855218164290788660L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// this part will recognize a request for access to use the cloud for external use
		// will need the server address, a request id,
		String request = req.getParameter("request");
		String organization = req.getParameter("org");
		String email = req.getParameter("email");
		String username = req.getParameter("username");
		String server = req.getParameter("server");
		String userId = req.getParameter("userid");
		String hashKey = req.getParameter("key");
		String globalKey = req.getParameter("globalKey");
		String response = req.getParameter("response");
 		SqlManager sql = SqlManager.getInstance();
		// this is when another instance is requesting access to run jobs on this cluster
		if (request != null && organization != null && email != null && username != null && server != null && userId != null&&response==null) {
			int userIdI = Integer.parseInt(userId);
			int id = sql.executeUpdate(sql.getPreparedStatement("insert into pending_cluster(request,organization, username,user,email,status,type,server) values (?,?,?," + userIdI + ",?,?,1,?)", request, organization, username, email, "Pending", server));
			PendingCluster pending = new PendingCluster(email, request, organization, "Pending", username);
			pending.setId(id);
			if (sql.runQuery(sql.getPreparedStatement("select * from auto_cluster where server=?", server)).size() > 0) {
				// TODO: do the step where we generate a the key and send it back to the server
				sql.executeUpdate(sql.getPreparedStatement("update pending_cluster set status='Accepted' where id=" + id));
				pending.setStatus("Accepted");
			}
			CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.ADDED, pending)), -1);
			resp.getWriter().write("OK");
			resp.flushBuffer();

		} else if (request != null && response != null && globalKey != null && hashKey != null) {
			System.out.println("approved!!");
			if (response.equals("denied")) {
				// sorry the request was denied :( hmm what do I do?
				
			} else {
				PendingCluster pending = sql.getPendingCluster(request);
				int globalClusterId;
				Vector<String[]> temp = sql.runQuery(sql.getPreparedStatement("select id from global_cluster where address=? and global_cluster.key=? and type=1", pending.getServer(), globalKey));
				if (temp.size() > 0)
					globalClusterId = Integer.parseInt(temp.get(0)[0]);
				else
					globalClusterId = sql.executeUpdate(sql.getPreparedStatement("insert into global_cluster values (null,?,?,1 )", pending.getServer().split(":")[0], globalKey));
				sql.executeUpdate(sql.getPreparedStatement("insert into user_cluster values(null," + globalClusterId + ",?," + Integer.parseInt(userId) + ",?)", hashKey,organization));
				sql.executeUpdate(sql.getPreparedStatement("update pending_cluster set status='Approved' where request=?", request));
				//TODO: make it so this event gets broadcasted to the right person. maybe even send them an email
				pending.setStatus("Approved");
				ClusterAPIService.getInstance().startConnections();
				CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<PendingCluster>(ETATypeEvent.UPDATED, pending)), pending.getUserId());
				Cluster newCluster = sql.getCluster(globalClusterId, pending.getUserId());
				CommunicationImpl.getInstance().addEvent(new ETAEvent(ETAEvent.ETA_TYPE, new ETATypeEvent<Cluster>(ETATypeEvent.UPDATED, newCluster)), pending.getUserId());

				resp.getWriter().write("Good");
				resp.flushBuffer();
			}
		}
	}
}
