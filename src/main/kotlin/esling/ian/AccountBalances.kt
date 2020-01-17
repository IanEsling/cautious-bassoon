package esling.ian

import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import java.math.BigDecimal

interface UpdateAccounts {
    fun update(fromAccount: Account, toAccount: Account, amount: BigDecimal)
}

class AccountBalances(
    transferTransactions: List<TransferTransaction>,
    startingBalance: BigDecimal,
    accounts: Collection<Account>
) : UpdateAccounts {
    constructor(
        startingBalance: BigDecimal,
        accounts: Collection<Account>
    ) : this(listOf(), startingBalance, accounts)

    private val accountBalances = mutableMapOf<Account, BigDecimal>()

    init {
        accountBalances.putAll(accounts.map {
            it to calculateAccountBalance(transferTransactions, it).plus(
                startingBalance
            )
        })
        if (accountBalances.any { it.value.signum() <= 0 })
            throw UnsupportedOperationException("Account balance must be positive: $accountBalances")
    }

    override fun update(fromAccount: Account, toAccount: Account, amount: BigDecimal) {
        //unknown accounts are dealt with upstream so safe using !! here
        val newFromAccountBalance = accountBalances[fromAccount]!!.minus(amount)
        val newToAccountBalance = accountBalances[toAccount]!!.plus(amount)

        if (newFromAccountBalance.signum() < 0 || newToAccountBalance.signum() < 0) {
            throw UnsupportedOperationException("Account balance cannot be negative")
        } else {
            accountBalances[fromAccount] = newFromAccountBalance
            accountBalances[toAccount] = newToAccountBalance
        }
    }

    fun balanceForAccount(account: Account): BigDecimal? {
        return accountBalances[account]
    }
}