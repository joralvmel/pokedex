package es.upm.miw.pokedex.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder> {
    private List<PokemonDetail> pokemonList;
    private final SparseBooleanArray favoriteStatusArray;
    private final DatabaseReference databaseReference;
    private final FirebaseUser currentUser;

    public PokemonAdapter(List<PokemonDetail> pokemonList) {
        this.pokemonList = pokemonList;
        this.favoriteStatusArray = new SparseBooleanArray();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("favorites");
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favoriteStatusArray.clear();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        int pokemonId = Integer.parseInt(childSnapshot.getKey());
                        boolean isFavorite = Boolean.TRUE.equals(childSnapshot.getValue(Boolean.class));
                        favoriteStatusArray.put(pokemonId, isFavorite);
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }
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

        boolean isFavorite = favoriteStatusArray.get(pokemon.getId(), false);
        holder.heartIconEmpty.setVisibility(isFavorite ? View.GONE : View.VISIBLE);
        holder.heartIconFilled.setVisibility(isFavorite ? View.VISIBLE : View.GONE);

        View.OnClickListener toggleFavoriteListener = v -> {
            boolean newFavoriteStatus = !favoriteStatusArray.get(pokemon.getId(), false);
            favoriteStatusArray.put(pokemon.getId(), newFavoriteStatus);
            holder.heartIconEmpty.setVisibility(newFavoriteStatus ? View.GONE : View.VISIBLE);
            holder.heartIconFilled.setVisibility(newFavoriteStatus ? View.VISIBLE : View.GONE);

            if (currentUser != null) {
                databaseReference.child(currentUser.getUid()).child(String.valueOf(pokemon.getId())).setValue(newFavoriteStatus);
            }
        };

        holder.heartIconEmpty.setOnClickListener(toggleFavoriteListener);
        holder.heartIconFilled.setOnClickListener(toggleFavoriteListener);
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
        ImageView heartIconEmpty;
        ImageView heartIconFilled;

        public PokemonViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.pokemon_image);
            nameTextView = itemView.findViewById(R.id.pokemon_name);
            numberTextView = itemView.findViewById(R.id.pokemon_number);
            typesContainer = itemView.findViewById(R.id.pokemon_types_container);
            heartIconEmpty = itemView.findViewById(R.id.heart_icon_empty);
            heartIconFilled = itemView.findViewById(R.id.heart_icon_filled);
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