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
package cgrb.eta.server.rmi;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.spec.SecretKeySpec;


public class AESRMIConnection extends RMIConnection {
	private String key;
	private Cipher deCiper;
	private Cipher enCiper;
	private int globalId;

	public AESRMIConnection(String key, int globalId, Socket clientSocket, RemoteService runner, boolean asClient) {
		super(clientSocket, runner, asClient);
		this.key = key;
		this.globalId = globalId;
		try {
			setupCiphers();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	public AESRMIConnection(String key, Socket clientSocket, RemoteService runner, boolean asClient, ConnectionListener list) {
		super(clientSocket, runner, asClient, list);
		this.key = key;
		try {
			setupCiphers();
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}

	public void setupCiphers() throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, NoSuchProviderException {
		SecretKeySpec secretKey = new SecretKeySpec(key.substring(0,16).getBytes(), "AES");
		deCiper = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
		deCiper.init(Cipher.DECRYPT_MODE, secretKey);

		enCiper = Cipher.getInstance("AES/ECB/PKCS5Padding", "SunJCE");
		enCiper.init(Cipher.ENCRYPT_MODE, secretKey);
	}

	public int getGlobalId() {
		return globalId;
	}

	public Object getObject() {
		Object ret = null;
		try {
			synchronized (reciveLock) {
				ret = ois.readUnshared();
			}
			if (ret instanceof SealedObject) {
				try {
					ret = ((SealedObject) ret).getObject(deCiper);
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				}
			} else {
				ret = null;
			}
		} catch (java.io.EOFException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String getKey() {
		return key;
	}

	@Override
	public void writeObject(Object obj) throws IOException {
		try {
			super.writeObject(new SealedObject((Serializable) obj, enCiper));
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		}
	}
}
