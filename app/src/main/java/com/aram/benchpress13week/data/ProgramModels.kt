package com.aram.benchpress13week.data

import java.time.LocalDate
import kotlin.math.round

enum class MaxType {
    BENCH,
    SQUAT,
    DEADLIFT,
    PRESS,
}

data class ExerciseTemplate(
    val name: String,
    val prescriptions: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
    val maxType: MaxType? = null,
)

data class ExerciseBlock(
    val primary: ExerciseTemplate,
    val alternative: ExerciseTemplate? = null,
)

data class ProgramDayTemplate(
    val index: Int,
    val week: Int,
    val dayCode: String,
    val dayOffset: Int,
    val exercises: List<ExerciseBlock>,
)

data class GeneratedExercise(
    val name: String,
    val prescriptions: List<String>,
    val notes: List<String> = emptyList(),
    val alternative: GeneratedExercise? = null,
)

data class GeneratedWorkoutDay(
    val index: Int,
    val week: Int,
    val dayLabel: String,
    val date: LocalDate,
    val exercises: List<GeneratedExercise>,
)

data class UserProfile(
    val benchMaxKg: Double = 100.0,
    val squatMaxKg: Double = 140.0,
    val deadliftMaxKg: Double = 170.0,
    val pressMaxKg: Double = 60.0,
    val accessoryWeightsKg: Map<String, Double> = emptyMap(),
    val roundingStepKg: Double = 2.5,
    val startDate: LocalDate = LocalDate.now(),
)

data class WorkoutProgress(
    val currentWorkoutIndex: Int = 0,
    val isPaused: Boolean = false,
)

object BenchProgramParser {
    private val weekRegex = Regex("""^НЕДЕЛЯ\s+(\d+)$""")
    private val exerciseRegex = Regex("""^\d+\.\s+(.*?)(?:\s+-\s+(.*))?$""")
    private val dayOffsets = mapOf("ПН" to 0, "СР" to 2, "ПТ" to 4)

    fun parse(text: String): List<ProgramDayTemplate> {
        val lines = text.lines()
        val days = mutableListOf<ProgramDayTemplate>()
        var currentWeek = 0
        var index = 0

        while (index < lines.size) {
            val trimmed = lines[index].trim()
            when {
                trimmed.isBlank() -> index++
                weekRegex.matches(trimmed) -> {
                    currentWeek = weekRegex.matchEntire(trimmed)!!.groupValues[1].toInt()
                    index++
                }

                dayOffsets.containsKey(trimmed) -> {
                    val dayCode = trimmed
                    index++
                    val blocks = mutableListOf<ExerciseBlock>()

                    while (index < lines.size) {
                        val line = lines[index].trim()
                        if (line.isBlank()) {
                            index++
                            continue
                        }
                        if (weekRegex.matches(line) || dayOffsets.containsKey(line)) {
                            break
                        }
                        if (exerciseRegex.matches(line)) {
                            val (primary, nextIndex) = parseExercise(lines, index)
                            index = nextIndex

                            var alternative: ExerciseTemplate? = null
                            if (index < lines.size && lines[index].trim() == "или") {
                                index++
                                if (index < lines.size && exerciseRegex.matches(lines[index].trim())) {
                                    val (alt, afterAlt) = parseExercise(lines, index)
                                    alternative = alt
                                    index = afterAlt
                                }
                            }

                            blocks += ExerciseBlock(primary = primary, alternative = alternative)
                        } else {
                            index++
                        }
                    }

                    days += ProgramDayTemplate(
                        index = days.size,
                        week = currentWeek,
                        dayCode = dayCode,
                        dayOffset = dayOffsets.getValue(dayCode),
                        exercises = blocks,
                    )
                }

                else -> index++
            }
        }

        return days
    }

    private fun parseExercise(lines: List<String>, startIndex: Int): Pair<ExerciseTemplate, Int> {
        val line = lines[startIndex].trim()
        val match = exerciseRegex.matchEntire(line)
            ?: error("Unexpected exercise line: $line")

        val name = match.groupValues[1].trim()
        val rawPrescription = match.groupValues.getOrNull(2)?.trim().orEmpty()
        val prescriptions = rawPrescription
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val notes = mutableListOf<String>()
        var index = startIndex + 1
        while (index < lines.size) {
            val next = lines[index].trim()
            if (
                next.isBlank() ||
                next == "или" ||
                weekRegex.matches(next) ||
                dayOffsets.containsKey(next) ||
                exerciseRegex.matches(next)
            ) {
                break
            }
            notes += next
            index++
        }

        return ExerciseTemplate(
            name = name,
            prescriptions = prescriptions,
            notes = notes,
            maxType = inferMaxType(name, prescriptions),
        ) to index
    }

