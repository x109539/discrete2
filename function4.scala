import scala.io.StdIn
import com.github.tototoshi.csv.CSVReader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.nio.file.{Files, Paths}
import scala.util.Try

case object Function4 {
  //Represents energy records. Each record has a time stamp and a corresponding value.
  case class EnergyRecord(time: LocalDateTime, value: Double)
//data is a variable attribute used to store energy records read from a CSV file. It is initially set to None
  var data: Option[List[EnergyRecord]] = None 
//Accepts a file path as a parameter and attempts to read data from the CSV file specified by that path.
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
        Some(data) //If successful, it parses the data into a list of energy records and returns it wrapped in Some
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

  //This function attempts to parse a date-time string using the given date-time pattern.If successful, the parsed LocalDateTime object is returned; Otherwise, None is returned
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

  //The entry point of the program. It prompts the user to enter the path of the CSV file and attempt to read the data. If the data is successfully read, it stores the data in the data variable and begins the analysis process.
  def function4(args: Array[String]): Unit = {
    //Perform different data analysis tasks (mode, scope, or exit) depending on the user's choice
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
