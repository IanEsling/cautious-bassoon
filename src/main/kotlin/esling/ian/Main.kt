package esling.ian

import esling.ian.handlers.*
import esling.ian.model.accountStartingBalance
import esling.ian.model.knownAccounts
import esling.ian.model.startingTransactions
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.Clock

private val accountBalances = AccountBalances(startingTransactions, accountStartingBalance, knownAccounts)

private val transferRequestProcessor = TransferRequestProcessor(
    startingTransactions.toMutableList(),
    Clock.systemDefaultZone(),
    accountBalances
)

val defaultValidators = listOf(
    requestAmountValidator(),
    sameAccountsValidator(),
    amountPrecisionValidator(),
    knownAccountsValidator()
)

fun accountTransferApp(
    transferRequestHandler: HttpHandler,
    balanceRequestHandler: HttpHandler
): HttpHandler =
    routes(
        "/ping" bind Method.GET to { Response(OK) },
        "/transfer" bind Method.PUT to transferRequestHandler,
        "/balances/{ref:.*}" bind Method.GET to balanceRequestHandler
    )

fun accountTransferServer(port: Int): Http4kServer = accountTransferApp(
    transferRequestHandler(
        transferRequestProcessor,
        defaultValidators
    ),
    balanceRequestHandler(accountBalances)
).asServer(Jetty(port))

fun main() {
    val server = accountTransferServer(9000)

    server.start()
}
