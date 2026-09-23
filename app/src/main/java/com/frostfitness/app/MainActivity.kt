package com.frostfitness.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Lime=Color(0xFFB7FF32); private val Bg=Color(0xFF090B0A); private val Card=Color(0xFF151816); private val Muted=Color(0xFFA8AEA9)
data class Profile(var goal:String="",var age:String="",var height:String="",var weight:String="",var experience:String="",var days:Int=3,var place:String="Зал",var health:String="")
data class Exercise(val name:String,val dose:String)
data class Workout(val name:String,val exercises:List<Exercise>)
data class Food(val name:String,val kcal:Int,val p:Int,val c:Int,val f:Int)
enum class Screen{WELCOME,GOAL,BASICS,EXP,DAYS,PLACE,HEALTH,HOME,WORKOUT,FOOD,PROGRESS,PROFILE}
class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}}

@Composable fun App(){
 var s by remember{mutableStateOf(Screen.WELCOME)};val p=remember{Profile()};var chosen by remember{mutableStateOf<Workout?>(null)};var completed by remember{mutableStateOf(0)};var foods by remember{mutableStateOf(listOf<Food>())}
 MaterialTheme(colorScheme=darkColorScheme(primary=Lime,background=Bg,surface=Card,onPrimary=Color.Black,onBackground=Color.White,onSurface=Color.White)){Surface(Modifier.fillMaxSize(),color=Bg){when(s){
  Screen.WELCOME->Page("Frost Fitness","Тренировки, питание и прогресс в одном приложении."){Info("FF создаёт программу по твоей цели, опыту, оборудованию и ограничениям здоровья.");Go("Создать программу"){s=Screen.GOAL}}
  Screen.GOAL->Select("Твоя цель",listOf("Снизить вес","Набрать мышцы","Стать сильнее","Поддерживать форму"),p.goal,{p.goal=it}){s=Screen.BASICS}
  Screen.BASICS->Page("Основные данные","Для персонализации и отслеживания прогресса."){Input("Возраст",p.age){p.age=it};Input("Рост, см",p.height){p.height=it};Input("Вес, кг",p.weight){p.weight=it};Go("Продолжить",p.age.isNotBlank()&&p.height.isNotBlank()&&p.weight.isNotBlank()){s=Screen.EXP}}
  Screen.EXP->Select("Опыт тренировок",listOf("Начинающий","Есть опыт","Продвинутый"),p.experience,{p.experience=it}){s=Screen.DAYS}
  Screen.DAYS->Page("Тренировки в неделю","Выбери реалистичный ритм."){Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){(2..5).forEach{n->FilterChip(selected=p.days==n,onClick={p.days=n},label={Text("$n")})}};Text("${p.days} раза в неделю",color=Muted);Go(){s=Screen.PLACE}}
  Screen.PLACE->Select("Где тренируемся?",listOf("Зал","Дом: гантели","Дом: без оборудования"),p.place,{p.place=it}){s=Screen.HEALTH}
  Screen.HEALTH->Page("Здоровье и ограничения","Обязательный шаг перед созданием программы."){Info("Укажи заболевания, травмы, медицинские ограничения, противопоказания и движения, которые вызывают боль. FF не ставит диагноз и не заменяет врача.");OutlinedTextField(p.health,{p.health=it},label={Text("Что нужно учитывать?")},placeholder={Text("Если ограничений нет — напиши «Нет»")},minLines=5,modifier=Modifier.fillMaxWidth());Go("Создать программу",p.health.isNotBlank()){s=Screen.HOME}}
  Screen.HOME->Home(p,completed,{chosen=it;s=Screen.WORKOUT},{s=Screen.FOOD},{s=Screen.PROGRESS},{s=Screen.PROFILE})
  Screen.WORKOUT->WorkoutPage(chosen?:plan(p).first(),{completed++;s=Screen.HOME}){s=Screen.HOME}
  Screen.FOOD->FoodPage(p,foods,{foods=foods+it}){s=Screen.HOME}
  Screen.PROGRESS->ProgressPage(p,completed){s=Screen.HOME}
  Screen.PROFILE->ProfilePage(p){s=Screen.HOME}
 }}}
}

