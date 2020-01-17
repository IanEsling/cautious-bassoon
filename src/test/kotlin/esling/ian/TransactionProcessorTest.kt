package esling.ian

import assertk.assertThat
import assertk.assertions.hasSize
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class TransactionProcessorTest {

    @Test
    fun `transfer amount between accounts`() {
        val transactions: MutableList<Transaction> = mutableListOf()
        val processor = TransactionProcessor(transactions)

        processor.processTransaction(Account("Ref1"), Account("Ref2"), BigDecimal.TEN)
        assertThat(transactions).hasSize(2)
    }
}