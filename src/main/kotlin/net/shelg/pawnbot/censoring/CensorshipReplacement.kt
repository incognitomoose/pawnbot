package net.shelg.pawnbot.censoring

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class CensorshipReplacement(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        val guildId: Long,
        val groupKey: String,
        val matchPhrase: String,
        val replacementPhrase: String
)

@Repository
interface CensorshipReplacementRepository : CrudRepository<CensorshipReplacement, Long> {
    fun findAllByGuildId(guildId: Long): List<CensorshipReplacement>
    fun findByGuildIdAndGroupKeyAndMatchPhrase(guildId: Long, groupKey: String, matchPhrase: String): CensorshipReplacement?
}
