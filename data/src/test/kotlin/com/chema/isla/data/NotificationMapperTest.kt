package com.chema.isla.data

import com.chema.isla.data.notification.NotificationMapper
import com.chema.isla.data.notification.RawNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationMapperTest {

    private val raw = RawNotification(
        key = "0|com.whatsapp|1",
        packageName = "com.whatsapp",
        appLabel = "WhatsApp",
        title = "  Ana ",
        text = "Hola",
        bigText = null,
        postTime = 42L,
        isOngoing = false,
        isGroupSummary = false,
        template = null,
    )

    @Test
    fun `maps basic fields and trims text`() {
        val n = NotificationMapper.map(raw)
        assertEquals("0|com.whatsapp|1", n.key)
        assertEquals("WhatsApp", n.appLabel)
        assertEquals("Ana", n.title)
        assertEquals("Hola", n.text)
        assertEquals(42L, n.postedAtMillis)
        assertFalse(n.isMediaStyle)
    }

    @Test
    fun `prefers big text over short text`() {
        assertEquals("Mensaje largo", NotificationMapper.map(raw.copy(bigText = "Mensaje largo")).text)
    }

    @Test
    fun `falls back to package name when label is missing`() {
        assertEquals("com.whatsapp", NotificationMapper.map(raw.copy(appLabel = " ")).appLabel)
    }

    @Test
    fun `null texts become empty strings`() {
        val n = NotificationMapper.map(raw.copy(title = null, text = null))
        assertEquals("", n.title)
        assertEquals("", n.text)
    }

    @Test
    fun `detects media style templates`() {
        val n = NotificationMapper.map(raw.copy(template = "android.app.Notification\$MediaStyle"))
        assertTrue(n.isMediaStyle)
    }
}
