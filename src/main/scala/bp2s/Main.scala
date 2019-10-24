package bp2s

import java.nio.file.Paths
import java.util.concurrent.CancellationException
import java.security.PublicKey;
import java.util.Collections

import scala.util.Using
import scala.util.Using.Releasable
  
import cats.effect.ExitCase.Canceled
import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all._
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.shell.ProcessShellFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory
import org.slf4j.{Logger, LoggerFactory}

object Main extends IOApp {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  def launch(sshd: SshServer): IO[Unit] = {
    def go: IO[Unit] = IO.suspend(go)

    sshd.start()
    logger.info(s"sshd.isStarted(): ${sshd.isStarted()}")
    go
  }

  def getServer(conf: Conf): SshServer = {
    val sshd = SshServer.setUpDefaultServer()
    val sftpSubsystemFactory = new SftpSubsystemFactory
    val sftpEventListener: SFTPServiceEventListener = new SFTPServiceEventListener(conf)

    sshd.setPublickeyAuthenticator(new AuthorizedKeysAuthenticator(conf.clientKeyPath))
    sshd.setPort(2221)
    sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(conf.serverkeyPath))
    sshd.setSubsystemFactories(Collections.singletonList(sftpSubsystemFactory))
    sftpSubsystemFactory.addSftpEventListener(sftpEventListener)

    sshd
  }

  def run(args: List[String]): IO[ExitCode] = {
    logger.info(s"running program Main with args: $args")
    val conf = Conf.read(args)
    val sshServer = getServer(conf)
    launch(sshServer).guaranteeCase {
      case Canceled => 
        logger.info("stopping the ssh server")
        sshServer.stop()
        IO.pure(())
      case _ => IO.pure(())
    }.as(ExitCode(0))
  }

}
