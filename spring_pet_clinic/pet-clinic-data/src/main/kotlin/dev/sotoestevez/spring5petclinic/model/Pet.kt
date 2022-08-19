package dev.sotoestevez.spring5petclinic.model

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "pets")
class Pet(
	name: String,

	@ManyToOne
	@JoinColumn(name = "type_id")
	var petType: PetType,

	@Column(name = "birth_date")
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	val birthDate: LocalDate
) : NamedEntity(name) {

	@ManyToOne
	@JoinColumn(name = "owner_id")
	private var _owner: Owner? = null

	var owner: Owner
		get() = _owner ?: throw IllegalStateException("Pet owner is required")
		set(value) {
			_owner = value
		}

}

@Entity
@Table(name = "types")
class PetType(name: String) : NamedEntity(name)
