package nerd.tuxmobil.fahrplan.congress.schedule

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MINUTES_OF_ONE_DAY
import nerd.tuxmobil.fahrplan.congress.models.Session

// TODO Use Moment class, merge with ConferenceTimeFrame class?
data class Conference(

        val firstSessionStartsAt: Moment,
        val lastSessionEndsAt: Moment,
        val spansMultipleDays: Boolean

) {

    companion object {

        /**
         * Creates a [Conference] from the given chronologically sorted [sessions].
         */
        @JvmStatic
        fun ofSessions(sessions: List<Session>): Conference {
            require(sessions.isNotEmpty()) { "Empty list of sessions." }
            val first = Moment.ofEpochMilli(sessions.first().dateUTC)
            // TODO Replace with sessions.first().toStartsAtMoment() once Session#relStartTime is no longer used.
            val endingLatest = sessions.endingLatest()
            val endsAt = endingLatest.endsAtDateUtc
            val last = Moment.ofEpochMilli(endsAt)
            val minutesToAdd = if (first.monthDay == last.monthDay) 0 else MINUTES_OF_ONE_DAY
            val veryLast = last.plusMinutes(minutesToAdd.toLong())
            return Conference(
                    firstSessionStartsAt = first,
                    lastSessionEndsAt = veryLast,
                    spansMultipleDays = minutesToAdd > 0
            )
        }

    }

}

/**
 * Returns the [Session] which ends the latest compared to all other [sessions][this].
 */
private fun List<Session>.endingLatest(): Session {
    var endsAt = 0L
    var latestSession = first()
    map { it to it.endsAtDateUtc }.forEach { (session, sessionEndsAt) ->
        if (endsAt == 0L || sessionEndsAt > endsAt) {
            latestSession = session
            endsAt = sessionEndsAt
        }
    }
    return latestSession
}
