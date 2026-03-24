package com.myoshita.bookshelf.data.api

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.UnknownChildHandler
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue
import org.koin.dsl.module

val ndlApiManagerModule = module {
    single<NdlApiManager> { NdlApiManagerImpl(get()) }
}

interface NdlApiManager : BookApiManager<SearchRetrieveResponse>

private class NdlApiManagerImpl(
    private val client: HttpClient,
) : NdlApiManager {

    @OptIn(ExperimentalXmlUtilApi::class)
    private val xml = XML {
        autoPolymorphic = true
        defaultPolicy {
            ignoreUnknownChildren()
            ignoreNamespaces()
            unknownChildHandler = UnknownChildHandler { _, _, _, _, _ -> emptyList() }
        }
    }


    override suspend fun getBook(isbn: String): SearchRetrieveResponse {
        val url =
            "https://ndlsearch.ndl.go.jp/api/sru?operation=searchRetrieve&recordSchema=dcndl&onlyBib=true&recordPacking=xml&query=isbn=$isbn"
        try {
            val response = client.get(url)
            if (response.status == HttpStatusCode.OK) {
                val xmlString = response.bodyAsText()
                Napier.d(xmlString)
                val searchRetrieveResponse = xml.decodeFromString<SearchRetrieveResponse>(xmlString)
                return searchRetrieveResponse
            } else {
                throw Exception("${response.status}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}

@Serializable
@SerialName("searchRetrieveResponse")
data class SearchRetrieveResponse(
    @XmlElement(true)
    val records: Records?
) {
    @Serializable
    @SerialName("records")
    data class Records(
        @XmlElement(true)
        val record: Record,
    )

    @Serializable
    @SerialName("record")
    data class Record(
        @XmlElement(true)
        val recordData: RecordData
    )

    @Serializable
    @SerialName("recordData")
    data class RecordData(
        @XmlElement(true)
        val rdf: Rdf,
    )

    @Serializable
    @XmlSerialName("RDF", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
    data class Rdf(
        @XmlElement(true)
        val bibResource: List<BibResource>
    )

    @Serializable
    @XmlSerialName("BibResource", "http://ndl.go.jp/dcndl/terms/", "dcndl")
    data class BibResource(
        @XmlElement(false)
        @XmlSerialName("about", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
        val about: String?,

        @XmlElement(true)
        val title: Title?,

        @XmlElement(true)
        val seriesTitle: SeriesTitle?,

        @XmlElement(true)
        val identifier: List<Identifier>,

        @XmlElement(true)
        @XmlSerialName("price", "http://ndl.go.jp/dcndl/terms/", "dcndl")
        val price: String?,

        @XmlElement(true)
        val publisher: Publisher?,

        @XmlElement(true)
        @XmlSerialName("extent", "http://purl.org/dc/terms/", "dcterms")
        private val _extent: String?,

        @XmlElement(true)
        @XmlSerialName("date", "http://purl.org/dc/terms/", "dcterms")
        val date: String?,

        @XmlElement(true)
        val creators: List<Creator>,

        @XmlElement(true)
        @XmlSerialName("edition", "http://ndl.go.jp/dcndl/terms/", "dcndl")
        val edition: String?,

        @XmlElement(true)
        val volume: Volume?,
    ) {
        val extent: Int? = "\\d+".toRegex().find(_extent.orEmpty())?.value?.toIntOrNull()

        val isbn: String = identifier.firstOrNull {
            it.datatype.contains("ISBN", ignoreCase = true)
        }?.data?.replace("-", "").let {
            if (it?.length == 10) {
                isbn10ToIsbn13(it)
            } else {
                it
            }
        }.orEmpty()

        private fun isbn10ToIsbn13(isbn10: String): String {
            check(isbn10.length == 10 && isbn10.toIntOrNull() == null) {
                "ISBN-10 must be 10 digits"
            }
            val isbn = isbn10.take(9)
            var operator = 1
            val sum = ("978${isbn}").sumOf {
                val digit = it.digitToInt()
                val tmp = operator
                operator = if (operator == 1) 3 else 1
                digit * tmp
            }
            val checkDigit = 10 - (sum % 10)
            return "987$isbn$checkDigit"
        }

        @Serializable
        @XmlSerialName("title", "http://purl.org/dc/elements/1.1/", "dc")
        data class Title(
            @XmlElement(true)
            val description: Description
        ) {
            @Serializable
            @XmlSerialName("Description", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
            data class Description(
                @XmlElement(true)
                @XmlSerialName("value", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
                val value: String,
                @XmlElement(true)
                @XmlSerialName("transcription", "http://ndl.go.jp/dcndl/terms/", "dcndl")
                val transcription: String?,
            )
        }

        @Serializable
        @XmlSerialName("seriesTitle", "http://ndl.go.jp/dcndl/terms/", "dcndl")
        data class SeriesTitle(
            @XmlElement(true)
            val description: Description,
        ) {
            @Serializable
            @XmlSerialName("Description", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
            data class Description(
                @XmlElement(true)
                @XmlSerialName("value", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
                val value: String,
            )
        }

        @Serializable
        @XmlSerialName("identifier", "http://purl.org/dc/terms/", "dcterms")
        data class Identifier(
            @XmlElement(false)
            @XmlSerialName("datatype", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
            val datatype: String,
            @XmlValue(true)
            val data: String,
        )

        @Serializable
        @XmlSerialName("creator", "http://purl.org/dc/terms/", "dcterms")
        data class Creator(
            @XmlElement(true)
            val description: Agent
        ) {
            @Serializable
            @XmlSerialName("Agent", "http://xmlns.com/foaf/0.1/", "foaf")
            data class Agent(
                @XmlElement(true)
                @XmlSerialName("name", "http://xmlns.com/foaf/0.1/", "foaf")
                private val _name: String,

                @XmlElement(false)
                @XmlSerialName("about", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
                val about: String?,

                @XmlElement(true)
                @XmlSerialName("transcription", "http://ndl.go.jp/dcndl/terms/", "dcndl")
                private val _transcription: String?,
            ) {
                val name = _name.split(",\\s*".toRegex())
                    .filter { it.contains("""\d{4,}""".toRegex()).not() }
                    .joinToString(separator = " ")

                val transcription = _transcription?.split(",\\s*".toRegex())
                    ?.filter { it.contains("""\d{4,}""".toRegex()).not() }
                    ?.joinToString(separator = " ")
            }
        }

        @Serializable
        @XmlSerialName("publisher", "http://purl.org/dc/terms/", "dcterms")
        data class Publisher(
            @XmlElement(true)
            val agent: Agent,
        ) {
            @Serializable
            @XmlSerialName("Agent", "http://xmlns.com/foaf/0.1/", "foaf")
            data class Agent(
                @XmlElement(true)
                @XmlSerialName("name", "http://xmlns.com/foaf/0.1/", "foaf")
                val name: String,
            )
        }

        @Serializable
        @XmlSerialName("volume", "http://ndl.go.jp/dcndl/terms/", "dcndl")
        data class Volume(
            @XmlElement(true)
            val description: Description
        ) {
            @Serializable
            @XmlSerialName("Description", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
            data class Description(
                @XmlElement
                @XmlSerialName("value", "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf")
                val value: String,
            )
        }
    }
}
