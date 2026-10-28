package com.chema.isla.data

import com.chema.isla.data.notification.InMemoryNotificationRepository
import com.chema.isla.domain.model.IslandNotification
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryNotificationRepositoryTest {

    private fun n(key: String, title: String = "t") = IslandNotification(key, "p", "App", title, "", 0)

    @Test
    fun `upsert replaces notifications with the same key`() {
        val repo = InMemoryNotificationRepository()
        repo.upsert(n("a", "uno"))
        repo.upsert(n("b"))
        repo.upsert(n("a", "dos"))
        assertEquals(listOf("b", "a"), repo.notifications.value.map { it.key })
        assertEquals("dos", repo.notifications.value.last().title)
    }

    @Test
    fun `silent re-post with the same content keeps the original time`() {
        val repo = InMemoryNotificationRepository()
        repo.upsert(n("a").copy(postedAtMillis = 100))
        repo.upsert(n("a").copy(postedAtMillis = 900))
        assertEquals(100L, repo.notifications.value.single().postedAtMillis)

        repo.upsert(n("a", title = "nuevo").copy(postedAtMillis = 1_000))
        assertEquals(1_000L, repo.notifications.value.single().postedAtMillis)
    }

    @Test
    fun `remove and replaceAll`() {
        val repo = InMemoryNotificationRepository()
        repo.replaceAll(listOf(n("a"), n("b"), n("a")))
        assertEquals(listOf("a", "b"), repo.notifications.value.map { it.key })
        repo.remove("a")
        assertEquals(listOf("b"), repo.notifications.value.map { it.key })
    }
}
