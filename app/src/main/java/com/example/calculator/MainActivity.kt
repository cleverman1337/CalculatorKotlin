package com.example.calculator

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var expression: String = ""
    private var isSecondNumber: Boolean = false
    private var currentOperation: String? = null
    private val pickImageRequestCode = 1
    private lateinit var clickSound: MediaPlayer

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Разрешение получено, открываем галерею
            openImagePicker()
        } else {
            // Разрешение не получено, показываем сообщение
            Log.d("Calculator", "Permission denied")
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val selectedImageUri = result.data?.data
            selectedImageUri?.let {
                try {
                    // Сохраняем URI изображения
                    saveBackgroundUri(it.toString())

                    // Преобразуем URI в BitmapDrawable
                    val inputStream = contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val drawable = BitmapDrawable(resources, bitmap)

                    // Устанавливаем фон
                    findViewById<ImageView>(R.id.background_image).setImageDrawable(drawable)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Запрос разрешений на Android 13 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                //openImagePicker()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            // Для старых версий Android, проверяем READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        restoreBackground()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        window.decorView.isSoundEffectsEnabled = false

        findViewById<Button>(R.id.button_1).setOnClickListener { appendNumber("1") }
        findViewById<Button>(R.id.button_2).setOnClickListener { appendNumber("2") }
        findViewById<Button>(R.id.button_3).setOnClickListener { appendNumber("3") }
        findViewById<Button>(R.id.button_4).setOnClickListener { appendNumber("4") }
        findViewById<Button>(R.id.button_5).setOnClickListener { appendNumber("5") }
        findViewById<Button>(R.id.button_6).setOnClickListener { appendNumber("6") }
        findViewById<Button>(R.id.button_7).setOnClickListener { appendNumber("7") }
        findViewById<Button>(R.id.button_8).setOnClickListener { appendNumber("8") }
        findViewById<Button>(R.id.button_9).setOnClickListener { appendNumber("9") }
        findViewById<Button>(R.id.button_0).setOnClickListener { appendNumber("0") }
        findViewById<Button>(R.id.button_dot).setOnClickListener { appendNumber(".") }
        findViewById<Button>(R.id.button_plus).setOnClickListener { setOperation("+") }
        findViewById<Button>(R.id.button_minus).setOnClickListener { setOperation("-") }
        findViewById<Button>(R.id.button_multiply).setOnClickListener { setOperation("*") }
        findViewById<Button>(R.id.button_divide).setOnClickListener { setOperation("/") }
        findViewById<Button>(R.id.button_procent).setOnClickListener { calculatePercentage() }
        findViewById<Button>(R.id.button_delete_char).setOnClickListener { deleteLastChar() }

        findViewById<Button>(R.id.button_clear).setOnClickListener { clear() }
        findViewById<Button>(R.id.button_equals).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.button_settings).setOnClickListener { openImagePicker() }

        clickSound = MediaPlayer.create(this, R.raw.uwu)
        findViewById<Button>(R.id.button_uwu).setOnClickListener {
            playSound()
        }
    }

    //тоже звук
    private fun playSound() {
        if (clickSound.isPlaying) {
            clickSound.stop()
            clickSound.prepare()
        }
        clickSound.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        clickSound.release()
    }
    //конец звука
    private fun appendNumber(number: String) {
        expression += number
        updateDisplay(expression)
    }

    private fun setOperation(operation: String) {
        if (expression.isNotEmpty() && !expression.endsWith(" ")) {
            expression += " $operation "
            updateDisplay(expression)
        }
    }

    private fun calculateResult() {
        try {
            val result = evaluateExpression(expression)
            updateDisplay(result.toString())
            expression = result.toString()
        } catch (e: Exception) {
            updateDisplay("Error")
        }
    }

    private fun evaluateExpression(expr: String): Any {
        val parts = expr.split(" ").toMutableList()
        var result = parts[0].toDouble()
        var currentOp = ""
        val secondN = ""
        val firstN=""
        val procent=""

        for (i in 1 until parts.size step 2) {
            val op = parts[i]
            val num = parts[i + 1].toDouble()

            when (op) {
                "+" -> result += num
                "-" -> result -= num
                "*" -> result *= num
                "/" -> if (num != 0.0) result /= num else throw ArithmeticException("Division by zero")

            }
        }

        return if (result == result.toInt().toDouble()) {
            result.toInt()
        } else {
            result
        }
    }


    private fun calculatePercentage() {
        if (expression.isNotEmpty()) {
            val parts = expression.split(" ")
            val lastNumber = parts.lastOrNull()?.toDoubleOrNull()
            val base = parts.getOrNull(parts.size - 3)?.toDoubleOrNull()
            val op = parts.getOrNull(parts.size - 2)

            if (lastNumber != null && base != null && op != null) {
                val basepercent = base * (lastNumber / 100)

                val result = when (op) {
                    "+" -> base + basepercent
                    "-" -> base - basepercent
                    "*" -> base * basepercent
                    "/" -> if (basepercent != 0.0) base / basepercent else Double.NaN
                    else -> null
                }

                if (result != null) {
                    expression = expression.dropLast(lastNumber.toString().length + op.length + base.toString().length + 2)
                    expression += " $result"
                    val vivod = expression
                    clear()
                    if (vivod.toDoubleOrNull() != null && vivod.toDouble() == vivod.toDouble().toInt().toDouble()) {
                        updateDisplay(vivod.toDouble().toInt().toString())
                    } else {
                        updateDisplay(vivod)
                    }


                }
            }

        }
    }


    private fun deleteLastChar() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            updateDisplay(expression)
        }
    }

    private fun clear() {
        expression = ""
        updateDisplay("")
    }

    private fun updateDisplay(value: String) {
        Log.d("Calculator", "Updating display with: $value")
        findViewById<TextView>(R.id.textView).text = value
    }
    // открытие галереи
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }
    private fun saveBackgroundUri(uri: String) {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("background_image_uri", uri)
        editor.apply()

        Log.d("Calculator", "Saved URI: $uri")
    }

    private fun restoreBackground() {
        val sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val savedUri = sharedPreferences.getString("background_image_uri", null)
        Log.d("Calculator", "Restored URI: $savedUri")

        savedUri?.let {
            try {
                val uri = Uri.parse(it)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val drawable = BitmapDrawable(resources, bitmap)
                findViewById<ImageView>(R.id.background_image).setImageDrawable(drawable)
            } catch (e: Exception) {
                Log.e("Calculator", "Error loading image", e)
            }
        }
    }

}