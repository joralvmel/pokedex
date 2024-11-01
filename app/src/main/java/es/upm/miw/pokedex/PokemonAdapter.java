package es.upm.miw.pokedex;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.stream.Collectors;
import es.upm.miw.pokedex.api.PokemonDetail;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private final List<PokemonDetail> pokemonList;

    public PokemonAdapter(List<PokemonDetail> pokemonList) {
        this.pokemonList = pokemonList;
    }

    @NonNull
    @Override
    public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pokemon, parent, false);
        return new PokemonViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull PokemonViewHolder holder, int position) {
        PokemonDetail pokemon = pokemonList.get(position);
        holder.nameTextView.setText(pokemon.getName());
        holder.numberTextView.setText(String.format("#%03d", pokemon.getId()));
        Glide.with(holder.itemView.getContext()).load(pokemon.getSprites().getFrontDefault()).into(holder.imageView);

        String types = pokemon.getTypes().stream()
                .map(type -> type.getTypeInfo().getName())
                .collect(Collectors.joining(", "));
        holder.typesTextView.setText(types);
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView numberTextView;
        TextView typesTextView;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pokemon_image);
            nameTextView = itemView.findViewById(R.id.pokemon_name);
            numberTextView = itemView.findViewById(R.id.pokemon_number);
            typesTextView = itemView.findViewById(R.id.pokemon_types);
        }
    }
}