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
        final String botClass = args[2];

        final GenericUrl gameUrl;

        if ("TRAINING".equals(arena))
            gameUrl = VindiniumUrl.getTrainingUrl();
        else if ("COMPETITION".equals(arena))
            gameUrl = VindiniumUrl.getCompetitionUrl();
        else
            gameUrl = new VindiniumUrl(arena);

        for ( int i = 0; i < 5; i++){
            runAdvancedBot(key, gameUrl, botClass);
        }
    }

    private static void runAdvancedBot(String key, GenericUrl gameUrl, String botClass) throws Exception {
        Class<?> clazz = Class.forName(botClass);
        Class<? extends AdvancedBot> botClazz = clazz.asSubclass(AdvancedBot.class);
        AdvancedBot bot = botClazz.newInstance();
        ApiKey apiKey = new ApiKey(key);
        AdvancedBotRunner runner = new AdvancedBotRunner(apiKey, gameUrl, bot);
        runner.call();
    }

    /**
     * Represents the endpoint URL
     */
    public static class VindiniumUrl extends GenericUrl {
        private final static String TRAINING_URL = "http://vindinium.org/api/training";
        private final static String COMPETITION_URL = "http://vindinium.org/api/arena";

        public VindiniumUrl(String encodedUrl) {
            super(encodedUrl);
        }

        public static VindiniumUrl getCompetitionUrl() {
            return new VindiniumUrl(COMPETITION_URL);
        }

        public static VindiniumUrl getTrainingUrl() {
            return new VindiniumUrl(TRAINING_URL);
        }
    }
}
