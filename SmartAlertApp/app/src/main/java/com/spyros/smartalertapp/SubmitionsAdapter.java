package com.spyros.smartalertapp;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class SubmitionsAdapter extends FirestoreRecyclerAdapter<submitions , SubmitionsAdapter.SubmitionsHolder> {
    public static OnItemClickListener listener;
    public SubmitionsAdapter(@NonNull FirestoreRecyclerOptions<submitions> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull SubmitionsHolder holder, int position, @NonNull submitions model) {

        holder.titleTextView.setText(model.getDangerType());
        holder.timestampTextView.setText(String.valueOf(model.getCurrentTime()));
        holder.priorityTextView.setText("Προτεραιότητα: " + String.valueOf(model.getPriority()));

        String DangerType = model.getDangerType();
        if (DangerType.equals("Πυρκαγιά") || DangerType.equals("Fire")) {
            holder.eventImageView.setImageResource(R.drawable.fire_logo);
        }
        else if (DangerType.equals("Πλυμμήρα")|| DangerType.equals("Flood")){
            holder.eventImageView.setImageResource(R.drawable.flood_logo);
        }
        else if (DangerType.equals("Σεισμός")|| DangerType.equals("Earthquake")){
            holder.eventImageView.setImageResource(R.drawable.earthquake_logo);
        }
        else if (DangerType.equals("Καταιγίδα")|| DangerType.equals("Storm")){
            holder.eventImageView.setImageResource(R.drawable.storm_logo);
        }
        else if (DangerType.equals("Ανεμοστρόβηλος")|| DangerType.equals("Tornado")){
            holder.eventImageView.setImageResource(R.drawable.tornado_logo);
        }
    }

    @NonNull
    @Override
    public SubmitionsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row,
                parent,false );
        return new SubmitionsHolder(v);
    }

    class SubmitionsHolder extends RecyclerView.ViewHolder{

        TextView titleTextView;
        TextView timestampTextView;
        TextView priorityTextView;
        ImageView eventImageView;

        public SubmitionsHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
            priorityTextView = itemView.findViewById(R.id.priorityTextView);
            eventImageView = itemView.findViewById(R.id.eventImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position) , position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position );
    }

    public void setOnItemClickListener (OnItemClickListener listener){
        this.listener = listener;
    }

}
