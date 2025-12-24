package com.example.koperasi.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.koperasi.wilayah.WilayahOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOfBirthPicker(
    label: String,
    value: String,
    provinces: List<WilayahOption>,
    regencies: List<WilayahOption>,
    loading: Boolean,
    enabled: Boolean = true,
    onPickProvince: (provinceCode: String) -> Unit,
    onPickRegency: (regencyName: String) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(Step.PROVINCE) }
    var selectedProvinceName by remember { mutableStateOf<String?>(null) }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                open = true
                step = Step.PROVINCE
                selectedProvinceName = null
            }
    )

    if (open) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { open = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header + tombol back kalau sudah di step kab/kota
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = when (step) {
                            Step.PROVINCE -> "Pilih Provinsi"
                            Step.REGENCY -> "Pilih Kab/Kota"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (step == Step.REGENCY) {
                        TextButton(onClick = {
                            step = Step.PROVINCE
                            selectedProvinceName = null
                        }) {
                            Text("Kembali")
                        }
                    }
                }

                selectedProvinceName?.let {
                    Spacer(Modifier.height(6.dp))
                    Text("Provinsi: $it", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(12.dp))

                if (loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }

                when (step) {
                    Step.PROVINCE -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 520.dp)
                        ) {
                            items(provinces) { p ->
                                ListItem(
                                    headlineContent = { Text(p.name) },
                                    modifier = Modifier.clickable {
                                        selectedProvinceName = p.name
                                        onPickProvince(p.code) // load regencies
                                        step = Step.REGENCY
                                    }
                                )
                            }
                        }
                    }

                    Step.REGENCY -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 520.dp)
                        ) {
                            items(regencies) { r ->
                                ListItem(
                                    headlineContent = { Text(r.name) },
                                    modifier = Modifier.clickable {
                                        onPickRegency(r.name) // isi field tempatLahirKabKota
                                        open = false
                                    }
                                )
                            }
                        }

                        if (!loading && regencies.isEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text("Kab/Kota belum tersedia. Coba pilih provinsi lain atau cek koneksi.")
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

private enum class Step { PROVINCE, REGENCY }
