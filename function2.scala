import java.net.{HttpURLConnection, URL}
import java.io.{BufferedReader, InputStreamReader, PrintWriter}
import play.api.libs.json.{Json, JsValue}
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.io.StdIn.readLine

case object Function2 {
  def function2(args: Array[String]): Unit = {
    // API details map
    val apiDetails = List(
      "wind" -> "181",
      "solar" -> "248",
      "hydropower" -> "191",
      "production" -> "192",
      "consumption" -> "193"
    )

    // Get user input for start and end time
    println("Please enter the start time (YYYY-MM-DDTHH:MM:SSZ):")
    val startTime = readLine()
    println("Please enter the end time (YYYY-MM-DDTHH:MM:SSZ):")
    val endTime = readLine()

    // Choose time interval
    println("Do you want to aggregate the data? Choose one of the following:")
    println("1. No (Output original data)")
    println("2. Hourly")
    println("3. Daily")
    println("4. Weekly")
    println("5. Monthly")
    val aggregationChoice = readLine().toInt

    // Define time interval and statistical function based on user choice
    var interval = ""
    var statFunc: Seq[Double] => Double = null
    if (aggregationChoice != 1) {
      println("Choose the type of statistical measurement:")
      println("1. Mean")
      println("2. Median")
      println("3. Midrange")
      val measurementChoice = readLine().toInt
      val results = parseUserChoices(aggregationChoice, measurementChoice)
      interval = results._1
      statFunc = results._2
    }

    // Recursive function to select API
    def selectApi(apiIndex: Int = 0): Unit = {
      if (apiIndex < apiDetails.length) {
        val selectedApi = apiDetails(apiIndex)._1

        // Fetch data for the selected API
        val apiKey = "f775993eb01747438015e4f25ec7041b"
        val data = fetchData(apiDetails(apiIndex)._2, startTime, endTime, apiKey)

        // Write data to CSV file
        val desktopPath = Paths.get(System.getProperty("user.home"), "Desktop").toString
        val fileName = s"$desktopPath/energy_data_${selectedApi}_$interval.csv"
        if (aggregationChoice == 1) {
          writeDataToFile(data, fileName)
        } else {
          processDataAndWriteToFile(data, fileName, interval, statFunc)
        }
        println(s"Data for $selectedApi has been written to $fileName")

        // Ask user if they want to continue selecting API
        println("Do you want to select another API? (yes/no)")
        val continueInput = readLine()
        if (continueInput.toLowerCase == "yes") selectApi(apiIndex + 1) // Recursive call if user wants to continue
      }
    }

    // Start selecting API
    selectApi()
  }

  def fetchData(datasetId: String, startTime: String, endTime: String, apiKey: String): String = {
    val pageSize = 20000
    val url = s"https://data.fingrid.fi/api/datasets/$datasetId/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize"
    fetchData(url, apiKey)
  }

  def fetchData(url: String, apiKey: String): String = {
    val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setRequestProperty("x-api-key", apiKey)
    val responseCode = connection.getResponseCode

    if (responseCode == 200) {
      val inputStream = connection.getInputStream
      val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
      readLines(bufferedReader, new StringBuilder).toString
    } else {
      s"Error: $responseCode"
    }
  }

  def readLines(bufferedReader: BufferedReader, response: StringBuilder): StringBuilder = {
    val inputLine = bufferedReader.readLine()
    if (inputLine != null) {
      response.append(inputLine)
      readLines(bufferedReader, response)
    } else {
      bufferedReader.close()
      response
    }
  }

  def writeDataToFile(data: String, fileName: String): Unit = {
    val json: JsValue = Json.parse(data)
    val records = (json \ "data").as[Seq[JsValue]].map { record =>
      val endTime = (record \ "endTime").as[String]
      val value = (record \ "value").as[Double]
      val formattedTime = LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
      s"$formattedTime,$value"
    }

    val header = "Time,Value\n"
    val csvContent = header + records.mkString("\n")
    val writer = new PrintWriter(fileName)
    writer.write(csvContent)
    writer.close()
  }

  def parseUserChoices(aggregationChoice: Int, measurementChoice: Int): (String, Seq[Double] => Double) = {
    val interval = aggregationChoice match {
      case 1 => "original"
      case 2 => "hourly"
      case 3 => "daily"
      case 4 => "weekly"
      case 5 => "monthly"
    }

    val statFunc: Seq[Double] => Double = measurementChoice match {
      case 1 => data: Seq[Double] => data.sum / data.length // Mean
      case 2 => data: Seq[Double] => {
        val sorted = data.sorted
        if (sorted.length % 2 == 0) {
          (sorted(sorted.length / 2 - 1) + sorted(sorted.length / 2)) / 2.0 // Median
        } else {
          sorted(sorted.length / 2)
        }
      }
      case 3 => data: Seq[Double] => (data.min + data.max) / 2.0 // Midrange
    }
    (interval, statFunc)
  }

  def processDataAndWriteToFile(data: String, fileName: String, interval: String, statFunc: Seq[Double] => Double): Unit = {
    val json: JsValue = Json.parse(data)
    val records = (json \ "data").as[Seq[JsValue]]
    val timeValues = records.map { record =>
      val endTime = (record \ "endTime").as[String]
      val value = (record \ "value").as[Double]
      (endTime, value)
    }

    val formattedData = timeValues.groupBy { case (time, _) =>
      interval match {
        case "hourly" => time.substring(0, 13).replace('T', ' ')
        case "daily" => time.substring(0, 10).replace('T', ' ')
        case "weekly" => {
          val dateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
          val weekStart = dateTime.minusDays(dateTime.getDayOfWeek.getValue - 1)
          weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")).replace('T', ' ')
        }
        case "monthly" => time.substring(0, 7).replace('T', ' ')
        case _ => throw new IllegalArgumentException("Invalid interval")
      }
    }.mapValues(_.map(_._2).toList).toList.sortBy(_._1).map { case (time, values) =>
      val aggregatedValue = statFunc(values)
      s"$time,$aggregatedValue"
    }

    val header = "Time,Value\n"
    val csvContent = header + formattedData.mkString("\n")
    val writer = new PrintWriter(fileName)
    writer.write(csvContent)
    writer.close()
  }

  def aggregateByHour(timeValues: Seq[(String, Double)]): Map[String, Seq[Double]] = {
    timeValues.groupBy { case (time, _) =>
      LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).substring(0, 13) // Group by hour
    }.mapValues(_.map(_._2).toList).toMap
  }

  def aggregateByDay(timeValues: Seq[(String, Double)]): Map[String, Seq[Double]] = {
    timeValues.groupBy { case (time, _) =>
      LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).substring(0, 10) // Group by day
    }.mapValues(_.map(_._2).toList).toMap
  }

  def aggregateByWeek(timeValues: Seq[(String, Double)]): Map[String, Seq[Double]] = {
    timeValues.groupBy { case (time, _) =>
      val dateTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
      val weekStart = dateTime.minusDays(dateTime.getDayOfWeek.getValue - 1)
      weekStart.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")) // Group by week
    }.mapValues(_.map(_._2).toList).toMap
  }

  def aggregateByMonth(timeValues: Seq[(String, Double)]): Map[String, Seq[Double]] = {
    timeValues.groupBy { case (time, _) =>
      LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")).substring(0, 7) // Group by month
    }.mapValues(_.map(_._2).toList).toMap
  }
}
