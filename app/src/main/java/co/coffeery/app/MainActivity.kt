package co.coffeery.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.screens.root.RootScreen

class MainActivity : AppCompatActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RootScreen(viewModel)
        }
    }
}
