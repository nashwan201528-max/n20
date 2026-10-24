package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationRecord
import com.example.ui.history.HistoryViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onReuseInCalculator: (CalculationRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    // Helper to copy text to system clipboard
    fun copyToClipboard(text: String, label: String = "Calculation") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        copiedMessage = "Copied to clipboard!"
    }

    LaunchedEffect(copiedMessage) {
        if (copiedMessage != null) {
            kotlinx.coroutines.delay(2000)
            copiedMessage = null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Bar & Clear Action Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Search past calculations...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f).testTag("history_search_input")
            )

            if (uiState.records.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.showClearAllDialog(true) },
                    modifier = Modifier.testTag("btn_clear_all_history")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = CoralRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        val categories = listOf(
            "ALL" to "All",
            "CALCULATOR" to "Calculator",
            "CURRENCY" to "Currency",
            "UNITS" to "Units",
            "TAX" to "Tax",
            "LOAN" to "Loan",
            "DISCOUNT" to "Discount",
            "TIP" to "Tip & Split",
            "MARGIN" to "Margin",
            "FAVORITE" to "Starred"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().testTag("history_category_filters")
        ) {
            items(categories) { (key, label) ->
                FilterChip(
                    selected = uiState.selectedCategory == key,
                    onClick = { viewModel.onCategorySelected(key) },
                    label = { Text(label, fontSize = 12.sp) },
                    leadingIcon = if (key == "FAVORITE") {
                        { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = AmberAccent) }
                    } else null,
                    modifier = Modifier.testTag("filter_$key")
                )
            }
        }

        // Notification Snackbar
        AnimatedVisibility(
            visible = copiedMessage != null || uiState.userNotice != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val msg = copiedMessage ?: uiState.userNotice ?: ""
            Surface(
                color = Slate800,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // History Content or Empty State
        if (uiState.filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp).testTag("history_empty_state")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty()) "No matching records found" else "No Calculation History Yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Calculations and transactions you evaluate or save will appear in this tape. Tap any past entry to inspect or re-use it.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f).testTag("history_records_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.filteredRecords, key = { it.id }) { record ->
                    HistoryRecordCard(
                        record = record,
                        onClick = { viewModel.onSelectRecord(record) },
                        onReuse = { onReuseInCalculator(record) },
                        onCopy = { copyToClipboard("${record.expression} = ${record.result}") },
                        onToggleFavorite = { viewModel.onToggleFavorite(record) },
                        onDelete = { viewModel.onDeleteRecord(record) }
                    )
                }
            }
        }
    }

    // Detail Dialog when a record is clicked
    uiState.selectedRecordForDetail?.let { record ->
        HistoryDetailDialog(
            record = record,
            onDismiss = viewModel::onDismissDetail,
            onReuse = {
                onReuseInCalculator(record)
                viewModel.onDismissDetail()
            },
            onCopy = { copyToClipboard("${record.expression} = ${record.result}\n${record.secondaryDetails}") },
            onDelete = { viewModel.onDeleteRecord(record) },
            onToggleFavorite = { viewModel.onToggleFavorite(record) }
        )
    }

    // Clear All Confirmation Dialog
    if (uiState.isConfirmClearAllVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearAllDialog(false) },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently remove all past calculations and transaction receipts from local storage.") },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmClearAll,
                    colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearAllDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: CalculationRecord,
    onClick: () -> Unit,
    onReuse: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, badgeColor) = getCategoryMeta(record.type)
    val timeFormatted = remember(record.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth().testTag("history_item_${record.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Type badge, timestamp, favorite star, delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = record.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp).testTag("btn_fav_${record.id}")
                    ) {
                        Icon(
                            imageVector = if (record.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (record.isFavorite) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Expression & Main Result
            Text(
                text = record.expression,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = record.result,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (record.secondaryDetails.isNotBlank()) {
                Text(
                    text = record.secondaryDetails,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Row: "Re-use", "Copy", "Delete"
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReuse,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("btn_reuse_${record.id}")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-use in Calc", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp).testTag("btn_copy_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("btn_delete_${record.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete record",
                            tint = CoralRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryDetailDialog(
    record: CalculationRecord,
    onDismiss: () -> Unit,
    onReuse: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val timeFormatted = remember(record.timestamp) {
        SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault()).format(Date(record.timestamp))
    }
    val (icon, badgeColor) = getCategoryMeta(record.type)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor)
                Text(record.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(timeFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                HorizontalDivider()

                Column {
                    Text("Input Expression / Formula:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(record.expression, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Column {
                    Text("Evaluated Result:", fontSize = 11.sp, color = TealAccent)
                    Text(
                        record.result,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (record.secondaryDetails.isNotBlank()) {
                    Column {
                        Text("Breakdown / Details:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(record.secondaryDetails, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onReuse) {
                Icon(Icons.AutoMirrored.Filled.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Re-use in Calculator")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                TextButton(onClick = { onDelete(); onDismiss() }) {
                    Text("Delete", color = CoralRed)
                }
            }
        }
    )
}

fun getCategoryMeta(type: String): Pair<ImageVector, Color> {
    return when (type) {
        "CURRENCY" -> Icons.Default.CurrencyExchange to TealAccent
        "UNITS" -> Icons.Default.Straighten to TealPrimary
        "TAX" -> Icons.Default.Receipt to AmberAccent
        "LOAN" -> Icons.Default.AccountBalance to IndigoAccent
        "DISCOUNT" -> Icons.Default.LocalOffer to TealPrimary
        "TIP" -> Icons.Default.Restaurant to AmberAccent
        "MARGIN" -> Icons.AutoMirrored.Filled.TrendingUp to TealAccent
        "SCIENTIFIC" -> Icons.Default.Science to IndigoAccent
        else -> Icons.Default.Calculate to Slate400
    }
}
