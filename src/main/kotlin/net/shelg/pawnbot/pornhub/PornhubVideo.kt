package net.shelg.pawnbot.pornhub

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
data class PornhubVideo(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        val gay: Boolean,

        @Column(length = 1024)
        val title: String,
        val url: String,

        @OneToMany(mappedBy = "video", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
        var comments: List<PornhubComment> = listOf()
) {
    val summarized get() = "${if (gay) "gay" else "straight"} video \"$title\" with ${comments.size} comments"
}

@Repository
interface PornhubVideoRepository : CrudRepository<PornhubVideo, Long>