package com.example.koperasi.wilayah

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WilayahUiState(
    val provinces: List<WilayahOption> = emptyList(),
    val regencies: List<WilayahOption> = emptyList(),
    val districts: List<WilayahOption> = emptyList(),
    val villages: List<WilayahOption> = emptyList(),

    // ✅ NEW: Kab/Kota seluruh Indonesia (untuk tempat lahir independen)
    val allRegencies: List<WilayahOption> = emptyList(),

    val loading: Boolean = false,
    val error: String? = null
)

class WilayahViewModel(
    private val repo: WilayahRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(WilayahUiState())
    val ui = _ui.asStateFlow()

    fun loadProvinces() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        runCatching { repo.getProvinces() }
            .onSuccess { _ui.value = _ui.value.copy(provinces = it, loading = false) }
            .onFailure { _ui.value = _ui.value.copy(error = it.message, loading = false) }
    }

    // ✅ NEW
    fun loadAllRegenciesForBirthPlace() = viewModelScope.launch {
        _ui.value = _ui.value.copy(loading = true, error = null)
        runCatching { repo.getAllRegenciesForBirthPlace() }
            .onSuccess { _ui.value = _ui.value.copy(allRegencies = it, loading = false) }
            .onFailure { _ui.value = _ui.value.copy(error = it.message, loading = false) }
    }

    fun selectProvince(provinceCode: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(
            regencies = emptyList(), districts = emptyList(), villages = emptyList(),
            loading = true, error = null
        )
        runCatching { repo.getRegencies(provinceCode) }
            .onSuccess { _ui.value = _ui.value.copy(regencies = it, loading = false) }
            .onFailure { _ui.value = _ui.value.copy(error = it.message, loading = false) }
    }

    fun selectRegency(regencyCode: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(districts = emptyList(), villages = emptyList(), loading = true, error = null)
        runCatching { repo.getDistricts(regencyCode) }
            .onSuccess { _ui.value = _ui.value.copy(districts = it, loading = false) }
            .onFailure { _ui.value = _ui.value.copy(error = it.message, loading = false) }
    }

    fun selectDistrict(districtCode: String) = viewModelScope.launch {
        _ui.value = _ui.value.copy(villages = emptyList(), loading = true, error = null)
        runCatching { repo.getVillages(districtCode) }
            .onSuccess { _ui.value = _ui.value.copy(villages = it, loading = false) }
            .onFailure { _ui.value = _ui.value.copy(error = it.message, loading = false) }
    }
}
