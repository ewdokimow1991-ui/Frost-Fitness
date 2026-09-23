package com.frostfitness.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val Accent=Color(0xFF9BE52A)
private val LightBg=Color(0xFFF2F2F7); private val LightCard=Color.White; private val LightInk=Color(0xFF111111); private val LightSub=Color(0xFF6E6E73)
private val DarkBg=Color.Black; private val DarkCard=Color(0xFF1C1C1E); private val DarkInk=Color(0xFFF5F5F7); private val DarkSub=Color(0xFF98989D)
private var UiBg=LightBg; private var UiCard=LightCard; private var UiInk=LightInk; private var UiSub=LightSub

enum class ThemeMode{SYSTEM,LIGHT,DARK}
enum class Screen{WELCOME,THEME,BASICS,DETAILS,GOAL,EXP,DAYS,PLACE,EQUIPMENT,HEALTH,HOME,PROFILE}
class Profile{var name by mutableStateOf("");var city by mutableStateOf("");var about by mutableStateOf("");var occupation by mutableStateOf("");var goal by mutableStateOf("");var age by mutableStateOf("");var height by mutableStateOf("");var weight by mutableStateOf("");var experience by mutableStateOf("");var days by mutableIntStateOf(3);var place by mutableStateOf("Зал");var equipment by mutableStateOf("");var health by mutableStateOf("");var photoUri by mutableStateOf("")}
data class Measurement(val date:String,val values:Map<String,String>,val photos:Map<String,String> = emptyMap())

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{App()}}}

@Composable fun App(){
 val c=LocalContext.current; val p=remember{loadProfile(c)}; var screen by remember{mutableStateOf(loadScreen(c))}; var mode by remember{mutableStateOf(loadTheme(c))}; var measurements by remember{mutableStateOf(loadMeasurements(c))}
 val dark=when(mode){ThemeMode.SYSTEM->isSystemInDarkTheme();ThemeMode.LIGHT->false;ThemeMode.DARK->true}; UiBg=if(dark)DarkBg else LightBg;UiCard=if(dark)DarkCard else LightCard;UiInk=if(dark)DarkInk else LightInk;UiSub=if(dark)DarkSub else LightSub
 LaunchedEffect(screen,mode,p.name,p.city,p.about,p.occupation,p.goal,p.age,p.height,p.weight,p.experience,p.days,p.place,p.equipment,p.health,p.photoUri,measurements){save(c,p,screen,mode,measurements)}
 MaterialTheme(colorScheme=if(dark)darkColorScheme(background=UiBg,surface=UiCard,onBackground=UiInk,onSurface=UiInk) else lightColorScheme(background=UiBg,surface=UiCard,onBackground=UiInk,onSurface=UiInk)){Surface(Modifier.fillMaxSize(),color=UiBg){
  when(screen){
   Screen.WELCOME->Welcome{screen=Screen.THEME}; Screen.THEME->ThemePick(mode,{mode=it}){screen=Screen.BASICS}; Screen.BASICS->Basics(p){screen=Screen.DETAILS}; Screen.DETAILS->Details(p){screen=Screen.GOAL}
   Screen.GOAL->Choice("Какая у тебя цель?",listOf("Снизить вес","Набрать мышцы","Стать сильнее","Поддерживать форму"),p.goal,{p.goal=it}){screen=Screen.EXP}
   Screen.EXP->Choice("Опыт тренировок",listOf("Начинающий","До 1 года","1–3 года","Более 3 лет"),p.experience,{p.experience=it}){screen=Screen.DAYS}
   Screen.DAYS->Page("Ритм тренировок","Сколько раз в неделю тебе удобно заниматься?"){Segment((2..5).map{it.toString()},p.days.toString()){p.days=it.toInt()};Primary{screen=Screen.PLACE}}
   Screen.PLACE->PlaceScreen(p){screen=if(p.place=="Дом")Screen.EQUIPMENT else Screen.HEALTH}; Screen.EQUIPMENT->EquipmentScreen(p){screen=Screen.HEALTH}; Screen.HEALTH->HealthScreen(p){screen=Screen.HOME}; Screen.HOME->Home(p){screen=Screen.PROFILE}; Screen.PROFILE->ProfilePage(p,mode,{mode=it},measurements,{measurements=measurements+it}){screen=Screen.HOME}
  }
 }}}

