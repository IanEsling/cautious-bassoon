package esling.ian

import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import esling.ian.model.accountStartingBalance
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime

class TransferRequestProcessor(
    private val transferTransactions: MutableList<TransferTransaction>,
    private val clock: Clock,
    private val accountBalances: UpdateAccounts
) {
    constructor(
        transferTransactions: MutableList<TransferTransaction>,
        accounts: List<Account>,
        clock: Clock
    ) : this(transferTransactions, clock, AccountBalances(transferTransactions, accountStartingBalance, accounts))

    @Synchronized
    fun processTransaction(fromAccount: Account, toAccount: Account, amount: BigDecimal) {

        accountBalances.update(fromAccount, toAccount, amount)
        transferTransactions.add(TransferTransaction(fromAccount, toAccount, amount, LocalDateTime.now(clock)))
    }
}