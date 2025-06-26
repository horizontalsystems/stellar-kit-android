package io.horizontalsystems.stellartkit.sample

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.stellarkit.TagQuery
import io.horizontalsystems.stellarkit.room.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionsViewModel : ViewModel() {
    private val kit = App.kit
    private var operations: List<Operation>? = null
    private val tagQuery = TagQuery(null, null, null)
    private var page = 1

    var uiState by mutableStateOf(
        EventsUiState(
            operations = operations
        )
    )
        private set

    init {
        viewModelScope.launch(Dispatchers.Default) {
            kit.operationFlow(tagQuery).collect {
                reloadEvents()
            }
        }

        reloadEvents()
    }

    fun onBottomReached() {
        page++
        reloadEvents()
    }

    private fun reloadEvents() {
        operations = kit.operationsBefore(tagQuery, limit = 10 * page)
        emitState()
    }

    private fun emitState() {
        viewModelScope.launch {
            uiState = EventsUiState(
                operations = operations
            )
        }
    }
}


data class EventsUiState(val operations: List<Operation>?)