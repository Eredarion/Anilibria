package ru.radiationx.anilibria.model.data.remote.parsers

import android.util.Log
import com.mintrocket.gisdelivery.extension.nullGet
import org.json.JSONObject
import ru.radiationx.anilibria.entity.app.Paginated
import ru.radiationx.anilibria.entity.app.release.*
import ru.radiationx.anilibria.entity.app.search.SearchItem
import ru.radiationx.anilibria.model.data.remote.Api
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by radiationx on 18.12.17.
 */
class ReleaseParser(private val apiUtils: IApiUtils) {

    private val fastSearchPatternSource = "<a[^>]*?href=\"[^\"]*?\\/release\\/[^\"]*?\"[^>]*?>[^<]*?<img[^>]*?>([\\s\\S]*?) \\/ ([\\s\\S]*?)(?:<\\/a>[^>]*?)?<\\/td>"
    private val idNamePatternSource = "\\/release\\/([\\s\\S]*?)\\.html"

    /*
    * 1.    String  Description
    * 2.    String  ELEMENT_CODE / idName
    * 3.    Int     Id
    * 4.    String  Image url
    * 5.    String  Title
    * */
    private val favoritesPatternSource = "<article[^>]*?class=\"favorites_block\"[^>]*?>[^<]*?<div[^>]*?>[^<]*?<p[^>]*?class=\"favorites_description\"[^>]*?>([\\s\\S]*?)<\\/p>[^<]*?<a[^>]*?href=\"\\/release\\/([\\s\\S]*?)\\.html\"[^>]*?>[^<]*?<\\/a>[^<]*?<a[^>]*?id=\"asd_fd_(\\d+)[^\"]*?\"[^>]*?>[^<]*?<\\/a>[^<]*?<\\/div>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?>[^<]*?<h2[^>]*?>([\\s\\S]*?)<\\/h2>"

