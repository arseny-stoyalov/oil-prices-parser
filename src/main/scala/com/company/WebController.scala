package com.company

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.company.Application._
import com.company.services.PriceService
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.io.Source

object WebController extends LazyLogging {

  private val config = ConfigFactory.load()

  val route: Route =
    concat(
      path("") {
        get {
          complete(
            HttpEntity("Server is working")
          )
        }
      },
      //-------------GET-PRICE---------------------
      path("getPrice") {
        get {
          concat(
            parameters("on".as[String]) { date =>
              complete(
                PriceService.getPriceByDate(date, getRawData("urals")) match {
                  case Left(error) => HttpEntity(error)
                  case Right(answer) => HttpEntity(answer)
                }
              )
            },
            parameters("from".as[String], "to".as[String]) {
              (date1, date2) =>
                complete(
                  PriceService.getAvgPriceByPeriod(date1, date2, getRawData("urals")) match {
                    case Left(error) => HttpEntity(error)
                    case Right(answer) => HttpEntity(answer)
                  }
                )
            })
        }
      },
      //-------------GET-MIN-MAX-PRICES---------------------
      path("getMinMaxPrices") {

        get {
          parameters("from".as[String], "to".as[String]) { (date1, date2) =>
            complete(
              PriceService.getMinMaxPrices(date1, date2, getRawData("urals")) match {
                case Left(error) => HttpEntity(error)
                case Right(json) => HttpEntity(ContentTypes.`application/json`, json)
              }
            )
          }

        }
      },
      //-------------GET-STATS---------------------
      path("getStats") {

        get {
          complete(
            PriceService.getStats(getRawData("urals")) match {
              case Left(error) => HttpEntity(error)
              case Right(json) => HttpEntity(ContentTypes.`application/json`, json)
            }
          )
        }

      },
      //-------------GET-LOGS---------------------
      path("getLogs") {
        get {
          complete({
            val src = Source.fromFile("logs/oil-price/oil-price-parser.log")
            val response = src.mkString
            src.close()
            HttpEntity(response)
          })
        }

      }
    )

  def start(): Unit = {
    Http().newServerAt(config.getString("app.url"), config.getInt("app.port"))
      .bind(route)
  }

  def getRawData(brand: String): String = {
    //Выбор марки нефти
    val sourceUrl = config.getString(s"source.data-gov-ru.json.$brand") +
      //TODO: ENV VARIABLE
      config.getString("source.data-gov-ru.access-token")
    val src = Source.fromURL(sourceUrl)
    val json = src.mkString
    src.close()
    json
  }

}
