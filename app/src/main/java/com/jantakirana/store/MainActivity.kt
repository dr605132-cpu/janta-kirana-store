
package com.jantakirana.store

import android.app.Activity
import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val bg = Color.rgb(8, 11, 16)
    private val card = Color.rgb(20, 29, 41)
    private val red = Color.rgb(245, 25, 48)
    private val white = Color.WHITE
    private val muted = Color.rgb(165, 178, 192)

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
        loadData()
        showHome()
    }

    private fun dp(n: Int) =
        (n * resources.displayMetrics.density).toInt()

    private fun money(n: Double) =
        "₹" + String.format(Locale.US, "%.2f", n)

    private fun now() = System.currentTimeMillis()

    private fun date(ts: Long): String =
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .format(Date(ts))

    private fun panel(): GradientDrawable =
        GradientDrawable().apply {
            setColor(card)
            cornerRadius = dp(15).toFloat()
        }

    private fun label(
        value: String,
        size: Float = 15f,
        color: Int = white,
        bold: Boolean = false
    ) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) setTypeface(null, Typeface.BOLD)
        setPadding(dp(2), dp(5), dp(2), dp(5))
    }

    private fun gap(n: Int) = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(1, dp(n))
    }

    private fun vertical() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    private fun loadData() {
        storeName = prefs.getString("store_name", "Janta Kirana Store")
            ?: "Janta Kirana Store"
        lowStockLimit = prefs.getInt("low_stock", 5)

        products.clear()
        val p = JSONArray(prefs.getString("products", "[]"))
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
        val s = JSONArray(prefs.getString("sales", "[]"))
        for (i in 0 until s.length()) sales.add(s.getJSONObject(i))

        orders.clear()
        val d = JSONArray(prefs.getString("orders", "[]"))
        for (i in 0 until d.length()) orders.add(d.getJSONObject(i))
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

    private fun openScreen(title: String, subtitle: String = "") {
        root = vertical().apply { setBackgroundColor(bg) }

        val header = vertical().apply {
            setPadding(dp(18), dp(20), dp(18), dp(10))
        }
        header.addView(label("🛒  $storeName", 19f, white, true))
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
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        val nav = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(13, 18, 26))
        }

        listOf(
            "⌂\nHome", "▣\nStock", "₹\nBilling",
            "▤\nOrders", "•••\nMore"
        ).forEachIndexed { index, name ->
            val t = label(
                name, 11f,
                if (title == listOf(
                    "Dashboard", "Inventory", "Billing",
                    "Delivery Orders", "More"
                )[index]) red else muted,
                true
            )
            t.gravity = Gravity.CENTER
            t.setOnClickListener {
                when (index) {
                    0 -> showHome()
                    1 -> showStock()
                    2 -> showBilling()
                    3 -> showOrders()
                    else -> showMore()
                }
            }
            nav.addView(t, LinearLayout.LayoutParams(0, dp(53), 1f))
        }

        root.addView(nav)
        setContentView(root)
    }

    private fun button(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val t = label(title, 15f, white, true).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(13), dp(12), dp(13))
            background = GradientDrawable().apply {
                setColor(red)
                cornerRadius = dp(11).toFloat()
            }
            isClickable = true
            setOnClickListener { action() }
        }
        parent.addView(
            t,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(9)
            }
        )
    }

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(gap(12))
        parent.addView(label(title, 18f, white, true))
        parent.addView(gap(5))
    }

    private fun infoCard(
        parent: LinearLayout,
        title: String,
        value: String,
        note: String
    ) {
        val box = vertical().apply {
            setPadding(dp(12), dp(12), dp(10), dp(12))
            background = panel()
        }
        box.addView(label(title, 11f, muted))
        box.addView(label(value, 21f, white, true))
        box.addView(label(note, 10f, red))
        parent.addView(
            box,
            LinearLayout.LayoutParams(0, -2, 1f).apply {
                marginEnd = dp(6)
            }
        )
    }

    private fun showHome() {
        openScreen("Dashboard", "YOUR STORE AT A GLANCE")

        val today = sales.filter {
            isToday(it.optLong("time"))
        }
        val total = today.sumOf { it.optDouble("total") }
        val profit = today.sumOf { it.optDouble("profit") }
        val low = products.count { it.qty <= lowStockLimit }

        val banner = vertical().apply {
            setPadding(dp(17), dp(16), dp(17), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.rgb(125, 8, 28), red)
            ).apply { cornerRadius = dp(16).toFloat() }
        }
        banner.addView(label("TODAY'S SALES", 12f, white, true))
        banner.addView(label(money(total), 31f, white, true))
        banner.addView(label("${today.size} completed bills today", 12f, white))
        content.addView(banner)

        section(content, "STORE OVERVIEW")
        val row = LinearLayout(this)
        infoCard(row, "PRODUCTS", "${products.size}", "In inventory")
        infoCard(row, "LOW STOCK", "$low", "Need attention")
        infoCard(row, "ORDERS", "${orders.size}", "Saved orders")
        content.addView(row)

        section(content, "QUICK ACTIONS")
        button(content, "＋   Add Product") { productDialog(null) }
        button(content, "₹   Start New Billing") { showBilling() }
        button(content, "▦   Search / Scan Barcode") { scanBarcode() }
        button(content, "➜   Customer & Delivery Orders") { showOrders() }

        section(content, "TODAY'S PERFORMANCE")
        content.addView(label("Sales: ${money(total)}", 15f))
        content.addView(label("Estimated profit: ${money(profit)}", 14f, muted))
        content.addView(label("Low-stock products: $low", 14f, muted))

        section(content, "LOW STOCK ALERTS")
        val lowItems = products.filter { it.qty <= lowStockLimit }
        if (lowItems.isEmpty()) {
            content.addView(label("All products are above the alert limit.", 13f, muted))
        } else {
            lowItems.take(5).forEach {
                content.addView(
                    label("${it.name}  •  Qty: ${it.qty}", 14f, Color.YELLOW)
                )
            }
        }
    }

    private fun isToday(ts: Long): Boolean {
        val a = java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(Date(ts))
        val b = java.text.SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        return a == b
    }

    private fun productDialog(existing: Product?) {
        val box = vertical().apply { setPadding(dp(18), dp(8), dp(18), dp(4)) }

        fun field(hint: String, value: String = "", numeric: Boolean = false): EditText {
            return EditText(this).apply {
                this.hint = hint
                setSingleLine(true)
                setText(value)
                if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
        }

        val name = field("Product name", existing?.name ?: "")
        val barcode = field("Barcode (optional)", existing?.barcode ?: "")
        val buy = field("Purchase price", existing?.buy?.toString() ?: "", true)
        val sell = field("Selling price", existing?.sell?.toString() ?: "", true)
        val qty = field("Stock quantity", existing?.qty?.toString() ?: "", true)

        listOf(name, barcode, buy, sell, qty).forEach { box.addView(it) }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Product" else "Edit Product")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim()
                val b = buy.text.toString().toDoubleOrNull()
                val s = sell.text.toString().toDoubleOrNull()
                val q = qty.text.toString().toIntOrNull()

                if (n.isBlank() || b == null || s == null || q == null ||
                    b < 0 || s < 0 || q < 0
                ) {
                    toast("Enter a valid name, prices and quantity")
                    return@setPositiveButton
                }

                if (existing == null) {
                    products.add(
                        Product(
                            java.util.UUID.randomUUID().toString(),
                            n, barcode.text.toString().trim(), b, s, q
                        )
                    )
                } else {
                    existing.name = n
                    existing.barcode = barcode.text.toString().trim()
                    existing.buy = b
                    existing.sell = s
                    existing.qty = q
                }

                saveData()
                showStock()
            }
            .show()
    }

    private fun showStock() {
        openScreen("Inventory", "PRODUCTS • PRICES • STOCK")

        val search = EditText(this).apply {
            hint = "Search product or barcode..."
            setSingleLine(true)
        }
        content.addView(search)
        button(content, "＋   Add New Product") { productDialog(null) }

        val list = vertical()
        content.addView(list)

        fun refresh(query: String = "") {
            list.removeAllViews()
            val filtered = products.filter {
                it.name.contains(query, true) ||
                    it.barcode.contains(query, true)
            }

            if (filtered.isEmpty()) {
                list.addView(label("No products found. Add your first product.", 14f, muted))
            }

            filtered.forEach { p ->
                val box = vertical().apply {
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    background = panel()
                }
                box.addView(label(p.name, 16f, white, true))
                box.addView(label("Barcode: ${p.barcode.ifBlank { "Not set" }}", 11f, muted))
                box.addView(label("Buy ${money(p.buy)}  •  Sell ${money(p.sell)}", 13f))
                box.addView(
                    label(
                        "Stock: ${p.qty}" +
                            if (p.qty <= lowStockLimit) "  ⚠ LOW STOCK" else "  • In stock",
                        13f,
                        if (p.qty <= lowStockLimit) Color.YELLOW else Color.GREEN
                    )
                )

                button(box, "Edit Product") { productDialog(p) }
                button(box, "Delete Product") {
                    AlertDialog.Builder(this)
                        .setTitle("Delete ${p.name}?")
                        .setMessage("This product will be removed from inventory.")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete") { _, _ ->
                            products.remove(p)
                            saveData()
                            showStock()
                        }
                        .show()
                }

                list.addView(box)
                list.addView(gap(8))
            }
        }

        search.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    refresh(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        )

        refresh()
    }

    private fun showBilling() {
        openScreen("Billing", "ADD PRODUCTS • CHECK OUT")

        val search = EditText(this).apply {
            hint = "Search product or barcode..."
            setSingleLine(true)
        }
        content.addView(search)

        val productList = vertical()
        content.addView(productList)

        val cartTitle = label("CURRENT CART", 18f, white, true)
        content.addView(cartTitle)
        val cartList = vertical()
        content.addView(cartList)
        val totalLabel = label("Total: ₹0.00", 23f, red, true)
        content.addView(totalLabel)

        fun refreshCart() {
            cartList.removeAllViews()
            var total = 0.0
            cart.toMap().forEach { (id, qty) ->
                val p = products.find { it.id == id } ?: return@forEach
                total += p.sell * qty
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                }
                row.addView(
                    label("${p.name} × $qty  ${money(p.sell * qty)}", 13f),
                    LinearLayout.LayoutParams(0, -2, 1f)
                )
                val minus = label(" − ", 22f, red, true)
                minus.setOnClickListener {
                    val newQty = (cart[id] ?: 0) - 1
                    if (newQty <= 0) cart.remove(id) else cart[id] = newQty
                    refreshCart()
                }
                val plus = label(" + ", 22f, red, true)
                plus.setOnClickListener {
                    if ((cart[id] ?: 0) < p.qty) {
                        cart[id] = (cart[id] ?: 0) + 1
                    } else toast("Not enough stock")
                    refreshCart()
                }
                row.addView(minus)
                row.addView(plus)
                cartList.addView(row)
            }
            totalLabel.text = "Total: ${money(total)}"
        }

        fun refreshProducts(query: String = "") {
            productList.removeAllViews()
            products.filter {
                it.qty > 0 && (
                    it.name.contains(query, true) ||
                    it.barcode.contains(query, true)
                )
            }.take(30).forEach { p ->
                button(
                    productList,
                    "${p.name}  •  ${money(p.sell)}  •  Stock ${p.qty}  •  ADD"
                ) {
                    val current = cart[p.id] ?: 0
                    if (current < p.qty) {
                        cart[p.id] = current + 1
                        refreshCart()
                    } else toast("Not enough stock")
                }
            }
        }

        search.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    refreshProducts(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            }
        )

        button(content, "▦   Scan Barcode") { scanBarcode() }
        button(content, "Clear Cart") {
            cart.clear()
            refreshCart()
        }
        button(content, "CHECKOUT / SAVE BILL") {
            if (cart.isEmpty()) {
                toast("Add products to the cart first")
            } else {
                checkout()
            }
        }

        refreshProducts()
        refreshCart()
    }

    private fun checkout() {
        val customer = EditText(this).apply { hint = "Customer name (optional)" }
        val phone = EditText(this).apply {
            hint = "Customer phone (optional)"
            inputType = InputType.TYPE_CLASS_PHONE
        }
        val address = EditText(this).apply { hint = "Delivery address (optional)" }

        val box = vertical().apply {
            setPadding(dp(18), dp(8), dp(18), dp(4))
            addView(customer)
            addView(phone)
            addView(address)
        }

        AlertDialog.Builder(this)
            .setTitle("Complete Sale")
            .setMessage("Save bill and reduce stock?")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save Bill") { _, _ ->
                if (cart.isEmpty()) return@setPositiveButton

                var total = 0.0
                var profit = 0.0
                val items = JSONArray()

                for ((id, qty) in cart) {
                    val p = products.find { it.id == id } ?: continue
                    if (qty > p.qty) {
                        toast("Stock changed. Check the cart.")
                        return@setPositiveButton
                    }

                    total += p.sell * qty
                    profit += (p.sell - p.buy) * qty

                    items.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("qty", qty)
                        put("price", p.sell)
                        put("buy", p.buy)
                    })
                }

                val sale = JSONObject().apply {
                    put("id", java.util.UUID.randomUUID().toString())
                    put("time", now())
                    put("total", total)
                    put("profit", profit)
                    put("customer", customer.text.toString().trim())
                    put("phone", phone.text.toString().trim())
                    put("address", address.text.toString().trim())
                    put("items", items)
                }

                cart.forEach { (id, qty) ->
                    products.find { it.id == id }?.let { it.qty -= qty }
                }

                sales.add(sale)

                if (address.text.toString().trim().isNotEmpty() ||
                    customer.text.toString().trim().isNotEmpty()
                ) {
                    orders.add(JSONObject().apply {
                        put("saleId", sale.optString("id"))
                        put("time", sale.optLong("time"))
                        put("customer", customer.text.toString().trim())
                        put("phone", phone.text.toString().trim())
                        put("address", address.text.toString().trim())
                        put("total", total)
                        put("status", "Pending")
                    })
                }

                cart.clear()
                saveData()

                AlertDialog.Builder(this)
                    .setTitle("Bill Saved")
                    .setMessage(
                        "Total: ${money(total)}\n" +
                            "Estimated profit: ${money(profit)}\n" +
                            "Bill time: ${date(sale.optLong("time"))}"
                    )
                    .setPositiveButton("OK") { _, _ -> showHome() }
                    .show()
            }
            .show()
    }

    private fun showOrders() {
        openScreen("Delivery Orders", "CUSTOMERS • LOCAL ORDER HISTORY")
        button(content, "＋   Create Order Through Billing") { showBilling() }

        if (orders.isEmpty()) {
            content.addView(label("No customer orders saved yet.", 14f, muted))
        }

        orders.asReversed().forEach { o ->
            val box = vertical().apply {
                setPadding(dp(12), dp(12), dp(12), dp(12))
                background = panel()
            }

            box.addView(label(o.optString("customer", "Customer"), 16f, white, true))
            box.addView(label(o.optString("phone"), 13f, muted))
            box.addView(label(o.optString("address"), 13f, muted))
            box.addView(label(money(o.optDouble("total")), 16f, red, true))
            box.addView(label(date(o.optLong("time")), 11f, muted))
            box.addView(label("Status: ${o.optString("status")}", 13f))

            button(box, "Update Order Status") {
                val statuses = arrayOf("Pending", "Out for delivery", "Delivered", "Cancelled")
                AlertDialog.Builder(this)
                    .setTitle("Order status")
                    .setItems(statuses) { _, which ->
                        o.put("status", statuses[which])
                        saveData()
                        showOrders()
                    }
                    .show()
            }

            content.addView(box)
            content.addView(gap(8))
        }
    }

    private fun showReports() {
        openScreen("Reports", "SALES • PROFIT • BILL HISTORY")

        val total = sales.sumOf { it.optDouble("total") }
        val profit = sales.sumOf { it.optDouble("profit") }
        val todaySales = sales.filter { isToday(it.optLong("time")) }
            .sumOf { it.optDouble("total") }

        content.addView(label("All-time sales: ${money(total)}", 21f, white, true))
        content.addView(label("Today's sales: ${money(todaySales)}", 17f, red, true))
        content.addView(label("Estimated gross profit: ${money(profit)}", 16f, muted))
        content.addView(label("Completed bills: ${sales.size}", 15f, muted))

        section(content, "BILL HISTORY")

        if (sales.isEmpty()) {
            content.addView(label("No bills saved yet.", 14f, muted))
        }

        sales.asReversed().take(100).forEach { s ->
            content.addView(
                label(
                    "${date(s.optLong("time"))}\n" +
                        "${s.optString("customer").ifBlank { "Walk-in customer" }}  •  " +
                        money(s.optDouble("total")),
                    14f
                )
            )
        }
    }

    private fun showMore() {
        openScreen("More", "STORE SETTINGS • REPORTS • BACKUP")

        button(content, "📊   Sales & Profit Reports") { showReports() }
        button(content, "⚙   Store Settings") { settingsDialog() }
        button(content, "💾   Export Data Backup") { exportBackup() }
        button(content, "⚠   Low Stock Products") {
            openScreen("Low Stock", "PRODUCTS TO RESTOCK")
            val low = products.filter { it.qty <= lowStockLimit }
            if (low.isEmpty()) {
                content.addView(label("No low-stock products.", 14f, muted))
            }
            low.forEach {
                content.addView(
                    label("${it.name}  •  Qty ${it.qty}", 15f, Color.YELLOW)
                )
            }
        }
        button(content, "ℹ   About This App") {
            AlertDialog.Builder(this)
                .setTitle(storeName)
                .setMessage(
                    "Offline-first store manager.\n\n" +
                        "Products, bills and orders are saved on this phone.\n" +
                        "Export backups regularly.\n\n" +
                        "Online sync and online customer ordering are not connected."
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun settingsDialog() {
        val name = EditText(this).apply {
            hint = "Store name"
            setText(storeName)
        }
        val limit = EditText(this).apply {
            hint = "Low stock warning quantity"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(lowStockLimit.toString())
        }

        val box = vertical().apply {
            setPadding(dp(18), dp(8), dp(18), dp(4))
            addView(name)
            addView(limit)
        }

        AlertDialog.Builder(this)
            .setTitle("Store Settings")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim()
                val l = limit.text.toString().toIntOrNull()

                if (n.isNotBlank() && l != null && l >= 0) {
                    storeName = n
                    lowStockLimit = l
                    saveData()
                    showMore()
                } else toast("Enter valid settings")
            }
            .show()
    }

    private fun exportBackup() {
        val backup = JSONObject().apply {
            put("app", "Janta Kirana Store")
            put("exportedAt", date(now()))
            put("storeName", storeName)
            put("lowStockLimit", lowStockLimit)

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

            val o = JSONArray()
            orders.forEach { o.put(it) }

            put("products", p)
            put("sales", s)
            put("orders", o)
        }

        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Janta Kirana Store Backup")
            putExtra(Intent.EXTRA_TEXT, backup.toString(2))
        }

        startActivity(Intent.createChooser(send, "Save or share backup"))
    }

    private fun scanBarcode() {
        try {
            com.google.mlkit.vision.codescanner.GmsBarcodeScanning
                .getClient(this)
                .startScan()
                .addOnSuccessListener { barcode ->
                    val value = barcode.rawValue ?: ""
                    if (value.isBlank()) {
                        toast("No barcode found")
                        return@addOnSuccessListener
                    }

                    val existing = products.find { it.barcode == value }
                    if (existing != null) {
                        AlertDialog.Builder(this)
                            .setTitle(existing.name)
                            .setMessage(
                                "Barcode: $value\n" +
                                    "Price: ${money(existing.sell)}\n" +
                                    "Stock: ${existing.qty}"
                            )
                            .setPositiveButton("OK", null)
                            .setNeutralButton("Edit") { _, _ ->
                                productDialog(existing)
                            }
                            .show()
                    } else {
                        productDialog(
                            Product(
                                java.util.UUID.randomUUID().toString(),
                                "", value, 0.0, 0.0, 0
                            )
                        )
                    }
                }
                .addOnFailureListener {
                    toast("Barcode scan failed")
                }
        } catch (e: Exception) {
            toast("Scanner unavailable. Check Google Play services.")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
