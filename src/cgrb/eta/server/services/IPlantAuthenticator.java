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

import java.util.HashMap;

import com.google.gson.JsonParser;

import cgrb.eta.server.HTTPSPost;

public class IPlantAuthenticator extends AuthenticationService {
	public static final String ADDRESS = "https://foundation.iplantcollaborative.org/";
	private static HashMap<String,String> userTokens = new HashMap<String,String>();
	@Override
	public boolean checkCredentials(String user, String password) {
			HashMap<String, String> settings = new HashMap<String, String>();
			settings.put("lifetime", "1209600");
			String ret = HTTPSPost.post(ADDRESS + "auth-v1/", settings, user, password);
			if (ret ==null)return false;
			String token = new JsonParser().parse(ret).getAsJsonObject().getAsJsonObject("result").getAsJsonPrimitive("token").getAsString();
			userTokens.put(user, token);
			return true;
	}

	@Override
	public String changePassword(String user, String oldPassword, String newPassword) {
		return null;
	}

	@Override
	public String getUserFullName(String username) {
		return username;
	}

	public static String getUserToken(String username){
		return userTokens.get(username);
	}
	
}
