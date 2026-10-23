package com.hasantheabu.bibahobondhu

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

private const val PREF = "bibahobondhu_v1"
private const val KEY = "members"
private const val SESSION = "session"
private const val FAV = "favorites"

data class Member(
    val phone: String, val hash: String, val name: String, val gender: String,
    val age: Int, val district: String, val education: String, val job: String,
    val about: String, val visible: Boolean = true
)

private fun passwordHash(s: String): String =
    MessageDigest.getInstance("SHA-256").digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

private fun Member.encode() = JSONObject().apply {
    put("phone", phone); put("hash", hash); put("name", name); put("gender", gender)
    put("age", age); put("district", district); put("education", education)
    put("job", job); put("about", about); put("visible", visible)
}
private fun decode(o: JSONObject) = Member(
    o.optString("phone"), o.optString("hash"), o.optString("name"), o.optString("gender"),
    o.optInt("age", 18), o.optString("district"), o.optString("education"),
    o.optString("job"), o.optString("about"), o.optBoolean("visible", true)
)
private fun readMembers(c: Context): List<Member> = try {
    val a = JSONArray(c.getSharedPreferences(PREF, 0).getString(KEY, "[]"))
    (0 until a.length()).map { decode(a.getJSONObject(it)) }
} catch (_: Exception) { emptyList() }
private fun writeMembers(c: Context, ms: List<Member>) {
    val a = JSONArray(); ms.forEach { a.put(it.encode()) }
    c.getSharedPreferences(PREF, 0).edit().putString(KEY, a.toString()).apply()
}
private fun readFavorites(c: Context) =
    c.getSharedPreferences(PREF, 0).getStringSet(FAV, emptySet())?.toSet() ?: emptySet()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App(this) }
    }
}

