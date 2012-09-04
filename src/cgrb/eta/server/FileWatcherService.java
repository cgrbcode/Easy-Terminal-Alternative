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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Iterator;

public class FileWatcherService implements Runnable {

	private static FileWatcherService instance;
	private WatchService watcher;
	private HashMap<WatchKey, File> keys = new HashMap<>();
	private HashMap<WatchKey, FolderChangedListener> listeners = new HashMap<>();

	public static FileWatcherService getInstance() {
		return instance == null ? instance = new FileWatcherService() : instance;
	}

	public FileWatcherService() {
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(this).start();
	}

	public void watchFolder(File folder, FolderChangedListener listener) {
		try {
			System.out.println("adding " + folder.getName());
			WatchKey key = folder.toPath().register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
			keys.put(key, folder);
			listeners.put(key, listener);
			File[] files = folder.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					watchFolder(file, listener);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		for (;;) {
			// wait for key to be signaled
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException x) {
				return;
			}
			FolderChangedListener listener = listeners.get(key);
			File parent = keys.get(key);

			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.OVERFLOW) {
					continue;
				}
				// The filename is the
				// context of the event.
				@SuppressWarnings("unchecked")
				WatchEvent<Path> ev = (WatchEvent<Path>) event;
				Path filename = ev.context();
				File file = new File(parent.getAbsolutePath()+"/"+filename.getFileName());

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					System.out.println(file.getAbsolutePath() + " is new");
					listener.fileCreated(file, parent);
					if(file.isDirectory()){
						watchFolder(file, listener);
					}
				} else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
					System.out.println(file.getAbsolutePath() + " is modified");
					listener.fileModified(file, parent);
				} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
					System.out.println(file.getAbsolutePath() + " is deleted");
					listener.fileRemoved(file, parent);
					if(file.isDirectory()){
						Iterator<WatchKey> it = keys.keySet().iterator();
						while(it.hasNext()){
							WatchKey keye = it.next();
							File folder = keys.get(keye);
							if(folder.toPath().startsWith(file.toPath())){
								keys.remove(keye);
								listeners.remove(keye);
							}
						}
					}
				}
			}
			boolean valid = key.reset();
			if (!valid) {
				break;
			}
		}
	}

}
