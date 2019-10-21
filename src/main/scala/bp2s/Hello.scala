package bp2s

import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.security.PublicKey;


import scala.util.Using
import scala.util.Using.Releasable
  
import cats.effect.ExitCase.Canceled
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.slf4j.{Logger, LoggerFactory}

object Hello extends IOApp {
  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def launch(sshd: SshServer): IO[Unit] = {
    def go: IO[Unit] = IO.suspend(go)

    sshd.start()
    logger.debug(s"sshd.isStarted(): ${sshd.isStarted()}")
    go
  }


  def getServer(): SshServer = {
    val sshd = SshServer.setUpDefaultServer()
    val keyPath = Paths.get("ssh_host_key.pub")

    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
      def authenticate(username: String, password: String, session: ServerSession): Boolean = {
        // username != null && username.equals(password)
        true
      }
    });
    sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {
      def authenticate(username: String, key: PublicKey, session: ServerSession): Boolean = {
        //File f = new File("/Users/" + username + "/.ssh/authorized_keys");
        true
      }
    })
    sshd.setPort(2221)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath))
    sshd
  }

  def run(args: List[String]): IO[ExitCode] = {
    val sshServer = getServer()
    launch(sshServer).guaranteeCase {
      case Canceled => 
        logger.debug("stopping the ssh server")
        sshServer.stop()
        IO.pure(())
      case _ => IO.pure(())
    }.as(ExitCode(0))
  }

}
