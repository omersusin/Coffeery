package co.coffeery.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.coffeery.app.R
import co.coffeery.app.ui.components.AppText
import co.coffeery.app.ui.components.LineIcon
import co.coffeery.app.ui.components.Glyph
import co.coffeery.app.ui.components.PrimaryButton
import co.coffeery.app.ui.components.SecondaryButton
import co.coffeery.app.ui.screens.root.AppViewModel
import co.coffeery.app.ui.theme.CoffeeTheme

data class OnboardingSlide(
    val glyph: Glyph,
    val titleRes: Int,
    val bodyRes: Int,
)

private val slides = listOf(
    OnboardingSlide(Glyph.CUP, R.string.onboarding_welcome, R.string.onboarding_welcome_desc),
    OnboardingSlide(Glyph.CONE, R.string.onboarding_brew, R.string.onboarding_brew_desc),
    OnboardingSlide(Glyph.BOOKMARK, R.string.onboarding_track, R.string.onboarding_track_desc),
    OnboardingSlide(Glyph.BOOK, R.string.onboarding_learn, R.string.onboarding_learn_desc),
)

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    val colors = CoffeeTheme.colors
    var page by remember { mutableIntStateOf(0) }
    val safePage = page.coerceIn(0, slides.lastIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = safePage,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(tween(300)) { direction * it / 2 } + fadeIn(tween(300)))
                    .togetherWith(slideOutHorizontally(tween(300)) { -direction * it / 2 } + fadeOut(tween(200)))
            },
            label = "onboardSlide",
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            val slide = slides[page]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            ) {
                Spacer(Modifier.weight(0.6f))
                LineIcon(slide.glyph, colors.accent, Modifier.size(80.dp))
                Spacer(Modifier.height(40.dp))
                AppText(stringResource(slide.titleRes),
                    style = CoffeeTheme.type.display, color = colors.textPrimary,
                    align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(20.dp))
                AppText(stringResource(slide.bodyRes),
                    style = CoffeeTheme.type.body, color = colors.textSecondary,
                    align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SecondaryButton(text = stringResource(R.string.onboard_skip)) { vm.completeOnboarding() }
            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(slides.size) { i ->
                    val dotColor by animateColorAsState(
                        targetValue = if (i == safePage) colors.accent else colors.outline,
                        animationSpec = tween(300),
                    )
                    Box(modifier = Modifier
                        .size(if (i == safePage) 10.dp else 8.dp)
                        .clip(CircleShape).background(dotColor))
                }
            }

            Spacer(Modifier.weight(1f))

            val isLast = safePage == slides.lastIndex
            if (isLast) {
                PrimaryButton(text = stringResource(R.string.onboarding_get_started)) { vm.completeOnboarding() }
            } else {
                PrimaryButton(text = stringResource(R.string.onboard_next)) {
                    page = (safePage + 1).coerceAtMost(slides.lastIndex)
                }
            }
        }
    }
}
