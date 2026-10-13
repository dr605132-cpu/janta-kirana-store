
package com.jantakirana.store

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : Activity() {

    private val red = Color.rgb(220, 35, 45)
    private val black = Color.rgb(20, 20, 20)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 20)
            setBackgroundColor(black)
        }

        val title = TextView(this).apply {
            text = "JANTA KIRANA STORE"
            textSize = 25f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 8)
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "SMART STORE • INVENTORY • BILLING"
            textSize = 12f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        root.addView(subtitle)

        addButton(root, "📦  Inventory / Stock") {
            Toast.makeText(this, "Inventory module next step mein add hoga", Toast.LENGTH_SHORT).show()
        }

        addButton(root, "🧾  Billing / Sales") {
            Toast.makeText(this, "Billing module next step mein add hoga", Toast.LENGTH_SHORT).show()
        }

        addButton(root, "📷  Scan Barcode") {
            try {
                GmsBarcodeScanning.getClient(this)
                    .startScan()
                    .addOnSuccessListener { barcode ->
                        val value = barcode.rawValue ?: "Unknown"
                        Toast.makeText(this, "Barcode: $value", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Barcode scan failed", Toast.LENGTH_LONG).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_LONG).show()
            }
        }

        addButton(root, "🛵  Delivery Orders") {
            Toast.makeText(this, "Delivery module next step mein add hoga", Toast.LENGTH_SHORT).show()
        }

        addButton(root, "📊  Store Summary") {
            Toast.makeText(this, "Summary module next step mein add hoga", Toast.LENGTH_SHORT).show()
        }

        val note = TextView(this).apply {
            text = "Janta Kirana Store\nYour shop, managed digitally."
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 0)
        }
        root.addView(note)

        setContentView(root)
    }

    private fun addButton(
        root: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = TextView(this).apply {
            text = label
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 17, 12, 17)
            setBackgroundColor(red)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 7, 0, 7)
        root.addView(button, params)
    }
}
