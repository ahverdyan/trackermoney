package com.example.trackermoney.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackermoney.viewmodel.TrackerViewModel

/** Hosts the existing Compose analytics screen as a real Fragment destination. */
class AnalyticsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val trackerViewModel: TrackerViewModel = viewModel(viewModelStoreOwner = requireActivity())
            val state by trackerViewModel.uiState.collectAsState()
            TrackerMoneyTheme(isDarkTheme = state.isDarkTheme) {
                AnalyticsScreen(
                    data = state.analyticsData,
                    selectedPeriod = state.analyticsPeriod,
                    onPeriodChange = trackerViewModel::selectAnalyticsPeriod
                )
            }
        }
    }
}
