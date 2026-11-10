package com.restrusher.partypuzl.data.packBank

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** The last downloaded `packs.json` exactly as served, with the ETag it came with. */
data class CachedPackBank(val body: String, val etag: String?)

/**
 * Keeps the last good download in app storage. Each file is written to a temp file and renamed
 * into place, so a crash mid-write leaves the previous copy intact rather than a truncated one.
 */
class PackBankCache(private val directory: File) {

    @Inject
    constructor(@ApplicationContext context: Context) : this(File(context.filesDir, DIRECTORY))

    suspend fun read(): CachedPackBank? = withContext(Dispatchers.IO) {
        runCatching { readFiles() }.getOrNull()
    }

    suspend fun write(cached: CachedPackBank) = withContext(Dispatchers.IO) {
        runCatching {
            directory.mkdirs()
            writeAtomically(bodyFile(), cached.body)
            cached.etag?.let { writeAtomically(etagFile(), it) } ?: etagFile().delete()
        }
    }

    private fun readFiles(): CachedPackBank? {
        val body = bodyFile().takeIf(File::exists)?.readText() ?: return null
        return CachedPackBank(body, etagFile().takeIf(File::exists)?.readText())
    }

    private fun writeAtomically(target: File, text: String) {
        val temp = File(directory, "${target.name}.tmp")
        temp.writeText(text)
        check(temp.renameTo(target))
    }

    private fun bodyFile() = File(directory, BODY_FILE)

    private fun etagFile() = File(directory, ETAG_FILE)

    private companion object {
        const val DIRECTORY = "pack_bank"
        const val BODY_FILE = "packs_v1.json"
        const val ETAG_FILE = "packs_v1.etag"
    }
}
