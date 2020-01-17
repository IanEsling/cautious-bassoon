package esling.ian.handlers

import com.google.gson.Gson
import esling.ian.TransferRequestProcessor
import esling.ian.model.Account
import esling.ian.model.TransferRequest
import esling.ian.model.knownAccounts
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import java.math.BigDecimal


typealias TransferRequestValidator = (TransferRequest) -> Boolean

fun requestAmountValidator(): TransferRequestValidator {
    return { tr -> tr.amount > 0 }
}

fun sameAccountsValidator(): TransferRequestValidator {
    return { tr -> tr.fromAccount != tr.toAccount }
}

fun amountPrecisionValidator(): TransferRequestValidator {
    return { tr -> BigDecimal.valueOf(tr.amount).scale() <= 2 }
}

fun knownAccountsValidator(): TransferRequestValidator {
    return { tr -> knownAccounts.contains(Account(tr.fromAccount)) && knownAccounts.contains(Account(tr.toAccount)) }
}

fun transferRequestHandler(
    transferRequestProcessor: TransferRequestProcessor,
    validators: Collection<TransferRequestValidator> = listOf(),
    gson: Gson = Gson()
): HttpHandler = { request ->

    try {
        val transferRequest = gson.fromJson(request.bodyString(), TransferRequest::class.java)
        if (validators.map { v -> v.invoke(transferRequest) }.any { !it }) {
            throw IllegalArgumentException()
        }
        transferRequestProcessor.processTransaction(
            Account(transferRequest.fromAccount),
            Account(transferRequest.toAccount),
            BigDecimal.valueOf(transferRequest.amount)
        )

        Response(Status.OK)
    } catch (e: Exception) {
        Response(Status.BAD_REQUEST)
    }
}