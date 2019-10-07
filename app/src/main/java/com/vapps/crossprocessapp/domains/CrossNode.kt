package com.vapps.crossprocessapp.domains

data class CrossNode(
    var momentCoeffLeft: Double,
    var momentCoeffRight: Double,
    var transmissionLeftFac: Double,
    var transmissionRightFac: Double,
    var nodeRotation: Double
) {
    constructor() : this(0.0, 0.0, 0.0, 0.0, 0.0)
}
