package esling.ian

import assertk.assertThat
import assertk.assertions.isEqualTo
import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class AccountBalanceCalculatorTest {

    private val account1 = Account("1")
    private val account2 = Account("2")
    private val account3 = Account("3")

    @Test
    fun `calculate account balance`() {
        val transactions = listOf(
            TransferTransaction(account1, account2, BigDecimal.TEN),
            TransferTransaction(account2, account1, BigDecimal.valueOf(3)),
            TransferTransaction(account1, account2, BigDecimal.ONE)
        )
        assertThat(calculateAccountBalance(transactions, account2)).isEqualTo(BigDecimal.valueOf(8))
    }

    @Test
    fun `ignore transactions for other accounts`() {
        val transactions = listOf(
            TransferTransaction(account1, account3, BigDecimal.TEN),
            TransferTransaction(account3, account1, BigDecimal.valueOf(3)),
            TransferTransaction(account1, account2, BigDecimal.ONE)
        )
        assertThat(calculateAccountBalance(transactions, account2)).isEqualTo(BigDecimal.valueOf(1))
    }

    @Test
    fun `cope with negative balance`() {
        val transactions = listOf(
            TransferTransaction(account2, account3, BigDecimal.TEN),
            TransferTransaction(account3, account2, BigDecimal.valueOf(3)),
            TransferTransaction(account1, account2, BigDecimal.ONE)
        )
        assertThat(calculateAccountBalance(transactions, account2)).isEqualTo(BigDecimal.valueOf(-6))
    }

    @Test
    fun `zero if no transactions for account`() {
        val transactions = listOf(
            TransferTransaction(account2, account3, BigDecimal.TEN),
            TransferTransaction(account3, account2, BigDecimal.valueOf(3)),
            TransferTransaction(account3, account2, BigDecimal.ONE)
        )
        assertThat(calculateAccountBalance(transactions, account1)).isEqualTo(BigDecimal.ZERO)
    }
}