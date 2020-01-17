package esling.ian.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class TransferRequest(
    val fromAccount: String,
    val toAccount: String,
    val amount: Double
)

data class Account(val accountRef: String)

data class AccountBalance(
    private val accountRef: String,
    private val amount: Double
)

data class TransferTransaction(
    val fromAccount: Account,
    val toAccount: Account,
    val amount: BigDecimal,
    val dateTime: LocalDateTime
) {
    constructor(
        fromAccount: Account,
        toAccount: Account,
        amount: BigDecimal
    ) : this(fromAccount, toAccount, amount, LocalDateTime.now())
}