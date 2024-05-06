//function 1
//Get energy data for the last week, and then analyze the data to output summary information and corresponding warnings.
import java.time.{LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.net.{URL, HttpURLConnection}
import java.io.{BufferedReader, InputStreamReader}

case object Function1 {
  def function1(args: Array[String]): Unit = {
    // Calculates the start time and end time of the latest week
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val currentTime = LocalDateTime.now()
    val endTime = currentTime.format(formatter)
    val startTime = currentTime.minusWeeks(1).format(formatter)
    
    val pageSize=20000;
    //Map the energy type to the corresponding API address.
    val energyTypes = Map(
      "Solar" -> s"https://data.fingrid.fi/api/datasets/248/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize",
      "Wind" -> s"https://data.fingrid.fi/api/datasets/181/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize",
      "Hydropower" -> s"https://data.fingrid.fi/api/datasets/191/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize"
    )

    println("Fetching data for all energy types:")
    //Iterate over the energyTypes map, make network requests for each energy type, and parse the returned JSON data using the parseData method.
    energyTypes.foreach { case (typeLabel, apiUrl) =>
      val data = fetchData(apiUrl)
      println(s"$typeLabel: " + parseData(data))
    }

    val generatedPowerData = fetchData(s"https://data.fingrid.fi/api/datasets/192/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize")
    val consumedPowerData = fetchData(s"https://data.fingrid.fi/api/datasets/193/data?startTime=$startTime&endTime=$endTime&pageSize=$pageSize")

    val generatedPower = parseData(generatedPowerData)
    val consumedPower = parseData(consumedPowerData)

    println("Summary of power data:")
    println(s"Generated power: $generatedPower")
    println(s"Consumed power: $consumedPower")

    // Output a warning or message based on the relationship between power generated and power consumed.
    if (generatedPower._2 < consumedPower._2 * 1.2) {
      println("Warning: Insufficient power generation, request for additional power input is needed.")
    } else if (generatedPower._2 > consumedPower._2 * 2.0) {
      println("Sufficient power generation, capable of supplying power to other regions.")
    }
  }

  //Used to get data from a specified URL
  def fetchData(url: String): String = {
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setRequestProperty("x-api-key", "f775993eb01747438015e4f25ec7041b") // Use your actual API key

    val responseCode = connection.getResponseCode
    if (responseCode == 200) {
      val inputStream = connection.getInputStream
      val bufferedReader = new BufferedReader(new InputStreamReader(inputStream))
      val response = new StringBuilder
      var inputLine = ""

      while ({inputLine = bufferedReader.readLine(); inputLine != null}) {
        response.append(inputLine)
      }
      bufferedReader.close()
      response.toString
    } else {
      s"Error: $responseCode"
    }
  }

  //Parses the JSON data returned from the API and returns a tuple containing a time and a value.
  def parseData(jsonResponse: String): (String, Double) = {
    // Simplified JSON parsing, just for illustration
    val valuePattern = "\"value\":(\\d+\\.?\\d*)".r
    val timePattern = "\"startTime\":\"(.*?)\"".r

    val value = valuePattern.findFirstMatchIn(jsonResponse).map(_.group(1).toDouble).getOrElse(0.0)
    val time = timePattern.findFirstMatchIn(jsonResponse).map(_.group(1)).getOrElse("No time data")

    (time, value)
  }
}
