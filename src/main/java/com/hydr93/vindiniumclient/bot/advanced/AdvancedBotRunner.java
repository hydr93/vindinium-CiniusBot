package com.hydr93.vindiniumclient.bot.advanced;

import com.hydr93.vindiniumclient.Main;
import com.hydr93.vindiniumclient.bot.BotMove;
import com.hydr93.vindiniumclient.dto.ApiKey;
import com.hydr93.vindiniumclient.dto.GameState;
import com.hydr93.vindiniumclient.dto.Move;
import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AdvancedBotRunner implements Callable<GameState> {
    private static final HttpTransport HTTP_TRANSPORT = new ApacheHttpTransport();
    private static final JsonFactory JSON_FACTORY = new GsonFactory();
    private static final HttpRequestFactory REQUEST_FACTORY =
            HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(JSON_FACTORY));
                }
            });
    private static final Logger logger = LogManager.getLogger(AdvancedBotRunner.class);


    private final ApiKey apiKey;
    private final Map<String, String> contentMap;
    private final GenericUrl gameUrl;
    private final AdvancedBot bot;

    public AdvancedBotRunner(Map<String, String> cMap, GenericUrl gameUrl, AdvancedBot bot) {
        this.apiKey = new ApiKey(cMap.get("key"));
        this.contentMap = cMap;
        this.gameUrl = gameUrl;
        this.bot = bot;
    }

    @Override
    public GameState call() throws Exception {
        HttpContent content;
        HttpRequest request;
        HttpResponse response;
        GameState gameState = null;
        AdvancedGameState advancedGameState;

        try {
            // Initial request
            logger.info("Sending initial request...");
            content = new UrlEncodedContent(contentMap);
            request = REQUEST_FACTORY.buildPostRequest(gameUrl, content);
            request.setReadTimeout(0); // Wait forever to be assigned to a game

            response = request.execute();
            gameState = response.parseAs(GameState.class);
            logger.info("Game URL: {}", gameState.getViewUrl());

            advancedGameState = new AdvancedGameState(gameState);

            // Game loop
            while (!gameState.getGame().isFinished() && !gameState.getHero().isCrashed()) {
                logger.info("Taking turn " + gameState.getGame().getTurn());
                BotMove direction;

                // If our decisioner crashes, stay put; don't time out.
                try {
                    direction = bot.move(advancedGameState);
                } catch(Throwable t) {
                    logger.error("Error while making move", t);
                    direction = BotMove.STAY;
                }
                Move move = new Move(apiKey.getKey(), direction.toString());


                HttpContent turn = new UrlEncodedContent(move);
                HttpRequest turnRequest = REQUEST_FACTORY.buildPostRequest(new GenericUrl(gameState.getPlayUrl()), turn);
                HttpResponse turnResponse = turnRequest.execute();

                gameState = turnResponse.parseAs(GameState.class);
                advancedGameState = new AdvancedGameState(advancedGameState, gameState);
            }

        } catch (Exception e) {
            logger.error("Error during game play", e);
        }
        logger.info("Gold: "+gameState.getHero().getGold());
        logger.info("Game over");
        return gameState;
    }
}
