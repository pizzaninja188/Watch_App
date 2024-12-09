package com.ece454.watchapp

import android.os.Parcel
import android.os.Parcelable

data class Workout(
    val activity: String,
    val avgHeartRate: Float,
    val minHeartRate: Float,
    val maxHeartRate: Float,
    val duration: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(activity)
        parcel.writeFloat(avgHeartRate)
        parcel.writeFloat(minHeartRate)
        parcel.writeFloat(maxHeartRate)
        parcel.writeString(duration)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Workout> {
        override fun createFromParcel(parcel: Parcel): Workout {
            return Workout(parcel)
        }

        override fun newArray(size: Int): Array<Workout?> {
            return arrayOfNulls(size)
        }
    }
}
