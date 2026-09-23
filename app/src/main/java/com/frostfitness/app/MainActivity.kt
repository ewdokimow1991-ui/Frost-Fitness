package com.frostfitness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FrostGreen = Color(0xFFB7FF32)
private val FrostBlack = Color(0xFF090B0A)
private val FrostSurface = Color(0xFF151816)
private val FrostMuted = Color(0xFFA8AEA9)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FrostFitnessApp() }
    }
}

private enum class Screen { Welcome, Goal, Profile, Health, Ready }

@Composable
private fun FrostFitnessApp() {
    var screen by remember { mutableStateOf(Screen.Welcome) }
    var goal by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var healthNotes by remember { mutableStateOf("") }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = FrostGreen,
            background = FrostBlack,
            surface = FrostSurface,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = FrostBlack) {
            when (screen) {
                Screen.Welcome -> Welcome { screen = Screen.Goal }
                Screen.Goal -> GoalScreen(goal, { goal = it }) { screen = Screen.Profile }
                Screen.Profile -> ProfileScreen(age, height, weight, { age = it }, { height = it }, { weight = it }) { screen = Screen.Health }
                Screen.Health -> HealthScreen(healthNotes, { healthNotes = it }) { screen = Screen.Ready }
                Screen.Ready -> ReadyScreen(goal, healthNotes)
            }
        }
    }
}

@Composable
private fun Page(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("FF", color = FrostGreen, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Text(title, fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = FrostMuted, fontSize = 16.sp, lineHeight = 22.sp)
        content()
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = FrostGreen, contentColor = Color.Black)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
private fun Welcome(next: () -> Unit) = Page(
    "Frost Fitness",
    "Персональные тренировки и питание, которые учитывают не только цель, но и твои реальные ограничения."
) {
    Spacer(Modifier.weight(1f))
    Box(
        Modifier.fillMaxWidth().background(FrostSurface, RoundedCornerShape(24.dp)).padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Твоя программа. Твой прогресс.", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Тренировки • питание • замеры • история", color = FrostMuted)
        }
    }
    PrimaryButton("Начать") { next() }
}

@Composable
private fun GoalScreen(goal: String, setGoal: (String) -> Unit, next: () -> Unit) = Page(
    "Какая у тебя цель?",
    "Это один из факторов программы. Дальше мы учтём опыт, оборудование и состояние здоровья."
) {
    listOf("Снизить вес", "Набрать мышцы", "Стать сильнее", "Поддерживать форму").forEach { item ->
        FilterChip(
            selected = goal == item,
            onClick = { setGoal(item) },
            label = { Text(item) },
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton("Продолжить", goal.isNotBlank(), next)
}

@Composable
private fun ProfileScreen(
    age: String, height: String, weight: String,
    setAge: (String) -> Unit, setHeight: (String) -> Unit, setWeight: (String) -> Unit,
    next: () -> Unit
) = Page("Основные данные", "Они нужны для персонализации нагрузки и отслеживания прогресса.") {
    FrostField("Возраст", age, setAge)
    FrostField("Рост, см", height, setHeight)
    FrostField("Вес, кг", weight, setWeight)
    Spacer(Modifier.weight(1f))
    PrimaryButton("Продолжить", age.isNotBlank() && height.isNotBlank() && weight.isNotBlank(), next)
}

@Composable
private fun FrostField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun HealthScreen(notes: String, setNotes: (String) -> Unit, next: () -> Unit) = Page(
    "Здоровье и ограничения",
    "Этот шаг обязателен. Укажи заболевания, травмы, ограничения врача, противопоказания или движения, которые вызывают боль."
) {
    Box(Modifier.fillMaxWidth().background(FrostSurface, RoundedCornerShape(18.dp)).padding(16.dp)) {
        Text("Frost Fitness не ставит диагнозы и не заменяет врача. Эти данные используются, чтобы не предлагать заведомо неподходящие упражнения и интенсивность.", color = FrostMuted)
    }
    OutlinedTextField(
        value = notes,
        onValueChange = setNotes,
        label = { Text("Что нужно учитывать?") },
        placeholder = { Text("Например: травма колена, ограничения по нагрузке…") },
        minLines = 5,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    )
    Text("Если ограничений нет, напиши «Нет».", color = FrostMuted, fontSize = 14.sp)
    Spacer(Modifier.weight(1f))
    PrimaryButton("Сохранить и продолжить", notes.isNotBlank(), next)
}

@Composable
private fun ReadyScreen(goal: String, healthNotes: String) = Page(
    "Профиль готов",
    "Следующий этап — генерация программы по цели, опыту, доступному оборудованию, частоте тренировок и ограничениям."
) {
    Box(Modifier.fillMaxWidth().background(FrostSurface, RoundedCornerShape(20.dp)).padding(18.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Цель", color = FrostMuted)
            Text(goal, fontWeight = FontWeight.Bold)
            Text("Ограничения учтены", color = FrostMuted)
            Text(if (healthNotes.equals("нет", true)) "Не указаны" else "Да", fontWeight = FontWeight.Bold)
        }
    }
    Spacer(Modifier.weight(1f))
    PrimaryButton("Перейти к программе") { }
}
