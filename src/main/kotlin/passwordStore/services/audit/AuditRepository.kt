/*
 *
 * MIT License
 *
 * Copyright (c) 2020 Mirko Sciachero
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package passwordStore.services.audit


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import passwordStore.crypto.CryptExtension
import passwordStore.sql.saveOrUpdate
import passwordStore.utils.*
import javax.sql.DataSource

class AuditRepository(
    private val ds: DataSource,
    private val cryptExtension: CryptExtension
) {

    internal fun track(event: Event) {
        ds.saveOrUpdate(
            """ insert into services_hist
              (service, username, password, note, lastUpdate, userid, operation, operation_date, tags, url)
            values
              (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          """,
            event.service.service,
            event.service.username,
            event.service.password.crypt(),
            event.service.note.crypt(),
            event.service.updateTime.toTimestamp(timezone),
            event.service.userid,
            event.action.name,
            event.actionDate.toTimestamp(timezone),
            event.service.tags.joinToString(",") { it.asTitle().trim() },
            event.service.url
        )
    }

    private fun String.crypt() = cryptExtension.crypt(this)
}

class AuditEventDeque(
    private val repository: AuditRepository,
    private val eventBus: EventBus
) : EventListener<AuditMessage> {

    init {
        register()
    }

    fun register() {
        eventBus.subscribe(this)
    }

    override suspend fun onEvent(event: AuditMessage) {
        withContext(Dispatchers.IO) {
            launch {
                LOGGER.info { "Storing event $event" }
                repository.track(event.event)
            }
        }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }

}