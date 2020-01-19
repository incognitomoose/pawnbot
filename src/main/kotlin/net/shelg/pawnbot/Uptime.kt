package net.shelg.pawnbot

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class Uptime {
    val birthTimestamp = Instant.now()

    fun get(): Duration = Duration.between(birthTimestamp, Instant.now())
}