fun plan(p:Profile):List<Workout>{val gym=listOf(Exercise("Жим ногами","3 × 8–12"),Exercise("Жим от груди","3 × 8–12"),Exercise("Тяга верхнего блока","3 × 8–12"),Exercise("Ягодичный мост","3 × 10–15"),Exercise("Планка","3 × 30–45 сек"));val home=listOf(Exercise("Присед к стулу","3 × 10–15"),Exercise("Отжимания от опоры","3 × 6–12"),Exercise("Ягодичный мост","3 × 12–15"),Exercise("Bird dog","3 × 8/сторона"));val e=if(p.place=="Зал")gym else home;return List(p.days){Workout("День ${it+1} • Всё тело",e)}}
@Composable fun Page(title:String,sub:String="",body:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text("FF",color=Lime,fontSize=30.sp,fontWeight=FontWeight.Black);Text(title,fontSize=30.sp,fontWeight=FontWeight.Bold);if(sub.isNotBlank())Text(sub,color=Muted);body();Spacer(Modifier.height(20.dp))}}
@Composable fun Info(t:String){Column(Modifier.fillMaxWidth().background(Card,RoundedCornerShape(20.dp)).padding(18.dp)){Text(t,lineHeight=21.sp)}}
@Composable fun Go(text:String="Продолжить",enabled:Boolean=true,click:()->Unit){Button(click,Modifier.fillMaxWidth().height(56.dp),enabled=enabled,shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Color.Black)){Text(text,fontWeight=FontWeight.Bold)}}
@Composable fun Input(label:String,value:String,set:(String)->Unit){OutlinedTextField(value,set,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth())}
@Composable fun Select(title:String,items:List<String>,value:String,set:(String)->Unit,next:()->Unit)=Page(title){items.forEach{FilterChip(value==it,{set(it)},{Text(it)},modifier=Modifier.fillMaxWidth())};Go(enabled=value.isNotBlank(),click=next)}

@Composable fun Home(p:Profile,completed:Int,start:(Workout)->Unit,food:()->Unit,progress:()->Unit,profile:()->Unit)=Page("Сегодня","${p.goal} • ${p.days}× в неделю"){
 Info("Программа сформирована по анкете. Если указаны ограничения здоровья, выполняй упражнения только в рамках рекомендаций врача и без боли.");plan(p).forEach{w->Column(Modifier.fillMaxWidth().background(Card,RoundedCornerShape(18.dp)).padding(16.dp)){Text(w.name,fontWeight=FontWeight.Bold);Text("${w.exercises.size} упражнений",color=Muted);Button({start(w)},colors=ButtonDefaults.buttonColors(containerColor=Lime,contentColor=Color.Black)){Text("Начать")}}};Text("Завершено тренировок: $completed",color=Muted);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){OutlinedButton(food,Modifier.weight(1f)){Text("Питание",fontSize=11.sp)};OutlinedButton(progress,Modifier.weight(1f)){Text("Прогресс",fontSize=11.sp)};OutlinedButton(profile,Modifier.weight(1f)){Text("Профиль",fontSize=11.sp)}}
}
@Composable fun WorkoutPage(w:Workout,finish:()->Unit,back:()->Unit)=Page(w.name,"Отмечай выполненные упражнения."){var done by remember(w.name){mutableStateOf(setOf<Int>())};w.exercises.forEachIndexed{i,e->Row(Modifier.fillMaxWidth().background(Card,RoundedCornerShape(16.dp)).padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween){Column{Text(e.name,fontWeight=FontWeight.Bold);Text(e.dose,color=Lime)};Checkbox(i in done,{done=if(it)done+i else done-i})}};Text("Отдых 60–120 сек. Остановись при боли, головокружении или необычных симптомах.",color=Muted);Go("Завершить",done.size==w.exercises.size,finish);OutlinedButton(back,Modifier.fillMaxWidth()){Text("Назад")}}
fun calories(p:Profile):Int{val kg=p.weight.replace(',','.').toDoubleOrNull()?:70.0;return when(p.goal){"Снизить вес"->(kg*27).toInt();"Набрать мышцы"->(kg*35).toInt();else->(kg*31).toInt()}.coerceIn(1400,4000)}
@Composable fun FoodPage(p:Profile,foods:List<Food>,add:(Food)->Unit,back:()->Unit)=Page("Питание","Ориентировочный дневник питания."){val target=calories(p);val used=foods.sumOf{it.kcal};Info("$used / $target ккал\nБелки ${foods.sumOf{it.p}} г • Жиры ${foods.sumOf{it.f}} г • Углеводы ${foods.sumOf{it.c}} г");listOf(Food("Завтрак",450,25,50,15),Food("Основной приём",650,40,70,20),Food("Перекус",250,15,30,8)).forEach{f->OutlinedButton({add(f)},Modifier.fillMaxWidth()){Text("+ ${f.name} • ${f.kcal} ккал")}};Text("Калорийность — ориентир, не лечебная диета. При медицинских состояниях питание согласовывается со специалистом.",color=Muted,fontSize=13.sp);OutlinedButton(back,Modifier.fillMaxWidth()){Text("Назад")}}
@Composable fun ProgressPage(p:Profile,completed:Int,back:()->Unit)=Page("Прогресс"){Info("$completed завершённых тренировок");Info("Текущий вес: ${p.weight} кг\nЦель: ${p.goal}");Text("Оценивай тренд за несколько недель, а не одно измерение.",color=Muted);OutlinedButton(back,Modifier.fillMaxWidth()){Text("Назад")}}
@Composable fun ProfilePage(p:Profile,back:()->Unit)=Page("Профиль","Изменения сразу влияют на программу."){Input("Возраст",p.age){p.age=it};Input("Рост, см",p.height){p.height=it};Input("Вес, кг",p.weight){p.weight=it};Text("Ограничения здоровья",color=Muted);OutlinedTextField(p.health,{p.health=it},minLines=3,modifier=Modifier.fillMaxWidth());Go("Сохранить",click=back)}
