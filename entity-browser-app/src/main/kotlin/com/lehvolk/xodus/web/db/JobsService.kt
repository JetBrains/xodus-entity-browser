package com.lehvolk.xodus.web.db

import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStore
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


private val log = LoggerFactory.getLogger(JobsService::class.java)

class JobsService {

    private val pool = Executors.newSingleThreadExecutor()

    fun submit(job: Job) {
        log.info("submitting {} for execution", job)
        pool.submit(job)
    }

    fun shutdown() {
        log.info("shutdown jobs")
        pool.shutdown()
    }

}

abstract class Job : Runnable

abstract class BulkJob : Job() {

    var iteration = 1

    override fun run() {
        log.info("running {}", this)
        while (shouldContinue()) {
            log.info("iteration {} of ", iteration, this)
            newSubJob().run()
            iteration++
        }
    }

    abstract fun shouldContinue(): Boolean

    abstract fun newSubJob(): Job

}

abstract class EntityBulkJob(internal val store: PersistentEntityStore) : BulkJob() {

    open val bulkSize = 1000

    var size = 0L

    init {
        resolveEntities()
    }

    abstract fun getAffectedEntities(): EntityIterable

    private fun resolveEntities(): EntityIterable {
        return getAffectedEntities().apply {
            size = readonly(store) {
                size()
            }
        }
    }

    override fun shouldContinue(): Boolean {
        return size >= bulkSize
    }

    override fun newSubJob(): Job = newEntitySubJob(transactional(store) {
        resolveEntities().take(bulkSize)
    })

    abstract fun newEntitySubJob(entities: EntityIterable): Job


}