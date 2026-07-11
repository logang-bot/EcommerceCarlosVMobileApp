package com.restrusher.ecomercecarlosv.ui.screen.perfil

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.restrusher.ecomercecarlosv.R
import com.restrusher.ecomercecarlosv.ui.common.PedidosTopBar

private data class HelpTopicRes(val titleRes: Int, val bodyRes: Int)

private val helpTopics = listOf(
    HelpTopicRes(R.string.help_topic_inicio_title, R.string.help_topic_inicio_body),
    HelpTopicRes(R.string.help_topic_mercados_title, R.string.help_topic_mercados_body),
    HelpTopicRes(R.string.help_topic_clientes_title, R.string.help_topic_clientes_body),
    HelpTopicRes(R.string.help_topic_pedidos_title, R.string.help_topic_pedidos_body),
    HelpTopicRes(R.string.help_topic_productos_title, R.string.help_topic_productos_body),
    HelpTopicRes(R.string.help_topic_lista_negra_title, R.string.help_topic_lista_negra_body),
    HelpTopicRes(R.string.help_topic_reportes_title, R.string.help_topic_reportes_body),
    HelpTopicRes(R.string.help_topic_sincronizacion_title, R.string.help_topic_sincronizacion_body),
    HelpTopicRes(R.string.help_topic_seguridad_title, R.string.help_topic_seguridad_body),
    HelpTopicRes(R.string.help_topic_equipo_title, R.string.help_topic_equipo_body),
)

@Composable
fun HelpScreen(navController: NavController) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            PedidosTopBar(
                title = stringResource(R.string.help_title),
                onBack = { navController.popBackStack() },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            items(helpTopics) { topic ->
                HelpTopicCard(
                    title = stringResource(topic.titleRes),
                    body = stringResource(topic.bodyRes),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}
