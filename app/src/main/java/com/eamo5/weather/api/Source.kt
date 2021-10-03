package com.eamo5.weather.api

data class Source(
    val crawl_rate: Int,
    val slug: String,
    val title: String,
    val url: String
)