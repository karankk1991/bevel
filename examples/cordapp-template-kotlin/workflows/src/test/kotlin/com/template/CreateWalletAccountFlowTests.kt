package com.template

import com.r3.corda.lib.accounts.contracts.states.AccountInfo
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.template.states.TemplateState
import java.util.concurrent.Future;
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import com.template.flows.Initiator
import com.template.flows.accounts.CreateNewAccount
import com.template.flows.wallet.CreateWalletAccountInitiator
import com.template.states.WalletState
import net.corda.core.concurrent.CordaFuture
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault.StateStatus


class CreateWalletAccountFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode
    private var accountName="account-1"

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))))
        a = network.createPartyNode()
        b = network.createPartyNode()
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }
    @Test
    fun `CreateWallet`() {
        val flow = CreateWalletAccountInitiator(accountName)
        val future: Future<SignedTransaction> = a.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.
         val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
         val states = a.services.vaultService.queryBy(WalletState::class.java, inputCriteria).states[0].state.data
    }
    @Test
    fun `CreateAccountForWallet`() {
        val flow = CreateNewAccount(accountName)
//        val future: CordaFuture<SignedTransaction> = a.startFlow(flow)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)
//        val states = b.services.vaultService.queryBy(AccountInfo::class.java, inputCriteria).states[0].state.data
    }
}