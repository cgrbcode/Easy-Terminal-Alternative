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
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cgrb.eta.server.settings.Settings;

import com.techventus.server.voice.Voice;

public class Notifier {
	private static Settings settings = Settings.getInstance();

	public static void sendEmail(String fromEmail, String toEmail, String subject, String body) {
		String host = settings.getSetting("smtpServer").getStringValue();
		String from = settings.getSetting("smtpEmail").getStringValue();
		String pass = settings.getSetting("smtpPass").getStringValue();
		Properties props = System.getProperties();
		props.put("mail.smtp.host", host);		
		props.put("mail.smtp.auth", "false");
		props.put("mail.smtp.port", settings.getSetting("smtpPort").getStringValue());
		System.out.println("sending email to:"+toEmail+" from:"+fromEmail);
		if (!pass.equals("")) {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.password", pass);
			props.put("mail.smtp.user", from);
			props.put("mail.smtp.starttls.enable", "true"); // added this line
		}
		System.out.println(props.toString());
		String[] to = { toEmail }; // added this line
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(fromEmail));
			InternetAddress[] toAddress = new InternetAddress[to.length];
			// To get the array of addresses
			for (int i = 0; i < to.length; i++) { // changed from a while loop
				toAddress[i] = new InternetAddress(to[i]);
			}

			for (int i = 0; i < toAddress.length; i++) {
				message.addRecipient(Message.RecipientType.TO, toAddress[i]);
			}
			message.setSubject(subject);
			message.setText(body);
			if (!pass.equals("")) {
				Transport transport = session.getTransport("smtp");
				transport.connect(host, from, pass);
				transport.sendMessage(message, message.getAllRecipients());
				transport.close();
			}else{
				Transport.send(message);
			}
			
		} catch (AddressException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void sendTextNotification(String number, int job) {
		try {
			String from = settings.getSetting("gvEmail").getStringValue();
			String pass = settings.getSetting("gvPass").getStringValue();
			Voice caller = new Voice(from, pass);
			caller.login();
			String to = number;
			try {
				caller.sendSMS(to, "ETA Job#" + job + " finished. View at " + settings.getSetting("etaHost").getStringValue() + "#home,cr#" + job);
			} catch (Exception e2) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
