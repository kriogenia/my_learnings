package dev.sotoestevez.spring5petclinic.controllers

import dev.sotoestevez.spring5petclinic.services.VetService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/vets")
class VetController(
	private val service: VetService
) {

	companion object {
		private const val ROOT = "vet/"
		private const val INDEX = "index"
		private const val VETS = "vets"
	}

	@RequestMapping("", "/${INDEX}", "/${INDEX}.html")
	fun list(model: Model): String {
		model.addAttribute(VETS, service.findAll())
		return ROOT + INDEX
	}

}