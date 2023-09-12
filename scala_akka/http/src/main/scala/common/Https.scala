package common

import akka.http.scaladsl.model.HttpMessage.HttpMessageScalaDSLSugar
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}
import scala.concurrent.Future

object Https extends HttpApp {

  // 1. Set-up keystore object
  private val ks: KeyStore = KeyStore.getInstance("PKCS12")
  private val ksFile: InputStream = getClass.getClassLoader.getResourceAsStream("keystore.pkcs12")
  private val pass = "akka-https".toCharArray // Fetch a password from a safe place (not hardcoded like this)
  ks.load(ksFile, pass)

  // 2. Initialize a key manager
  private val algorithm = "SunX509"
  private val keyManagerFactory = KeyManagerFactory.getInstance(algorithm) // PKI = public key infrastructure
  keyManagerFactory.init(ks, pass)

  // 3. Initialize a trust manager
  private val trustManager = TrustManagerFactory.getInstance(algorithm)
  trustManager.init(ks)

  // 4. Initialize a SSL context
  private val sslContext = SSLContext.getInstance("TLS")
  sslContext.init(keyManagerFactory.getKeyManagers, trustManager.getTrustManagers, new SecureRandom())

  // 5. Return the HTTPS connection context
  private val httpsConnectionContext: HttpsConnectionContext = ConnectionContext.httpsServer(sslContext)

  import actorSystem.dispatcher // recommended to use a different dispatcher
  private val requestHandler: HttpRequest => Future[HttpResponse] = {
    any: HttpRequest =>
      any.discardEntityBytes()
      Future(HttpResponse(StatusCodes.OK))
  }

  Http().newServerAt(host, defaultPort).enableHttps(httpsConnectionContext).bind(requestHandler)

}
