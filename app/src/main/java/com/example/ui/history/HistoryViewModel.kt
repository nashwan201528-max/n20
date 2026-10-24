package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val records: List<CalculationRecord> = emptyList(),
    val filteredRecords: List<CalculationRecord> = emptyList(),
    val selectedCategory: String = "ALL", // "ALL", "CALCULATOR", "CURRENCY", "TAX", "LOAN", "DISCOUNT", "TIP", "MARGIN", "FAVORITE"
    val searchQuery: String = "",
    val selectedRecordForDetail: CalculationRecord? = null,
    val isConfirmClearAllVisible: Boolean = false,
    val userNotice: String? = null
)

class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.getAllRecords().collect { allList ->
                _uiState.update { current ->
                    val filtered = applyFilter(allList, current.selectedCategory, current.searchQuery)
                    current.copy(records = allList, filteredRecords = filtered)
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.records, current.selectedCategory, query)
            current.copy(searchQuery = query, filteredRecords = filtered)
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.records, category, current.searchQuery)
            current.copy(selectedCategory = category, filteredRecords = filtered)
        }
    }

    fun onSelectRecord(record: CalculationRecord) {
        _uiState.update { it.copy(selectedRecordForDetail = record) }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(selectedRecordForDetail = null) }
    }

    fun onToggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            historyRepository.toggleFavorite(record.id, !record.isFavorite)
        }
    }

    fun onDeleteRecord(record: CalculationRecord) {
        viewModelScope.launch {
            historyRepository.deleteRecord(record)
            _uiState.update {
                if (it.selectedRecordForDetail?.id == record.id) {
                    it.copy(selectedRecordForDetail = null, userNotice = "Record deleted")
                } else {
                    it.copy(userNotice = "Record deleted")
                }
            }
        }
    }

    fun showClearAllDialog(show: Boolean) {
        _uiState.update { it.copy(isConfirmClearAllVisible = show) }
    }

    fun onConfirmClearAll() {
        viewModelScope.launch {
            historyRepository.clearAll()
            _uiState.update { it.copy(isConfirmClearAllVisible = false, selectedRecordForDetail = null, userNotice = "All history cleared") }
        }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }

    private fun applyFilter(list: List<CalculationRecord>, category: String, query: String): List<CalculationRecord> {
        return list.filter { record ->
            val matchesCategory = when (category) {
                "ALL" -> true
                "FAVORITE" -> record.isFavorite
                "CALCULATOR" -> record.type == "STANDARD" || record.type == "SCIENTIFIC" || record.type == "CALCULATOR"
                else -> record.type.equals(category, ignoreCase = true)
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                record.expression.contains(query, ignoreCase = true) ||
                        record.result.contains(query, ignoreCase = true) ||
                        record.title.contains(query, ignoreCase = true) ||
                        record.secondaryDetails.contains(query, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }
}

class HistoryViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
