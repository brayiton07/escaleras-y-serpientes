package com.example.escaleras_y_serpientes

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.example.escaleras_y_serpientes.models.Board //modelo de tablero
import com.example.escaleras_y_serpientes.models.Player //modelo del jugaador

//clase principal para la actividad del juego
class GameActivity : AppCompatActivity() {
    //declaracion de varaibles para los componenetes de la interfaz y logica del juego
    private lateinit var gridLayout: GridLayout // Contenedor del tablero
    private lateinit var turnInfo: TextView // Información sobre el turno actual
    private lateinit var rollDiceButton: Button // Botón para tirar los dados
    private lateinit var diceInfo: TextView // Texto para mostrar los valores de los dados
    private lateinit var board: Board // Objeto del tablero
    private lateinit var player1: Player// Jugador 1
    private lateinit var player2: Player// Jugador 2
    private var currentPlayer: Player? = null // Jugador actual en el turno
    private var player1Icon: ImageView? = null // Icono del jugador 1 en el tablero
    private var player2Icon: ImageView? = null // Icono del jugador 2 en el tablero
    private var sixCountPlayer1 = 0 // Contador de 6 consecutivos del jugador 1
    private var sixCountPlayer2 = 0 // Contador de 6 consecutivos del jugador 2

    // Método llamado al crear la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game) // Asignar el diseño a la actividad

         // Inicialización de los componentes de la interfaz
        gridLayout = findViewById(R.id.board_grid)
        turnInfo = findViewById(R.id.tv_turn_info)
        rollDiceButton = findViewById(R.id.btn_roll_dice)
        diceInfo = findViewById(R.id.tv_dice_info)

         // Inicialización de objetos de lógica del juego
        board = Board()  // Crear tablero
        player1 = Player("Jugador 1")  // Crear jugador 1
        player2 = Player("Jugador 2")  // Crear jugador 2
        currentPlayer = player1 // Asignar al jugador 1 como el jugador inicial

        createBoard() // Crear visualmente el tablero

