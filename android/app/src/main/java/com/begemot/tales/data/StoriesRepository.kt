package com.begemot.tales.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object StoriesRepository {
    private const val SUPABASE_URL = "https://jjzgflwjhoilfuvlzave.supabase.co"
    private const val ANON_KEY = "sb_publishable_gg5k0IAuZBRMSglIRfXkvw_w2oY_woH"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val fallbackStories = listOf(
Story(
            id = "XsywRVA7B-E",
            title = "НЕ ПРИКАСАЙСЯ К НИМ! Часть 6. Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1957,
            durationFormatted = "32:37",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/XsywRVA7B-E/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/XsywRVA7B-E/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "XHritlEwpSc",
            title = "МОЯ КВАРТИРА НАЧАЛА УМЕНЬШАТЬСЯ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1688,
            durationFormatted = "28:08",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/XHritlEwpSc/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/XHritlEwpSc/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "iDdBfbX5Blc",
            title = "НЕ ПРИКАСАЙСЯ К НИМ! Часть 7 Финал. Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2033,
            durationFormatted = "33:53",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/iDdBfbX5Blc/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/iDdBfbX5Blc/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "uEYQrZkmLJQ",
            title = "СНЕГУРОЧКА ГОРИ! (Совместно с Гробовщиком) Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1693,
            durationFormatted = "28:13",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/uEYQrZkmLJQ/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/uEYQrZkmLJQ/cover.jpg",
            description = "Продолжение - https://youtu.be/fuqtnb5V08o    https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По ",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "3l4MpEfu748",
            title = "ПРОГОНИ НЕЧИСТЬ ИЗ ДОМА! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1570,
            durationFormatted = "26:10",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/3l4MpEfu748/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/3l4MpEfu748/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("мистика", "ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "Xky4qpQp5Ug",
            title = "ТАКСИ НЕ ДОМОЙ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1391,
            durationFormatted = "23:11",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/Xky4qpQp5Ug/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/Xky4qpQp5Ug/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "t8Nz6Yjd6zM",
            title = "НЕЧТО ЗАКРЫЛО МЕНЯ В КЛАДОВКЕ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1645,
            durationFormatted = "27:25",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/t8Nz6Yjd6zM/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/t8Nz6Yjd6zM/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "VwK2W1ploo0",
            title = "ВАМ ВЫДЕЛЕНА КОМНАТА... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1599,
            durationFormatted = "26:39",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/VwK2W1ploo0/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/VwK2W1ploo0/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "qQZeUaF5r6c",
            title = "ТРАНСПЛАНТАЦИЯ ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1892,
            durationFormatted = "31:32",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/qQZeUaF5r6c/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/qQZeUaF5r6c/cover.jpg",
            description = "https://boosty.to/wbk - Бусти   https://vk.ru/all_cash1996 - Автор истории. По вопросам рекламы - vitaly212121@gmail.com",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "GppxF9tY-QY",
            title = "ТОЛЬКО НЕ СМОТРИ ВНИЗ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1677,
            durationFormatted = "27:57",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/GppxF9tY-QY/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/GppxF9tY-QY/cover.jpg",
            description = "Лучшие цены на авто у Автоспот: https://autospot.ru/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_content=fb2",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "VOun_r6NI1o",
            title = "В ДЕРЕВНЕ ПОЯВЛЯЕТСЯ СТРАННАЯ ДВЕРЬ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1595,
            durationFormatted = "26:35",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/VOun_r6NI1o/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/VOun_r6NI1o/cover.jpg",
            description = "Лучшие цены ТО на Автоспот: https://autospot.ru/autoservice/to/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_",
            tags = listOf("ночь", "деревня", "страшилки")
        ),
        Story(
            id = "yOm7yekwWk8",
            title = "БАБКА-ТРЯПКА... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1650,
            durationFormatted = "27:30",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/yOm7yekwWk8/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/yOm7yekwWk8/cover.jpg",
            description = "Лучшие цены ТО на Автоспот: https://autospot.ru/autoservice/to/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "fMdeA1We8UM",
            title = "ТАРАКАННИК... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1734,
            durationFormatted = "28:54",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/fMdeA1We8UM/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/fMdeA1We8UM/cover.jpg",
            description = "Лучшие цены ТО на Автоспот: https://autospot.ru/autoservice/to/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "S8DPLzQ2hc0",
            title = "ТЕЛО БЕЗ КОЖИ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1526,
            durationFormatted = "25:26",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/S8DPLzQ2hc0/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/S8DPLzQ2hc0/cover.jpg",
            description = "https://boosty.to/wbk - Бусти   По вопросам рекламы - vitaly212121@gmail.com https://vk.com/public73066442 - Группа ВК h",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "Aq3HZ7HeXWo",
            title = "БЕГИТЕ,ПОКА НЕ СТЕМНЕЛО! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1642,
            durationFormatted = "27:22",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/Aq3HZ7HeXWo/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/Aq3HZ7HeXWo/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "LOJS6H40Vb8",
            title = "ПУСТЬ АНГЕЛ ТЕБЯ БЕРЕЖЁТ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1663,
            durationFormatted = "27:43",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/LOJS6H40Vb8/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/LOJS6H40Vb8/cover.jpg",
            description = "https://boosty.to/wbk - Бусти Купить мои книги - https://wbk-shop.ru/  По вопросам рекламы - vitaly212121@gmail.com http",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "oIuxKFqkKiQ",
            title = "MЕРTBOPOЖДЁHHЫE... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2042,
            durationFormatted = "34:02",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/oIuxKFqkKiQ/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/oIuxKFqkKiQ/cover.jpg",
            description = "Лучшие цены на авто у Автоспот: https://autospot.ru/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_content=ap1",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "eVg149bup6k",
            title = "ВЫГОНИТЕ ЭТО ИЗ МОЕЙ КВАРТИРЫ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1583,
            durationFormatted = "26:23",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/eVg149bup6k/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/eVg149bup6k/cover.jpg",
            description = "Смотрите «Радар» только в Okko: https://okko.tv/s/zhr Подписка на 30 дней за 1₽ по промокоду «RADAR»  https://boosty.to/",
            tags = listOf("ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "NxurvV1AdE8",
            title = "ПОМНИШЬ МЕНЯ?! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1780,
            durationFormatted = "29:40",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/NxurvV1AdE8/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/NxurvV1AdE8/cover.jpg",
            description = "https://chemistry1.tb.ru/ - Курсы подготовки к ЕГЭ и ОГЭ Chem_is_try. https://t.me/chem_is_try1 - Бесплатный ТГ канал.  ",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "9X7P-gWvjNk",
            title = "ТЕЛО ДЛЯ БЕСА... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1763,
            durationFormatted = "29:23",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/9X7P-gWvjNk/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/9X7P-gWvjNk/cover.jpg",
            description = "Смотрите «След Чикатило» только в Okko: https://vk.cc/cY5JIj Подписка на 30 дней за 1₽ по промокоду «TRUECRIME»  Реклама",
            tags = listOf("ночь", "страшилки")
        ),
        Story(
            id = "c_D4ciAJk38",
            title = "ЛУЖА... ЧАСТЬ 3. Страшные истории на ночь",
            durationSeconds = 1680,
            durationFormatted = "28:00",
            channel = "WorldBegemotKot",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/c_D4ciAJk38/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/c_D4ciAJk38/cover.jpg",
            description = "Таёжные аномалии и мистика у глухой лужи.",
            tags = listOf("аномалии", "тайга", "мистика", "ночь", "страшилки")
        ),
        Story(
            id = "_rVLdd3C_Fc",
            title = "НЕ СМОТРИ БОКОВЫМ ЗРЕНИЕМ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1675,
            durationFormatted = "27:55",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/_rVLdd3C_Fc/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/_rVLdd3C_Fc/cover.jpg",
            description = "По вопросам рекламы - vitaly212121@gmail.com https://vk.com/public73066442 - Группа ВК https://www.instagram.com/maxbege",
            tags = listOf("аномалии", "мистика", "ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "gOn0eoJdKKc",
            title = "СОБАКА ПОКУСАЛА! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1910,
            durationFormatted = "31:50",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/gOn0eoJdKKc/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/gOn0eoJdKKc/cover.jpg",
            description = "Лучшие цены на авто у Автоспот: https://autospot.ru/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_content=jn1",
            tags = listOf("деревня", "тайга", "ночь", "мистика", "страшилки")
        ),
        Story(
            id = "2fJ0G5HwIyI",
            title = "МНОГОРУК... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1707,
            durationFormatted = "28:27",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/2fJ0G5HwIyI/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/2fJ0G5HwIyI/cover.jpg",
            description = "🔥Станьте Python-разработчиком с нуля в Skillfactory: https://go.skillfactory.ru/0azQcg?erid=2VtzqwYBpY1. Скидки до 65% +",
            tags = listOf("аномалии", "тайга", "ночь", "мистика", "страшилки")
        ),
        Story(
            id = "0LL5pskAQYo",
            title = "\"СГИНЬТЕ ВЫРОДКИ\". Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2023,
            durationFormatted = "33:43",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/0LL5pskAQYo/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/0LL5pskAQYo/cover.jpg",
            description = "Лучшие цены на авто у Автоспот: https://autospot.ru/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&utm_content=jn2",
            tags = listOf("тайга", "деревня", "мистика", "аномалии", "страшилки")
        ),
        Story(
            id = "mhDFecPHD7E",
            title = "НЕ ВКЛЮЧАЙ СВЕТ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1644,
            durationFormatted = "27:24",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/mhDFecPHD7E/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/mhDFecPHD7E/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("ночь", "мистика", "заброшки", "страшилки")
        ),
        Story(
            id = "iBymu7U-Bik",
            title = "ЭТИХ ЖЕ ФОТОГРАФИЙ НЕ БЫЛО В АЛЬБОМЕ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2017,
            durationFormatted = "33:37",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/iBymu7U-Bik/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/iBymu7U-Bik/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("мистика", "заброшки", "ночь", "страшилки")
        ),
        Story(
            id = "UvqcAgF1N-E",
            title = "ВПУСТИ МЕНЯ! Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1674,
            durationFormatted = "27:54",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/UvqcAgF1N-E/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/UvqcAgF1N-E/cover.jpg",
            description = "Лучшие цены на ТО у Автоспот: https://autospot.ru/autoservice/to/?utm_source=ytb&utm_medium=bloggers&utm_term=newcars&ut",
            tags = listOf("ночь", "мистика", "деревня", "страшилки")
        ),
        Story(
            id = "j5qhpGmHpto",
            title = "БЕЛЯШИКИ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2774,
            durationFormatted = "46:14",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/j5qhpGmHpto/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/j5qhpGmHpto/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("деревня", "мистика", "ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "7D5yIiznnJQ",
            title = "ПРАВИЛА ОБЩЕЖИТИЯ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 2224,
            durationFormatted = "37:04",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/7D5yIiznnJQ/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/7D5yIiznnJQ/cover.jpg",
            description = "🔥Стань “Белым” хакером в Skillfactory: https://go.skillfactory.ru/Lhld2w Скидки до 65% + 5 мини-курсов в подарок по пром",
            tags = listOf("заброшки", "мистика", "ночь", "страшилки")
        ),
        Story(
            id = "ZQnW0-3mN5E",
            title = "ЛУЖА... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1762,
            durationFormatted = "29:22",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/ZQnW0-3mN5E/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/ZQnW0-3mN5E/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("аномалии", "тайга", "мистика", "ночь", "страшилки")
        ),
        Story(
            id = "96KThVNaNhU",
            title = "ВЫМОРОЧНОЕ ЖИЛЬЁ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1623,
            durationFormatted = "27:03",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/96KThVNaNhU/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/96KThVNaNhU/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("заброшки", "деревня", "мистика", "ночь", "страшилки")
        ),
        Story(
            id = "QzF33NC0_cE",
            title = "ЮЛЯ? Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1764,
            durationFormatted = "29:24",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/QzF33NC0_cE/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/QzF33NC0_cE/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("мистика", "ночь", "заброшки", "страшилки")
        ),
        Story(
            id = "K-GJGNOmxj0",
            title = "ГЛАЗАСТЫЙ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1590,
            durationFormatted = "26:30",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/K-GJGNOmxj0/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/K-GJGNOmxj0/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("тайга", "ночь", "мистика", "деревня", "страшилки")
        ),
        Story(
            id = "566xEt2T0Us",
            title = "В ЧИСТОМ ПОЛЕ ПОЯВИЛАСЬ СТЕНА... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1590,
            durationFormatted = "26:30",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/566xEt2T0Us/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/566xEt2T0Us/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("аномалии", "мистика", "заброшки", "ночь", "страшилки")
        ),
        Story(
            id = "UPV3yeqxiPE",
            title = "ЛУЖА... ЧАСТЬ 2 Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1699,
            durationFormatted = "28:19",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/UPV3yeqxiPE/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/UPV3yeqxiPE/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("аномалии", "тайга", "ночь", "мистика", "страшилки")
        ),
        Story(
            id = "HbX7VNTWACw",
            title = "ПОСЫЛКИ ПОД ДВЕРЬЮ... Страшные истории на ночь. Страшилки на ночь.",
            durationSeconds = 1524,
            durationFormatted = "25:24",
            channel = "WorldBegemotKot † Страшные истории †",
            audioUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/HbX7VNTWACw/audio.mp3",
            coverUrl = "https://jjzgflwjhoilfuvlzave.supabase.co/storage/v1/object/public/audio/HbX7VNTWACw/cover.jpg",
            description = "https://wbk-shop.ru/ - Купить книги и мерч. https://vk.com/public73066442 - Купить книги и мерч. https://boosty.to/wbk -",
            tags = listOf("мистика", "заброшки", "ночь", "деревня", "страшилки")
        ),
    )

    var cachedStories: List<Story> = fallbackStories

    suspend fun getStories(forceRefresh: Boolean = false): Result<List<Story>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedStories.isNotEmpty()) {
            return@withContext Result.success(cachedStories)
        }

        val request = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/stories?select=*&order=created_at.desc")
            .header("apikey", ANON_KEY)
            .header("Authorization", "Bearer $ANON_KEY")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(cachedStories)
                }
                val body = response.body?.string() ?: "[]"
                val stories = json.decodeFromString<List<Story>>(body)
                if (stories.isNotEmpty()) {
                    cachedStories = stories
                    Result.success(stories)
                } else {
                    Result.success(cachedStories)
                }
            }
        } catch (_: Exception) {
            Result.success(cachedStories)
        }
    }
}
