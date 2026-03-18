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

data class GeneratedSet(
    val id: String,
    val label: String,
    val isCompleted: Boolean,
)

data class GeneratedPrescription(
    val summary: String,
    val sets: List<GeneratedSet>,
)

data class GeneratedExercise(
    val name: String,
    val prescriptions: List<GeneratedPrescription>,
    val notes: List<String> = emptyList(),
    val alternative: GeneratedExercise? = null,
) {
    val totalSets: Int get() = prescriptions.sumOf { it.sets.size }
    val completedSets: Int get() = prescriptions.sumOf { prescription -> prescription.sets.count { it.isCompleted } }
}

data class GeneratedWorkoutDay(
    val index: Int,
    val week: Int,
    val dayLabel: String,
    val date: LocalDate,
    val exercises: List<GeneratedExercise>,
) {
    val totalSets: Int get() = exercises.sumOf { exerciseTotal(it) }
    val completedSets: Int get() = exercises.sumOf { exerciseCompleted(it) }

    private fun exerciseTotal(exercise: GeneratedExercise): Int {
        return exercise.totalSets + (exercise.alternative?.totalSets ?: 0)
    }

    private fun exerciseCompleted(exercise: GeneratedExercise): Int {
        return exercise.completedSets + (exercise.alternative?.completedSets ?: 0)
    }
}

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
    val completedSetIds: Set<String> = emptySet(),
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
                        if (weekRegex.matches(line) || dayOffsets.containsKey(line)) break
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
        val match = exerciseRegex.matchEntire(line) ?: error("Unexpected exercise line: $line")
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
    fun generate(
        program: List<ProgramDayTemplate>,
        profile: UserProfile,
        completedSetIds: Set<String>,
    ): List<GeneratedWorkoutDay> {
        return program.map { day ->
            GeneratedWorkoutDay(
                index = day.index,
                week = day.week,
                dayLabel = dayLabel(day.dayCode),
                date = profile.startDate.plusDays(((day.week - 1) * 7L) + day.dayOffset),
                exercises = day.exercises.mapIndexed { blockIndex, block ->
                    generateExercise(
                        template = block.primary,
                        profile = profile,
                        completedSetIds = completedSetIds,
                        exercisePath = "e${blockIndex}p",
                        workoutIndex = day.index,
                    ).copy(
                        alternative = block.alternative?.let {
                            generateExercise(
                                template = it,
                                profile = profile,
                                completedSetIds = completedSetIds,
                                exercisePath = "e${blockIndex}a",
                                workoutIndex = day.index,
                            )
                        }
                    )
                },
            )
        }
    }

    private fun generateExercise(
        template: ExerciseTemplate,
        profile: UserProfile,
        completedSetIds: Set<String>,
        exercisePath: String,
        workoutIndex: Int,
    ): GeneratedExercise {
        val translatedName = translateExerciseName(template.name)
        return GeneratedExercise(
            name = translatedName,
            prescriptions = template.prescriptions.mapIndexed { prescriptionIndex, raw ->
                generatePrescription(
                    exerciseName = translatedName,
                    raw = raw,
                    maxType = template.maxType,
                    profile = profile,
                    completedSetIds = completedSetIds,
                    workoutIndex = workoutIndex,
                    exercisePath = exercisePath,
                    prescriptionIndex = prescriptionIndex,
                )
            },
            notes = template.notes.map(::translateNote),
        )
    }

    private fun generatePrescription(
        exerciseName: String,
        raw: String,
        maxType: MaxType?,
        profile: UserProfile,
        completedSetIds: Set<String>,
        workoutIndex: Int,
        exercisePath: String,
        prescriptionIndex: Int,
    ): GeneratedPrescription {
        val parts = raw.split("x")
        if (raw.startsWith("[инд.]")) {
            val reps = parts.getOrNull(1) ?: "?"
            val setCount = parts.getOrNull(2)?.toIntOrNull() ?: 0
            val configuredWeight = profile.accessoryWeightsKg[exerciseName]
            val summary = if (configuredWeight != null && configuredWeight > 0.0) {
                "${formatKg(configuredWeight)} kg · $reps reps x $setCount sets"
            } else {
                "Set exact kg in Setup · $reps reps x $setCount sets"
            }
            return GeneratedPrescription(
                summary = summary,
                sets = (1..setCount).map { setNumber ->
                    val id = setId(workoutIndex, exercisePath, prescriptionIndex, setNumber - 1)
                    GeneratedSet(
                        id = id,
                        label = if (configuredWeight != null && configuredWeight > 0.0) {
                            "$setNumber x $reps @ ${formatKg(configuredWeight)} kg"
                        } else {
                            "$setNumber x $reps"
                        },
                        isCompleted = id in completedSetIds,
                    )
                }
            )
        }

        return when (parts.size) {
            2 -> {
                val reps = parts[0]
                val setCount = parts[1].toIntOrNull() ?: 0
                GeneratedPrescription(
                    summary = "$reps reps x $setCount sets",
                    sets = (1..setCount).map { setNumber ->
                        val id = setId(workoutIndex, exercisePath, prescriptionIndex, setNumber - 1)
                        GeneratedSet(
                            id = id,
                            label = "$setNumber x $reps",
                            isCompleted = id in completedSetIds,
                        )
                    }
                )
            }

            3 -> {
                val first = parts[0]
                val reps = parts[1]
                val setCount = parts[2].toIntOrNull() ?: 0
                val descriptor = buildDescriptor(first, reps, setCount, maxType, profile)
                val setSuffix = descriptor.setLabelSuffix
                GeneratedPrescription(
                    summary = descriptor.summary,
                    sets = (1..setCount).map { setNumber ->
                        val id = setId(workoutIndex, exercisePath, prescriptionIndex, setNumber - 1)
                        GeneratedSet(
                            id = id,
                            label = "$setNumber x $reps$setSuffix",
                            isCompleted = id in completedSetIds,
                        )
                    }
                )
            }

            else -> GeneratedPrescription(summary = raw, sets = emptyList())
        }
    }

    private fun buildDescriptor(
        first: String,
        reps: String,
        setCount: Int,
        maxType: MaxType?,
        profile: UserProfile,
    ): PrescriptionDescriptor {
        if (maxType != null) {
            val percent = first.toDoubleOrNull()
            if (percent != null) {
                val weight = roundToStep(resolveMax(profile, maxType) * percent / 100.0, profile.roundingStepKg)
                return PrescriptionDescriptor(
                    summary = "${formatKg(weight)} kg (${formatNumber(percent)}%) · $reps reps x $setCount sets",
                    setLabelSuffix = " @ ${formatKg(weight)} kg",
                )
            }
        }

        val weight = first.toDoubleOrNull()
        return if (weight != null) {
            PrescriptionDescriptor(
                summary = "${formatKg(weight)} kg · $reps reps x $setCount sets",
                setLabelSuffix = " @ ${formatKg(weight)} kg",
            )
        } else {
            PrescriptionDescriptor(
                summary = "$first · $reps reps x $setCount sets",
                setLabelSuffix = "",
            )
        }
    }

    private data class PrescriptionDescriptor(
        val summary: String,
        val setLabelSuffix: String,
    )

    private fun setId(workoutIndex: Int, exercisePath: String, prescriptionIndex: Int, setIndex: Int): String {
        return "$workoutIndex|$exercisePath|$prescriptionIndex|$setIndex"
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
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }

    private fun dayLabel(dayCode: String): String {
        return when (dayCode) {
            "ПН" -> "Monday"
            "СР" -> "Wednesday"
            "ПТ" -> "Friday"
            else -> dayCode
        }
    }

    fun accessoryExerciseNames(program: List<ProgramDayTemplate>): List<String> {
        return program.flatMap { day ->
            day.exercises.flatMap { block -> listOfNotNull(block.primary, block.alternative) }
        }.filter { exercise ->
            exercise.prescriptions.any { it.startsWith("[инд.]") }
        }.map { translateExerciseName(it.name) }
            .distinct()
            .sorted()
    }

    private fun translateExerciseName(name: String): String {
        return when (name) {
            "Вертикальная тяга блока к груди" -> "Lat Pulldown"
            "Гиперэкстензия" -> "Hyperextensions"
            "Гиперэкстензия с весом" -> "Weighted Hyperextensions"
            "Горизонтальная тяга блока к поясу" -> "Seated Cable Row"
            "Жим гантелей лежа" -> "Dumbbell Bench Press"
            "Жим гантелей лежа на скамье 30°" -> "30° Incline Dumbbell Press"
            "Жим гантелей сидя" -> "Seated Dumbbell Press"
            "Жим лежа в слингшоте или с бруска 15 см" -> "Slingshot Bench or 15 cm Board Press"
            "Жим лежа негативный" -> "Negative Bench Press"
            "Жим лежа средним хватом" -> "Medium Grip Bench Press"
            "Жим средним хватом" -> "Medium Grip Bench Press"
            "Жим штанги лежа" -> "Bench Press"
            "Жим штанги лежа на скамье 30°" -> "30° Incline Barbell Press"
            "Жим штанги лежа скоростной" -> "Speed Bench Press"
            "Жим штанги лежа узким хватом" -> "Close Grip Bench Press"
            "Жим штанги сидя" -> "Seated Barbell Press"
            "Жим штанги стоя" -> "Standing Barbell Press"
            "И так до потолка, жмем на 1 раз увеличивая вес." -> "Keep increasing the weight for singles"
            "Махи гантелей в сторону" -> "Dumbbell Lateral Raise"
            "Медитация в зале" -> "Meditation in the Gym"
            "Мертвая тяга" -> "Stiff-Leg Deadlift"
            "Негативные подтягивания" -> "Negative Pull-Ups"
            "Подтягивания узким хватом" -> "Close Grip Pull-Ups"
            "Подтягивания широким хватом" -> "Wide Grip Pull-Ups"
            "Пресс" -> "Abs"
            "Приседания" -> "Squats"
            "Разводка гантелей лежа на скамье" -> "Dumbbell Fly"
            "Разводка гантелей лежа на скамье 30°" -> "30° Incline Dumbbell Fly"
            "Разгибания на трицепс в блоке" -> "Cable Triceps Pushdown"
            "Разгибание бедра в тренажере" -> "Leg Extension Machine"
            "СУПЕРСЕТ" -> "Superset"
            "Сгибание на бицепс обратным хватом" -> "Reverse Grip Curl"
            "Сгибание штанги на бицепс" -> "Barbell Curl"
            "Сгибания кисти" -> "Wrist Curl"
            "Сгибания на бицепс обратным хватом" -> "Reverse Grip Curl"
            "Сгибания на бицепс с гантелями \"молотки\"" -> "Hammer Curl"
            "Сгибания штанги на бицепс" -> "Barbell Curl"
            "Сгибания штанги на бицепс обратным хватом" -> "Reverse Grip Barbell Curl"
            "Тяга Т-грифа" -> "T-Bar Row"
            "Тяга гантели в наклоне" -> "One-Arm Dumbbell Row"
            "Тяга становая" -> "Deadlift"
            "Французский жим" -> "French Press"
            else -> name
        }
    }

    private fun translateNote(note: String): String {
        return when (note) {
            "(см. Глоссарий)" -> "(see glossary)"
            "Вас погубит жадность. Не прибавляйте вес." -> "Greed will ruin this session. Do not add weight."
            "Если грядут соревнования - разбираем штангу." -> "If a competition is coming up, rack it after this."
            "И берём 13 дней на полное восстановление ЦНС." -> "Take 13 days for full CNS recovery."
            "Опускаем медленно и взрыв вверх." -> "Lower the bar slowly and drive up explosively."
            "Отдых между подходами не более 1 мин." -> "Rest no more than 1 minute between sets."
            "Это тренировка духа. Тренировка воли." -> "This is a test of discipline and willpower."
            "легко" -> "easy"
            "негативный жим это только фаза опускания штанги на грудь, а вверх поднимает страхующий" ->
                "Negative bench means only the lowering phase. Your spotter helps lift it back up."
            "очень аккуратно" -> "very carefully"
            "очень легко" -> "very easy"
            "трицепс очень аккуратно" -> "be very careful with the triceps work"
            else -> note
        }
    }
}
