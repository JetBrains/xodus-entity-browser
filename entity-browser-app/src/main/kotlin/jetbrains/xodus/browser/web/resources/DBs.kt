package jetbrains.xodus.browser.web.resources


import jetbrains.xodus.browser.web.DBSummary
import jetbrains.xodus.browser.web.Resource
import jetbrains.xodus.browser.web.db.DatabaseService
import jetbrains.xodus.browser.web.db.Databases
import jetbrains.xodus.browser.web.safeGet
import jetbrains.xodus.browser.web.safePost
import spark.kotlin.Http

class DBs : Resource {

    private val databaseService = DatabaseService()

    override fun registerRouting(http: Http) {
        http.service.path("/api/dbs") {
            http.safeGet {
                Databases.all().map { it.secureCopy() }
            }
            http.safePost<DBSummary> {
                databaseService.add(it).secureCopy()
            }
        }
    }

    private fun DBSummary.secureCopy(): DBSummary {
        return copy(encryptionKey = null, encryptionIV = null, encryptionProvider = null)
    }

}
