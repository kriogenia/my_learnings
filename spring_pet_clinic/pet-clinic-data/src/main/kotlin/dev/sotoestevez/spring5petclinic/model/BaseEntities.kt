package dev.sotoestevez.spring5petclinic.model

import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.NotEmpty

@MappedSuperclass
open class BaseEntity(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null
) : Serializable {

	val isNew: Boolean
		get() = this.id == null

}

@MappedSuperclass
open class NamedEntity(
	@Column(name = "name")
	var name: String? = null
) : BaseEntity() {

	override fun toString(): String = name ?: super.toString()

}

@MappedSuperclass
open class Person(
	@Column(name = "first_name")
	@NotEmpty
	var firstName: String = "",

	@Column(name = "last_name")
	@NotEmpty
	var lastName: String = ""
) : BaseEntity()