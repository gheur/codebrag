package com.softwaremill.codebrag.rest

import com.softwaremill.codebrag.AuthenticatableServletSpec
import com.softwaremill.codebrag.service.user.{RegisterService, Authenticator}
import com.softwaremill.codebrag.service.user.UserJsonBuilder._
import org.scalatra.auth.Scentry
import com.softwaremill.codebrag.service.data.UserJson
import org.mockito.Mockito._
import org.json4s.JsonDSL._
import com.softwaremill.codebrag.service.config.CodebragConfig
import com.typesafe.config.ConfigFactory
import java.util.Properties
import com.softwaremill.codebrag.activities.{ModifyUserDetailsUseCase, UserToRegister, RegisterNewUserUseCase}
import com.softwaremill.codebrag.dao.ObjectIdTestUtils
import com.softwaremill.codebrag.finders.user.{ManagedUserView, ManagedUsersListView, UserFinder}

class UsersServletSpec extends AuthenticatableServletSpec {

  val registerService = mock[RegisterService]
  val registerUseCase = mock[RegisterNewUserUseCase]
  val modifyUserUseCase = mock[ModifyUserDetailsUseCase]
  var userFinder: UserFinder = _
  var config: CodebragConfig = _

  override def beforeEach() {
    super.beforeEach()
    userFinder = mock[UserFinder]
    config = configWithDemo(false)
  }

  "GET /" should "return empty list of registered users if in demo mode" in {
    config = configWithDemo(true)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(someUser)
    get("/") {
      status should be(200)
      val expectedBody = ManagedUsersListView(users = List.empty)
      body should be(asJson(expectedBody))
    }
  }

  "GET /" should "return actual list of registered users if not in demo mode" in {
    val user = ManagedUserView(ObjectIdTestUtils.oid(100), "john@doe.com", "John Doe", active = true, admin = true)
    val registeredUsers = ManagedUsersListView(List(user))
    when(userFinder.findAllAsManagedUsers()).thenReturn(registeredUsers)
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    userIsAuthenticatedAs(someUser)
    get("/") {
      status should be(200)
      body should be(asJson(registeredUsers))
    }
  }


  def configWithDemo(mode: Boolean) = {
    val p = new Properties()
    p.setProperty("codebrag.demo", mode.toString)
    new CodebragConfig {
      def rootConfig = ConfigFactory.parseProperties(p)
    }
  }

  "GET /first-registration" should "return firstRegistration flag" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    //given
    val currentUser = someUser
    userIsAuthenticatedAs(currentUser)
    when(registerService.firstRegistration).thenReturn(true)
    //when
    get("/first-registration") {
      //then
      status should be(200)
      body should be("{\"firstRegistration\":true}")
    }
  }

  "POST /register" should "call the register service and return 200 if registration is successful" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val newUser = UserToRegister("adamw", "adam@example.org", "123456", "code")
    when(registerUseCase.execute(newUser)).thenReturn(Right())

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(200)
    }
  }

  "POST /register" should "call the register service and return 403 if registration is unsuccessful" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val newUser = UserToRegister("adamw", "adam@example.org", "123456", "code")
    when(registerUseCase.execute(newUser)).thenReturn(Left("error"))

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456", "invitationCode" -> "code")),
      defaultJsonHeaders) {
      status should be(403)
    }
  }

  "POST /register" should "fallback to empty registration code when one not provided in request" in {
    addServlet(new TestableUsersServlet(fakeAuthenticator, fakeScentry), "/*")
    val newUser = UserToRegister("adamw", "adam@example.org", "123456", "")
    when(registerUseCase.execute(newUser)).thenReturn(Right())

    post("/register",
      mapToJson(Map("login" -> "adamw", "email" -> "adam@example.org", "password" -> "123456")), defaultJsonHeaders) {
      verify(registerUseCase).execute(newUser)
    }
  }

  class TestableUsersServlet(fakeAuthenticator: Authenticator, fakeScentry: Scentry[UserJson])
    extends UsersServlet(fakeAuthenticator, registerService, registerUseCase, userFinder, modifyUserUseCase, config) {
    override def scentry(implicit request: javax.servlet.http.HttpServletRequest) = fakeScentry
  }

}
