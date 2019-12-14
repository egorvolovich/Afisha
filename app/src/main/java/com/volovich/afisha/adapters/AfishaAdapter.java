package com.volovich.afisha.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.volovich.afisha.AfishaActivity;
import com.volovich.afisha.Event;
import com.volovich.afisha.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AfishaAdapter extends RecyclerView.Adapter<AfishaAdapter.EventViewHolder> {

    private ArrayList<Event> events;
    private Context context;

    public AfishaAdapter(Context context, ArrayList<Event> events) {
        this.context = context;
        this.events = events;
    }

    public void addEvent(Event event) {
        events.add(event);
        Log.d("logs", "added");
        checkIfEventIsInWishlist(event);
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        holder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {

        private ImageView eventImageView;
        private TextView eventTitleTextView;
        private TextView eventDescriptionTextView;
        private TextView eventPlaceTextView;
        private TextView eventAddressTextView;
        private TextView eventDateTextView;
        private TextView eventPriceTextView;
        private Button eventMarkButton;

        EventViewHolder(View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.event_image_view);
            eventTitleTextView = itemView.findViewById(R.id.event_title_text_view);
            eventDescriptionTextView = itemView.findViewById(R.id.event_description_text_view);
            eventPlaceTextView = itemView.findViewById(R.id.event_place_text_view);
            eventAddressTextView = itemView.findViewById(R.id.event_address_text_view);
            eventDateTextView = itemView.findViewById(R.id.event_date_text_view);
            eventPriceTextView = itemView.findViewById(R.id.event_price_text_view);
            eventMarkButton = itemView.findViewById(R.id.event_mark_button);
        }

        void bind(final Event event) {

            Date eventDate = event.getDate().toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm, dd.MM.yyyy", Locale.ENGLISH);

            Picasso.get().load(event.getImageURL()).centerCrop().fit().into(eventImageView);
            eventTitleTextView.setText(event.getTitle());
            eventDescriptionTextView.setText(event.getDescription());
            eventPlaceTextView.setText(event.getPlace());
            eventAddressTextView.setText(event.getAddress());
            eventDateTextView.setText(dateFormat.format(eventDate));
            eventPriceTextView.setText(String.format(context.getString(R.string.event_price_value), event.getPrice()));
            setButtonMark(event.isMark());
            eventMarkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addEventToWishlist(event);
                    event.setMark(true);
                    setButtonMark(event.isMark());
                }
            });

        }

        private void setButtonMark(boolean mark) {
            eventMarkButton.setClickable(!mark);
            if (mark) eventMarkButton.setText(context.getText(R.string.event_added));
            else eventMarkButton.setText(context.getString(R.string.add_event));
//            notifyDataSetChanged();
        }

        private void addEventToWishlist(Event event) {
            Map<String, Object> data = new HashMap<>();
            data.put(context.getString(R.string.firestore_field_uid), AfishaActivity.getUID());
            data.put(context.getString(R.string.firestore_field_event_id), event.getDocumentId());
            FirebaseFirestore.getInstance()
                    .collection(context.getString(R.string.firestore_collection_wishlists))
                    .document().set(data);
        }
    }

    private void checkIfEventIsInWishlist(final Event event) {

        FirebaseFirestore.getInstance()
                .collection(context.getString(R.string.firestore_collection_wishlists))
                .whereEqualTo(context.getString(R.string.firestore_field_event_id), event.getDocumentId())
                .whereEqualTo(context.getString(R.string.firestore_field_uid), AfishaActivity.getUID())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                //if query task brings a result which size is not 0, it means that user marked this event previously
                if (task.isSuccessful() && task.getResult() != null && task.getResult().size() != 0) {
                    event.setMark(true);
                    Log.d("logs", "true");
                } else {
                    event.setMark(false);
                    Log.d("logs", "false");
                }
                Log.d("logs", "marked");
                //needs to update displaying of recycler view
                notifyItemChanged(events.indexOf(event));
                notifyDataSetChanged();
            }
        });
    }

}
