package com.company.parsing

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.company.data.OilDataGov
import io.circe.{Decoder, HCursor}

import scala.util.Try

object OilDataGovParser {

  //--------------FORMATTERS-----------------------
  private val dateFormatterDataGov: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MMMyy", new Locale("ru"))

  private val numberFormatterDataGov = NumberFormat.getInstance(new Locale("ru"))

  //--------------DECODERS-----------------------
  private val decoderDataGovDate: Decoder[LocalDate] = Decoder.decodeString.emapTry[LocalDate](str => {
    Try(LocalDate.parse(makeMonthValidForJavaLocale(str), dateFormatterDataGov))
  })

  private val decoderDataGovDouble: Decoder[Double] = Decoder.decodeString.emapTry[Double](str => {
    Try(numberFormatterDataGov.parse(str).doubleValue())
  })

  implicit val decodeOilDataGov: Decoder[com.company.data.OilDataGov] = new Decoder[OilDataGov] {
    final def apply(c: HCursor): Decoder.Result[OilDataGov] = for {
      start <- c.get[LocalDate]("Начало периодамониторинга цен на нефть")(decoderDataGovDate)
      end <- c.get[LocalDate]("Конец периодамониторинга цен на нефть")(decoderDataGovDate)
      avg <- c.get[Double]("Средняя цена на нефть сырую марки " +
        "«Юралс» на мировых рынках нефтяного сырья " +
        "(средиземноморском и роттердамском)")(decoderDataGovDouble)
    } yield {
      OilDataGov(start, end, avg)
    }
  }

  def makeMonthValidForJavaLocale(date: String) = {

    val september = "([\\d]+\\.сен\\.[\\d]+)".r
    val november = "([\\d]+\\.ноя\\.[\\d]+)".r
    val february = "([\\d]+\\.фев\\.[\\d]+)".r
    val may = "([\\d]+\\.май\\.[\\d]+)".r

    date match {
      case september(date) =>
        date.replaceAll("\\.сен\\.", ".сент.")
      case november(date) =>
        date.replaceAll("\\.ноя\\.", ".нояб.")
      case february(date) =>
        date.replaceAll("\\.фев\\.", ".февр.")
      case may(date) =>
        date.replaceAll("\\.май\\.", ".мая")
      case _ => date
    }

  }

}
