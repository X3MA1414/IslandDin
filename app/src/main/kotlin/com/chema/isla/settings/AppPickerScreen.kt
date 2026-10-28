package com.chema.isla.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chema.isla.appContainer
import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.SelectableApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    apps: List<SelectableApp>?,
    filterMode: AppFilterMode?,
    onToggle: (String) -> Unit,
    onBack: () -> Unit,
) {
    val icons = LocalContext.current.appContainer.appIconProvider
    var query by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (filterMode == AppFilterMode.ONLY_SELECTED) "Apps que se muestran" else "Apps ocultas")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                },
            )
        },
    ) { padding ->
        if (apps == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val filtered = remember(apps, query) {
            if (query.isBlank()) apps else apps.filter { it.app.label.contains(query, ignoreCase = true) }
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
            items(filtered, key = { it.app.packageName }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onToggle(item.app.packageName) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val bitmap = remember(item.app.packageName) { icons.icon(item.app.packageName) }
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), null, Modifier.size(40.dp))
                    } else {
                        Spacer(Modifier.size(40.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(item.app.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                    Checkbox(checked = item.isSelected, onCheckedChange = { onToggle(item.app.packageName) })
                }
            }
        }
    }
}
