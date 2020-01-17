package esling.ian

import esling.ian.model.Account
import esling.ian.model.TransferTransaction
import java.math.BigDecimal

fun calculateAccountBalance(transactions: Collection<TransferTransaction>, account: Account): BigDecimal {

    return transactions.filter { t -> t.fromAccount == account || t.toAccount == account }
        .fold(BigDecimal.ZERO) { acc, t ->
            if (t.fromAccount == account) acc.minus(t.amount)
            else acc.add(t.amount)
        }

}