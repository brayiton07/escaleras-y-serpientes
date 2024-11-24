package com.example.escaleras_y_serpientes.models

class Board {
    private val snakesAndLadders = mapOf(
        8 to 39, 12 to 38, 32 to 47, 44 to 61, // Escaleras
        31 to 3, 51 to 5, 60 to 43, 41 to 22  // Serpientes
    )


    fun getNewPosition(position: Int): Int {
        // Devuelve la nueva posición si la casilla tiene serpiente o escalera, si no devuelve la misma posición
        return snakesAndLadders[position] ?: position
    }
}
