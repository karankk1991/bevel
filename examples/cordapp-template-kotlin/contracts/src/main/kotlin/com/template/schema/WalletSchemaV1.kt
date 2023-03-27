package com.template.schema

import com.template.entity.AccountInformation
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

object WalletSchema

object WalletSchemaV1 : MappedSchema(
        schemaFamily = WalletSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentWallet::class.java)) {

    override val migrationResource: String?
        get() = "wallet.changelog-master";

    @Entity
    @Table(name = "WALLET_STATE")
    class PersistentWallet(
            @Column(name = "walletAddress")
            @Type(type = "uuid-char")
            val walletAddress: UUID,

            @Column(name = "walletId")
            val walletId: String,

            @Column(name = "userName")
            var userName: String,

            @Column(name = "linkedAccounts")
            var linkedAccounts: String,

            @Column(name = "walletProvider")
            var walletProvider: String,

            @Column(name = "myAccountKey")
            var myAccountKey: String,

            @Column(name = "linear_id")
            @Type(type = "uuid-char")
            var linearId: UUID
    ): PersistentState() {
        // Default constructor required by hibernate.
        constructor(): this(UUID.randomUUID(),"", "", "","", "",UUID.randomUUID())
    }
}