@Composable fun Welcome(next:()->Unit)=Page("Добро пожаловать\nв Frost Fitness","Твой персональный путь к форме начинается здесь."){Card{Text("Фростик — твой спортивный помощник",fontSize=24.sp,fontWeight=FontWeight.Bold);Text("Помогу настроить тренировки и следить за прогрессом.",color=UiSub)};Primary("Заполнить анкету",click=next)}
@Composable fun ThemePick(mode:ThemeMode,set:(ThemeMode)->Unit,next:()->Unit)=Page("Выбери оформление"){Select("Светлая тема",mode==ThemeMode.LIGHT){set(ThemeMode.LIGHT)};Select("Как на телефоне",mode==ThemeMode.SYSTEM){set(ThemeMode.SYSTEM)};Select("Тёмная тема",mode==ThemeMode.DARK){set(ThemeMode.DARK)};Primary(click=next)}

@Composable fun PhotoPicker(uri:String,onPicked:(String)->Unit){val c=LocalContext.current;val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u:Uri?->if(u!=null){runCatching{c.contentResolver.takePersistableUriPermission(u,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)};onPicked(u.toString())}};Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){if(uri.isNotBlank())UriImage(uri,110);Secondary(if(uri.isBlank())"Добавить фотографию" else "Изменить фотографию"){launcher.launch(arrayOf("image/*"))}}}
@Composable fun UriImage(uri:String,size:Int){AndroidView(factory={ctx->ImageView(ctx).apply{scaleType=ImageView.ScaleType.CENTER_CROP}},update={it.setImageURI(Uri.parse(uri))},modifier=Modifier.size(size.dp).clip(RoundedCornerShape(18.dp)))}

@Composable fun Basics(p:Profile,next:()->Unit)=Page("Расскажи о себе"){PhotoPicker(p.photoUri){p.photoUri=it};Field("Имя",p.name){p.name=it};Field("Город",p.city){p.city=it};Num("Возраст",p.age){if(it.length<=2)p.age=it};Num("Рост, см",p.height){if(it.length<=3)p.height=it};Num("Вес, кг",p.weight,true){if(it.length<=6)p.weight=it};val ok=(p.age.toIntOrNull()?:0) in 14..99&&(p.height.toIntOrNull()?:0) in 120..230&&(p.weight.toDoubleOrNull()?:0.0) in 30.0..300.0;Primary(enabled=p.name.isNotBlank()&&p.city.isNotBlank()&&ok,click=next)}
@Composable fun Details(p:Profile,next:()->Unit)=Page("Ещё немного о тебе"){Field("Род деятельности",p.occupation){p.occupation=it};Field("О себе",p.about,4){p.about=it};Primary(enabled=p.occupation.isNotBlank(),click=next)}
@Composable fun Choice(title:String,items:List<String>,initial:String,set:(String)->Unit,next:()->Unit){var v by remember{mutableStateOf(initial)};Page(title){items.forEach{Select(it,v==it){v=it;set(it)}};Primary(enabled=v.isNotBlank(),click=next)}}
@Composable fun PlaceScreen(p:Profile,next:()->Unit){var v by remember{mutableStateOf(p.place)};Page("Где тренируемся?","Выбери основное место тренировок."){Select("Зал",v=="Зал"){v="Зал";p.place="Зал";p.equipment=""};Select("Дом",v=="Дом"){v="Дом";p.place="Дом"};Primary(enabled=v.isNotBlank(),click=next)}}
@Composable fun EquipmentScreen(p:Profile,next:()->Unit){val opts=listOf("Гантели","Штанга и блины","Гири","Резинки","Турник","Скамья","Коврик","Без оборудования");val selected=remember{mutableStateListOf<String>().apply{addAll(p.equipment.split('|').filter{it.isNotBlank()})}};Page("Что есть дома?","Можно выбрать несколько вариантов."){opts.forEach{o->Select(o,o in selected){if(o=="Без оборудования"){selected.clear();selected.add(o)}else{selected.remove("Без оборудования");if(o in selected)selected.remove(o)else selected.add(o)};p.equipment=selected.joinToString("|")}};Primary(enabled=selected.isNotEmpty(),click=next)}}
@Composable fun HealthScreen(p:Profile,next:()->Unit){var none by remember{mutableStateOf(p.health=="Нет противопоказаний")};Page("Здоровье","Укажи ограничения, которые важно учитывать."){Select("Нет противопоказаний",none){none=true;p.health="Нет противопоказаний"};Field("Ограничения и противопоказания",if(none)"" else p.health,4){none=false;p.health=it};Primary("Завершить анкету",p.health.isNotBlank(),next)}}
@Composable fun Home(p:Profile,profile:()->Unit)=Page(if(p.name.isBlank())"Сегодня" else "Привет, ${p.name}","${p.goal} · ${p.days} тренировки в неделю"){Card{Text("Тренировка всего тела",fontSize=22.sp,fontWeight=FontWeight.Bold);Text("45 мин · 6 упражнений",color=UiSub);Primary("Начать тренировку"){} };Secondary("Мой профиль и замеры",profile)}

