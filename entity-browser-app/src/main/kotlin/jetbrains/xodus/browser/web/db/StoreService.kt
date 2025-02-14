package jetbrains.xodus.browser.web.db


import jetbrains.xodus.browser.web.*
import java.io.IOException
import java.io.InputStream

interface StoreService {

    val isReadonly: Boolean

    fun validate()

    fun stop()

    fun addType(type: String): Int

    fun allTypes(): Array<EntityType>

    fun searchType(typeId: Int, q: String?, offset: Int, pageSize: Int): SearchPager

    fun searchEntity(id: String, linkName: String, offset: Int, pageSize: Int): LinkPager

    fun newEntity(typeId: Int, vo: ChangeSummary): EntityView

    @Throws(IOException::class)
    fun getBlob(id: String, blobName: String): InputStream

    @Throws(IOException::class)
    fun getBlobString(id: String, blobName: String): String

    fun updateEntity(id: String, vo: ChangeSummary): EntityView

    fun getEntity(id: String): EntityView

    fun deleteEntity(id: String)

    fun deleteEntitiesJob(typeId: Int, term: String?): Job
}