        // Configurar la acción al presionar el botón de tirar dados
        rollDiceButton.setOnClickListener {
            // Lanzamos los dos dados
            val diceRoll1 = (1..6).random()  // Generar valor aleatorio para dado 1
            val diceRoll2 = (1..6).random()  // Generar valor aleatorio para dado 2
            val diceSum = diceRoll1 + diceRoll2

            diceInfo.text = "Dados: $diceRoll1 y $diceRoll2"  // Muestra los resultados de los dados


            turnInfo.text = "Es el turno de ${currentPlayer?.name}"  // Mostrar el jugador actual

            // Regla de tres 6 consecutivos
            if (currentPlayer == player1) {
                if (diceRoll1 == 6 || diceRoll2 == 6) {
                    sixCountPlayer1++ // aumenta el contadaor cada vez que saque 6
                }
                if (sixCountPlayer1 == 3) { // validamos si la cantidad de tiros son igual a 3 se devuelve
                    player1.position = 1 // Reiniciar posición del jugador a  1
                    sixCountPlayer1 = 0 // Reiniciar contador
                    turnInfo.text = "Jugador 1 tiró tres 6s, vuelve al inicio."
                }
            } else {
                if (diceRoll1 == 6 || diceRoll2 == 6) {
                    sixCountPlayer2++ // aumenta el contadaor cada vez que saque 6
                }
                if (sixCountPlayer2 == 3) {
                    player2.position = 1 // Reiniciar posición a 1
                    sixCountPlayer2 = 0 // Reiniciar contador
                    turnInfo.text = "Jugador 2 tiró tres 6s, vuelve al inicio."
                }
            }

            // Guardamos la posición anterior del jugador para saber si avanzó por una serpiente o escalera
            val previousPosition = if (currentPlayer == player1) player1.position else player2.position

            // Avanzar la posición del jugador actual
            if (currentPlayer == player1) {
                player1.position += diceSum
                player1.position = board.getNewPosition(player1.position) // Comprobar serpientes/escaleras
            } else {
                player2.position += diceSum
                player2.position = board.getNewPosition(player2.position) // Comprobar serpientes/escaleras
            }

            // Verificar si el jugador ganó
            if (currentPlayer?.position!! >= 64) { // validamos que la posicion sea la casilla 64
                currentPlayer?.position = 64
                turnInfo.text = "${currentPlayer?.name} ¡Ganó!" //mostramos el mensaje de ganador
                rollDiceButton.isEnabled = false
            }

            // Mostrar el mensaje de serpiente o escalera solo si hubo un cambio de posición
            if (currentPlayer?.position != previousPosition) { // validamos que la posicion del jugador es diferente
                if (currentPlayer?.position!! > previousPosition) { // si la posicion es mayor es porque subio escalera
                    showLadderDialog(previousPosition, currentPlayer?.position!!, diceSum)
                } else { // si no es mayor quiere decir que el jugador paso por una serpiente
                    showSnakeDialog(previousPosition, currentPlayer?.position!!)
                }
            } else {
                // Si no hubo cambio debido a una serpiente o escalera, mostrar el mensaje normal
                showNormalMoveDialog(previousPosition, diceSum)
            }

            // Regla de turno extra si saca un 6
            if (diceRoll1 == 6 || diceRoll2 == 6) {
                turnInfo.text = "${currentPlayer?.name} tiene un turno extra."
            }

            // Alternar entre jugadores
            currentPlayer = if (currentPlayer == player1) player2 else player1

            // Actualizar el tablero visualmente
            updateBoard()

            // Borrar el mensaje de turno extra cuando el otro jugador tira los dados
            if (turnInfo.text.contains("turno extra")) {
                turnInfo.text = ""
            }
        }
    }

    // Funcion que crear el tablero de 8x8 con 64 casillas
    private fun createBoard() {
        for (i in 1..64) { // Iterar por 64 casillas
            val cell = TextView(this) // Crear una nueva celda
            cell.setPadding(10, 10, 10, 10)  // Padding para que las celdas se vean bien
            cell.text = i.toString()  // Número de la casilla
            cell.setBackgroundResource(R.drawable.board_tile)  // Fondo para cada celda
            cell.gravity = android.view.Gravity.CENTER  // Centrado del texto
            cell.textSize = 14f  // Tamaño de texto
            gridLayout.addView(cell)  // Añadir la celda al GridLayout
        }
    }

    // Funcion que muestra el diálogo cuando se cae en una escalera
    private fun showLadderDialog(previousPosition: Int, newPosition: Int, diceSum: Int) {
        val message = "El Jugador ${currentPlayer?.name} ha subido de la casilla $previousPosition a la casilla $newPosition ya que sacó $diceSum en los dados."
        // Crear el cuadro de diálogo
        AlertDialog.Builder(this)
            .setTitle("Escalera")  // Título del diálogo
            .setMessage(message)  // Mensaje que muestra el movimiento
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()  // Cierra el diálogo al presionar "OK"
            }
            .show()  // Muestra el cuadro de diálogo
    }


    // Funcion que muestra el diálogo cuando se cae en una serpiente
    private fun showSnakeDialog(previousPosition: Int, newPosition: Int) {
        val message = "El Jugador ${currentPlayer?.name} ha bajado de la casilla $previousPosition a la casilla $newPosition por una serpiente."
        // Crear el cuadro de diálogo
        AlertDialog.Builder(this)
            .setTitle("Serpiente")  // Título del diálogo
            .setMessage(message)  // Mensaje que muestra el movimiento
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()  // Cierra el diálogo al presionar "OK"
            }
            .show()  // Muestra el cuadro de diálogo
    }


    // Funcion que muestra el mensaje cuando el jugador se mueve normalmente
    private fun showNormalMoveDialog(previousPosition: Int, diceSum: Int) {
        val message = "El Jugador ${currentPlayer?.name} ha avanzado de la casilla $previousPosition a la casilla ${currentPlayer?.position}, ya que sacó $diceSum en los dados."
        // Crear el cuadro de diálogo
        AlertDialog.Builder(this)
            .setTitle("Movimiento Normal")  // Título del diálogo
            .setMessage(message)  // Mensaje que muestra el movimiento
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()  // Cierra el diálogo al presionar "OK"
            }
            .show()  // Muestra el cuadro de diálogo
    }


    // Actualizar las casillas visualmente
    private fun updateBoard() {
        val cells = gridLayout.children.toList()  // Obtiene todas las celdas del tablero

        // Recorrer cada celda y actualizar su contenido
        cells.forEachIndexed { index, view ->
            if (view is TextView) {  // Verifica si la vista es un TextView
                val cell = view
                // Si es la posición de Jugador 1, mostrar su icono
                if (index + 1 == player1.position) {
                    player1Icon?.let {
                        gridLayout.removeView(it)  // Elimina el icono anterior del jugador
                    }
                    player1Icon = ImageView(this)
                    player1Icon?.layoutParams = GridLayout.LayoutParams().apply {
                        width = 30 * resources.displayMetrics.density.toInt()  // Tamaño en dp
                        height = 30 * resources.displayMetrics.density.toInt()  // Tamaño en dp
                        rowSpec = GridLayout.spec(index / 8)  // Especifica la fila
                        columnSpec = GridLayout.spec(index % 8)  // Especifica la columna
                    }
                    player1Icon?.setImageResource(R.drawable.player1_icon)  // Asigna el icono
                    gridLayout.addView(player1Icon)  // Añade el icono al tablero
                    cell.text = ""  // Limpia el texto de la celda
                }
                // Si es la posición de Jugador 2, mostrar su icono
                else if (index + 1 == player2.position) {
                    player2Icon?.let {
                        gridLayout.removeView(it)  // Elimina el icono anterior del jugador
                    }
                    player2Icon = ImageView(this)
                    player2Icon?.layoutParams = GridLayout.LayoutParams().apply {
                        width = 30 * resources.displayMetrics.density.toInt()  // Tamaño en dp
                        height = 30 * resources.displayMetrics.density.toInt()  // Tamaño en dp
                        rowSpec = GridLayout.spec(index / 8)  // Especifica la fila
                        columnSpec = GridLayout.spec(index % 8)  // Especifica la columna
                    }
                    player2Icon?.setImageResource(R.drawable.player2_icon)  // Asigna el icono
                    gridLayout.addView(player2Icon)  // Añade el icono al tablero
                    cell.text = ""  // Limpia el texto de la celda
                }
                // Si la celda no contiene a ningún jugador
                else {
                    cell.text = (index + 1).toString()  // Asigna el número de la casilla
                    cell.setBackgroundResource(R.drawable.board_tile)  // Asigna el fondo de la casilla
                }
            }
        }
    }

}
