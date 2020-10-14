import java.time.LocalDate

import com.company.data.OilDataGov
import com.company.parsing.OilDataGovParser._
import io.circe.parser.decode

object TestEntry extends App {
  val date = LocalDate.of(2013, 3, 20)
  val json =
    """[
      |  {
      |    "Начало периодамониторинга цен на нефть": "15.мар.17",
      |    "Конец периодамониторинга цен на нефть": "14.апр.17",
      |    "Средняя цена на нефть сырую марки «Юралс» на мировых рынках нефтяного сырья (средиземноморском и роттердамском)": "365,3"
      |  }
      |  ]""".stripMargin

  val obj = decode[Seq[OilDataGov]](json).getOrElse(Nil)
  println(obj)
}
