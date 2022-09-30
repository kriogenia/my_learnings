package dev.sotoestevez.inventoryservice.domain

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp
import java.sql.Timestamp
import java.time.Instant
import java.util.*
import javax.persistence.*

@MappedSuperclass
open class BaseEntity {

    @field:Column(length = 36, columnDefinition = "varchar(36)", updatable = false, nullable = false)
    @field:Type(type = "org.hibernate.type.UUIDCharType")
    @field:GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @field:GeneratedValue(generator = "UUID")
    @field:Id
    var id: UUID? = null

    @field:Version
    var version: Long = 0

    @field:CreationTimestamp
    @Column(updatable = false)
    var createdDate: Timestamp = Timestamp.from(Instant.now())

    @field:UpdateTimestamp
    var lastModifiedDate: Timestamp = Timestamp.from(Instant.now())

    @Transient
    val isNew = this.id == null

}