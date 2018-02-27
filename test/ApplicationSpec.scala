import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
  * Application test specification
  */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {
    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HelthCheckController" should {
     "answer random string" in {
       val healthCheck = route(app, FakeRequest(GET, "/healthcheck")).get
       status(healthCheck) mustBe OK
       contentType(healthCheck) mustBe Some("application/json")
     }
  }

  "BetsController"  should {

    "Place Bet" in {
      val placeBet = route(app, FakeRequest(POST, "/bets/place")
        .withJsonBody(Json.obj("playerId" -> "6ef84294-abda-4be7-964c-e4530e91a235", "gameId"->"1", "amount"->1.5))).get
      status(placeBet) mustBe ACCEPTED
      contentAsString(placeBet) must include ("currentBalance")
    }


    "Show Bets" in {
      val bets = route(app, FakeRequest(GET, "/bets?id=6ef84294-abda-4be7-964c-e4530e91a235")).get
      status(bets) mustBe OK
      contentType(bets) mustBe Some("application/json")
    }

  }

}