package com.hydr93.vindiniumclient;

import com.hydr93.vindiniumclient.bot.advanced.AdvancedBot;
import com.hydr93.vindiniumclient.bot.advanced.AdvancedBotRunner;
import com.hydr93.vindiniumclient.dto.ApiKey;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * CLI program for launching a bot
 */
public class Main {
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final Logger gameStateLogger = LogManager.getLogger("gameStateLogger");

    public static void main(String args[]) throws Exception {

        final String key = args[0];
        final String arena = args[1];
        String mapType = "";
        if (args.length > 2){
            mapType = args[2];
        }
        final String botClass = "com.hydr93.vindiniumclient.ciniusbot.CiniusBot";

        final GenericUrl gameUrl;

        if ("TRAINING".equals(arena))
            gameUrl = VindiniumUrl.getTrainingUrl();
        else if ("ARENA".equals(arena))
            gameUrl = VindiniumUrl.getCompetitionUrl();
        else
            gameUrl = new VindiniumUrl(arena);

        Map<String, String> contentMap = new HashMap<String, String>();
        contentMap.put("key", key );
        if (gameUrl.toString().endsWith("training") && args.length > 2){
            contentMap.put("map",mapType );
        }

        runAdvancedBot(contentMap, gameUrl, botClass);

    }

    private static void runAdvancedBot(Map<String, String> contentMap, GenericUrl gameUrl, String botClass) throws Exception {
        Class<?> clazz = Class.forName(botClass);
        Class<? extends AdvancedBot> botClazz = clazz.asSubclass(AdvancedBot.class);
        AdvancedBot bot = botClazz.newInstance();
        AdvancedBotRunner runner = new AdvancedBotRunner(contentMap, gameUrl, bot);
        runner.call();
    }

    /**
     * Represents the endpoint URL
     */
    public static class VindiniumUrl extends GenericUrl {
        private final static String TRAINING_URL = "http://vindinium.org/api/training";
        private final static String ARENA_URL = "http://vindinium.org/api/arena";

        public VindiniumUrl(String encodedUrl) {
            super(encodedUrl);
        }

        public static VindiniumUrl getCompetitionUrl() {
            return new VindiniumUrl(ARENA_URL);
        }

        public static VindiniumUrl getTrainingUrl() {
            return new VindiniumUrl(TRAINING_URL);
        }
    }
}
