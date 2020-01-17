package esling.ian

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isTrue
import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

internal class TransferRequestProcessorTest {

    private val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val knownFromAccount = Account("Ref1")
    private val knownToAccount = Account("Ref2")
    private val accounts = listOf(knownFromAccount, knownToAccount)
    private val exceptionThrowingAccountUpdater = object : UpdateAccounts {
        override fun update(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
            throw UnsupportedOperationException()
        }
    }

    @Test
    fun `no transaction if balance update fails`() {
        val transferTransactions: MutableList<TransferTransaction> = LinkedList()
        val processor = TransferRequestProcessor(transferTransactions, clock, exceptionThrowingAccountUpdater)

        try {
            processor.processTransaction(knownFromAccount, knownToAccount, BigDecimal.ONE)
            fail("expected exception from processor")
        } catch (e: Exception) {
            assertThat(transferTransactions).hasSize(0)
        }
    }

    @Test
    fun `create transfer transaction`() {
        val transferTransactions: MutableList<TransferTransaction> = LinkedList()
        val processor = TransferRequestProcessor(transferTransactions, accounts, clock)

        processor.processTransaction(knownFromAccount, knownToAccount, BigDecimal.TEN)
        assertThat(transferTransactions).hasSize(1)
        assertThat(transferTransactions).contains(
            TransferTransaction(
                knownFromAccount,
                knownToAccount,
                BigDecimal.TEN,
                LocalDateTime.now(clock)
            )
        )
    }

    @Test
    fun `update balances for transaction`() {
        var called = false
        val stubAccountUpdater = object : UpdateAccounts {
            override fun update(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
                called = true
            }
        }
        val transferTransactions: MutableList<TransferTransaction> = LinkedList()
        val processor = TransferRequestProcessor(
            transferTransactions,
            clock,
            stubAccountUpdater
        )

        processor.processTransaction(knownFromAccount, knownToAccount, BigDecimal.TEN)
        assertThat(called).isTrue()
    }
}