package com.chema.isla.domain.policy

import com.chema.isla.domain.fakes.notification
import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.IslandSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultNotificationFilterTest {

    private val filter = DefaultNotificationFilter(ownPackageName = "com.chema.isla")
    private val settings = IslandSettings()

    @Test
    fun `regular notification is allowed`() {
        assertTrue(filter.isAllowed(notification(), settings))
    }

    @Test
    fun `ongoing, group summary and media notifications are rejected`() {
        assertFalse(filter.isAllowed(notification(isOngoing = true), settings))
        assertFalse(filter.isAllowed(notification(isGroupSummary = true), settings))
        assertFalse(filter.isAllowed(notification(isMediaStyle = true), settings))
    }

    @Test
    fun `own notifications and empty ones are rejected`() {
        assertFalse(filter.isAllowed(notification(packageName = "com.chema.isla"), settings))
        assertFalse(filter.isAllowed(notification(title = "", text = ""), settings))
    }

    @Test
    fun `blocklist mode hides selected apps`() {
        val blocked = settings.copy(selectedPackages = setOf("com.whatsapp"))
        assertFalse(filter.isAllowed(notification(packageName = "com.whatsapp"), blocked))
        assertTrue(filter.isAllowed(notification(packageName = "org.telegram"), blocked))
    }

    @Test
    fun `allowlist mode only shows selected apps`() {
        val allowed = settings.copy(
            appFilterMode = AppFilterMode.ONLY_SELECTED,
            selectedPackages = setOf("com.whatsapp"),
        )
        assertTrue(filter.isAllowed(notification(packageName = "com.whatsapp"), allowed))
        assertFalse(filter.isAllowed(notification(packageName = "org.telegram"), allowed))
    }
}
