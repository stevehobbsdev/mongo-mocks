import play.api.libs.json.Json
import play.modules.reactivemongo.json._

class ApiSpec extends SpecBase {

  case class TestObject(id: String)

  object TestObject {
    implicit val formats = Json.format[TestObject]
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  describe("The mock collection api") {

    it ("should be able to create a mock collection") {
      val collection = mockCollection()
      collection should not be Nil
    }

    describe ("the find methods") {

      val collection = mockCollection()

      it ("should be able to mock a find for any object") {

        val obj = mock[TestObject]

        collection.setupFind(Some(obj))

        val result = await(collection.find(Json.obj()).one[TestObject])

        result should be(Some(obj))
      }

      it ("should be able to mock a find for a list of objects") {

        val list = List(mock[TestObject], mock[TestObject])

        collection.setupFind(list)

        val result = await(collection.find(Json.obj()).cursor[TestObject]().collect[List]())

        result should be(list)
      }

      it ("should be able to mock a find for an object, with a filter") {

        val obj = mock[TestObject]

        collection.setupFind(Json.obj("someProp" -> 12), Some(obj))

        val result = await(collection.find(Json.obj("someProp" -> 12)).one[TestObject])

        result should be(Some(obj))
      }
      
      it ("should be able to mock a find for a list, with a filter") {
        val list = List(mock[TestObject], mock[TestObject])

        collection.setupFind(Json.obj("someProp" -> 12), list)

        val result = await(collection.find(Json.obj("someProp" -> 12)).cursor[TestObject]().collect[List]())

        result should be(list)
      }

    }

  }

}
