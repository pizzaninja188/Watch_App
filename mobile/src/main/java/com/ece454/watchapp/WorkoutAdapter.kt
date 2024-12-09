package com.ece454.watchapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter(private val workouts: List<Workout>) :
    RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val activityText: TextView = view.findViewById(R.id.activityText)
        val statsText: TextView = view.findViewById(R.id.statsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.activityText.text = "Workout ${position + 1}: ${workout.activity}"
        holder.statsText.text = "Duration: ${workout.duration}\n" +
                "Avg HR: ${workout.avgHeartRate.toInt()} BPM\n" +
                "Min HR: ${workout.minHeartRate.toInt()} BPM\n" +
                "Max HR: ${workout.maxHeartRate.toInt()} BPM"
    }

    override fun getItemCount() = workouts.size
}
