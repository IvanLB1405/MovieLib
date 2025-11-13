package com.movielib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.adapters.MovieAdapter
import com.movielib.base.BaseMovieActivity
import com.movielib.extensions.handle
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ActivityMainBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Pantalla principal que muestra películas populares, mejor valoradas y biblioteca del usuario
 *
 * ACTIVITY LIFECYCLE:
 * Cualquier Activity pasa por varios estados desde que se crea hasta que se destruye:
 * onCreate() → onStart() → onResume() → Running → onPause() → onStop() → onDestroy()
 *
 * CARACTERÍSTICAS:
 * - Sección hero con película destacada
 * - Listas horizontales de películas populares y mejor valoradas
 * - Sección de biblioteca personal (visible solo si no está vacía)
 * - Navegación a pantallas de búsqueda y biblioteca
 *
 * ARQUITECTURA:
 * - Extiende BaseMovieActivity para acceso al repository que gestiona
 * - Usa ViewBinding para acceso a las vistas
 * - Usa Kotlin Coroutines para operaciones asíncronas
 * - Usa Flow para streams de datos reactivos
 *
 * @see BaseMovieActivity Clase base que proporciona el repository
 */
class MainActivity : BaseMovieActivity() {

    /**
     * ViewBinding que contiene referencias type-safe a todas las vistas del layout
     *
     * LATEINIT:
     * - Se asegura que la variable será inicializada antes de usarse
     * - Permite declarar variables no-nullable sin inicializarlas en el constructor
     * - Lanzará excepción si se usa antes de inicializar
     */
    private lateinit var binding: ActivityMainBinding

    /**
     * Adapters para los tres RecyclerViews de la pantalla
     *
     * Cada adapter gestiona una lista diferente de películas:
     * - popularAdapter: Películas populares actualmente
     * - topRatedAdapter: Películas mejor valoradas por la comunidad
     * - favoritesAdapter: Películas en la biblioteca personal del usuario
     */
    private lateinit var popularAdapter: MovieAdapter
    private lateinit var topRatedAdapter: MovieAdapter
    private lateinit var favoritesAdapter: MovieAdapter

    /**
     * Primer metodo del lifecycle llamado cuando se crea la Activity
     *
     * ONCREATE SE LLAMA:
     * - Primera vez que se lanza la Activity
     * - Después de onDestroy() (recreación completa)
     * - Después de cambio de configuración (rotación de pantalla)
     *
     * ORDEN DE INICIALIZACIÓN:
     * 1. Inflar el layout con ViewBinding
     * 2. Establecer el layout como contenido de la Activity
     * 3. Configurar RecyclerViews
     * 4. Configurar listeners de clicks
     * 5. Configurar la sección hero
     * 6. Cargar datos desde la API
     *
     * @param savedInstanceState Estado guardado de la Activity para optimizar (null si es primera vez)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Llamar a la implementación de la clase padre PRIMERO
        super.onCreate(savedInstanceState)

        // Inflar el layout usando ViewBinding
        // layoutInflater viene de Activity para convertir XML en Views
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Establecer el layout como contenido de esta Activity
        // binding.root es el ViewGroup raíz del layout
        setContentView(binding.root)

        // Configurar todos los componentes en orden
        setupRecyclerViews()
        setupClickListeners()
        setupHeroSection()
        loadData()
    }

    /**
     * Llamado cuando la Activity vuelve a primer plano (se hace visible e interactiva)
     *
     * ONRESUME SE LLAMA:
     * - Después de onCreate() y onStart() (primera vez)
     * - Al volver de onPause() (ej: volviendo de otra Activity)
     * - Al volver de onStop() vía onRestart() → onStart() → onResume()
     *
     * USO HABITUAL:
     * - Reanudar animaciones pausadas
     * - Refrescar datos que podrían haber cambiado
     * - Reiniciar listeners o sensores
     *
     * AQUÍ:
     * Recargamos la biblioteca porque el usuario podría haber añadido/eliminado
     * películas en MovieDetailActivity y necesitamos refrescar la lista.
     */
    override fun onResume() {
        super.onResume()
        // Recargar favoritos porque podrían haber cambiado en otra pantalla
        loadFavorites()
    }

    private fun setupRecyclerViews() {
        popularAdapter = createHorizontalAdapter()
        topRatedAdapter = createHorizontalAdapter()
        favoritesAdapter = createHorizontalAdapter()

        setupRecyclerView(binding.popularMoviesRecyclerView, popularAdapter)
        setupRecyclerView(binding.topRatedMoviesRecyclerView, topRatedAdapter)
        setupRecyclerView(binding.favoritesRecyclerView, favoritesAdapter)
    }

    /**
     * Función auxiliar para crear adaptadores de películas horizontales (principio DRY, no repetirse!!)
     */
    private fun createHorizontalAdapter() = MovieAdapter(MovieAdapter.LayoutType.HORIZONTAL) { movie ->
        navigateToMovieDetail(movie)
    }

