package com.soniclab3d.physics

data class ReferenceParticle(
    val particleIndex: Int = -1,
    val equilibriumX: Double = 2.0,
    val equilibriumY: Double = 0.0,
    val equilibriumZ: Double = 0.0,
    val sample: AcousticSample = AcousticSample(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
    val simulationTimeS: Double = 0.0
) {
    val isSelected: Boolean get() = particleIndex >= 0
    val instantaneousX: Double get() = equilibriumX + sample.displacementXM
    val instantaneousY: Double get() = equilibriumY + sample.displacementYM
    val instantaneousZ: Double get() = equilibriumZ + sample.displacementZM
}
