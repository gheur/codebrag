package com.softwaremill.codebrag.dao.reporting

import org.scalatest.matchers.ShouldMatchers
import com.softwaremill.codebrag.dao.eventstream.EventRecord
import org.joda.time.{DateTimeZone, DateTime}
import com.softwaremill.codebrag.domain.reactions.{LikeEvent, CommentAddedEvent, CommitReviewedEvent}
import com.softwaremill.codebrag.dao.events.NewUserRegistered
import org.bson.types.ObjectId
import com.softwaremill.codebrag.test.{FlatSpecWithMongo, ClearMongoDataAfterTest}

class StatsEventsFinderSpec extends FlatSpecWithMongo with ClearMongoDataAfterTest with ShouldMatchers {

  val finder = new StatsEventsFinder
  val today = DateTime.now(DateTimeZone.UTC)
  val yesterday = today.minusDays(1).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).withMillisOfSecond(999)

  val CommitReviewedEventType = CommitReviewedEvent.EventType
  val CommentAddedEventType = CommentAddedEvent.EventType
  val LikeAddedEventType = LikeEvent.EventType
  val NewUserRegisteredEventType = NewUserRegistered.EventType

  override def beforeEach() {

  }

  it should "get number of reviewed commits for given day only" in {
    // given
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(today, CommitReviewedEventType)
    storeEventWith(yesterday, CommitReviewedEventType)

    // when
    val count = finder.reviewedCommitsCount(today)

    // then
    count should be(2)
  }

  it should "get number of likes for given day only" in {
    // given
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(today, LikeAddedEventType)
    storeEventWith(yesterday, LikeAddedEventType)

    storeEventWith(today, CommentAddedEventType)
    storeEventWith(yesterday, CommentAddedEventType)

    // when
    val likesCount = finder.likesCount(today)
    val commentsCount = finder.commentsCount(today)

    // then
    likesCount should be(2)
    commentsCount should be(1)
  }

  it should "get number of registered users for given day only" in {
    // given
    storeEventWith(today, NewUserRegisteredEventType)
    storeEventWith(yesterday, LikeAddedEventType)

    // when
    val count = finder.registeredUsersCount(today)

    // then
    count should be(1)
  }

  it should "get number of active users (without registered)" in {
    // given
    storeEventWith(today, NewUserRegisteredEventType, userId = Some(new ObjectId))
    storeEventWith(today, LikeAddedEventType, userId = Some(new ObjectId))
    storeEventWith(today, CommentAddedEventType, userId = Some(new ObjectId))

    // when
    val count = finder.activeUsersCount(today)

    // then
    count should be(2)
  }

  it should "count user as active only once" in {
    // given
    val user = new ObjectId
    storeEventWith(today, LikeAddedEventType, userId = Some(user))
    storeEventWith(today, CommentAddedEventType, userId = Some(user))

    // when
    val count = finder.activeUsersCount(today)

    // then
    count should be(1)

  }

  private def storeEventWith(date: DateTime, eventType: String, userId: Option[ObjectId] = None) = EventRecord.createRecord.date(date.toDate).originatingUserId(userId).eventType(eventType).save
}
