package ru.radiationx.anilibria.model.data.remote.api

import io.reactivex.Single
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import ru.radiationx.anilibria.model.data.remote.IClient
import ru.radiationx.anilibria.model.data.remote.parsers.ReleaseParser

/* Created by radiationx on 31.10.17. */

class ReleaseApi(
        private val client: IClient,
        apiUtils: IApiUtils
) {

    private val releaseParser = ReleaseParser(apiUtils)

    fun getRelease(releaseId: Int): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_ID" to releaseId.toString()
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.release(it) }
    }

    fun getRelease(releaseIdName: String): Single<ReleaseFull> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "release",
                "ELEMENT_CODE" to releaseIdName
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.release(it) }
    }

    fun getGenres(): Single<List<GenreItem>> {
        val args: MutableMap<String, String> = mutableMapOf("action" to "tags")
        return client.get(Api.API_URL, args)
                .map { releaseParser.genres(it) }
    }

    fun getReleases(page: Int): Single<Paginated<List<ReleaseItem>>> {
        val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        return client.get(Api.API_URL, args)
                .map { releaseParser.releases(it) }
    }

    fun getFavorites(page: Int): Single<Paginated<List<ReleaseItem>>> {
        //val args: MutableMap<String, String> = mutableMapOf("PAGEN_1" to page.toString())
        val args: MutableMap<String, String> = mutableMapOf("SHOWALL_1" to "1")
        return client.get("${Api.BASE_URL}/izbrannoe.php", args)
                .map { releaseParser.favorites(it) }
    }

    fun getFavorites2(): Single<FavoriteData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "SHOWALL_1" to "1",
                "action" to "favorites"
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.favorites2(it) }
    }

    fun deleteFavorite(id: Int, sessId: String): Single<FavoriteData> {
        val args: MutableMap<String, String> = mutableMapOf(
                "SHOWALL_1" to "1",
                "action" to "favorites",
                "a" to "",
                "sessid" to sessId,
                "del" to id.toString()
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.favorites2(it) }
    }

    fun getComments(id: Int, page: Int): Single<Paginated<List<Comment>>> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to "comments",
                "id" to id.toString(),
                "from" to "release",
                "PAGEN_1" to page.toString()
        )
        return client.get(Api.API_URL, args)
                .map { releaseParser.comments(it) }
    }

    fun sendFav(id: Int, isFaved: Boolean, sessId: String, sKey: String): Single<Int> {
        val args: MutableMap<String, String> = mutableMapOf(
                "action" to if (isFaved) "like" else "unlike",
                "id" to id.toString(),
                "sessid" to sessId,
                "key" to sKey,
                "type" to "unknown"
        )
        return client.get("${Api.BASE_URL}/bitrix/tools/asd_favorite.php", args)
                .map { releaseParser.favXhr(it) }
    }

    /*id:5620
    action:unlike
    sessid:c5d7f1327a736c710cc3957d953ae06d
    type:unknown
    key:8313132f7b58b3424bca9d3629993929*/

}
