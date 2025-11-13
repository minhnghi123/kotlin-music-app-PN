package com.example.musicapp.models.songs

import android.util.Log
import com.example.musicapp.models.artists.Artist
import com.google.gson.*
import java.lang.reflect.Type

class SongForArtistDeserializer : JsonDeserializer<SongForArtist> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SongForArtist {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Invalid SongForArtist JSON")
        }

        val obj = json.asJsonObject
        
        // Parse artist field
        val artistList = parseArtists(obj.get("artist"), context)
        
        Log.d("SongForArtistDeserializer", "Parsed song: ${obj.get("title")?.asString}")
        Log.d("SongForArtistDeserializer", "  Artists: ${artistList.size}")
        artistList.forEach { 
            Log.d("SongForArtistDeserializer", "    - ${it.fullName}")
        }
        
        return SongForArtist(
            _id = obj.get("_id")?.asString ?: "",
            title = obj.get("title")?.asString ?: "",
            artist = artistList,
            album = obj.get("album")?.asString,
            topic = parseStringList(obj.get("topic")),
            fileUrl = obj.get("fileUrl")?.asString,
            coverImage = obj.get("coverImage")?.asString,
            likes = parseStringList(obj.get("likes")),
            lyrics = obj.get("lyrics")?.asString,
            description = obj.get("description")?.asString,
            status = obj.get("status")?.asString,
            deleted = obj.get("deleted")?.asBoolean,
            createdAt = obj.get("createdAt")?.asString,
            updatedAt = obj.get("updatedAt")?.asString
        )
    }
    
    private fun parseArtists(
        element: JsonElement?,
        context: JsonDeserializationContext?
    ): List<Artist> {
        if (element == null || element.isJsonNull) {
            return emptyList()
        }
        
        return try {
            when {
                element.isJsonArray -> {
                    val list = mutableListOf<Artist>()
                    element.asJsonArray.forEach { item ->
                        parseSingleArtist(item)?.let { list.add(it) }
                    }
                    list
                }
                element.isJsonObject -> {
                    val artist = parseSingleArtist(element)
                    if (artist != null) listOf(artist) else emptyList()
                }
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    listOf(Artist(
                        _id = element.asString,
                        fullName = "Unknown Artist",
                        country = "",
                        coverImage = null
                    ))
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e("SongForArtistDeserializer", "Error parsing artists: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun parseSingleArtist(element: JsonElement?): Artist? {
        if (element == null || !element.isJsonObject) return null
        
        return try {
            val obj = element.asJsonObject
            
            // Check nested artist object
            val nestedArtist = obj.get("artist")
            if (nestedArtist != null && nestedArtist.isJsonObject) {
                return parseSingleArtist(nestedArtist)
            }
            
            Artist(
                _id = obj.get("_id")?.asString 
                    ?: obj.get("id")?.asString 
                    ?: "",
                fullName = obj.get("fullName")?.asString 
                    ?: obj.get("name")?.asString 
                    ?: "Unknown Artist",
                country = obj.get("country")?.asString ?: "",
                coverImage = obj.get("coverImage")?.asString
            )
        } catch (e: Exception) {
            Log.e("SongForArtistDeserializer", "Error parsing single artist", e)
            null
        }
    }
    
    private fun parseStringList(element: JsonElement?): List<String> {
        if (element == null || element.isJsonNull) return emptyList()
        if (!element.isJsonArray) return emptyList()
        
        return try {
            element.asJsonArray.mapNotNull { 
                if (it.isJsonPrimitive) it.asString else null 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
