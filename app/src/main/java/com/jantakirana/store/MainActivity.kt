package com.jantakirana.store

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.text.TextWatcher
import android.text.Editable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : Activity() {

    private val red = Color.rgb(239, 35, 60)
    private val white = Color.WHITE
    private val muted = Color.rgb(190, 190, 200)
    private val bg = Color.rgb(12, 12, 16)
    private val card = Color.rgb(30, 30, 36)
    private val inputBg = Color.rgb(42, 42, 48)
    private val green = Color.rgb(46, 204, 113)
    private val yellow = Color.rgb(255, 193, 7)

    private val prefs by lazy {
        getSharedPreferences("janta_store_data", MODE_PRIVATE)
    }

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private data class Product(
        val id: String,
        var name: String,
        var barcode: String,
        var buy: Double,
        var sell: Double,
        var qty: Int
    )

    private val products = mutableListOf<Product>()
    private val cart = linkedMapOf<String, Int>()
    private val sales = mutableListOf<JSONObject>()
    private val orders = mutableListOf<JSONObject>()

    private var storeName = "Janta Kirana Store"
    private var lowStockLimit = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.systemUiVisibility = 0
        loadData()
        showHome()
    }

    private fun dp(n: Int): Int =
        (n * resources.displayMetrics.density).toInt()

    private fun money(n: Double): String =
        "₹" + String.format(Locale.US, "%.2f", n)

    private fun now(): Long = System.currentTimeMillis()

    private fun date(ts: Long): String =
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(ts))

    private fun panel(): GradientDrawable {
        return GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(15).toFloat()
        }
    }

    private fun label(
        value: String,
        size: Float = 15f,
        color: Int = white,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(dp(2), dp(5), dp(2), dp(5))
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun field(
        hintText: String,
        value: String = "",
        numeric: Boolean = false
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            setSingleLine(true)
            setText(value)
            setTextColor(white)
            setHintTextColor(Color.LTGRAY)
            textSize = 15f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = GradientDrawable().apply {
                setColor(inputBg)
                cornerRadius = dp(10).toFloat()
                setStroke(dp(1), Color.rgb(70, 70, 78))
            }

            if (numeric) {
                inputType =
                    InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }
    }

    private fun gap(n: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(n))
        }
    }

    private fun vertical(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
    }

    private fun loadData() {
        storeName = prefs.getString(
            "store_name",
            "Janta Kirana Store"
        ) ?: "Janta Kirana Store"

        lowStockLimit = prefs.getInt("low_stock", 5)

        products.clear()
        val p = JSONArray(prefs.getString("products", "[]") ?: "[]")

        for (i in 0 until p.length()) {
            val o = p.getJSONObject(i)
            products.add(
                Product(
                    o.optString("id"),
                    o.optString("name"),
                    o.optString("barcode"),
                    o.optDouble("buy"),
                    o.optDouble("sell"),
                    o.optInt("qty")
                )
            )
        }

        sales.clear()
        val s = JSONArray(prefs.getString("sales", "[]") ?: "[]")
        for (i in 0 until s.length()) {
            sales.add(s.getJSONObject(i))
        }

        orders.clear()
        val d = JSONArray(prefs.getString("orders", "[]") ?: "[]")
        for (i in 0 until d.length()) {
            orders.add(d.getJSONObject(i))
        }
    }

    private fun saveData() {
        val p = JSONArray()

        products.forEach {
            p.put(JSONObject().apply {
                put("id", it.id)
                put("name", it.name)
                put("barcode", it.barcode)
                put("buy", it.buy)
                put("sell", it.sell)
                put("qty", it.qty)
            })
        }

        val s = JSONArray()
        sales.forEach { s.put(it) }

        val d = JSONArray()
        orders.forEach { d.put(it) }

        prefs.edit()
            .putString("products", p.toString())
            .putString("sales", s.toString())
            .putString("orders", d.toString())
            .putString("store_name", storeName)
            .putInt("low_stock", lowStockLimit)
            .apply()
    }

    private fun openScreen(
        title: String,
        subtitle: String = ""
    ) {
        root = vertical().apply {
            setBackgroundColor(bg)
        }

        val header = vertical().apply {
            setPadding(dp(18), dp(15), dp(18), dp(8))
        }

        header.addView(label("🛒  $storeName", 20f, white, true))

        if (subtitle.isNotEmpty()) {
            header.addView(label(subtitle, 11f, muted))
        }

        root.addView(header)

        val scroll = ScrollView(this)

        content = vertical().apply {
            setPadding(dp(16), dp(10), dp(16), dp(20))
        }

        scroll.addView(content)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f
