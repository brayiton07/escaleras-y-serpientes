package com.example.escaleras_y_serpientes

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.escaleras_y_serpientes.models.Board
import com.example.escaleras_y_serpientes.models.Player

class GameActivity : AppCompatActivity() {
    private lateinit var gridLayout: GridLayout
    private lateinit var turnInfo: TextView
    private lateinit var rollDiceButton: Button
    private lateinit var diceInfo: TextView
    private lateinit var board: Board
    private lateinit var player1: Player
    private lateinit var player2: Player
    private var currentPlayer: Player? = null
    private var player1Icon: ImageView? = null
    private var player2Icon: ImageView? = null
    private var sixCountPlayer1 = 0
    private var sixCountPlayer2 = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        gridLayout = findViewById(R.id.board_grid)
        turnInfo = findViewById(R.id.tv_turn_info)
        rollDiceButton = findViewById(R.id.btn_roll_dice)
        diceInfo = findViewById(R.id.tv_dice_info)

        board = Board()
        player1 = Player("Jugador 1")
        player2 = Player("Jugador 2")
        currentPlayer = player1

        createBoard()

        rollDiceButton.setOnClickListener {
            // Lanzamos los dos dados
            val diceRoll1 = (1..6).random()
            val diceRoll2 = (1..6).random()
            val diceSum = diceRoll1 + diceRoll2

            diceInfo.text = "Dados: $diceRoll1 y $diceRoll2"  // Muestra los resultados de los dados

            Toast.makeText(this, "Tirada: $diceRoll1 y $diceRoll2", Toast.LENGTH_SHORT).show()

            // Regla de tres 6 consecutivos
            if (currentPlayer == player1) {
                if (diceRoll1 == 6 || diceRoll2 == 6) {
                    sixCountPlayer1++
                }
                if (sixCountPlayer1 == 3) {
                    player1.position = 1
                    sixCountPlayer1 = 0
                    turnInfo.text = "Jugador 1 tiró tres 6s, vuelve al inicio."
                }
            } else {
                if (diceRoll1 == 6 || diceRoll2 == 6) {
                    sixCountPlayer2++
                }
                if (sixCountPlayer2 == 3) {
                    player2.position = 1
                    sixCountPlayer2 = 0
                    turnInfo.text = "Jugador 2 tiró tres 6s, vuelve al inicio."
                }
            }

            // Avanzar la posición del jugador actual
            if (currentPlayer == player1) {
                player1.position += diceSum
                player1.position = board.getNewPosition(player1.position) // Actualizar posición según serpientes y escaleras
            } else {
                player2.position += diceSum
                player2.position = board.getNewPosition(player2.position) // Actualizar posición según serpientes y escaleras
            }

            // Verificar si el jugador ganó
            if (currentPlayer?.position!! >= 64) {
                currentPlayer?.position = 64
                turnInfo.text = "${currentPlayer?.name} ¡Ganó!"
                rollDiceButton.isEnabled = false
            }

            // Regla de turno extra si saca un 6
            if (diceRoll1 == 6 || diceRoll2 == 6) {
                turnInfo.text = "${currentPlayer?.name} tiene un turno extra."
            }

            // Alternar entre jugadores
            currentPlayer = if (currentPlayer == player1) player2 else player1

            // Actualizar el tablero
            updateBoard()

            // Borrar el mensaje de turno extra cuando el otro jugador tira los dados
            if (turnInfo.text.contains("turno extra")) {
                turnInfo.text = ""
            }
        }
    }

    // Crear el tablero de 8x8 con 64 casillas
    private fun createBoard() {
        for (i in 1..64) {
            val cell = TextView(this)
            cell.setPadding(10, 10, 10, 10)
            cell.text = i.toString()
            cell.setBackgroundResource(R.drawable.board_tile)
            cell.gravity = android.view.Gravity.CENTER
            gridLayout.addView(cell)
        }
    }

    // Actualizar las casillas visualmente
    private fun updateBoard() {
        val cells = gridLayout.children.toList()

        // Recorrer cada casilla y actualizar el texto o el fondo si es necesario
        cells.forEachIndexed { index, view ->
            if (view is TextView) {
                val cell = view
                if (index + 1 == player1.position) {
                    // Si es la posición de Jugador 1, mostrar su imagen
                    player1Icon?.let {
                        gridLayout.removeView(it)  // Eliminar la imagen del Jugador 1 de la posición anterior
                    }
                    player1Icon = ImageView(this)
                    player1Icon?.layoutParams = GridLayout.LayoutParams().apply {
                        width = 30 * resources.displayMetrics.density.toInt()  // 30dp
                        height = 30 * resources.displayMetrics.density.toInt()  // 30dp
                        rowSpec = GridLayout.spec(index / 8)
                        columnSpec = GridLayout.spec(index % 8)
                    }
                    player1Icon?.setImageResource(R.drawable.p1)
                    gridLayout.addView(player1Icon)
                    cell.text = ""  // Borra el número porque el jugador está allí
                } else if (index + 1 == player2.position) {
                    // Si es la posición de Jugador 2, mostrar su imagen
                    player2Icon?.let {
                        gridLayout.removeView(it)  // Eliminar la imagen del Jugador 2 de la posición anterior
                    }
                    player2Icon = ImageView(this)
                    player2Icon?.layoutParams = GridLayout.LayoutParams().apply {
                        width = 30 * resources.displayMetrics.density.toInt()  // 30dp
                        height = 30 * resources.displayMetrics.density.toInt()  // 30dp
                        rowSpec = GridLayout.spec(index / 8)
                        columnSpec = GridLayout.spec(index % 8)
                    }
                    player2Icon?.setImageResource(R.drawable.p2)
                    gridLayout.addView(player2Icon)
                    cell.text = ""  // Borra el número porque el jugador está allí
                } else {
                    cell.text = (index + 1).toString()
                    cell.setBackgroundResource(R.drawable.board_tile)
                }
            }
        }
    }
}
