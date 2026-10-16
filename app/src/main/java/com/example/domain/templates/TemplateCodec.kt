package com.example.domain.templates

import com.example.data.local.RuleTemplateEntity
import org.json.JSONArray
import org.json.JSONObject

object TemplateCodec {

    fun encode(templates: List<RuleTemplateEntity>, removePersonalNumbers: Boolean): String {
        val root = JSONObject()
        root.put("format", "smsgram-templates")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val array = JSONArray()
        for (t in templates) {
            val obj = JSONObject()
            obj.put("name", t.name)
            obj.put("description", t.description)

            var senderPattern = t.senderPattern
            if (removePersonalNumbers && TemplateValidator.isPersonalMobileNumber(senderPattern)) {
                senderPattern = ""
            }
            obj.put("senderPattern", senderPattern)
            obj.put("targetFolderId", t.targetFolderId)

            val kwList = if (t.keywords.isBlank()) emptyList() else t.keywords.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            val kwArray = JSONArray()
            for (kw in kwList) {
                kwArray.put(kw)
            }
            obj.put("keywords", kwArray)
            obj.put("isRegex", t.isRegex)

            array.put(obj)
        }
        root.put("templates", array)
        return root.toString(4)
    }

    fun decode(
        text: String,
        existingChecksums: Set<String> = emptySet(),
        existingFolderIds: Set<String> = emptySet()
    ): DecodeResult {
        return TemplateValidator.decode(text, existingChecksums, existingFolderIds)
    }
}
