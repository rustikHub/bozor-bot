package uz.rustik.bozorbot

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uz.ugnis.tgbotlib.Chat
import java.util.*
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var lastModifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)

@Entity
class MyChat(
    @Id @Column(unique = true) val chatId: Long = 0,
    var chatStep: String = "/start",
    var language: String = "en",
    var isNew: Boolean = true,
    var note: String = "",
    @ManyToOne var user: User? = null
)

@Entity(name = "users")
class User(
    @Column(unique = true) val userName: String,
    val password: String,
    var language: String = "en",
    @ElementCollection var roles: MutableList<String>,
    @OneToMany(mappedBy = "user") var chats: List<MyChat>? = null,
    @OneToMany(mappedBy = "user") var myChats: MutableList<MyChat>? = null,
    @ManyToOne() val shop: Shop,
    @OneToMany(mappedBy = "", fetch = FetchType.LAZY) val shops: List<Shop>,
    @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY) val orders: List<Order>
) : BaseEntity()

@Entity
class Shop(
    val name: String,
    @ManyToOne val boss: User,
    @OneToMany(mappedBy = "shop", fetch = FetchType.LAZY) val workers: List<User>
) : BaseEntity()

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