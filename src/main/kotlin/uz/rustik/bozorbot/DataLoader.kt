package uz.rustik.bozorbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val userService: UserService,
    @Value("\${root.username}")
    private val username: String,
    @Value("\${root.password}")
    private val password: String
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        var rootUser = userService.findByUsername(username)
        if (rootUser == null) {
            rootUser =
                User(username, password, roles = mutableListOf(Role.ROOT.name, Role.BOSS.name, Role.MODERATOR.name))
            userService.save(rootUser)
        }
    }
}