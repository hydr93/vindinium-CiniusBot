package com.hydr93.vindiniumclient.bot.advanced;

import com.hydr93.vindiniumclient.dto.GameState;

import java.util.List;

/**
 * Represents some traversable tile on the board
 */
public class Vertex {
    private final GameState.Position position;
    private final List<Vertex> adjacentVertices;

    public Vertex(GameState.Position position, List<Vertex> adjacentVertices) {
        this.position = position;
        this.adjacentVertices = adjacentVertices;
    }

    public GameState.Position getPosition() {
        return position;
    }

    public List<Vertex> getAdjacentVertices() {
        return adjacentVertices;
    }
}