@Composable fun ProfilePage(p:Profile,mode:ThemeMode,setMode:(ThemeMode)->Unit,items:List<Measurement>,add:(Measurement)->Unit,back:()->Unit){var tab by remember{mutableIntStateOf(0)};Page("Профиль",p.name){Segment(listOf("Профиль","Замеры"),if(tab==0)"Профиль" else "Замеры"){tab=if(it=="Профиль")0 else 1};if(tab==0)ProfileFields(p,mode,setMode) else Measurements(items,add);Secondary("Готово",back)}}
@Composable fun ProfileFields(p:Profile,mode:ThemeMode,setMode:(ThemeMode)->Unit){PhotoPicker(p.photoUri){p.photoUri=it};Card{Text(if(p.name.isBlank())"Профиль" else p.name,fontSize=24.sp,fontWeight=FontWeight.Bold);Text(p.city,color=UiSub);Text(listOf(p.age.takeIf{it.isNotBlank()}?.let{"$it лет"},p.height.takeIf{it.isNotBlank()}?.let{"$it см"},p.weight.takeIf{it.isNotBlank()}?.let{"$it кг"}).filterNotNull().joinToString(" · "),color=UiSub)};Card{Text("О себе",fontWeight=FontWeight.Bold);Field("Род деятельности",p.occupation){p.occupation=it};Field("О себе",p.about,3){p.about=it}};Card{Text("Тренировки",fontWeight=FontWeight.Bold);Field("Цель",p.goal){p.goal=it};Field("Стаж тренировок",p.experience){p.experience=it};Field("Место",p.place){p.place=it};if(p.place=="Дом")Field("Оборудование",p.equipment){p.equipment=it}};Card{Text("Здоровье",fontWeight=FontWeight.Bold);Field("Противопоказания",p.health){p.health=it}};Card{Text("Основные данные",fontWeight=FontWeight.Bold);Field("Имя",p.name){p.name=it};Field("Город",p.city){p.city=it};Num("Возраст",p.age){if(it.length<=2)p.age=it};Num("Рост, см",p.height){if(it.length<=3)p.height=it};Num("Вес, кг",p.weight,true){if(it.length<=6)p.weight=it}};Card{Text("Оформление",fontWeight=FontWeight.Bold);Segment(listOf("Светлая","Система","Тёмная"),when(mode){ThemeMode.LIGHT->"Светлая";ThemeMode.SYSTEM->"Система";ThemeMode.DARK->"Тёмная"}){setMode(when(it){"Светлая"->ThemeMode.LIGHT;"Тёмная"->ThemeMode.DARK;else->ThemeMode.SYSTEM})}}}

