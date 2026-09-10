package mg.annuaire.app.ui.visitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mg.annuaire.app.data.model.Avis
import mg.annuaire.app.data.model.Commune
import mg.annuaire.app.data.model.Metier
import mg.annuaire.app.data.model.PrestataireDetail
import mg.annuaire.app.data.model.PrestataireListItem
import mg.annuaire.app.data.model.Quartier
import mg.annuaire.app.data.repository.AnnuaireRepository

data class SearchUiState(
    val metiers: List<Metier> = emptyList(),
    val communes: List<Commune> = emptyList(),
    val quartiers: List<Quartier> = emptyList(),
    val selectedMetierId: Long? = null,
    val selectedQuartierId: Long? = null,
    val metierQuery: String = "",
    val quartierQuery: String = "",
    val certifiedOnly: Boolean = false,
    val availableOnly: Boolean = false,
    val results: List<PrestataireListItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(private val repository: AnnuaireRepository) : ViewModel() {
    private val filters = MutableStateFlow(
        Filters(null, null, certifiedOnly = false, availableOnly = false)
    )
    private val metierQuery = MutableStateFlow("")
    private val quartierQuery = MutableStateFlow("")

    private data class Filters(
        val metierId: Long?,
        val quartierId: Long?,
        val certifiedOnly: Boolean,
        val availableOnly: Boolean
    )

    val uiState: StateFlow<SearchUiState> = combine(
        repository.observeApprovedMetiers(),
        repository.observeCommunes(),
        repository.observeQuartiers(),
        combine(
            metierQuery,
            quartierQuery,
            filters.flatMapLatest { f ->
                repository.search(
                    metierId = f.metierId,
                    communeId = null,
                    quartierId = f.quartierId,
                    certifiedOnly = f.certifiedOnly,
                    availableOnly = f.availableOnly
                ).map { results -> f to results }
            }
        ) { mq, qq, pair -> Triple(mq, qq, pair) }
    ) { metiers, communes, quartiers, triple ->
        val (mq, qq, pair) = triple
        val (f, results) = pair
        SearchUiState(
            metiers = metiers,
            communes = communes,
            quartiers = quartiers,
            selectedMetierId = f.metierId,
            selectedQuartierId = f.quartierId,
            metierQuery = mq,
            quartierQuery = qq,
            certifiedOnly = f.certifiedOnly,
            availableOnly = f.availableOnly,
            results = results
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun updateMetierQuery(query: String) {
        metierQuery.value = query
        filters.update { it.copy(metierId = null) }
    }

    fun pickMetier(id: Long, nom: String) {
        metierQuery.value = nom
        filters.update { it.copy(metierId = id) }
    }

    fun updateQuartierQuery(query: String) {
        quartierQuery.value = query
        filters.update { it.copy(quartierId = null) }
    }

    fun pickQuartier(id: Long, nom: String) {
        quartierQuery.value = nom.substringBefore(" · ").trim()
        filters.update { it.copy(quartierId = id) }
    }

    fun toggleCertified() = filters.update { it.copy(certifiedOnly = !it.certifiedOnly) }
    fun toggleAvailable() = filters.update { it.copy(availableOnly = !it.availableOnly) }

    companion object {
        fun factory(repository: AnnuaireRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SearchViewModel(repository) as T
        }
    }
}

class DetailViewModel(
    private val repository: AnnuaireRepository,
    private val prestataireId: Long
) : ViewModel() {
    private val _detail = MutableStateFlow<PrestataireDetail?>(null)
    val detail: StateFlow<PrestataireDetail?> = _detail

    val avis: StateFlow<List<Avis>> = repository.observeAvis(prestataireId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _feedback = MutableStateFlow<String?>(null)
    val feedback: StateFlow<String?> = _feedback

    init {
        viewModelScope.launch {
            _detail.value = repository.getDetail(prestataireId)
        }
    }

    fun submitAvis(auteurNom: String, note: Int, commentaire: String) {
        viewModelScope.launch {
            _feedback.value = null
            repository.addAvis(prestataireId, auteurNom, note, commentaire)
                .onSuccess { _feedback.value = "Avis publié. Merci !" }
                .onFailure { _feedback.value = it.message }
        }
    }

    companion object {
        fun factory(repository: AnnuaireRepository, id: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                DetailViewModel(repository, id) as T
        }
    }
}
