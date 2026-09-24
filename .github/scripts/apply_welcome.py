from pathlib import Path

p = Path("app/src/main/java/com/frostfitness/app/MainActivity.kt")
s = p.read_text()
old = '@Composable fun Welcome(next:()->Unit)=Page("Добро пожаловать\\nв Frost Fitness","Твой персональный путь к форме начинается здесь."){Card{Text("Фростик — твой спортивный помощник",fontSize=24.sp,fontWeight=FontWeight.Bold);Text("Помогу настроить тренировки и следить за прогрессом.",color=UiSub)};Primary("Заполнить анкету",click=next)}'
new = '''@Composable fun Welcome(next:()->Unit){
 Box(Modifier.fillMaxSize().background(Color.Black)){
  AndroidView(factory={ctx->ImageView(ctx).apply{scaleType=ImageView.ScaleType.CENTER_CROP;setImageResource(R.drawable.welcome)}},modifier=Modifier.fillMaxSize())
  Box(Modifier.align(Alignment.BottomCenter).padding(start=32.dp,end=32.dp,bottom=126.dp).fillMaxWidth().height(76.dp).clickable{next()})
  Text("Нажимая «Заполнить анкету», вы соглашаетесь на обработку персональных данных в соответствии с Политикой конфиденциальности.",color=Color(0xFF6E6E73),fontSize=11.sp,modifier=Modifier.align(Alignment.BottomCenter).padding(start=34.dp,end=34.dp,bottom=42.dp))
 }
}'''
if old not in s:
    raise SystemExit("Expected old Welcome() not found")
p.write_text(s.replace(old, new, 1))
