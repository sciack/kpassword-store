package passwordStore

import passwordStore.services.Service
import passwordStore.users.Roles
import passwordStore.users.User


fun testUser(): User = User(
    id = 0,
    email = "test@example.com",
    fullName = "test user",
    userid = "testUser",
    roles = setOf(Roles.NormalUser),
)

fun testService(service:String = "test") = Service(
    service = service,
    userid = testUser().userid,
    username = "testUser",
    password = "testPwd"
)