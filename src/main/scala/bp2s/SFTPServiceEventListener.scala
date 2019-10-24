package bp2s

import java.io.File
import java.nio.file.{CopyOption, Path}
import java.util.Collection

import org.apache.sshd.server.subsystem.sftp.SftpEventListener
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.subsystem.sftp.Handle
import org.slf4j.{Logger, LoggerFactory}

import com.zengularity.benji.s3.{ S3, WSS3 }
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.ByteString
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import akka.util.ByteString
import play.api.libs.ws.DefaultBodyWritables._
import scala.concurrent.Await

class SFTPServiceEventListener(conf: Conf) extends SftpEventListener {
  private val logger: Logger = LoggerFactory.getLogger(this.getClass())

  implicit val system = ActorSystem("actor-system-sftp")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val ws: StandaloneAhcWSClient = StandaloneAhcWSClient()
  val s3: WSS3 = S3(conf.s3User, conf.s3Password, "http", conf.s3Host)

  val kafkaProducerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
    .withBootstrapServers(conf.kafkaBootstrapServer)

  private def handle(file: File): scala.concurrent.Future[Unit] =
    for {
      bucketRef <- {
        logger.info(s"Cfreating S3 bucket")
        val ref = s3.bucket(conf.bucketName)
        ref.create(failsIfExists = false).map(_ => ref)
      }
      storageObj = bucketRef.obj(file.getName())
      message = Message(storageObj.bucket, storageObj.name).jsonify
      _ = {
        logger.info(s"sending file to S3 ${conf.s3Host} then sending a message into kafka topic ${conf.kafkaTopic}")
      }
      _ <- FileIO.fromPath(file.toPath())
        .alsoTo(storageObj.put[ByteString])
        .map { _ =>
          logger.info(s"message to send to kafka: $message")
          new ProducerRecord[String, String](conf.kafkaTopic, message)
        }
        .runWith(Producer.plainSink(kafkaProducerSettings))
    } yield ()

  override def open(serverSession: ServerSession, remoteHandle: String, localHandle: Handle): Unit = {
    // File openedFile = localHandle.getFile().toFile();
    // if (openedFile.exists() && openedFile.isFile()) {
    // }
    ()
  }

  override def closed(serverSession: ServerSession, remoteHandle: String, localHandle: Handle, thrown: Throwable): Unit = {
    val closedFile: File = localHandle.getFile().toFile()
    if (closedFile.exists() && closedFile.isFile()) {
      logger.info(s"User ${serverSession.getUsername()} closed file: '${localHandle.getFile().toAbsolutePath()}'")
      handle(closedFile).onComplete { r => 
        logger.info(s"result of file handle: $r")
      }
    }
  }

  override def created(serverSession: ServerSession, path: Path, attrs: java.util.Map[String, _], thrown: Throwable): Unit = {
      val username: String = serverSession.getUsername()
      logger.info(String.format("User %s created: \"%s\"", username, path.toString()))
      // service.UserWroteFile(username, path)
      ()
  }

  override def moved(serverSession: ServerSession, source: Path, destination: Path, collection: Collection[CopyOption], throwable: Throwable): Unit = {
      val username: String = serverSession.getUsername()
      logger.info(String.format("User %s moved: \"%s\" to \"%s\"", username, source.toString(), destination.toString()))
      // service.UserWroteFile(username, destination)
      ()
  }
}
