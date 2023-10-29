package com.xbot.musifyze.data

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * There is no official way, so here's the workaround
 * @see <a href= "https://github.com/Kotlin/kotlinx.coroutines/issues/2631">https://github.com/Kotlin/kotlinx.coroutines/issues/2631</a>
 */
fun <T, R> StateFlow<T>.stateMap(transform: (T) -> R): StateFlow<R> {
    return object : StateFlow<R> {

        override val replayCache: List<R>
            get() = this@stateMap.replayCache.map { transform(it) }

        override val value: R
            get() = transform(this@stateMap.value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@stateMap.map { transform(it) }.collect(collector)
            error("StateFlow collection never ends.")
        }
    }
}