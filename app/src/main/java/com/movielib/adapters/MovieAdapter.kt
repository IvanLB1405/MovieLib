package com.movielib.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.movielib.R
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants

/**
 * Adapter para mostrar películas en un RecyclerView con soporte para diferentes diseños
 *
 * RECYCLERVIEW PATTERN:
 * RecyclerView es un componente de Android para mostrar listas eficientemente.
 * A diferencia de ListView antiguo, RecyclerView reutiliza las vistas (Views) en lugar
 * de crear una nueva para cada item, lo que mejora el rendimiento.
 *
 * LISTADAPTER vs RECYCLERVIEW.ADAPTER:
 * ListAdapter extiende RecyclerView.Adapter y añade:
 * - Gestión automática de diferencias (DiffUtil) para updates eficientes
 * - Método submitList() para actualizar la lista de forma asíncrona
 * - No necesita notifyDataSetChanged() manual
 *
 * DIFFUTIL:
 * Calcula qué items cambiaron, se añadieron o eliminaron,
 * y anima solo esos cambios en lugar de recrear toda la lista.
 *
 * @param layoutType Tipo de diseño a usar (horizontal para carruseles, grid para cuadrículas)
 * @param onMovieClick Callback que se ejecuta cuando el usuario toca una película
 *
 * @see MovieViewHolder ViewHolder que contiene las vistas de cada item
 * @see MovieDiffCallback Lógica para calcular diferencias entre listas
 */