@Composable fun MeasurementPhoto(label:String,uri:String,set:(String)->Unit){val c=LocalContext.current;val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){u:Uri?->if(u!=null){runCatching{c.contentResolver.takePersistableUriPermission(u,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)};set(u.toString())}};Card{Text(label,fontWeight=FontWeight.Bold);if(uri.isNotBlank())UriImage(uri,140);Secondary(if(uri.isBlank())"Добавить фото" else "Заменить фото"){launcher.launch(arrayOf("image/*"))}}}
@Composable fun Measurements(items:List<Measurement>,add:(Measurement)->Unit){var adding by remember{mutableStateOf(false)};var opened by remember{mutableStateOf<Measurement?>(null)};val labels=listOf("Вес","Шея","Плечи","Грудь","Талия","Живот","Бёдра","Бицепс левый","Бицепс правый","Предплечье левое","Предплечье правое","Бедро левое","Бедро правое","Икра левая","Икра правая");val values=remember{mutableStateMapOf<String,String>()};val photos=remember{mutableStateMapOf<String,String>()};var status by remember{mutableStateOf("")}
 when{opened!=null->{Text(prettyDate(opened!!.date),fontSize=24.sp,fontWeight=FontWeight.Bold);opened!!.values.filterValues{it.isNotBlank()}.forEach{(k,v)->Card{Text(k,color=UiSub);Text(v,fontSize=20.sp,fontWeight=FontWeight.Bold)}};if(opened!!.photos.isNotEmpty()){Text("Фотографии прогресса",fontSize=20.sp,fontWeight=FontWeight.Bold);opened!!.photos.forEach{(k,v)->Card{Text(k,fontWeight=FontWeight.Bold);UriImage(v,180)}}};Secondary("Назад"){opened=null}}
 adding->{Text("Новый замер",fontSize=24.sp,fontWeight=FontWeight.Bold);Text("Дата будет определена автоматически при сохранении.",color=UiSub);labels.forEach{l->Num(if(l=="Вес")"$l, кг" else "$l, см",values[l]?:"",true){values[l]=it}};Text("Фотографии",fontSize=20.sp,fontWeight=FontWeight.Bold);MeasurementPhoto("Спереди",photos["Спереди"]?:""){photos["Спереди"]=it};MeasurementPhoto("Сбоку",photos["Сбоку"]?:""){photos["Сбоку"]=it};MeasurementPhoto("Сзади",photos["Сзади"]?:""){photos["Сзади"]=it};if(status.isNotBlank())Text(status,color=UiSub);Primary("Сохранить замер",values.values.any{it.isNotBlank()}||photos.isNotEmpty()){status="Получаю дату из интернета…";networkDate{date->if(date==null)status="Не удалось получить дату" else{add(Measurement(date,values.toMap(),photos.toMap()));values.clear();photos.clear();status="";adding=false}}};Secondary("Отмена"){adding=false}}
 else->{Primary("＋ Добавить замер"){adding=true};if(items.isEmpty())Text("Пока нет сохранённых замеров.",color=UiSub)else items.asReversed().forEach{m->Card{Row(Modifier.fillMaxWidth().clickable{opened=m},verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(prettyDate(m.date),fontWeight=FontWeight.Bold);Text("Замеры: ${m.values.count{it.value.isNotBlank()}} · Фото: ${m.photos.size}",color=UiSub)};Text("›",fontSize=28.sp)}}}}
 }
}

fun networkDate(done:(String?)->Unit){Thread{val r=runCatching{val c=URL("https://www.google.com/generate_204").openConnection() as HttpURLConnection;c.requestMethod="HEAD";c.connectTimeout=5000;c.readTimeout=5000;c.connect();val ms=c.date;c.disconnect();if(ms<=0)error("date");Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate().toString()}.getOrNull();Handler(Looper.getMainLooper()).post{done(r)}}.start()}
fun prettyDate(s:String)=runCatching{java.time.LocalDate.parse(s).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}.getOrDefault(s)

