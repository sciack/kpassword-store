package passwordStore

import passwordStore.users.Roles
import passwordStore.users.User


fun testUser(): User = User(
    id = 0,
    email = "test@example.com",
    fullName = "test user",
    userid = "testUser",
    roles = setOf(Roles.NormalUser),
    token = "dfafasd"
)