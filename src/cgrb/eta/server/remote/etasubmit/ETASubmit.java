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
package cgrb.eta.server.remote.etasubmit;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import cgrb.eta.server.RemoteJobService;
import cgrb.eta.server.rmi.Method;
import cgrb.eta.server.rmi.RMIConnection;
import cgrb.eta.server.rmi.Result;
import cgrb.eta.server.settings.Settings;
import cgrb.eta.shared.ETAEvent;

public class ETASubmit implements RemoteJobService {
	private Socket sock;
	private JobService con;
	private int jobId;
	String userName = System.getenv("USER");
	String server = "localhost";
	int userId;

	public ETASubmit(String[] command) {
		String jobName = "External";
		File etaSettings = new File(System.getenv("HOME") + "/ETA/.settings");
		if (etaSettings.exists()) {
			if (etaSettings.canRead()) {
				Settings settings = Settings.getInstance(etaSettings);
				userId = settings.getSetting("user").getIntValue();
				server = settings.getSetting("host").getStringValue();
			} else {
				System.out.println("Error it looks like you aren't who you seem you are. Exiting. If this is an error try to run it again.");
				System.exit(0);
			}
		} else {
			// we have to add this as an account
			try {
				Runtime.getRuntime().exec(new String[] { "ETAStart" });
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			sock = new Socket(server, 3256);
		} catch (UnknownHostException e) {
			System.out.println("Sorry couldn't connect to ETA Server:" + server + " please make sure ETA is running");
			System.exit(0);
		} catch (IOException e) {
			System.out.println("Sorry couldn't connect to ETA Server:" + server + " please make sure ETA is running");
			System.exit(0);
		}
		RMIConnection connection = new RMIConnection(sock, this, true);
		con=(JobService)connection.getService(JobService.class);
		int ret2 =  con.checkUser( userId, userName);
		if (ret2 == -2) {
			System.out.println("Sorry can't run, it looks like you are not who you say you are. If this is an error try to run it again.");
			System.exit(0);
		} else if (ret2 == -1) {
			// we need to start ETAStart
			try {
				Runtime.getRuntime().exec(new String[] { "ETAStart" });
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		boolean interactive = false;
		boolean notify = false;
		String stdOutFile ="";
		int waitfor = 0;
		String request = "-V";
		String commandS = null;
		int start = 0;
		int parent=0;
		String parentName=null;
		for (int i = 0; i < command.length; i++) {
			String c = command[i];
			if (c.equals("-R")) {
				request += " " + command[++i];
			} else if (c.equals("-c")) {
				commandS = command[++i];
				if(commandS.startsWith("'")){
					commandS=commandS.substring(1,commandS.length()-1);
				}
				System.out.println(commandS);
			} else if (c.equals("-I")) {
				interactive = true;
			} else if (c.equals("-m")) {
				request += " -l mem_free=" + command[++i];
			} else if (c.equals("-P")) {
				request += " -pe thread " + command[++i];
			} else if (c.equals("-q")) {
				request += " -q " + command[++i];
			} else if (c.equals("-r")) {
				jobName = command[++i];
			} else if (c.equals("-W")) {
				waitfor = Integer.parseInt(command[++i]);
			} else if (c.equals("-cp")) {
				 parentName=command[++i];
			} else if (c.equals("-S")) {
				 stdOutFile=command[++i];
			} else if (c.equals("-M")) {
				notify = true;
			} else if (c.equals("-f")) {
				parent = Integer.parseInt(command[++i]);
			}else if (!c.trim().equals("")) {
				start = i;
				break;
			}
		}
		if(parentName!=null){
			int ret =  con.createParent( userId,parentName,waitfor,parent);
			System.out.println(ret);
			System.exit(1);
		}
		if (commandS == null) {
			commandS = "";
			for (int i = start; i < command.length; i++) {
				commandS += command[i] + " ";
			}
		}
		//do some checking on the command that is being ran. Check to see if there are any ; or > or >>
		String[] commandArr = commandS.split(" ");
		for(String part:commandArr){
			if(part.equals(";")){
				System.out.println("Sorry you have put a ';' in your command string. This isn't allowed.");
				exitShowingSGE(commandS, jobName);
			}else if(part.equals(">>")){
				System.out.println("Sorry you have put a '>>' in your command string. You can not append stdout to a file for safty reasons.");
				exitShowingSGE(commandS, jobName);
			}else if(part.equals(">")){
				commandS="";
				for(int i=0;i<commandArr.length;i++){
					if(commandArr[i].equals(">")){
						stdOutFile=commandArr[i+1];
						break;
					}
					commandS+= commandArr[i]+" ";
				}
				break;
			}
		}
		
		int ret = con.runCmd(userId, commandS, request, jobName, waitfor,parent, new File("").getAbsolutePath(),stdOutFile);

		try {
			this.jobId = ret;
			if (notify) {
				con.addNotification(jobId, userId);
			}
		} catch (Exception e) {
			System.out.println("connections failed due to: " + ret);
			System.exit(0);
		}
		System.out.println(ret);
		if (!interactive) {
			System.exit(0);
		} else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					con.sendOut( this.jobId, line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println();
			System.exit(0);
		}
	}
	
	public void exitShowingSGE(String command,String jobName){
		System.out.println("exiting.. You can try to run this job using SGE_Batch by going:\n SGE_Batch -c '"+command+"' -r "+jobName);
		System.exit(1);
	}

	public static void main(String[] arrrrr) {
		if (arrrrr.length < 1) {
			printMenu();
			Iterator<String> it = commandArgs.keySet().iterator();
			String[] arg = new String[commandArgs.size() * 2];
			int size = 0;
			while (it.hasNext()) {
				String key = it.next();
				arg[size++] = "-" + key;
				if (key.equals("c")) {
					arg[size++] = "'" + commandArgs.get(key) + "'";
				} else
					arg[size++] = commandArgs.get(key);
			}
			new ETASubmit(arg);
		} else if (arrrrr[0].equals("-h") || arrrrr[0].equals("-help") || arrrrr[0].equals("--help")) {
			System.out.println("		  Usage:");
			System.out.println("		    ETASubmit -c '<command>' -m <max_memory> -P <number_processors> -r <Run_ID> -p <priority> -M  -q <queue> -s <file>");
			System.out.println("		   -c	The command to submit. (REQUIRED: Make sure to use '')\n");
			System.out.println("		   -m	The maximum memory needed for this job (1G, 4G, 32G etc.).\n");
			System.out.println("		   -P	The number of processors needed for this job if you have a threaded application (default 1).\n");
			System.out.println("		   -r	The ETA job name.\n");
			System.out.println("		   -q	The QUEUE to use. (default to use any node you have access to)\n");
			System.out.println("		   -p	The priority of job submitted. (range -10 to 10, default 0)\n");
			System.out.println("		   -M	Send notification on job completion.\n");
			System.out.println("		   -W	wait for job # to finish before running this job.\n");
			System.out.println("		   -f	assign a parent job for this job.\n");
			System.out.println("		   -S	save standard out to this file.\n");
			System.out.println("		   -h	Print Help Page.\n");
		} else
			new ETASubmit(arrrrr);
	}

	private static HashMap<String, String> commandArgs = new HashMap<String, String>();

	private static void printMenu() {
		System.out.println();
		System.out.println("\t\t\033[31m\033[1m ETASubmit Menu \033[0m");
		System.out.println("\t(items with a \"*\" are required)");
		System.out.println("");
		System.out.println("\t*Command to Run:\t\t\033[31m\033[1m c \033[0m");
		System.out.println("\tMax Memory (512M, 4G):\t\t\033[31m\033[1m m \033[0m");
		System.out.println("\tNum. Processors (1,2,4 etc):\t\033[31m\033[1m P \033[0m");
		System.out.println("\tQUEUE to Use:\t\t\t\033[31m\033[1m q \033[0m");
		System.out.println("\tJob Name:\t\t\t\033[31m\033[1m r \033[0m");
		System.out.println("\tJob Priority:\t\t\t\033[31m\033[1m p \033[0m");
		System.out.println("\tNotify on completion:\t\t\033[31m\033[1m M \033[0m");
		System.out.println("\tWait for:\t\t\t\033[31m\033[1m W \033[0m");
		System.out.println("\tShow Setup Info:\t\t\033[31m\033[1m h \033[0m");
		System.out.println("\tSave STDOut:\t\t\t\033[31m\033[1m S \033[0m");
		System.out.println("\tSubmit Job:\t\t\t\033[31m\033[1m s \033[0m");
		System.out.println("\tExit:\t\t\t\t\033[31m\033[1m e \033[0m");
		System.out.println();
		System.out.print("   Please enter a letter corresponding to an option:");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String line = reader.readLine();
			String value = "";
			boolean getValue = true;
			if (line == null || line.equals("")) {
				System.out.println("Invalid option!");
				getValue = false;
			} else if (line.equals("c")) {
				System.out.print("   Please enter a \"Command\" to run:  ");
			} else if (line.equals("m")) {
				System.out.print("   Please enter the \"MAX Memory\" needed:  ");
			} else if (line.equals("P")) {
				System.out.print("   Please enter the \"Number of Processors\" (1,2,4 etc):  ");
			}  else if (line.equals("S")) {
				System.out.print("   Please enter the file to save stdout to:  ");
			} else if (line.equals("q")) {
				System.out.print("   Please enter the \"QUEUE\" to use:  ");
			} else if (line.equals("W")) {
				System.out.print("   Please enter the \"Job number\" to wait for:  ");
			} else if (line.equals("r")) {
				System.out.print("   Please enter a \"Run ID\":  ");
			} else if (line.equals("p")) {
				System.out.print("   Please enter the \"Job Priority\" (Value between -10 and 10):  ");
			} else if (line.equals("M")) {
				getValue = false;
				if (commandArgs.containsKey("M")) {
					commandArgs.remove("M");
					System.out.print("	You will no longer be notified when the job completes.");
				} else {
					commandArgs.put("M", "");
					System.out.print("	You will be notified when the job completes.");
				}
			} else if (line.equals("h")) {
				System.out.println("   Current Setup");
				System.out.println();
				if (commandArgs.containsKey("c"))
					System.out.println("	Command:	" + commandArgs.get("c"));
				if (commandArgs.containsKey("m"))
					System.out.println("	Max Memory:	" + commandArgs.get("m"));
				if (commandArgs.containsKey("P"))
					System.out.println("	Processors:	" + commandArgs.get("P"));
				if (commandArgs.containsKey("S"))
					System.out.println("	STDout file:	" + commandArgs.get("S"));
				if (commandArgs.containsKey("q"))
					System.out.println("	Queue:	" + commandArgs.get("q"));
				if (commandArgs.containsKey("r"))
					System.out.println("	Run ID:	" + commandArgs.get("r"));
				if (commandArgs.containsKey("W"))
					System.out.println("	Wait for:	" + commandArgs.get("W"));
				if (commandArgs.containsKey("M"))
					System.out.println("	You will be notified on completion");
				System.out.print("   Please press \"Enter\" when you are done:  ");

			} else if (line.equals("s")) {
				return;
			} else if (line.equals("e")) {
				System.exit(0);
			} else {
				getValue = false;
				System.out.println("Invalid option:" + line + "!");
			}
			if (getValue)
				value = reader.readLine();
			if (value != null && !value.equals("")) {
				commandArgs.put(line, value);
			}
			printMenu();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public Result runMethod(Method method, RMIConnection con) {
		return null;
	}

	@Override
	public void eventOccured(ETAEvent event, int user) {
		
	}
}
