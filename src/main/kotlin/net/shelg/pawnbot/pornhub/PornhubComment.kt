package net.shelg.pawnbot.pornhub

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
class PornhubComment(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @ManyToOne
        @JoinColumn
        val video: PornhubVideo,

        @Column(length = 4096)
        val text: String,

        @Column(length = 256)
        val user: String,

        val numWords: Int,
        var numUsages: Int,

        @OneToMany(mappedBy = "comment", cascade = [CascadeType.ALL])
        var usages: List<PornhubCommentUsage> = listOf()
) {
    override fun toString(): String {
        return "PornhubComment(id=$id, videoId=${video.id}, text='$text', numWords=$numWords, numUsages=$numUsages)"
    }
}

@Repository
interface PornhubCommentRepository : CrudRepository<PornhubComment, Long> {
    fun countByVideoGayEqualsAndNumUsagesEquals(gay: Boolean, numUsages: Int): Long

    fun countByVideoGayEqualsAndNumWordsBetweenAndNumUsagesEquals(
            gay: Boolean,
            minNumWords: Int,
            maxNumWords: Int,
            numUsages: Int
    ): Long

    fun findByVideoGayEqualsAndNumWordsBetweenAndNumUsagesEquals(
            gay: Boolean,
            minNumWords: Int,
            maxNumWords: Int,
            numUsages: Int,
            pageable: Pageable
    ): Page<PornhubComment>
}