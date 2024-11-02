package com.example.flagquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TitleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        // スタートボタンをクリックしたときにクイズ画面へ遷移
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener {
            // クイズ画面（MainActivity）へ遷移
            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
        }
    }
}