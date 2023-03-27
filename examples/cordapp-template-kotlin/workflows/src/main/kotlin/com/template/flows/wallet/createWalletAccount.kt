package com.template.flows.wallet

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.accounts.workflows.accountService
import com.r3.corda.lib.accounts.workflows.flows.RequestKeyForAccount
import com.r3.corda.lib.tokens.workflows.utilities.ourSigningKeys
import com.template.contracts.CreateWalletAccountContract
import com.template.flows.accounts.CreateNewAccount
import com.template.states.WalletState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatingFlow
@StartableByRPC
class CreateWalletAccountInitiator(
        private val onlineAccountName:String,
        private val offlineAccountName:String,
        private val walletId:String,
        private val accountHolderName:String
        ) : FlowLogic<WalletState>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): WalletState {

        val notary = serviceHub.networkMapCache.getNotary( CordaX500Name.parse("O=Notary,L=London,C=GB"))


        val onlineAccount:UniqueIdentifier= subFlow(CreateNewAccount(onlineAccountName))
        val offlineAccount=subFlow(CreateNewAccount(offlineAccountName))

        val linkedAccounts= mapOf(onlineAccountName to onlineAccount, offlineAccountName to offlineAccount)
        val accountInfoOnlineAccount  = accountService.accountInfo(onlineAccountName).single().state.data
        val onlineKey = subFlow(RequestKeyForAccount(accountInfoOnlineAccount))

        val accountInfoOfflineAccount  = accountService.accountInfo(onlineAccountName).single().state.data
        val offlineKey = subFlow(RequestKeyForAccount(accountInfoOfflineAccount))
        val output = WalletState(UniqueIdentifier(),walletId,accountHolderName, linkedAccounts,ourIdentity, listOf(onlineKey,offlineKey))


        val builder = TransactionBuilder(notary)
                .addCommand(CreateWalletAccountContract.Commands.Create(), listOf(ourIdentity.owningKey,onlineKey.owningKey,offlineKey.owningKey))
                .addOutputState(output)


        val ledgerTransaction: LedgerTransaction =builder?.toLedgerTransaction(serviceHub)
        val ourSigningKeys = ledgerTransaction.ourSigningKeys(serviceHub)

        val stx = builder?.let {
            serviceHub.signInitialTransaction(it, signingPubKeys = ourSigningKeys)
        }
        subFlow(FinalityFlow(stx,emptyList()))

        return stx.tx.outputStates.last() as WalletState



    }
}
