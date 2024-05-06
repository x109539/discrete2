import com.github.tototoshi.csv.CSVReader
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.knowm.xchart.{XYChartBuilder, SwingWrapper}
import org.knowm.xchart.style.markers.SeriesMarkers
import scala.annotation.tailrec
import scala.io.StdIn.readLine

case object Function3 {//Implement a system that can read the energy data in CSV file and draw the corresponding chart according to the date-time format provided by the user.

  case class EnergyRecord(time: LocalDateTime, value: Double)

  // Function to read data from a CSV file with custom date time format
  def readCSV(filePath: String, dateTimeFormat: String): Option[List[EnergyRecord]] = {
    try {
      val reader = CSVReader.open(new File(filePath))
      val data = reader.allWithHeaders().map { map =>
        val time = LocalDateTime.parse(map("Time"), DateTimeFormatter.ofPattern(dateTimeFormat))
        val value = map("Value").toDouble
        EnergyRecord(time, value)
      }
      reader.close()
      Some(data)
    } catch {
      case _: Throwable =>
        println(s"Error: Unable to read data from file $filePath.")
        None
    }
  }

  // Function to plot data from a single data set
  def plotData(data: List[EnergyRecord], title: String): Unit = {
    val chart = new XYChartBuilder().width(800).height(600).title(title).xAxisTitle("Time").yAxisTitle("Value").build()
    val xData = data.map(_.time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond.toDouble)
    val yData = data.map(_.value)
    val series = chart.addSeries("Energy Data", xData.toArray, yData.toArray)
    series.setMarker(SeriesMarkers.NONE)
    new SwingWrapper(chart).displayChart()
  }

  // Function to plot multiple data sets on the same chart
  def plotMultipleData(dataSets: Map[String, List[EnergyRecord]], title: String): Unit = {
    val chart = new XYChartBuilder().width(800).height(600).title(title).xAxisTitle("Time").yAxisTitle("Value").build()
    dataSets.foreach { case (name, data) =>
      val xData = data.map(_.time.atZone(java.time.ZoneId.systemDefault()).toEpochSecond.toDouble)
      val yData = data.map(_.value)
      val series = chart.addSeries(name, xData.toArray, yData.toArray)
      series.setMarker(SeriesMarkers.NONE)
    }
    new SwingWrapper(chart).displayChart()
  }

  @tailrec //Represents tail recursion.
  def runFunction3(): Unit = {
    //Prompts the user to enter the path to the CSV file and splits the path into an array
    println("Enter file paths separated by commas (e.g., C:\\path1.csv,C:\\path2.csv):")
    val inputPaths = readLine().split(",").map(_.trim)
    
    val dataSets = inputPaths.flatMap { path =>
      println(s"Enter date time format for file $path:")
      val dateTimeFormat = readLine().trim
      readCSV(path.replace("\\", "\\\\"), dateTimeFormat).map(path -> _)
    }.toMap

    if (dataSets.nonEmpty) {
      plotMultipleData(dataSets, "Combined Energy Data")

      // Operation command input
      println("Please provide an operation command (or enter 'exit' to quit):")
      @tailrec
      def processCommands(): Unit = {
        val command = readLine()
        if (command != "exit") {
          if (command.matches("[a-zA-Z\\s]+")) {
            println("Operation has been executed.")
          } else {
            println("Invalid operation.")
          }
          println("Provide another command (or 'exit' to quit):")
          processCommands()
        }
      }
      processCommands()
    } else {
      println("No valid data sets found.")//error management
    }

    println("Do you want to continue? (yes/no)")
    val continueInput = readLine().toLowerCase
    if (continueInput == "yes") {
      runFunction3()
    }
  }

  def function3(args: Array[String]): Unit = {
    runFunction3()
  }
}
