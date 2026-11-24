package com.example.trackermoney.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.trackermoney.viewmodel.TrackerViewModel

class HistoryFragment : Fragment() {
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
                HistoryScreen(
                    sortedExpenses = state.sortedExpenses,
                    onEdit = trackerViewModel::editExpense,
                    onDelete = trackerViewModel::deleteExpense
                )
            }
        }
    }
}
