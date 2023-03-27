package com.template.flows.accounts

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.workflows.utilities.rowsToAmount
import com.r3.corda.lib.tokens.workflows.utilities.sumTokenCriteria
import com.template.flows.utility.AccountAlreadyExistingException
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.flows.StartableByService
import net.corda.core.identity.Party
import net.corda.core.identity.PartyAndCertificate
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.getOrThrow
import java.util.*

/**
*   CREATE NEW ACCOUNT
* **/
@StartableByRPC
@StartableByService
@InitiatingFlow
class CreateNewAccount(private val acctName: String) : FlowLogic<UniqueIdentifier>() {

    @Suspendable
    override fun call(): UniqueIdentifier {
        //Create a new account
        val newAccount = accountService.createAccount(name = acctName).toCompletableFuture().getOrThrow() ?: throw AccountAlreadyExistingException(acctName)
        val acct = newAccount.state.data
        return acct.identifier
    }
}

/**
 *   NEW KEY FOR ACCOUNT
 **/
@StartableByRPC
@StartableByService
class NewKeyForAccount(private val accountId: UUID) : FlowLogic<PartyAndCertificate>() {
    @Suspendable
    override fun call(): PartyAndCertificate {
        return serviceHub.keyManagementService.freshKeyAndCert(
                identity = ourIdentityAndCert,
                revocationEnabled = false,
                externalId = accountId
        )
    }
}

/**
 *   SHARE ACCOUNT INFO
 **/
@StartableByRPC
@StartableByService
@InitiatingFlow
class ShareAccountTo(
        private val acctNameShared: String,
        private val shareTo: Party
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {

        //Create a new account
        val AllmyAccounts = accountService.ourAccounts()
        val SharedAccount = AllmyAccounts.single { it.state.data.name == acctNameShared }.state.data.identifier.id
        accountService.shareAccountInfoWithParty(SharedAccount, shareTo)
//        accountService.shareAccountInfo()
        return "Shared " + acctNameShared + " with " + shareTo.name.organisation
    }
}

/**
 *   VIEW MY ACCOUNTS
 **/
@StartableByRPC
@StartableByService
@InitiatingFlow
class ViewMyAccounts() : FlowLogic<List<String>>() {

    @Suspendable
    override fun call(): List<String> {
        //Create a new account

        val myAccounts= accountService.ourAccounts().map { it.state.data.name }
        return myAccounts
    }
}
/**
 *   VIEW ACCOUNT BALANCE
 **/

@StartableByRPC
@StartableByService
@InitiatingFlow
class ViewBalanceForAccounts(
        val acctname : String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {

        val myAccount = accountService.accountInfo(acctname).single().state.data
        val criteria = QueryCriteria.VaultQueryCriteria(
                externalIds = listOf(myAccount.identifier.id)
        )
        val criteria1 = QueryCriteria.VaultQueryCriteria(
                externalIds = listOf(myAccount.identifier.id)
        ).and(sumTokenCriteria())

        val accountBalanceInfo1 = serviceHub.vaultService.queryBy(
                contractStateType = FungibleToken::class.java,
                criteria = criteria
        )
        val qCriteria = rowsToAmount(accountBalanceInfo1.states.last().state.data.tokenType, serviceHub.vaultService.queryBy<FungibleToken>(criteria1))

        return "Balance of $acctname : ${qCriteria.quantity}"
    }
}


@StartableByRPC
@StartableByService
@InitiatingFlow
class GetTxnHistory(
        val acctname : String
) : FlowLogic<List<StateAndRef<FungibleToken>>>() {

    @Suspendable
    override fun call(): List<StateAndRef<FungibleToken>> {

        val myAccount = accountService.accountInfo(acctname).single().state.data
        val criteria = QueryCriteria.VaultQueryCriteria(
                externalIds = listOf(myAccount.identifier.id)
        )
        val criteria1 = QueryCriteria.VaultQueryCriteria(
                externalIds = listOf(myAccount.identifier.id),
                status= Vault.StateStatus.UNCONSUMED
        )

        val accountBalanceInfo1 = serviceHub.vaultService.queryBy(
                contractStateType = FungibleToken::class.java,
                criteria = criteria
        )
        val test=serviceHub.vaultService.queryBy(FungibleToken::class.java,criteria1).states
//        val qCriteria= rowsToAmount(accountBalanceInfo1.states.last().state.data.tokenType, serviceHub.vaultService.queryBy<FungibleToken>(criteria1))
//
//        return "Balance of $acctname : ${qCriteria.quantity}"
        return test
    }
}