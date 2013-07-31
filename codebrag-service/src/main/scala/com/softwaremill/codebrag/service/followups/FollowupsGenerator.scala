package com.softwaremill.codebrag.service.followups

import com.softwaremill.codebrag.dao.{FollowupWithReactionsDAO, CommitInfoDAO, UserDAO, FollowupDAO}
import akka.actor.Actor
import com.softwaremill.codebrag.domain.reactions.{UnlikeEvent, LikeEvent}

class FollowupsGenerator(followupDaoArg: FollowupDAO, userDaoArg: UserDAO, commitInfoDaoArg: CommitInfoDAO, followupWithReactionsDaoArg: FollowupWithReactionsDAO) extends Actor with FollowupsGeneratorActions {

  override def followupDao: FollowupDAO = followupDaoArg
  override def userDao: UserDAO = userDaoArg
  override def commitDao: CommitInfoDAO = commitInfoDaoArg
  override def followupWithReactionsDao: FollowupWithReactionsDAO = followupWithReactionsDaoArg


  def receive = {
    case (event: LikeEvent) => handleCommitLiked(event)
    case (event: UnlikeEvent) => handleUnlikeEvent(event)
  }

}
