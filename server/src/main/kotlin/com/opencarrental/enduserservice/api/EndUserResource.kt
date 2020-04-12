package com.opencarrental.enduserservice.api

import org.springframework.hateoas.RepresentationModel
import java.time.LocalDateTime

class EndUserResource(val id: String, val firstName: String, val lastName: String,
                      val email: String, val verified: Boolean,
                      val registeredTime: LocalDateTime, val loggedInTime: LocalDateTime? = null) : RepresentationModel<EndUserResource>()