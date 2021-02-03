package io.johnsonlee

import java.io.EOFException
import java.io.InputStream
import java.io.PushbackReader
import java.io.Reader

/**
 * `"`
 */
private const val DOUBLE_QUOTE: Int = 34

/**
 * `'`
 */
private const val SINGLE_QUOTE: Int = 39

/**
 * `\`
 */
private const val BACK_SLASH: Int = 92

/**
 * Look ahead reader
 */
open class LookAheadReader @JvmOverloads constructor(
        reader: Reader,
        bufferSize: Int = DEFAULT_BUFFER_SIZE
) : PushbackReader(reader.buffered(), bufferSize) {

    companion object {
        const val EOF: Int = -1
    }

    @JvmOverloads
    constructor(
            input: InputStream,
            bufferSize: Int = DEFAULT_BUFFER_SIZE
    ) : this(input.bufferedReader(), bufferSize)

    /**
     * Read sequence wrapped by [left] and [right]
     */
    @JvmOverloads
    open fun readWrappedString(left: Int, right: Int = left, escape: Int = if (left == right) left else EOF): String? {
        var prev: Int
        var next = read()

        if (EOF == next || left != next) {
            return null
        }

        val str = StringBuilder()
        while (true) {
            prev = next
            next = read()

            if (next == EOF) throw EOFException()

            if (next == right && prev != escape) {
                if (next != escape) {
                    break
                }

                val ahead = read()
                if (ahead == next) {
                    unread(ahead)
                } else {
                    break
                }
            }

            str.append(next.toChar())
        }

        return str.toString()
    }

    private fun readQuotedString(quote: Int): String? = readWrappedString(quote, quote, BACK_SLASH)

    /**
     * Read double quoted string, supports quote escape
     */
    open fun readDoubleQuotedString(): String? = readQuotedString(DOUBLE_QUOTE)

    /**
     * Read single quoted string, supports quote escape
     */
    open fun readSingleQuotedString(): String? = readQuotedString(SINGLE_QUOTE)

    /**
     * Read unsigned int value
     */
    open fun readUnsignedInt(): Int? = readDigits()?.toInt()

    /**
     * Read signed int value
     */
    open fun readSignedInt(): Int? = when (val c = read()) {
        /* + */ 43 -> readUnsignedInt()
        /* - */ 45 -> readUnsignedInt()?.let { -it }
        else -> if (Character.isDigit(c)) {
            unread(c)
            readUnsignedInt()
        } else null
    }

    /**
     * Read digit sequence
     */
    open fun readDigits(): String? {
        var c: Int
        val digits = StringBuilder(10)

        while (true) {
            c = read()

            when {
                c == EOF -> break
                !Character.isDigit(c) -> {
                    unread(c)
                    break
                }
            }

            digits.append(c.toChar())
        }

        return digits.takeIf(StringBuilder::isNotEmpty)?.toString()
    }

    /**
     * Read boolean literal
     */
    open fun readBoolean(): Boolean? = when (peak()) {
        /* t */ 116 -> readTrue()
        /* f */ 102 -> readFalse()
        else -> null
    }

    /**
     * Read literal `true`
     */
    open fun readTrue(): Boolean? {
        val buf = CharArray(4)
        val n = read(buf, 0, buf.size)
        return if (n == buf.size && buf[0] == 't' && buf[1] == 'r' && buf[2] == 'u' && buf[3] == 'e') {
            true
        } else {
            unread(buf, 0, n)
            null
        }
    }

    /**
     * Read literal `false`
     */
    open fun readFalse(): Boolean? {
        val buf = CharArray(5)
        val n = read(buf, 0, buf.size)
        return if (n == buf.size && buf[0] == 'f' && buf[1] == 'a' && buf[2] == 'l' && buf[3] == 's' && buf[4] == 'e') {
            false
        } else {
            unread(buf, 0, n)
            null
        }
    }

    /**
     * Read the whole line from current position
     */
    open fun readLine(): String? {
        val line = StringBuilder()
        var c: Int

        while (true) {
            c = read()
            if (c == EOF || c == 10 /* \n */ || c == 13 /* \r */) {
                break
            }
            line.append(c.toChar())
        }

        if (line.isEmpty() && c == EOF) {
            return null
        }

        return line.toString()
    }

    /**
     * Skip whitespace
     */
    open fun skipWhitespace() {
        var c: Int

        while (true) {
            c = read()
            if (!Character.isWhitespace(c)) {
                unread(c)
                break
            }
        }
    }

    /**
     * Skip blank lines
     */
    open fun skipBlankLines() {
        var line: String?

        do {
            line = readLine()
        } while (line?.isEmpty() == true || line?.isBlank() == true)

        if (line?.isNotEmpty() == true) {
            unread(10) // put \n back
            unread(line.toCharArray())
        }
    }

    /**
     * Returns the next char without consuming the stream
     */
    open fun peak(): Int {
        val c = read()
        unread(c)
        return c
    }


}