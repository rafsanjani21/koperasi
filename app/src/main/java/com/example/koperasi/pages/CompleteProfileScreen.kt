package com.example.koperasi.pages

import android.Manifest
import com.example.koperasi.R
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.koperasi.data.RegisterRepository
import com.example.koperasi.data.remote.ApiClient
import com.example.koperasi.ocr.scanNikFromUri
import com.example.koperasi.ui.PlaceOfBirthDropdown
import com.example.koperasi.utils.createImageUri
import com.example.koperasi.wilayah.*
import com.example.koperasi.wilayah.db.WilayahDb
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.firebase.auth.FirebaseAuth


data class Option(val label: String, val value: String)

data class CompleteProfileForm(
    val ktpImageUri: Uri? = null,
    val profilePhotoUri: Uri? = null,

    val nik: String = "",
    val nama: String = "",
    val email: String = "",   // ⬅️ dari Firebase, bukan input
    val noHp: String = "",
    val npwp: String = "",

    val tempatLahirKabKota: String = "",
    val tglLahir: String = "",
    val jenisKelamin: String = "",

    val provinsi: String = "",
    val kabupaten: String = "",
    val kecamatan: String = "",
    val kelurahan: String = "",

    val rt: String = "",
    val rw: String = "",

    val alamat: String = "",
    val agama: String = "",
    val statusPerkawinan: String = "",
    val pekerjaan: String = "",
    val kewarganegaraan: String = "",

    val bloodType: String = "",
    val lastEducation: String = "",
    val activeAs: String = "",
    val motherName: String = "",

    val registerLocation: String = "ANDROID_APP",
    val registerId: String = ""
)


