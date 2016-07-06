package org.elkdanger.testing

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.{UpdateWriteResult, WriteConcern, WriteResult}
import reactivemongo.api.indexes.CollectionIndexesManager
import reactivemongo.api.{CollectionProducer, DefaultDB, FailoverStrategy}
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

trait MongoMocks extends MongoMockFinds with MockitoSugar {

  implicit val mockMongoApi = mock[ReactiveMongoApi]
  implicit val mockMongoDb = mock[DefaultDB]

  when(mockMongoApi.database).thenReturn(Future.successful(mockMongoDb))

  def mockCollection(name: Option[String] = None)(implicit db: DefaultDB, ec: ExecutionContext): JSONCollection = {
    val collection = mock[JSONCollection]

    val matcher = name match {
      case Some(x) => eqTo(x)
      case _ => any()
    }

    when(db.collection(matcher, any[FailoverStrategy])
      (any[CollectionProducer[JSONCollection]]()))
      .thenReturn(collection)

    val mockIndexManager = mock[CollectionIndexesManager]
    when(mockIndexManager.ensure(any())).thenReturn(Future.successful(true))
    when(collection.indexesManager).thenReturn(mockIndexManager)

    setupAnyUpdateOn(collection)
    setupAnyInsertOn(collection)

    collection
  }

  def mockWriteResult(fails: Boolean = false) = {
    val m = mock[WriteResult]
    when(m.ok).thenReturn(!fails)
    m
  }

  def mockUpdateWriteResult(fails: Boolean = false) = {
    val m = mock[UpdateWriteResult]
    when(m.ok).thenReturn(!fails)
    m
  }

  def verifyAnyInsertOn(collection: JSONCollection) = {
    verify(collection).insert(any, any())(any(), any())
  }

  def verifyInsertWith[T](collection: JSONCollection, obj: T) = {
    verify(collection).insert(eqTo(obj), any())(any(), any())
  }

  def verifyInsertWith[T](collection: JSONCollection, captor: ArgumentCaptor[T]) = {
    verify(collection).insert(captor.capture(), any[WriteConcern])(any(), any[ExecutionContext])
  }

  def verifyUpdateOn[T](collection: JSONCollection, filter: (JsObject) => Unit = null, update: (JsObject) => Unit = null) = {
    val filterCaptor = ArgumentCaptor.forClass(classOf[JsObject])
    val updaterCaptor = ArgumentCaptor.forClass(classOf[JsObject])

    verify(collection).update(filterCaptor.capture(), updaterCaptor.capture(), any[WriteConcern], anyBoolean(), anyBoolean())(any(), any(), any[ExecutionContext])

    if (filter != null)
      filter(filterCaptor.getValue)

    if (update != null)
      update(updaterCaptor.getValue)
  }

  def verifyAnyUpdateOn[T](collection: JSONCollection) = {
    verify(collection).update(any(), any(), any[WriteConcern], anyBoolean(), anyBoolean())(any(), any(), any[ExecutionContext])
  }



  def setupInsertOn[T](collection: JSONCollection, obj: T, fails: Boolean = false) = {
    val m = mockWriteResult(fails)
    when(collection.insert(eqTo(obj), any())(any(), any()))
      .thenReturn(Future.successful(m))
  }

  def setupAnyInsertOn(collection: JSONCollection, fails: Boolean = false) = {
    val m = mockWriteResult(fails)
    when(collection.insert(any(), any())(any(), any()))
      .thenReturn(Future.successful(m))
  }

  def setupAnyUpdateOn(collection: JSONCollection, fails: Boolean = false) = {
    val m = mockUpdateWriteResult(fails)
    when(
      collection.update(any(), any(), any(), anyBoolean, anyBoolean)(any(), any(), any[ExecutionContext])
    ) thenReturn Future.successful(m)
  }
}
