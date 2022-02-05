package uz.rustik.bozorbot

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun trash(id: Long): T
    fun findAllNotDeleted(): List<T>
}

interface UserRepository : BaseRepository<User> {
    fun findByUserNameAndPasswordAndDeletedFalse(userName: String, password: String): User?
    fun existsByUserNameAndDeletedFalse(userName: String): Boolean
}

interface ChatRepository : JpaRepository<MyChat, Long> {
    fun findByChatId(telegramId: Long): MyChat?
}

interface ShopRepository : BaseRepository<Shop> {
}

interface OrderRepository : BaseRepository<Order> {
}

interface OrderItemRepository : BaseRepository<OrderItem> {
}

interface ProductsRepository : BaseRepository<Products> {
}

interface InventoryRepository : BaseRepository<Inventory> {
}