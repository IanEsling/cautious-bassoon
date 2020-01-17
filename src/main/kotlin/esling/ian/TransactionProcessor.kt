package esling.ian

import java.math.BigDecimal
import java.time.LocalDateTime

data class Account(val accountRef: String)

data class Transaction(
    val account: Account,
    val amount: BigDecimal,
    val dateTime: LocalDateTime
)

class TransactionProcessor(private val transactions: MutableList<Transaction>) {

    fun processTransaction(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
        transactions.add(Transaction(fromAccount, amount.negate(), LocalDateTime.now()))
        transactions.add(Transaction(toAccount, amount, LocalDateTime.now()))
    }
}