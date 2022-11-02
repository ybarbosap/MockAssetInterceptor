package com.github.ybarbosap.mockassetinterceptor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class MockAsset(
    val asset: String,
    val apply: Boolean,
    val delay: Long = 1000L
)
