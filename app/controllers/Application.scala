package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future
import play.api.libs.json.Json._
import play.api.data.Forms._
import models._
import views._
import play.api.data._
import JsonHelper._
import scala.util.{Success, Try}
import play.api.libs.iteratee.Enumeratee
import play.api.libs.EventSource

object Application extends Controller with LocationProvider with WeatherProviderStrategies with ConcreteProviders {

  /**
   * Describe the computer form (used in both edit and create screens).
   */
  val addressForm = Form(
    mapping(
      "address" -> nonEmptyText
    )(Address.apply)(Address.unapply)
  )


  def getWeather(locations: Seq[Location]): Future[Seq[LocationWithWeather]] =  Future.sequence(locations map all)

  def weatherPost() = Action.async(parse.json) {
    implicit request =>
      addressForm.bindFromRequest.fold(formWithErrors => Future {
        BadRequest("Unable to parse form")
      },
      address => {
        getWeatherAsJson(address.address)
      })
  }

  def weatherGet(address: String) = Action.async {
    getWeatherAsJson(address)
  }

  /**
   * Method that returns a stream to be consumed by an HTML5 EventSource
   *
   * Try out with: curl -vN  http://localhost:9000/weatherstream/strandvägen
   *
   * @param address the address to search for
   * @return Chuncked stream
   */
  def getWeatherStream(address: String) = Action.async { request =>
    import util.EnumeratorUtil._

    val enumerator = locationWithWeatherEnumerator[Location, LocationWithWeather](getLocations(address), _ map all)

    val formatMessage = Enumeratee.map[Try[LocationWithWeather]] {
      case Success(m) => toJson(m)
    }

    Future(Ok.chunked(enumerator through formatMessage through EventSource()).as(EVENT_STREAM))
  }

  def index = Action.async {
    Future {
      Ok(html.main())
    }
  }

  private def getWeatherAsJson(address: String) = {
    getLocations(address)
      .flatMap(getWeather)
      .map(s => Ok(toJson(s)))
  }

}