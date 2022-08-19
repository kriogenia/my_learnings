package dev.sotoestevez.spring5petclinic.services

import dev.sotoestevez.spring5petclinic.model.Pet
import dev.sotoestevez.spring5petclinic.model.PetType

interface PetService : CrudService<Pet, Long> {

	fun findPetTypes(): List<PetType>

}