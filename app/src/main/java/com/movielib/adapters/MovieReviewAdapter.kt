package com.movielib.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.movielib.movielib.R
import com.movielib.movielib.databinding.ItemMovieReviewBinding
import com.movielib.movielib.models.Movie
import com.movielib.movielib.utils.Constants

/**
 * Adapter para mostrar películas que tienen reseñas del usuario
 *
 * Similar a MovieAdapter pero usa un layout diferente (ItemMovieReviewBinding) que incluye
 * espacio para mostrar la reseña completa del usuario además del título y rating.
 *
 * VIEWBINDING:
 * Este adapter usa ViewBinding en lugar de findViewById():
 * - ViewBinding genera automáticamente una clase con referencias type-safe a todas las vistas
 * - Evita NullPointerException (tipo seguro en tiempo de compilación)
 * - Más eficiente y seguro que findViewById()
 *
 * @param onMovieClick Callback que se ejecuta cuando el usuario toca una película con reseña
 *
 * @see MovieAdapter Para el adapter de películas general
 * @see ItemMovieReviewBinding Binding generado automáticamente desde item_movie_review.xml
 */
class MovieReviewAdapter(
    private val onMovieClick: (Movie) -> Unit
) : ListAdapter<Movie, MovieReviewAdapter.ReviewViewHolder>(MovieDiffCallback()) {

    /**
     * Crea un nuevo ViewHolder para películas con reseñas
     *
     * DIFERENCIA CON MovieAdapter:
     * Aquí usamos ViewBinding (ItemMovieReviewBinding) en lugar de inflar manualmente el XML.
     * ViewBinding genera una clase binding que contiene referencias a todas las vistas del layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Inflar el layout usando ViewBinding (método estático inflate())
        val binding = ItemMovieReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false  // No adjuntar inmediatamente al parent (RecyclerView lo hace después)
        )
        // Crear el ViewHolder pasándole el binding (que contiene todas las vistas)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder para items de películas con reseñas
     *
     * INNER CLASS:
     * Usamos "inner class" en lugar de "class" normal para tener acceso a los miembros
     * de la clase externa (MovieReviewAdapter), específicamente a onMovieClick.
     *
     * @param binding Objeto generado por ViewBinding con referencias a todas las vistas
     */
    inner class ReviewViewHolder(
        private val binding: ItemMovieReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Vincula los datos de una película (con reseña) a las vistas
         *
         * @param movie Película que debe tener userReview no nulo
         */
        fun bind(movie: Movie) {
            // Establecer el título de la película
            binding.titleTextView.text = movie.title

            // Mostrar el rating del usuario si existe
            // let{} se ejecuta solo si userRating no es null (safe call)
            movie.userRating?.let { rating ->
                // Formatear como "★ 8.5" con estrella unicode
                binding.userRatingTextView.text = "★ ${String.format("%.1f", rating)}"
            }

            // Mostrar la reseña del usuario (o string vacío si es null)
            // Usamos elvis operator ?: para proporcionar valor por defecto
            binding.reviewTextView.text = movie.userReview ?: ""

            // Construir y cargar la URL del póster con Glide
            val posterUrl = Constants.buildPosterUrl(
                movie.posterPath,
                Constants.IMAGE_SIZE_W342
            )

            Glide.with(binding.posterImageView.context)
                .load(posterUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.placeholder_movie)
                .into(binding.posterImageView)

            // Configurar click listener en la vista raíz (todo el item es clickeable)
            // Como ReviewViewHolder es inner class, tiene acceso a onMovieClick de la clase externa
            binding.root.setOnClickListener {
                onMovieClick(movie)
            }
        }
    }

    /**
     * Callback de DiffUtil para calcular diferencias entre listas
     *
     * PRIVATE CLASS vs INNER CLASS:
     * Usamos "private class" aquí porque NO necesita acceso a la clase externa.
     * Es más eficiente que inner class porque no mantiene una referencia a la clase externa.
     */
    private class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
