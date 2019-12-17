package com.volovich.afisha.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.volovich.afisha.Event;
import com.volovich.afisha.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.MarkedEventViewHolder> {

    private ArrayList<Event> markedEvents;
    private Context context;

    public WishlistAdapter(Context context) {
        this.context = context;
        this.markedEvents = new ArrayList<>();
    }

    public void addEvent(Event event) {
        markedEvents.add(event);
        //notifyItemInserted(markedEvents.size() - 1);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MarkedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_marked_event, parent, false);
        return new MarkedEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MarkedEventViewHolder holder, int position) {
        holder.bind(markedEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return markedEvents.size();
    }

    class MarkedEventViewHolder extends RecyclerView.ViewHolder {


        private TextView markedEventTitleTextView;
        private TextView markedEventPlaceTextView;

        private TextView markedEventDateTextView;
        private TextView markedEventPriceTextView;

        MarkedEventViewHolder(View itemView) {
            super(itemView);
            markedEventTitleTextView = itemView.findViewById(R.id.marked_event_title_text_view);
            markedEventPlaceTextView = itemView.findViewById(R.id.marked_event_place_text_view);
            markedEventDateTextView = itemView.findViewById(R.id.marked_event_date_text_view);
            markedEventPriceTextView = itemView.findViewById(R.id.marked_event_price_text_view);
        }

        void bind(final Event markedEvent) {

            Date eventDate = markedEvent.getDate().toDate();
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm\ndd.MM.yyyy", Locale.ENGLISH);

            markedEventTitleTextView.setText(markedEvent.getTitle());
            markedEventPlaceTextView.setText(markedEvent.getPlace());
            markedEventDateTextView.setText(dateFormat.format(eventDate));
            markedEventPriceTextView.setText(String.format(context.getString(R.string.wishlist_price_value)
                    , markedEvent.getCount()
                    , markedEvent.getPrice()
                    , markedEvent.getCount() * markedEvent.getPrice()));
        }
    }

    public void removeEventFromWishlist(/*Event markedEvent*/ int position) {

        FirebaseFirestore.getInstance()
                .collection(context.getString(R.string.firestore_collection_wishlists))
                .document(markedEvents.get(position).getWishListDocumentId())
                .delete();
        markedEvents.remove(position);
        notifyItemRemoved(position);
        //notifyDataSetChanged();
    }

}

