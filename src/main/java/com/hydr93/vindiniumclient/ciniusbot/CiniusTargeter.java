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
 * Created by hydr93 on 01/01/16.
 */
public class CiniusTargeter {

    private static AdvancedGameState game;

    public static GameState.Position findTarget(AdvancedGameState gameState){
        game = gameState;

        GameState.Hero me = gameState.getMe();
        GameState.Position destination = new GameState.Position();

        boolean considerHeroes = true;
        boolean considerMines = true;
        boolean considerPubs = true;

        // Decision Tree for Extreme Cases

        // Absolute Win looks if the current situation will result with a win
        boolean absoluteWin = true;
        for ( GameState.Hero hero : gameState.getHeroesById().values()){
            if ( hero.getId() == me.getId() ) continue;
            int win = (me.getGold() - hero.getGold()) + (((gameState.getTotalTurns()-gameState.getCurrentTurn())/4)*(me.getMineCount() - hero.getMineCount()));
            if ( win <= 0 ){
                absoluteWin = false;
            }
        }

        if (absoluteWin){
            considerHeroes = false;
            considerMines = false;
            considerPubs = false;
            for ( GameState.Hero hero : gameState.getHeroesById().values()){
                if ( CiniusUtility.distanceManhattan(hero.getPos(), me.getPos()) <= 2 && me.getLife() <= 20){
                    considerPubs = true;
                }
            }
            considerPubs = !CiniusUtility.isPositionNearPub(gameState,me.getPos());
            destination = getDestination(gameState,considerHeroes,considerMines,considerPubs);
        }else{
            // Greedy Best First Search Algorithm is used.
            destination = getDestination(gameState,considerHeroes,considerMines,considerPubs);
        }



        System.out.println("Heroes: "+considerHeroes+"\tMines: "+considerMines+"\tPubs:"+considerPubs);
        System.out.println("Current Position " + ": " + me.getPos().getX() + "," + me.getPos().getY());
        System.out.println("Target "+": "+destination.getX()+","+destination.getY());
        return destination;

    }

    public static GameState.Position getDestination(AdvancedGameState gameState, boolean considerHeroes, boolean considerMines, boolean considerPubs){

        GameState.Hero me = gameState.getMe();
        GameState.Position destination = me.getPos();
        int minimumHeuristic = CiniusUtility.INT_MAX;

        if (considerHeroes) {
            for (GameState.Hero hero : gameState.getHeroesById().values()) {
                if (hero.getId() == me.getId()) continue;
                int heuristic = CiniusTargeter.calculateHeuristic(me, hero);
                if (heuristic < minimumHeuristic) {
                    minimumHeuristic = heuristic;
                    destination = hero.getPos();
                }
            }
        }

        if (considerMines) {
            for (Mine mine : gameState.getMines().values()) {
                if (mine.getOwner() != null && mine.getOwner().getId() == me.getId()) continue;
                int heuristic = CiniusTargeter.calculateHeuristic(me, mine);
                if (heuristic < minimumHeuristic) {
                    minimumHeuristic = heuristic;
                    destination = mine.getPosition();
                }
            }
        }

        if (considerPubs) {
            for (Pub pub : gameState.getPubs().values()) {
                int heuristic = CiniusTargeter.calculateHeuristic(me, pub);
                if (heuristic < minimumHeuristic) {
                    minimumHeuristic = heuristic;
                    destination = pub.getPosition();
                }
            }
        }

        return destination;
    }

    public static int calculateHeuristic(GameState.Hero me, GameState.Hero enemy){
        int heuristic = 50;
        //Hero
        if ( me.getLife() > enemy.getLife() ) {
            heuristic = (100-(me.getLife()-enemy.getLife()))/2;
        }
        heuristic -= enemy.getMineCount();
        if ( me.getGold() + enemy.getGold() > 0){
            heuristic +=  5*(me.getGold() - enemy.getGold()) / (me.getGold()+enemy.getGold());
        }

        boolean enemyNearPub = false;

        if (CiniusUtility.isPositionNearPub(game,enemy.getPos())){
            heuristic = CiniusUtility.maximumHeuristic;
        }

        heuristic += CiniusUtility.distanceManhattan(me.getPos(), enemy.getPos());
        return heuristic;
    }

    public static int calculateHeuristic(GameState.Hero me, Pub pub){
        int heuristic = 0;
        //Pub
        if ( me.getLife() > 80 )
            heuristic = CiniusUtility.maximumHeuristic;
        else if ( me.getLife() > 20 ){
            heuristic = me.getLife();
        }

        heuristic += CiniusUtility.distanceManhattan(me.getPos(), pub.getPosition());
        return heuristic;
    }

    public static int calculateHeuristic(GameState.Hero me, Mine mine){
        int heuristic =  0;
        //Mine
        if ( me.getLife() > 20 )
            heuristic = (100-me.getLife());
        else
            heuristic = CiniusUtility.maximumHeuristic;

        heuristic += CiniusUtility.distanceManhattan(me.getPos(), mine.getPosition());
        return heuristic;
    }

}
