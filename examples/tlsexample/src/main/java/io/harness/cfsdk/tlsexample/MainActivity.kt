package io.harness.cfsdk.tlsexample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.events.AuthResult
import io.harness.cfsdk.cloud.events.EvaluationListener
import io.harness.cfsdk.cloud.model.AuthInfo
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.oksse.EventsListener
import io.harness.cfsdk.logging.CfLog
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.crypto.params.Blake3Parameters.context
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import java.io.*
import java.nio.charset.StandardCharsets
import java.security.Provider
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import java.util.stream.Collectors


class MainActivity : AppCompatActivity() {

    private lateinit var client: CfClient
    private lateinit var timer: Timer
    private val logTag = MainActivity::class.simpleName
    private val logPrefix : String = "SDK"
    private val sdkKey : String = "<ADD YOUR SDK KEY HERE>"
    private val flagName : String = System.getenv("FF_FLAG_NAME") ?: "harnessappdemodarkmode"
    private val bcProvider: Provider = BouncyCastleProvider()

    private val pemFile = "-----BEGIN CERTIFICATE-----\n" +
            "<ADD YOUR PEM FILE HERE>\n" +
            "-----END CERTIFICATE-----";



    private var eventsListener = EventsListener { event ->
        CfLog.OUT.v(logTag, "Event: ${event.eventType}")
    }

    private val flag1Listener: EvaluationListener = EvaluationListener {
        val eval = client.boolVariation(flagName, false)
        CfLog.OUT.v(logTag, "$flagName value: $eval")
    }



    private fun authSuccess(authInfo: AuthInfo, result: AuthResult) {

        if (!result.isSuccess) {
            handleError(result)
            return
        }

        val registerEventsOk = client.registerEventsListener(eventsListener)
        var registerEvaluationsOk = 0

        if (client.registerEvaluationListener("flag1", flag1Listener)) {
            registerEvaluationsOk++
        }

        if (registerEventsOk && registerEvaluationsOk > 0) {
            CfLog.OUT.i(logTag, "$logPrefix Registrations OK")
        }

        setupWatchTimer()

    }

    private fun handleError(result: AuthResult) {
        val e = result.error
        var msg = "$logPrefix Initialization error"
        e?.let { err ->
            err.message?.let { errMsg ->
                msg = errMsg
            }
            CfLog.OUT.e(logTag, msg, err)
        }
        runOnUiThread {

            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (sdkKey == null || sdkKey.isEmpty()) {
            throw java.lang.IllegalArgumentException("No SDK key given")
        }

        val trustedServers = loadCerts(pemFile)

        client = CfClient()

        val config = CfConfiguration.builder()
            .enableAnalytics(true)
            .enableStream(true)
            .tlsTrustedCAs(trustedServers)
            .build()

        val target = Target().identifier("Android_TLS_Example").name("Android TLS Example")
        client.initialize(
            this,
            sdkKey,
            config,
            target,
            this::authSuccess
        )
    }

    private fun readEvaluations(client: CfClient, logPrefix: String) {
        val value = client.boolVariation(flagName, false)
        CfLog.OUT.v(logTag, "$logPrefix flag $flagName: $value")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        timer.purge()
        client.destroy()
    }

    private fun setupWatchTimer() {
        try {
            timer = Timer()
            timer.schedule(
                object : TimerTask() {
                    override fun run() {
                        readEvaluations(client, logPrefix)
                    }
                },
                0,
                10000
            )

        } catch (e:Exception) {
            CfLog.OUT.e(logTag, "Error", e)
        }
    }

    // Here we're using BC's PKIX lib to convert the PEM to an X.509, you can use any crypto library you prefer
    @Throws(IOException::class, CertificateException::class)
    private fun loadCerts(pem: String): List<X509Certificate>? {
        val list: MutableList<X509Certificate> = ArrayList()
        PEMParser(StringReader(pem)).use { parser ->
            var obj: Any?
            while (parser.readObject().also { obj = it } != null) {
                if (obj is X509CertificateHolder) {
                    list.add(
                        JcaX509CertificateConverter().setProvider(bcProvider)
                            .getCertificate(obj as X509CertificateHolder?)
                    )
                }
            }
        }
        return list
    }
}