package edu.stanford.bdh.engagehf.modules.healthconnectonfhir

import androidx.health.connect.client.records.metadata.Metadata
import java.util.UUID

/**
 * Returns a manual entry metadata with a random uuid as id
 */
@Suppress("FunctionNaming")
fun Metadata(): Metadata = Metadata(id = UUID.randomUUID().toString())

/**
 * Returns a manual entry metadata with the supplied id
 */
@Suppress("FunctionNaming")
fun Metadata(id: String) = Metadata.manualEntryWithId(id = id)
