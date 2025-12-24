package com.example.koperasi.navigation

import androidx.compose.runtime.mutableStateListOf

data class CartItem(
    val id: String,
    val name: String,
    val price: String,
    val imageRes: Int,
    val qty: Int = 1,
    val notes: String = ""
)

class CartState {
    val items = mutableStateListOf<CartItem>()

    fun addOrIncrement(id: String, name: String, price: String, imageRes: Int) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val old = items[idx]
            items[idx] = old.copy(qty = old.qty + 1)
        } else {
            items.add(CartItem(id = id, name = name, price = price, imageRes = imageRes))
        }
    }

    fun increment(id: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) items[idx] = items[idx].copy(qty = items[idx].qty + 1)
    }

    fun decrement(id: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val cur = items[idx]
            val newQty = cur.qty - 1
            if (newQty <= 0) items.removeAt(idx)
            else items[idx] = cur.copy(qty = newQty)
        }
    }

    fun updateNotes(id: String, notes: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) items[idx] = items[idx].copy(notes = notes)
    }
}