@Composable
fun CompleteProfileScreen(
    initial: CompleteProfileForm = CompleteProfileForm(),
    idTokenProvider: suspend () -> String,
    onSuccess: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ pakai RegisterRepository yang terpisah filenya
    // bikin repo dengan tokenManager
    val tokenManager = remember { com.example.koperasi.TokenManager(ctx) }
    val registerRepo = remember { RegisterRepository(ApiClient.api, ctx, tokenManager) }


    // ====== 2 VM biar tempat lahir & alamat tidak saling ketimpa ======
    val birthVm: WilayahViewModel = viewModel(
        key = "birthVm",
        factory = WilayahViewModelFactory(
            WilayahRepository(
                api = WilayahRetrofit.createApi(),
                dao = WilayahDb.get(ctx).wilayahDao()
            )
        )
    )
    val wBirth by birthVm.ui.collectAsState()

    val addrVm: WilayahViewModel = viewModel(
        key = "addrVm",
        factory = WilayahViewModelFactory(
            WilayahRepository(
                api = WilayahRetrofit.createApi(),
                dao = WilayahDb.get(ctx).wilayahDao()
            )
        )
    )
    val wAddr by addrVm.ui.collectAsState()

    var form by remember { mutableStateOf(initial) }
    val firebaseUser = remember {
        FirebaseAuth.getInstance().currentUser
    }

    LaunchedEffect(firebaseUser) {
        firebaseUser?.email?.let { email ->
            form = form.copy(email = email)
        }
    }


    var loadingOcr by remember { mutableStateOf(false) }
    var loadingSubmit by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }


    // validasi hp wajib 0
    val hpValid = remember(form.noHp) { form.noHp.isNotBlank() && form.noHp.startsWith("0") }
    val emailValid = remember(form.email) {
        form.email.isNotBlank() && form.email.contains("@")
    }

    LaunchedEffect(Unit) {
        birthVm.loadProvinces()
        addrVm.loadProvinces()
    }

    // ====== cascading alamat codes ======
    var addrProvinceCode by remember { mutableStateOf("") }
    var addrRegencyCode by remember { mutableStateOf("") }
    var addrDistrictCode by remember { mutableStateOf("") }

    // ====== dropdown options ======
    val jenisKelaminOptions = remember {
        listOf(
            Option("LAKI-LAKI", "L"),
            Option("PEREMPUAN", "P")
        )
    }
    val rtOptions = remember { (1..20).map { it.toString().padStart(3, '0') } }
    val rwOptions = remember { (1..20).map { it.toString().padStart(3, '0') } }
    val agamaOptions = remember { listOf("ISLAM", "KRISTEN", "KATHOLIK", "HINDU", "BUDHA", "KONGHUCHU") }
    val statusOptions = remember { listOf("BELUM KAWIN", "KAWIN", "CERAI HIDUP", "CERAI MATI") }
    val pekerjaanOptions = remember { listOf("PELAJAR/MAHASISWA", "KARYAWAN", "WIRASWASTA", "PNS", "LAINNYA") }
    val kewarganegaraanOptions = remember { listOf("WNI", "WNA") }

    // ====== Launchers: KTP camera ======
    var ktpCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val takeKtpPicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = ktpCaptureUri
        if (success && uri != null) {
            form = form.copy(ktpImageUri = uri)

            scope.launch {
                loadingOcr = true
                errorMsg = null
                try {
                    val (nik, _) = scanNikFromUri(ctx, uri)
                    if (nik != null) {
                        form = form.copy(nik = nik)
                    } else {
                        errorMsg = "NIK tidak terdeteksi. Coba foto ulang (hindari silau, zoom 2x)."
                    }
                } catch (e: Exception) {
                    errorMsg = "OCR gagal: ${e.message}"
                } finally {
                    loadingOcr = false
                }
            }
        }
    }

    val requestCameraForKtp = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(ctx, "ktp")
            ktpCaptureUri = uri
            takeKtpPicture.launch(uri)
        } else {
            errorMsg = "Izin kamera ditolak."
        }
    }

    // ====== Launchers: Profile photo camera only ======
    var profileCaptureUri by remember { mutableStateOf<Uri?>(null) }

    val takeProfilePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = profileCaptureUri
        if (success && uri != null) {
            form = form.copy(profilePhotoUri = uri)
        }
    }

    val requestCameraForProfile = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createImageUri(ctx, "profile")
            profileCaptureUri = uri
            takeProfilePicture.launch(uri)
        } else {
            errorMsg = "Izin kamera ditolak."
        }
    }

    // ====== button enabled rules ======
    val canSubmit = emailValid &&      // ⬅️ WAJIB ADA
            hpValid &&
            form.ktpImageUri != null &&
            form.profilePhotoUri != null &&
            form.nik.trim().isNotEmpty() &&
            form.nama.trim().isNotEmpty() &&
            form.tempatLahirKabKota.trim().isNotEmpty() &&
            form.tglLahir.trim().isNotEmpty() &&
            form.jenisKelamin.trim().isNotEmpty() &&
            form.provinsi.isNotBlank() &&
            form.kabupaten.isNotBlank() &&
            form.kecamatan.isNotBlank() &&
            form.kelurahan.isNotBlank()


    // ====== UI ======
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("User Data",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontSize = 18.sp,
            color = Color(0xFF4461AD),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier
            .height(12.dp))

        // KTP preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Kartu Tanda Penduduk",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable(
                            enabled = !loadingOcr && !loadingSubmit
                        ) {
                            requestCameraForKtp.launch(Manifest.permission.CAMERA)
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        // ===== Background KTP Image =====
                        Image(
                            painter = if (form.ktpImageUri != null)
                                rememberAsyncImagePainter(form.ktpImageUri)
                            else
                                painterResource(R.drawable.ktp_placeholder), // gambar KTP full
                            contentDescription = "KTP",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // ===== Overlay Center Text =====
                        if (form.ktpImageUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.9f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Scan KTP",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        // ===== Loading OCR =====
                        if (loadingOcr) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                }
            }
        }


        Spacer(Modifier.height(14.dp))

