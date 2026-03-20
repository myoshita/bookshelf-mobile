package com.myoshita.bookshelf.util

object Kana {
    fun String.katakanaToHiragana(): String {
        return this.hankakuToZenkakuKatakana().map {
            when {
                it.isKatakana -> it - (FIRST_KATAKANA - FIRST_HIRAGANA)
                else -> it
            }
        }.joinToString("")
    }

    private val Char.isKatakana: Boolean get() = this in (FIRST_KATAKANA..LAST_KATAKANA)

    fun String.hiraganaToKatakana(): String = map {
        when {
            it.isHiragana -> it + (FIRST_KATAKANA - FIRST_HIRAGANA)
            else -> it
        }
    }.joinToString("").hankakuToZenkakuKatakana()

    private val Char.isHiragana: Boolean get() = this in (FIRST_HIRAGANA..LAST_HIRAGANA)

    fun String.hankakuToZenkakuKatakana(): String {
        val map = HANKAKU_ZENKAKU_KATAKANA_MAP
        return buildString {
            var previousChar: Char? = null
            for (char in this@hankakuToZenkakuKatakana) {
                if (char == 'ﾞ' || char == 'ﾟ') {
                    // 濁点・半濁点を前の文字に適用
                    previousChar?.let {
                        val combinedChar = when (char) {
                            'ﾞ' -> when (it) {
                                'カ' -> 'ガ'; 'キ' -> 'ギ'; 'ク' -> 'グ'; 'ケ' -> 'ゲ'; 'コ' -> 'ゴ'
                                'サ' -> 'ザ'; 'シ' -> 'ジ'; 'ス' -> 'ズ'; 'セ' -> 'ゼ'; 'ソ' -> 'ゾ'
                                'タ' -> 'ダ'; 'チ' -> 'ヂ'; 'ツ' -> 'ヅ'; 'テ' -> 'デ'; 'ト' -> 'ド'
                                'ハ' -> 'バ'; 'ヒ' -> 'ビ'; 'フ' -> 'ブ'; 'ヘ' -> 'ベ'; 'ホ' -> 'ボ'
                                else -> it
                            }
                            'ﾟ' -> when (it) {
                                'ハ' -> 'パ'; 'ヒ' -> 'ピ'; 'フ' -> 'プ'; 'ヘ' -> 'ペ'; 'ホ' -> 'ポ'
                                else -> it
                            }
                            else -> it
                        }
                        setLength(length - 1) // 前の文字を削除
                        append(combinedChar)
                        previousChar = combinedChar
                    }
                } else {
                    val fullWidthChar = map[char] ?: char
                    append(fullWidthChar)
                    previousChar = fullWidthChar
                }
            }
        }
    }

    private const val FIRST_HIRAGANA = '\u3041' // 'ぁ'
    private const val LAST_HIRAGANA = '\u3093' // 'ん'
    private const val FIRST_KATAKANA = '\u30A1' // 'ァ'
    private const val LAST_KATAKANA = '\u30F3' // 'ン'
    private val HANKAKU_ZENKAKU_KATAKANA_MAP: Map<Char, Char>
        get() = mapOf(
            'ｱ' to 'ア', 'ｲ' to 'イ', 'ｳ' to 'ウ', 'ｴ' to 'エ', 'ｵ' to 'オ',
            'ｶ' to 'カ', 'ｷ' to 'キ', 'ｸ' to 'ク', 'ｹ' to 'ケ', 'ｺ' to 'コ',
            'ｻ' to 'サ', 'ｼ' to 'シ', 'ｽ' to 'ス', 'ｾ' to 'セ', 'ｿ' to 'ソ',
            'ﾀ' to 'タ', 'ﾁ' to 'チ', 'ﾂ' to 'ツ', 'ﾃ' to 'テ', 'ﾄ' to 'ト',
            'ﾅ' to 'ナ', 'ﾆ' to 'ニ', 'ﾇ' to 'ヌ', 'ﾈ' to 'ネ', 'ﾉ' to 'ノ',
            'ﾊ' to 'ハ', 'ﾋ' to 'ヒ', 'ﾌ' to 'フ', 'ﾍ' to 'ヘ', 'ﾎ' to 'ホ',
            'ﾏ' to 'マ', 'ﾐ' to 'ミ', 'ﾑ' to 'ム', 'ﾒ' to 'メ', 'ﾓ' to 'モ',
            'ﾔ' to 'ヤ', 'ﾕ' to 'ユ', 'ﾖ' to 'ヨ',
            'ﾗ' to 'ラ', 'ﾘ' to 'リ', 'ﾙ' to 'ル', 'ﾚ' to 'レ', 'ﾛ' to 'ロ',
            'ﾜ' to 'ワ', 'ｦ' to 'ヲ', 'ﾝ' to 'ン',
            'ｧ' to 'ァ', 'ｨ' to 'ィ', 'ｩ' to 'ゥ', 'ｪ' to 'ェ', 'ｫ' to 'ォ',
            'ｬ' to 'ャ', 'ｭ' to 'ュ', 'ｮ' to 'ョ', 'ｯ' to 'ッ',
            'ｰ' to 'ー', 'ﾞ' to '゛', 'ﾟ' to '゜'
        )
}