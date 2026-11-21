package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.R
import com.example.engine.CombatPhase
import com.example.engine.CombatState
import com.example.engine.ManaPaymentPreview
import com.example.engine.PriorityHolder
import com.example.ui.theme.ArcaneCyan
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.CrimsonNexus
import com.example.ui.theme.DarkButtonInk
import com.example.ui.theme.HextechGold
import com.example.ui.theme.HextechGoldBright
import com.example.ui.theme.HextechGoldDeep
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.LegendaryAmber
import com.example.ui.theme.ManaBlue
import com.example.ui.theme.MutedSilver
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ParchmentWhite
import kotlin.math.cos
import kotlin.math.sin

val StatDropShadow = Shadow(
    color = Color.Black.copy(alpha = 0.92f),
    offset = Offset(0f, 3f),
    blurRadius = 6f
)

fun tabularStatStyle(fontSize: TextUnit, color: Color = ParchmentWhite): TextStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = fontSize,
    color = color,
    fontFeatureSettings = "tnum",
    shadow = StatDropShadow
)

fun fantasyHeadingStyle(
    fontSize: TextUnit,
    color: Color = ParchmentWhite,
    withShadow: Boolean = true
): TextStyle = TextStyle(
    fontFamily = CinzelFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = fontSize,
    color = color,
    letterSpacing = 0.6.sp,
    shadow = if (withShadow) StatDropShadow else null
)

fun interBodyStyle(fontSize: TextUnit, color: Color = ParchmentWhite): TextStyle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = fontSize,
    color = color
)

/**
 * Subtle physical film grain / noise overlay (5–10% opacity) using SrcOver so it works reliably
 * across all Android GPU/software canvases without BlendMode.Overlay compatibility crashes.
 */
@Composable
fun FilmGrainOverlay(
    alpha: Float = 0.06f,
    modifier: Modifier = Modifier
) {
    val noiseBrush = remember {
        val size = 96
        val pixels = IntArray(size * size)
        var seed = 0x5F3759DF
        for (i in pixels.indices) {
            seed = seed * 1664525 + 1013904223
            val raw = (seed ushr 24) and 0xFF
            val isLight = raw >= 128
            val dist = if (isLight) raw - 128 else 128 - raw
            val pxAlpha = (dist * 2).coerceIn(0, 255)
            val lum = if (isLight) 248 else 8
            pixels[i] = android.graphics.Color.argb(pxAlpha, lum, lum, lum)
        }
        val bmp = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
        ShaderBrush(ImageShader(bmp, TileMode.Repeated, TileMode.Repeated))
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (size.width <= 1f || size.height <= 1f) return@Canvas
        drawRect(
            brush = noiseBrush,
            alpha = alpha.coerceIn(0f, 0.2f)
        )
    }
}

/**
 * Shared LoR Dark & Metallic Obsidian-Navy gradient backdrop with carved stone texture,
 * subtle corner glow & film grain so non-combat screens share the same tactile richness as Combat.
 */
@Composable
fun LorScreenBackdrop(
    modifier: Modifier = Modifier,
    accentGlow: Color = ManaBlue,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_board_stone_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.24f,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xD80E182C),
                            Color(0xEA0A111F),
                            Color(0xF5070C17)
                        )
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accentGlow.copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(w * 0.14f, h * 0.10f),
                    radius = w * 0.70f
                ),
                radius = w * 0.70f,
                center = Offset(w * 0.14f, h * 0.10f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(HextechGold.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.88f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.88f, h * 0.88f)
            )
        }
        content()
        FilmGrainOverlay(alpha = 0.045f, modifier = Modifier.fillMaxSize())
    }
}

/**
 * Full-bleed LoR Dark & Metallic Obsidian Stone Battlefield (#0A111F) with textured stone slabs,
 * arcane cyan corner glows, luminous frosted ice fissures, corner filigree brackets,
 * bottom-up mist lighting, and glowing #D4AF37->#FFD700 gold centerline.
 */
@Composable
fun FreljordBattlefieldBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_board_arena_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 1f,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle outer perimeter vignette for HUD and card readability without obscuring the arena art
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 1f || h <= 1f) return@Canvas
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x16050912),
                        Color(0x4604070E)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.78f
                )
            )
        }
    }
}

/**
 * Faceted #FF4500 Crimson (Enemy) or #00BFFF Sapphire (Player) Nexus crystal inside an ornate #D4AF37->#FFD700 gold bezel
 * with attached HP nameplate displaying e.g. 16/20 or 45/45.
 */
