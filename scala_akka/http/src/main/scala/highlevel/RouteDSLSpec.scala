package highlevel

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json._

case class Book(isbn: String, author: String, title: String)

trait BookJsonProtocol extends DefaultJsonProtocol {
  implicit val bookFormat: RootJsonFormat[Book] = jsonFormat3(Book)
}

class RouteDSLSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with BookJsonProtocol {
  import RouteDSLSpec._

  "A digital library backend" should {
    val path = "/api/book"
    "return all the stored books" in {
       Get(path) ~> libraryRoute ~> check {
         status shouldBe StatusCodes.OK
         entityAs[List[Book]] shouldBe books
       }
    }
    "return the given book given a correct isbn" in {
      Get(s"$path/${books.head.isbn}") ~> libraryRoute ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Option[Book]] shouldBe Some(books.head)
      }
    }
    "add the given book to the storage" in {
      val book = Book("9780552166591", "Terry Pratchett", "The Colour of Magic")
      Post(path, book) ~> libraryRoute ~> check {
        status shouldBe StatusCodes.Created
        assert(books.contains(book))
      }
    }
    "not accept other methods" in {
      Delete(path) ~> libraryRoute ~> check {
        rejections should not be empty
      }
    }
  }
}

object RouteDSLSpec extends BookJsonProtocol with SprayJsonSupport {
  // code under test
  private var books = List(
    Book("9781250776631", "Brandon Sanderson", "The Way of Kings"),
    Book("9781509899012", "Naomi Novik", "Spinning Silver"),
    Book("8601419987948", "Pierce Brown", "Red Rising"),
  )

  /*
    GET /api/book -> returns all books
    GET /api/book/isbn -> returns a book
    POST /api/book -> inserts a book
   */
  private val libraryRoute = pathPrefix("api" / "book") {
    get {
      path(Segment) { isbn =>
        complete(books.find(_.isbn == isbn))
      } ~
      pathEndOrSingleSlash {
        complete(books)
      }
    } ~
    post {
      entity(as[Book]) { book =>
        books = books :+ book
        complete(StatusCodes.Created)
      } ~ complete(StatusCodes.BadRequest)
    }
  }

}
