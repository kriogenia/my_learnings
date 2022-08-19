package dev.sotoestevez.spring5petclinic.model

import javax.persistence.*
import javax.validation.constraints.Digits
import javax.validation.constraints.NotEmpty

@Entity
@Table(name = "owners")
class Owner(
	firstName: String,
	lastName: String,

	@Column(name = "address")
	@NotEmpty
	var address: String = "",

	@Column(name = "city")
	@NotEmpty
	var city: String = "",

	@Column(name = "telephone")
	@NotEmpty
	@Digits(fraction = 0, integer = 10)
	var telephone: String = ""
) : Person(firstName, lastName) {

	@OneToMany(cascade = [CascadeType.ALL], mappedBy = "_owner")
	private val _pets: MutableSet<Pet> = mutableSetOf()

	val pets: Set<Pet>
		get() = _pets

	fun addPet(pet: Pet) = _pets.add(pet.also { it.owner = this })

	val fullName = "$firstName $lastName"

}