@Composable fun Page(title:String,sub:String="",body:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){Spacer(Modifier.height(12.dp));Text("FROST FITNESS",fontSize=12.sp,fontWeight=FontWeight.Bold,color=UiSub);Text(title,fontSize=34.sp,lineHeight=42.sp,fontWeight=FontWeight.Bold);if(sub.isNotBlank())Text(sub,color=UiSub);body();Spacer(Modifier.height(40.dp))}}
@Composable fun Card(body:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxWidth().background(UiCard,RoundedCornerShape(20.dp)).padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=body)}
@Composable fun Select(text:String,on:Boolean,click:()->Unit){Button(click,Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=UiCard,contentColor=UiInk)){Text(text,Modifier.weight(1f));if(on)Text("✓",color=Accent,fontWeight=FontWeight.Bold)}}
@Composable fun Primary(text:String="Продолжить",enabled:Boolean=true,click:()->Unit){Button(click,Modifier.fillMaxWidth().height(54.dp),enabled=enabled,shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Accent,contentColor=Color.Black)){Text(text,fontWeight=FontWeight.Bold)}}
@Composable fun Secondary(text:String,click:()->Unit){OutlinedButton(click,Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)){Text(text,color=UiInk)}}
@Composable fun Field(label:String,value:String,lines:Int=1,set:(String)->Unit){OutlinedTextField(value,set,label={Text(label)},singleLine=lines==1,minLines=lines,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))}
@Composable fun Num(label:String,value:String,decimal:Boolean=false,set:(String)->Unit){OutlinedTextField(value,{raw->val x=raw.replace(',','.');set(if(decimal)x.filterIndexed{i,ch->ch.isDigit()||(ch=='.'&&i>0&&x.take(i).none{it=='.'})}else x.filter{it.isDigit()})},label={Text(label)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=if(decimal)KeyboardType.Decimal else KeyboardType.Number),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))}
@Composable fun Segment(items:List<String>,selected:String,set:(String)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)){items.forEach{item->Button({set(item)},Modifier.weight(1f),colors=ButtonDefaults.buttonColors(containerColor=if(item==selected)UiCard else Color.Transparent,contentColor=UiInk)){Text(item,fontSize=12.sp)}}}}

fun prefs(c:Context)=c.getSharedPreferences("frost_fitness",Context.MODE_PRIVATE)
fun loadProfile(c:Context)=Profile().apply{val x=prefs(c);name=x.getString("name","")!!;city=x.getString("city","")!!;about=x.getString("about","")!!;occupation=x.getString("occupation","")!!;goal=x.getString("goal","")!!;age=x.getString("age","")!!;height=x.getString("height","")!!;weight=x.getString("weight","")!!;experience=x.getString("experience","")!!;days=x.getInt("days",3);place=x.getString("place","Зал")!!.let{if(it.startsWith("Дом"))"Дом" else it};equipment=x.getString("equipment","")!!;health=x.getString("health","")!!;photoUri=x.getString("photoUri","")!!}
fun loadScreen(c:Context)=runCatching{Screen.valueOf(prefs(c).getString("screen","WELCOME")!!)}.getOrDefault(Screen.WELCOME)
fun loadTheme(c:Context)=runCatching{ThemeMode.valueOf(prefs(c).getString("theme","SYSTEM")!!)}.getOrDefault(ThemeMode.SYSTEM)
fun esc(s:String)=android.util.Base64.encodeToString(s.toByteArray(),android.util.Base64.NO_WRAP)
fun unesc(s:String)=runCatching{String(android.util.Base64.decode(s,android.util.Base64.NO_WRAP))}.getOrDefault("")
fun save(c:Context,p:Profile,s:Screen,t:ThemeMode,m:List<Measurement>){val encoded=m.joinToString(";"){mm->esc(mm.date)+","+esc(mm.values.entries.joinToString("|"){e->e.key+"="+e.value})+","+esc(mm.photos.entries.joinToString("|"){e->e.key+"="+e.value})};prefs(c).edit().putString("name",p.name).putString("city",p.city).putString("about",p.about).putString("occupation",p.occupation).putString("goal",p.goal).putString("age",p.age).putString("height",p.height).putString("weight",p.weight).putString("experience",p.experience).putInt("days",p.days).putString("place",p.place).putString("equipment",p.equipment).putString("health",p.health).putString("photoUri",p.photoUri).putString("screen",s.name).putString("theme",t.name).putString("measurements",encoded).apply()}
fun decodeMap(s:String)=unesc(s).split('|').mapNotNull{e->val z=e.split('=',limit=2);if(z.size==2)z[0] to z[1] else null}.toMap()
fun loadMeasurements(c:Context):List<Measurement>{val raw=prefs(c).getString("measurements","")?:"";if(raw.isBlank())return emptyList();return raw.split(';').mapNotNull{r->val q=r.split(',',limit=3);if(q.size<2)null else Measurement(unesc(q[0]),decodeMap(q[1]),if(q.size>=3)decodeMap(q[2]) else emptyMap())}}
