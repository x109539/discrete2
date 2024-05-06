import scala.io.StdIn
import com.github.tototoshi.csv.CSVReader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.file.{Files, Paths}
import scala.util.Try

case object Function4 {
  case class EnergyRecord(time: LocalDateTime, value: Double)

  var data: Option[List[EnergyRecord]] = None // Add data variable as an object property

  def readCSV(filePath: String): Option[List[EnergyRecord]] = {
    if (Files.exists(Paths.get(filePath))) {
      val reader = CSVReader.open(new File(filePath))
      try {
        val data = reader.allWithHeaders().flatMap { map =>
          val time = tryParseDateTime(map("Time"), Seq("yyyy/MM/dd HH:mm", "yyyy-MM-dd'T'HH:mm:ss"))
          val value = Try(map("Value").toDouble)
          for {
            t <- time
            v <- value.toOption
          } yield EnergyRecord(t, v)
        }
        Some(data)
      } catch {
        case _: Throwable =>
          println("Error: Failed to read data from the CSV file.")
          None
      } finally {
        reader.close()
      }
    } else {
      println("Error: File not found or invalid path.")
      None
    }
  }

  def tryParseDateTime(dateTimeString: String, patterns: Seq[String]): Option[LocalDateTime] = {
    patterns.foldLeft[Option[LocalDateTime]](None) { (result, pattern) =>
      result.orElse(Try(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern))).toOption)
    }
  }

  def calculateMode(data: List[EnergyRecord]): Double = {
    val frequencyMap = data.groupBy(_.value).mapValues(_.size)
    frequencyMap.maxBy(_._2)._1
  }

  def calculateRange(data: List[EnergyRecord]): Double = {
    val values = data.map(_.value)
    values.max - values.min
  }

  def function4(args: Array[String]): Unit = {
    def analyze(): Unit = {
      println("Do you want to calculate the 'Mode' or the 'Range'? Type 'Mode', 'Range', or 'Quit' to stop:")
      val analysisType = StdIn.readLine().toLowerCase()

      analysisType match {
        case "mode" =>
          data.foreach { dataList =>
            val mode = calculateMode(dataList)
            println(f"The mode of the values is: $mode%.2f")
          }
          analyze()
        case "range" =>
          data.foreach { dataList =>
            val range = calculateRange(dataList)
            println(f"The range of the values is: $range%.2f")
          }
          analyze()
        case "quit" =>
          println("Exiting the program.")
        case _ =>
          println("Invalid input. Please type 'Mode', 'Range' or 'Quit'.")
          analyze()
      }
    }

    println("Please enter the path to the CSV file:")
    var filePath = StdIn.readLine().trim()
    if (filePath.toLowerCase == "quit") {
      println("Exiting the program.")
      return
    }

    val dataFromFile: Option[List[EnergyRecord]] = readCSV(filePath)

    if (dataFromFile.isEmpty) {
      println("File not found or invalid path.")
      function4(args)
      return
    }

    data = dataFromFile // Assign dataFromFile to the data variable
    analyze()
  }
}
