package com.hydr93.vindiniumclient.bot;

import com.hydr93.vindiniumclient.bot.advanced.AdvancedBot;
import com.hydr93.vindiniumclient.bot.advanced.AdvancedGameState;
import com.hydr93.vindiniumclient.dto.GameState;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BotUtils {

    /**
     * Given a current position and some position directly adjacent to the current position, this method will calculate
     * the direction of the move.
     *
     * This method is NOT safe to use if the target position is more than 1 move away from the current position.
     * @param currentLocation
     * @param target some position adjacent to the current position
     * @return
     */
    public static BotMove directionTowards(GameState.Position currentLocation, GameState.Position target) {
        if (target.getX() < currentLocation.getX()) {
            return BotMove.NORTH;
        } else if (target.getX() > currentLocation.getX()) {
            return BotMove.SOUTH;
        } else if (target.getY() < currentLocation.getY()) {
            return BotMove.WEST;
        } else if (target.getY() > currentLocation.getY()) {
            return BotMove.EAST;
        } else {
            return BotMove.STAY;
        }
    }

}
