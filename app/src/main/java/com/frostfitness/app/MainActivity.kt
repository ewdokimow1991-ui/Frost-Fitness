package com.frostfitness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Lime = Color(0xFFB7FF32)
private val Bg = Color(0xFF090B0A)
private val Card = Color(0xFF151816)
private val Muted = Color(0xFFA8AEA9)
private val Danger = Color(0xFFFF6B6B)

data class UserProfile(var goal:String="", var age:String="", var height:String="", var weight:String="", var experience:String="", var days:Int=3, var equipment:String="Зал", var health:String="")
data class Exercise(val name:String,val sets:Int,val reps:String,val note:String="")
data class Workout(val title:String,val subtitle:String,val exercises:List<Exercise>)
data class FoodEntry(val name:String,val kcal:Int,val protein:Int,val carbs:Int,val fat:Int)

enum class AppScreen { Welcome, Goal, Basics, Experience, Schedule, Equipment, Health, Home, Workout, Nutrition, Progress, Profile }

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { FrostFitness() } }
}

@Composable fun FrostFitness(){
 var screen by remember { mutableStateOf(AppScreen.Welcome) }
 val profile = remember { UserProfile() }
 var activeWorkout by remember { mutableStateOf<Workout?>(null) }
 var completed by remember { mutableStateOf(0) }
 var foods by remember { mutableStateOf(listOf<FoodEntry>()) }
 val scheme=darkColorScheme(primary=Lime,background=Bg,surface=Card,onPrimary=Color.Black,onBackground=Color.White,onSurface=Color.White,error=Danger)
 MaterialTheme(colorScheme=scheme){ Surface(Modifier.fillMaxSize(),color=Bg){
  when(screen){
   AppScreen.Welcome -> Welcome { screen=AppScreen.Goal }
   AppScreen.Goal -> ChoicePage("Твоя цель","Программа будет строиться вокруг неё.",listOf("Снизить вес","Набрать мышцы","Стать сильнее","Поддерживать форму"),profile.goal,{profile.goal=it}){screen=AppScreen.Basics}
   AppScreen.Basics -> Basics(profile){screen=AppScreen.Experience}
   AppScreen.Experience -> ChoicePage("Опыт тренировок","Это влияет на объём и сложность.",listOf("Начинающий","Есть опыт","Продвинутый"),profile.experience,{profile.experience=it}){screen=AppScreen.Schedule}
   AppScreen.Schedule -> Schedule(profile){screen=AppScreen.Equipment}
   AppScreen.Equipment -> ChoicePage("Где тренируемся?","Упражнения подберутся под доступное оборудование.",listOf("Зал","Дом: гантели","Дом: без оборудования"),profile.equipment,{profile.equipment=it}){screen=AppScreen.Health}
   AppScreen.Health -> Health(profile){screen=AppScreen.Home}
   AppScreen.Home -> Home(profile,completed,{w->activeWorkout=w;screen=AppScreen.Workout},{screen=AppScreen.Nutrition},{screen=AppScreen.Progress},{screen=AppScreen.Profile})
   AppScreen.Workout -> WorkoutScreen(activeWorkout?:program(profile).first(),{completed++;screen=AppScreen.Home},{screen=AppScreen.Home})
   AppScreen.Nutrition -> Nutrition(profile,foods,{foods=foods+it},{screen=AppScreen.Home})
   AppScreen.Progress -> Progress(profile,completed,{screen=AppScreen.Home})
   AppScreen.Profile -> Profile(profile,{screen=AppScreen.Home})
  }
 }}
}

fun program(p:UserProfile):List<Workout>{
 val caution=!p.health.trim().equals("нет",true) && p.health.isNotBlank()
 val note=if(caution) "Выполняй только если движение не противоречит указанным ограничениям и не вызывает боли." else "Оставляй 2–3 повтора в запасе."
 val gym=listOf(
  Workout("Тренировка A","Всё тело • 45–60 мин",listOf(Exercise("Жим ногами",3,"8–12",note),Exercise("Жим от груди",3,"8–12",note),Exercise("Тяга верхнего блока",3,"8–12",note),Exercise("Румынская тяга с лёгким весом",2,"8–10",note),Exercise("Планка",3,"30–45 сек",note))),
  Workout("Тренировка B","Всё тело • 45–60 мин",listOf(Exercise("Присед к скамье",3,"8–12",note),Exercise("Горизонтальная тяга",3,"8–12",note),Exercise("Жим гантелей сидя",2,"8–12",note),Exercise("Сгибание ног",3,"10–15",note),Exercise("Dead bug",3,"8–12/сторона",note))),
  Workout("Тренировка C","Всё тело • 45–60 мин",listOf(Exercise("Гоблет-присед",3,"8–12",note),Exercise("Тяга гантели",3,"8–12",note),Exercise("Отжимания / жим",3,"8–12",note),Exercise("Ягодичный мост",3,"10–15",note),Exercise("Боковая планка",2,"20–40 сек",note))))
 val home=listOf(
  Workout("Домашняя A","Всё тело • 30–40 мин",listOf(Exercise("Присед к стулу",3,"10–15",note),Exercise("Отжимания от опоры",3,"6–12",note),Exercise("Ягодичный мост",3,"12–15",note),Exercise("Bird dog",3,"8/сторона",note))),
  Workout("Домашняя B","Всё тело • 30–40 мин",listOf(Exercise("Выпад назад с опорой",3,"8/сторона",note),Exercise("Отжимания",3,"6–12",note),Exercise("Good morning без веса",3,"12–15",note),Exercise("Dead bug",3,"8/сторона",note))))
 val base=if(p.equipment=="Зал") gym else home
 return List(p.days.coerceIn(2,5)){base[it%base.size].copy(title="День ${it+1} • ${base[it%base.size].title}")}
}

