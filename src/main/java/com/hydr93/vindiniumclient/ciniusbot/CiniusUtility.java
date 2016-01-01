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

    public static GameState.Position findTarget(AdvancedGameState gameState){

        GameState.Hero me = gameState.getMe();

        GameState.Position destination = new GameState.Position();
        int minimumHeuristic = 999999999;

        boolean bestMiner = true;
        for ( Iterator<GameState.Hero> heroIterator = gameState.getHeroesById().values().iterator(); heroIterator.hasNext();){
            GameState.Hero enemy = heroIterator.next();
            if ( enemy.getId() == gameState.getMe().getId() )continue;

            if ( gameState.getMe().getMineCount() <= enemy.getMineCount()){
                bestMiner = false;
            }
        }
        if (!bestMiner) {
            for (Iterator<GameState.Hero> heroIterator = gameState.getHeroesById().values().iterator(); heroIterator.hasNext();){
                GameState.Hero hero = heroIterator.next();
                if ( hero.getId() == me.getId() ) continue;
                int heuristic = CiniusUtility.calculateHeuristic(me, hero);
                if ( heuristic < minimumHeuristic ){
                    minimumHeuristic = heuristic;
                    destination = hero.getPos();
                }
            }

            for (Iterator<Mine> mineIterator = gameState.getMines().values().iterator(); mineIterator.hasNext();){
                Mine mine = mineIterator.next();
                if (mine.getOwner() != null && mine.getOwner().getId() == me.getId()) continue;
                int heuristic = CiniusUtility.calculateHeuristic(me, mine);
                if ( heuristic < minimumHeuristic ){
                    minimumHeuristic = heuristic;
                    destination = mine.getPosition();
                }

            }
        }


        for (Iterator<Pub> pubIterator = gameState.getPubs().values().iterator(); pubIterator.hasNext();){
            Pub pub = pubIterator.next();
            int heuristic = CiniusUtility.calculateHeuristic(me, pub);
            if ( heuristic < minimumHeuristic ){
                minimumHeuristic = heuristic;
                destination = pub.getPosition();
            }
        }

        System.out.println("Current Position " + ": " + me.getPos().getX() + "," + me.getPos().getY());
        System.out.println("Target "+": "+destination.getX()+","+destination.getY());
        return destination;

    }

    public static BotMove findPath(AdvancedGameState gameState, GameState.Position target) {
        List<GameState.Position> explored = new ArrayList<GameState.Position>();
        Comparator<PriorityPosition> positionComparator = new Comparator<PriorityPosition>(){
            @Override
            public int compare(PriorityPosition p1, PriorityPosition p2) {
                return p1.priority() - p2.priority();
            }
        };
        PriorityQueue<PriorityPosition> frontier = new PriorityQueue<PriorityPosition>(400,positionComparator);
        List<BotMove> allActions = new ArrayList<BotMove>();

        GameState.Position currentPosition = gameState.getMe().getPos();
        // A-Star Algorithm

        PriorityPosition currentPriorityPosition = new PriorityPosition(currentPosition, new ArrayList<BotMove>(), CiniusUtility.distanceManhattan(currentPosition, target));
        frontier.add(currentPriorityPosition);
        explored.add(currentPosition);

        while (!frontier.isEmpty()) {
            PriorityPosition position = frontier.remove();
//            System.out.println("Priority Position " + ": " + position.getPosition().getX() + "," + position.getPosition().getY());
            if (position.getPosition().equals(target)) {
                allActions = position.getActions();
                break;
            }

            for (Iterator<Vertex> vertexIterator = gameState.getBoardGraph().get(position.getPosition()).getAdjacentVertices().iterator(); vertexIterator.hasNext(); ) {
                Vertex vertex = vertexIterator.next();
                if (!explored.contains(vertex.getPosition())) {
                    List<BotMove> actions = new ArrayList<BotMove>(position.getActions());
                    actions.add(BotUtils.directionTowards(position.getPosition(), vertex.getPosition()));
                    explored.add(vertex.getPosition());
                    int heuristic = 0;
                    if ( gameState.getMines().containsKey(vertex.getPosition())){
                        heuristic = CiniusUtility.calculateHeuristic(gameState.getMe(),gameState.getMines().get(vertex.getPosition()));
                    }else if ( gameState.getPubs().containsKey(vertex.getPosition())){
                        heuristic = CiniusUtility.calculateHeuristic(gameState.getMe(),gameState.getPubs().get(vertex.getPosition()));
                    }else if ( gameState.getHeroesByPosition().containsKey(vertex.getPosition())){
                        heuristic = CiniusUtility.calculateHeuristic(gameState.getMe(),gameState.getHeroesByPosition().get(vertex.getPosition()));
                    }else{
                        heuristic = CiniusUtility.calculateHeuristic(gameState,gameState.getMe(),vertex);
                    }
                    frontier.add(new PriorityPosition(vertex.getPosition(), (ArrayList<BotMove>) actions, heuristic));
                }
            }
        }

        for (Iterator<BotMove> botMoveIterator = allActions.iterator(); botMoveIterator.hasNext(); ) {
            BotMove move = botMoveIterator.next();
            System.out.println(move.toString());
        }

        return allActions.remove(0);

    }

    public static int calculateHeuristic(GameState.Hero me, GameState.Hero enemy){
        int heuristic = 30;
        //Hero
        if ( me.getLife() > 20) {
            heuristic = (enemy.getLife() - me.getLife()) / 10;
        }

        heuristic += distanceManhattan(me.getPos(), enemy.getPos());
        return heuristic;
    }

    public static int calculateHeuristic(GameState.Hero me, Pub pub){
        int heuristic = 0;
        //Pub
        if ( me.getLife() > 80 )
            heuristic = 10;
        else if ( me.getLife() > 20 )
            heuristic = me.getLife()/10;

        heuristic += distanceManhattan(me.getPos(), pub.getPosition());
        return heuristic;
    }

    public static int calculateHeuristic(GameState.Hero me, Mine mine){
        int heuristic =  10;
        //Mine
        if (mine.getOwner() == null || mine.getOwner().getId() != me.getId() ){
            if ( me.getLife() > 20 )
                heuristic = (100-me.getLife())/10;
        }else{
            heuristic = 0;
        }

        heuristic += distanceManhattan(me.getPos(), mine.getPosition());
        return heuristic;
    }

    public static int calculateHeuristic(AdvancedGameState game, GameState.Hero me, Vertex vertex){
        int heuristic = 0;
        if (game.getMines().containsKey(vertex.getPosition()))
            heuristic = CiniusUtility.calculateHeuristic(me,game.getMines().get(vertex.getPosition()));
        else if (game.getPubs().containsKey(vertex.getPosition()))
            heuristic = CiniusUtility.calculateHeuristic(me,game.getPubs().get(vertex.getPosition()));
        else if (game.getHeroesByPosition().containsKey(vertex.getPosition()))
            heuristic = CiniusUtility.calculateHeuristic(me,game.getHeroesByPosition().get(vertex.getPosition()));
        else
            heuristic += distanceManhattan(me.getPos(), vertex.getPosition());
        return heuristic;
    }

    public static int distanceManhattan(GameState.Position currentPosition, GameState.Position desiredPosition){
        int dx = Math.abs(currentPosition.getX() - desiredPosition.getX());
        int dy = Math.abs(currentPosition.getY() - desiredPosition.getY());
        return dx+dy;
    }

}
