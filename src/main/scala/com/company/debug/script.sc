import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
val dateFormatterDataGov: DateTimeFormatter =
  DateTimeFormatter.ofPattern("dd.MMMyy", new Locale("ru"))

LocalDate.parse(
  makeMonthValidForJavaLocale(
    makeMonthValidForJavaLocale("14.май.13")
  ),
  dateFormatterDataGov)

