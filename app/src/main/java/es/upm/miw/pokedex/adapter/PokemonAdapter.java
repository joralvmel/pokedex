package es.upm.miw.pokedex.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import es.upm.miw.pokedex.R;
import es.upm.miw.pokedex.api.PokemonDetail;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private List<PokemonDetail> pokemonList;

    public PokemonAdapter(List<PokemonDetail> pokemonList) {
        this.pokemonList = pokemonList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setPokemonList(List<PokemonDetail> pokemonList) {
        this.pokemonList = pokemonList;
        notifyDataSetChanged();
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

        holder.typesContainer.removeAllViews();
        for (PokemonDetail.Type type : pokemon.getTypes()) {
            TextView typeTextView = new TextView(holder.itemView.getContext());
            typeTextView.setText(type.getTypeInfo().getName().toUpperCase());
            typeTextView.setPadding(4, 4, 4, 4);
            typeTextView.setTextColor(Color.WHITE);
            typeTextView.setTextSize(12);
            typeTextView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.type_background));
            typeTextView.getBackground().setTint(getTypeColor(holder.itemView.getContext(), type.getTypeInfo().getName()));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            typeTextView.setLayoutParams(params);

            holder.typesContainer.addView(typeTextView);
        }
    }

    @Override
    public int getItemCount() {
        return pokemonList.size();
    }

    public static class PokemonViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView;
        TextView numberTextView;
        LinearLayout typesContainer;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pokemon_image);
            nameTextView = itemView.findViewById(R.id.pokemon_name);
            numberTextView = itemView.findViewById(R.id.pokemon_number);
            typesContainer = itemView.findViewById(R.id.pokemon_types_container);
        }
    }

    private int getTypeColor(Context context, String typeName) {
        int colorId;
        switch (typeName.toLowerCase()) {
            case "normal":
                colorId = R.color.type_normal;
                break;
            case "fire":
                colorId = R.color.type_fire;
                break;
            case "water":
                colorId = R.color.type_water;
                break;
            case "electric":
                colorId = R.color.type_electric;
                break;
            case "grass":
                colorId = R.color.type_grass;
                break;
            case "ice":
                colorId = R.color.type_ice;
                break;
            case "fighting":
                colorId = R.color.type_fighting;
                break;
            case "poison":
                colorId = R.color.type_poison;
                break;
            case "ground":
                colorId = R.color.type_ground;
                break;
            case "flying":
                colorId = R.color.type_flying;
                break;
            case "psychic":
                colorId = R.color.type_psychic;
                break;
            case "bug":
                colorId = R.color.type_bug;
                break;
            case "rock":
                colorId = R.color.type_rock;
                break;
            case "ghost":
                colorId = R.color.type_ghost;
                break;
            case "dragon":
                colorId = R.color.type_dragon;
                break;
            case "dark":
                colorId = R.color.type_dark;
                break;
            case "steel":
                colorId = R.color.type_steel;
                break;
            case "fairy":
                colorId = R.color.type_fairy;
                break;
            default:
                colorId = R.color.type_default;
                break;
        }
        return ContextCompat.getColor(context, colorId);
    }
}