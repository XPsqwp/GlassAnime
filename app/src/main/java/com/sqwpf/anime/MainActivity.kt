package com.sqwpf.anime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { GlassAnimeApp() } }
    }
}

private data class TabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val TABS = listOf(
    TabItem("推荐", Icons.Filled.Home),
    TabItem("时间表", Icons.Filled.DateRange),
    TabItem("追番", Icons.Filled.Favorite),
    TabItem("我的", Icons.Filled.Settings),
)

private data class AnimeItem(val title: String, val sub: String, val hue: Float)

private val SAMPLE = listOf(
    AnimeItem("药屋少女的呢喃 第三季", "12 话 · 悬疑", 210f),
    AnimeItem("某暗部的少女共栖", "科幻 · TV · 日本", 260f),
    AnimeItem("装备我最强", "12 话 · TV", 300f),
    AnimeItem("萤火虫的出嫁", "12 话 · 恋爱", 30f),
    AnimeItem("冰之城墙 第二季", "TV · 日本", 200f),
    AnimeItem("尼古喵喵", "TV · 日本", 120f),
    AnimeItem("乱马1/2 第三季", "TV · 日本", 340f),
    AnimeItem("青之箱 第二季", "TV · 恋爱", 20f),
)

@Composable
fun GlassAnimeApp() {
    var index by remember { mutableIntStateOf(0) }
    val backdrop = rememberLayerBackdrop()
    val density = LocalDensity.current

    val barHeight = 64.dp
    val margin = 20.dp
    var barWidthPx by remember { mutableFloatStateOf(0f) }

    // 选中胶囊的位置（px）与按压进度
    val pillX = remember { Animatable(0f) }
    val press = remember { Animatable(0f) }

    val tabPx = if (barWidthPx > 0f) barWidthPx / TABS.size else 0f
    val scope = rememberCoroutineScope()

    LaunchedEffect(index, tabPx) {
        if (tabPx > 0f) {
            pillX.animateTo(
                targetValue = index * tabPx,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow)
            )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEAF4E6), Color(0xFFF6F1E4), Color(0xFFE8EEF8))
                )
            )
    ) {
        // ① 内容层：被 backdrop 捕获（玻璃采样的就是它）
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            when (index) {
                0 -> HomePage()
                1 -> PlaceholderPage("时间表", "按星期浏览当季新番")
                2 -> PlaceholderPage("追番", "你的收藏会出现在这里")
                else -> PlaceholderPage("我的", "设置 · 同步 · 关于")
            }
        }

        // ② 玻璃 Tab 栏
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = margin, end = margin, bottom = 16.dp)
                .fillMaxWidth()
                .height(barHeight)
                .onSizeChanged { barWidthPx = it.width.toFloat() }
                .pointerInput(tabPx) {
                    if (tabPx <= 0f) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { scope.launch { press.snapTo(1f) } },
                        onDragEnd = {
                            val target = (pillX.value / tabPx).roundToInt()
                                .coerceIn(0, TABS.size - 1)
                            index = target
                            scope.launch { press.animateTo(0f) }
                        },
                        onDragCancel = { scope.launch { press.animateTo(0f) } }
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            pillX.snapTo((pillX.value + dragAmount).coerceIn(0f, (TABS.size - 1) * tabPx))
                        }
                    }
                }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { RoundedCornerShape(percent = 50) },
                    effects = {
                        vibrancy()
                        blur(8.dp.toPx())
                        lens(24.dp.toPx(), 24.dp.toPx())
                    },
                    highlight = { Highlight.Default },
                    shadow = { Shadow(alpha = 0.55f) },
                    onDrawSurface = { drawRect(Color(0xFFFAFAFA).copy(alpha = 0.40f)) },
                    layerBlock = {
                        val p = press.value
                        val s = 1f + 16.dp.toPx() / size.width * p
                        scaleX = s
                        scaleY = s
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.fillMaxSize()) {
                if (tabPx > 0f) {
                    val pillW = with(density) { (tabPx - 8.dp.toPx()).toDp() }
                    val pillH = barHeight - 8.dp
                    // 选中透镜：和整条栏同一套玻璃管线（vibrancy + blur + lens），
                    // 再叠 Highlight / Shadow / InnerShadow，静态时就是一块玻璃，
                    // 拖动时 progress 把 lens 和色散加进来。
                    Box(
                        Modifier
                            .offset {
                                IntOffset(
                                    pillX.value.roundToInt(),
                                    4.dp.roundToPx()
                                )
                            }
                            .size(width = pillW, height = pillH)
                            .drawBackdrop(
                                backdrop = backdrop,
                                shape = { RoundedCornerShape(percent = 50) },
                                effects = {
                                    val pr = press.value
                                    vibrancy()
                                    blur(6.dp.toPx())
                                    lens(
                                        10.dp.toPx() * pr,
                                        14.dp.toPx() * pr,
                                        chromaticAberration = pr > 0.05f
                                    )
                                },
                                highlight = {
                                    Highlight.Default.copy(alpha = 0.45f + 0.55f * press.value)
                                },
                                shadow = {
                                    Shadow(
                                        radius = 10.dp,
                                        alpha = 0.35f + 0.5f * press.value
                                    )
                                },
                                innerShadow = {
                                    InnerShadow(radius = 6.dp, alpha = 0.55f)
                                },
                                onDrawSurface = {
                                    drawRect(Color.White.copy(alpha = 0.26f))
                                    drawRect(Color(0xFF0088FF).copy(alpha = 0.05f))
                                }
                            )
                    )
                }

                Row(Modifier.fillMaxSize()) {
                    TABS.forEachIndexed { i, tab ->
                        val selected = i == index
                        Box(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .pointerInput(i) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { },
                                        onDragEnd = { }
                                    ) { change, _ -> change.consume() }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { index = i }
                            ) {
                                Spacer(Modifier.height(10.dp))
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (selected) Color(0xFF0B1B2B) else Color(0xFF5B6470),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) Color(0xFF0B1B2B) else Color(0xFF5B6470)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePage() {
    Column(Modifier.fillMaxSize()) {
        Text(
            "热门番组",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF12261A),
            modifier = Modifier.padding(start = 20.dp, top = 56.dp, bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(SAMPLE) { item ->
                Column {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.7f)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color.hsl(item.hue % 360f, 0.55f, 0.72f),
                                        Color.hsl((item.hue + 30f) % 360f, 0.5f, 0.55f)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        color = Color(0xFF1B2B22),
                        maxLines = 2
                    )
                }
            }
            item { Spacer(Modifier.height(110.dp)) }
        }
    }
}

@Composable
private fun PlaceholderPage(title: String, sub: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF12261A))
            Spacer(Modifier.height(6.dp))
            Text(sub, fontSize = 13.sp, color = Color(0xFF4A5B50))
        }
    }
}
