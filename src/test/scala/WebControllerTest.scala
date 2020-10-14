import com.company.WebController
import com.company.data.OilDataGov
import io.circe.parser.decode
import com.company.parsing.OilDataGovParser._
import org.scalatest.{FunSuite, Matchers}

import scala.util.{Success, Try}

class WebControllerTest extends FunSuite with Matchers{

  test("testGetRawData") {
    val tryGetJson = Try(WebController.getRawData("urals"))
    tryGetJson shouldBe a[Success[String]]
    val optionOilData = decode[Seq[OilDataGov]](tryGetJson.get)
    optionOilData shouldBe a[Right[Error, String]]
  }

}
