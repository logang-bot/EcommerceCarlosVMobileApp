package com.restrusher.ecomercecarlosv.ui.screen.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.theme.extendedColors

private data class NavTab(val icon: ImageVector, val labelRes: Int)

private val NAV_TABS = listOf(
    NavTab(Icons.Default.GridView, R.string.nav_mercados),
    NavTab(Icons.Default.Sell, R.string.nav_productos),
    NavTab(Icons.Default.Assessment, R.string.nav_reporte),
)

@Composable
fun AppBottomNavBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val ext = MaterialTheme.extendedColors
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        NAV_TABS.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.labelRes), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = ext.accentSoft,
                    unselectedIconColor = ext.text3,
                    unselectedTextColor = ext.text3,
                ),
            )
        }
    }
}
