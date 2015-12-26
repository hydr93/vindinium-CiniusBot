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
    public static GameState.Position target;
    public List<GameState.Position> explored = new ArrayList<GameState.Position>();
    public static Comparator<PriorityPosition> positionComparator = new Comparator<PriorityPosition>(){
        @Override
        public int compare(PriorityPosition p1, PriorityPosition p2) {
            return p1.priority() - p2.priority();
        }
    };
    public PriorityQueue<PriorityPosition> frontier = new PriorityQueue<PriorityPosition>(400,positionComparator);
    public List<BotMove> allActions = new ArrayList<BotMove>();
    public boolean newFrontier = false;


    public void findTarget(AdvancedGameState gameState){
        GameState.Position destination;
        GameState.Position currentPosition = gameState.getMe().getPos();
        int life = gameState.getMe().getLife();

        if ( life > 20 ){
            // Find Nearest Mine
            List<Mine> mines = new ArrayList<Mine>(gameState.getMines().values());
            Mine nearestMine = mines.get(0);
            int minimumDistance = 99999999;
            for ( int i = 0 ; i < gameState.getMines().size() ; i++){
                Mine mine = mines.get(i);

                if ( mine.getOwner() == null || mine.getOwner().getId() != gameState.getMe().getId() ) {
                    int distance = distanceManhattan(currentPosition, mine.getPosition());
                    if (distance < minimumDistance) {
                        nearestMine = mine;
                        minimumDistance = distance;
                    }
                }
            }
            destination = nearestMine.getPosition();
        }else{
            // Find Nearest Pub
            List<Pub> pubs = new ArrayList<Pub>(gameState.getPubs().values());
            Pub nearestPub = pubs.get(0);
            int minimumDistance = 99999999;
            for ( int i = 0 ; i < gameState.getPubs().size() ; i++){
                Pub pub = pubs.get(i);

                int distance = distanceManhattan(currentPosition, pub.getPosition());
                if (distance < minimumDistance) {
                    nearestPub = pub;
                    minimumDistance = distance;

                }
            }
            destination = nearestPub.getPosition();
        }




        if (!destination.equals(target)){
            System.out.println("Target "+": "+destination.getX()+","+destination.getY());
            explored.clear();
            frontier.clear();
            allActions.clear();
            target = destination;
            newFrontier = true;
        }
    }

    @Override
    public BotMove move(AdvancedGameState gameState) {

        GameState.Position currentPosition = gameState.getMe().getPos();
        System.out.println("Current Position "+": "+currentPosition.getX()+","+currentPosition.getY());

        findTarget(gameState);
        if ( newFrontier == true ){
            newFrontier = false;
        }else{

            return allActions.remove(0);
        }


        // A-Star Algorithm
        PriorityPosition currentPriorityPosition = new PriorityPosition(currentPosition,new ArrayList<BotMove>(),distanceManhattan(currentPosition,target));
        frontier.add(currentPriorityPosition);
        explored.add(currentPosition);

        while ( !frontier.isEmpty()){
            PriorityPosition position = frontier.remove();
//            System.out.println("Priority Position " + ": " + position.getPosition().getX() + "," + position.getPosition().getY());
            if ( position.getPosition().equals(target)){
                this.allActions = position.getActions();
                break;
            }

            for (Iterator<Vertex> vert = gameState.getBoardGraph().get(position.getPosition()).getAdjacentVertices().iterator(); vert.hasNext();){
                Vertex vertex = vert.next();
                if ( !explored.contains(vertex.getPosition())){
                    List<BotMove> actions = new ArrayList<BotMove>(position.getActions());
                    actions.add(BotUtils.directionTowards(position.getPosition(),vertex.getPosition()));
                    explored.add(vertex.getPosition());
                    frontier.add(new PriorityPosition(vertex.getPosition(),(ArrayList<BotMove>)actions,distanceManhattan(vertex.getPosition(),target)));
                }
            }
        }

        for ( Iterator<BotMove> mov = allActions.iterator(); mov.hasNext();){
            BotMove move = mov.next();
            System.out.println(move.toString());
        }

        return allActions.remove(0);
    }

    public static int distanceManhattan(GameState.Position currentPosition, GameState.Position desiredPosition){
        int dx = Math.abs(currentPosition.getX() - desiredPosition.getX());
        int dy = Math.abs(currentPosition.getY() - desiredPosition.getY());
        return dx+dy;
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
