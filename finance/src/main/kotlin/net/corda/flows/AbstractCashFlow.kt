package net.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.TxKeyFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker

/**
 * Initiates a flow that produces an Issue/Move or Exit Cash transaction.
 */
abstract class AbstractCashFlow<T>(override val progressTracker: ProgressTracker) : FlowLogic<T>() {
    companion object {
        object GENERATING_ID : ProgressTracker.Step("Generating anonymous identities")
        object GENERATING_TX : ProgressTracker.Step("Generating transaction")
        object SIGNING_TX : ProgressTracker.Step("Signing transaction")
        object FINALISING_TX : ProgressTracker.Step("Finalising transaction")

        fun tracker() = ProgressTracker(GENERATING_ID, GENERATING_TX, SIGNING_TX, FINALISING_TX)
    }

    @Suspendable
    internal fun finaliseTx(participants: Set<Party>, tx: SignedTransaction, message: String) {
        try {
            subFlow(FinalityFlow(tx, participants))
        } catch (e: NotaryException) {
            throw CashException(message, e)
        }
    }
}

class CashException(message: String, cause: Throwable) : FlowException(message, cause)