@Composable fun Shell(title:String,sub:String="",back:(()->Unit)?=null,body:@Composable ColumnScope.()->Unit){
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){ if(back!=null) Text("‹",fontSize=38.sp,modifier=Modifier.clickable{back()}.padding(end=14.dp)); Text("FF",color=Lime,fontSize=28.sp,fontWeight=FontWeight.Black) }
  Text(title,fontSize=30.sp,lineHeight=34.sp,fontWeight=FontWeight.Bold); if(sub.isNotBlank()) Text(sub,color=Muted,lineHeight=21.sp); body(); Spacer(Modifier.height(18.dp))
 }
}
@Composable fun Next(text:String="Продолжить",enabled:Boolean=true,onClick:()->Unit){Button(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Color.Black)){Text(text,fontWeight=FontWeight.Bold)}}
@Composable fun FrostCard(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxWidth().background(Card,RoundedCornerShape(20.dp)).padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)}

@Composable fun Welcome(next:()->Unit)=Shell("Frost Fitness","Персональные тренировки, питание и прогресс — в одном месте."){
 Spacer(Modifier.height(30.dp)); FrostCard{Text("Твоя программа. Твой темп.",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("FF учитывает цель, опыт, доступное оборудование и ограничения здоровья.",color=Muted)}; Spacer(Modifier.height(24.dp));Next("Создать программу",onClick=next)
}
@Composable fun ChoicePage(title:String,sub:String,items:List<String>,selected:String,set:(String)->Unit,next:()->Unit)=Shell(title,sub){items.forEach{v->FilterChip(selected=selected==v,onClick={set(v)},label={Text(v)},modifier=Modifier.fillMaxWidth())};Spacer(Modifier.height(10.dp));Next(enabled=selected.isNotBlank(),onClick=next)}
@Composable fun Field(label:String,value:String,set:(String)->Unit){OutlinedTextField(value=value,onValueChange=set,label={Text(label)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
@Composable fun Basics(p:UserProfile,next:()->Unit)=Shell("Основные данные","Нужны для персонализации и отслеживания изменений."){Field("Возраст",p.age,{p.age=it});Field("Рост, см",p.height,{p.height=it});Field("Вес, кг",p.weight,{p.weight=it});Next(enabled=p.age.isNotBlank()&&p.height.isNotBlank()&&p.weight.isNotBlank(),onClick=next)}
@Composable fun Schedule(p:UserProfile,next:()->Unit)=Shell("Сколько тренировок?","Выбери реалистичный ритм на неделю."){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){(2..5).forEach{n->FilterChip(selected=p.days==n,onClick={p.days=n},label={Text("$n")},modifier=Modifier.weight(1f))}};Text("${p.days} тренировок в неделю",color=Muted);Next(onClick=next)}
@Composable fun Health(p:UserProfile,next:()->Unit)=Shell("Здоровье и ограничения","Обязательный шаг перед созданием программы."){FrostCard{Text("Укажи заболевания, травмы, медицинские ограничения, противопоказания и движения, которые вызывают боль.",fontWeight=FontWeight.Bold);Text("FF не ставит диагноз и не заменяет врача. При острых симптомах или запрете врача тренировка не должна выполняться.",color=Muted)};OutlinedTextField(value=p.health,onValueChange={p.health=it},label={Text("Что нужно учитывать?")},placeholder={Text("Если ограничений нет — напиши «Нет»")},minLines=5,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp));Next("Создать программу",p.health.isNotBlank(),next)}

@Composable fun Home(p:UserProfile,completed:Int,openWorkout:(Workout)->Unit,nutrition:()->Unit,progress:()->Unit,profile:()->Unit)=Shell("Сегодня","${p.goal} • ${p.days}× в неделю"){
 val workouts=program(p); FrostCard{Text("Твоя программа",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("Сформирована с учётом анкеты и ограничений.",color=Muted)}
 workouts.forEach{w->FrostCard{Text(w.title,fontWeight=FontWeight.Bold);Text(w.subtitle,color=Muted);Button(onClick={openWorkout(w)},colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Color.Black)){Text("Начать")}}}
 Text("Разделы",fontSize=20.sp,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){SmallNav("Питание",Modifier.weight(1f),nutrition);SmallNav("Прогресс",Modifier.weight(1f),progress);SmallNav("Профиль",Modifier.weight(1f),profile)};Text("Завершено тренировок: $completed",color=Muted)
}
@Composable fun SmallNav(text:String,modifier:Modifier=Modifier,onClick:()->Unit){OutlinedButton(onClick=onClick,modifier=modifier.height(52.dp)){Text(text,fontSize=12.sp)}}

@Composable fun WorkoutScreen(w:Workout,finish:()->Unit,back:()->Unit)=Shell(w.title,w.subtitle,back){
 var done by remember(w.title){mutableStateOf(setOf<Int>())};w.exercises.forEachIndexed{i,e->FrostCard{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column(Modifier.weight(1f)){Text(e.name,fontWeight=FontWeight.Bold);Text("${e.sets} × ${e.reps}",color=Lime);if(e.note.isNotBlank())Text(e.note,color=Muted,fontSize=13.sp)};Checkbox(checked=i in done,onCheckedChange={done=if(it)done+i else done-i})}};Text("Отдых между подходами: 60–120 сек. Увеличивай нагрузку только при уверенной технике.",color=Muted);Next("Завершить тренировку",done.size==w.exercises.size,finish)
}

fun targets(p:UserProfile):Int { val kg=p.weight.replace(',','.').toDoubleOrNull()?:70.0; return when(p.goal){"Снизить вес"->(kg*27).toInt();"Набрать мышцы"->(kg*35).toInt();else->(kg*31).toInt()}.coerceIn(1400,4000) }
@Composable fun Nutrition(p:UserProfile,foods:List<FoodEntry>,add:(FoodEntry)->Unit,back:()->Unit)=Shell("Питание","Ориентир, а не медицинская диета.",back){
 val target=targets(p);val used=foods.sumOf{it.kcal};FrostCard{Text("$used / $target ккал",fontSize=24.sp,fontWeight=FontWeight.Bold);LinearProgressIndicator(progress={ (used.toFloat()/target).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth());Text("Белки ${foods.sumOf{it.protein}} г • Жиры ${foods.sumOf{it.fat}} г • Углеводы ${foods.sumOf{it.carbs}} г",color=Muted)};Text("Быстро добавить",fontWeight=FontWeight.Bold);listOf(FoodEntry("Завтрак",450,25,50,15),FoodEntry("Основной приём пищи",650,40,70,20),FoodEntry("Перекус",250,15,30,8)).forEach{f->OutlinedButton(onClick={add(f)},modifier=Modifier.fillMaxWidth()){Text("+ ${f.name} • ${f.kcal} ккал")}};Text("Расчёт калорий приблизительный. При заболеваниях, беременности, РПП или лечебной диете питание следует согласовать со специалистом.",color=Muted,fontSize=13.sp)
}
@Composable fun Progress(p:UserProfile,completed:Int,back:()->Unit)=Shell("Прогресс","Главное — последовательность, а не идеальная неделя.",back){FrostCard{Text("$completed",fontSize=38.sp,fontWeight=FontWeight.Black,color=Lime);Text("тренировок завершено")};FrostCard{Text("Текущий вес",color=Muted);Text("${p.weight} кг",fontSize=26.sp,fontWeight=FontWeight.Bold)};FrostCard{Text("Цель",color=Muted);Text(p.goal,fontWeight=FontWeight.Bold);Text("Рекомендуется оценивать тренд по нескольким неделям, а не по одному измерению.",color=Muted)}}
@Composable fun Profile(p:UserProfile,back:()->Unit)=Shell("Профиль","Анкету можно менять — программа обновится автоматически.",back){Text("Цель",color=Muted);ChoiceInline(listOf("Снизить вес","Набрать мышцы","Стать сильнее","Поддерживать форму"),p.goal){p.goal=it};Field("Возраст",p.age){p.age=it};Field("Рост, см",p.height){p.height=it};Field("Вес, кг",p.weight){p.weight=it};Text("Тренировок в неделю: ${p.days}",fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){(2..5).forEach{n->FilterChip(selected=p.days==n,onClick={p.days=n},label={Text("$n")})}};Text("Ограничения здоровья",color=Muted);OutlinedTextField(value=p.health,onValueChange={p.health=it},minLines=3,modifier=Modifier.fillMaxWidth());Next("Сохранить",onClick=back)}
@Composable fun ChoiceInline(items:List<String>,selected:String,set:(String)->Unit){Column(verticalArrangement=Arrangement.spacedBy(6.dp)){items.forEach{FilterChip(selected=it==selected,onClick={set(it)},label={Text(it)},modifier=Modifier.fillMaxWidth())}}}
