package uitest

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FunSuite}
import javax.servlet.ServletContext
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.concurrent.TimeUnit
import com.softwaremill.codebrag.{WebServerConfig, EmbeddedJetty, Beans}
import pages.{MainPage, LoginPage}
import org.openqa.selenium.support.PageFactory

class CodebragUITest extends FunSuite with UITestsEmbeddedJetty with BeforeAndAfterAll with BeforeAndAfter {
  final val REGUSER = "reguser"
  final val REGPASS = "regpass"
  final val REGMAIL = "reguser@regmail.pl"

  var driver: FirefoxDriver = _
  var loginPage: LoginPage = _
  var mainPage: MainPage = _
  var beans: Beans = _

  override def beforeAll() {
    startJetty()
    beans = context.getAttribute("codebrag").asInstanceOf[Beans]
  }

  before {
    driver = new FirefoxDriver()
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS)
    loginPage = PageFactory.initElements(driver, classOf[LoginPage])
    mainPage = PageFactory.initElements(driver, classOf[MainPage])
  }

  after {
    driver.close()
    driver = null
  }

  override def afterAll() {
    stopJetty()
  }
}

trait UITestsEmbeddedJetty extends EmbeddedJetty {
  protected var context: ServletContext = null

  override protected def prepareContext() = {
    val context = super.prepareContext()
    context.setResourceBase("codebrag-ui/src/main/webapp")
    this.context = context.getServletContext
    context
  }

  def webServerConfig = new WebServerConfig {
    def rootConfig = null
    override lazy val webServerHost = "0.0.0.0"
    override lazy val webServerPort = 8080
  }
}