@Composable
fun FacetedNexusCrystal(
    hp: Int,
    maxHp: Int,
    isEnemy: Boolean,
    hitDelta: Int,
    crystalSize: Dp,
    hpFontSize: TextUnit,
    hpTestTag: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val primaryGem = if (isEnemy) Color(0xFFFF4500) else Color(0xFF00BFFF)
    val brightGem = if (isEnemy) Color(0xFFFF8C5A) else Color(0xFF7BE4FF)
    val deepGem = if (isEnemy) Color(0xFF8C1B00) else Color(0xFF0A4F8C)
    val glowColor = if (isEnemy) Color(0x66FF4500) else Color(0x6600BFFF)

    Box(
        modifier = modifier
            .then(if (hpTestTag != null) Modifier.testTag(hpTestTag) else Modifier)
            .then(
                if (onClick != null) Modifier
                    .minimumInteractiveComponentSize()
                    .clickable(onClick = onClick)
                else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((-6).dp)
        ) {
            // Faceted Gem inside Ornate Gold Bezel
            Box(
                modifier = Modifier.size(crystalSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (size.width <= 1f || size.height <= 1f) return@Canvas
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = minOf(cx, cy)

                    // Outer glow on damage or steady aura
                    drawCircle(
                        color = if (hitDelta > 0) CrimsonNexus.copy(alpha = 0.80f) else glowColor,
                        radius = r * 1.05f,
                        center = Offset(cx, cy)
                    )

                    // Outer carved dark iron & gold bezel ring
                    drawCircle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFF6D77B), Color(0xFFB5862A), Color(0xFF543C11))
                        ),
                        radius = r * 0.92f,
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color(0xFF28354D),
                        radius = r * 0.78f,
                        center = Offset(cx, cy)
                    )

                    // Faceted octagonal/hexagonal crystal gem
                    val gemR = r * 0.74f
                    val points = List(6) { i ->
                        val angle = Math.toRadians((i * 60 - 90).toDouble())
                        Offset(
                            cx + (gemR * cos(angle)).toFloat(),
                            cy + (gemR * sin(angle)).toFloat()
                        )
                    }
                    val gemPath = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                        close()
                    }
                    drawPath(
                        path = gemPath,
                        brush = Brush.linearGradient(
                            colors = listOf(brightGem, primaryGem, deepGem),
                            start = Offset(cx - gemR, cy - gemR),
                            end = Offset(cx + gemR, cy + gemR)
                        )
                    )

                    // Top-left bright facet highlight
                    val topFacet = Path().apply {
                        moveTo(cx, cy)
                        lineTo(points[0].x, points[0].y)
                        lineTo(points[5].x, points[5].y)
                        close()
                    }
                    drawPath(path = topFacet, color = Color.White.copy(alpha = 0.36f))

                    // Bottom-right deep shadow facet
                    val bottomFacet = Path().apply {
                        moveTo(cx, cy)
                        lineTo(points[2].x, points[2].y)
                        lineTo(points[3].x, points[3].y)
                        close()
                    }
                    drawPath(path = bottomFacet, color = Color.Black.copy(alpha = 0.32f))

                    // Inner crown facet lines
                    val innerR = gemR * 0.44f
                    val innerPts = List(6) { i ->
                        val angle = Math.toRadians((i * 60 - 90).toDouble())
                        Offset(cx + (innerR * cos(angle)).toFloat(), cy + (innerR * sin(angle)).toFloat())
                    }
                    val innerPath = Path().apply {
                        moveTo(innerPts[0].x, innerPts[0].y)
                        for (i in 1 until innerPts.size) lineTo(innerPts[i].x, innerPts[i].y)
                        close()
                    }
                    drawPath(path = innerPath, color = brightGem.copy(alpha = 0.45f))
                    drawPath(path = innerPath, color = Color.White.copy(alpha = 0.45f), style = Stroke(1.2f))

                    for (i in 0 until 6) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.30f),
                            start = innerPts[i],
                            end = points[i],
                            strokeWidth = 1.2f
                        )
                    }

                    // Gold bezel rim stroke
                    drawCircle(
                        color = Color(0xFFF3CE6B),
                        radius = r * 0.90f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.2f)
                    )
                }
            }

            // Attached Dark-Obsidian & Gold HP Banner Plate
            Surface(
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 3.dp, bottomStart = 3.dp),
                color = Color(0xF00A111F),
                border = BorderStroke(1.4.dp, if (hitDelta > 0) CrimsonNexus else HextechGold),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = "$hp/$maxHp",
                    style = tabularStatStyle(
                        fontSize = hpFontSize,
                        color = if (hitDelta > 0) CrimsonNexus else ParchmentWhite
                    ),
                    maxLines = 1,
                    modifier = Modifier.padding(start = 8.dp, end = 7.dp, top = 2.dp, bottom = 2.dp)
                )
            }
        }

        // Hit delta popup badge
        AnimatedVisibility(
            visible = hitDelta > 0,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrimsonNexus,
                border = BorderStroke(1.dp, ParchmentWhite)
            ) {
                Text(
                    text = "-$hitDelta",
                    style = tabularStatStyle((hpFontSize.value * 0.78f).coerceAtLeast(10f).sp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Anchors live HP plaques and interactive Nexus hitboxes directly over the Enemy (top-left red gem)
 * and Player (bottom-left blue gem) Nexuses painted inside R.drawable.img_board_arena_bg (1076x614),
 * replacing the baked "20" numbers cleanly using exact ContentScale.Crop geometry.
 */
@Composable
fun ArenaBoardNexusOverlay(
    enemyHp: Int,
    enemyMaxHp: Int,
    enemyHitDelta: Int,
    playerHp: Int,
    playerMaxHp: Int,
    playerHitDelta: Int,
    onEnemyNexusBoundsChanged: (Rect) -> Unit,
    onEnemyNexusClick: () -> Unit,
    onPlayerNexusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerW = maxWidth
        val containerH = maxHeight
        if (containerW <= 1.dp || containerH <= 1.dp) return@BoxWithConstraints

        val imgW = 1076f
        val imgH = 614f
        val scaleDp = maxOf(containerW.value / imgW, containerH.value / imgH)
        val drawnW = (imgW * scaleDp).dp
        val drawnH = (imgH * scaleDp).dp
        val offsetX = (containerW - drawnW) / 2f
        val offsetY = (containerH - drawnH) / 2f

        // Enemy Nexus in img_board_arena_bg: crystal+plaque ~ x=[100..211], y=[200..260]; "20" box ~ x=[161..210], y=[213..249]
        ArenaSingleNexusOverlay(
            hp = enemyHp,
            maxHp = enemyMaxHp,
            isEnemy = true,
            hitDelta = enemyHitDelta,
            scaleDp = scaleDp,
            imgOffsetX = offsetX,
            imgOffsetY = offsetY,
            containerW = containerW,
            containerH = containerH,
            plaqueImgLeft = 161f,
            plaqueImgTop = 213f,
            plaqueImgWidth = 49f,
            plaqueImgHeight = 36f,
            nexusImgLeft = 100f,
            nexusImgTop = 200f,
            nexusImgWidth = 112f,
            nexusImgHeight = 60f,
            hpTestTag = "enemy_nexus_hp",
            onBoundsChanged = onEnemyNexusBoundsChanged,
            onClick = onEnemyNexusClick
        )

        // Player Nexus in img_board_arena_bg: crystal+plaque ~ x=[100..211], y=[332..392]; "20" box ~ x=[161..210], y=[343..379]
        ArenaSingleNexusOverlay(
            hp = playerHp,
            maxHp = playerMaxHp,
            isEnemy = false,
            hitDelta = playerHitDelta,
            scaleDp = scaleDp,
            imgOffsetX = offsetX,
            imgOffsetY = offsetY,
            containerW = containerW,
            containerH = containerH,
            plaqueImgLeft = 161f,
            plaqueImgTop = 343f,
            plaqueImgWidth = 49f,
            plaqueImgHeight = 36f,
            nexusImgLeft = 100f,
            nexusImgTop = 332f,
            nexusImgWidth = 112f,
            nexusImgHeight = 60f,
            hpTestTag = "player_nexus_hp",
            onBoundsChanged = null,
            onClick = onPlayerNexusClick
        )
    }
}

@Composable
private fun ArenaSingleNexusOverlay(
    hp: Int,
    maxHp: Int,
    isEnemy: Boolean,
    hitDelta: Int,
    scaleDp: Float,
    imgOffsetX: Dp,
    imgOffsetY: Dp,
    containerW: Dp,
    containerH: Dp,
    plaqueImgLeft: Float,
    plaqueImgTop: Float,
    plaqueImgWidth: Float,
    plaqueImgHeight: Float,
    nexusImgLeft: Float,
    nexusImgTop: Float,
    nexusImgWidth: Float,
    nexusImgHeight: Float,
    hpTestTag: String,
    onBoundsChanged: ((Rect) -> Unit)?,
    onClick: () -> Unit
) {
    val effectivePlaqueImgWidth = if (hp >= 100) maxOf(plaqueImgWidth, 58f) else plaqueImgWidth
    val hitW = (maxOf(nexusImgWidth, (plaqueImgLeft - nexusImgLeft) + effectivePlaqueImgWidth) * scaleDp).dp.coerceAtLeast(64.dp)
    val hitH = (nexusImgHeight * scaleDp).dp.coerceAtLeast(48.dp)
    val hitLeft = (imgOffsetX + (nexusImgLeft * scaleDp).dp).coerceIn(0.dp, (containerW - hitW).coerceAtLeast(0.dp))
    val hitTop = (imgOffsetY + (nexusImgTop * scaleDp).dp).coerceIn(0.dp, (containerH - hitH).coerceAtLeast(0.dp))

    val plaqueW = (effectivePlaqueImgWidth * scaleDp).dp.coerceAtLeast(32.dp)
    val plaqueH = (plaqueImgHeight * scaleDp).dp.coerceAtLeast(22.dp)
    val plaqueRelX = ((imgOffsetX + (plaqueImgLeft * scaleDp).dp) - hitLeft).coerceAtLeast(0.dp)
    val plaqueRelY = ((imgOffsetY + (plaqueImgTop * scaleDp).dp) - hitTop).coerceAtLeast(0.dp)
    val baseFontSp = (plaqueH.value * 0.54f).coerceIn(11.5f, 19f)
    val hpFontSp = (if (hp >= 100) baseFontSp * 0.80f else baseFontSp).sp

    val descLabel = if (isEnemy) {
        "Nhà Chính Địch: $hp/$maxHp máu"
    } else {
        "Nhà Chính Của Bạn: $hp/$maxHp máu"
    }

    Box(
        modifier = Modifier
            .offset(x = hitLeft, y = hitTop)
            .size(width = hitW, height = hitH)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .minimumInteractiveComponentSize()
                .then(
                    if (onBoundsChanged != null) {
                        Modifier.onGloballyPositioned { onBoundsChanged(it.boundsInRoot()) }
                    } else {
                        Modifier
                    }
                )
                .testTag(hpTestTag)
                .semantics { contentDescription = descLabel }
                .clickable(onClick = onClick)
        ) {
            // Subtle damage pulse aura over the painted Nexus gem when taking damage
            if (hitDelta > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(hitH * 0.92f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CrimsonNexus.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                )
            }

            // Opaque carved plaque covering the baked "20" inside the arena image and displaying live HP
            Box(
                modifier = Modifier
                    .offset(x = plaqueRelX, y = plaqueRelY)
                    .size(width = plaqueW, height = plaqueH)
                    .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp, topStart = 3.dp, bottomStart = 3.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = if (hitDelta > 0) {
                                listOf(Color(0xFF4C0D16), Color(0xFF2B070C))
                            } else {
                                listOf(Color(0xFF1A1E2A), Color(0xFF0E1118))
                            }
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        color = when {
                            hitDelta > 0 -> CrimsonNexus
                            isEnemy -> Color(0xFFB58638)
                            else -> Color(0xFFC49742)
                        },
                        shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp, topStart = 3.dp, bottomStart = 3.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$hp",
                    style = tabularStatStyle(
                        fontSize = hpFontSp,
                        color = if (hitDelta > 0) Color(0xFFFCA5A5) else ParchmentWhite
                    ),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Floating damage delta pill kept outside clickable semantics so hpTestTag always reports clean HP text
        AnimatedVisibility(
            visible = hitDelta > 0,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(220)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-6).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = CrimsonNexus,
                border = BorderStroke(1.dp, ParchmentWhite)
            ) {
                Text(
                    text = "-$hitDelta",
                    style = tabularStatStyle((hpFontSp.value * 0.78f).coerceAtLeast(10f).sp),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Square carved-gold scroll/history button placed on the left end of the golden centerline.
 */
@Composable
fun CenterlineScrollButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(),
        label = "scroll_btn_scale"
    )

    Surface(
        modifier = modifier
            .size(maxOf(42.dp, size))
            .minimumInteractiveComponentSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("combat_log_button")
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1F170A),
        border = BorderStroke(2.dp, HextechGoldBright),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF4F3B17), Color(0xFF23190B))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Nhật Ký Giao Tranh",
                tint = HextechGoldBright,
                modifier = Modifier.size(size * 0.56f)
            )
        }
    }
}

