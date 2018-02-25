package controllers

import javax.inject._

import models.{Bet, City}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import utils.Errors

import scala.concurrent.{ExecutionContext, Future}


/**
  * Simple controller that directly stores and retrieves [models.City] instances into a MongoDB Collection
  * Input is first converted into a city and then the city is converted to JsObject to be stored in MongoDB
  */
@Singleton
class BetsController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def betsFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("bets"))

  def place(id: String, amount: Double) = Action.async {
    Future.successful(BadRequest("Could not build a city from the json provided. " ))
  }

  def show(id: String) = Action {
    val bets = findBetsById(id)
    // everything's ok! Let's reply with a JsValue
    bets.map { bet =>
      Ok(Json.toJson(bet))
    }
    BadRequest("Could not build a city from the json provided. " )
  }

  def findBetsById(id: String) : Future[List[Bet]] = {
    // let's do our query
    val futureCitiesList: Future[List[Bet]] =  betsFuture.flatMap {
      // find all cities with name `name`
      _.find(Json.obj("playerId" -> id)).
      // perform the query and get a cursor of JsObject
      cursor[Bet](ReadPreference.primary).
      // Coollect the results as a list
      collect[List]()
    }

    futureCitiesList
  }

}


