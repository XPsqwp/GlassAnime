package com.sqwpf.anime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

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

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFEAF4E6), Color(0xFFF6F1E4), Color(0xFFE8EEF8))
                )
            )
    ) {
        // 内容层被录进 backdrop，供玻璃采样
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

        // 原版 LiquidBottomTabs（直接来自 AndroidLiquidGlass 的 catalog 组件）
        LiquidBottomTabs(
            selectedTabIndex = { index },
            onTabSelected = { index = it },
            backdrop = backdrop,
            tabsCount = TABS.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            TABS.forEachIndexed { i, tab ->
                LiquidBottomTab({ index = i }) {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF12261A)
                    )
                    Text(tab.label, fontSize = 11.sp, color = Color(0xFF12261A))
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
