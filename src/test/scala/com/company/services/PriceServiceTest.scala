package com.company.services

import java.time.LocalDate

import com.company.data.{MinMaxPrice, OilDataGov, Stats}
import com.company.parsing.OilDataGovParser._
import com.company.parsing.ResponseParsers._
import io.circe.syntax._
import org.scalatest.{FunSuite, Matchers}

import scala.util.{Success, Try}

class PriceServiceTest extends FunSuite with Matchers {

  def testOilVal: String = {
    val src: scala.io.Source =
      scala.io.Source.fromFile("src/test/test-vals/test-val-oil.json")
    val str: String = src.mkString
    src.close()
    str
  }

  def testMinMaxVal: String = {
    val src: scala.io.Source =
      scala.io.Source.fromFile("src/test/test-vals/test-val-response-minmax.json")
    val str: String = src.mkString
    src.close()
    str
  }

  def testStatsVal: String = {
    val src: scala.io.Source =
      scala.io.Source.fromFile("src/test/test-vals/test-val-response-stats.json")
    val str: String = src.mkString
    src.close()
    str
  }

  test("testDecodeJson") {
    MinMaxPrice(3.5, 4).asJson.toString shouldBe testMinMaxVal
    Stats(15).asJson.toString shouldBe testStatsVal
    val optionOilData = PriceService.decodeJson[Seq[OilDataGov]](testOilVal)
    optionOilData shouldBe a[Some[Seq[OilDataGov]]]
    val firstRecord = optionOilData.get.head
    firstRecord.avgPrice shouldBe 764.6
    firstRecord.periodStart shouldBe LocalDate.of(2013, 3, 15)
  }

  test("testGetMinMaxPrices") {
    val response = PriceService
      .getMinMaxPrices("2013-03-15", "2013-09-14", testOilVal)
    response shouldBe Right(MinMaxPrice(732.8, 827.9).asJson.toString)
    val swappedDatesCase = PriceService
      .getMinMaxPrices("2013-09-14", "2013-03-15", testOilVal)
    swappedDatesCase shouldBe a[Left[String, String]]
  }

  test("testGetAvgPriceByPeriod") {
    val response = PriceService
      .getAvgPriceByPeriod("2013-03-15", "2013-05-20", testOilVal)
    response shouldBe a[Right[String, String]]
    val numResponse = Try(response.toOption.get.toDouble)
    numResponse shouldBe a[Success[Double]]
    numResponse.get shouldBe 748.9 +- .5
  }

  test("testGetPriceByDate") {
    val response = PriceService
      .getPriceByDate("2013-09-23", testOilVal)
    response shouldBe Right("793.8")
  }

  test("testGetStats") {
    val response = PriceService
      .getStats(testOilVal)
    response shouldBe Right(Stats(8).asJson.toString)
  }

}
