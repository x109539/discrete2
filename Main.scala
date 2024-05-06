import scala.io.StdIn.readLine
import Function1._
import Function2._
import Function3._
import Function4._
import scala.util.{Try, Success, Failure}

object Main {
  def main(args: Array[String]): Unit = {
    def iterate(): Unit = {
      println("\nWhich functionality do you want to use?")
      println("1. Monitoring and controlling renewable energy in power plants.")
      println("2. Collect data and store it")
      println("3. View of power generation and storage capabilities")
      println("4. Analyze data")
      println("5. Stop\n")

      Try(readLine().toInt) match {
        case Success(choice) =>
          choice match {
            case 1 => Function1.function1(Array.empty)
            case 2 => Function2.function2(Array.empty)
            case 3 => Function3.function3(args)
            case 4 => Function4.function4(Array.empty)
            case 5 => // Exit the function
            case _ =>
              println("Invalid choice.")
          }
          if (choice != 5) iterate() // Recursive call to iterate function if choice is not 5 (exit)
        case Failure(_) =>
          println("Invalid input. Please enter a number between 1 and 5.")
          iterate() // Restart the iteration
      }
    }

    iterate() // Initial call to start the iteration
  }
}