class MovieAdapter(
    private val layoutType: LayoutType,
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) {

    /**
     * Tipos de layout disponibles para mostrar películas
     */
    enum class LayoutType {
        HORIZONTAL,  // Para listas horizontales (carruseles) - item_movie_horizontal.xml
        GRID         // Para cuadrículas (grids) - item_movie_grid.xml
    }

    /**
     * Crea un nuevo ViewHolder cuando RecyclerView necesita uno
     *
     * Esta función se llama SOLO cuando RecyclerView necesita crear una nueva vista
     * (no hay vistas reciclables disponibles). Una vez creada, la vista se reutiliza
     * para diferentes items mediante onBindViewHolder().
     *
     * PROCESO:
     * 1. RecyclerView determina que necesita mostrar un nuevo item
     * 2. Llama a esta función si no hay vistas reciclables
     * 3. Inflamos el XML del layout apropiado
     * 4. Creamos y retornamos un ViewHolder que envuelve la vista
     *
     * @param parent ViewGroup padre (el RecyclerView)
     * @param viewType Tipo de vista (no usado aquí, pero útil para adapters con múltiples tipos)
     * @return ViewHolder recién creado listo para ser vinculado con datos
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        // Seleccionar el layout XML según el tipo configurado en el constructor
        val layoutId = when (layoutType) {
            LayoutType.HORIZONTAL -> R.layout.item_movie_horizontal
            LayoutType.GRID -> R.layout.item_movie_grid
        }

        // Inflar el layout XML para convertirlo en una View de Android
        // LayoutInflater convierte XML en objetos View que Android puede renderizar
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        // Crear y retornar el ViewHolder que contendrá esta vista
        return MovieViewHolder(view)
    }

    /**
     * Vincula datos de una película con un ViewHolder existente
     *
     * Esta función se llama cada vez que RecyclerView necesita mostrar un item:
     * - Cuando se muestra por primera vez
     * - Cuando se recicla una vista (scroll)
     * - Cuando los datos cambian (submitList con DiffUtil)
     *
     * Es MUY IMPORTANTE que esta función sea rápida porque se llama frecuentemente.
     * Operaciones pesadas aquí causarán lag en el scroll.
     *
     * @param holder ViewHolder a vincular (puede ser nuevo o reciclado)
     * @param position Posición del item en la lista (0-indexed)
     */
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        // Obtener la película en esta posición (ListAdapter maneja la lista internamente)
        val movie = getItem(position)
        // Delegar al ViewHolder para que vincule los datos con las vistas
        holder.bind(movie, onMovieClick)
    }

    /**
     * ViewHolder que contiene las vistas de un item de película
     *
     * PATRÓN VIEWHOLDER:
     * ViewHolder cachea las referencias a las vistas de un item.
     * Sin ViewHolder, findViewById() se llamaría en cada bind(), lo cual es MUY LENTO.
     *
     * VENTAJAS:
     * - findViewById() se llama solo una vez (en el constructor)
     * - Las referencias se reutilizan cuando se recicla la vista
     * - Mejora el rendimiento del scroll
     *
     * @param itemView Vista raíz del layout de un item individual
     */
    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cachear referencias a las vistas para evitar findViewById() repetidos
        private val moviePoster: ImageView = itemView.findViewById(R.id.moviePoster)
        private val movieTitle: TextView = itemView.findViewById(R.id.movieTitle)
        private val movieRating: TextView = itemView.findViewById(R.id.movieRating)
        private val favoriteIndicator: ImageView = itemView.findViewById(R.id.favoriteIndicator)

        /**
         * Vincula los datos de una película con las vistas de este ViewHolder
         *
         * Esta función actualiza SOLO el contenido de las vistas, no las crea.
         * Las vistas ya están creadas y cacheadas desde el constructor.
         *
         * @param movie Datos de la película a mostrar
         * @param onMovieClick Callback a ejecutar cuando el usuario toca el item
         */
        fun bind(movie: Movie, onMovieClick: (Movie) -> Unit) {
            // Establecer el título de la película
            movieTitle.text = movie.title

            // Formatear y mostrar la puntuación con 1 decimal
            movieRating.text = String.format("%.1f", movie.voteAverage)

            // Construir URL completa del póster combinando base URL + tamaño + path
            val posterUrl = Constants.buildPosterUrl(
                movie.posterPath,    // Path relativo de TMDb
                Constants.IMAGE_SIZE_W342  // Tamaño de imagen (342px de ancho)
            )

            // GLIDE: Librería de carga de imágenes con características avanzadas:
            // - Caché automática (memoria)
            // - Redimensionamiento automático
            // - Placeholders mientras carga
            // - Manejo de errores
            Glide.with(itemView.context)
                .load(posterUrl)  // URL de la imagen a cargar
                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cachear imagen original y transformada
                .placeholder(R.drawable.placeholder_movie)  // Imagen mientras carga
                .error(R.drawable.placeholder_movie)        // Imagen si falla la carga
                .into(moviePoster)  // ImageView destino

            // Mostrar u ocultar el indicador de favorito según si está en biblioteca
            // Usamos visibilidad condicional con un if
            favoriteIndicator.visibility = if (movie.isInLibrary) View.VISIBLE else View.GONE

            // Configurar listener para detectar clicks en todo el item
            // Cuando el usuario toca el item, ejecutar el callback con esta película
            itemView.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    /**
     * Callback para calcular diferencias entre dos listas de películas
     *
     * DIFFUTIL:
     * DiffUtil algoritmo que calcula eficientemente las diferencias
     * entre dos listas y genera el set mínimo de operaciones para actualizar una lista a otra.
     *
     * VENTAJAS sobre notifyDataSetChanged():
     * - Solo actualiza items que realmente cambiaron
     * - Anima automáticamente los cambios (añadir/eliminar/mover)
     * - Evita parpadeos de pantalla
     * - Mejor rendimiento con listas grandes
     *
     * FUNCIONAMIENTO:
     * 1. submitList() recibe una nueva lista
     * 2. DiffUtil compara la lista vieja con la nueva usando estos callbacks
     * 3. Calcula el set mínimo de cambios
     * 4. Aplica los cambios con animaciones
     */
    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        /**
         * Determina si dos items representan la misma entidad
         *
         * Esto NO verifica si el contenido es igual, solo si representan
         * el mismo objeto (misma movie). Se usa el ID único.
         *
         * EJEMPLO:
         * - Movie(id=1, title="Inception", rating=8.0) vs
         * - Movie(id=1, title="Inception", rating=9.0)
         * → areItemsTheSame = TRUE (mismo ID, aunque el rating cambió)
         *
         * @return true si representan la misma película (mismo ID)
         */
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Determina si dos items tienen exactamente el mismo contenido
         *
         * Solo se llama si areItemsTheSame() retornó true.
         * Verifica si TODOS los campos son iguales.
         *
         * EJEMPLO:
         * - Movie(id=1, title="Inception", rating=8.0) vs
         * - Movie(id=1, title="Inception", rating=9.0)
         * → areContentsTheSame = FALSE (el rating cambió)
         *
         * Kotlin genera automáticamente equals() para data classes que compara
         * todos los campos, por eso simplemente usamos oldItem == newItem.
         *
         * @return true si TODO el contenido es idéntico
         */
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}

