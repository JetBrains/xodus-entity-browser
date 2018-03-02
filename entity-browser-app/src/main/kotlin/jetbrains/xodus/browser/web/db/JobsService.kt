package jetbrains.xodus.browser.web.db

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStore
import mu.KLogging
import java.util.concurrent.Executors


class JobsService {

    companion object : KLogging()

    private val pool = Executors.newSingleThreadExecutor()

    fun submit(job: Job) {
        logger.info { "submitting $job for execution" }
        pool.submit(job)
    }

    fun stop() {
        logger.info("stop jobs")
        pool.shutdown()
    }

}

abstract class Job : Runnable

abstract class EntityBulkJob(private val store: PersistentEntityStore) : Job() {

    companion object : KLogging()

    open val bulkSize = 1000
    private var step = 1

    abstract val affectedEntities: EntityIterable

    abstract fun Entity.doAction()

    override fun run() {
        logger.info { "step $step of $this" }
        try {
            store.transactional {
                affectedEntities.take(bulkSize).asSequence().forEach { it.doAction() }
            }
        } catch (e: Exception) {
            logger.error(e) { "error executing $step of $this" }
        }
        step++
    }

}