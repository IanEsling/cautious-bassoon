package esling.ian.handlers

import com.google.gson.Gson
import esling.ian.AccountBalances
import esling.ian.model.Account
import esling.ian.model.AccountBalance
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path
import org.http4k.lens.string


val accountRef = Path.string().of("ref")

fun balanceRequestHandler(
    balances: AccountBalances,
    gson: Gson = Gson()
): HttpHandler = { request ->

    val accountRef = accountRef(request)
    val balance = balances.balanceForAccount(Account(accountRef))
    if (balance == null) {
        Response(Status.NOT_FOUND)
    } else {
        Response(Status.OK).body(gson.toJson(AccountBalance(accountRef, balance.toDouble())))
    }
}