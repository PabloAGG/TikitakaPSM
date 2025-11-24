package com.example.tikitaka.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Adapter personalizado para convertir números (0/1) en booleanos
 * Maneja tanto booleanos nativos como números del backend
 */
class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }
    
    override fun read(input: JsonReader): Boolean {
        return when (input.peek()) {
            JsonToken.BOOLEAN -> input.nextBoolean()
            JsonToken.NUMBER -> input.nextInt() != 0
            JsonToken.STRING -> {
                val str = input.nextString()
                str.equals("true", ignoreCase = true) || str == "1"
            }
            JsonToken.NULL -> {
                input.nextNull()
                false
            }
            else -> false
        }
    }
}
