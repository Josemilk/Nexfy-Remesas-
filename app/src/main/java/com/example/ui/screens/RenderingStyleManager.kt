package com.example.ui.screens

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.StringReader

data class RenderingProperty(
    val name: String,
    val attr: String,
    val type: String, // "boolean", "color", "string", "int"
    val description: String,
    val defaultValue: String
)

data class RenderingAttribute(
    val name: String,
    val category: String,
    val value: String
)

data class MapFeatureStyle(
    val tagKey: String,
    val tagValue: String,
    val minZoom: Int = 1,
    val maxZoom: Int = 19,
    val colorHex: String,
    val strokeWidthDp: Float = 2.0f,
    val fillColorHex: String = "#00000000"
)

data class RenderingStyle(
    val name: String,
    val title: String,
    val depends: String = "default",
    val description: String = "",
    val backgroundColor: String = "#F8F9FA",
    val roadColor: String = "#FFFFFF",
    val primaryRoadColor: String = "#FEF3C7",
    val highwayColor: String = "#FDE68A",
    val buildingColor: String = "#E5E7EB",
    val waterColor: String = "#93C5FD",
    val contourColor: String = "#D97706",
    val properties: List<RenderingProperty> = emptyList(),
    val attributes: List<RenderingAttribute> = emptyList(),
    val featureStyles: List<MapFeatureStyle> = emptyList()
)

object RenderingStyleManager {

    val STYLE_DEFAULT = RenderingStyle(
        name = "default",
        title = "Estándar Vectorial",
        description = "Estilo clásico OSMAnd optimizado para navegación general en ciudad y carretera.",
        backgroundColor = "#F8FAFC",
        roadColor = "#FFFFFF",
        primaryRoadColor = "#FDE68A",
        highwayColor = "#F59E0B",
        buildingColor = "#E2E8F0",
        waterColor = "#60A5FA",
        contourColor = "#B45309",
        properties = listOf(
            RenderingProperty("showBuildings", "appMode", "boolean", "Mostrar polígonos 3D de edificios", "true"),
            RenderingProperty("showContourLines", "showContourLines", "boolean", "Mostrar curvas de nivel SRTM", "false")
        )
    )

    val STYLE_TOPO = RenderingStyle(
        name = "topo",
        title = "Topográfico / Senderismo",
        description = "Estilo con relieve, sombreado hillshade, curvas de nivel detalladas y sendas.",
        backgroundColor = "#F0FDF4",
        roadColor = "#FEF08A",
        primaryRoadColor = "#F97316",
        highwayColor = "#DC2626",
        buildingColor = "#CBD5E1",
        waterColor = "#3B82F6",
        contourColor = "#78350F",
        properties = listOf(
            RenderingProperty("showContourLines", "showContourLines", "boolean", "Mostrar curvas de nivel SRTM", "true"),
            RenderingProperty("showHillshade", "showHillshade", "boolean", "Sombreado de relieve", "true")
        )
    )

    val STYLE_NAUTICAL = RenderingStyle(
        name = "nautical",
        title = "Náutico / Marítimo",
        description = "Estilo marino con profundidades de agua, faros, boyas y batimetría.",
        backgroundColor = "#0F172A",
        roadColor = "#475569",
        primaryRoadColor = "#38BDF8",
        highwayColor = "#0284C7",
        buildingColor = "#1E293B",
        waterColor = "#0284C7",
        contourColor = "#0EA5E9",
        properties = listOf(
            RenderingProperty("showSeamarks", "showSeamarks", "boolean", "Mostrar señales marítimas y boyas", "true")
        )
    )

    val STYLE_WINTER = RenderingStyle(
        name = "winter",
        title = "Invernal / Esquí",
        description = "Estilo para deportes de invierno con pistas de esquí (azules, rojas, negras) y remontes.",
        backgroundColor = "#F1F5F9",
        roadColor = "#E2E8F0",
        primaryRoadColor = "#CBD5E1",
        highwayColor = "#94A3B8",
        buildingColor = "#CBD5E1",
        waterColor = "#93C5FD",
        contourColor = "#475569",
        properties = listOf(
            RenderingProperty("showSkiSlopes", "showSkiSlopes", "boolean", "Mostrar pistas de esquí categorizadas", "true")
        )
    )

    private val PRESET_STYLES = mapOf(
        "default" to STYLE_DEFAULT,
        "topo" to STYLE_TOPO,
        "nautical" to STYLE_NAUTICAL,
        "winter" to STYLE_WINTER
    )

    fun getAvailableStyles(): List<RenderingStyle> = PRESET_STYLES.values.toList()

    fun getStyleByName(name: String): RenderingStyle {
        return PRESET_STYLES[name.lowercase()] ?: STYLE_DEFAULT
    }

