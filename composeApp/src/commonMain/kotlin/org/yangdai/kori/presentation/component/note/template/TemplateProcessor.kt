package org.yangdai.kori.presentation.component.note.template

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime

class TemplateProcessor(
    defaultDateFormat: String = "yyyy-MM-dd",
    defaultTimeFormat: String = "HH:mm"
) {
    private var defaultDateFormat: String =
        defaultDateFormat.takeIf { it.isNotBlank() } ?: "yyyy-MM-dd"
    private var defaultTimeFormat: String = defaultTimeFormat.takeIf { it.isNotBlank() } ?: "HH:mm"

    // 修改正则表达式以先匹配简单格式
    private val simpleDatePattern = "\\{\\{date\\}\\}"
    private val simpleTimePattern = "\\{\\{time\\}\\}"
    private val dateWithFormatPattern = "\\{\\{date:([^}]+)\\}\\}"
    private val timeWithFormatPattern = "\\{\\{time:([^}]+)\\}\\}"

    @OptIn(FormatStringsInDatetimeFormats::class)
    fun process(content: String): String {
        if (content.isBlank()) return content

        val localeDateTime =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val localDate = localeDateTime.date
        val localTime = localeDateTime.time

        var result = content

        // 先处理简单格式
        result = result.replace(simpleDatePattern.toRegex()) {
            try {
                localDate.format(LocalDate.Format { byUnicodePattern(defaultDateFormat) })
            } catch (_: Exception) {
                localDate.format(LocalDate.Format { byUnicodePattern("yyyy-MM-dd") })
            }
        }

        result = result.replace(simpleTimePattern.toRegex()) {
            try {
                localTime.format(LocalTime.Format { byUnicodePattern(defaultTimeFormat) })
            } catch (_: Exception) {
                localTime.format(LocalTime.Format { byUnicodePattern("HH:mm") })
            }
        }

        // 再处理带格式的模式
        result = result.replace(dateWithFormatPattern.toRegex()) { matchResult ->
            val format = matchResult.groupValues[1]
            try {
                localDate.format(LocalDate.Format { byUnicodePattern(format) })
            } catch (_: Exception) {
                localDate.format(LocalDate.Format { byUnicodePattern("yyyy-MM-dd") })
            }
        }

        result = result.replace(timeWithFormatPattern.toRegex()) { matchResult ->
            val format = matchResult.groupValues[1]
            try {
                localTime.format(LocalTime.Format { byUnicodePattern(format) })
            } catch (_: Exception) {
                localTime.format(LocalTime.Format { byUnicodePattern("HH:mm") })
            }
        }

        return result
    }
}