// Informasi User
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ){
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Informasi User",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.height(8.dp))
                val labelStyle = MaterialTheme.typography.labelMedium.copy(
                    color = Color.Black
                )

                val fieldShape = RoundedCornerShape(8.dp)
                // NIK
                Text("NIK", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.nik,
                    onValueChange = { form = form.copy(nik = it.filter { ch -> ch.isDigit() }) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(10.dp))

                // Nama
                Text("Nama", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.nama,
                    onValueChange = { form = form.copy(nama = it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent
                    )
                )
                Spacer(Modifier.height(10.dp))

                // No HP wajib 0
                Text("Nomor HP (wajib diawali 0)", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.noHp,
                    onValueChange = { raw ->
                        val digitsOnly = raw.filter { it.isDigit() }
                        form = form.copy(noHp = digitsOnly)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent
                    ),
                    isError = form.noHp.isNotBlank() && !form.noHp.startsWith("0"),
                    supportingText = {
                        if (form.noHp.isNotBlank() && !form.noHp.startsWith("0")) {
                            Text("Nomor HP harus diawali 0 (contoh: 08123456789)")
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))

                // NPWP opsional
                Text("NPWP (Opsional)", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.npwp,
                    onValueChange = { raw ->
                        val digitsOnly = raw.filter { it.isDigit() }
                        form = form.copy(npwp = digitsOnly)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))

                // Tempat lahir
                Text("Tempat Lahir", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                PlaceOfBirthDropdown(
                    label = "Tempat Lahir (Kab/Kota)",
                    value = form.tempatLahirKabKota,
                    provinces = wBirth.provinces,
                    regencies = wBirth.regencies,
                    loading = wBirth.loading,
                    enabled = wBirth.provinces.isNotEmpty(),
                    onPickProvince = { provinceCode -> birthVm.selectProvince(provinceCode) },
                    onPickRegency = { regencyName ->
                        form = form.copy(tempatLahirKabKota = regencyName)
                    }
                )

                // Tanggal lahir
                Text("Tanggal Lahir", style = labelStyle)
                Spacer(Modifier.height(4.dp))
                DatePickerField(
                    label = "Tanggal Lahir",
                    value = form.tglLahir,
                    onDateSelected = { picked -> form = form.copy(tglLahir = picked) }
                )

                // Jenis kelamin
                Text("Jenis Kelamin", style = labelStyle)
                Spacer(Modifier.height(4.dp))
                SimpleDropdownOption(
                    label = "Jenis Kelamin",
                    selectedValue = form.jenisKelamin,
                    options = jenisKelaminOptions,
                    enabled = true,
                    onSelected = { opt -> form = form.copy(jenisKelamin = opt.value) }
                )

                Spacer(Modifier.height(6.dp))
                Text("Alamat (Wilayah)", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))

                // Provinsi
                SimpleDropdown(
                    label = "Provinsi",
                    value = form.provinsi,
                    options = wAddr.provinces.map { it.name },
                    enabled = wAddr.provinces.isNotEmpty(),
                    onSelected = { pickedName ->
                        val picked = wAddr.provinces.first { it.name == pickedName }
                        form = form.copy(
                            provinsi = picked.name,
                            kabupaten = "",
                            kecamatan = "",
                            kelurahan = ""
                        )
                        addrProvinceCode = picked.code
                        addrRegencyCode = ""
                        addrDistrictCode = ""
                        addrVm.selectProvince(picked.code)
                    }
                )

                // Kabupaten/Kota
                SimpleDropdown(
                    label = "Kabupaten/Kota",
                    value = form.kabupaten,
                    options = wAddr.regencies.map { it.name },
                    enabled = addrProvinceCode.isNotBlank() && wAddr.regencies.isNotEmpty(),
                    onSelected = { pickedName ->
                        val picked = wAddr.regencies.first { it.name == pickedName }
                        form = form.copy(kabupaten = picked.name, kecamatan = "", kelurahan = "")
                        addrRegencyCode = picked.code
                        addrDistrictCode = ""
                        addrVm.selectRegency(picked.code)
                    }
                )

                // Kecamatan
                SimpleDropdown(
                    label = "Kecamatan",
                    value = form.kecamatan,
                    options = wAddr.districts.map { it.name },
                    enabled = addrRegencyCode.isNotBlank() && wAddr.districts.isNotEmpty(),
                    onSelected = { pickedName ->
                        val picked = wAddr.districts.first { it.name == pickedName }
                        form = form.copy(kecamatan = picked.name, kelurahan = "")
                        addrDistrictCode = picked.code
                        addrVm.selectDistrict(picked.code)
                    }
                )

                // Kelurahan/Desa
                SimpleDropdown(
                    label = "Kelurahan/Desa",
                    value = form.kelurahan,
                    options = wAddr.villages.map { it.name },
                    enabled = addrDistrictCode.isNotBlank() && wAddr.villages.isNotEmpty(),
                    onSelected = { pickedName -> form = form.copy(kelurahan = pickedName) }
                )

                // RT/RW
                SimpleDropdown("RT", form.rt, rtOptions, enabled = true) {
                    form = form.copy(rt = it)
                }
                SimpleDropdown("RW", form.rw, rwOptions, enabled = true) {
                    form = form.copy(rw = it)
                }

                // alamat detail
                Text("Alamat (Detail)", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.alamat,
                    onValueChange = { form = form.copy(alamat = it) },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(Modifier.height(10.dp))

                // lain-lain
                SimpleDropdown("Agama", form.agama, agamaOptions, enabled = true) {
                    form = form.copy(agama = it)
                }
                SimpleDropdown(
                    "Status Perkawinan",
                    form.statusPerkawinan,
                    statusOptions,
                    enabled = true
                ) { form = form.copy(statusPerkawinan = it) }
                SimpleDropdown(
                    "Pekerjaan",
                    form.pekerjaan,
                    pekerjaanOptions,
                    enabled = true
                ) { form = form.copy(pekerjaan = it) }
                SimpleDropdown(
                    "Kewarganegaraan",
                    form.kewarganegaraan,
                    kewarganegaraanOptions,
                    enabled = true
                ) { form = form.copy(kewarganegaraan = it) }

                Spacer(Modifier.height(18.dp))

        }

    }
        Spacer(Modifier.height(18.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                // Title
                Text(
                    text = "Foto Profile",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(12.dp))

                // Foto area
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            enabled = !loadingSubmit
                        ) {
                            requestCameraForProfile.launch(Manifest.permission.CAMERA)
                        },
                    contentAlignment = Alignment.Center
                ) {

                    if (form.profilePhotoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(form.profilePhotoUri),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.regisprofile),
                            contentDescription = "Placeholder",
                            modifier = Modifier
                                .size(72.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Klik untuk upload foto",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }


        Spacer(Modifier.height(20.dp))

        // ===== BUTTON LANJUT =====
        Button(
            onClick = {
                scope.launch {
                    loadingSubmit = true
                    errorMsg = null
                    successMsg = null
                    try {
                        val idToken = idTokenProvider()

                        if (form.ktpImageUri == null) {
                            errorMsg = "Foto KTP wajib."
                            return@launch
                        }
                        if (form.profilePhotoUri == null) {
                            errorMsg = "Foto profil wajib."
                            return@launch
                        }
                        if (form.tglLahir.isBlank()) {
                            errorMsg = "Tanggal lahir wajib diisi."
                            return@launch
                        }

                        // ✅ REGISTER lalu LOGIN lalu SAVE TOKEN
                        registerRepo.registerThenLogin(idToken, form)

                        successMsg = "Register + Login berhasil."
                        onSuccess() // di NavGraph arahkan ke home
                    } catch (e: Exception) {
                        errorMsg = "Submit gagal: ${e.message}"
                    } finally {
                        loadingSubmit = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit && !loadingSubmit
        ) {
            if (loadingSubmit) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Mengirim...")
            } else {
                Text("Simpan")
            }
        }
    }
}


/* -------------------- UI components -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdownOption(
    label: String,
    selectedValue: String,
    options: List<Option>,
    enabled: Boolean = true,
    onSelected: (Option) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedLabel = remember(selectedValue, options) {
        options.firstOrNull { it.value == selectedValue }?.label.orEmpty()
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt.label) },
                    onClick = {
                        onSelected(opt)
                        expanded = false
                    }
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val state = rememberDatePickerState()

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { open = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Pilih tanggal")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(10.dp))

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) onDateSelected(formatDateDdMmYyyy(millis))
                        open = false
                    }
                ) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Batal") } }
        ) {
            DatePicker(state = state)
        }
    }
}

private fun formatDateDdMmYyyy(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val day = cal.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val month = (cal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val year = cal.get(Calendar.YEAR).toString()
    return "$day-$month-$year"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompleteProfileScreenPreview() {
    MaterialTheme {
        CompleteProfileScreen(
            initial = CompleteProfileForm(
                nik = "3276010101010001",
                nama = "Supardjo",
                noHp = "081234567890",
                tempatLahirKabKota = "KOTA BANDUNG",
                tglLahir = "01-01-1990",
                jenisKelamin = "L",
                provinsi = "JAWA BARAT",
                kabupaten = "KOTA BANDUNG",
                kecamatan = "COBLONG",
                kelurahan = "DAGO",
                alamat = "Jl. Contoh No. 123",
                agama = "ISLAM",
                statusPerkawinan = "KAWIN",
                pekerjaan = "KARYAWAN",
                kewarganegaraan = "WNI",
                rt = "001",
                rw = "002"
            ),
            idTokenProvider = {
                // mock token untuk preview
                "dummy-token"
            },
            onSuccess = {
                // tidak melakukan apa-apa di preview
            }
        )
    }
}
