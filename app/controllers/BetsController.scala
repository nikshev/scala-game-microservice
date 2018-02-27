package controllers

import javax.inject._

import models.{Bet, Wallet}
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
  * Simple controller that directly stores and retrieves [models.Bets] instances into a MongoDB Collection
  * Input is first converted into a bets and then the bets is converted to JsObject to be stored in MongoDB
  * Also I use Wallet case class for work with deposit
  */
@Singleton
class BetsController @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit exec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def betsFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("bets"))
  def walletsFuture: Future[JSONCollection] = database.map(_.collection[JSONCollection]("wallets"))

  /**
    * Place bet
    * @return
    */
  def place = Action.async(parse.json) { request =>
    Json.fromJson[Bet](request.body) match {
      case JsSuccess(bet, _) =>
        val walletsList = findWalletById(bet.playerId)
        walletsList.map { wallets =>
          val walletFromBase = wallets.headOption.getOrElse(Wallet("",0.0))
          val currentBalance = walletFromBase.balance - bet.amount
          if (walletFromBase.id != "" && currentBalance > 0) {
            updateBalanceById(bet.playerId, currentBalance)
            for {
              bets <- betsFuture
              lastError <- bets.insert(bet)
            } yield
              Future.successful(Accepted(Json.obj("currentBalance" -> currentBalance)))
            Accepted(Json.obj("currentBalance" -> currentBalance))
          } else
            BadRequest(Json.obj("error" -> "Can't place this sum!!!"))
        }
      case JsError(errors) =>
        Future.successful(BadRequest("Can't create operation from the json provided. " + Errors.show(errors)))
    }
  }

  /**
    * Show bets
    * @param id - player/wallet id
    * @return
    */
  def show(id: String) = Action.async {
    val betsList = findBetsById(id)
    // everything's ok! Let's reply with a JsValue
    betsList.map { bets =>
      Ok(Json.toJson(bets))
    }
  }

  /**
    * Find bet by player/wallet id
    * @param id
    * @return
    */
  def findBetsById(id: String) : Future[List[Bet]] = {
    // let's do our query
    val futureBetsList: Future[List[Bet]] =  betsFuture.flatMap {
      // find all bets with player id
      _.find(Json.obj("playerId" -> id)).
      // perform the query and get a cursor of JsObject
      cursor[Bet](ReadPreference.primary).
      // Collect the results as a list
      collect[List]()
    }

    futureBetsList
  }



  /**
    * Find wallet by id
    *
    * @param id - string UUID
    * @return
    */
  def findWalletById(id: String): Future[List[Wallet]] = {
    // let's do our query
    val futureWalletsList: Future[List[Wallet]] = walletsFuture.flatMap {
      // find wallet with name `id`
      _.find(Json.obj("id" -> id)).
        // perform the query and get a cursor of JsObject
        cursor[Wallet](ReadPreference.primary).
        // Coollect the results as a list
        collect[List]()
    }

    futureWalletsList
  }


  /**
    * Update balance by id
    * @param id - wallet id
    * @param currentBalance - balance for update
    */
  def updateBalanceById(id: String, currentBalance: Double): Unit = {
    val modifier = Json.obj(
      "$set" -> Json.obj(
        "balance" -> JsNumber(currentBalance)
      )
    )
    for {
      collection <- walletsFuture
      lastError <- collection.update(Json.obj("id" -> id), modifier)
    } yield {
      Logger.info(s"Successfully inserted with LastError: $lastError")
    }
  }

}


