package com.machiav3lli.backup.ui.navigation

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SlidePager(
    modifier: Modifier = Modifier,
    pageItems: ImmutableList<NavItem>,
    pagerState: PagerState,
) {
    // @hg42x: it does not make sense, to look for slight optimizations but recompose
    // all pages on every state change (and other reactivity), so probably all the time
    // I actually see pauses (busy is off) because of this
    // breakpoints in backupItems are triggered a lot while none is visible
    // I tried beyondBoundsPageCount = 0 and I think it is fast enough,
    // if we would *really* want speed, I would remove animations
    // (also, users can set time to 0 or use the switch in accessibility settings)
    // another way would be using another kind of transition animation, e.g.
    // sliding away the current page but fading in the new page
    // that would give immediate feedback (slide)
    // but compose would have more time to render the page when fading it in
    // @machiav3lli: 0 causes crashes when ListDetail composables are included,
    // therefore it is now back to full pre-composition
    HorizontalPager(modifier = modifier, state = pagerState, beyondViewportPageCount = 3) { page ->
        pageItems[page].content()
    }
}