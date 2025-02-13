package jetbrains.xodus.browser.web.db

import com.jetbrains.youtrack.db.api.DatabaseType

data class EnvironmentParameters(
    var key: String,
    var location: String,
    var user: String = "admin",
    var password: String = "admin",
    var type: DatabaseType = DatabaseType.PLOCAL,
    var isReadonly: Boolean = false,
    var isEncrypted: Boolean = false,
    var encryptionKey: String? = null,
    var encryptionIV: String? = null,
    var withCloseDatabaseInDbProvider: Boolean = true
)