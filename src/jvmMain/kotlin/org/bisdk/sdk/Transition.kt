package org.bisdk.sdk

import org.bisdk.toByteArray
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.util.*


data class Transition(
    /**
     * 100 is OPEN, 0 = CLOSED
     * 200 = UNLOCKED, 0 = LOCKED????
     */
    val stateInPercent: Int,
    /**
     * 100 is OPEN, 0 = CLOSED
     */
    val desiredStateInPercent: Int,
    val error: Boolean,
    val autoClose: Boolean,
    val driveTime: Int,
    val gk: Int,
    val hcp: HCP,
    val exst: List<Byte>,
    val time: LocalDateTime,
    val ignoreRetries: Boolean
) {

    fun isDriving() = driveTime != 0 || hcp.driving

    fun drivingDirection(): DriveDirection? = if (driveTime == 0 && hcp.driving) {
        if (hcp.drivingToClose) {
            DriveDirection.TO_CLOSE
        } else {
            DriveDirection.TO_OPEN
        }
    } else if (driveTime > 0) {
        if (desiredStateInPercent > stateInPercent) {
            DriveDirection.TO_OPEN
        } else {
            DriveDirection.TO_CLOSE
        }
    } else {
        null
    }

    companion object {
        @OptIn(ExperimentalUnsignedTypes::class)
        fun from(ba: ByteArray): Transition {
            val byte3 = BitSet.valueOf(ba[2].toByteArray())
            return Transition(
                stateInPercent = ba[0].toUByte().toInt() / 2,
                desiredStateInPercent = ba[1].toUByte().toInt() / 2,
                error = byte3[7],
                autoClose = byte3[6],
                driveTime = ba[3].toInt(),  // TODO: clear 6th and 7th bit from byte3 and shift add it here
                gk = ByteBuffer.wrap(ba.copyOfRange(4, 6)).short.toInt(),
                hcp = HCP.from(ba.copyOfRange(6, 8)),
                exst = ba.copyOfRange(8, 16).toList().reversed(),
                time = LocalDateTime.now(),
                ignoreRetries = true
            )
        }
    }
}


data class HCP(
    var positionOpen: Boolean,
    var positionClose: Boolean,
    var optionRelais: Boolean,
    var lightBarrier: Boolean,
    var error: Boolean,
    var drivingToClose: Boolean,
    var driving: Boolean,
    var halfOpened: Boolean,
    var forecastLeadTime: Boolean,
    var learned: Boolean,
    var notReferenced: Boolean
) {
    companion object {
        fun from(ba: ByteArray): HCP {
//            println("From bytes " + ba.toHexString())
            val bs = BitSet.valueOf(ba)
//            println("Bitset: $bs")
            return HCP(
                bs.get(0),
                bs.get(1),
                bs.get(2),
                bs.get(3),
                bs.get(4),
                bs.get(5),
                bs.get(6),
                bs.get(7),
                bs.get(8),
                bs.get(9),
                bs.get(10)
            )
        }
    }
}

enum class DriveDirection {
    TO_CLOSE, TO_OPEN
}
