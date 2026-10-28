package com.chema.isla.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.permissions.PermissionStatus
import kotlin.math.roundToInt

private val AccentPresets = listOf(
    0xFF34C759L, 0xFF0A84FFL, 0xFFBF5AF2L, 0xFFFF9F0AL, 0xFFFF375FL, 0xFF64D2FFL, 0xFFFFFFFFL,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: IslandSettings?,
    permissions: PermissionStatus,
    onUpdate: ((IslandSettings) -> IslandSettings) -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenAppDetails: () -> Unit,
    onPickApps: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeTopAppBar(title = { Text("Isla") }, scrollBehavior = scrollBehavior) },
    ) { padding ->
        if (settings == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            permissionsSection(permissions, onOpenAccessibility, onOpenNotificationAccess, onOpenBattery, onOpenAppDetails)
            section("General") {
                SwitchRow("Isla activada", settings.enabled) { v -> onUpdate { it.copy(enabled = v) } }
                SwitchRow("Mostrar píldora en reposo", settings.showIdlePill) { v -> onUpdate { it.copy(showIdlePill = v) } }
                SwitchRow("Ocultar en horizontal", settings.hideInLandscape) { v -> onUpdate { it.copy(hideInLandscape = v) } }
            }
            section("Contenido") {
                SwitchRow("Notificaciones", settings.showNotifications) { v -> onUpdate { it.copy(showNotifications = v) } }
                SwitchRow("Música", settings.showMedia) { v -> onUpdate { it.copy(showMedia = v) } }
                SwitchRow("Carga de batería", settings.showCharging) { v -> onUpdate { it.copy(showCharging = v) } }
                SliderRow(
                    label = "Duración de notificaciones",
                    value = settings.notificationDurationMillis.toInt(),
                    range = IslandSettings.DURATION_RANGE.first.toInt()..IslandSettings.DURATION_RANGE.last.toInt(),
                    format = { "%.1f s".format(it / 1000f) },
                ) { v -> onUpdate { it.copy(notificationDurationMillis = v.toLong()) } }
            }
            section("Tamaño y posición") {
                Text(
                    "Los cambios se ven al momento en la isla real.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                SliderRow("Ancho de la píldora", settings.pillWidthDp, IslandSettings.PILL_WIDTH_RANGE, { "$it dp" }) { v ->
                    onUpdate { it.copy(pillWidthDp = v) }
                }
                SliderRow("Alto de la píldora", settings.pillHeightDp, IslandSettings.PILL_HEIGHT_RANGE, { "$it dp" }) { v ->
                    onUpdate { it.copy(pillHeightDp = v) }
                }
                SliderRow("Ancho expandida", settings.expandedWidthDp, IslandSettings.EXPANDED_WIDTH_RANGE, { "$it dp" }) { v ->
                    onUpdate { it.copy(expandedWidthDp = v) }
                }
                SliderRow("Desplazamiento horizontal", settings.offsetXDp, IslandSettings.OFFSET_RANGE, { "$it dp" }) { v ->
                    onUpdate { it.copy(offsetXDp = v) }
                }
                SliderRow("Desplazamiento vertical", settings.offsetYDp, IslandSettings.OFFSET_RANGE, { "$it dp" }) { v ->
                    onUpdate { it.copy(offsetYDp = v) }
                }
                TextButton(
                    onClick = {
                        onUpdate {
                            val d = IslandSettings()
                            it.copy(
                                pillWidthDp = d.pillWidthDp,
                                pillHeightDp = d.pillHeightDp,
                                expandedWidthDp = d.expandedWidthDp,
                                offsetXDp = 0,
                                offsetYDp = 0,
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) { Text("Restablecer tamaño y posición") }
            }
            section("Color de acento") {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    AccentPresets.forEach { color ->
                        val selected = color == settings.accentColorArgb
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(Color(color.toInt()), CircleShape)
                                .border(
                                    BorderStroke(if (selected) 3.dp else 1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = if (selected) 1f else 0.2f)),
                                    CircleShape,
                                )
                                .clickable { onUpdate { it.copy(accentColorArgb = color) } },
                        )
                    }
                }
            }
            section("Apps") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    val modes = listOf(AppFilterMode.ALL_EXCEPT_SELECTED to "Todas excepto", AppFilterMode.ONLY_SELECTED to "Solo elegidas")
                    modes.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = settings.appFilterMode == mode,
                            onClick = { onUpdate { it.copy(appFilterMode = mode) } },
                            shape = SegmentedButtonDefaults.itemShape(index, modes.size),
                        ) { Text(label) }
                    }
                }
                NavRow(
                    title = "Elegir apps",
                    subtitle = "${settings.selectedPackages.size} seleccionadas",
                    onClick = onPickApps,
                )
            }
            item {
                Text(
                    "Toca la isla con música para expandirla. Mantén pulsado para abrir la app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }
    }
}

private fun LazyListScope.permissionsSection(
    permissions: PermissionStatus,
    onOpenAccessibility: () -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenBattery: () -> Unit,
    onOpenAppDetails: () -> Unit,
) {
    section("Permisos") {
        PermissionRow(
            title = "Accesibilidad",
            description = "Para dibujar la isla sobre la barra de estado.",
            granted = permissions.accessibility,
            onClick = onOpenAccessibility,
        )
        if (!permissions.accessibility) {
            Text(
                "¿Aparece desactivado y no te deja activarlo? Como la app no viene de Google Play, " +
                    "Android lo bloquea: abre Información de la app, toca ⋮ arriba a la derecha y " +
                    "elige \"Permitir ajustes restringidos\".",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            TextButton(onClick = onOpenAppDetails, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text("Abrir información de la app")
            }
        }
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        PermissionRow(
            title = "Acceso a notificaciones",
            description = "Para mostrar notificaciones y controlar la música.",
            granted = permissions.notificationListener,
            onClick = onOpenNotificationAccess,
        )
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        PermissionRow(
            title = "Sin restricción de batería",
            description = "Recomendado para que One UI no cierre la isla.",
            granted = permissions.ignoringBatteryOptimizations,
            onClick = onOpenBattery,
        )
    }
}

private fun LazyListScope.section(title: String, content: @Composable () -> Unit) {
    item(key = title) {
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(Modifier.padding(vertical = 8.dp)) { content() }
            }
        }
    }
}

@Composable
private fun PermissionRow(title: String, description: String, granted: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (granted) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = if (granted) Color(0xFF34C759) else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!granted) {
            Spacer(Modifier.width(8.dp))
            OutlinedButton(onClick = onClick) { Text("Activar") }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** Slider que aplica el cambio al momento para ver el resultado en la isla real. */
@Composable
private fun SliderRow(
    label: String,
    value: Int,
    range: IntRange,
    format: (Int) -> String,
    onChange: (Int) -> Unit,
) {
    var local by remember(value) { mutableFloatStateOf(value.toFloat()) }
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row {
            Text(label, Modifier.weight(1f))
            Text(format(local.roundToInt()), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(
            value = local,
            onValueChange = {
                local = it
                onChange(it.roundToInt())
            },
            valueRange = range.first.toFloat()..range.last.toFloat(),
        )
    }
}

@Composable
private fun NavRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

