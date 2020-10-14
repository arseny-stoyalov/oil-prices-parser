package com.company.parsing

import com.company.data.{MinMaxPrice, Stats}
import io.circe.Encoder

object ResponseParsers {

  implicit val encodeMinMaxPrice: Encoder[MinMaxPrice] =
    Encoder.forProduct2("min", "max")(entity =>
      (entity.min, entity.max)
    )

  implicit val encodeStats: Encoder[Stats] =
    Encoder.forProduct1("records-total")(_.recordNum)

}
