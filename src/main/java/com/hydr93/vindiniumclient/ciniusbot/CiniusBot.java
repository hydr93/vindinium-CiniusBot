package com.hydr93.vindiniumclient.ciniusbot;

import com.hydr93.vindiniumclient.bot.BotMove;
import com.hydr93.vindiniumclient.bot.BotUtils;
import com.hydr93.vindiniumclient.bot.advanced.*;
import com.hydr93.vindiniumclient.dto.GameState;

import java.util.*;

/**
 * Created by hydr93 on 25/12/15.
 */

public class CiniusBot implements AdvancedBot {




    @Override
    public BotMove move(AdvancedGameState gameState) {

        GameState.Position target = CiniusUtility.findTarget(gameState);
        BotMove action = CiniusUtility.findPath(gameState, target);

        return action;
    }

    @Override
    public void setup() {
        // No-op
    }

    @Override
    public void shutdown() {
        // No-op
    }
}
