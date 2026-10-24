package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.HistoryRepository
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.calculator.CalculatorViewModelFactory
import com.example.ui.history.HistoryViewModel
import com.example.ui.history.HistoryViewModelFactory
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.transactions.TransactionsViewModel
import com.example.ui.transactions.TransactionsViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var calculatorViewModel: CalculatorViewModel
    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room local database and repository
        val database = AppDatabase.getInstance(applicationContext)
        val historyRepository = HistoryRepository(database.historyDao())

        // Initialize ViewModels with Repository injection
        calculatorViewModel = ViewModelProvider(
            this,
            CalculatorViewModelFactory(historyRepository)
        )[CalculatorViewModel::class.java]

        transactionsViewModel = ViewModelProvider(
            this,
            TransactionsViewModelFactory(historyRepository)
        )[TransactionsViewModel::class.java]

        historyViewModel = ViewModelProvider(
            this,
            HistoryViewModelFactory(historyRepository)
        )[HistoryViewModel::class.java]

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainScreen(
                    calculatorViewModel = calculatorViewModel,
                    transactionsViewModel = transactionsViewModel,
                    historyViewModel = historyViewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}
