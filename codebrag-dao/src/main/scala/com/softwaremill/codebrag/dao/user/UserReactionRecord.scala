package com.softwaremill.codebrag.dao.user

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import net.liftweb.mongodb.record.field.{ObjectIdPk, DateField, ObjectIdField}
import net.liftweb.record.field.OptionalIntField
import com.softwaremill.codebrag.dao.mongo.{OptionalLongStringField, LongStringField}

trait UserReactionRecord[MyType <: MongoRecord[MyType]] {

  self: MongoRecord[MyType] =>

  object commitId extends ObjectIdField(self.asInstanceOf[MyType])

  object authorId extends ObjectIdField(self.asInstanceOf[MyType])

  object date extends DateField(self.asInstanceOf[MyType])

  object fileName extends OptionalLongStringField(self.asInstanceOf[MyType])

  object lineNumber extends OptionalIntField(self.asInstanceOf[MyType])

}



class CommentRecord extends MongoRecord[CommentRecord] with ObjectIdPk[CommentRecord] with UserReactionRecord[CommentRecord] {

  def meta = CommentRecord

  object message extends LongStringField(this)

}

object CommentRecord extends CommentRecord with MongoMetaRecord[CommentRecord] {
  override def collectionName = "commit_comments"
}




class LikeRecord extends MongoRecord[LikeRecord] with ObjectIdPk[LikeRecord] with UserReactionRecord[LikeRecord] {

  def meta = LikeRecord

}

object LikeRecord extends LikeRecord with MongoMetaRecord[LikeRecord] {
  override def collectionName = "commit_likes"
}