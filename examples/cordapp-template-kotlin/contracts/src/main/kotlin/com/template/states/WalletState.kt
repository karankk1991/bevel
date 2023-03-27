package com.template.states

import com.template.contracts.CreateWalletAccountContract
import com.template.contracts.TemplateContract
import net.corda.core.contracts.BelongsToContract
import com.template.entity.AccountInformation
import com.template.schema.WalletSchemaV1
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(CreateWalletAccountContract::class)
data class WalletState(val walletAddress: UniqueIdentifier,//This is the accountIdentifier of the account created for the particular user
                       val walletId: String,
                       val userName:String,
                       val linkedAccounts: Map<String,UniqueIdentifier>,
                       val walletProvider: Party,
                       val myAccountKeys: List<AbstractParty>,
                       override val participants: List<AbstractParty> = listOf(walletProvider)+myAccountKeys,
                       override val linearId: UniqueIdentifier=UniqueIdentifier()
) : LinearState,QueryableState {
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is WalletSchemaV1 -> WalletSchemaV1.PersistentWallet(
                    this.walletAddress.id,
                    this.walletId,
                    this.userName,
                    this.linkedAccounts.toString(),
                    this.walletProvider.toString(),
                    this.myAccountKeys.toString(),
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(WalletSchemaV1)

//    fun linkBankAccount(bankDetails: MutableList<AccountInformation>) = copy(linkedAccounts = bankDetails)

}


