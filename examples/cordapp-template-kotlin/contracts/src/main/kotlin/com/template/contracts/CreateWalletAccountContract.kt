package com.template.contracts

import com.template.entity.AccountInformation
import com.template.states.TemplateState
import com.template.states.WalletState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat

// ************
// * Contract *
// ************
class CreateWalletAccountContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.CreateWalletAccountContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        val output = tx.outputsOfType<WalletState>().first()
        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when a wallet is created.".using(tx.inputStates.isEmpty())

            }
            is Commands.Update -> requireThat {
                "There should be exactly one input state while linking a new account.".using(tx.inputStates.size==1)
                "There should be exactly one output state while linking a new account.".using(tx.outputStates.size==1)

            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Update : Commands
    }
}