    /**
     * Función auxiliar para preparar RecyclerViews horizontales
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: MovieAdapter) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        binding.searchIcon.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.libraryIcon.setOnClickListener {
            startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun setupHeroSection() {
        // Lambda para obtener el ID de la película desde la sección destacada
        val getHeroMovieId = { binding.heroSection.tag as? Int }

        binding.rateButton.setOnClickListener {
            getHeroMovieId()?.let { movieId ->
                navigateToMovieDetail(movieId)
            }
        }

        binding.favoriteButton.setOnClickListener {
            getHeroMovieId()?.let { movieId ->
                lifecycleScope.launch {
                    repository.addToLibrary(movieId)
                    loadFavorites()
                }
            }
        }
    }

    private fun loadData() {
        showLoading()
        loadPopularMovies()
        loadTopRatedMovies()
        loadFavorites()
    }

    /**
     * Carga las películas populares desde el repository
     *
     * KOTLIN COROUTINES:
     * Hilos ligeros de Kotlin
     *
     * LIFECYCLESCOPE:
     * - Scope ligado al lifecycle de la Activity
     * - Cancela automáticamente las coroutines cuando la Activity se destruye
     * - Evita fugas de memoria y crasheos por usar Activities destruidas
     *
     * LAUNCH:
     * - Inicia una nueva coroutine (no bloquea el hilo principal)
     * - No retorna resultado
     * - Para retornar resultado se usa "async" + "await"
     *
     * FLOW:
     * - Stream de datos asíncrono (como RxJava Observable)
     * - Emite múltiples valores a lo largo del tiempo
     * - Flujo frio: no emite hasta que alguien lo pide, si no, seria un leak de datos constante
     *
     * COLLECT:
     * - Terminal operator que "consume" el Flow
     * - Función suspendible que recibe cada valor emitido
     * - Se ejecuta solo en el contexto de la coroutine (thread seguro)
     *
     * FLUJO DE EJECUCIÓN:
     * 1. lifecycleScope.launch{} crea una coroutine ligada a la Activity
     * 2. repository.getPopularMovies() retorna un Flow<ApiResponse<List<Movie>>>
     * 3. .collect{} empieza a recibir valores del Flow
     * 4. Primero emite ApiResponse.Loading
     * 5. Luego emite ApiResponse.Success con las películas (o Error/NetworkError)
     * 6. response.handle{} procesa cada estado emitido
     * 7. onSuccess actualiza el adapter y muestra la primera película en hero
     */
    private fun loadPopularMovies() {
        // Iniciar coroutine ligada al lifecycle de la Activity
        lifecycleScope.launch {
            // Obtener Flow de películas populares
            repository.getPopularMovies().collect { response ->
                // Manejar cada estado emitido por el Flow usando extension function
                response.handle(
                    onSuccess = { movies ->
                        // Actualizar el adapter con la lista de películas
                        // submitList() usa DiffUtil automáticamente (eficiente)
                        popularAdapter.submitList(movies)

                        // Si hay películas, mostrar la primera en la sección hero
                        if (movies.isNotEmpty()) {
                            displayHeroMovie(movies.first())
                        }

                        // Ocultar indicador de carga
                        hideLoading()
                    },
                    onError = { _, _ -> hideLoading() },      // Ocultar loading en error
                    onNetworkError = { hideLoading() }        // Ocultar loading en error de red
                )
            }
        }
    }

    private fun loadTopRatedMovies() {
        lifecycleScope.launch {
            repository.getTopRatedMovies().collect { response ->
                response.handle(
                    onSuccess = { movies ->
                        topRatedAdapter.submitList(movies)
                    }
                )
            }
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            repository.getLibraryMoviesFlow().collectLatest { movies ->
                binding.favoritesSection.visibility = if (movies.isNotEmpty()) {
                    favoritesAdapter.submitList(movies)
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun displayHeroMovie(movie: Movie) {
        binding.heroSection.tag = movie.id
        binding.heroTitle.text = movie.title
        binding.heroOverview.text = movie.overview

        val backdropUrl = Constants.buildPosterUrl(
            movie.posterPath,
            Constants.IMAGE_SIZE_W780
        )

        Glide.with(this)
            .load(backdropUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.placeholder_movie)
            .error(R.drawable.placeholder_movie)
            .into(binding.heroBackdrop)
    }

    private fun navigateToMovieDetail(movie: Movie) {
        navigateToMovieDetail(movie.id)
    }

    private fun navigateToMovieDetail(movieId: Int) {
        val intent = Intent(this, MovieDetailActivity::class.java).apply {
            putExtra(SearchActivity.EXTRA_MOVIE_ID, movieId)
        }
        startActivity(intent)
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
}