    /**
     * Parses an OSMAnd rendering.xml stream to populate a RenderingStyle object.
     */
    fun parseRenderingXml(stream: InputStream): RenderingStyle {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(stream, "UTF-8")

        var name = "custom"
        var title = "Estilo Personalizado"
        var depends = "default"
        var description = ""
        var bgColor = "#F8FAFC"
        var roadColor = "#FFFFFF"
        var primaryColor = "#FDE68A"
        var highwayColor = "#F59E0B"
        var buildingColor = "#E2E8F0"
        var waterColor = "#60A5FA"
        var contourColor = "#B45309"

        val props = mutableListOf<RenderingProperty>()
        val attrs = mutableListOf<RenderingAttribute>()
        val features = mutableListOf<MapFeatureStyle>()

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "renderingStyle" -> {
                            name = parser.getAttributeValue(null, "name") ?: name
                            title = parser.getAttributeValue(null, "title") ?: name
                            depends = parser.getAttributeValue(null, "depends") ?: depends
                            description = parser.getAttributeValue(null, "description") ?: ""
                        }
                        "renderingProperty" -> {
                            val pName = parser.getAttributeValue(null, "attr") ?: "prop"
                            val pType = parser.getAttributeValue(null, "type") ?: "boolean"
                            val pDesc = parser.getAttributeValue(null, "description") ?: ""
                            val pDef = parser.getAttributeValue(null, "defaultValue") ?: "false"
                            props.add(RenderingProperty(pName, pName, pType, pDesc, pDef))
                        }
                        "renderingAttribute" -> {
                            val aName = parser.getAttributeValue(null, "name") ?: ""
                            val aValue = parser.getAttributeValue(null, "value") ?: ""
                            attrs.add(RenderingAttribute(aName, "general", aValue))

                            when (aName) {
                                "backgroundColor" -> bgColor = aValue
                                "roadColor" -> roadColor = aValue
                                "primaryRoadColor" -> primaryColor = aValue
                                "highwayColor" -> highwayColor = aValue
                                "buildingColor" -> buildingColor = aValue
                                "waterColor" -> waterColor = aValue
                                "contourColor" -> contourColor = aValue
                            }
                        }
                        "line", "polygon" -> {
                            val tagK = parser.getAttributeValue(null, "tag") ?: ""
                            val tagV = parser.getAttributeValue(null, "value") ?: ""
                            val color = parser.getAttributeValue(null, "color") ?: "#000000"
                            val minZ = parser.getAttributeValue(null, "minzoom")?.toIntOrNull() ?: 1
                            val maxZ = parser.getAttributeValue(null, "maxzoom")?.toIntOrNull() ?: 19
                            if (tagK.isNotEmpty()) {
                                features.add(MapFeatureStyle(tagK, tagV, minZ, maxZ, color))
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return RenderingStyle(
            name = name,
            title = title,
            depends = depends,
            description = description,
            backgroundColor = bgColor,
            roadColor = roadColor,
            primaryRoadColor = primaryColor,
            highwayColor = highwayColor,
            buildingColor = buildingColor,
            waterColor = waterColor,
            contourColor = contourColor,
            properties = props,
            attributes = attrs,
            featureStyles = features
        )
    }

    /**
     * Serializes a RenderingStyle into valid OsmAnd rendering.xml structure.
     */
    fun exportToXmlString(style: RenderingStyle): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<renderingStyle name=\"${style.name}\" title=\"${style.title}\" depends=\"${style.depends}\" description=\"${style.description}\">\n")
        
        style.properties.forEach { p ->
            sb.append("  <renderingProperty attr=\"${p.attr}\" type=\"${p.type}\" description=\"${p.description}\" defaultValue=\"${p.defaultValue}\"/>\n")
        }

        sb.append("  <renderingAttribute name=\"backgroundColor\" value=\"${style.backgroundColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"roadColor\" value=\"${style.roadColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"primaryRoadColor\" value=\"${style.primaryRoadColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"highwayColor\" value=\"${style.highwayColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"buildingColor\" value=\"${style.buildingColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"waterColor\" value=\"${style.waterColor}\"/>\n")
        sb.append("  <renderingAttribute name=\"contourColor\" value=\"${style.contourColor}\"/>\n")

        style.featureStyles.forEach { f ->
            sb.append("  <line tag=\"${f.tagKey}\" value=\"${f.tagValue}\" minzoom=\"${f.minZoom}\" maxzoom=\"${f.maxZoom}\" color=\"${f.colorHex}\"/>\n")
        }

        sb.append("</renderingStyle>\n")
        return sb.toString()
    }
}