    private val fastSearchPattern: Pattern by lazy {
        Pattern.compile(fastSearchPatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val idNamePattern: Pattern by lazy {
        Pattern.compile(idNamePatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val favoritesPattern: Pattern by lazy {
        Pattern.compile(favoritesPatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun fastSearch(httpResponse: String): List<SearchItem> {
        val result: MutableList<SearchItem> = mutableListOf()
        val matcher: Matcher = fastSearchPattern.matcher(httpResponse)
        while (matcher.find()) {
            result.add(SearchItem().apply {
                originalTitle = matcher.group(1)
                title = matcher.group(2)
            })
        }
        return result
    }

    fun genres(httpResponse: String): List<GenreItem> {
        val result: MutableList<GenreItem> = mutableListOf()
        val jsonItems = JSONObject(httpResponse).getJSONArray("data")
        for (i in 0 until jsonItems.length()) {
            val genreText = jsonItems.getString(i)
            val genreItem = GenreItem().apply {
                title = genreText.substring(0, 1).toUpperCase() + genreText.substring(1)
                value = genreText
            }
            result.add(genreItem)
        }
        return result
    }

    fun releases(httpResponse: String): Paginated<List<ReleaseItem>> {
        val resItems = mutableListOf<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = ReleaseItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")
            item.idName = jsonItem.getString("code")

            val titles = jsonItem.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }

            item.torrentLink = Api.BASE_URL + jsonItem.getString("torrent_link")
            item.link = Api.BASE_URL + jsonItem.getString("link")
            item.image = Api.BASE_URL_IMAGES + jsonItem.getString("image")
            item.episodesCount = jsonItem.getString("episode")
            item.description = jsonItem.getString("description").trim()

            val jsonSeasons = jsonItem.getJSONArray("season")
            for (j in 0 until jsonSeasons.length()) {
                item.seasons.add(jsonSeasons.getString(j))
            }

            val jsonVoices = jsonItem.getJSONArray("voices")
            for (j in 0 until jsonVoices.length()) {
                item.voices.add(jsonVoices.getString(j))
            }

            val jsonGenres = jsonItem.getJSONArray("genres")
            for (j in 0 until jsonGenres.length()) {
                item.genres.add(jsonGenres.getString(j))
            }

            val jsonTypes = jsonItem.getJSONArray("types")
            for (j in 0 until jsonTypes.length()) {
                item.types.add(jsonTypes.getString(j))
            }

            resItems.add(item)
        }
        val pagination = Paginated(resItems)
        val jsonNav = responseJson.getJSONObject("navigation")
        jsonNav.nullGet("total")?.let { pagination.total = it.toString().toInt() }
        jsonNav.nullGet("page")?.let { pagination.current = it.toString().toInt() }
        jsonNav.nullGet("total_pages")?.let { pagination.allPages = it.toString().toInt() }
        return pagination
    }

    fun release(httpResponse: String): ReleaseFull {
        val release = ReleaseFull()

        val responseJson = JSONObject(httpResponse)

        release.id = responseJson.getInt("id")
        release.idName = responseJson.getString("code")

        val titles = responseJson.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (titles.isNotEmpty()) {
            release.originalTitle = apiUtils.escapeHtml(titles[0])
            if (titles.size > 1) {
                release.title = apiUtils.escapeHtml(titles[1])
            }
        }

        responseJson.getString("torrent_link").let {
            if (!it.isNullOrEmpty()) {
                release.torrentLink = Api.BASE_URL + it
            }
        }
        release.link = Api.BASE_URL + responseJson.getString("link")
        release.image = Api.BASE_URL_IMAGES + responseJson.getString("image")
        //release.setEpisodesCount(responseJson.getString("episode"));
        release.description = responseJson.getString("description").trim()
        release.releaseStatus = responseJson.optString("release_status")
        release.isBlocked = responseJson.optBoolean("isBlocked", false)
        release.contentBlocked = responseJson.optString("contentBlocked", null)

        responseJson.optJSONArray("season")?.let {
            for (j in 0 until it.length()) {
                release.seasons.add(it.optString(j))
            }
        }
        responseJson.optJSONArray("voices")?.let {
            for (j in 0 until it.length()) {
                release.voices.add(it.optString(j))
            }
        }
        responseJson.optJSONArray("genres")?.let {
            for (j in 0 until it.length()) {
                release.genres.add(it.optString(j))
            }
        }
        responseJson.optJSONArray("types")?.let {
            for (j in 0 until it.length()) {
                release.types.add(it.optString(j))
            }
        }

        responseJson.optJSONArray("mp4")?.let { jsonMp4 ->
            for (j in 0 until jsonMp4.length()) {
                jsonMp4.optJSONObject(j)?.let {
                    val episode = ReleaseFull.Episode()
                    episode.title = "Cерия ${jsonMp4.length() - j}"
                    episode.urlSd = it.optString("sd")
                    episode.urlHd = it.optString("hd")
                    episode.type = ReleaseFull.Episode.Type.SOURCE
                    release.episodesSource.add(episode)
                }
            }
        }

        responseJson.optJSONArray("Uppod")?.let { jsonEpisodes ->
            for (j in 0 until jsonEpisodes.length()) {
                jsonEpisodes.optJSONObject(j)?.let {
                    val episode = ReleaseFull.Episode()

                    episode.releaseId = release.id
                    episode.id = jsonEpisodes.length() - j

                    episode.title = it.optString("comment")
                    episode.urlSd = it.optString("file")
                    episode.urlHd = it.optString("filehd")
                    episode.type = ReleaseFull.Episode.Type.ONLINE
                    release.episodes.add(episode)
                }
            }
        }

        responseJson.optString("Moonwalk").let {
            val matcher = Pattern.compile("<iframe[^>]*?src=\"([^\"]*?)\"[^>]*?>").matcher(it)
            if (matcher.find()) {
                var mwUrl = matcher.group(1)
                if (mwUrl.substring(0, 2) == "//") {
                    mwUrl = "https:" + mwUrl
                }
                release.moonwalkLink = mwUrl
            }
        }

        responseJson.getJSONArray("torrentList")?.let {
            for (j in 0 until it.length()) {
                it.optJSONObject(j)?.let {
                    release.torrents.add(TorrentItem().apply {
                        episode = it.getString("episode")
                        quality = it.getString("quality")
                        size = it.getString("size")
                        url = Api.BASE_URL + it.getString("url")
                    })
                }
            }
        }

        val jsonFav = responseJson.getJSONObject("fav")
        Log.e("S_DEF_LOG", "loaded json fav " + jsonFav.toString(4))
        release.favoriteCount.apply {
            id = jsonFav.getInt("id")
            count = jsonFav.getInt("count")
            isFaved = jsonFav.getBoolean("isFaved")
            sessId = jsonFav.getString("sessId")
            skey = jsonFav.getString("skey")
            isGuest = jsonFav.getBoolean("isGuest")
        }

        return release
    }

    fun favorites(httpResponse: String): Paginated<List<ReleaseItem>> {
        val resItems = mutableListOf<ReleaseItem>()
        val matcher = favoritesPattern.matcher(httpResponse)
        while (matcher.find()) {
            val item = ReleaseItem()
            item.description = apiUtils.escapeHtml(matcher.group(1))
            item.idName = matcher.group(2)
            item.id = matcher.group(3).toInt()
            item.image = Api.BASE_URL_IMAGES + matcher.group(4)

            val titles = matcher.group(5).split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }
            resItems.add(item)
        }
        /*val jsonNav = responseJson.getJSONObject("navigation")
        pagination.total = jsonNav.get("total").toString().toInt()
        pagination.current = jsonNav.get("page").toString().toInt()
        pagination.allPages = jsonNav.get("total_pages").toString().toInt()*/
        return Paginated(resItems)
    }

    fun favorites2(httpResponse: String): FavoriteData {
        val resItems = mutableListOf<ReleaseItem>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = ReleaseItem()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")

            val matcher = idNamePattern.matcher(jsonItem.getString("link"))
            if (matcher.find()) {
                item.idName = matcher.group(1)
            }

            item.description = jsonItem.getString("description")
            item.image = Api.BASE_URL_IMAGES + jsonItem.get("image")

            val titles = jsonItem.getString("title").split(" / ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (titles.isNotEmpty()) {
                item.originalTitle = apiUtils.escapeHtml(titles[0])
                if (titles.size > 1) {
                    item.title = apiUtils.escapeHtml(titles[1])
                }
            }
            resItems.add(item)
        }
        val result = FavoriteData()
        result.sessId = responseJson.getString("sessId")
        result.items = Paginated(resItems)
        return result
    }

    fun comments(httpResponse: String): Paginated<List<Comment>> {
        val resItems = mutableListOf<Comment>()
        val responseJson = JSONObject(httpResponse)
        val jsonItems = responseJson.getJSONArray("items")
        for (i in 0 until jsonItems.length()) {
            val item = Comment()
            val jsonItem = jsonItems.getJSONObject(i)
            item.id = jsonItem.getInt("id")
            item.forumId = jsonItem.getInt("forumId")
            item.topicId = jsonItem.getInt("topicId")
            item.date = jsonItem.getString("postDate")
            item.message = jsonItem.getString("postMessage")
            item.authorId = jsonItem.getInt("authorId")
            item.authorNick = jsonItem.getString("authorName")
            item.avatar = Api.BASE_URL_IMAGES + jsonItem.getString("avatar")
            item.userGroup = jsonItem.optInt("userGroup", 0)
            item.userGroupName = jsonItem.optString("userGroupName", null)
            resItems.add(item)
        }
        val pagination = Paginated(resItems)
        val jsonNav = responseJson.getJSONObject("navigation")
        jsonNav.nullGet("total")?.let { pagination.total = it.toString().toInt() }
        jsonNav.nullGet("page")?.let { pagination.current = it.toString().toInt() }
        jsonNav.nullGet("total_pages")?.let { pagination.allPages = it.toString().toInt() }
        return pagination
    }

    fun favXhr(httpResponse: String): Int {
        Log.e("S_DEF_LOG", "favXhr " + httpResponse)
        return JSONObject(httpResponse).getInt("COUNT")
    }
}
