package jetbrains.xodus.browser.web.db


import jetbrains.exodus.entitystore.PersistentEntityStore
import mu.KLogging

class PersistentStoreService: AbstractStoreService {

    companion object : KLogging()

    override val store: PersistentEntityStore
    override val isReadonly: Boolean

    constructor(store: PersistentEntityStore, isReadonly: Boolean) {
        this.store = store
        this.isReadonly = isReadonly
    }

    override fun stop() {
        var proceed = true
        var count = 1
        while (proceed && count <= 10) {
            try {
                logger.info { "trying to close persistent store. attempt $count" }
                store.close()
                proceed = false
                logger.info("persistent store closed")
            } catch (e: RuntimeException) {
                logger.error(e) { "error closing persistent store" }
                count++
            }
        }
    }
}