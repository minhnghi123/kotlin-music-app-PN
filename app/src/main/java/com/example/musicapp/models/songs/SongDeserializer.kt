package com.example.musicapp.models.songs

import android.util.Log
import com.example.musicapp.models.artists.Artist
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class SongDeserializer : JsonDeserializer<Song> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Song {
        if (json == null || !json.isJsonObject) {
            throw JsonParseException("Invalid song JSON")
        }

        val obj = json.asJsonObject
        
        // Parse artist field (có thể là array hoặc object)
        val artistList = parseArtists(obj.get("artist"), context)
        
        return Song(
            _id = obj.get("_id")?.asString ?: "",
            title = obj.get("title")?.asString ?: "",
            artist = artistList,
            album = obj.get("album")?.asString ?: "",
            topic = parseStringList(obj.get("topic")),
            fileUrl = obj.get("fileUrl")?.asString ?: "",
            coverImage = obj.get("coverImage")?.asString ?: "",
            likes = parseStringList(obj.get("likes")),
            lyrics = obj.get("lyrics")?.asString,
            description = obj.get("description")?.asString,
            status = obj.get("status")?.asString ?: "active",
            deleted = obj.get("deleted")?.asBoolean ?: false,
            createdAt = obj.get("createdAt")?.asString ?: "",
            updatedAt = obj.get("updatedAt")?.asString ?: ""
        )
    }
    
    private fun parseArtists(
        element: JsonElement?,
        context: JsonDeserializationContext?
    ): List<Artist> {
        if (element == null || element.isJsonNull) {
            Log.w("SongDeserializer", "Artist is null")
            return emptyList()
        }
        
        return try {
            when {
                // Case 1: Array của artists
                element.isJsonArray -> {
                    Log.d("SongDeserializer", "Parsing artist as ARRAY")
                    val list = mutableListOf<Artist>()
                    element.asJsonArray.forEach { item ->
                        try {
                            val artist = context?.deserialize<Artist>(item, Artist::class.java)
                            if (artist != null) list.add(artist)
                        } catch (e: Exception) {
                            Log.e("SongDeserializer", "Failed to parse artist: ${e.message}")
                        }
                    }
                    list
                }
                
                // Case 2: Object đơn
                element.isJsonObject -> {
                    Log.d("SongDeserializer", "Parsing artist as OBJECT")
                    try {
                        val artist = context?.deserialize<Artist>(element, Artist::class.java)
                        if (artist != null) listOf(artist) else emptyList()
                    } catch (e: Exception) {
                        Log.e("SongDeserializer", "Failed to parse single artist: ${e.message}")
                        emptyList()
                    }
                }
                
                // Case 3: String (ID)
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    Log.d("SongDeserializer", "Parsing artist as STRING ID")
                    listOf(Artist(
                        _id = element.asString,
                        fullName = "Unknown Artist",
                        country = "",
                        coverImage = null
                    ))
                }
                
                else -> {
                    Log.w("SongDeserializer", "Unknown artist JSON type")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e("SongDeserializer", "Error parsing artists: ${e.message}", e)
            emptyList()
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
