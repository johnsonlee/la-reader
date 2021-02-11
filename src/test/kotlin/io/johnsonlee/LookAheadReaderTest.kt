package io.johnsonlee

import org.junit.Test
import java.io.StringReader
import kotlin.test.assertEquals

class LookAheadReaderTest {

    @Test
    fun `test read wrapped string`() {
        StringReader("%abc%").use { reader ->
            val lar = LookAheadReader(reader)
            val str = lar.readWrappedString('%'.toInt())
            assertEquals("abc", str)
        }
    }

    @Test
    fun `test read wrapped string with escape`() {
        StringReader("%a%%b%%c%").use { reader ->
            val lar = LookAheadReader(reader)
            val str = lar.readWrappedString('%'.toInt(), '%'.toInt())
            assertEquals("a%%b%%c", str)
        }
    }

    @Test
    fun `test read double quoted string`() {
        StringReader("\"abc\"").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("abc", lar.readDoubleQuotedString())
        }
    }

    @Test
    fun `test read double quoted string with escape`() {
        StringReader(""""abc${'\\'}${'"'}d"""").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("abc${'\\'}${'"'}d", lar.readDoubleQuotedString())
        }
    }

    @Test
    fun `test read single quoted string`() {
        StringReader("'abc'").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("abc", lar.readSingleQuotedString())
        }
    }

    @Test
    fun `test read single quoted string with escape`() {
        StringReader("'abc\\'d'").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("abc\\'d", lar.readSingleQuotedString())
        }
    }

    @Test
    fun `test read signed int`() {
        StringReader("32").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(32, lar.readSignedInt())
        }
        StringReader("+32").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(32, lar.readSignedInt())
        }
        StringReader("-32").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(-32, lar.readSignedInt())
        }
    }

    @Test
    fun `test read unsigned int`() {
        StringReader("00032").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(32, lar.readUnsignedInt())
        }
    }

    @Test
    fun `test read line`() {
        StringReader("""
        1234567890
        abcdefghij
        """.trimIndent()).use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("1234567890", lar.readLine())
            assertEquals("abcdefghij", lar.readLine())
        }
    }

    @Test
    fun `test skip whitespace`() {
        StringReader("        \t 1234567890").use { reader ->
            val lar = LookAheadReader(reader)
            lar.skipWhitespace()
            assertEquals("1234567890", lar.readLine())
        }
    }

    @Test
    fun `test skip blank lines`() {
        StringReader("""
        |
        |
        |1234567890
        """.trimMargin()).use { reader ->
            val lar = LookAheadReader(reader)
            lar.skipBlankLines()
            assertEquals("1234567890", lar.readLine())
        }
    }

    @Test
    fun `test read Boolean`() {
        StringReader("true").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(true, lar.readBoolean())
        }
        StringReader("false").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(false, lar.readBoolean())
        }
        StringReader("????").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals(null, lar.readBoolean())
        }
    }

    @Test
    fun `test read token`() {
        StringReader("abcde (status)").use { reader ->
            val lar = LookAheadReader(reader)
            assertEquals("abcde", lar.readToken())
        }
    }

}