@Composable
private fun App(ctx: Context) {
    val prefs = remember { ctx.getSharedPreferences(PREF, 0) }
    var members by remember { mutableStateOf(readMembers(ctx)) }
    var session by remember { mutableStateOf(prefs.getString(SESSION, null)) }
    var page by remember { mutableStateOf("find") }
    var mode by remember { mutableStateOf("register") }
    var note by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Member?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("সব") }
    var favorites by remember { mutableStateOf(readFavorites(ctx)) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("পাত্র") }
    var age by remember { mutableStateOf("২৫") }
    var district by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    val me = members.firstOrNull { it.phone == session }

    fun save(list: List<Member>) { members = list; writeMembers(ctx, list) }
    fun logout() { session = null; prefs.edit().remove(SESSION).apply(); page = "find"; mode = "login" }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF913B5B),
        secondary = androidx.compose.ui.graphics.Color(0xFFB76E79),
        background = androidx.compose.ui.graphics.Color(0xFFFFF8F7)
    )) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("বিবাহবন্ধু", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("বিশ্বাস • সম্মান • সুন্দর সম্পর্ক")
                }
                if (me == null) {
                    Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(mode == "register", { mode = "register"; note = "" }, label = { Text("নিবন্ধন") })
                        FilterChip(mode == "login", { mode = "login"; note = "" }, label = { Text("লগইন") })
                    }
                    if (mode == "register") {
                        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("নতুন সদস্য হোন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("পূর্ণ নাম *") }, singleLine = true)
                            OutlinedTextField(phone, { phone = it.trim() }, Modifier.fillMaxWidth(), label = { Text("মোবাইল নম্বর *") }, singleLine = true)
                            OutlinedTextField(pass, { pass = it }, Modifier.fillMaxWidth(), label = { Text("পাসওয়ার্ড (কমপক্ষে ৮ অক্ষর) *") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                            OutlinedTextField(age, { age = it }, Modifier.fillMaxWidth(), label = { Text("বয়স *") }, singleLine = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(gender == "পাত্র", { gender = "পাত্র" }, label = { Text("পাত্র") })
                                FilterChip(gender == "পাত্রী", { gender = "পাত্রী" }, label = { Text("পাত্রী") })
                            }
                            OutlinedTextField(district, { district = it }, Modifier.fillMaxWidth(), label = { Text("জেলা *") }, singleLine = true)
                            OutlinedTextField(education, { education = it }, Modifier.fillMaxWidth(), label = { Text("শিক্ষাগত যোগ্যতা") }, singleLine = true)
                            OutlinedTextField(job, { job = it }, Modifier.fillMaxWidth(), label = { Text("পেশা") }, singleLine = true)
                            OutlinedTextField(about, { about = it }, Modifier.fillMaxWidth(), label = { Text("নিজের সম্পর্কে") }, minLines = 3)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(consent, { consent = it })
                                Text("আমি ১৮+ এবং বুঝেছি তথ্য এই ফোনে থাকবে।")
                            }
                            Button(onClick = {
                                val n = age.toIntOrNull()
                                when {
                                    name.isBlank() || phone.isBlank() || district.isBlank() || pass.length < 8 -> note = "নাম, মোবাইল, জেলা ও অন্তত ৮ অক্ষরের পাসওয়ার্ড দিন।"
                                    !phone.matches(Regex("^[+]?[0-9]{8,15}$")) -> note = "সঠিক মোবাইল নম্বর দিন।"
                                    n == null || n < 18 || n > 100 -> note = "বয়স ১৮ থেকে ১০০-এর মধ্যে হতে হবে।"
                                    members.any { it.phone == phone } -> note = "এই নম্বরে অ্যাকাউন্ট আছে। লগইন করুন।"
                                    !consent -> note = "নিবন্ধনের আগে সম্মতি দিন।"
                                    else -> {
                                        val m = Member(phone, passwordHash(pass), name.trim(), gender, n, district.trim(), education.trim(), job.trim(), about.trim())
                                        save(members + m); session = phone; prefs.edit().putString(SESSION, phone).apply()
                                        page = "find"; note = "এই ডিভাইসে নিবন্ধন সম্পন্ন হয়েছে।"
                                    }
                                }
                            }, Modifier.fillMaxWidth()) { Text("নিবন্ধন করুন") }
                            if (note.isNotBlank()) Text(note, color = MaterialTheme.colorScheme.primary)
                            Text("এটি লোকাল সংস্করণ—অনলাইন সার্ভার বা OTP নেই।", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(phone, { phone = it.trim() }, Modifier.fillMaxWidth(), label = { Text("মোবাইল নম্বর") }, singleLine = true)
                            OutlinedTextField(pass, { pass = it }, Modifier.fillMaxWidth(), label = { Text("পাসওয়ার্ড") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                            Button(onClick = {
                                val found = members.firstOrNull { it.phone == phone && it.hash == passwordHash(pass) }
                                if (found == null) note = "নম্বর বা পাসওয়ার্ড মেলেনি।"
                                else { session = found.phone; prefs.edit().putString(SESSION, found.phone).apply(); note = "লগইন সফল।" }
                            }, Modifier.fillMaxWidth()) { Text("লগইন") }
                            if (note.isNotBlank()) Text(note, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf("find" to "খুঁজুন", "edit" to "বায়োডাটা", "saved" to "পছন্দ", "account" to "অ্যাকাউন্ট").forEach { (key, label) ->
                            FilterChip(page == key, { page = key; note = "" }, label = { Text(label) })
                        }
                    }
                    when (page) {
                        "find" -> {
                            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(12.dp), label = { Text("নাম, জেলা, শিক্ষা বা পেশা") }, singleLine = true)
                            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("সব", "পাত্র", "পাত্রী").forEach { g -> FilterChip(filter == g, { filter = g }, label = { Text(g) }) }
                            }
                            val found = members.filter { m -> m.phone != me.phone && m.visible && (filter == "সব" || m.gender == filter) && (query.isBlank() || listOf(m.name, m.district, m.education, m.job).any { it.contains(query, true) }) }
                            if (found.isEmpty()) {
                                Column(Modifier.weight(1f).padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("এখনো অন্য কোনো স্থানীয় সদস্য নেই", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("এই সংস্করণে প্রোফাইল শুধু একই ফোনের নিবন্ধিত সদস্যদের মধ্যে দেখা যায়। অন্য ফোনের সদস্যদের জন্য অনলাইন সার্ভার লাগবে।")
                                }
                            } else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(found, key = { it.phone }) { m ->
                                    Card(Modifier.fillMaxWidth().clickable { selected = m }) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(m.name + " • " + m.gender, fontWeight = FontWeight.Bold)
                                            Text(m.age.toString() + " বছর • " + m.district)
                                            Text(listOf(m.education, m.job).filter { it.isNotBlank() }.joinToString(" • "))
                                            Text("বিস্তারিত দেখতে চাপ দিন", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                        "edit" -> {
                            var eName by remember(me.phone) { mutableStateOf(me.name) }
                            var eGender by remember(me.phone) { mutableStateOf(me.gender) }
                            var eAge by remember(me.phone) { mutableStateOf(me.age.toString()) }
                            var eDistrict by remember(me.phone) { mutableStateOf(me.district) }
                            var eEdu by remember(me.phone) { mutableStateOf(me.education) }
                            var eJob by remember(me.phone) { mutableStateOf(me.job) }
                            var eAbout by remember(me.phone) { mutableStateOf(me.about) }
                            var visible by remember(me.phone) { mutableStateOf(me.visible) }
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("আমার বায়োডাটা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("মোবাইল নম্বর ব্যক্তিগত রাখা হয়: " + me.phone)
                                OutlinedTextField(eName, { eName = it }, Modifier.fillMaxWidth(), label = { Text("নাম") })
                                OutlinedTextField(eAge, { eAge = it }, Modifier.fillMaxWidth(), label = { Text("বয়স") })
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(eGender == "পাত্র", { eGender = "পাত্র" }, label = { Text("পাত্র") })
                                    FilterChip(eGender == "পাত্রী", { eGender = "পাত্রী" }, label = { Text("পাত্রী") })
                                }
                                OutlinedTextField(eDistrict, { eDistrict = it }, Modifier.fillMaxWidth(), label = { Text("জেলা") })
                                OutlinedTextField(eEdu, { eEdu = it }, Modifier.fillMaxWidth(), label = { Text("শিক্ষা") })
                                OutlinedTextField(eJob, { eJob = it }, Modifier.fillMaxWidth(), label = { Text("পেশা") })
                                OutlinedTextField(eAbout, { eAbout = it }, Modifier.fillMaxWidth(), label = { Text("নিজের সম্পর্কে") }, minLines = 3)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(visible, { visible = it }); Spacer(Modifier.width(8.dp))
                                    Text(if (visible) "প্রোফাইল দৃশ্যমান" else "প্রোফাইল লুকানো")
                                }
                                Button(onClick = {
                                    val n = eAge.toIntOrNull()
                                    if (eName.isBlank() || eDistrict.isBlank() || n == null || n < 18 || n > 100) note = "নাম, জেলা ও সঠিক বয়স দিন।"
                                    else {
                                        val u = me.copy(name = eName.trim(), gender = eGender, age = n, district = eDistrict.trim(), education = eEdu.trim(), job = eJob.trim(), about = eAbout.trim(), visible = visible)
                                        save(members.map { if (it.phone == me.phone) u else it }); note = "বায়োডাটা সংরক্ষণ হয়েছে।"
                                    }
                                }, Modifier.fillMaxWidth()) { Text("পরিবর্তন সংরক্ষণ") }
                                if (note.isNotBlank()) Text(note, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        "saved" -> {
                            val saved = members.filter { it.phone in favorites && it.phone != me.phone && it.visible }
                            if (saved.isEmpty()) Text("এখনো পছন্দের প্রোফাইল নেই।", Modifier.padding(18.dp))
                            else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp)) {
                                items(saved, key = { it.phone }) { m ->
                                    ListItem(headlineContent = { Text(m.name) }, supportingContent = { Text(m.age.toString() + " বছর • " + m.district) }, modifier = Modifier.clickable { selected = m })
                                }
                            }
                        }
                        else -> {
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("আমার অ্যাকাউন্ট", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(me.name, style = MaterialTheme.typography.titleMedium)
                                Text("মোবাইল: " + me.phone)
                                Text("তথ্য এই ফোনে থাকে। অ্যাপের ডেটা মুছে গেলে তথ্য হারাতে পারে।")
                                OutlinedButton(onClick = { logout() }, Modifier.fillMaxWidth()) { Text("লগআউট") }
                                OutlinedButton(onClick = {
                                    save(members.filterNot { it.phone == me.phone })
                                    favorites = favorites - me.phone
                                    prefs.edit().putStringSet(FAV, favorites).remove(SESSION).apply()
                                    session = null; page = "find"; mode = "register"; note = "এই ডিভাইস থেকে অ্যাকাউন্ট মুছে ফেলা হয়েছে।"
                                }, Modifier.fillMaxWidth()) { Text("অ্যাকাউন্ট ও বায়োডাটা মুছুন") }
                                Text("বাস্তব অনলাইন সেবার জন্য নিরাপদ সার্ভার, OTP, সার্ভার-সাইড অনুমতি, রিপোর্ট/ব্লক মডারেশন ও গোপনীয়তা নীতি লাগবে।", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (note.isNotBlank() && page != "edit" && page != "account") Text(note, Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
            if (selected != null && me != null) {
                val m = selected!!
                AlertDialog(
                    onDismissRequest = { selected = null },
                    title = { Text(m.name, fontWeight = FontWeight.Bold) },
                    text = { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(m.gender + " • " + m.age + " বছর")
                        Text("জেলা: " + m.district)
                        if (m.education.isNotBlank()) Text("শিক্ষা: " + m.education)
                        if (m.job.isNotBlank()) Text("পেশা: " + m.job)
                        if (m.about.isNotBlank()) Text("পরিচিতি: " + m.about)
                        Text("মোবাইল নম্বর গোপন রাখা হয়েছে।")
                    } },
                    confirmButton = {
                        TextButton(onClick = {
                            favorites = if (m.phone in favorites) favorites - m.phone else favorites + m.phone
                            prefs.edit().putStringSet(FAV, favorites).apply(); selected = null
                        }) { Text(if (m.phone in favorites) "পছন্দ বাদ দিন" else "পছন্দ সংরক্ষণ") }
                    },
                    dismissButton = { TextButton(onClick = { selected = null }) { Text("বন্ধ করুন") } }
                )
            }
        }
    }
}
