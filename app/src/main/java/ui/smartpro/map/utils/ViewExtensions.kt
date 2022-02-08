package ui.smartpro.map.utils

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ui.smartpro.map.R

fun Fragment.navigate(resId: Int, bundle: Bundle? = null, bundle2: Bundle? = null) {
    NavHostFragment.findNavController(this).navigate(resId, bundle)
}
fun Fragment.navigate(dir: NavDirections) {
    findNavController().navigate(dir)
}

fun View.showsnackBar(message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Snackbar
            .make(this, message, Snackbar.LENGTH_LONG)
            .withColor(resources.getColor(R.color.alert_snackbar, null))
            .show()
    }
}

fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar {
    this.view.setBackgroundColor(colorInt)
    return this
}

fun View.showToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

fun Fragment.showToast(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}