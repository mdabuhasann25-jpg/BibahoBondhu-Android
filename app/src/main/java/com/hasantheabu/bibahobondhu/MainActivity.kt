package com.hasantheabu.bibahobondhu

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

private data class Profile(
    val uid: String = "",
    val name: String = "",
    val gender: String = "পাত্র",
    val age: Int = 18,
    val district: String = "",
    val education: String = "",
    val job: String = "",
    val about: String = "",
    val visible: Boolean = true,
    val online: Boolean = false
) {
    fun toMap() = hashMapOf<String, Any>(
        "uid" to uid, "name" to name, "gender" to gender, "age" to age,
        "district" to district, "education" to education, "job" to job,
        "about" to about, "visible" to visible, "online" to online,
        "lastSeen" to FieldValue.serverTimestamp()
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (FirebaseApp.getApps(this).isEmpty() &&
                BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
                BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
                BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
            ) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(BuildConfig.FIREBASE_API_KEY)
                    .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                    .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (_: Exception) { }
        setContent { BibahoBondhuOnline() }
    }
}

@Composable
private fun BibahoBondhuOnline() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val configured = remember(context) {
        FirebaseApp.getApps(context).isNotEmpty()
    }
    val auth = remember(configured) { if (configured) FirebaseAuth.getInstance() else null }
    val db = remember(configured) { if (configured) FirebaseFirestore.getInstance() else null }
    var user by remember { mutableStateOf(auth?.currentUser) }
    var profiles by remember { mutableStateOf<List<Profile>>(emptyList()) }
    var myProfile by remember { mutableStateOf<Profile?>(null) }
    var page by remember { mutableStateOf("find") }
    var mode by remember { mutableStateOf("register") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Profile?>(null) }
    var query by remember { mutableStateOf("") }
    var genderFilter by remember { mutableStateOf("সব") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("পাত্র") }
    var age by remember { mutableStateOf("২৫") }
    var district by remember { mutableStateOf("") }
    var education by remember { mutableStateOf("") }
    var job by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf<Set<String>>(emptySet()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(auth) {
        if (auth == null) onDispose { }
        else {
            val listener = FirebaseAuth.AuthStateListener { a -> user = a.currentUser }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }
    }

    DisposableEffect(db, user?.uid, lifecycleOwner) {
        val uid = user?.uid
        if (db == null || uid == null) {
            profiles = emptyList()
            myProfile = null
            onDispose { }
        } else {
            val publicRegistration = db.collection("profiles").whereEqualTo("visible", true).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "প্রোফাইল লোড হয়নি। Firebase security rules ও internet পরীক্ষা করুন।"
                } else if (snapshot != null) {
                    profiles = snapshot.documents.mapNotNull { doc ->
                        try {
                            Profile(
                                uid = doc.id,
                                name = doc.getString("name") ?: "",
                                gender = doc.getString("gender") ?: "পাত্র",
                                age = (doc.getLong("age") ?: 18L).toInt(),
                                district = doc.getString("district") ?: "",
                                education = doc.getString("education") ?: "",
                                job = doc.getString("job") ?: "",
                                about = doc.getString("about") ?: "",
                                visible = doc.getBoolean("visible") ?: true,
                                online = doc.getBoolean("online") ?: false
                            )
                        } catch (_: Exception) { null }
                    }
                }
            }
            val ownRegistration = db.collection("profiles").document(uid).addSnapshotListener { doc, error ->
                if (error == null && doc != null && doc.exists()) {
                    myProfile = try {
                        Profile(
                            uid = doc.id,
                            name = doc.getString("name") ?: "",
                            gender = doc.getString("gender") ?: "পাত্র",
                            age = (doc.getLong("age") ?: 18L).toInt(),
                            district = doc.getString("district") ?: "",
                            education = doc.getString("education") ?: "",
                            job = doc.getString("job") ?: "",
                            about = doc.getString("about") ?: "",
                            visible = doc.getBoolean("visible") ?: true,
                            online = doc.getBoolean("online") ?: false
                        )
                    } catch (_: Exception) { null }
                } else if (doc != null && !doc.exists()) myProfile = null
            }
            val observer = LifecycleEventObserver { _, event ->
                val ref = db.collection("profiles").document(uid)
                if (event == Lifecycle.Event.ON_START) {
                    ref.update(mapOf("online" to true, "lastSeen" to FieldValue.serverTimestamp()))
                } else if (event == Lifecycle.Event.ON_STOP) {
                    ref.update(mapOf("online" to false, "lastSeen" to FieldValue.serverTimestamp()))
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                publicRegistration.remove()
                ownRegistration.remove()
                lifecycleOwner.lifecycle.removeObserver(observer)
                db.collection("profiles").document(uid).update(mapOf("online" to false, "lastSeen" to FieldValue.serverTimestamp()))
            }
        }
    }

    val me = myProfile ?: profiles.firstOrNull { it.uid == user?.uid }

    fun writeProfile(profile: Profile, success: String) {
        if (db == null || user == null) return
        busy = true
        db.collection("profiles").document(user!!.uid).set(profile.toMap())
            .addOnSuccessListener { message = success; busy = false }
            .addOnFailureListener { e -> message = "সংরক্ষণ হয়নি: " + (e.localizedMessage ?: "অনুমতি বা নেটওয়ার্ক পরীক্ষা করুন"); busy = false }
    }

    MaterialTheme(colorScheme = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF913B5B),
        secondary = androidx.compose.ui.graphics.Color(0xFFB76E79),
        background = androidx.compose.ui.graphics.Color(0xFFFFF8F7)
    )) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Text("বিবাহবন্ধু", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("অনলাইন বায়োডাটা ও জীবনসঙ্গী খোঁজার প্ল্যাটফর্ম")
                }

                if (!configured || auth == null || db == null) {
                    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Firebase সংযোগ এখনো সেটআপ হয়নি", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("অনলাইন নিবন্ধন ও লাইভ প্রোফাইল চালু করতে Firebase API key, App ID ও Project ID GitHub Actions secrets-এ যোগ করতে হবে। Firebase Authentication ও Cloud Firestore enable না থাকলে অ্যাপ অনলাইনে কাজ করবে না.", Modifier.padding(top = 12.dp))
                        Text("সেটআপ নির্দেশিকা Repository-তে FIREBASE_SETUP_BN.md নামে আছে।", Modifier.padding(top = 10.dp))
                    }
                } else if (user == null) {
                    Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(mode == "register", { mode = "register"; message = "" }, label = { Text("নিবন্ধন") })
                        FilterChip(mode == "login", { mode = "login"; message = "" }, label = { Text("লগইন") })
                    }
                    Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(if (mode == "register") "নতুন সদস্য হোন" else "অ্যাকাউন্টে লগইন করুন", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedTextField(email, { email = it.trim() }, Modifier.fillMaxWidth(), label = { Text("ইমেইল ঠিকানা *") }, singleLine = true)
                        OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label = { Text("পাসওয়ার্ড (কমপক্ষে ৮ অক্ষর) *") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                        if (mode == "register") {
                            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("পূর্ণ নাম *") }, singleLine = true)
                            OutlinedTextField(age, { age = it }, Modifier.fillMaxWidth(), label = { Text("বয়স *") }, singleLine = true)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(gender == "পাত্র", { gender = "পাত্র" }, label = { Text("পাত্র") })
                                FilterChip(gender == "পাত্রী", { gender = "পাত্রী" }, label = { Text("পাত্রী") })
                            }
                            OutlinedTextField(district, { district = it }, Modifier.fillMaxWidth(), label = { Text("জেলা *") })
                            OutlinedTextField(education, { education = it }, Modifier.fillMaxWidth(), label = { Text("শিক্ষাগত যোগ্যতা") })
                            OutlinedTextField(job, { job = it }, Modifier.fillMaxWidth(), label = { Text("পেশা") })
                            OutlinedTextField(about, { about = it }, Modifier.fillMaxWidth(), label = { Text("নিজের সম্পর্কে") }, minLines = 3)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(consent, { consent = it })
                                Text("আমার বয়স ১৮+ এবং প্রোফাইল তথ্য দেখানোর বিষয়ে সম্মত।")
                            }
                        }
                        Button(enabled = !busy, onClick = {
                            if (email.isBlank() || password.length < 8) {
                                message = "সঠিক ইমেইল ও অন্তত ৮ অক্ষরের পাসওয়ার্ড দিন।"
                            } else if (mode == "register") {
                                val n = age.toIntOrNull()
                                when {
                                    name.isBlank() || district.isBlank() -> message = "নাম ও জেলা দিন।"
                                    n == null || n < 18 || n > 100 -> message = "বয়স ১৮ থেকে ১০০-এর মধ্যে হতে হবে।"
                                    !consent -> message = "নিবন্ধনের আগে সম্মতি দিন।"
                                    else -> {
                                        busy = true
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnSuccessListener { result ->
                                                val uid = result.user?.uid
                                                if (uid == null) { busy = false; message = "অ্যাকাউন্ট তৈরি হয়নি।" }
                                                else {
                                                    val p = Profile(uid, name.trim(), gender, n, district.trim(), education.trim(), job.trim(), about.trim(), online = true)
                                                    db.collection("profiles").document(uid).set(p.toMap())
                                                        .addOnSuccessListener { busy = false; page = "find"; message = "অনলাইন অ্যাকাউন্ট তৈরি হয়েছে।" }
                                                        .addOnFailureListener { e -> busy = false; message = "অ্যাকাউন্ট হয়েছে, কিন্তু বায়োডাটা সংরক্ষণ হয়নি: " + (e.localizedMessage ?: "") }
                                                }
                                            }.addOnFailureListener { e -> busy = false; message = e.localizedMessage ?: "নিবন্ধন ব্যর্থ হয়েছে।" }
                                    }
                                }
                            } else {
                                busy = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { busy = false; page = "find"; message = "লগইন সফল।" }
                                    .addOnFailureListener { e -> busy = false; message = e.localizedMessage ?: "লগইন ব্যর্থ হয়েছে।" }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "অপেক্ষা করুন..." else if (mode == "register") "অনলাইনে নিবন্ধন করুন" else "লগইন") }
                        if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
                        Text("ইমেইল/পাসওয়ার্ড Firebase Authentication-এ থাকে; প্রোফাইলে লগইন গোপন তথ্য প্রকাশ করা হয় না.", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 5.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf("find" to "সদস্য খুঁজুন", "edit" to "বায়োডাটা", "saved" to "পছন্দ", "account" to "অ্যাকাউন্ট").forEach { (key, label) ->
                            FilterChip(page == key, { page = key; message = "" }, label = { Text(label) })
                        }
                    }
                    when (page) {
                        "find" -> {
                            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(12.dp), label = { Text("নাম, জেলা, শিক্ষা বা পেশা") }, singleLine = true)
                            Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("সব", "পাত্র", "পাত্রী").forEach { g -> FilterChip(genderFilter == g, { genderFilter = g }, label = { Text(g) }) }
                            }
                            val found = profiles.filter { p ->
                                p.uid != user?.uid && p.visible && (genderFilter == "সব" || p.gender == genderFilter) &&
                                    (query.isBlank() || listOf(p.name, p.district, p.education, p.job).any { it.contains(query, true) })
                            }
                            if (found.isEmpty()) {
                                Column(Modifier.weight(1f).padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("কোনো প্রোফাইল পাওয়া যায়নি", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("অন্যান্য নিবন্ধিত সদস্যরা প্রোফাইল তৈরি করলে তা এখানে লাইভ দেখা যাবে। নিজের প্রোফাইল দৃশ্যমান আছে কি না সেটিও পরীক্ষা করুন।")
                                }
                            } else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(found, key = { it.uid }) { p ->
                                    Card(Modifier.fillMaxWidth().clickable { selected = p }) {
                                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(p.name + " • " + p.gender, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                                Text(if (p.online) "● অনলাইন" else "○ অফলাইন", color = if (p.online) androidx.compose.ui.graphics.Color(0xFF16834A) else androidx.compose.ui.graphics.Color.Gray, style = MaterialTheme.typography.bodySmall)
                                            }
                                            Text(p.age.toString() + " বছর • " + p.district)
                                            Text(listOf(p.education, p.job).filter { it.isNotBlank() }.joinToString(" • "))
                                            Text("বিস্তারিত দেখতে চাপ দিন", color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                        "edit" -> {
                            var eName by remember(me?.uid) { mutableStateOf(me?.name ?: "") }
                            var eGender by remember(me?.uid) { mutableStateOf(me?.gender ?: "পাত্র") }
                            var eAge by remember(me?.uid) { mutableStateOf((me?.age ?: 25).toString()) }
                            var eDistrict by remember(me?.uid) { mutableStateOf(me?.district ?: "") }
                            var eEducation by remember(me?.uid) { mutableStateOf(me?.education ?: "") }
                            var eJob by remember(me?.uid) { mutableStateOf(me?.job ?: "") }
                            var eAbout by remember(me?.uid) { mutableStateOf(me?.about ?: "") }
                            var visible by remember(me?.uid) { mutableStateOf(me?.visible ?: true) }
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("আমার বায়োডাটা", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                OutlinedTextField(eName, { eName = it }, Modifier.fillMaxWidth(), label = { Text("নাম") })
                                OutlinedTextField(eAge, { eAge = it }, Modifier.fillMaxWidth(), label = { Text("বয়স") })
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(eGender == "পাত্র", { eGender = "পাত্র" }, label = { Text("পাত্র") })
                                    FilterChip(eGender == "পাত্রী", { eGender = "পাত্রী" }, label = { Text("পাত্রী") })
                                }
                                OutlinedTextField(eDistrict, { eDistrict = it }, Modifier.fillMaxWidth(), label = { Text("জেলা") })
                                OutlinedTextField(eEducation, { eEducation = it }, Modifier.fillMaxWidth(), label = { Text("শিক্ষা") })
                                OutlinedTextField(eJob, { eJob = it }, Modifier.fillMaxWidth(), label = { Text("পেশা") })
                                OutlinedTextField(eAbout, { eAbout = it }, Modifier.fillMaxWidth(), label = { Text("নিজের সম্পর্কে") }, minLines = 3)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(visible, { visible = it }); Spacer(Modifier.width(8.dp))
                                    Text(if (visible) "প্রোফাইল দৃশ্যমান" else "প্রোফাইল লুকানো")
                                }
                                Button(enabled = !busy, onClick = {
                                    val n = eAge.toIntOrNull()
                                    if (eName.isBlank() || eDistrict.isBlank() || n == null || n < 18 || n > 100) message = "নাম, জেলা ও সঠিক বয়স দিন।"
                                    else writeProfile(Profile(user?.uid ?: return@Button, eName.trim(), eGender, n, eDistrict.trim(), eEducation.trim(), eJob.trim(), eAbout.trim(), visible, me?.online ?: true), "বায়োডাটা অনলাইনে সংরক্ষণ হয়েছে।")
                                }, modifier = Modifier.fillMaxWidth()) { Text("পরিবর্তন সংরক্ষণ") }
                                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        "saved" -> {
                            val saved = profiles.filter { it.uid in favorites && it.visible }
                            if (saved.isEmpty()) Text("এই সেশনে কোনো পছন্দের প্রোফাইল নেই।", Modifier.padding(18.dp))
                            else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(12.dp)) {
                                items(saved, key = { it.uid }) { p ->
                                    ListItem(headlineContent = { Text(p.name) }, supportingContent = { Text(p.age.toString() + " বছর • " + p.district) }, modifier = Modifier.clickable { selected = p })
                                }
                            }
                        }
                        else -> {
                            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("আমার অ্যাকাউন্ট", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(me?.name ?: "প্রোফাইল তৈরি করুন", style = MaterialTheme.typography.titleMedium)
                                Text("ইমেইল: " + (user?.email ?: ""))
                                Text("ইমেইল ঠিকানা অন্য সদস্যদের প্রোফাইলে দেখানো হয় না।")
                                OutlinedButton(onClick = { deletePassword = ""; showDeleteDialog = true }, Modifier.fillMaxWidth()) { Text("স্থায়ীভাবে অ্যাকাউন্ট মুছুন") }
                                OutlinedButton(onClick = { auth?.signOut(); page = "find"; message = "লগআউট হয়েছে।", Modifier.fillMaxWidth()) { Text("লগআউট") }
                                Text("অনলাইন স্ট্যাটাস অ্যাপ সামনে থাকলে আপডেট হয়। নেটওয়ার্ক বিচ্ছিন্ন বা অ্যাপ জোর করে বন্ধ হলে অবস্থা আপডেট হতে দেরি হতে পারে।", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (message.isNotBlank() && page != "edit" && page != "account") Text(message, Modifier.padding(8.dp), color = MaterialTheme.colorScheme.primary)
                }
            }
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { if (!busy) showDeleteDialog = false },
                    title = { Text("অ্যাকাউন্ট স্থায়ীভাবে মুছবেন?") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("আপনার বায়োডাটা ও Firebase লগইন অ্যাকাউন্ট মুছে ফেলা হবে। এই কাজ ফিরিয়ে নেওয়া যাবে না। পরিচয় নিশ্চিত করতে বর্তমান পাসওয়ার্ড দিন।")
                            OutlinedTextField(
                                value = deletePassword,
                                onValueChange = { deletePassword = it },
                                label = { Text("বর্তমান পাসওয়ার্ড") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(enabled = !busy && deletePassword.isNotBlank(), onClick = {
                            val current = auth.currentUser
                            val currentEmail = current?.email
                            if (current == null || currentEmail.isNullOrBlank()) {
                                message = "অ্যাকাউন্ট যাচাই করা যাচ্ছে না। আবার লগইন করুন।"
                            } else {
                                busy = true
                                val credential = EmailAuthProvider.getCredential(currentEmail, deletePassword)
                                current.reauthenticate(credential)
                                    .addOnSuccessListener {
                                        db?.collection("profiles")?.document(current.uid)?.delete()
                                            .addOnSuccessListener {
                                                current.delete()
                                                    .addOnSuccessListener {
                                                        busy = false
                                                        showDeleteDialog = false
                                                        deletePassword = ""
                                                        message = "অ্যাকাউন্ট ও বায়োডাটা স্থায়ীভাবে মুছে ফেলা হয়েছে।"
                                                        page = "find"
                                                    }
                                                    .addOnFailureListener { e ->
                                                        busy = false
                                                        message = "লগইন অ্যাকাউন্ট মুছতে পারেনি। আবার লগইন করে চেষ্টা করুন: " + (e.localizedMessage ?: "")
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                busy = false
                                                message = "বায়োডাটা মুছতে পারেনি: " + (e.localizedMessage ?: "")
                                            }
                                    }
                                    .addOnFailureListener {
                                        busy = false
                                        message = "পাসওয়ার্ড সঠিক নয় বা পুনরায় লগইন প্রয়োজন।"
                                    }
                            }
                        }) { Text(if (busy) "মুছছে..." else "হ্যাঁ, স্থায়ীভাবে মুছুন") }
                    },
                    dismissButton = {
                        TextButton(enabled = !busy, onClick = { showDeleteDialog = false }) { Text("বাতিল") }
                    }
                )
            }
            if (selected != null) {
                val p = selected!!
                AlertDialog(
                    onDismissRequest = { selected = null },
                    title = { Text(p.name, fontWeight = FontWeight.Bold) },
                    text = { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(p.gender + " • " + p.age + " বছর")
                        Text("জেলা: " + p.district)
                        if (p.education.isNotBlank()) Text("শিক্ষা: " + p.education)
                        if (p.job.isNotBlank()) Text("পেশা: " + p.job)
                        if (p.about.isNotBlank()) Text("পরিচিতি: " + p.about)
                        Text(if (p.online) "● এখন অনলাইন" else "○ এখন অফলাইন")
                        Text("ইমেইল ও পাসওয়ার্ডের তথ্য দেখানো হয় না।")
                    } },
                    confirmButton = {
                        TextButton(onClick = {
                            favorites = if (p.uid in favorites) favorites - p.uid else favorites + p.uid
                            selected = null
                        }) { Text(if (p.uid in favorites) "পছন্দ বাদ দিন" else "পছন্দ সংরক্ষণ") }
                    },
                    dismissButton = { TextButton(onClick = { selected = null }) { Text("বন্ধ করুন") } }
                )
            }
        }
    }
}
