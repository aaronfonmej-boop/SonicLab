package com.soniclab3d.particles

enum class ParticleQuality(val label: String, val particleCount: Int) {
    SAVER("Ahorro", 30_000),
    NORMAL("Normal", 60_000),
    ULTRA("Ultra", 120_000),
    EXTREME("Extremo", 200_000),
    EXPERIMENTAL("Experimental", 600_000)
}
