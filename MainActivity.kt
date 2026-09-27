package com.ckpay.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri

data class Order(
    val id: String,
    val type: String,
    val amount: String,
    val status: String
)

class MainActivity : ComponentActivity() {
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) selectedProof.value = uri
    }

    companion object {
        val selectedProof = mutableStateOf<Uri?>(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CKPayApp(onPickProof = { pickImage.launch("image/*") }) }
    }
}

@Composable
fun CKPayApp(onPickProof: () -> Unit) {
    var screen by remember { mutableStateOf("home") }
    var orderType by remember { mutableStateOf("Buy") }
    var amount by remember { mutableStateOf("") }
    var orders by remember { mutableStateOf(listOf<Order>()) }
    var currentOrder by remember { mutableStateOf<Order?>(null) }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("CK Pay") })
            }
        ) { padding ->
            when (screen) {
                "home" -> HomeScreen(
                    orders = orders,
                    onCreate = { type ->
                        orderType = type
                        screen = "create"
                    },
                    onOrder = { o -> currentOrder = o; screen = "order" }
                )
                "create" -> CreateOrderScreen(
                    type = orderType,
                    amount = amount,
                    onAmount = { amount = it },
                    onBack = { screen = "home" },
                    onContinue = {
                        val id = "CK${System.currentTimeMillis().toString().takeLast(8)}"
                        currentOrder = Order(id, orderType, amount, "Payment Pending")
                        orders = listOf(currentOrder!!) + orders
                        screen = "payment"
                    }
                )
                "payment" -> PaymentScreen(
                    order = currentOrder!!,
                    onPickProof = onPickProof,
                    onSubmitted = {
                        currentOrder = currentOrder!!.copy(status = "Pending Verification")
                        orders = orders.map { if (it.id == currentOrder!!.id) currentOrder!! else it }
                        screen = "order"
                    }
                )
                "order" -> OrderScreen(
                    order = currentOrder!!,
                    onBack = { screen = "home" },
                    onPay = { screen = "payment" }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(orders: List<Order>, onCreate: (String) -> Unit, onOrder: (Order) -> Unit) {
    Column(Modifier.padding(20.dp)) {
        Text("Welcome to CK Pay", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onCreate("Buy") }, modifier = Modifier.weight(1f)) { Text("Buy") }
            OutlinedButton(onClick = { onCreate("Sell") }, modifier = Modifier.weight(1f)) { Text("Sell") }
        }
        Spacer(Modifier.height(24.dp))
        Text("Orders", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (orders.isEmpty()) Text("No orders yet.")
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { o ->
                Card(onClick = { onOrder(o) }, modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${o.type} • ₹${o.amount}")
                        Text("Order ${o.id}")
                        Text(o.status)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateOrderScreen(
    type: String, amount: String, onAmount: (String) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        Text("$type Order", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = amount,
            onValueChange = { if (it.all(Char::isDigit)) onAmount(it) },
            label = { Text("Amount (₹)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(18.dp))
        Button(
            enabled = amount.isNotBlank() && amount.toLongOrNull() ?: 0 > 0,
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continue") }
        TextButton(onClick = onBack) { Text("Back") }
    }
}

@Composable
fun PaymentScreen(order: Order, onPickProof: () -> Unit, onSubmitted: () -> Unit) {
    val proof = MainActivity.selectedProof.value
    Column(Modifier.padding(20.dp)) {
        Text("Payment", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(10.dp))
        Text("Order: ${order.id}")
        Text("Amount: ₹${order.amount}")
        Spacer(Modifier.height(18.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Payment details", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text("UPI ID: your-upi@bank")
                Text("Account No.: 000000000000")
                Text("IFSC: DEMO0000000")
                Spacer(Modifier.height(8.dp))
                Text("Demo details only — replace before production.")
            }
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onPickProof, modifier = Modifier.fillMaxWidth()) {
            Text(if (proof == null) "Upload payment screenshot" else "Screenshot selected")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            enabled = proof != null,
            onClick = onSubmitted,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Payment Done → Send for Verification") }
    }
}

@Composable
fun OrderScreen(order: Order, onBack: () -> Unit, onPay: () -> Unit) {
    Column(Modifier.padding(20.dp)) {
        Text("Order Details", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Order ID: ${order.id}")
        Text("Type: ${order.type}")
        Text("Amount: ₹${order.amount}")
        Spacer(Modifier.height(12.dp))
        Text("Status: ${order.status}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(20.dp))
        if (order.status == "Payment Pending") {
            Button(onClick = onPay, modifier = Modifier.fillMaxWidth()) { Text("Pay Now") }
        }
        TextButton(onClick = onBack) { Text("Back to Home") }
    }
}
