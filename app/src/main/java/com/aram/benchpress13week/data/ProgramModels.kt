package com.aram.benchpress13week.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.round

data class WorkoutSet(
    val percent: Double,
    val reps: Int,
    val sets: Int,
)

data class WorkoutDay(
    val week: Int,
    val slot: Int,
    val label: String,
    val sets: List<WorkoutSet>,
)

data class GeneratedWorkoutDay(
    val week: Int,
    val slot: Int,
    val label: String,
    val date: LocalDate?,
    val sets: List<GeneratedWorkoutSet>,
)

data class GeneratedWorkoutSet(
    val percent: Double,
    val reps: Int,
    val sets: Int,
    val weightKg: Double,
)

data class UserProfile(
    val oneRepMaxKg: Double = 100.0,
    val trainingMaxPercent: Int = 100,
    val roundingStepKg: Double = 2.5,
    val startDate: LocalDate = LocalDate.now(),
)

object BenchProgramTemplate {
    val plan = listOf(
        WorkoutDay(1, 1, "Monday", listOf(WorkoutSet(0.75, 3, 5))),
        WorkoutDay(1, 2, "Friday", listOf(WorkoutSet(0.60, 5, 2))),
        WorkoutDay(2, 1, "Monday", listOf(WorkoutSet(0.70, 4, 2), WorkoutSet(0.77, 3, 5))),
        WorkoutDay(2, 2, "Friday", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 5))),
        WorkoutDay(3, 1, "Monday", listOf(WorkoutSet(0.80, 3, 6))),
        WorkoutDay(3, 2, "Friday", listOf(WorkoutSet(0.70, 5, 5))),
        WorkoutDay(4, 1, "Monday", listOf(
            WorkoutSet(0.70, 4, 2), WorkoutSet(0.75, 4, 2), WorkoutSet(0.77, 3, 2), WorkoutSet(0.85, 2, 2)
        )),
        WorkoutDay(4, 2, "Friday", listOf(WorkoutSet(0.82, 2, 3))),
        WorkoutDay(5, 1, "Monday", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.75, 3, 5))),
        WorkoutDay(5, 2, "Friday", listOf(
            WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 6), WorkoutSet(0.60, 6, 1), WorkoutSet(0.65, 5, 1),
            WorkoutSet(0.70, 4, 1), WorkoutSet(0.80, 3, 1), WorkoutSet(0.85, 2, 1), WorkoutSet(0.87, 1, 1),
            WorkoutSet(0.825, 3, 1), WorkoutSet(0.75, 4, 1), WorkoutSet(0.60, 6, 1), WorkoutSet(0.55, 8, 1)
        )),
        WorkoutDay(6, 1, "Monday", listOf(WorkoutSet(0.70, 4, 2), WorkoutSet(0.80, 3, 2), WorkoutSet(0.85, 2, 3))),
        WorkoutDay(6, 2, "Friday", listOf(WorkoutSet(0.80, 4, 4))),
        WorkoutDay(7, 1, "Monday", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 2), WorkoutSet(0.85, 2, 3), WorkoutSet(0.80, 1, 2))),
        WorkoutDay(7, 2, "Friday", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 1), WorkoutSet(0.85, 2, 3), WorkoutSet(0.87, 1, 2))),
        WorkoutDay(8, 1, "First", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 2), WorkoutSet(0.85, 2, 5))),
        WorkoutDay(8, 2, "Second", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 4, 4))),
        WorkoutDay(9, 1, "First", listOf(
            WorkoutSet(0.50, 8, 1), WorkoutSet(0.60, 6, 1), WorkoutSet(0.70, 5, 2), WorkoutSet(0.80, 4, 2),
            WorkoutSet(0.85, 3, 2), WorkoutSet(0.87, 2, 2)
        )),
        WorkoutDay(9, 2, "Second", listOf(WorkoutSet(0.70, 3, 2), WorkoutSet(0.80, 3, 2), WorkoutSet(0.85, 2, 3))),
        WorkoutDay(10, 1, "First", listOf(WorkoutSet(0.60, 6, 1), WorkoutSet(0.70, 5, 1), WorkoutSet(0.80, 4, 4))),
        WorkoutDay(10, 2, "Second", listOf(WorkoutSet(0.80, 2, 4))),
        WorkoutDay(11, 1, "Monday", listOf(
            WorkoutSet(0.60, 6, 1), WorkoutSet(0.70, 5, 1), WorkoutSet(0.80, 4, 1), WorkoutSet(0.85, 3, 1),
            WorkoutSet(0.90, 3, 1), WorkoutSet(0.925, 3, 1), WorkoutSet(0.95, 3, 1)
        )),
        WorkoutDay(11, 2, "Second", listOf(WorkoutSet(0.70, 5, 5), WorkoutSet(0.80, 2, 4))),
        WorkoutDay(12, 1, "First", listOf(WorkoutSet(0.70, 4, 2), WorkoutSet(0.80, 3, 1), WorkoutSet(0.825, 2, 3))),
        WorkoutDay(12, 2, "Second", listOf(WorkoutSet(0.70, 4, 4))),
        WorkoutDay(13, 1, "First", listOf(
            WorkoutSet(0.50, 6, 1), WorkoutSet(0.60, 5, 1), WorkoutSet(0.70, 3, 1), WorkoutSet(0.80, 2, 1),
            WorkoutSet(0.90, 1, 1), WorkoutSet(1.00, 1, 1), WorkoutSet(1.02, 1, 1)
        )),
    )
}

object BenchProgramGenerator {
    fun generate(profile: UserProfile): List<GeneratedWorkoutDay> {
        val trainingMax = profile.oneRepMaxKg * profile.trainingMaxPercent / 100.0
        return BenchProgramTemplate.plan.map { day ->
            GeneratedWorkoutDay(
                week = day.week,
                slot = day.slot,
                label = day.label,
                date = profile.startDate.plusDays(((day.week - 1) * 7L) + if (day.slot == 1) 0 else 4),
                sets = day.sets.map { set ->
                    GeneratedWorkoutSet(
                        percent = set.percent,
                        reps = set.reps,
                        sets = set.sets,
                        weightKg = roundToStep(trainingMax * set.percent, profile.roundingStepKg),
                    )
                }
            )
        }
    }

    fun currentWorkout(profile: UserProfile, today: LocalDate = LocalDate.now()): GeneratedWorkoutDay? {
        val all = generate(profile)
        return all.minByOrNull { workout ->
            val d = workout.date ?: return@minByOrNull Long.MAX_VALUE
            kotlin.math.abs(ChronoUnit.DAYS.between(today, d))
        }
    }

    private fun roundToStep(value: Double, step: Double): Double {
        if (step <= 0.0) return value
        return round(value / step) * step
    }
}
