package com.example.escaleras_y_serpientes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startGameButton: Button = findViewById(R.id.btn_start_game) // Encuentra el botón de iniciar juego
        startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)  // Inicia GameActivity
            startActivity(intent)
        }
    }
}
