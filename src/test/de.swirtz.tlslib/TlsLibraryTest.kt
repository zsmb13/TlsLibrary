import de.swirtz.tlslib.api.serverSocketFactory
import de.swirtz.tlslib.api.socketFactory
import de.swirtz.tlslib.server.TLSServer
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import java.nio.file.Paths
import org.junit.Assert.assertEquals
import java.io.DataOutputStream

class TlsLibraryTest {

    var tlsServer: TLSServer? = null

    @Before
    fun setup() = startServer()

    @Test
    fun firstTest() {
        val fac = socketFactory {
            trustManager {
                storeFile = Paths.get("certsandstores/myTruststore")
                password = "123456"
                fileType = "jks"
            }
            sockets {
                //                cipherSuites = listOf("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
//                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA")
                timeout = 10_000
            }
        }
        fac.createSocket("localhost", 9333).use {
            DataOutputStream(it.getOutputStream()).use {
                it.writeUTF("Hello World")
            }
        }
        runBlocking { delay(1000) }
        assertEquals("Hello World", tlsServer?.getLastMsg())
    }

    private fun startServer() {
        val fac = serverSocketFactory {
            keyManager {
                storeFile = Paths.get("certsandstores/clientkeystore")
                password = "123456"
                fileType = "jks"
            }
            sockets {
                //                cipherSuites = listOf("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
//                        "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA")
                timeout = 10_000
            }
        }

        tlsServer = TLSServer(9333, fac)
        tlsServer?.start()
    }
}