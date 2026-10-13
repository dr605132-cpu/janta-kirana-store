package com.jantakirana.store

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

class MainActivity : Activity() {

    private val bg = Color.rgb(8, 11, 16)
    private val card = Color.rgb(19, 27, 38)
    private val red = Color.rgb(245, 25, 48)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 172, 188)
    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 16): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int = white,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun gap(height: Int): View =
        View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(height))
        }

    private fun column(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

    private fun base(title: String, subtitle: String = "") {
        root = column().apply {
            setBackgroundColor(bg)
        }

        val header = column().apply {
            setPadding(dp(20), dp(22), dp(20), dp(16))
        }

        header.addView(text("🛒  JANTA KIRANA STORE", 20f, white, true))
        header.addView(gap(5))

        if (subtitle.isNotEmpty()) {
            header.addView(text(subtitle, 12f, muted))
        }

        root.addView(header)

        val content = column().apply {
            setPadding(dp(18), dp(4), dp(18), dp(18))
        }

        content.addView(text(title, 25f, white, true))
        content.addView(gap(14))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(9), dp(4), dp(9))
            setBackgroundColor(Color.rgb(13, 18, 26))
        }

        addNav(nav, "⌂\nHome") { showHome() }
        addNav(nav, "▣\nStock") { showStock() }
        addNav(nav, "₹\nBilling") { showBilling() }
        addNav(nav, "▤\nOrders") { showOrders() }
        addNav(nav, "•••\nMore") { showMore() }

        root.addView(nav)
        setContentView(root)
    }

    private fun addNav(
        nav: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val item = text(label, 12f, muted, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(2), dp(5), dp(2), dp(5))
            isClickable = true
            setOnClickListener { action() }
        }

        nav.addView(
            item,
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )
    }

    private fun addCard(
        parent: LinearLayout,
        title: String,
        value: String,
        detail: String,
        accent: Int = red
    ) {
        val box = column().apply {
            setPadding(dp(14), dp(14), dp(12), dp(14))
            background = rounded(card)
        }

        box.addView(text(title, 12f, muted))
        box.addView(gap(8))
        box.addView(text(value, 23f, white, true))
        box.addView(gap(5))
        box.addView(text(detail, 11f, accent))

        parent.addView(
            box,
            LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                marginEnd = dp(7)
            }
        )
    }

    private fun addAction(
        parent: LinearLayout,
        icon: String,
        title: String,
        detail: String,
        action: () -> Unit
    ) {
        val box = column().apply {
            setPadding(dp(13), dp(15), dp(8), dp(13))
            background = rounded(card)
            isClickable = true
            setOnClickListener { action() }
        }

        box.addView(text(icon, 25f, red, true))
        box.addView(gap(9))
        box.addView(text(title, 15f, white, true))
        box.addView(gap(4))
        box.addView(text(detail, 11f, muted))

        parent.addView(
            box,
            LinearLayout.LayoutParams(0, dp(112), 1f).apply {
                marginEnd = dp(8)
                bottomMargin = dp(8)
            }
        )
    }

    private fun addRow(
        parent: LinearLayout,
        left: Triple<String, String, String>,
        right: Triple<String, String, String>,
        leftAction: () -> Unit,
        rightAction: () -> Unit
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addAction(row, left.first, left.second, left.third, leftAction)
        addAction(row, right.first, right.second, right.third, rightAction)
        parent.addView(row)
    }

    private fun showHome() {
        base("Dashboard", "YOUR STORE, SMARTER EVERY DAY")

        val content = findContent()
        content.addView(gap(4))

        val sales = column().apply {
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(Color.rgb(115, 8, 24))
        }

        sales.addView(text("TODAY'S SALES", 13f, Color.LTGRAY, true))
        sales.addView(gap(8))
        sales.addView(text("₹ 0.00", 32f, white, true))
        sales.addView(gap(5))
        sales.addView(text("Sales total will appear after billing", 12f, white))

        content.addView(sales)
        content.addView(gap(14))

        val metrics = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        addCard(metrics, "PRODUCTS", "0", "Manage inventory")
        addCard(metrics, "LOW STOCK", "0", "Restock alerts", Color.YELLOW)
        addCard(metrics, "ORDERS", "0", "Today's orders", Color.CYAN)
        content.addView(metrics)

        content.addView(gap(22))
        content.addView(text("QUICK ACTIONS", 17f, white, true))
        content.addView(gap(12))

        addRow(
            content,
            Triple("＋", "Add Product", "New stock item"),
            Triple("₹", "New Billing", "Create a bill"),
            { showStock() },
            { showBilling() }
        )

        addRow(
            content,
            Triple("▦", "Scan Barcode", "Find a product"),
            Triple("➜", "Delivery Orders", "Manage orders"),
            { scanBarcode() },
            { showOrders() }
        )

        content.addView(gap(14))
        content.addView(text("STORE TOOLS", 17f, white, true))
        content.addView(gap(10))

        val tools = column().apply {
            setPadding(dp(15), dp(15), dp(15), dp(15))
            background = rounded(card)
        }

        tools.addView(text("📈  Sales & Reports", 16f, white, true))
        tools.addView(gap(5))
        tools.addView(text("View your store performance and totals.", 12f, muted))
        tools.addView(gap(12))
        tools.addView(text("⚙  Store Settings", 16f, white, true))
        tools.isClickable = true
        tools.setOnClickListener { showMore() }
        content.addView(tools)

        content.addView(gap(18))
        content.addView(
            text("JANTA KIRANA STORE  •  SMART SHOP MANAGEMENT", 10f, muted)
        )
    }

    private fun findContent(): LinearLayout {
        val scroll = root.getChildAt(1) as ScrollView
        return scroll.getChildAt(0) as LinearLayout
    }

    private fun showStock() {
        base("Inventory", "PRODUCT AND STOCK MANAGEMENT")
        val c = findContent()

        c.addView(text("Your product list will appear here.", 15f, muted))
        c.addView(gap(16))

        addAction(c, "＋", "Add Product", "Start adding products") {
            Toast.makeText(this, "Product form will be added next", Toast.LENGTH_SHORT).show()
        }

        addAction(c, "▦", "Scan Barcode", "Scan product code") {
            scanBarcode()
        }
    }

    private fun showBilling() {
        base("Billing", "CREATE A CUSTOMER BILL")
        val c = findContent()

        c.addView(text("No products added yet.", 15f, muted))
        c.addView(gap(12))

        addAction(c, "▦", "Scan Barcode", "Find a product") {
            scanBarcode()
        }

        addAction(c, "＋", "Add Product", "Add an inventory item") {
            showStock()
        }

        c.addView(gap(12))
        c.addView(text("TOTAL: ₹ 0.00", 23f, red, true))
        c.addView(gap(8))
        c.addView(text("Add products before creating a bill.", 13f, muted))
    }

    private fun showOrders() {
        base("Delivery Orders", "MANAGE CUSTOMER DELIVERIES")
        val c = findContent()

        c.addView(text("No delivery orders yet.", 16f, muted))
        c.addView(gap(12))
        c.addView(text("Customer orders can be connected in the next step.", 13f, muted))
    }

    private fun showMore() {
        base("More", "STORE MANAGEMENT")
        val c = findContent()

        c.addView(text("Janta Kirana Store", 20f, white, true))
        c.addView(gap(10))
        c.addView(text("App version 1.0", 13f, muted))
        c.addView(gap(14))
        c.addView(text("Next upgrades", 17f, white, true))
        c.addView(gap(10))
        c.addView(text("• Saved inventory\n• Working billing and receipts\n• Sales reports\n• Customer delivery orders\n• Data backup", 15f, muted))
    }

    private fun scanBarcode() {
        try {
            GmsBarcodeScanning.getClient(this)
                .startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue ?: "Unknown"
                    Toast.makeText(
                        this,
                        "Scanned code: $value",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Barcode scan failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_LONG).show()
        }
    }
}
