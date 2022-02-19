package uz.rustik.bozorbot

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.sql.ResultSet
import java.util.*
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity
class MyChat(
    @Column(unique = true) val chatId: Long = 0,
    var chatStep: String = "/start",
    var language: String = "en",
    var isNew: Boolean = true,
    var note: String = "",
    var botMessageId: Int? = null,
    @ManyToOne var user: User? = null
) : BaseEntity()

@Entity(name = "users")
class User(
    @Column(unique = true) var userName: String,
    var password: String,
    var language: String = "en",
    var blocked: Boolean = false,
    @ElementCollection(fetch = FetchType.EAGER) var roles: MutableList<String>,
    @OneToMany(mappedBy = "user") var chats: List<MyChat>? = null,
    @ManyToOne(fetch = FetchType.EAGER) var boss: User? = null,
    @OneToMany(mappedBy = "boss", fetch = FetchType.LAZY) var workers: List<User>? = null,
    @OneToMany(mappedBy = "user") var myChats: MutableList<MyChat>? = null,
    @ManyToOne() var store: Store? = null,
    @OneToMany(mappedBy = "", fetch = FetchType.LAZY) val stores: List<Store>? = null,
    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY) val orders: List<Order>? = null
) : BaseEntity() {
    companion object {
        fun toEntity(rs: ResultSet): User {
            val user = User(
                rs.getString("user_name"),
                rs.getString("password"),
                roles = mutableListOf(Role.BOSS.name),
            )
            user.id = rs.getLong("id")
            return user
        }
    }

    fun getEmoji(): String {
        return when {
            this.roles.contains(Role.ROOT) -> {
                "\uD83E\uDDD9\uD83C\uDFFB\u200D♂️"
            }
            this.roles.contains(Role.BOSS) -> {
                "\uD83E\uDDDB\uD83C\uDFFB"
            }
            this.roles.contains(Role.MODERATOR) -> {
                "\uD83D\uDC68\u200D\uD83D\uDCBC"
            }
            else -> {
                "\uD83D\uDC77"
            }
        }
    }
}

@Entity
class Store(
    var name: String,
    @Enumerated(EnumType.STRING) val storeType: StoreType = StoreType.SHOP,
    @ManyToOne val boss: User,
    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY) val workers: List<User>? = null
) : BaseEntity() {
    companion object {
        fun toEntity(rs: ResultSet, entityManager: EntityManager): Store {
            return Store(
                rs.getString("name"),
                StoreType.valueOf(rs.getString("store_type")),
                entityManager.getReference(User::class.java, rs.getLong("boss_id"))
            ).apply { id = rs.getLong("id") }
        }
    }

    fun getStoreTypeEmoji() = if (storeType != StoreType.INVENTORY) {
        "\uD83D\uDCE6"
    } else {
        "\uD83C\uDFEA"
    }

}

@Entity(name = "my_order")
class Order(
    val clientName: String,
    val clientPhoneNumber: String,
    val amount: Double,
    @OneToMany(fetch = FetchType.LAZY) val orderItems: List<OrderItem>,
    @ManyToOne val seller: User,
) : BaseEntity()

@Entity
class OrderItem(
    val quantity: Int,
    val soldPrice: Double,
    @OneToOne(fetch = FetchType.LAZY) val product: Products,
    @ManyToOne val order: Order
) : BaseEntity()

@Entity
class Products(
    @Enumerated(EnumType.STRING) val category: Category,
    val name: String = "",
    val code: String = "",
    val price: Double,
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY) val inventory: List<Inventory>
) : BaseEntity()

@Entity
class Inventory(
    val quantity: Int,
    val batch: String,
    @ManyToOne val product: Products
) : BaseEntity()