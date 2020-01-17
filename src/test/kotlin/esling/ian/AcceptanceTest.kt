package esling.ian

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.google.gson.Gson
import esling.ian.handlers.balanceRequestHandler
import esling.ian.handlers.transferRequestHandler
import esling.ian.model.Account
import esling.ian.model.AccountBalance
import esling.ian.model.TransferTransaction
import esling.ian.model.knownAccounts
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.math.BigDecimal
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class AcceptanceTest {

    private val clock = Clock.systemDefaultZone()
    private val transactions = LinkedList<TransferTransaction>()
    private val accountBalances = AccountBalances(BigDecimal.valueOf(1000000), knownAccounts)
    private val trp = TransferRequestProcessor(transactions, clock, accountBalances)

    private val app =
        accountTransferApp(transferRequestHandler(trp, defaultValidators), balanceRequestHandler(accountBalances))
    private var jettyServer = app.asServer(Jetty(9000))

    private fun transferRequest(body: String): Request {
        return Request(Method.PUT, "http://localhost:9000/transfer")
            .body(body.trimIndent())
    }

    private fun balanceRequest(accountReference: String): Request {
        return Request(Method.GET, "http://localhost:9000/balances/$accountReference")
    }

    @BeforeEach
    fun `start server`() {
        jettyServer.start()
    }

    @AfterEach
    fun `stop server`() {
        jettyServer.stop()
    }

    @Test
    fun `transfer requests`() {

        val transferRequest = transferRequest(
            """{"fromAccount":"Ref1","toAccount":"Ref2","amount":12.34}"""
        )

        val start = LocalDateTime.now()
        runBlocking {
            repeat(10) {
                val client = ApacheClient()
                launch {
                    repeat(100) {
                        assertThat(client(transferRequest).status).isEqualTo(Status.OK)
                        assertThat(client(balanceRequest("Ref1")).status).isEqualTo(Status.OK)
                        assertThat(client(balanceRequest("Ref2")).status).isEqualTo(Status.OK)
                    }
                }
            }
        }
        println("completed transfers, total time: ${start.until(LocalDateTime.now(), ChronoUnit.MILLIS)}ms")
        assertThat(transactions).hasSize(1000)
        assertThat(
            accountBalances.balanceForAccount(Account("Ref1"))?.intValueExact()
                ?: fail("balance found for account Ref1")
        ).isEqualTo(java.lang.Double.valueOf(1000 * 12.34 * -1).toInt().plus(1000000))
        assertThat(
            accountBalances.balanceForAccount(Account("Ref2"))?.intValueExact()
                ?: fail("balance found for account Ref2")
        )
            .isEqualTo(java.lang.Double.valueOf(1000 * 12.34).toInt().plus(1000000))

    }

    private val startingAmount = BigDecimal.valueOf(5000000)

    @Test
    fun `concurrency reassurance`() {
        val threads = mutableListOf<Thread>()
        val accountBalances = AccountBalances(
            transactions, startingAmount,
            listOf(Account("Ref1"), Account("Ref2"))
        )
        val balanceCheckingTransactionProcessor = TransferRequestProcessor(
            transactions,
            clock,
            accountBalances
            )

        val fails = AtomicInteger(0)
        repeat(2000) {
            threads.add(thread {
                try {
                    balanceCheckingTransactionProcessor.processTransaction(
                        Account("Ref1"),
                        Account("Ref2"),
                        BigDecimal.valueOf(12.34)
                    )
                    accountBalances.balanceForAccount(Account("Ref1"))
                    accountBalances.balanceForAccount(Account("Ref2"))
                } catch (e: Exception) {
                    println(e)
                    fails.incrementAndGet()
                }
            })
        }

        threads.forEach { t -> t.join() }
        assertThat(fails.get()).isEqualTo(0)
        assertThat(transactions).hasSize(2000)
    }

    @Test
    fun `get balances`() {
        val gson = Gson()
        val request = Request(Method.GET, "http://localhost:9000/balances/Ref1")
        val client = ApacheClient()
        val response = client(request)
        assertThat(response.status).isEqualTo(Status.OK)
        assertThat(gson.fromJson(response.bodyString(), AccountBalance::class.java))
            .isEqualTo(AccountBalance("Ref1", 1000000.0))

    }

    @Test
    fun `get balance for unknown account returns 404`() {
        val request = Request(Method.GET, "http://localhost:9000/balances/XXX")
        val client = ApacheClient()
        val response = client(request)
        println(response)
        assertThat(response.status).isEqualTo(Status.NOT_FOUND)
        assertThat(response.bodyString()).isEmpty()

    }

    @Test
    fun `bad payloads cause 400`() {
        val missingToAccount = """{"fromAccount":"Ref1","amount":123}"""
        val missingFromAccount = """{"toAccount":"Ref1","amount":123}"""
        val missingAmount = """{"fromAccount":"Ref1","toAccount":"Ref2"}"""
        val zeroAmount = """{"fromAccount":"Ref1","toAccount":"Ref2","amount":0}"""
        val negativeAmount = """{"fromAccount":"Ref1","toAccount":"Ref2","amount":-100.0}"""
        val sameAccounts = """{"fromAccount":"Ref1","toAccount":"Ref1","amount":100}"""
        val tooLargePrecision = """{"fromAccount":"Ref1","toAccount":"Ref2","amount":100.123}"""
        val fromUnknownAccount = """{"fromAccount":"XXX","toAccount":"Ref2","amount":100.12}"""
        val toUnknownAccount = """{"fromAccount":"Ref1","toAccount":"XXX","amount":100.12}"""
        val transferOverdrawsFromAccount = """{"fromAccount":"Ref1","toAccount":"Ref2","amount":1000000.12}"""

        val client = ApacheClient()
        listOf(
            missingToAccount, missingFromAccount, missingAmount, zeroAmount, negativeAmount, sameAccounts,
            tooLargePrecision, fromUnknownAccount, toUnknownAccount, transferOverdrawsFromAccount
        ).forEach { b ->
            val request = transferRequest(b)
            val response = client(request)
            println("got respone $response for payload $b")
            assertThat(response.status).isEqualTo(Status.BAD_REQUEST)
        }

    }
}