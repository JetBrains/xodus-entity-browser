package jetbrains.xodus.browser.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

class RestApiTest : TestSupport() {

    // See DBSummary
    class MalformedDBSummary(
        var location: String,
        var key: String,
        @JsonProperty("opened")
        var isOpened: Boolean,
        var unknownProperty: String
    )

    interface DBsApiTest {

        @POST("api/dbs")
        fun new(@Body dbSummary: MalformedDBSummary): Call<DBSummary>
    }

    @Test
    fun `should be able to add new db with unknown json property `() {
        // Given
        val api = retrofit.create(DBsApiTest::class.java)
        val db = MalformedDBSummary(
            location = newLocation(),
            key = "malformed-summary",
            isOpened = true,
            unknownProperty = "unknown"
        )

        // When
        val response = api.new(db).execute()
        val body = response.body()!!

        // Then
        assertEquals(200, response.code())
        assertEquals(db.location, body.location)
        assertEquals(db.isOpened, body.isOpened)
        assertEquals(db.key, body.key)
    }
}