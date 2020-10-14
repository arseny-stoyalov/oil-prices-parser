package com.company.data

import java.time.LocalDate

trait Data

final case class OilDataGov(periodStart: LocalDate, periodEnd: LocalDate, avgPrice: Double) extends Data

final case class MinMaxPrice(min: Double, max: Double) extends Data

final case class Stats(recordNum: Int) extends Data



