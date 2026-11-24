package com.example.trackermoney

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.trackermoney.data.UserPreferencesRepository
import com.example.trackermoney.ui.TrackerMoneyTheme
import kotlinx.coroutines.launch

class AboutActivity : AppCompatActivity() {
    private var isDarkTheme = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            isDarkTheme = UserPreferencesRepository(applicationContext).loadDarkTheme()
            recreateContent()
        }
        recreateContent()
    }

    private fun recreateContent() {
        setContent {
            TrackerMoneyTheme(isDarkTheme = isDarkTheme) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.about_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.about_description))
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { finish() }) {
                        Text(stringResource(R.string.about_back))
                    }
                }
            }
        }
    }
}
