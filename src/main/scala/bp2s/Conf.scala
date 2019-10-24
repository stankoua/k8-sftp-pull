package bp2s

import java.nio.file.{Path, Paths}

case class Conf(
  serverkeyPath: Path,
  clientKeyPath: Path,
  s3User: String,
  s3Password: String,
  s3Host: String,
  bucketName: String,
  kafkaTopic: String,
  kafkaBootstrapServer: String
)

object Conf {
  def read(args: List[String]): Conf = Conf(
    serverkeyPath = Paths.get(args(0)),
    clientKeyPath = Paths.get(args(1)),
    s3User = args(2),
    s3Password = args(3),
    s3Host = args(4),
    bucketName = args(5),
    kafkaTopic = args(6),
    kafkaBootstrapServer = args(7)
  )
}
