package esling.ian.model

import java.math.BigDecimal


private val account1 = Account("Ref1")
private val account2 = Account("Ref2")
private val account3 = Account("Ref3")
private val account4 = Account("Ref4")

val knownAccounts = listOf(
    account1,
    account2,
    account3,
    account4
)

val startingTransactions = listOf(
    TransferTransaction(account1, account2, BigDecimal.valueOf(5.99)),
    TransferTransaction(account1, account3, BigDecimal.valueOf(12.34)),
    TransferTransaction(account1, account4, BigDecimal.valueOf(9.87)),
    TransferTransaction(account2, account4, BigDecimal.valueOf(6)),
    TransferTransaction(account4, account3, BigDecimal.valueOf(10.01)),
    TransferTransaction(account3, account1, BigDecimal.valueOf(9)),
    TransferTransaction(account2, account1, BigDecimal.valueOf(2)),
    TransferTransaction(account3, account4, BigDecimal.valueOf(1.99)),
    TransferTransaction(account4, account2, BigDecimal.valueOf(1.67))
)

val accountStartingBalance: BigDecimal = BigDecimal.valueOf(500)
