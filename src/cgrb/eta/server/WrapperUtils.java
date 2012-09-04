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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Vector;

import cgrb.eta.shared.wrapper.Input;
import cgrb.eta.shared.wrapper.Wrapper;


public class WrapperUtils {
	private Wrapper wrapper;
	
	//Constructor
	public WrapperUtils(Wrapper theWrapper) {
		wrapper = theWrapper;
	}
	
	//Iterates through the wrappers inputs, and places their values in an array for later use
	private ArrayList<String[]> getInputs() {
		ArrayList<String[]> inputList = new ArrayList<String[]>();
		System.out.println(wrapper.getName());
		Vector<Input> inputArr = wrapper.getInputs();

		for (Input in : inputArr) {
			String[] inputEntry = { in.getName(), in.getDescription(), in.getFlag(), new Boolean(in.isRequired()).toString(), in.getType(), in.getDefaultValue() };
			inputList.add(inputEntry);
		}
		return inputList;
	}

	/**
	 * Creates and writes the template to the new Perl file
	 * @param outputFile the path for generated perl template
	 * @throws IOException
	 */
	public void createPerlTemplate(File outputFile) throws IOException {
		if (!outputFile.exists()) {
			outputFile.createNewFile();
		} else {
			System.out.println("File already exists!\n");
			return;
		}

		FileWriter fstream = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(fstream);
		
		//print header material
		out.write("#!/usr/bin/perl\n" + "#########################################################\n" + "##################Autocreated with ETA###################\n" + "#########################################################\n\n" + "use strict;\nuse warnings;\nuse Getopt::Std;\n"
				+ "\n#########################################################\n" + "# Start Variable declarations                        ETA#\n" + "#########################################################\nuse vars qw/ %opt /;\n");

		//print getopts
		out.write("my $opt_string = '");
		
		ArrayList<String[]> inputList = getInputs();
		Iterator<String[]> cursor = inputList.iterator();
		
		while (cursor.hasNext()) {
			String[] tempArray = cursor.next();

			String cleanFlag = tempArray[2];
			cleanFlag = cleanFlag.replace("-", "");
			
			if(tempArray[4].equals("Flag")){	
				out.write(cleanFlag);
			}
			else out.write(cleanFlag+":");

		}
		
		out.write("';\ngetopts(\"$opt_string\", \\%opt);\n\n");

		
		//print variable declarations
		out.write("my (");

		Iterator<String[]> opt = inputList.iterator();
		while (opt.hasNext()) {
			String[] tempArray = opt.next();

			String cleanName = tempArray[0];
			cleanName = cleanName.replace(" ", "_");
			cleanName = cleanName.toLowerCase();
			
			if(opt.hasNext()){
				out.write("$"+cleanName+",");
			}
			else out.write("$"+cleanName);
		}
		
		out.write(");");

		out.write("\n\n&var_check();\n");
		
		//print headers,footers
		out.write("#########################################################\n# End Variable declarations                          ETA#\n#########################################################\n");
		out.write("#########################################################\n" + "# Start Main body of Program                         ETA#\n" + "#########################################################\n\n\n\n\n");
		out.write("#########################################################\n# End Main body of Program                           ETA#\n#########################################################\n");
		out.write("#########################################################\n# Start Variable Check Subroutine                    ETA#\n#########################################################\n");
		
		//Print var_check subroutine. Checks for required arguments, and sets their coresponding variables with values.
		//Also gives default values if no value is given.
		out.write("sub var_check()\n{\n");
		out.write("\tif(");

		String tempIfString = "";

		ListIterator<String[]> cursor2 = inputList.listIterator();
		while (cursor2.hasNext()) {
			String[] tempArray = cursor2.next();

			String cleanFlag = tempArray[2];
			cleanFlag = cleanFlag.replace("-", "");

			if (tempArray[3].equals("true")) {
				tempIfString = tempIfString + ("!$opt{" + cleanFlag + "}||");
			}
		}
		
		int temp = tempIfString.length();
		tempIfString = tempIfString.substring(0, temp - 2);
		out.write(tempIfString);

		out.write(")\n\t{\n" + "\t\t&var_error();\n\t}\n\n");
		
		
		ListIterator<String[]> cursor5 = inputList.listIterator();
		while (cursor5.hasNext()) {
			String[] tempArray = cursor5.next();

			String cleanName = tempArray[0];
			cleanName = cleanName.replace(" ", "_");
			cleanName = cleanName.toLowerCase();
			
			String cleanFlag = tempArray[2];
			cleanFlag = cleanFlag.replace("-", "");

			if (tempArray[5] != null && !tempArray[5].equals("")) {
				if(tempArray[5].equals("false"))
					out.write("\tif(!$opt{"+cleanFlag+"})\n\t{\n\t\t$"+cleanName+" = 0;\n\t}\n");
				else
					out.write("\tif(!$opt{"+cleanFlag+"})\n\t{\n\t\t$"+cleanName+" = "+tempArray[5]+";\n\t}\n");
				out.write("\telse\n\t{\n\t\t$"+cleanName+" = $opt{"+cleanFlag+"};\n\t}\n");
			}
			
			else out.write("\t$"+cleanName+" = $opt{"+cleanFlag+"};\n");
		}
		
		out.write("}\n");
		out.write("#########################################################\n" + "# End Variable Check Subroutine                      ETA#\n" + "#########################################################\n");
		out.write("#########################################################\n" + "# Start Variable Error Subroutine                    ETA#\n" + "#########################################################\n");

		//print var_error subroutine
		out.write("sub var_error()\n{\n");
		out.write("\tprint(\"\\n\\tName: " + wrapper.getName() + "\\n\");\n\tprint(\"" + "\\tDescription: \\n\\t" + wrapper.getDescription() + "\\n\\n\");\n");
		out.write("\tprint(\"\\tUsage:\\n\\t" + wrapper.getProgram() + ".pl \");\n");

		Iterator<String[]> cursor3 = inputList.iterator();
		while (cursor3.hasNext()) {
			String[] tempArray = cursor3.next();
			out.write("\tprint(\"" + tempArray[2] + " <" + tempArray[0] + "> \");\n");
		}

		out.write("\tprint(\"\\n\");\n\n");

		Iterator<String[]> cursor4 = inputList.iterator();
		while (cursor4.hasNext()) {
			String[] tempArray = cursor4.next();
			out.write("\tprint(\"\\t" + tempArray[2] + "  " + tempArray[1]);
			if (tempArray[3].equals("true")) {
				out.write(": Required\\n\");\n");
			} else
				out.write(": Not Required\\n\");\n");
		}

		out.write("\tprint(\"\\n\");\n\texit 1;\n}\n");
		out.write("#########################################################\n" + "# End Variable Error Subroutine                      ETA#\n" + "#########################################################\n");

		out.close();

	}
}
