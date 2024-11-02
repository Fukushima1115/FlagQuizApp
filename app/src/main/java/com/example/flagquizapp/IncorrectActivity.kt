package com.example.flagquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class IncorrectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incorrect)

        // Intentから正解した国のリストを受け取る
        val correctCountries = intent.getStringArrayListExtra("correct_countries") ?: arrayListOf()
        val incorrectFlagUrl = intent.getStringExtra("incorrect_flag_url")
        val incorrectCountryName = intent.getStringExtra("incorrect_country_name")

        // 正解した国の数を取得
        val uniqueCorrectCountryCount = correctCountries.size

        // TextViewに正解した国の数を表示
        val correctCountTextView: TextView = findViewById(R.id.correctCountTextView)
        correctCountTextView.text = getString(R.string.score, uniqueCorrectCountryCount)

        // 不正解の国旗画像を表示する ImageView
        val flagImageView: ImageView = findViewById(R.id.incorrectFlagImageView)

        // コルーチンを使ってSVG画像を読み込み、表示
        CoroutineScope(Dispatchers.Main).launch {
            val svgDrawable = loadSvgFromUrl(incorrectFlagUrl ?: "")
            svgDrawable?.let {
                flagImageView.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
                flagImageView.setImageDrawable(it)
            }
        }

        // TextViewに不正解の国名を表示
        val countryNameTextView: TextView = findViewById(R.id.countryNameTextView)
        countryNameTextView.text = incorrectCountryName

        // タイトル画面に戻るボタン
        val returnToTitleButton: Button = findViewById(R.id.returnToTitleButton)
        returnToTitleButton.setOnClickListener {
            val intent = Intent(this, TitleActivity::class.java)
            startActivity(intent)
        }
    }
}

// SVG画像をURLから読み込む関数
private suspend fun loadSvgFromUrl(url: String): PictureDrawable? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val svg = SVG.getFromInputStream(inputStream)
            val picture = svg.renderToPicture()
            PictureDrawable(picture)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
