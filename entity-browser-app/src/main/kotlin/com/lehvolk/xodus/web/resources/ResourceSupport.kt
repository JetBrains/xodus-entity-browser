package com.lehvolk.xodus.web.resources

import com.lehvolk.xodus.web.DBSummary
import com.lehvolk.xodus.web.DatabaseNotFoundException
import com.lehvolk.xodus.web.db.Databases
import com.lehvolk.xodus.web.db.JobsService
import com.lehvolk.xodus.web.db.StoreService
import com.lehvolk.xodus.web.servicesOf
import spark.kotlin.RouteHandler

interface ResourceSupport {

    val RouteHandler.db: DBSummary
        get() {
            val uuid = request.params("uuid")
            return Databases.find(uuid)
        }

    val RouteHandler.jobsService: JobsService
        get() {
            val uuid = request.params("uuid")
            return servicesOf(uuid).jobsService
        }

    val RouteHandler.storeService: StoreService
        get() {
            val uuid = request.params("uuid")
            return servicesOf(uuid).storeService
        }

}