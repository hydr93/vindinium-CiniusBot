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
public class CiniusMover {

    private static final int heuristicMax = 99999999;
    private static List<BotMove> actions = new ArrayList<BotMove>();

    public static void findPath(AdvancedGameState gameState, GameState.Position target) {


        GameState.Position currentPosition = gameState.getMe().getPos();
        if ( currentPosition.getX() == target.getX() && currentPosition.getY() == target.getY()){
            actions.add(BotMove.STAY);
            return;
        }

        List<GameState.Position> explored = new ArrayList<GameState.Position>();
        Comparator<PriorityPosition> positionComparator = new Comparator<PriorityPosition>(){
            @Override
            public int compare(PriorityPosition p1, PriorityPosition p2) {
                return p1.priority() - p2.priority();
            }
        };
        PriorityQueue<PriorityPosition> frontier = new PriorityQueue<PriorityPosition>(400,positionComparator);
        List<BotMove> allActions = new ArrayList<BotMove>();

        // A-Star Algorithm

        boolean targetIsHero = gameState.getHeroesById().containsKey(target);

        PriorityPosition currentPriorityPosition = new PriorityPosition(currentPosition, new ArrayList<BotMove>(), CiniusUtility.distanceManhattan(currentPosition, target));
        frontier.add(currentPriorityPosition);
        explored.add(currentPosition);

        while (!frontier.isEmpty()) {
            PriorityPosition position = frontier.remove();
            if (position.getPosition().equals(target)) {
                allActions = position.getActions();
                break;
            }

            for (Vertex vertex : gameState.getBoardGraph().get(position.getPosition()).getAdjacentVertices()){
                if (!explored.contains(vertex.getPosition())) {
                    List<BotMove> actions = new ArrayList<BotMove>(position.getActions());
                    actions.add(BotUtils.directionTowards(position.getPosition(), vertex.getPosition()));
                    explored.add(vertex.getPosition());
                    int heuristic =  CiniusMover.calculateHeuristic(gameState,vertex,target);
                    frontier.add(new PriorityPosition(vertex.getPosition(), (ArrayList<BotMove>) actions, heuristic));
                }
            }
        }
        actions = allActions;
    }

    public static BotMove getAction(){
        for ( BotMove move : actions){
            System.out.println(move.toString());
        }
        return actions.remove(0);

    }

    public static int calculateHeuristic(AdvancedGameState game,  Vertex vertex, GameState.Position target){
        int heuristic = 0;

        if (game.getHeroesByPosition().containsKey(vertex.getPosition()) && game.getHeroesByPosition().get(vertex.getPosition()).getId() != game.getMe().getId())
            heuristic = CiniusMover.calculateHeuristic(game,target,game.getHeroesByPosition().get(vertex.getPosition()));
        else{
            for (Vertex vert : vertex.getAdjacentVertices()){
                if ( game.getHeroesByPosition().containsKey(vertex.getPosition()) && game.getHeroesByPosition().get(vertex.getPosition()).getId() == game.getMe().getId()){
                    heuristic = CiniusMover.calculateHeuristic(game,target,game.getHeroesByPosition().get(vert.getPosition()))/2;
                }
                for (Vertex ver : vert.getAdjacentVertices()){
                    if ( game.getHeroesByPosition().containsKey(vertex.getPosition()) && game.getHeroesByPosition().get(vertex.getPosition()).getId() == game.getMe().getId() && ver != vertex){
                        heuristic = CiniusMover.calculateHeuristic(game,target,game.getHeroesByPosition().get(ver.getPosition()))/4;
                    }
                }
            }
        }


        heuristic += CiniusUtility.distanceManhattan(vertex.getPosition(), target);
        return heuristic;
    }

    public static int calculateHeuristic(AdvancedGameState game, GameState.Position target, GameState.Hero enemy){
        int heuristic = 50;
        //Hero
        if ( game.getMe().getLife() > enemy.getLife() ) {
            heuristic = 100-(game.getMe().getLife()-enemy.getLife());
        }
        heuristic -= enemy.getMineCount();

        if ( game.getMe().getGold() + enemy.getGold() > 0){
            heuristic +=  5*(game.getMe().getGold() - enemy.getGold()) / (game.getMe().getGold()+enemy.getGold());
        }

        if ( enemy.getLife() > 20 && CiniusUtility.isPositionNearPub(game,enemy.getPos())){
            heuristic = 100;
        }

        return (heuristic/10)-5;
    }

}
