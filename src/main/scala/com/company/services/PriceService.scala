package com.company.services

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.company.data.{MinMaxPrice, OilDataGov, Stats}
import com.company.parsing.OilDataGovParser._
import com.company.parsing.ResponseParsers._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.parser.decode
import io.circe.syntax._

import scala.util.{Failure, Success, Try}

object PriceService extends LazyLogging {


  private val errorMessage = "Invalid query"
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def getPriceByDate(strDate: String, json: String): Either[String, String] = {

    logger.info("Processing request for getPriceByDate method\n" +
      s"args: $strDate")
    val date: LocalDate =
      Try(LocalDate.parse(strDate, dateFormatter)) match {
        case Success(date) => date
        case Failure(_) => return Left(errorMessage)
      }

    val oilDataGovList = decodeJson[Seq[OilDataGov]](json).get

    if (oilDataGovList.isEmpty) return Left(errorMessage)

    Right(oilDataGovList
      .find(p => isDateInPeriod(date, p.periodStart, p.periodEnd))
      .get.avgPrice.toString)
  }

  def getAvgPriceByPeriod(strDate1: String, strDate2: String, json: String): Either[String, String] = {

    logger.info("Processing request for getAvgPriceByPeriod method\n" +
      s"args: $strDate1 , $strDate2")
    val oilDataGovPeriodList = getOilDataGovPeriodList(strDate1, strDate2, json) match {
      case Some(list) => list
      case None => return Left(errorMessage)
    }

    Right((oilDataGovPeriodList
      .map(_.avgPrice)
      .sum / oilDataGovPeriodList.length).toString)
  }

  def getStats(json: String): Either[String, String] = {
    logger.info("Processing request for getStats method\n")
    val oilDataGovList = decodeJson[Seq[OilDataGov]](json).get
    Right(Stats(oilDataGovList.length).asJson.toString)
  }

  def getMinMaxPrices(strDate1: String, strDate2: String, json: String): Either[String, String] = {

    logger.info("Processing request for getMinMaxPrices method\n" +
      s"args: $strDate1 , $strDate2")
    val oilDataGovPeriodList = getOilDataGovPeriodList(strDate1, strDate2, json) match {
      case Some(list) => list
      case None => return Left(errorMessage)
    }

    Right(MinMaxPrice(
      oilDataGovPeriodList.minBy(_.avgPrice).avgPrice,
      oilDataGovPeriodList.maxBy(_.avgPrice).avgPrice)
      .asJson.toString)
  }

  private def isDateInPeriod(date: LocalDate, start: LocalDate, end: LocalDate): Boolean = {
    start.compareTo(date) <= 0 && end.compareTo(date) >= 0
  }

  private def getOilDataGovPeriodList(strDate1: String, strDate2: String,
                                      json: String): Option[Seq[OilDataGov]] = {

    val date1 = Try(LocalDate.parse(strDate1, dateFormatter))
    val date2 = Try(LocalDate.parse(strDate2, dateFormatter))

    val period = (date1, date2) match {
      case (Success(date1), Success(date2)) => (date1, date2)
      case _ => return None
    }

    val oilDataGovList = decodeJson[Seq[OilDataGov]](json).get

    oilDataGovList.filter(p => isDateInPeriod(p.periodStart, period._1, period._2)
      && isDateInPeriod(p.periodEnd, period._1, period._2)) match {
      case list if list.nonEmpty => Option(list)
      case _ => None
    }
  }

  def decodeJson[T](json: String)(implicit decoder: Decoder[T]): Option[T] = {
    decode[T](json) match {
      case Left(error) =>
        logger.debug(s"Got troubles parsing json data: ${error.getMessage}")
        Option.empty[T]
      case Right(parsedDate) => Option(parsedDate)
    }
  }

}
