package com.softwaremill.codebrag.dao.reaction

import com.softwaremill.codebrag.dao.sql.{SQLDatabase, WithSQLSchemas}
import com.softwaremill.codebrag.domain.{Comment, ThreadDetails}
import org.bson.types.ObjectId
import scala.slick.driver.JdbcProfile

class SQLCommitCommentDAO(val database: SQLDatabase) extends CommitCommentDAO with WithSQLSchemas with SQLReactionSchema {
  import database.driver.simple._
  import database._

  def save(comment: Comment) = db.withTransaction { implicit session =>
    comments += comment
  }

  def findCommentsForCommits(commitId: ObjectId*): List[Comment] = db.withTransaction { implicit session =>
    comments
      .filter(_.commitId inSet commitId.toSet)
      .sortBy(_.postingTime.asc)
      .list()
  }

  def findAllCommentsForThread(thread: ThreadDetails): List[Comment] = db.withTransaction { implicit session =>
    comments
      .filter(c => c.commitId === thread.commitId && positionFilter(thread, c))
      .list()
  }

  def schemas: Iterable[JdbcProfile#DDLInvoker] = List(comments.ddl)
}
