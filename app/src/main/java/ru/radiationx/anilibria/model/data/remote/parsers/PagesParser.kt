package ru.radiationx.anilibria.model.data.remote.parsers

import ru.radiationx.anilibria.entity.app.page.PageLibria
import ru.radiationx.anilibria.model.data.remote.IApiUtils
import java.util.regex.Pattern

/**
 * Created by radiationx on 13.01.18.
 */
class PagesParser(private val apiUtils: IApiUtils) {

    private val pagePatternSource = "(<div[^>]*?class=\"[^\"]*?libria_static_page[^\"]*?\"[^>]*?>[\\s\\S]*?<\\/div>)[^<]*?(?:<\\/?br[^>]*?>[^<]*?)?(?=<div[^>]*?class=\"[^\"]*?libria_static_page[^\"]*?\"[^>]*?>|<\\/article>)"
    private val titlePatternSource = "<title>([\\s\\S]*?)<\\/title>"

    private val pagePattern: Pattern by lazy {
        Pattern.compile(pagePatternSource, Pattern.CASE_INSENSITIVE)
    }

    private val titlePattern: Pattern by lazy {
        Pattern.compile(titlePatternSource, Pattern.CASE_INSENSITIVE)
    }

    fun baseParse(httpResponse: String): PageLibria {
        val result = PageLibria()
        var matcher = pagePattern.matcher(httpResponse)
        var content = ""
        while (matcher.find()) {
            content += matcher.group(1)
        }
        matcher = titlePattern.matcher(httpResponse)
        if (matcher.find()) {
            result.title = matcher.group(1)
        }
        result.content = content
        return result
    }
}