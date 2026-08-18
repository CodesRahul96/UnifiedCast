package com.codesrahul.unifiedcast.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codesrahul.unifiedcast.models.TvAppItem
import com.codesrahul.unifiedcast.ui.theme.AccentCyan

@Composable
fun TvAppLauncherTab(
    appList: List<TvAppItem> = defaultTvAppsList,
    onLaunchApp: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "INSTANT TV APP LAUNCHER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = AccentCyan,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(appList) { app ->
                TvAppCard(app = app, onLaunchApp = onLaunchApp)
            }
        }
    }
}

@Composable
fun TvAppCard(
    app: TvAppItem,
    onLaunchApp: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onLaunchApp(app.packageName) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = app.backgroundColor.copy(alpha = 0.25f)),
        border = BorderStroke(1.dp, app.backgroundColor.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(app.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = app.name.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

val defaultTvAppsList = listOf(
    TvAppItem("YouTube", "com.google.android.youtube.tv", Color(0xFFFF0000)),
    TvAppItem("Netflix", "com.netflix.ninja", Color(0xFFE50914)),
    TvAppItem("Prime Video", "com.amazon.amazonvideo.livingroom", Color(0xFF00A8E1)),
    TvAppItem("Disney+", "com.disney.disneyplus", Color(0xFF113CCF)),
    TvAppItem("Spotify", "com.spotify.tv.android", Color(0xFF1DB954)),
    TvAppItem("Kodi", "org.xbmc.kodi", Color(0xFF17B2E7)),
    TvAppItem("Plex", "com.plexapp.android", Color(0xFFE5A00D)),
    TvAppItem("HBO Max", "com.hbo.hbonow", Color(0xFF5822B4)),
    TvAppItem("Apple TV", "com.apple.atve.amazon.appletv", Color(0xFF444444))
)
