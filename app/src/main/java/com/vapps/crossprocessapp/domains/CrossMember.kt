package com.vapps.crossprocessapp.domains

class CrossMember(val length: Double, val load: Double) {
    var leftMoment: Double = 0.0
    var rightMoment: Double = 0.0
    var rotStiffness: Double = 0.0

    fun setRotStiffHingedClamped(flexuralStiff: Double) {
        this.rotStiffness = 3.0 * flexuralStiff / this.length
    }

    fun setRotStiffClampedClamped(flexuralStiff: Double) {
        this.rotStiffness = 4.0 * flexuralStiff / this.length
    }

    fun setMomentsHingedClamped() {
        this.leftMoment = 0.0
        this.rightMoment = -load * (length * length) / 8.0
    }

    fun setMomentsClampedClamped() {
        this.leftMoment = load * (length * length) / 12.0
        this.rightMoment = -load * (length * length) / 12.0
    }

    fun setMomentsClampedHinged() {
        this.leftMoment = load * (length * length) / 8.0
        this.rightMoment = 0.0
    }
}