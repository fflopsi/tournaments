package me.frauenfelderflorian.tournamentscompose.common.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimation
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object TournamentList : Screen()

    @Serializable
    data object TournamentEditor : Screen()

    @Serializable
    data object TournamentViewer : Screen()

    @Serializable
    data class PlayerViewer(val player: String) : Screen()

    @Serializable
    data object GameEditor : Screen()

    @Serializable
    data object GameViewer : Screen()

    @Serializable
    data object PlayersEditor : Screen()

    @Serializable
    data object AppSettings : Screen()
}

val LocalComponentContext: ProvidableCompositionLocal<ComponentContext> =
    staticCompositionLocalOf { error("Root component context was not provided") }

@Composable
fun ProvideComponentContext(componentContext: ComponentContext, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalComponentContext provides componentContext, content = content)
}

@Composable
inline fun <reified C : Any> ChildStack(
    source: StackNavigation<C>,
    serializer: KSerializer<C>,
    noinline initialStack: () -> List<C>,
    modifier: Modifier = Modifier,
    handleBackButton: Boolean = false,
    animation: StackAnimation<C, ComponentContext>? = null,
    noinline content: @Composable (C) -> Unit,
) {
    val componentContext = LocalComponentContext.current
    Children(
        stack = remember {
            componentContext.childStack(
                source = source,
                serializer = serializer,
                initialStack = initialStack,
                handleBackButton = handleBackButton,
                childFactory = { _, childComponentContext -> childComponentContext },
            )
        },
        modifier = modifier,
        animation = animation,
    ) {
        ProvideComponentContext(it.instance) { content(it.configuration) }
    }
}
