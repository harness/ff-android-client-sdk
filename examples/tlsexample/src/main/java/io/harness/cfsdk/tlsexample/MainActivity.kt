package io.harness.cfsdk.tlsexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ch.qos.logback.classic.android.BasicLogcatConfigurator
import io.harness.cfsdk.FfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.model.Target
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser


import java.io.IOException
import java.io.StringReader
import java.security.Provider
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MainActivity : AppCompatActivity() {

    private val log: Logger = LoggerFactory.getLogger(MainActivity::class.java)

    companion object {
        init {
            BasicLogcatConfigurator.configureDefaultContext()
        }
    }

    private lateinit var client: CfClient
    private lateinit var timer: Timer
    private val logPrefix : String = "SDK"
    private val sdkKey : String = "<ADD YOUR SDK KEY HERE>"
    private val flagName : String = System.getenv("FF_FLAG_NAME") ?: "harnessappdemodarkmode"
    private val bcProvider: Provider = BouncyCastleProvider()

    private val pemFile = "-----BEGIN CERTIFICATE-----\n" +
            "<ADD YOUR PEM FILE HERE>\n" +
            "-----END CERTIFICATE-----";


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
            target
        )

        client.waitForInitialization()

        setupWatchTimer()

        log.info("init done")
    }

    private fun readEvaluations(client: CfClient, logPrefix: String) {
        val value = client.boolVariation(flagName, false)
        log.info("$logPrefix flag $flagName: $value")
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        timer.purge()
        client.close()
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
            log.error("WatchTimer ERROR", e)
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