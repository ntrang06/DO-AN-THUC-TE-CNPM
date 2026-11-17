package com.example.dattcnpm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dattcnpm.R

@Composable
fun MMRABottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
) {
    val items = listOf(
        BottomNavItem("Trang chủ", "home", R.drawable.home),
        BottomNavItem("Thuốc", "medicine", R.drawable.ic_pill),
        BottomNavItem("Lịch uống", "schedule", R.drawable.ic_calendar),
        BottomNavItem("Quét thuốc", "scan", R.drawable.scan),
        BottomNavItem("Lịch sử", "history", R.drawable.history),
        BottomNavItem("Gia đình", "family", R.drawable.family),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (title, route, iconRes) ->
                val selected = currentRoute == route
                val contentColor = if (selected) Color(0xFF0F5145) else Color(0xFF64748B)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            color = if (selected) Color(0xFFE6F4EA) else Color.Transparent,
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onTabSelected(route) }
                        .padding(vertical = 8.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = title,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = title,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val iconRes: Int,
)
