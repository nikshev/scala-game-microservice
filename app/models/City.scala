package models

import play.api.libs.json.Json

case class Bet(playerId: String, gameId: String, amount: Double)

object Bet {
  implicit val formatter = Json.format[Bet]
}