    private fun inferMaxType(name: String, prescriptions: List<String>): MaxType? {
        if (prescriptions.isEmpty() || prescriptions.first().startsWith("[инд.]")) return null
        return when {
            name.contains("Присед", ignoreCase = true) -> MaxType.SQUAT
            name.contains("Тяга становая", ignoreCase = true) -> MaxType.DEADLIFT
            name.contains("Мертвая тяга", ignoreCase = true) -> MaxType.DEADLIFT
            name.contains("Жим штанги стоя", ignoreCase = true) -> MaxType.PRESS
            name.contains("Жим штанги сидя", ignoreCase = true) -> MaxType.PRESS
            name.contains("Жим штанги лежа", ignoreCase = true) -> MaxType.BENCH
            name.contains("Жим лежа", ignoreCase = true) -> MaxType.BENCH
            name.contains("Жим средним хватом", ignoreCase = true) -> MaxType.BENCH
            else -> null
        }
    }
}

object BenchProgramGenerator {
    fun generate(program: List<ProgramDayTemplate>, profile: UserProfile): List<GeneratedWorkoutDay> {
        return program.map { day ->
            GeneratedWorkoutDay(
                index = day.index,
                week = day.week,
                dayLabel = dayLabel(day.dayCode),
                date = profile.startDate.plusDays(((day.week - 1) * 7L) + day.dayOffset),
                exercises = day.exercises.map { block ->
                    generateExercise(block.primary, profile).copy(
                        alternative = block.alternative?.let { generateExercise(it, profile) }
                    )
                },
            )
        }
    }

    private fun generateExercise(template: ExerciseTemplate, profile: UserProfile): GeneratedExercise {
        return GeneratedExercise(
            name = template.name,
            prescriptions = template.prescriptions.map { formatPrescription(template.name, it, template.maxType, profile) },
            notes = template.notes,
        )
    }

    private fun formatPrescription(exerciseName: String, raw: String, maxType: MaxType?, profile: UserProfile): String {
        if (raw.startsWith("[инд.]")) {
            val parts = raw.split("x")
            val configuredWeight = profile.accessoryWeightsKg[exerciseName]
            val reps = parts.getOrNull(1) ?: "?"
            val sets = parts.getOrNull(2) ?: "?"
            return if (configuredWeight != null && configuredWeight > 0.0) {
                "${formatKg(configuredWeight)} kg · $reps reps x $sets sets"
            } else {
                "Set exact kg in Setup · $reps reps x $sets sets"
            }
        }

        val parts = raw.split("x")
        return when (parts.size) {
            2 -> "${parts[0]} reps x ${parts[1]} sets"
            3 -> {
                val first = parts[0]
                val reps = parts[1]
                val sets = parts[2]
                if (maxType != null) {
                    val percent = first.toDoubleOrNull()
                    if (percent != null) {
                        val weight = roundToStep(resolveMax(profile, maxType) * percent / 100.0, profile.roundingStepKg)
                        "${formatKg(weight)} kg (${formatNumber(percent)}%) · $reps reps x $sets sets"
                    } else {
                        "$first% · $reps reps x $sets sets"
                    }
                } else {
                    "${formatNumber(first.toDoubleOrNull() ?: 0.0)} kg · $reps reps x $sets sets"
                }
            }

            else -> raw
        }
    }

    private fun resolveMax(profile: UserProfile, maxType: MaxType): Double {
        return when (maxType) {
            MaxType.BENCH -> profile.benchMaxKg
            MaxType.SQUAT -> profile.squatMaxKg
            MaxType.DEADLIFT -> profile.deadliftMaxKg
            MaxType.PRESS -> profile.pressMaxKg
        }
    }

    private fun roundToStep(value: Double, step: Double): Double {
        if (step <= 0.0) return value
        return round(value / step) * step
    }

    private fun formatKg(value: Double): String = formatNumber(value)

    private fun formatNumber(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString()
        }
    }

    private fun dayLabel(dayCode: String): String {
        return when (dayCode) {
            "ПН" -> "Mon"
            "СР" -> "Wed"
            "ПТ" -> "Fri"
            else -> dayCode
        }
    }

    fun accessoryExerciseNames(program: List<ProgramDayTemplate>): List<String> {
        return program.flatMap { day ->
            day.exercises.flatMap { block ->
                listOfNotNull(block.primary, block.alternative)
            }
        }.filter { exercise ->
            exercise.prescriptions.any { it.startsWith("[инд.]") }
        }.map { it.name }
            .distinct()
            .sorted()
    }
}
