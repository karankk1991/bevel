package com.template.flows.utility


class AccountAlreadyExistingException(private val name: String) : Exception("shilp; There is already an account registered with the specified name $name")