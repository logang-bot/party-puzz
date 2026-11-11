package com.restrusher.partypuzl.data.licenses

import android.content.Context
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.entity.License
import com.mikepenz.aboutlibraries.util.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Reads the library list the AboutLibraries Gradle plugin generates at build time
 * (`res/raw/aboutlibraries.json`), so a new dependency shows up without anyone editing a list.
 */
class OpenSourceLicensesSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun load(): List<OpenSourceLibrary> = withContext(Dispatchers.IO) {
        Libs.Builder().withContext(context).build().libraries
            .map { it.toOpenSourceLibrary() }
            .sortedBy { it.name.lowercase() }
    }

    private fun Library.toOpenSourceLibrary() = OpenSourceLibrary(
        id = uniqueId,
        name = name,
        version = artifactVersion,
        licenseNames = licenses.joinToString { it.name },
        licenseText = licenses.joinToString(separator = "\n\n") { it.fullText() }
    )

    private fun License.fullText(): String =
        licenseContent?.takeIf { it.isNotBlank() } ?: listOfNotNull(name, url).joinToString("\n")
}
