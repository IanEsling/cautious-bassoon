package esling.ian

import assertk.assertThat
import assertk.assertions.isEqualTo
import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertFailsWith

fun Int.bd(): BigDecimal {
    return BigDecimal.valueOf(this.toLong())
}

fun Double.bd(): BigDecimal {
    return BigDecimal.valueOf(this)
}

internal class AccountBalancesTest {

    private val accountStartBalance = 500

    private val account1 = Account("Ref1")
    private val account2 = Account("Ref2")
    private val accounts = listOf(account1, account2)

    @Test
    fun `calculate starting balances`() {
        val transferAmount = 10
        val ab = AccountBalances(
            listOf(TransferTransaction(account1, account2, transferAmount.bd())),
            accountStartBalance.bd(),
            accounts
        )

        assertThat(ab.balanceForAccount(account1)).isEqualTo((accountStartBalance - transferAmount).bd())
        assertThat(ab.balanceForAccount(account2)).isEqualTo((accountStartBalance + transferAmount).bd())
    }

    @Test
    fun `throw if any starting balance is negative`() {
        val transferAmount = 10
        assertFailsWith<UnsupportedOperationException> {
            AccountBalances(
                listOf(TransferTransaction(account1, account2, transferAmount.bd())),
                BigDecimal.ZERO,
                accounts
            )
        }
    }

    @Test
    fun `update balances`() {
        val transferAmount = 98.76
        val ab = AccountBalances(
            accountStartBalance.bd(),
            accounts
        )

        assertThat(ab.balanceForAccount(account1)).isEqualTo(accountStartBalance.bd())
        assertThat(ab.balanceForAccount(account2)).isEqualTo(accountStartBalance.bd())

        ab.update(account2, account1, transferAmount.bd())

        assertThat(ab.balanceForAccount(account1)).isEqualTo((accountStartBalance + transferAmount).bd())
        assertThat(ab.balanceForAccount(account2)).isEqualTo((accountStartBalance - transferAmount).bd())
    }

    @Test
    fun `throw if balance updates to negative`() {
        val transferAmount = 100
        val ab = AccountBalances(
            10.bd(),
            accounts
        )

        assertFailsWith<UnsupportedOperationException> {
            ab.update(account2, account1, transferAmount.bd())
        }
    }
}