package ru.radiationx.anilibria.model.data.holders

import io.reactivex.Observable
import ru.radiationx.anilibria.entity.app.release.ReleaseItem

interface HistoryHolder {
    fun observeEpisodes(): Observable<MutableList<ReleaseItem>>
    fun getReleases(): List<ReleaseItem>
    fun putRelease(release: ReleaseItem)
}