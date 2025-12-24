package com.example.koperasi.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.koperasi.wilayah.WilayahOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceOfBirthDropdown(
    label: String,
    value: String,
    provinces: List<WilayahOption>,
    regencies: List<WilayahOption>,
    enabled: Boolean = true,
    loading: Boolean = false,
    onPickProvince: (provinceCode: String) -> Unit,
    onPickRegency: (regencyName: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(BirthStep.PROVINCE) }
    var selectedProvinceName by remember { mutableStateOf<String?>(null) }

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
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                step = BirthStep.PROVINCE
                selectedProvinceName = null
            },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            // Header (non-clickable)
            DropdownMenuItem(
                text = {
                    Text(
                        when (step) {
                            BirthStep.PROVINCE -> "Pilih Provinsi"
                            BirthStep.REGENCY -> "Pilih Kab/Kota" + (selectedProvinceName?.let { " ($it)" } ?: "")
                        }
                    )
                },
                onClick = {},
                enabled = false
            )

            // Back button when in regency step
            if (step == BirthStep.REGENCY) {
                DropdownMenuItem(
                    text = { Text("← Kembali ke Provinsi") },
                    onClick = {
                        step = BirthStep.PROVINCE
                        selectedProvinceName = null
                    }
                )
            }

            // Loading state
            if (loading) {
                DropdownMenuItem(
                    text = { Text("Loading...") },
                    onClick = {},
                    enabled = false
                )
            }

            when (step) {
                BirthStep.PROVINCE -> {
                    if (!loading && provinces.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Provinsi belum tersedia") },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        provinces.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    selectedProvinceName = p.name
                                    onPickProvince(p.code)   // trigger load regencies
                                    step = BirthStep.REGENCY
                                }
                            )
                        }
                    }
                }

                BirthStep.REGENCY -> {
                    if (!loading && regencies.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Kab/Kota belum tersedia") },
                            onClick = {},
                            enabled = false
                        )
                    } else {
                        regencies.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = {
                                    onPickRegency(r.name)
                                    expanded = false
                                    step = BirthStep.PROVINCE
                                    selectedProvinceName = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(10.dp))
}

enum class BirthStep { PROVINCE, REGENCY }
