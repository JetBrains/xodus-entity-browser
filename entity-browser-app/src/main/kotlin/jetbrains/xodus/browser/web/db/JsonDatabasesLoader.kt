package jetbrains.xodus.browser.web.db

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jetbrains.xodus.browser.web.DBSummary
import mu.KLogging
import java.io.*
import java.nio.charset.StandardCharsets
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class JsonDatabasesLoader(private val location: String) {

    companion object : KLogging() {
        private const val DEFAULT_DBS_STORAGE = "databases.json.gz"
    }

    private val mapper: ObjectMapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerKotlinModule()

    private fun readJson(): Array<DBSummary> {
        return try {
            deserializeJson(File(location, DEFAULT_DBS_STORAGE).inputStream())
        } catch (_: FileNotFoundException) {
            emptyArray()
        } catch (e: Exception) {
            handleReadException(e)
            throw e
        }
    }

    private fun writeJson(json: Array<DBSummary>) {
        try {
            File(location, DEFAULT_DBS_STORAGE).outputStream().use { out ->
                serializeJson(json).copyTo(out)
            }
        } catch (e: Exception) {
            handleWriteException(e)
            throw e
        }
    }

    fun getAll() = readJson().toList()

    fun get(uuid: String) = readJson().find { it.uuid == uuid }

    fun update(summary: DBSummary): Boolean {
        val oldData = readJson()
        val dbNotFound = oldData.none { it.uuid == summary.uuid }
        if (dbNotFound) {
            return false
        }
        val newData = (oldData.filterNot { it.uuid == summary.uuid } + summary).toTypedArray()
        writeJson(newData)
        return true
    }

    fun add(summary: DBSummary): Boolean {
        val oldData = readJson()
        val alreadyExists = oldData.any { it.uuid == summary.uuid }
        if (alreadyExists) {
            return false
        }
        val newData = oldData + summary
        writeJson(newData)
        return true
    }

    fun delete(uuid: String): Boolean {
        val oldData = readJson()
        val dbNotFound = oldData.none { it.uuid == uuid }
        if (dbNotFound) {
            return false
        }
        val newData = oldData.filterNot { it.uuid == uuid }.toTypedArray()
        writeJson(newData)
        return true
    }


    private fun handleReadException(e: Exception) {
        when (e) {
            is IllegalStateException -> logger.warn {
                "Failed to deserialize db data: ${e.message}"
            }

            is JsonProcessingException -> logger.info {
                "Failed to deserialize db data due to obsolete data json structure: ${e.message}"
            }

            is ClassNotFoundException -> logger.info {
                "Failed to deserialize db data due to obsolete data classes: ${e.message}"
            }

            else -> logger.warn {
                "Failed to deserialize db data due to exception: ${e.message}"
            }
        }
    }

    private fun handleWriteException(e: Exception) {
        when (e) {
            is IOException -> logger.error(e) {
                "Failed to serialize db data due to I/O exception"
            }

            else -> logger.error(e) {
                "Failed to serialize db data due to exception"
            }
        }
    }

    private fun serializeJson(json: Array<DBSummary>): InputStream {
        val content = mapper.writeValueAsString(json)
        val gzipped = ByteArrayOutputStream(content.length).use { out ->
            GZIPOutputStream(out).use { gzip ->
                gzip.write(content.toByteArray(StandardCharsets.UTF_8))
            }
            out.toByteArray()
        }
        return ByteArrayInputStream(gzipped)
    }

    private fun deserializeJson(blob: InputStream): Array<DBSummary> {
        val out = ByteArrayOutputStream()
        GZIPInputStream(blob).use {
            it.copyTo(out)
        }
        val content = out.toString(StandardCharsets.UTF_8.name())
        return mapper.readValue(content, Array<DBSummary>::class.java)
    }
}