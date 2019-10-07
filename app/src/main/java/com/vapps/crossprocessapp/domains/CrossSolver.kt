package com.vapps.crossprocessapp.domains

import java.util.*
import kotlin.math.abs
import kotlin.math.pow

class CrossSolver(
    private val mMembers: Array<CrossMember>,
    private val mIsInitHinged: Boolean,
    private val mIsEndHinged: Boolean,
    private val mFlexuralStiffness: Double,
    private val mDecimalPlaces: Int
) {
    private val mNodes: Array<CrossNode> = Array(mMembers.size - 1) { CrossNode() }

    constructor() : this(
        arrayOf(
            CrossMember(8.0, 8.0),
            CrossMember(6.0, 38.0),
            CrossMember(6.0, 28.0),
            CrossMember(2.0, -10.0)
        ),
        mIsInitHinged = true,
        mIsEndHinged = false,
        mFlexuralStiffness = 10_000.0,
        mDecimalPlaces = 3
    )

    init {
        initStiffness()
        initNodes()
    }

    fun initStiffness() {
        if (this.mIsInitHinged)
            mMembers[0].setRotStiffHingedClamped(mFlexuralStiffness)
        else
            mMembers[0].setRotStiffClampedClamped(mFlexuralStiffness)

        if (this.mIsEndHinged)
            mMembers[mMembers.lastIndex].setRotStiffHingedClamped(mFlexuralStiffness)
        else
            mMembers[mMembers.lastIndex].setRotStiffClampedClamped(mFlexuralStiffness)

        for (i in 1 until mMembers.lastIndex) {
            mMembers[i].setRotStiffClampedClamped(mFlexuralStiffness)
        }
    }

    fun initNodes() {
        for (i in mNodes.indices) {
            val totalStiffness = mMembers[i].rotStiffness + mMembers[i + 1].rotStiffness
            mNodes[i].momentCoeffLeft = mMembers[i].rotStiffness / totalStiffness
            mNodes[i].momentCoeffRight = mMembers[i + 1].rotStiffness / totalStiffness

            mNodes[i].transmissionLeftFac = 0.5
            mNodes[i].transmissionRightFac = 0.5
        }

        if (this.mIsInitHinged)
            mNodes[0].transmissionLeftFac = 0.0
        if (this.mIsEndHinged)
            mNodes[mNodes.lastIndex].transmissionRightFac = 0.0

        this.mNodes.forEach { it.nodeRotation = 0.0 }
    }

    fun initMoments() {
        if (this.mIsInitHinged)
            this.mMembers[0].setMomentsHingedClamped()
        else
            this.mMembers[0].setMomentsClampedClamped()


        if (this.mIsEndHinged)
            this.mMembers[mMembers.lastIndex].setMomentsClampedHinged()
        else
            this.mMembers[mMembers.lastIndex].setMomentsClampedClamped()

        mMembers.forEachIndexed { i, member ->
            if (i in 1 until mMembers.lastIndex) {
                member.setMomentsClampedClamped()
            }
        }
    }

    private fun tolerance(): Double = (10.0).pow(-(this.mDecimalPlaces + 1))

    private fun unbalanced(nodeInd: Int): Double = mMembers[nodeInd].rightMoment + mMembers[nodeInd + 1].leftMoment

    /*fun totalLength(): Double {
        var tl = 0.0
        mMembers.forEach { tl += it.length }
        return tl
    }*/

    /*fun isNodeUnbalanced(nodeInd: Int): Boolean {
        return abs(unbalanced(nodeInd)) > tolerance()
    }*/

    private fun maxUnbalancedNode(): Int {
        var maxUnbalanced = tolerance()
        var nodeInd = -1
        for (i in 0..this.mNodes.lastIndex) {
            val unbalanced = unbalanced(i)
            if (abs(unbalanced) > maxUnbalanced) {
                nodeInd = i
                maxUnbalanced = abs(unbalanced)
            }
        }
        return nodeInd
    }

    private fun processNode(nodeInd: Int) {
        val unbalanced = unbalanced(nodeInd)
        val balancingLeftMoment = -unbalanced * mNodes[nodeInd].momentCoeffLeft
        val balancingRightMoment = -unbalanced * mNodes[nodeInd].momentCoeffRight
        val momentToLeft = balancingLeftMoment * mNodes[nodeInd].transmissionLeftFac
        val momentToRight = balancingRightMoment * mNodes[nodeInd].transmissionRightFac

        mMembers[nodeInd].rightMoment += balancingLeftMoment
        mMembers[nodeInd].leftMoment += momentToLeft
        mMembers[nodeInd + 1].rightMoment += momentToRight
        mMembers[nodeInd + 1].leftMoment += balancingRightMoment

        val nodeRot = -unbalanced / (mMembers[nodeInd].rotStiffness + mMembers[nodeInd + 1].rotStiffness)
        mNodes[nodeInd].nodeRotation += nodeRot
    }

    /*fun nodeStepSolver(nodeInd: Int): Boolean {
        if (nodeInd < 0 || nodeInd > mNodes.lastIndex)
            return false

        if (abs(unbalanced(nodeInd)) < tolerance())
            return false

        this.processNode(nodeInd)
        return true
    }*/

    fun thereIsMoreSteps(): Boolean {
        val n = maxUnbalancedNode()
        return n > -1
    }

    fun autoStepSolver() {
        val nodeInd = maxUnbalancedNode()
        if (nodeInd > -1)
            this.processNode(nodeInd)
    }

    fun goThruSolver() {
        if (!thereIsMoreSteps())
            return
        while (thereIsMoreSteps())
            this.autoStepSolver()
    }

    fun printModelInfo() {
        print("\n=========================================================\n")
        print("         CROSS - Cross Process of Continuous Beam\n")
        print("    PONTIFICAL CATHOLIC UNIVERSITY OF RIO DE JANEIRO\n")
        print("   DEPARTMENT OF CIVIL AND ENVIRONMENTAL ENGINEERING\n")
        print("                            \n")
        print("   ALUNO: VINICIUS ALMADA (1913147)       \n")
        print("   CIV2801 - FUNDAMENTOS DE COMPUTACAO GRAFICA APLICADA\n")
        print("=========================================================\n")

        print("\n____________ M O D E L  I N F O R M A T I O N ____________\n")
        print(" MEMBERS  EI [kNm^2]    HINGEi HINGEf  LENGTH [m]  Distrib. Load [kN/m]\n")
        mMembers.forEachIndexed { i, it ->
            val hingei = if (i == 0 && mIsInitHinged) "yes" else "no"
            val hingef = if (i == mMembers.lastIndex && mIsEndHinged) "yes" else "no"
            val length = it.length
            val q = it.load

            val iStr = String.format(Locale.US, "%5d", i + 1)
            val eiStr = String.format(Locale.US, "%12d", mFlexuralStiffness.toLong())
            val hingeiStr = String.format(Locale.US, "%10s", hingei)
            val hingefStr = String.format(Locale.US, "%6s", hingef)
            val lenStr = String.format(Locale.US, "%8.2f", length)
            val qStr = String.format(Locale.US, "%15.2f", q)
            print("$iStr $eiStr $hingeiStr $hingefStr $lenStr $qStr\n")
        }
    }

    fun printResults() {
        print("\n_____________ A N A L Y S I S  R E S U L T S _____________\n")
        print(" MEMBERS      Mom.Init [kNm]   Mom.End [kNm]\n")
        mMembers.forEachIndexed { i, it ->
            val iStr = String.format(Locale.US, "%5d", i + 1)
            val leftMStr = String.format(Locale.US, "%15.${mDecimalPlaces}f", it.leftMoment)
            val rightMStr = String.format(Locale.US, "%16.${mDecimalPlaces}f", it.rightMoment)
            print("$iStr $leftMStr $rightMStr\n")
        }
    }
}