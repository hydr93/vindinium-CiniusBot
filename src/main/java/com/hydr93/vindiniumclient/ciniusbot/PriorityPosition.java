package com.hydr93.vindiniumclient.ciniusbot;

import com.hydr93.vindiniumclient.bot.BotMove;
import com.hydr93.vindiniumclient.dto.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by hydr93 on 26/12/15.
 */
public class PriorityPosition{
    private int heuristic;
    private int costOfActions;
    private List<BotMove> actions;
    private GameState.Position position;

    public PriorityPosition(GameState.Position pos,ArrayList<BotMove> acts, int heuristic){
        this.position = pos;
        this.actions = acts;
        this.costOfActions = this.actions.size();
        this.heuristic = heuristic;
    }

    public int priority(){
        return this.heuristic + this.costOfActions;
    }
    public GameState.Position getPosition() { return this.position; }
    public List<BotMove> getActions() {return this.actions;}

}