/**
 * Visual stacked deck pile beside the lower-left stone border with card count badge.
 */
@Composable
fun StackedDeckPile(
    cardCount: Int,
    deckWidth: Dp,
    deckHeight: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(width = deckWidth + 8.dp, height = deckHeight + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Bottom 2 stacked card backs for depth
        if (cardCount > 2) {
            Box(
                modifier = Modifier
                    .offset(x = (-3).dp, y = 3.dp)
                    .size(width = deckWidth, height = deckHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF0E1729))
                    .border(1.dp, HextechGoldDeep, RoundedCornerShape(5.dp))
            )
        }
        if (cardCount > 1) {
            Box(
                modifier = Modifier
                    .offset(x = (-1.5).dp, y = 1.5.dp)
                    .size(width = deckWidth, height = deckHeight)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF14223B))
                    .border(1.dp, HextechGold, RoundedCornerShape(5.dp))
            )
        }
        // Top card back with Metallic Runeterra Crest
        Box(
            modifier = Modifier
                .size(width = deckWidth, height = deckHeight)
                .clip(RoundedCornerShape(5.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1C3359), Color(0xFF0C172B))
                    )
                )
                .border(1.4.dp, HextechGold, RoundedCornerShape(5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                if (size.width <= 1f || size.height <= 1f) return@Canvas
                val cx = size.width / 2f
                val cy = size.height / 2f
                val diamond = Path().apply {
                    moveTo(cx, cy - size.height * 0.30f)
                    lineTo(cx + size.width * 0.28f, cy)
                    lineTo(cx, cy + size.height * 0.30f)
                    lineTo(cx - size.width * 0.28f, cy)
                    close()
                }
                drawPath(path = diamond, color = Color(0x4500BFFF))
                drawPath(path = diamond, color = HextechGold, style = Stroke(1.4f))
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xE80A111F),
                border = BorderStroke(0.9.dp, HextechGoldBright)
            ) {
                Text(
                    text = "$cardCount",
                    style = tabularStatStyle(fontSize, HextechGoldBright),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

data class ManaConversionVisual(
    val convertedToSpell: Int,
    val discardedExcess: Int,
    val previousRegularRemaining: Int
)

private val SpellVioletCore = Color(0xFFEDE9FE)
private val SpellVioletBright = Color(0xFFA78BFA)
private val SpellVioletDeep = Color(0xFF5B21B6)
private val SpellVioletBorder = Color(0xFFDDD6FE)
private val SpellVioletEmptyTop = Color(0xFF241B3E)
private val SpellVioletEmptyBot = Color(0xFF120C24)

/**
 * Iconic Right-Edge Circular Pass/Action & Mechanical Mana Dial matching LoR Path of Champions.
 * Displays Enemy Mana (e.g. 3/10) + Spell Mana sockets on the top arc,
 * Attack Token sword icon docked on the upper-left shoulder,
 * Player Mana (up to 12 with Champion milestones) + segmented glowing crystal bars + 3 violet Spell Mana gems on the bottom arc,
 * and the tactile circular PASS / ATTACK / BLOCK / OK button on the right centerline.
 */
@Composable
fun RightManaActionDial(
    combat: CombatState,
    dialWidth: Dp,
    dialHeight: Dp,
    actionDiameter: Dp,
    descFontSize: TextUnit,
    statFontSize: TextUnit,
    onEnterAttack: () -> Unit,
    onCancelAttack: () -> Unit,
    onCommitAttack: () -> Unit,
    onCommitBlock: () -> Unit,
    onPassTurn: () -> Unit,
    modifier: Modifier = Modifier,
    showBackdropHousing: Boolean = false,
    manaPreview: ManaPaymentPreview? = null,
    spentRegularIndices: IntRange = IntRange.EMPTY,
    spentSpellIndices: IntRange = IntRange.EMPTY,
    spendAnimProgress: Float = 0f,
    refillProgress: Float = 1f,
    conversionVisual: ManaConversionVisual? = null,
    onManaPlaqueClick: (() -> Unit)? = null
) {
    val isPlayerPriority = combat.priorityHolder == PriorityHolder.PLAYER
    val pulseTransition = rememberInfiniteTransition(label = "dial_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(950),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dial_btn_pulse"
    )
    val previewPulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mana_preview_pulse"
    )

    val (btnLabel, btnTag, btnAction) = when {
        !isPlayerPriority -> Triple(
            "ĐỊCH\nĐANG ĐI",
            when (combat.phase) {
                CombatPhase.COMBAT_DECLARE_ATTACK_PHASE -> "commit_attack_button"
                CombatPhase.COMBAT_DECLARE_BLOCK_PHASE -> "commit_block_button"
                else -> "end_round_button"
            },
            onPassTurn
        )
        combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE -> {
            if (combat.spellStack.isNotEmpty()) {
                Triple("DÙNG", "end_round_button", onPassTurn)
            } else {
                Triple(
                    if (combat.attackingUnitIds.isNotEmpty()) "TẤN CÔNG\n${combat.attackingUnitIds.size}" else "TẤN CÔNG",
                    "commit_attack_button",
                    onCommitAttack
                )
            }
        }
        combat.phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE -> Triple(
            when {
                combat.playerInitiatedCombat -> if (combat.blockCommitted) "XÁC NHẬN" else "CHỜ ĐỊCH"
                combat.blockPairings.isNotEmpty() -> "CHẶN\n${combat.blockPairings.size}"
                combat.spellStack.isNotEmpty() -> "DÙNG"
                else -> "BỎ QUA"
            },
            "commit_block_button",
            onCommitBlock
        )
        combat.phase == CombatPhase.SPELL_STACK_PHASE || combat.spellStack.isNotEmpty() -> Triple(
            "DÙNG",
            "end_round_button",
            onPassTurn
        )
        else -> Triple(
            if (combat.consecutivePasses >= 1) "HẾT VÒNG" else "BỎ QUA",
            "end_round_button",
            onPassTurn
        )
    }

    val btnEnabled = when {
        !isPlayerPriority -> false
        combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE && combat.spellStack.isEmpty() ->
            combat.attackingUnitIds.isNotEmpty()
        else -> true
    }

    val btnInteraction = remember { MutableInteractionSource() }
    val btnPressed by btnInteraction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (btnPressed) 0.93f else 1f,
        animationSpec = spring(),
        label = "dial_press"
    )

    val canInitiateAttack = combat.phase == CombatPhase.MAIN_ACTION_PHASE &&
        isPlayerPriority &&
        combat.playerHasAttackToken &&
        combat.spellStack.isEmpty() &&
        combat.playerBoard.any { it.canAttack }

    val plaqueWidth = (dialWidth - 46.dp).coerceIn(96.dp, 128.dp)
    val plaqueEndInset = 6.dp
    val crystalHeight = (dialHeight * 0.056f).coerceIn(7.dp, 10.dp)

    val enemyTotalSlots = combat.displayEnemyManaSlots
    val enemyCrystalGap = if (enemyTotalSlots > 10) 0.9.dp else 1.2.dp
    val enemyCrystalW = ((plaqueWidth - 46.dp) / enemyTotalSlots.toFloat() - enemyCrystalGap).coerceIn(2.1.dp, 4.2.dp)

    val playerTotalSlots = combat.displayRegularManaSlots
    val playerCrystalGap = if (playerTotalSlots > 10) 0.9.dp else 1.2.dp
    val playerCrystalW = ((plaqueWidth - 46.dp) / playerTotalSlots.toFloat() - playerCrystalGap).coerceIn(2.1.dp, 4.2.dp)

    Box(
        modifier = modifier.size(width = dialWidth, height = dialHeight),
        contentAlignment = Alignment.CenterEnd
    ) {
        if (showBackdropHousing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width <= 1f || size.height <= 1f) return@Canvas
                val btnRadius = actionDiameter.toPx() * 0.5f
                val center = Offset(size.width - btnRadius - 6.dp.toPx(), size.height * 0.5f)
                val outerR = minOf(size.height * 0.48f, btnRadius * 2.15f)
                val innerR = btnRadius * 0.96f
                val housingLeft = maxOf(26.dp.toPx(), center.x - outerR * 1.25f)
                val housingRight = minOf(size.width - 2.dp.toPx(), center.x + outerR * 0.35f)
                val housingPath = Path().apply {
                    addArc(
                        oval = androidx.compose.ui.geometry.Rect(
                            housingLeft,
                            center.y - outerR,
                            housingRight,
                            center.y + outerR
                        ),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 180f
                    )
                    lineTo(center.x, center.y - innerR)
                    lineTo(center.x, center.y + innerR)
                    close()
                }
                drawPath(
                    path = housingPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF131D30), Color(0xFF1E2D47), Color(0xFF0A111F))
                    )
                )
                drawPath(path = housingPath, color = HextechGold, style = Stroke(width = 2.dp.toPx()))
            }
        }

        // TOP ARC PLAQUE: Opaque carved dark-obsidian plate covering background enemy dial + up to 12 crystals + 3 violet spell gems
        val plaqueTapModifier = if (onManaPlaqueClick != null) {
            Modifier
                .pointerInput(onManaPlaqueClick) {
                    detectTapGestures { onManaPlaqueClick() }
                }
                .semantics(mergeDescendants = false) {
                    onClick(label = "Chi tiết Năng Lượng") {
                        onManaPlaqueClick()
                        true
                    }
                }
        } else Modifier

        val enemySpellMana = combat.enemySpellMana.coerceIn(0, 3)
        val enemyPlaqueShape = RoundedCornerShape(topStart = 10.dp, bottomStart = 8.dp, topEnd = 12.dp, bottomEnd = 4.dp)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = plaqueEndInset, top = 1.dp)
                .width(plaqueWidth)
                .clip(enemyPlaqueShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xF5192232), Color(0xF50B111B))
                    )
                )
                .border(1.2.dp, Color(0xFFC49742), enemyPlaqueShape)
                .then(plaqueTapModifier)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val enemySpellFont = (descFontSize.value * 0.78f).coerceIn(8.5f, 11f).sp
                Text(
                    text = "$enemySpellMana",
                    style = tabularStatStyle(
                        enemySpellFont,
                        if (enemySpellMana > 0) SpellVioletBright else MutedSilver
                    ),
                    lineHeight = enemySpellFont,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("enemy_spell_mana_text")
                )
                Text(
                    text = "/3",
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = (enemySpellFont.value * 0.78f).coerceAtLeast(7f).sp,
                    lineHeight = enemySpellFont,
                    color = MutedSilver.copy(alpha = 0.75f),
                    maxLines = 1,
                    softWrap = false
                )
                repeat(3) { idx ->
                    val filled = idx < enemySpellMana
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) {
                                    Brush.radialGradient(listOf(SpellVioletCore, SpellVioletBright, SpellVioletDeep))
                                } else {
                                    Brush.radialGradient(listOf(SpellVioletEmptyTop, SpellVioletEmptyBot))
                                }
                            )
                            .border(
                                1.dp,
                                if (filled) SpellVioletBorder else HextechGold.copy(alpha = 0.65f),
                                CircleShape
                            )
                            .testTag("enemy_spell_mana_gem_$idx")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val enemyManaFont = (statFontSize.value * 0.84f).coerceIn(10f, 13.5f).sp
                Text(
                    text = "${combat.enemyMana}/$enemyTotalSlots",
                    style = tabularStatStyle(
                        fontSize = enemyManaFont,
                        color = ParchmentWhite
                    ),
                    lineHeight = enemyManaFont,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("enemy_mana_text")
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(enemyCrystalGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val maxEnemy = maxOf(combat.enemyMaxMana, combat.enemyMana).coerceIn(1, enemyTotalSlots)
                    val currentEnemy = combat.enemyMana.coerceIn(0, maxEnemy)
                    repeat(enemyTotalSlots) { idx ->
                        val filled = idx < currentEnemy
                        val unlockedEmpty = idx in currentEnemy until maxEnemy
                        Box(
                            modifier = Modifier
                                .width(enemyCrystalW)
                                .height(crystalHeight)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    when {
                                        filled -> CrimsonNexus
                                        unlockedEmpty -> Color(0xFF281924)
                                        else -> Color(0xFF070C14)
                                    }
                                )
                                .border(
                                    0.7.dp,
                                    when {
                                        filled -> Color(0xFFFDA4AF)
                                        unlockedEmpty -> Color(0xFF6B3E4B)
                                        else -> Color(0xFF1F2B3D)
                                    },
                                    RoundedCornerShape(1.5.dp)
                                )
                        )
                    }
                }
            }
        }

        // SHOULDER: Cancel Attack X button (rendered alongside Attack Token without overlap)
        if (combat.phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE) {
            val cancelYOffset = if (combat.enemyHasAttackToken && !combat.playerHasAttackToken) {
                dialHeight * 0.27f
            } else {
                -dialHeight * 0.27f
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-44).dp, y = cancelYOffset)
                    .size(48.dp)
                    .testTag("cancel_attack_button")
                    .clickable(enabled = isPlayerPriority && combat.spellStack.isEmpty(), onClick = onCancelAttack),
                shape = CircleShape,
                color = Color(0xFF28181D),
                border = BorderStroke(1.8.dp, CrimsonNexus),
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hủy Tấn Công",
                        tint = ParchmentWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Glowing Orange-Gold Attack Token Sword Icon
        run {
            val isPlayerToken = combat.playerHasAttackToken
            val isEnemyToken = combat.enemyHasAttackToken
            val tokenScale = if (isPlayerToken || isEnemyToken) pulseScale else 1f
            val verticalOffset = when {
                isPlayerToken -> dialHeight * 0.26f
                isEnemyToken -> -dialHeight * 0.26f
                else -> 0.dp
            }
            val auraColor = when {
                isPlayerToken -> HextechGoldBright.copy(alpha = 0.55f)
                isEnemyToken -> CrimsonNexus.copy(alpha = 0.50f)
                else -> Color.Transparent
            }
            val emblemBg = when {
                isPlayerToken -> Brush.radialGradient(
                    colors = listOf(Color(0xFFFFF4B8), Color(0xFFF59E0B), Color(0xFF78350F))
                )
                isEnemyToken -> Brush.radialGradient(
                    colors = listOf(Color(0xFFFECDD3), CrimsonNexus, Color(0xFF7F1D1D))
                )
                else -> Brush.radialGradient(
                    colors = listOf(Color(0xFF334155), Color(0xFF1E293B), Color(0xFF0F172A))
                )
            }
            val borderStrokeColor = when {
                isPlayerToken -> Color(0xFFFFF7CC)
                isEnemyToken -> Color(0xFFFDA4AF)
                else -> Color(0xFF64748B)
            }
            val statusText = when {
                isPlayerToken -> "LƯỢT CÔNG"
                isEnemyToken -> "ĐỊCH CÔNG"
                else -> "HẾT CÔNG"
            }
            val statusColor = when {
                isPlayerToken -> HextechGoldBright
                isEnemyToken -> Color(0xFFFCA5A5)
                else -> Color(0xFF94A3B8)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-14).dp, y = verticalOffset)
                    .graphicsLayer {
                        scaleX = tokenScale
                        scaleY = tokenScale
                    }
                    .minimumInteractiveComponentSize()
                    .then(
                        if (canInitiateAttack) {
                            Modifier
                                .testTag("enter_attack_button")
                                .clickable(onClick = onEnterAttack)
                        } else {
                            Modifier
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlayerToken || isEnemyToken) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(auraColor, Color.Transparent)
                                    )
                                )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(emblemBg)
                            .border(2.2.dp, borderStrokeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlayerToken || isEnemyToken) {
                            Canvas(
                                modifier = Modifier
                                    .size(24.dp)
                                    .graphicsLayer {
                                        rotationZ = if (isPlayerToken) -35f else 145f
                                    }
                            ) {
                                val sw = size.width
                                val sh = size.height
                                val bladeColor = if (isPlayerToken) Color(0xFF1F1202) else ParchmentWhite
                                val hiltColor = if (isPlayerToken) Color(0xFF5C2D05) else Color(0xFFFDE68A)
                                val bladePath = Path().apply {
                                    moveTo(sw * 0.50f, sh * 0.06f)
                                    lineTo(sw * 0.62f, sh * 0.24f)
                                    lineTo(sw * 0.58f, sh * 0.68f)
                                    lineTo(sw * 0.42f, sh * 0.68f)
                                    lineTo(sw * 0.38f, sh * 0.24f)
                                    close()
                                }
                                drawPath(bladePath, bladeColor)
                                drawLine(
                                    color = if (isPlayerToken) Color(0xFFFFE082) else Color(0xFFFCA5A5),
                                    start = Offset(sw * 0.50f, sh * 0.16f),
                                    end = Offset(sw * 0.50f, sh * 0.62f),
                                    strokeWidth = sw * 0.06f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = hiltColor,
                                    start = Offset(sw * 0.22f, sh * 0.69f),
                                    end = Offset(sw * 0.78f, sh * 0.69f),
                                    strokeWidth = sw * 0.13f,
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = hiltColor,
                                    start = Offset(sw * 0.50f, sh * 0.70f),
                                    end = Offset(sw * 0.50f, sh * 0.89f),
                                    strokeWidth = sw * 0.11f,
                                    cap = StrokeCap.Round
                                )
                                drawCircle(
                                    color = bladeColor,
                                    radius = sw * 0.09f,
                                    center = Offset(sw * 0.50f, sh * 0.91f)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Hết Thẻ Tấn Công",
                                tint = MutedSilver,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xF00B1322),
                    border = BorderStroke(1.1.dp, borderStrokeColor.copy(alpha = 0.85f)),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = statusText,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        letterSpacing = 0.4.sp,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                    )
                }
            }
        }

        // BOTTOM ARC PLAQUE: Opaque obsidian + gold plate supporting up to 12 regular gems, Champion milestone bonus, and 3 violet Spell Mana gems
        val playerPlaqueShape = RoundedCornerShape(topStart = 8.dp, bottomStart = 10.dp, topEnd = 4.dp, bottomEnd = 12.dp)
        val spellMana = combat.playerSpellMana.coerceIn(0, 3)
        val currentPips = combat.playerMana.coerceIn(0, playerTotalSlots)
        val maxPips = maxOf(combat.playerMaxMana, currentPips).coerceIn(1, playerTotalSlots)
        val milestoneBonus = combat.championMilestoneManaBonus

        val previewInsufficient = manaPreview != null && !manaPreview.hasEnoughMana
        val previewRegularStart = if (manaPreview != null && manaPreview.hasEnoughMana && manaPreview.regularManaUsed > 0) {
            (currentPips - manaPreview.regularManaUsed).coerceAtLeast(0)
        } else Int.MAX_VALUE
        val previewSpellStart = if (manaPreview != null && manaPreview.hasEnoughMana && manaPreview.isSpellLike && manaPreview.spellManaUsed > 0) {
            (spellMana - manaPreview.spellManaUsed).coerceAtLeast(0)
        } else Int.MAX_VALUE

        val plaqueBorderColor = when {
            previewInsufficient -> CrimsonNexus.copy(alpha = previewPulseAlpha)
            conversionVisual != null && conversionVisual.convertedToSpell > 0 -> SpellVioletBright
            manaPreview != null -> HextechGoldBright.copy(alpha = previewPulseAlpha)
            spellMana > 0 -> ManaBlue.copy(alpha = 0.85f)
            else -> Color(0xFFC49742)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = plaqueEndInset, bottom = 1.dp)
                .width(plaqueWidth)
                .clip(playerPlaqueShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xF5192334), Color(0xF50B111C))
                    )
                )
                .border(1.3.dp, plaqueBorderColor, playerPlaqueShape)
                .testTag("player_mana_plaque")
                .then(plaqueTapModifier)
                .padding(horizontal = 5.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val playerManaFont = (statFontSize.value * 0.86f).coerceIn(10f, 13.5f).sp
                Text(
                    text = "${combat.playerMana}/$playerTotalSlots",
                    style = tabularStatStyle(
                        fontSize = playerManaFont,
                        color = if (previewInsufficient) Color(0xFFFCA5A5) else ParchmentWhite
                    ),
                    lineHeight = playerManaFont,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("player_mana_text")
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(playerCrystalGap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(playerTotalSlots) { idx ->
                        val isFilled = idx < currentPips
                        val isRefilledInFrame = isFilled && (
                            refillProgress >= 0.99f ||
                                (idx + 0.5f) / maxOf(1, currentPips).toFloat() <= refillProgress
                            )
                        val isPreviewRegularSpend = isFilled && idx in previewRegularStart until currentPips
                        val isSpentAnimating = idx in spentRegularIndices && spendAnimProgress > 0f
                        val isUnlockedSpent = idx in currentPips until maxPips
                        val isMilestoneSlot = idx < milestoneBonus && idx < maxPips

                        val crystalBg = when {
                            previewInsufficient && isFilled -> Brush.verticalGradient(
                                listOf(Color(0xFFFECACA), CrimsonNexus, Color(0xFF991B1B))
                            )
                            isPreviewRegularSpend -> Brush.verticalGradient(
                                listOf(Color(0xFFFEF9C3), HextechGoldBright, HextechGold)
                            )
                            isSpentAnimating -> Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFEF08A).copy(alpha = spendAnimProgress),
                                    Color(0xFF1F2D42)
                                )
                            )
                            isFilled && isRefilledInFrame -> Brush.verticalGradient(
                                listOf(Color(0xFF93E5FF), ManaBlue, Color(0xFF2563EB))
                            )
                            isFilled -> Brush.verticalGradient(
                                listOf(Color(0xFF345480), Color(0xFF1D3252))
                            )
                            isUnlockedSpent -> Brush.verticalGradient(
                                listOf(Color(0xFF1B273A), Color(0xFF0E1622))
                            )
                            else -> Brush.verticalGradient(
                                listOf(Color(0xFF070B12), Color(0xFF04060B))
                            )
                        }
                        val crystalBorder = when {
                            previewInsufficient && isFilled -> Color(0xFFFDA4AF)
                            isPreviewRegularSpend -> Color(0xFFFFFBEB)
                            isSpentAnimating -> HextechGoldBright.copy(alpha = spendAnimProgress)
                            isFilled && isRefilledInFrame -> if (isMilestoneSlot) HextechGoldBright else Color(0xFFE0F2FE)
                            isUnlockedSpent -> if (isMilestoneSlot) HextechGoldDeep else Color(0xFF425978)
                            else -> Color(0xFF1B2536)
                        }
                        Box(
                            modifier = Modifier
                                .width(playerCrystalW)
                                .height(crystalHeight + 0.5.dp)
                                .graphicsLayer {
                                    val sc = when {
                                        isPreviewRegularSpend -> 1.0f + 0.14f * previewPulseAlpha
                                        isSpentAnimating -> 0.84f + 0.22f * spendAnimProgress
                                        else -> 1f
                                    }
                                    scaleX = sc
                                    scaleY = sc
                                }
                                .clip(RoundedCornerShape(1.8.dp))
                                .background(crystalBg)
                                .border(0.75.dp, crystalBorder, RoundedCornerShape(1.8.dp))
                                .testTag("player_regular_mana_gem_$idx")
                        )
                    }
                }
            }

            // Bottom Meta Row: Turn Max + Milestone Bonus (left) & Recessed Purple/Violet Spell Mana Badge + 3 Gems (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val metaFont = (descFontSize.value * 0.70f).coerceIn(7.2f, 9.2f).sp
                val metaLabel = buildString {
                    append("T:$maxPips")
                    if (milestoneBonus > 0) append(" ★+$milestoneBonus")
                }
                Text(
                    text = metaLabel,
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = metaFont,
                    lineHeight = metaFont,
                    color = if (milestoneBonus > 0) HextechGoldBright else MutedSilver,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag("player_mana_turn_meta")
                )

                val spellBadgeBorder = when {
                    conversionVisual != null && conversionVisual.convertedToSpell > 0 -> SpellVioletCore
                    manaPreview != null && manaPreview.isSpellLike && manaPreview.spellManaUsed > 0 ->
                        HextechGoldBright.copy(alpha = previewPulseAlpha)
                    manaPreview != null && !manaPreview.isSpellLike ->
                        Color(0xFF475569).copy(alpha = 0.65f)
                    spellMana > 0 -> SpellVioletBright.copy(alpha = 0.90f)
                    else -> HextechGold.copy(alpha = 0.65f)
                }

                Surface(
                    modifier = Modifier
                        .alpha(if (manaPreview != null && !manaPreview.isSpellLike) 0.72f else 1f)
                        .testTag("player_spell_mana_badge"),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xEE110C24),
                    border = BorderStroke(1.dp, spellBadgeBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.5.dp, vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val spellFont = (descFontSize.value * 0.78f).coerceIn(8.5f, 10.5f).sp
                        Text(
                            text = "$spellMana",
                            style = tabularStatStyle(
                                spellFont,
                                if (spellMana > 0) SpellVioletBright else ParchmentWhite.copy(alpha = 0.80f)
                            ),
                            lineHeight = spellFont,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.testTag("player_spell_mana_text")
                        )
                        Text(
                            text = "/3",
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = (spellFont.value * 0.78f).coerceAtLeast(7f).sp,
                            lineHeight = spellFont,
                            color = if (spellMana > 0) SpellVioletBorder.copy(alpha = 0.82f) else MutedSilver.copy(alpha = 0.70f),
                            maxLines = 1,
                            softWrap = false
                        )
                        repeat(3) { idx ->
                            val filled = idx < spellMana
                            val isPreviewSpellSpend = filled && idx in previewSpellStart until spellMana
                            val isSpellSpentAnim = idx in spentSpellIndices && spendAnimProgress > 0f
                            val isNewlyConverted = conversionVisual != null &&
                                conversionVisual.convertedToSpell > 0 &&
                                idx in (spellMana - conversionVisual.convertedToSpell).coerceAtLeast(0) until spellMana

                            val gemBrush = when {
                                isPreviewSpellSpend -> Brush.radialGradient(
                                    listOf(Color(0xFFFAF5FF), Color(0xFFD8B4FE), HextechGold)
                                )
                                isSpellSpentAnim -> Brush.radialGradient(
                                    listOf(
                                        Color(0xFFE9D5FF).copy(alpha = spendAnimProgress),
                                        SpellVioletEmptyBot
                                    )
                                )
                                filled -> Brush.radialGradient(
                                    listOf(SpellVioletCore, SpellVioletBright, SpellVioletDeep)
                                )
                                else -> Brush.radialGradient(
                                    listOf(SpellVioletEmptyTop, SpellVioletEmptyBot)
                                )
                            }
                            val gemBorder = when {
                                isPreviewSpellSpend -> HextechGoldBright
                                isNewlyConverted -> Color(0xFFFAF5FF)
                                isSpellSpentAnim -> SpellVioletBright.copy(alpha = spendAnimProgress)
                                filled -> SpellVioletBorder
                                else -> HextechGold.copy(alpha = 0.65f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(7.5.dp)
                                    .graphicsLayer {
                                        val sc = when {
                                            isPreviewSpellSpend -> 1f + 0.18f * previewPulseAlpha
                                            isNewlyConverted -> 1.18f
                                            isSpellSpentAnim -> 0.80f + 0.28f * spendAnimProgress
                                            else -> 1f
                                        }
                                        scaleX = sc
                                        scaleY = sc
                                    }
                                    .clip(CircleShape)
                                    .background(gemBrush)
                                    .border(1.dp, gemBorder, CircleShape)
                                    .testTag("player_spell_mana_gem_$idx")
                            )
                        }
                    }
                }
            }
        }

        // Turn-End Conversion Floating Micro-Badge (non-blocking visual feedback)
        AnimatedVisibility(
            visible = conversionVisual != null,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(260)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-10).dp, y = (-2).dp)
        ) {
            conversionVisual?.let { conv ->
                val msg = when {
                    conv.convertedToSpell > 0 && conv.discardedExcess > 0 ->
                        "+${conv.convertedToSpell} NL Phép (Bỏ ${conv.discardedExcess})"
                    conv.convertedToSpell > 0 ->
                        "+${conv.convertedToSpell} NL Phép"
                    else ->
                        "Phép 3/3 • Bỏ dư ${conv.discardedExcess}"
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xF01C1238),
                    border = BorderStroke(1.dp, SpellVioletBright),
                    shadowElevation = 4.dp,
                    modifier = Modifier.testTag("mana_conversion_toast")
                ) {
                    Text(
                        text = msg,
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        lineHeight = 10.sp,
                        color = SpellVioletCore,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // RIGHT CENTERLINE: Prominent Burnished Gold/Brass Circular Pass/Action Button (#D4AF37 -> #FFD700)
        val buttonBg = if (btnEnabled) {
            Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA8A), HextechGoldBright, HextechGold, HextechGoldDeep),
                center = Offset(0.35f, 0.28f)
            )
        } else {
            Brush.radialGradient(
                colors = listOf(Color(0xFF2B384F), Color(0xFF1A2436), Color(0xFF0E1522))
            )
        }

        // Outer Golden Aura Halo around active End Turn / Pass dial CTA
        if (btnEnabled) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 2.dp)
                    .size(maxOf(48.dp, actionDiameter) + 12.dp)
            ) {
                if (size.width <= 1f || size.height <= 1f) return@Canvas
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            HextechGoldBright.copy(alpha = 0.45f),
                            HextechGold.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    )
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
                .graphicsLayer {
                    val activePulse = if (btnEnabled) pulseScale else 1f
                    scaleX = activePulse * pressScale
                    scaleY = activePulse * pressScale
                }
                .size(maxOf(48.dp, actionDiameter))
                .minimumInteractiveComponentSize()
                .testTag(btnTag)
                .clickable(
                    enabled = btnEnabled,
                    interactionSource = btnInteraction,
                    indication = null,
                    onClick = btnAction
                ),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = if (btnEnabled) 14.dp else 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(HextechGoldBright, HextechGold, Color(0xFF5A410D))
                        )
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(buttonBg)
                    .border(
                        width = 1.4.dp,
                        color = if (btnEnabled) Color(0xCCFFF8D6) else Color(0x558C9BB0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val labelFontFactor = if (!isPlayerPriority || btnLabel.length > 7) 0.72f else 0.90f
                Text(
                    text = btnLabel,
                    style = fantasyHeadingStyle(
                        fontSize = (descFontSize.value * labelFontFactor).coerceIn(7.5f, 12.5f).sp,
                        color = if (btnEnabled) DarkButtonInk else MutedSilver,
                        withShadow = !btnEnabled
                    ),
                    lineHeight = (descFontSize.value * (labelFontFactor + 0.14f)).sp,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
