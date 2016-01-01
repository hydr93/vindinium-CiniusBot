package com.hydr93.vindiniumclient.ciniusbot;

import com.hydr93.vindiniumclient.bot.BotMove;
import com.hydr93.vindiniumclient.bot.BotUtils;
import com.hydr93.vindiniumclient.bot.advanced.AdvancedGameState;
import com.hydr93.vindiniumclient.bot.advanced.Mine;
import com.hydr93.vindiniumclient.bot.advanced.Pub;
import com.hydr93.vindiniumclient.bot.advanced.Vertex;
import com.hydr93.vindiniumclient.dto.GameState;

import java.util.*;

/**
 * Created by hydr93 on 28/12/15.
 */
public class CiniusUtility {

    public static final int INT_MAX = 99999999;
    public static final int maximumHeuristic = 1000;

    public static int distanceManhattan(GameState.Position currentPosition, GameState.Position desiredPosition){
        int dx = Math.abs(currentPosition.getX() - desiredPosition.getX());
        int dy = Math.abs(currentPosition.getY() - desiredPosition.getY());
        return dx+dy;
    }

    public static boolean isPositionNearPub(AdvancedGameState gameState, GameState.Position pos){
        Vertex vert = gameState.getBoardGraph().get(pos);
        for (Vertex vertex : vert.getAdjacentVertices()){
            if ( gameState.getPubs().containsKey(vertex.getPosition())){
                return true;
            }
        }
        return false;
    }

}
