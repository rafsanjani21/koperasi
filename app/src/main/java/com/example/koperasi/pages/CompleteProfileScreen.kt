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
import androidx.compose.remote.creation.second
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import android.widget.Toast



data class Option(val label: String, val value: String)

/**
 * Model data utama untuk menampung seluruh input form
 * yang akan dikirim ke backend saat proses registrasi.
 */
data class CompleteProfileForm(
    val ktpImageUri: Uri? = null,
    val profilePhotoUri: Uri? = null,

    val nik: String = "",
    val nama: String = "",
    val email: String = "",
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
    val kodePos: String = "",

    val alamat: String = "",
    val agama: String = "",
    val statusPerkawinan: String = "",

    // 🔥 BACKEND REQUIRED
    val pekerjaan: String = "",
    val kewarganegaraan: String = "",
    val bloodType: String = "",
    val lastEducation: String = "",
    val motherName: String = "",

    val activeAs: String = "ANGGOTA",
    val registerLocation: String = "ANDROID_APP",
    val registerId: String = ""
)


// SCREEN
@Composable
fun CompleteProfileScreen(
    initial: CompleteProfileForm = CompleteProfileForm(),
    idTokenProvider: suspend () -> String,
    onSuccess: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val fieldPositions = remember { mutableStateMapOf<String, Float>() }
    val errorFields = remember { mutableStateListOf<String>() }

    var form by remember { mutableStateOf(initial) }

    fun Modifier.trackField(key: String) = this.onGloballyPositioned {
        fieldPositions[key] = it.positionInParent().y
    }

    fun isError(key: String) = errorFields.contains(key)

    /**
     * Melakukan validasi seluruh field wajib.
     *
     * - Menandai field error
     * - Scroll otomatis ke field pertama yang error
     * - Menampilkan Toast error
     *
     * @return true jika valid, false jika ada error
     */

    fun validateAndScroll(): Boolean {
        errorFields.clear()

        val validations = listOf(
            Triple("nik", form.nik.length != 16, "NIK harus tepat 16 digit"),
            Triple("nama", form.nama.isBlank(), "Nama wajib diisi"),
            Triple(
                "noHp",
                form.noHp.length < 11 || form.noHp.length > 13,
                "Nomor HP harus 11–13 digit"
            ),
            Triple("motherName", form.motherName.isBlank(), "Nama ibu wajib diisi"),
            Triple("tempatLahirKabKota", form.tempatLahirKabKota.isBlank(), "Tempat lahir wajib"),
            Triple("tglLahir", form.tglLahir.isBlank(), "Tanggal lahir wajib"),
            Triple("jenisKelamin", form.jenisKelamin.isBlank(), "Jenis kelamin wajib"),
            Triple("provinsi", form.provinsi.isBlank(), "Provinsi wajib"),
            Triple("kabupaten", form.kabupaten.isBlank(), "Kabupaten wajib"),
            Triple("kecamatan", form.kecamatan.isBlank(), "Kecamatan wajib"),
            Triple("kelurahan", form.kelurahan.isBlank(), "Kelurahan wajib"),
            Triple("alamat", form.alamat.isBlank(), "Alamat wajib"),
            Triple("kodePos", form.kodePos.length != 5, "Kode pos harus 5 digit"),
            Triple("agama", form.agama.isBlank(), "Agama wajib"),
            Triple("statusPerkawinan", form.statusPerkawinan.isBlank(), "Status perkawinan wajib"),
            Triple("pekerjaan", form.pekerjaan.isBlank(), "Pekerjaan wajib"),
            Triple("kewarganegaraan", form.kewarganegaraan.isBlank(), "Kewarganegaraan wajib"),
            Triple("ktp", form.ktpImageUri == null, "Foto KTP wajib"),
            Triple("profile", form.profilePhotoUri == null, "Foto profil wajib")
        )

        validations
            .filter { it.second }
            .forEach { errorFields.add(it.first) }

        val firstError = validations.firstOrNull { it.second }
        if (firstError != null) {
            fieldPositions[firstError.first]?.let { y ->
                scope.launch {
                    scrollState.animateScrollTo(y.toInt())
                }
            }
            Toast.makeText(ctx, firstError.third, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

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
    var showErrorDialog by remember { mutableStateOf(false) }



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
    val pekerjaanOptions = remember { listOf("BELUM / TIDAK BEKERJA",
        "PELAJAR / MAHASISWA",
        "MENGURUS RUMAH TANGGA",
        "PENSIUNAN",

        "PEGAWAI NEGERI SIPIL (PNS)",
        "TNI",
        "POLRI",
        "KARYAWAN SWASTA",
        "KARYAWAN BUMN",
        "KARYAWAN HONORER",

        "WIRASWASTA",
        "PEDAGANG",
        "PENGUSAHA",
        "BURUH",
        "BURUH HARIAN LEPAS",
        "BURUH TANI / PERKEBUNAN",
        "PETANI",
        "PETERNAK",
        "NELAYAN",
        "PEKEBUN",

        "GURU",
        "DOSEN",
        "TENAGA PENGAJAR",
        "TENAGA KESEHATAN",
        "DOKTER",
        "PERAWAT",
        "BIDAN",
        "APOTEKER",

        "SOPIR",
        "OJEK",
        "KURIR",
        "MEKANIK",
        "TEKNISI",

        "KARYAWAN LEPAS / FREELANCER",
        "PEKERJA KREATIF",
        "PROGRAMMER",
        "DESAINER",
        "CONTENT CREATOR",

        "LAINNYA") }
    val kewarganegaraanOptions = remember { listOf("WNI", "WNA") }
    val bloodTypeOptions = remember {listOf("A", "B", "AB", "O", "-")}
    val educationOptions = remember {listOf("SD", "SMP", "SMA/SMK", "D1", "D2", "D3", "S1", "S2", "S3")}

    // ====== Launchers: KTP camera ======
    var ktpCaptureUri by remember { mutableStateOf<Uri?>(null) }

    /**
     * Launcher kamera untuk mengambil foto KTP.
     * Setelah foto berhasil:
     * - Menjalankan OCR
     * - Mengambil NIK dari gambar
     * - Mengisi field NIK otomatis
     */
    val takeKtpPicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = ktpCaptureUri
        if (!success || uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            loadingOcr = true
            errorMsg = null

            try {
                val (nikDetected, _) = scanNikFromUri(ctx, uri)

                if (!nikDetected.isNullOrBlank()) {
                    // ✅ OCR MENEMUKAN NIK (bebas panjang)
                    form = form.copy(
                        ktpImageUri = uri,
                        nik = nikDetected
                    )

                    errorFields.remove("ktp")
                    errorFields.remove("nik")

                } else {
                    // ❌ TIDAK ADA NIK → FOTO DITOLAK
                    errorMsg =
                        "NIK tidak terdeteksi. Pastikan foto jelas, tidak silau, dan posisi KTP lurus."
                    showErrorDialog = true
                }

            } catch (e: Exception) {
                errorMsg = "OCR gagal: ${e.message}"
                showErrorDialog = true
            } finally {
                loadingOcr = false
            }
        }
    }

    /**
     * Request permission kamera untuk foto KTP.
     * Jika diizinkan → buka kamera
     * Jika ditolak → tampilkan error
     */
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

    // ====== Dialogs Popup ======
    var showSuccessDialog by remember { mutableStateOf(false) }

    // ====== Launchers: Profile photo camera only ======
    var profileCaptureUri by remember { mutableStateOf<Uri?>(null) }

    /**
     * Launcher kamera untuk mengambil foto profil user.
     * Foto hanya disimpan jika capture berhasil.
     */
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
            .verticalScroll(scrollState)
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
                        .trackField("ktp")
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
                Text("NIK*", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.nik,
                    onValueChange = { input ->
                        // Ambil angka saja
                        val digits = input.filter { it.isDigit() }

                        // HARD LIMIT: tidak bisa lebih dari 16
                        if (digits.length <= 16) {
                            form = form.copy(nik = digits)

                            // Jika tepat 16 → hapus error
                            if (digits.length == 16) {
                                errorFields.remove("nik")
                            }
                        }
                    },
                    label = { Text("NIK") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackField("nik"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,

                    // 🔴 ERROR jika kurang / kosong
                    isError = isError("nik") || (form.nik.isNotEmpty() && form.nik.length != 16),

                    supportingText = {
                        if (form.nik.isNotEmpty() && form.nik.length != 16) {
                            Text(
                                text = "NIK harus tepat 16 digit",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )


                Spacer(Modifier.height(10.dp))

                // Nama
                Text("Nama*", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.nama,
                    onValueChange = { form = form.copy(nama = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackField("nama"),
                    isError = isError("nama"),
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
                Text("Nomor HP (wajib diawali 0)*", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.noHp,
                    onValueChange = { input ->
                        // Ambil angka saja
                        var digits = input.filter { it.isDigit() }

                        // Auto awali dengan 0
                        if (digits.isNotEmpty() && !digits.startsWith("0")) {
                            digits = "0$digits"
                        }

                        // Batasi maksimal 13 digit
                        if (digits.length > 13) {
                            digits = digits.take(13)
                        }

                        form = form.copy(noHp = digits)
                    },
                    label = { Text("No. HP") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackField("noHp"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    singleLine = true,
                    isError = isError("noHp") || (form.noHp.isNotEmpty() && form.noHp.length < 11),
                    supportingText = {
                        if (form.noHp.isNotEmpty() && form.noHp.length < 11) {
                            Text(
                                text = "Nomor HP harus 11–13 angka",
                                color = MaterialTheme.colorScheme.error
                            )
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

                // Nama Ibu Kandung
                Text("Nama Ibu Kandung*", style = labelStyle)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.motherName,
                    onValueChange = { form = form.copy(motherName = it) },
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


                // Tempat lahir
                Text("Tempat Lahir*", style = labelStyle)
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
                Text("Tanggal Lahir*", style = labelStyle)
                Spacer(Modifier.height(4.dp))
                DatePickerField(
                    label = "Tanggal Lahir",
                    value = form.tglLahir,
                    isError = isError("tglLahir"),
                    modifier = Modifier.trackField("tglLahir"),
                    onDateSelected = { form = form.copy(tglLahir = it) }
                )


                // Jenis kelamin
                Text("Jenis Kelamin*", style = labelStyle)
                Spacer(Modifier.height(4.dp))
                SimpleDropdownOption(
                    label = "Jenis Kelamin",
                    selectedValue = form.jenisKelamin,
                    options = jenisKelaminOptions,
                    enabled = true,
                    onSelected = { opt -> form = form.copy(jenisKelamin = opt.value) }
                )

                Spacer(Modifier.height(6.dp))
                Text("Alamat (Wilayah)*", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))

                // Provinsi
                SimpleDropdown(
                    label = "Provinsi*",
                    value = form.provinsi,
                    options = wAddr.provinces.map { it.name },
                    enabled = wAddr.provinces.isNotEmpty(),
                    modifier = Modifier.trackField("provinsi"),
                    isError = isError("provinsi"),
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
                    label = "Kabupaten/Kota*",
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
                    label = "Kecamatan*",
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
                    label = "Kelurahan/Desa*",
                    value = form.kelurahan,
                    options = wAddr.villages.map { it.name },
                    enabled = addrDistrictCode.isNotBlank() && wAddr.villages.isNotEmpty(),
                    onSelected = { pickedName -> form = form.copy(kelurahan = pickedName) }
                )

                // RT/RW
                SimpleDropdown(
                    label = "RT*",
                    value = form.rt,
                    options = rtOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(rt = selected)
                    }
                )

                SimpleDropdown(
                    label = "RW*",
                    value = form.rw,
                    options = rwOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(rw = selected)
                    }
                )


                // alamat detail
                Text("Alamat (Detail)*", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.alamat,
                    onValueChange = { form = form.copy(alamat = it) },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(Modifier.height(10.dp))

                // kode pos
                Text("Kode Pos*", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = form.kodePos,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }

                        // HARD LIMIT
                        if (digits.length <= 5) {
                            form = form.copy(kodePos = digits)

                            if (digits.length == 5) {
                                errorFields.remove("kodePos")
                            }
                        }
                    },
                    label = { Text("Kode Pos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .trackField("kodePos"),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,

                    // 🔴 ERROR jika tidak tepat 5
                    isError = isError("kodePos") || (form.kodePos.isNotEmpty() && form.kodePos.length != 5),

                    supportingText = {
                        if (form.kodePos.isNotEmpty() && form.kodePos.length != 5) {
                            Text(
                                text = "Kode pos harus tepat 5 digit",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )


                Spacer(Modifier.height(10.dp))

                // lain-lain
                SimpleDropdown(
                    label = "Agama*",
                    value = form.agama,
                    options = agamaOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(agama = selected)
                    }
                )

                SimpleDropdown(
                    label = "Status Perkawinan*",
                    value = form.statusPerkawinan,
                    options = statusOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(statusPerkawinan = selected)
                    }
                )

                SimpleDropdown(
                    label = "Pekerjaan*",
                    value = form.pekerjaan,
                    options = pekerjaanOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(pekerjaan = selected)
                    }
                )

                SimpleDropdown(
                    label = "Kewarganegaraan*",
                    value = form.kewarganegaraan,
                    options = kewarganegaraanOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(kewarganegaraan = selected)
                    }
                )

                SimpleDropdown(
                    label = "Golongan Darah*",
                    value = form.bloodType,
                    options = bloodTypeOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(bloodType = selected)
                    }
                )

                SimpleDropdown(
                    label = "Pendidikan Terakhir*",
                    value = form.lastEducation,
                    options = educationOptions,
                    enabled = true,
                    onSelected = { selected ->
                        form = form.copy(lastEducation = selected)
                    }
                )


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
                    text = "Foto Profile*",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(Modifier.height(12.dp))

                // Foto area
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .trackField("profile")
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
        /**
         * Tombol submit data.
         *
         * Proses:
         * 1. Validasi form
         * 2. Ambil Firebase ID Token
         * 3. Kirim data & foto ke backend
         * 4. Tampilkan dialog sukses
         */
        Button(
            onClick = {
                if (!validateAndScroll()) return@Button
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

                        // REGISTER + LOGIN
                        registerRepo.registerThenLogin(idToken, form)

                        // ✅ JANGAN navigate langsung
                        showSuccessDialog = true

                    } catch (e: Exception) {
                        errorMsg = e.message ?: "Terjadi kesalahan"
                        showErrorDialog = true
                    }
                    finally {
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
    if (showSuccessDialog) {
        RegisterSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                onSuccess() // 🔥 trigger navigasi ke login
            }
        )
    }
    if (showErrorDialog && errorMsg != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text("OK")
                }
            },
            title = {
                Text(
                    text = "Terjadi Kesalahan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = errorMsg!!,
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

/**
 * Dialog sukses setelah registrasi berhasil.
 * User tidak bisa dismiss dengan klik luar.
 */
@Composable
fun RegisterSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {}, // disable klik luar
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        icon = {
            Icon(
                painter = painterResource(R.drawable.verifikasi),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(64.dp)
            )
        },
        title = {
            Text(
                text = "Daftar Akun Berhasil",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Mohon tunggu KTP diverifikasi oleh admin.\nTerima kasih!",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

    )
}





/* -------------------- UI components -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleDropdown(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean = true,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false

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
            isError = isError,
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

/**
 * Field tanggal dengan DatePicker.
 * - Default tanggal: 1 Januari 2000
 * - Format output: dd-MM-yyyy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    label: String,
    value: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    onDateSelected: (String) -> Unit
) {
    var open by remember { mutableStateOf(false) }

    // 🔥 Set default ke 1 Januari 2000
    val calendar2000 = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, 2000)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val state = rememberDatePickerState(
        initialSelectedDateMillis = calendar2000.timeInMillis
        // atau:
        // initialDisplayedMonthMillis = calendar2000.timeInMillis
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        isError = isError,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { open = true }) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
            }
        },
        modifier = modifier.fillMaxWidth()
    )

    if (open) {
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onDateSelected(formatDateDdMmYyyy(it))
                    }
                    open = false
                }) {
                    Text("Pilih")
                }
            }
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "id:pixel_4"
)
@Composable
fun RegisterSuccessDialogPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33000000)), // simulasi background gelap
            contentAlignment = Alignment.Center
        ) {
            RegisterSuccessDialog(
                onDismiss = {}
            )
        }
    }
}

