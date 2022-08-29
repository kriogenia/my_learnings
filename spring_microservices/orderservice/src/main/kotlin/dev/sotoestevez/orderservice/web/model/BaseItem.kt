package dev.sotoestevez.orderservice.web.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime
import java.util.*

sealed class BaseItem(
    @field:JsonProperty("id")
    val id: UUID? = null,

    @field:JsonProperty("version")
    val version: Long? = null,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    @field:JsonProperty("createdDate")
    val createdDate: OffsetDateTime? = null,

    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ", shape = JsonFormat.Shape.STRING)
    @field:JsonProperty("lastModifiedDate")
    val lastModifiedDate: OffsetDateTime? = null,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseItem

        if (id != other.id) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = (31 * result + (version ?: 0)).toInt()
        return result
    }
}