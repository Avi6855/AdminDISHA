package com.avinashpatil.app.admindisha.utils

import android.content.Context
import android.widget.Toast

// Utility function to show toast messages
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}