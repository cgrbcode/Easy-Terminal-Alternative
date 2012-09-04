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
package cgrb.eta.remote.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cgrb.eta.server.HTTPSPost;
import cgrb.eta.server.rmi.AESRMIConnection;
import cgrb.eta.server.rmi.ConnectionListener;
import cgrb.eta.shared.etatype.Job;
import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;

/**
 * This serves as the API entry point to any other ETA instance. This can be used to submit a job, 
 * monitor said job, query the file system, and get file contents for job outputs. Although this
 * side of the code is done, the server side may not be. As of now, you can connect to an ETA server 
 * and query the file system but the code for sending a job over and monitoring it is not complete.
 * It should be done by 7/14/2012 though.
 * 
 * More methods will be added when other needs are found.
 * 
 * @author Alexander Boyd
 * 
 */
public final class ETAConnection {

	private String token;
	private String cypher;
	private String ETAAddress;
	private ETAConnectionService connection;
	private UserConnectionImpl impl = new UserConnectionImpl();

	/**
	 * This will attempt to construct a new instance. This will first make an https call to the ETA instance that is parsed from the userName and check to see if the user can be authenticated. If it is a valid user than this new instance will be returned and if the userName is invalid then an
	 * InvalidUserNameException will be thrown.
	 * 
	 * The userName is invalid if it doesn't contain an @ or if the url after the @ turns out to be an invalid ETA instance.
	 * 
	 * @param userName
	 *          the full userName of the user. This should look like boyda@eta.cgrb.oregonstate.edu.
	 * @param password
	 *          the password of the user account.
	 * @throws InValidUserNameException
	 */
	public ETAConnection(String userName, String password) throws InValidUserNameException {
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
		connect();
	}

	private void connect() {
		try {
			Socket s = new Socket(ETAAddress, 3289);
			s.getOutputStream().write(token.getBytes());
			s.getOutputStream().write(new byte[]{0,0,0});
			AESRMIConnection connection = new AESRMIConnection(cypher, s, impl, true, new ConnectionListener() {
				public void connectionLost() {
				}
			});
			System.out.println("connected");
			this.connection = (ETAConnectionService) connection.getService(ETAConnectionService.class);
			System.out.println("got connection");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will return a list of files that are in the folder that is specified
	 * 
	 * @param path The absolute file path to get files from. If this is an empty string the users home folder will be listed 
	 * @return A list of {@link cgrb.eta.shared.etatype.File} that are present in the path provided
	 */
	public Vector<cgrb.eta.shared.etatype.File> getFiles(String path) {
		return connection.getFiles(path);
	}

	/**
	 * This will register a JobListener for the specified job to be called whenever the status of the job changes.
	 * 
	 * @param job The job number to add this listener to.
	 * @param listener The job listener that will be called when the job status changes
	 * @see JobListener
	 */
	public void addJobListener(int job,JobListener listener){
		impl.addJobListener(job, listener);
	}
	
	/**
	 * @param program The name of the program you are attempting to use
	 * @param options The command line options that you wish to use, these often start with a -. For example if I want to run the command
	 * 								raxmlHPC-PTHREADS -T 10 -f a ... you would call getWrapper("raxmlHPC-PTHREADS","-T","-f")
	 * @return The wrapper that fits your needs. If a wrapper can not be found null will be returned
	 */
	public Wrapper getWrapper(String program, String... options){
		return connection.requestWrapper(program, options);
	}
	
	/**
	 * This will take a {@link Job} and go though the {@link Wrapper} and copy all the input files over to the server
	 * and then start the job. The reason we submit a job though the Job class is so the user using this API can 
	 * specify job options, like how many threads it needs and how much memory to use. 
	 * 
	 * @param job The {@link Job} object to run
	 * @return the job number that was submitted
	 */
	public int runJob(Job job){
		int jobId = connection.setupJob(job);
		Wrapper wrapper = job.getWrapper();
		for(Input input:wrapper.getInputs()){
			if(input.getType().startsWith("File")){
				//we need to copy this file over
				File copying = new File(input.getValue());
				if(copying.exists()){
					byte[] buff = new byte[1024*8];
					try {
						InputStream in = new FileInputStream(copying);
						int readBytes = 0;
						while((readBytes=in.read(buff))>0){
							if(readBytes!=buff.length){
								byte[] newBuff = new byte[readBytes];
								for(int i=0;i<newBuff.length;i++){
									newBuff[i]=buff[i];
								}
								buff=newBuff;
								break;
							}
							connection.saveFileBuffer(jobId, "/"+copying.getName(), buff);
						}
						in.close();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return connection.runJob(job);
	}
	
	/**
	 * This is a fall back method and probably shouldn't be used unless you have to. 
	 * You should always use the method getWrapper but if for some reason it won't work 
	 * for the command you need to run this method can be used. 
	 * 
	 * @param command The full command that you wish to run on the external server
	 * @return {@link Wrapper} that will run the command specified.
	 */
	public Wrapper getWrapperForCommand(String... command){
		Wrapper ret = new Wrapper();
		ret.setProgram(command[0]);
		for(int i=1;i<command.length;i++){
			if(new File(command[i]).exists()){
				//this is a file, make it a file input
				ret.addInput(new Input(0, "", "Arg "+i, command[i], "", false, i, "default", "File"));
			}else{
				ret.addInput(new Input(0, "", "Arg "+i, command[i], "", false, i, "default", "String"));
			}
		}
		ret.setDescription("Auto generated though the eAPI");
		ret.setName(command[0]);
		return ret;
	}
}
