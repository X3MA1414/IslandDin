package com.chema.isla.island

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chema.isla.domain.model.MediaAction
import com.chema.isla.domain.model.MediaInfo
import kotlinx.coroutines.delay

private val SpringSpec = spring<androidx.compose.ui.unit.Dp>(
    dampingRatio = 0.72f,
    stiffness = Spring.StiffnessMediumLow,
)

/** Callbacks de interacción de la isla. */
interface IslandCallbacks {
    fun onTap()
    fun onLongPress()
    fun onMediaAction(action: MediaAction)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Island(
    state: IslandUiState,
    icons: AppIconProvider,
    artwork: ArtworkProvider,
    callbacks: IslandCallbacks,
) {
    if (!state.visible) return
    val width by animateDpAsState(state.widthDp.dp, SpringSpec, label = "width")
    val height by animateDpAsState(state.heightDp.dp, SpringSpec, label = "height")
    val corner by animateDpAsState(state.cornerDp.dp, SpringSpec, label = "corner")
    val accent = Color(state.accentArgb.toInt())

    Box(
        modifier = Modifier
            .size(width, height)
            .clip(RoundedCornerShape(corner))
            .background(Color.Black)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = callbacks::onTap,
                onLongClick = callbacks::onLongPress,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = state.body,
            contentKey = { it::class },
            transitionSpec = { fadeIn(tween(220, delayMillis = 90)) togetherWith fadeOut(tween(90)) },
            label = "body",
        ) { body ->
            when (body) {
                IslandBody.Empty -> Spacer(Modifier.fillMaxSize())
                is IslandBody.Charging -> ChargingBody(body, accent)
                is IslandBody.Notification -> NotificationBody(body, icons.icon(body.packageName))
                is IslandBody.MediaCompact -> MediaCompactBody(body.media, artwork.artwork(body.media.artworkKey), icons, accent)
                is IslandBody.MediaExpanded -> MediaExpandedBody(
                    body.media,
                    artwork.artwork(body.media.artworkKey),
                    icons,
                    accent,
                    callbacks::onMediaAction,
                )
            }
        }
    }
}

@Composable
private fun ChargingBody(body: IslandBody.Charging, accent: Color) {
    val color = when {
        !body.isCharging && body.levelPercent <= 20 -> Color(0xFFFF453A)
        else -> accent
    }
    Row(
        Modifier.fillMaxSize().padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Icon(
            imageVector = if (body.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp),
        )
        Text("${body.levelPercent}%", color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun NotificationBody(body: IslandBody.Notification, icon: Bitmap?) {
    Row(
        Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon, size = 44)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = body.title.ifBlank { body.appLabel },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (body.text.isNotBlank()) {
                Text(
                    text = body.text,
                    color = Color(0xFFB0B0B5),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MediaCompactBody(media: MediaInfo, art: Bitmap?, icons: AppIconProvider, accent: Color) {
    Row(
        Modifier.fillMaxSize().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Artwork(art ?: icons.icon(media.packageName), size = 22, corner = 6)
        Equalizer(accent, playing = media.isPlaying)
    }
}

@Composable
private fun MediaExpandedBody(
    media: MediaInfo,
    art: Bitmap?,
    icons: AppIconProvider,
    accent: Color,
    onAction: (MediaAction) -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Artwork(art ?: icons.icon(media.packageName), size = 52, corner = 12)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(media.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(media.artist, color = Color(0xFFB0B0B5), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Equalizer(accent, playing = media.isPlaying)
        }
        Spacer(Modifier.height(12.dp))
        LiveProgress(media, accent)
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ControlButton(Icons.Filled.SkipPrevious) { onAction(MediaAction.PREVIOUS) }
            ControlButton(if (media.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, big = true) {
                onAction(MediaAction.PLAY_PAUSE)
            }
            ControlButton(Icons.Filled.SkipNext) { onAction(MediaAction.NEXT) }
        }
    }
}

/** Barra de progreso que avanza localmente mientras suena la canción. */
@Composable
private fun LiveProgress(media: MediaInfo, accent: Color) {
    var position by remember(media) { mutableLongStateOf(media.positionMillis) }
    LaunchedEffect(media) {
        while (media.isPlaying && media.durationMillis > 0) {
            delay(500)
            position = (position + 500).coerceAtMost(media.durationMillis)
        }
    }
    val progress = if (media.durationMillis > 0) position.toFloat() / media.durationMillis else 0f
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        color = accent,
        trackColor = Color(0xFF3A3A3C),
        drawStopIndicator = {},
    )
}

@Composable
private fun ControlButton(icon: ImageVector, big: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(if (big) 34.dp else 26.dp))
    }
}

@Composable
private fun AppIcon(bitmap: Bitmap?, size: Int) {
    if (bitmap != null) {
        Image(bitmap.asImageBitmap(), null, Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)))
    } else {
        Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 4).dp)).background(Color(0xFF2C2C2E)))
    }
}

@Composable
private fun Artwork(bitmap: Bitmap?, size: Int, corner: Int) {
    if (bitmap != null) {
        Image(
            bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size.dp).clip(RoundedCornerShape(corner.dp)),
        )
    } else {
        Icon(Icons.Filled.MusicNote, null, tint = Color.White, modifier = Modifier.size(size.dp))
    }
}

/** Cuatro barras que bailan mientras suena la música. */
@Composable
private fun Equalizer(color: Color, playing: Boolean) {
    val transition = rememberInfiniteTransition(label = "eq")
    Row(
        Modifier.height(18.dp).width(22.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(420, 300, 520, 360).forEach { duration ->
            val fraction by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(duration), RepeatMode.Reverse),
                label = "bar",
            )
            Box(
                Modifier
                    .width(3.5.dp)
                    .fillMaxHeight(if (playing) fraction else 0.25f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
