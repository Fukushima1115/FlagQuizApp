package com.example.flagquizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.graphics.drawable.PictureDrawable
import com.caverock.androidsvg.SVG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class QuizActivity : AppCompatActivity() {
    // 国旗のURLと国名を格納する配列を宣言
    private var correctCountries = mutableSetOf<String>()
    private lateinit var flagUrls: Array<String>
    private lateinit var countryNames: Array<String>
    private lateinit var correctFlagUrl: String
    private lateinit var options: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // 不正解画面から戻ったときに正解した国のセットを受け取る
        correctCountries = intent.getStringArrayListExtra("correct_countries")?.toMutableSet() ?: mutableSetOf()

        // リソースから国旗のURLと国名を読み込む
        flagUrls = resources.getStringArray(R.array.flag_urls)
        countryNames = resources.getStringArray(R.array.country_names)

        // 保存された状態がある場合は国旗URLと選択肢を復元
        if (savedInstanceState != null) {
            correctFlagUrl = savedInstanceState.getString("flag_url") ?: flagUrls[0]  // デフォルトのURLを指定
            options = savedInstanceState.getStringArrayList("options")?.toMutableList() ?: generateOptions(0).toMutableList()
        } else {
            // ランダムに国旗と選択肢を生成
            val correctIndex = Random.nextInt(flagUrls.size)
            correctFlagUrl = flagUrls[correctIndex]
            options = generateOptions(correctIndex).toMutableList()
        }

        // 国旗画像を表示する ImageView
        val flagImage: ImageView = findViewById(R.id.flagImage)

        // コルーチンを使ってSVG画像を読み込み、表示
        CoroutineScope(Dispatchers.Main).launch {
            val svgDrawable = loadSvgFromUrl(correctFlagUrl)
            svgDrawable?.let {
                flagImage.setLayerType(ImageView.LAYER_TYPE_SOFTWARE, null)
                flagImage.setImageDrawable(it)
            }
        }

        // ボタンに選択肢をセット
        val option1: Button = findViewById(R.id.option1)
        val option2: Button = findViewById(R.id.option2)
        val option3: Button = findViewById(R.id.option3)
        val option4: Button = findViewById(R.id.option4)

        option1.text = options[0]
        option2.text = options[1]
        option3.text = options[2]
        option4.text = options[3]

        // ボタンがクリックされたときの処理
        option1.setOnClickListener { checkAnswer(options[0], countryNames[flagUrls.indexOf(correctFlagUrl)]) }
        option2.setOnClickListener { checkAnswer(options[1], countryNames[flagUrls.indexOf(correctFlagUrl)]) }
        option3.setOnClickListener { checkAnswer(options[2], countryNames[flagUrls.indexOf(correctFlagUrl)]) }
        option4.setOnClickListener { checkAnswer(options[3], countryNames[flagUrls.indexOf(correctFlagUrl)]) }
    }

    // 状態保存
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("flag_url", correctFlagUrl)
        outState.putStringArrayList("options", ArrayList(options))  // 選択肢を保存
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

    // 正解1つとランダムな不正解3つを含む選択肢を生成する関数
    private fun generateOptions(correctIndex: Int): List<String> {
        val options = mutableListOf<String>()
        options.add(countryNames[correctIndex])  // 正解を追加

        // ランダムな不正解を3つ追加
        while (options.size < 4) {
            val randomIndex = Random.nextInt(countryNames.size)
            if (randomIndex != correctIndex && !options.contains(countryNames[randomIndex])) {
                options.add(countryNames[randomIndex])
            }
        }

        // 選択肢の順番をシャッフル
        options.shuffle()
        return options
    }

    // 回答が正解かどうかを判定する関数
    private fun checkAnswer(selectedAnswer: String, correctAnswer: String) {
        if (selectedAnswer == correctAnswer) {
            // 新しい国なら正解セットに追加
            correctCountries.add(correctAnswer)
            val intent = Intent(this, QuizActivity::class.java)
            intent.putStringArrayListExtra("correct_countries", ArrayList(correctCountries))
            startActivity(intent)
        } else {
            // 不正解なら不正解画面に遷移し、正解国セットを渡す
            val intent = Intent(this, IncorrectActivity::class.java)
            intent.putStringArrayListExtra("correct_countries", ArrayList(correctCountries))
            intent.putExtra("incorrect_flag_url", correctFlagUrl)
            intent.putExtra("incorrect_country_name", countryNames[flagUrls.indexOf(correctFlagUrl)])
            startActivity(intent)
        }
    }
}
