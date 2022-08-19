package dev.sotoestevez.spring5petclinic.services.map

import dev.sotoestevez.spring5petclinic.model.Owner
import dev.sotoestevez.spring5petclinic.services.OwnerService
import dev.sotoestevez.spring5petclinic.services.PetService
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class OwnerMapService(
	private val petService: PetService
) : AbstractMapService<Owner>(), OwnerService {

	override fun save(entity: Owner): Owner {
		entity.pets.forEach {
			petService.save(it)
		}
		return super.save(entity)
	}

	override fun findByLastName(lastName: String): List<Owner> =
		map.values.stream().filter { it.lastName == lastName }.collect(Collectors.toList())

}