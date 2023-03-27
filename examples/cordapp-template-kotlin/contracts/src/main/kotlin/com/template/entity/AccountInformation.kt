package com.template.entity

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
 class AccountInformation (
    val bankName: String="",
    val accountNumber: Int,
    val ifscCode: String = ""
 )