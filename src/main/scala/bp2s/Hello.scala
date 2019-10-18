package bp2s

import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.nio.file.Paths

object Hello extends App {

  val sshd = SshServer.setUpDefaultServer()
  val keyPath = Paths.get("ssh_host_key.pub")
  sshd.setPort(2221)
  sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(keyPath))


  println(s"========+========+==>> test 1")
}
