package com.template.flows.tokens

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import com.template.flows.accounts.ShareAccountTo
import net.corda.core.contracts.Amount
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap
import com.r3.corda.lib.tokens.contracts.types.IssuedTokenType
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy

// *********
// * Flows for issuing tokens for existing accounts *
// *********
@InitiatingFlow
@StartableByRPC
class IssueForAccount(private val amount: Long,
                      private val currency: String,
                      private val issuer: Party, private val holder:String) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {

        val counterpartySession = initiateFlow(issuer)

        counterpartySession.send(amount)
        counterpartySession.send(currency)
        counterpartySession.send(holder)
        subFlow(ShareAccountTo(holder,issuer))

        return counterpartySession.receive<String>().unwrap { it }
    }


}
@InitiatedBy(IssueForAccount::class)
class IssueForAccountResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {

        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {

            }
        }

        val amount = counterpartySession.receive<Long>().unwrap { it }
        val currency = counterpartySession.receive<String>().unwrap { it }
        val whoAmI = counterpartySession.receive<String>().unwrap { it }

        val token = FiatCurrency.Companion.getInstance(currency)

        /* Create an instance of IssuedTokenType for the fiat currency */
        val issuedTokenType: IssuedTokenType = token issuedBy ourIdentity

        val myAccount = accountService.accountInfo(whoAmI).single().state.data
        println("myAccoount: ${myAccount.identifier.id}")
        val myKey = subFlow(RequestKeyForAccount(myAccount))
        println("myKey: $myKey")
        /* Create an instance of FungibleToken for the fiat currency to be issued */
        val fungibleToken = FungibleToken(Amount(amount, issuedTokenType), myKey)
//        val signedTx = subFlow(com.template.flows.IssueFiatCurrencyToken(currency, amount, counterpartySession))
        val stx = subFlow(IssueTokens(listOf(fungibleToken)))
        val holderKey = (stx.coreTransaction.getOutput(0) as FungibleToken).holder
        val test= "Issued $amount $currency token(s) to ${ourIdentity.name.organisation} with key $holderKey" +
                "\ntxId: ${stx.id}"
        counterpartySession.send(test)
    }
}
